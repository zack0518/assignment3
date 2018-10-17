package mycontroller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
	public Coordinate exit;
	public Coordinate starPos;
	public List <KeyAndLocation> keyAndLocaitons;
	
//	public static boolean hasAllKeys;
	
	
	GoalMaker (int mapWidth, int mapHeight, HashMap<Coordinate, MapTile> currentMap, Car car){
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.currentMap = currentMap;
		futureGoal = new ArrayList <Coordinate>();
		visitedGoals = new ArrayList <Coordinate>();
		stuckWarn = new StuckedWarning(car);
		this.car = car;
		shouldBrake = false;
		this.keyAndLocaitons = new ArrayList <KeyAndLocation>();
		predefinedGoals();
	}

	public void predefinedGoals() {
		starPos = new Coordinate(this.car.getPosition());
		Coordinate corner1 = new Coordinate(0,0);
		if(!isValidGoal(corner1)) {
			corner1 = getClosestValidCoordinate(corner1);
		}
		Coordinate corner2 = new Coordinate(mapWidth - 5, 0);
		Coordinate cornerOffset1 = new Coordinate(mapWidth - 15, 0);
		if(!isValidGoal(cornerOffset1)) {
			cornerOffset1 = getClosestValidCoordinate(cornerOffset1);
		}
		if(!isValidGoal(corner2)) {
			corner2 = getClosestValidCoordinate(corner2);
		}
		Coordinate corner3 = new Coordinate(mapWidth - 5, mapHeight - 5);
		Coordinate cornerOffset2  = new Coordinate(mapWidth - 15, mapHeight - 15);
		if(!isValidGoal(cornerOffset2)) {
			cornerOffset2 = getClosestValidCoordinate(cornerOffset2);
		}
		if(!isValidGoal(corner3)) {
			corner3 = getClosestValidCoordinate(corner3);
		}
		Coordinate corner4 = new Coordinate(0, mapHeight - 5);
		Coordinate cornerOffset3 = new Coordinate(0, mapHeight - 15);
		if(!isValidGoal(cornerOffset3)) {
			cornerOffset3 = getClosestValidCoordinate(cornerOffset3);
		}
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
		candidateGoal.add(cornerOffset1);
		candidateGoal.add(midPoint);
		candidateGoal.add(corner3);
		candidateGoal.add(cornerOffset2);
		candidateGoal.add(corner4);	
		candidateGoal.add(cornerOffset3);
		
		for (Coordinate c : candidateGoal) {
			if (getManhattanDistance(c, starPos) > 5) {
				futureGoal.add(c);
			}
		}
		Collections.sort(futureGoal, new priorityComparator());
	}

	private class priorityComparator implements Comparator<Coordinate>{
		@Override
		public int compare(Coordinate c1, Coordinate c2) {
			int manhantanDistance1 = getManhattanDistance(c1, starPos);
			int manhantanDistance2 = getManhattanDistance(c2, starPos);
			return manhantanDistance1 - manhantanDistance2;
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
		for(Coordinate c : currentView.keySet()) {
			boolean isVisitedGoal = isVisitedGoal(c);
			if(currentView.get(c).getType() == MapTile.Type.TRAP && !isVisitedGoal) {
				if(shouldTrapBecomeGoal((TrapTile) currentView.get(c), c)) {
					futureGoal.add(0, c);
					visitedGoals.add(c);
				}
			}
			if (currentView.get(c).getType() == MapTile.Type.FINISH) {
				exit = c;
			}
		}
	}
	
	public boolean isHasTheKey(int key){
		Set<Integer> holdKeys= car.getKeys();
		for (Integer temp : holdKeys) {
			if (temp == key) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isVisitedGoal(Coordinate currGoal) {
		for(Coordinate c : visitedGoals) {
			if (c.equals(currGoal)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldTrapBecomeGoal(TrapTile trapTile, Coordinate c) {
		if(trapTile.getTrap().equals("lava")){
			LavaTrap lavaTrap = (LavaTrap) trapTile;
			if (lavaTrap.getKey() > 0) {
				KeyAndLocation keyAndLoc = new KeyAndLocation(lavaTrap.getKey(), c);
			}
			if (lavaTrap.getKey() > 0 && !isHasTheKey(lavaTrap.getKey()) && !isVisitedGoal(c)) {
				visitedGoals.add(c);
				return true;
			}
		}else if(trapTile.getTrap().equals("health") && car.getHealth() < 50 && !isVisitedGoal(c)) {
			visitedGoals.add(c);
			return true;
		}
		return false;
	}
	
	public boolean isKeySameLoaction(KeyAndLocation currK) {
		for (KeyAndLocation k : keyAndLocaitons) {
			if(currK.getLocaiton() == k.getLocaiton()) {
				return true;
			}
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
	
	public void removeDuplicate() {
		car.getKeys();
	}
	
	public void cancelGoal() {
		if(futureGoal.size() > 0) {
			futureGoal.remove(0);
		}
	}
	
	
	public Coordinate getCurrGoal() {
		System.out.println(futureGoal.size());
		if(futureGoal.size() == 0) {
			return exit;
		} 
		if (futureGoal.size() > 0) {
			return futureGoal.get(0);
		}else {
			return exit;
		}
	}


}


