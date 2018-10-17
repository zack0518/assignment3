package controller;

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.None;
import com.badlogic.gdx.utils.Queue;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import mycontroller.*;
import tiles.*;

public class AIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 3;
	
	private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
	
	// Car Speed to move at
	private final int CAR_MAX_SPEED = 1;

	private DetectAroundSensor detectAroundSensor;
	private GoalMaker currGoal;
	private Coordinate currDistination;
	private MoveSimulation moveSimulation;
	private int MapWidth;
	private int MapHeight;
	private HashSet<Coordinate> lava  = new HashSet<Coordinate>();
	private HashSet<Coordinate> grass = new HashSet<Coordinate>();
	private HashSet<Coordinate> mud = new HashSet<Coordinate>();
	private HashSet<Coordinate> health = new HashSet<Coordinate>();
	private Queue<Coordinate> previous = new Queue<Coordinate>();

	
	public AIController(Car car) {
		super(car);
		currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap());
		currDistination = currGoal.getRandomGoal();
		Coordinate coordinate = new Coordinate(-1,-1);
		previous.addFirst(coordinate);
	}
	
	public static int getManhattanDistance(Coordinate a, Coordinate b){

		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}
	public void turnAround(Coordinate a, Coordinate p, HashMap<Coordinate, MapTile> currentView) {
		for (Coordinate c: currentView.keySet()) {
			if(getManhattanDistance(a, c) == 1 && currentView.get(c).getType() == MapTile.Type.ROAD && c != p) {
				System.out.println(c);
			}
		}
		
	}
	
	// Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		for (Coordinate c: currentView.keySet()) {
			if (currentView.get(c).getType() == MapTile.Type.TRAP) {
				
				TrapTile trapTile = (TrapTile) currentView.get(c);
				String trapType = trapTile.getTrap();	
				switch (trapType) {
				case "grass":
					grass.add(c);
					break;
				case "mud":
					mud.add(c);
					break;
				case "lava":
					lava.add(c);
					break;
				case "health":
					health.add(c);
				default:
					break;
				}
			}
		}
		Coordinate currPos = new Coordinate(getPosition());
		previous.addLast(currPos);

		
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
			applyForwardAcceleration();   // Tough luck if there's a wall in the way
		}
		
		
		
//		update here
		if (isFollowingWall) {

			
			// If wall no longer on left, turn left
			if(!checkFollowingWall(getOrientation(), currentView)) {
				turnLeft();
			} 
			
			else {
				// If wall on left and wall straight ahead, turn right
				if(checkWallAhead(getOrientation(), currentView)) {
					turnRight();
					turnAround(currPos, previous.first(), currentView);
					previous.removeFirst();
				}
			}
		} 
		
		else {
			// Start wall-following (with wall on left) as soon as we see a wall straight ahead
			if(checkWallAhead(getOrientation(),currentView)) {
//				turnRight();
				turnLeft();
				turnAround(currPos, previous.first(), currentView);
				previous.removeFirst();
				isFollowingWall = true;
			}
		}
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
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
	
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}	
	}
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL) ){
				return true;
			}
		}
		return false;
	}

	public boolean checkAround(HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation) {
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i ++) {
			switch (orientation) {
			case EAST:
				MapTile titleLeftE = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
				MapTile titleRightE = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
				if(titleLeftE.isType(MapTile.Type.WALL) || titleRightE.isType(MapTile.Type.WALL) ){
					return true;
				}
				break;
				
			case WEST:
				MapTile titleLeftW = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
				MapTile titleRightW = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
				if(titleLeftW.isType(MapTile.Type.WALL) || titleRightW.isType(MapTile.Type.WALL) ){
					return true;
				}
				break;
				
			case NORTH:
				MapTile titleLeftN = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
				MapTile titleRightN = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y-i));
				if(titleLeftN.isType(MapTile.Type.WALL) || titleRightN.isType(MapTile.Type.WALL) ){
					return true;
				}
				break;
				
			case SOUTH:
				MapTile titleLeftS = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
				MapTile titleRightS = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
				if(titleLeftS.isType(MapTile.Type.WALL) || titleRightS.isType(MapTile.Type.WALL) ){
					return true;
				}
				break;

			default:
				break;
			}
		}
		
		return false;
		
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
}
