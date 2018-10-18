package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.Distance;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.utils.Queue;

import controller.CarController;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.MudTrap;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import mycontroller.*;

public class MyAIController extends CarController {

	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;

	private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.

	// Car Speed to move at
	private final int CAR_MAX_SPEED = 5;
	
	private static boolean stop = false;

	private HashSet<Coordinate> lava = new HashSet<Coordinate>();
	private HashSet<Coordinate> grass = new HashSet<Coordinate>();
	private HashSet<Coordinate> mud = new HashSet<Coordinate>();
	private HashSet<Coordinate> health = new HashSet<Coordinate>();
	private HashSet<Coordinate> choose = new HashSet<Coordinate>();
	private Queue<Coordinate> previous = new Queue<Coordinate>();
	public HashMap<Coordinate, MapTile> exploreMap = new HashMap<>();
	private HashSet<Coordinate> closedSet = new HashSet<>();
	HashMap<Coordinate, MapTile> currentMap;
	public static HashMap<Coordinate, MapTile> theMapWeVisited = new HashMap<>();

	private GoalMaker currGoal;
	private Coordinate currDistination;
	private DetectAroundSensor sensor;
	private Coordinate preLoc;

	public MyAIController(Car car) {
		super(car);
		sensor = new DetectAroundSensor(wallSensitivity, car);
		currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap(), car);
		currentMap = getMap();
	}

	// Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		HashMap<Coordinate, MapTile> mapTest = getMap();
		
		for(Coordinate c: currentView.keySet()) {
			if (currentView.get(c).isType(Type.TRAP)) {
				currentMap.put(c, currentView.get(c));
				TrapTile trapTile = (TrapTile) currentView.get(c);
				if (trapTile.getTrap().equals("health")) {
					health.add(c);
				}
			}

		}
		


		Coordinate currentPosition = new Coordinate(getPosition());
		currDistination = currGoal.getCurrGoal();
		Coordinate currPos = new Coordinate(getPosition());
		if (currPos.equals(currDistination)) {
			currGoal.reachedTheGoal(currPos);
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
		move(currentPosition, currDistination, mapTest);		

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

	private void move(Coordinate currentPosition, Coordinate currDistination, HashMap<Coordinate, MapTile> currentMap) {
		// TODO Auto-generated method stub
		HashMap<Coordinate, MapTile> currentView = getView();
		List<Coordinate> path = new ArrayList<>();
		boolean increaseHealth = false;
		System.out.println("goal:  "+ currDistination);
		System.out.println("goal type " + currentMap.get(currDistination).getType());
		if (getHealth() < 50 && !health.isEmpty()) {
			Coordinate nearestHealth = getShortPath(currentPosition, health);
			increaseHealth = true;
			path = PathFinding.aStarFindPath(currentPosition, nearestHealth, currentMap, currentView);
		} else {
			path = PathFinding.aStarFindPath(currentPosition, currDistination, currentMap, currentView);
		}
		System.out.println(path);
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
			int distance = getManhattanDistance(currentPosition, c);
			if (distance <= minDistance) {
				healthNode = c;
			}
		}
		return healthNode;
	}

	
	private void moveToGoal(Coordinate currentPosition, Coordinate nextPoisition, Direction direction) {
		Direction relativeDirection = faceToGoal(currentPosition, nextPoisition);

		if (relativeDirection != direction) {
			System.out.print(direction);
			System.out.println(relativeDirection);
			int directionNum = getNumberOfDirection(direction) - getNumberOfDirection(relativeDirection);
			if (directionNum == 1 || directionNum == -3) {
				if(sensor.checkWallAheadDistance(direction, getView()) <= 1) {
					applyReverseAcceleration();
					turnLeft();
				}
				turnLeft();
			} 

			else if (directionNum == 2 || directionNum == -2) {
				applyReverseAcceleration();
			} else {
				if(sensor.checkWallAheadDistance(direction, getView()) <= 1) {
					applyReverseAcceleration();
					turnRight();
				}
				System.out.println("turn right----");
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

	public static int getManhattanDistance(Coordinate a, Coordinate b) {
		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}

}