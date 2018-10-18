package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

public class MoveSimulation {
	
	public WorldSpatial.Direction turnLeft(WorldSpatial.Direction orientation, Coordinate coordinate){
		switch(orientation){
		case EAST:
			return WorldSpatial.Direction.NORTH;
		case NORTH:
			return WorldSpatial.Direction.WEST;
		case SOUTH:
			return WorldSpatial.Direction.EAST;
		case WEST:
			return WorldSpatial.Direction.SOUTH;
		}	
		return orientation;
	}
	
	public WorldSpatial.Direction turnRight(WorldSpatial.Direction orientation, Coordinate coordinate){
		switch(orientation){
		case EAST:
			return WorldSpatial.Direction.SOUTH;
		case NORTH:
			return WorldSpatial.Direction.EAST;
		case SOUTH:
			return WorldSpatial.Direction.WEST;
		case WEST:
			return WorldSpatial.Direction.NORTH;
		}	
		return orientation;
	}
	
	public Coordinate postionAfterMove(WorldSpatial.Direction orientation,  Coordinate coordinate) {
		switch(orientation){
		case EAST:
			return new Coordinate(coordinate.x + 1, coordinate.y);
		case NORTH:
			return new Coordinate(coordinate.x, coordinate.y + 1);
		case SOUTH:
			return new Coordinate(coordinate.x, coordinate.y - 1);
		case WEST:
			return new Coordinate(coordinate.x - 1, coordinate.y);
		}	
		return null;
	}
	
}
