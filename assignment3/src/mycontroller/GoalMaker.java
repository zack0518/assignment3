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
	public List <Coordinate> visitedPoint;
	public List <KeyAndLocation> keyAndLocaitons;
	
	
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
		this.visitedPoint = new ArrayList <Coordinate>();
		predefinedGoals();
	}

	public void predefinedGoals() {
		starPos = new Coordinate(this.car.getPosition());
		int tempWidth = mapWidth;
		int tempHeight = mapHeight;
		
		for(int i = 0; i < tempWidth; i = i + 4) {
			Coordinate c = getValidGoal(new Coordinate(i, 0));
			if (getManhattanDistance(c, starPos) > 5) {
				futureGoal.add(getValidGoal(c));
			}
		}
		
		for(int i = 0; i < tempWidth; i = i + 4) {
			Coordinate c = getValidGoal(new Coordinate(i, tempHeight - 1));
			if (getManhattanDistance(c, starPos) > 5) {
				futureGoal.add(getValidGoal(c));
			}
		}
		
		for(int i = 0; i < tempHeight; i = i + 8) {
			Coordinate c = getValidGoal(new Coordinate(0, i));
			if (getManhattanDistance(c, starPos) > 5) {
				futureGoal.add(getValidGoal(c));
			}
		}
		
		for(int i = 0; i < tempHeight; i = i + 8) {
			Coordinate c = getValidGoal(new Coordinate(tempWidth - 1, i));
			if (getManhattanDistance(c, starPos) > 5) {
				futureGoal.add(getValidGoal(c));
			}
		}
		futureGoal.add(getValidGoal(new Coordinate( tempWidth/2, tempHeight/2)));
		Collections.sort(futureGoal, new priorityComparator());
	}
	public Coordinate getValidGoal(Coordinate c) {
		if(!isValidGoal(c)) {
			return getClosestValidCoordinate(c);
		}
		return c;
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
	
	public void updateVisited(Coordinate c) {
		visitedPoint.add(c); 
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
	
	public boolean hasAllKeys() {
		int keyNumbers = car.numKeys;
		for (int i = 1; i <= keyNumbers; i++) {
			if(!car.getKeys().contains(i)) {
				return false;
			}
		}
		return true;
	}

	public Coordinate getCurrGoal() {
		if(hasAllKeys()) {
			return exit;
		}
		if (futureGoal.size() > 0) {
			return futureGoal.get(0);
		}else {
			return exit;
		}
	}
}
