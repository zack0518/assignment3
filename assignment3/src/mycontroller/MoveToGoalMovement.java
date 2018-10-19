package mycontroller;

import utilities.Coordinate;
import world.Car;
import world.WorldSpatial.Direction;

public class MoveToGoalMovement extends Movement {
	public Car car;
	Coordinate currentPosition;
	Coordinate nextPoisition;
	Direction direction;
	MoveToGoalMovement(Car car, Coordinate currentPosition, Coordinate nextPoisition, Direction direction, 
			DetectAroundSensor sensor){
		this.car = car;
		this.currentPosition = currentPosition;
		this.nextPoisition = nextPoisition;
		this.direction = direction;
	}
	@Override
	public void movement() {
		moveToGoal(currentPosition, nextPoisition, direction);
	}

	@Override
	public boolean shouldStop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldCancelGoal() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void moveToGoal(Coordinate currentPosition, Coordinate nextPoisition, Direction direction) {
		Direction relativeDirection = faceToGoal(currentPosition, nextPoisition);

		if (relativeDirection != direction) {
			int directionNum = getNumberOfDirection(direction) - getNumberOfDirection(relativeDirection);
			if (directionNum == 1 || directionNum == -3) {
				if (car.getVelocity() >0) {
					car.turnLeft();
				} else if (car.getVelocity()<0) {
					car.turnRight();
				}
				
			} 
			else if (directionNum == 2 || directionNum == -2) {
				car.applyReverseAcceleration();
			} else {
				if (car.getVelocity() >0) {
					car.turnRight();
				} else if (car.getVelocity()<0) {
					car.turnLeft();
				}
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
