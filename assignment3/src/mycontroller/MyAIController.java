package mycontroller;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController {
	
		// How many minimum units the wall is away from the player.
		private int wallSensitivity = 4;
		private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.			
		// Car Speed to move at
		private final int CAR_MAX_SPEED = 1;
		private DetectAroundSensor detectAroundSensor;
		private GoalMaker currGoal;
		private Coordinate currDistination;
		private MoveSimulation moveSimulation;
		private int MapWidth;
		private int MapHeight;
		
		public MyAIController(Car car) {
			super(car);
			detectAroundSensor = new DetectAroundSensor(this.wallSensitivity, car);
			moveSimulation = new MoveSimulation();
			currGoal = new GoalMaker(mapWidth(), mapHeight(), getMap());
			currDistination = currGoal.getRandomGoal();
			System.out.println(currDistination);
		}
		// Coordinate initialGuess;
		// boolean notSouth = true;
		@Override
		public void update() {
			//Gets what the car can see
			HashMap<Coordinate, MapTile> currentView = getView();
			
			HashMap<Coordinate, MapTile> currentMap = getMap();
			for (Coordinate c : currentMap.keySet()) {
				if (currentMap.get(c).getType() == MapTile.Type.ROAD) {
					System.out.println(c);
					System.out.println(MapTile.Type.ROAD);
				}
				
			}

			Coordinate currPos = new Coordinate(getPosition());
			currGoal.evaluateCurrView(currentView, currPos);
			Coordinate currgoal = currGoal.getRandomGoal();
			if(getSpeed() < CAR_MAX_SPEED){    // Need speed to turn and progress toward the exit
				applyForwardAcceleration();  // Tough luck if there's a wall in the way
			}
			if (detectAroundSensor.checkWallAheadDistance(getOrientation(), currentView) != wallSensitivity) {
				WorldSpatial.Direction currDirection;
				currDirection = moveSimulation.turnLeft(getOrientation(), new Coordinate(getPosition()));
				currDirection = moveSimulation.turnRight(getOrientation(), new Coordinate(getPosition()));
			}
			System.out.println(detectAroundSensor.checkWallAheadDistance(getOrientation(), currentView));
		}
}
