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
	public int offset;
	
	
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
		offset = 0;
	}

	public void predefinedGoals() {
		starPos = new Coordinate(this.car.getPosition());
		moreGoals(0);
	}
	
	public void moreGoals(int offset) {
		int tempWidth = mapWidth;
		int tempHeight = mapHeight;
		Coordinate corner1 = getValidGoal(new Coordinate(2 + offset, 2));
		Coordinate corner2 = getValidGoal(new Coordinate(2, 2 + offset));
		Coordinate corner3 = getValidGoal(new Coordinate(tempWidth - 2, 2 + offset));
		Coordinate corner4 = getValidGoal(new Coordinate(offset + 2,  tempHeight - 2));
		Coordinate corner5 = getValidGoal(new Coordinate(tempWidth - 2,  tempHeight - 2));
		Coordinate cornerOffset1 = getValidGoal(new Coordinate(2,  tempHeight/2));
		Coordinate cornerOffset2 = getValidGoal(new Coordinate(tempWidth - 14,  0));
		futureGoal.add(corner1);
		futureGoal.add(corner2);
		futureGoal.add(corner3);
		futureGoal.add(corner4);
		futureGoal.add(corner5);
		futureGoal.add(getValidGoal(new Coordinate(tempWidth / 2 , tempHeight / 2)));
		futureGoal.add(getValidGoal(new Coordinate(tempWidth / 2 - 2 , tempHeight - 10)));
		futureGoal.add(getValidGoal(cornerOffset1));
		futureGoal.add(getValidGoal(cornerOffset2));
		futureGoal.add(getValidGoal(new Coordinate(14 , 18)));
		Collections.sort(futureGoal, new priorityComparator());
	}
	
	public void traversingMapGoals() {
		Coordinate corner1 = getValidGoal(new Coordinate(2 + offset, 2));
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
			int manhantanDistance1 = getManhattanDistance(c1, new Coordinate(car.getPosition()));
			int manhantanDistance2 = getManhattanDistance(c2, new Coordinate(car.getPosition()));
			return manhantanDistance1 - manhantanDistance2;
		}
	}
	public boolean isValidGoal(Coordinate coordinate) {
		if (currentMap.get(coordinate).getType() == MapTile.Type.ROAD && !isVisited(coordinate)) {
			return true;
		}
		return false;
	}
	
	public boolean isVisited(Coordinate c1) {
		for (Coordinate c : visitedGoals) {
			if(c.equals(c1)) {
				return true;
			}
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
		checkGoalInNoKeyLava(currentView);
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
		Collections.sort(futureGoal, new priorityComparator());
		if(hasAllKeys() && exit != null) {
			futureGoal.clear();
			futureGoal.add(exit);
		}
	}
	
	public void checkGoalInNoKeyLava(HashMap<Coordinate, MapTile> currentView) {
		for (Coordinate c : currentView.keySet()) {
			if(currentView.get(c).getType() == MapTile.Type.TRAP) {
				TrapTile trapTile = (TrapTile) currentView.get(c);
				if(trapTile.getTrap().equals("lava")){
					LavaTrap lavaTrap = (LavaTrap) trapTile;
					if(lavaTrap.getKey() == 0 && futureGoal.contains(c)) {
							futureGoal.remove(c);
					}
				}
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
		futureGoal.remove(0);
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
		evaluateCurrentView(car.getView());
		if (futureGoal.size() == 0 &&(!hasAllKeys() || exit == null)) {
			offset = offset + 2;
			moreGoals(offset);
		}
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