package mycontroller;

import java.util.HashMap;
import java.util.Random;

import tiles.MapTile;
import utilities.Coordinate;

public class GoalMaker {
	public int stateReward;
	Coordinate targetPoint;
	public int mapWidth;
	public int mapHeight;
	HashMap<Coordinate, MapTile> currentMap;
	
	GoalMaker (int mapWidth, int mapHeight, HashMap<Coordinate, MapTile> currentMap){
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.currentMap = currentMap;
	}
	
	public Coordinate getConers() {
		Coordinate corner1 = new Coordinate(0,0);
		Coordinate corner2 = new Coordinate(mapWidth - 1, 0);
		Coordinate corner3 = new Coordinate(mapWidth - 1, mapHeight - 1);
		Coordinate corner4 = new Coordinate(0, mapHeight - 1);
		
		
		return null;
	}
	
	
	public void evaluateCurrView(HashMap<Coordinate, MapTile> currentView, Coordinate myPos) {
		System.out.println("---------------------");
		Coordinate targetPoint = null;
		int currManhattanDistance = 0;
		int maxManhattanDistance = 0;
		for (Coordinate c : currentView.keySet()) {
			currManhattanDistance = Math.abs(myPos.x - c.x) + Math.abs(myPos.y - c.y);
			if (currManhattanDistance > maxManhattanDistance) {
				 maxManhattanDistance = currManhattanDistance;
				 targetPoint = c;
			}
		}
		this.targetPoint =  targetPoint;
		System.out.println("---------------------");
	}
	
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
		return this.targetPoint;
	}
	
}
