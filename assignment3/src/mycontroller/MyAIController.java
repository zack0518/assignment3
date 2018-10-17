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
	private final int CAR_MAX_SPEED = 1;
	
	private HashSet<Coordinate> lava  = new HashSet<Coordinate>();
	private HashSet<Coordinate> grass = new HashSet<Coordinate>();
	private HashSet<Coordinate> mud = new HashSet<Coordinate>();
	private HashSet<Coordinate> health = new HashSet<Coordinate>();
	private HashSet<Coordinate> choose = new HashSet<Coordinate>();
	private Queue<Coordinate> previous = new Queue<Coordinate>();
	public HashMap<Coordinate, MapTile> exploreMap = new HashMap<>();
	private HashSet<Coordinate> closedSet = new HashSet<>();
	
	private GoalMaker currGoal;
	private Coordinate currDistination;
	private DetectAroundSensor sensor;
	
	public MyAIController(Car car) {
		super(car);
		sensor = new DetectAroundSensor(wallSensitivity,car);
		currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap(),car);
	}
	
	// Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		HashMap<Coordinate, MapTile> currentMap = getMap();
		boolean stop = false;
		
		Coordinate currentPosition = new Coordinate(getPosition());
		currGoal.evaluateCurrentView(currentView);
		currDistination = currGoal.getCurrGoal();
		Coordinate currPos = new Coordinate(getPosition());
		if (currPos.equals(currDistination)) {
			currGoal.reachedTheGoal(currPos);
			currDistination = currGoal.getCurrGoal();
			applyBrake();
			stop = true;
			
		}
		if (checkShouldBrake(currentView, new Coordinate(getPosition()))) {
			applyBrake();
			stop = true;
			if (getHealth() == 100) {
				stop = false;
			}
		}
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED && !stop ){       // Need speed to turn and progress toward the exit
			applyForwardAcceleration();   // Tough luck if there's a wall in the way
		}
		move(currentPosition, currDistination, currentMap);
	}

	private boolean checkShouldBrake(HashMap<Coordinate, MapTile> currentView, Coordinate pos) {
		if(currentView.get(pos).getType() == MapTile.Type.TRAP) {
			TrapTile trapTile = (TrapTile) currentView.get(pos);
			if (trapTile.getTrap().equals("health")) {
				return true;
			}
		}
		return false;
	}
	private void move(Coordinate currentPosition, Coordinate currDistination, HashMap<Coordinate, MapTile> currentMap) {
		// TODO Auto-generated method stub
		List<Coordinate> path = new ArrayList<>();
		if (getHealth() <= 100 && !health.isEmpty() ) {
			Coordinate nearestHealth = getShortPath(currentPosition, health);
			path = PathFinding.aStarFindPath(currentPosition, nearestHealth, currentMap);
		} else {
			path = PathFinding.aStarFindPath(currentPosition, currDistination, currentMap);
		}
		System.out.println(path.get(path.size() -1 ));
	    if (path == null) {
	        throw new IllegalArgumentException("No path to the given destination.");
	      }
	      if (path.size() >= 2) {
	        Coordinate nextPoisition = path.get(1);
	        moveToGoal(currentPosition, nextPoisition, getOrientation());
	      }
		}

	  private Coordinate getShortPath(Coordinate currentPosition, HashSet<Coordinate> health) {
		// TODO Auto-generated method stub
		  int minDistance = 1000;
		  Coordinate healthNode = null;
		  for (Coordinate c: health) {
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
			  int directionNum = getNumberOfDirection(direction) - getNumberOfDirection(relativeDirection);
		      if (directionNum == 1 || directionNum == -3) {
		        turnLeft();
		      } else if (directionNum == 2 || directionNum == -2) {
		    	  if (sensor.checkWallAheadDistance(direction,getView()) >= wallSensitivity) {
		          turnRight();
		        } else {
		          turnLeft();
		        }
		      } 
		      else {
		        turnRight();
		      }
		    }
		  }


	private int getNumberOfDirection(Direction direction) {
		
	    if (direction == Direction.EAST) {
		      return 0;
		    } else if (direction == Direction.SOUTH) {
		      return 1;
		    } else if (direction == Direction.WEST) {
		      return 2;
		    } else {
		      return 3;
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
	
	public static int getManhattanDistance(Coordinate a, Coordinate b){
		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}

	
}
