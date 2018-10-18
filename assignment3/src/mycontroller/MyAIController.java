package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;


import com.badlogic.gdx.utils.Queue;

import controller.CarController;
import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial.Direction;


public class MyAIController extends CarController {

	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;
	// Car Speed to move at 5
	private final int CAR_MAX_SPEED = 5;
	// Car can stop at a certain position.
	private static boolean stop = false;

	private HashSet<Coordinate> health = new HashSet<Coordinate>(); // record the health trap we explored.
	private Queue<Coordinate> previous = new Queue<Coordinate>(); // record the last position.
	
	HashMap<Coordinate, MapTile> currentMap; // the updated map record all the place we explored.

	private GoalMaker currGoal;
	private Coordinate currDistination;
	private DetectAroundSensor sensor;
	
	private static List<Coordinate> roadCandidates = new ArrayList<>();

	public MyAIController(Car car) {
		super(car);
		sensor = new DetectAroundSensor(wallSensitivity, car);
		currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap(), car);
		currentMap = getMap();
		Coordinate initialPrePos = new Coordinate(-1,-1);
		previous.addFirst(initialPrePos);
		
	}


	@Override
	public void update() {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		HashMap<Coordinate, MapTile> mapTest = getMap();
		currDistination = currGoal.getCurrGoal();
		
		
		updateMap(currentView, health);
		


		Coordinate currentPosition = new Coordinate(getPosition());	
//		currGoal.updateGoal(currentView);
//		Object currentGoal = currGoal.getGoal(currentMap, currentPosition);
		
		if (currentPosition.equals(currDistination)) {
			currGoal.reachedTheGoal(currentPosition);
			currDistination = currGoal.getCurrGoal();
		}
		if (checkShouldBrake(currentView, new Coordinate(getPosition()))) {
			applyBrake();
			stop = true;
			if (getHealth() == 100) {
				stop = false;
			}
		}
		// checkStateChange();
		if (getSpeed() < CAR_MAX_SPEED && stop == false) { // Need speed to turn and progress toward the exit
			applyForwardAcceleration();// Tough luck if there's a wall in the way
		}
		Coordinate previousPos = previous.first();
		previous.removeFirst();
		previous.addFirst(currentPosition);
		
		
		move(currentPosition, currDistination, mapTest, previousPos);		

	}

	private void updateMap(HashMap<Coordinate, MapTile> currentView, HashSet<Coordinate> health) {
		// TODO Auto-generated method stub
		for(Coordinate c: currentView.keySet()) {
			if (currentView.get(c).isType(Type.ROAD)) {
				roadCandidates.add(c);
			}
			
			if (currentView.get(c).isType(Type.TRAP)) {
				currentMap.put(c, currentView.get(c));
				TrapTile trapTile = (TrapTile) currentView.get(c);
				if (trapTile.getTrap().equals("health")) {
					health.add(c);
				}		
			}
		}	
	}


	private boolean checkShouldBrake(HashMap<Coordinate, MapTile> currentView, Coordinate pos) {
		if (currentView.get(pos).getType() == MapTile.Type.TRAP) {
			TrapTile trapTile = (TrapTile) currentView.get(pos);
			if (trapTile.getTrap().equals("health") || trapTile.getTrap().equals("mud")) {
				return true;
			}
		}
		return false;
	}

	private void move(Coordinate currentPosition, Coordinate currDistination, HashMap<Coordinate, MapTile> currentMap, Coordinate previousPos) {
		// TODO Auto-generated method stub
		HashMap<Coordinate, MapTile> currentView = getView();
		List<Coordinate> path = new ArrayList<>();
		boolean increaseHealth = false;
		if (getHealth() < 50 && !health.isEmpty()) {
			Coordinate nearestHealth = getShortPath(currentPosition, health);
			increaseHealth = true;
			path = PathFinding.aStarFindPath(currentPosition, nearestHealth, currentMap, previousPos);
		} else {
			path = PathFinding.aStarFindPath(currentPosition, currDistination, currentMap, previousPos);
		}
		/**
		 * Cancel the goal if there is mud on goal
		 */
		if (path != null) {
		for (Coordinate c : path) {
			if (currentView.get(c)!=null) {
				if(currentView.get(c).getType() == MapTile.Type.TRAP) {
					TrapTile currTrap = (TrapTile) currentView.get(c);
					if(currTrap.getTrap().equals("mud")) {
						currGoal.cancelGoal();
					}
					}
				}
			}
		}
		

		if (path != null) {
		if (path.size() >= 2 ) {
			Coordinate nextPoisition = path.get(1);
			if (needHealth(currentPosition, health)) {
				applyBrake();
			}
			else {
				moveToGoal(currentPosition, nextPoisition, getOrientation());	
			}
		
			} 
		}
		if (path == null) {
			currGoal.cancelGoal();
		}
		
	}

	private boolean needHealth(Coordinate currentPosition, HashSet<Coordinate> health) {
		// TODO Auto-generated method stub
		for(Coordinate c: health) {
			if (currentPosition.equals(c)) {
				getHealth();
				if (getHealth()<100) {
					return true;
				}
			}
		}
		return false;
	}

	private Coordinate getShortPath(Coordinate currentPosition, HashSet<Coordinate> health) {
		// TODO Auto-generated method stub
		int minDistance = 1000;
		Coordinate healthNode = null;
		for (Coordinate c : health) {
			int distance = util.getManhattanDistance(currentPosition, c);
			if (distance <= minDistance) {
				healthNode = c;
			}
		}
		return healthNode;
	}

	
	private void moveToGoal(Coordinate currentPosition, Coordinate nextPoisition, Direction direction) {
		Direction relativeDirection = faceToGoal(currentPosition, nextPoisition);
		
		if (relativeDirection != direction) {
			int directionNum = getNumberOfDirection(direction) - getNumberOfDirection(relativeDirection);
			if (directionNum == 1 || directionNum == -3) {
				turnLeft();
			} else if (directionNum == 2 || directionNum == -2) {
				if (sensor.chechWallRight(direction, getView()) <= 1) {
					turnLeft();
				} else if (sensor.chechWallLeft(direction, getView()) <=1 ) {
					turnRight();
				}
			} else {
				turnRight();
			}
			
		}
	}

	private int getNumberOfDirection(Direction direction) {

		if (direction == Direction.EAST) {
			return 1;
		} else if (direction == Direction.SOUTH) {
			return 2;
		} else if (direction == Direction.WEST) {
			return 3;
		} else {
			return 4;
		}
	}

	private Direction faceToGoal(Coordinate currentPosition, Coordinate nextPoisition) {
		// TODO Auto-generated method stub
		int deltaX = nextPoisition.x - currentPosition.x;
		int deltaY = nextPoisition.y - currentPosition.y;

		if (deltaX > 0) {
			return Direction.EAST;
		} else if (deltaX < 0) {
			return Direction.WEST;
		} else if (deltaY > 0) {
			return Direction.NORTH;
		} else if (deltaY < 0) {
			return Direction.SOUTH;
		}

		return null;
	}


}