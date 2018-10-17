package mycontroller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;

public class GoalMaker {
	
	public int stateReward;
	public Coordinate targetPoint;
	public int mapWidth;
	public int mapHeight;
	public HashMap<Coordinate, MapTile> currentMap;
	public ArrayList <Coordinate> futureGoal;
	public HashMap<Coordinate, MapTile> currentView;
	public ArrayList <Coordinate> visitedGoals;
	public StuckedWarning stuckWarn;
	public Car car;
	public boolean shouldBrake;
	
	GoalMaker (int mapWidth, int mapHeight, HashMap<Coordinate, MapTile> currentMap, Car car){
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.currentMap = currentMap;
		futureGoal = new ArrayList <Coordinate>();
		visitedGoals = new ArrayList <Coordinate>();
		stuckWarn = new StuckedWarning(car);
		this.car = car;
		shouldBrake = false;
		predefinedGoals();
	}
	
	public void predefinedGoals() {
		Coordinate currPos = new Coordinate(this.car.getPosition());
		Coordinate corner1 = new Coordinate(0,0);
		if(!isValidGoal(corner1)) {
			corner1 = getClosestValidCoordinate(corner1);
		}
		Coordinate corner2 = new Coordinate(mapWidth - 10, 0);
		if(!isValidGoal(corner2)) {
			corner2 = getClosestValidCoordinate(corner2);
		}
		Coordinate corner3 = new Coordinate(mapWidth - 10, mapHeight - 10);
		if(!isValidGoal(corner3)) {
			corner3 = getClosestValidCoordinate(corner3);
		}
		Coordinate corner4 = new Coordinate(0, mapHeight - 10);
		if(!isValidGoal(corner4)) {
			corner4 = getClosestValidCoordinate(corner4);
		}
		Coordinate midPoint = new Coordinate(mapWidth/2, mapHeight/2);
		if(!isValidGoal(midPoint)) {
			midPoint = getClosestValidCoordinate(midPoint);
		}
		ArrayList <Coordinate>  candidateGoal = new ArrayList <Coordinate> ();
		
		candidateGoal.add(corner1);
		candidateGoal.add(corner2);
		candidateGoal.add(midPoint);
		candidateGoal.add(corner3);
		candidateGoal.add(corner4);	
		
		for (Coordinate c : candidateGoal) {
			if (getManhattanDistance(c, currPos) > 5) {
				futureGoal.add(c);
			}
		}
	}
	
	public boolean isValidGoal(Coordinate coordinate) {
		if (currentMap.get(coordinate).getType() == MapTile.Type.ROAD) {
			return true;
		}
		return false;
	}
	
	public void reachedTheGoal(Coordinate c) {
		futureGoal.remove(c);
		visitedGoals.add(c);
	}
	public Coordinate getClosestValidCoordinate(Coordinate coordinate) {
		int minDistance = Integer.MAX_VALUE;
		Coordinate target = coordinate;
		for (Coordinate c : currentMap.keySet()) {
			if (getManhattanDistance(c, coordinate) < minDistance && this.currentMap.get(c).isType(MapTile.Type.ROAD)) {
				minDistance = getManhattanDistance(c, coordinate);
				target = c;
			}
		}
		return target;
	}
	
	public int getManhattanDistance(Coordinate c1, Coordinate c2) {
		return Math.abs(c1.x - c2.x) + Math.abs(c1.y - c2.y);
	}
	
	public void evaluateCurrentView(HashMap<Coordinate, MapTile> currentView) {
		shouldBrake = true; 
		for(Coordinate c : currentView.keySet()) {
			boolean isVisitedGoal = isVisitedGoal(c);
			if(currentView.get(c).getType() == MapTile.Type.TRAP && !isVisitedGoal) {
				if(shouldTrapBecomeGoal((TrapTile) currentView.get(c), c)) {
					futureGoal.add(0, c);
				}
				
				TrapTile trapTile = (TrapTile) currentView.get(c);
				if (shouldTrapBecomeGoal(trapTile, c)) {
					visitedGoals.add(c);
				}
			}
		}
	}
	
	public boolean shouldBrake() {
		return shouldBrake; 
	}
	public boolean isVisitedGoal(Coordinate currGoal) {
		for(Coordinate c : visitedGoals) {
			if (c.x == currGoal.x && c.y == currGoal.y) {
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldTrapBecomeGoal(TrapTile trapTile, Coordinate c) {
		if(trapTile.getTrap().equals("lava")){
			LavaTrap lavaTrap = (LavaTrap) trapTile;
			if (lavaTrap.getKey() > 0) {
//				System.out.println("key found : "+lavaTrap.getKey()) ;
//				System.out.println("key on : "+c);
				return true;
			}
			
		}else if(trapTile.getTrap().equals("health")) {
			shouldBrake = true;
			return true;
		}
		return false;
	}
	/**
	 * get the random goal for testing purpose
	 * @return
	 */
	public Coordinate getRandomGoal() {
		Random rand = new Random();
		int n = rand.nextInt(currentMap.size());
		int i = 0;
		for (Coordinate c : this.currentMap.keySet()) {
			if (currentMap.get(c).isType(MapTile.Type.ROAD) && i > currentMap.size()/2) {
				targetPoint = c;
			}
			i++;
		}
		return targetPoint;
	}
	
	public Coordinate getCurrGoal() {
		return futureGoal.get(0);
	}
}
