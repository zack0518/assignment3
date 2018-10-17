package controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.Distance;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.utils.Queue;

import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.MudTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import mycontroller.*;

public class AIController extends CarController {
	
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
	
	public AIController(Car car) {
		super(car);
//		currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap());
//		currDistination = currGoal.getRandomGoal();
		Coordinate coordinate = new Coordinate(-1,-1);
//		System.out.println(currDistination);
		previous.addFirst(coordinate);
	}
	
	// Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		HashMap<Coordinate, MapTile> currentMap = getMap();

		Coordinate currentPosition = new Coordinate(getPosition());
		currDistination = new Coordinate(35,3);

		
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
			applyForwardAcceleration();   // Tough luck if there's a wall in the way
		}
		
		move(currentPosition, currDistination, currentMap);
		
		
	}


	private void move(Coordinate currentPosition, Coordinate currDistination, HashMap<Coordinate, MapTile> currentMap) {
		// TODO Auto-generated method stub
		List<Coordinate> path = PathFinding.aStarFindPath(currentPosition, currDistination, currentMap);
		
	    if (path == null) {
	        throw new IllegalArgumentException("No path to the given destination.");
	      }
	      if (path.size() >= 2) {
	        Coordinate nextPoisition = path.get(1);
	        moveToGoal(currentPosition, nextPoisition, getOrientation());
	      }
		}

	  private void moveToGoal(Coordinate currentPosition, Coordinate nextPoisition, Direction direction) {
		  Direction relativeDirection = faceToGoal(currentPosition, nextPoisition);
		  int deltaX = nextPoisition.x - currentPosition.x;
		  int deltaY = nextPoisition.y - currentPosition.y;
		  

		
		  
		  if (relativeDirection != direction) {
			  int directionNum = getNumberOfDirection(direction) - getNumberOfDirection(relativeDirection);
		      if (directionNum == 1 || directionNum == -3) {
		        turnLeft();
		      } else if (directionNum == 2 || directionNum == -2) {
		    	  if (checkWallAhead(direction)) {
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

		/**
		 * Check if you have a wall in front of you!
		 * @param orientation the orientation we are in based on WorldSpatial
		 * @param currentView what the car can currently see
		 * @return
		 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation){
		HashMap<Coordinate, MapTile> currentView = getView();
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		}
	}
	  
	  
	  public boolean checkEast(HashMap<Coordinate, MapTile> currentView) {
		    // Check tiles to my right
		    Coordinate currentPosition = new Coordinate(getPosition());
		    for (int i = 0; i <= wallSensitivity; i++) {
		      MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y + i));
	//		      if (isWallOrMud(tile)) {
		      if (tile.isType(MapTile.Type.WALL)) {
		        return true;
		      }
		    }
		    return false;
		  }
		  
		  
	
	  public boolean checkWest(HashMap<Coordinate, MapTile> currentView) {
	    // Check tiles to my left
	    Coordinate currentPosition = new Coordinate(getPosition());
	    for (int i = 0; i <= wallSensitivity; i++) {
	      MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y - i));
	//		      if (isWallOrMud(tile)) {
	      if (tile.isType(MapTile.Type.WALL)) {
	        return true;
	      }
	    }
	    return false;
	  }
	
	  public boolean checkNorth(HashMap<Coordinate, MapTile> currentView) {
	    // Check tiles to towards the top
	    Coordinate currentPosition = new Coordinate(getPosition());
	    for (int i = 0; i <= wallSensitivity; i++) {
	      MapTile tile = currentView.get(new Coordinate(currentPosition.x - i, currentPosition.y));
	//		      if (isWallOrMud(tile)) {
	      if (tile.isType(MapTile.Type.WALL)) {
	        return true;
	      }
	    }
	    return false;
	  }
	
	  public boolean checkSouth(HashMap<Coordinate, MapTile> currentView) {
	    // Check tiles towards the bottom
	    Coordinate currentPosition = new Coordinate(getPosition());
	    for (int i = 0; i <= wallSensitivity; i++) {
	      MapTile tile = currentView.get(new Coordinate(currentPosition.x + i, currentPosition.y));
	//		      if (isWallOrMud(tile)) {
	      if (tile.isType(MapTile.Type.WALL)) {
	        return true;
	      }
	    }
	    return false;
	  }


	
	public static int getManhattanDistance(Coordinate a, Coordinate b){

		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}

	
}
