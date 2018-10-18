package mycontroller;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.TrapTile;
import utilities.Coordinate;


public class PathFinding {

	private static final int LAVA_COEFFICIENT = 100;
	private static final float MUD_COEFFICIENT = Float.MAX_VALUE;
	private static final int GRASS_COEFFICIENT = 2;
	private static final float HEALTH_COEFFICIENT = 0.1f;
	private static final float REVERSE_COEFFICIENT = 20;
	
//	private static Coordinate start;
//	private static Coordinate goal;
	private static HashMap<Coordinate, MapTile> map = new HashMap<>();
	private static HashMap<Coordinate, MapTile> currentView = new HashMap<>();
	
	private static List<Coordinate> exploreNodes; // the node visited
	private static HashMap<Coordinate, Float> unexploreNodes; // the node can see, but not visited
	private static HashMap<Coordinate, Float> costSum;
	private static HashMap<Coordinate, Coordinate> previousNode;
	private static Coordinate previousPos;
	
	
	public static List<Coordinate> aStarFindPath(Coordinate star, Coordinate goal, 
			HashMap<Coordinate, MapTile> currentMap, Coordinate previousPos1) {
		
		previousPos = previousPos1;
		map = currentMap;
		exploreNodes = new ArrayList<>();
		unexploreNodes = new HashMap<>();
		costSum = new HashMap<>();
		previousNode = new HashMap<>();
		
//		initial
		costSum.put(star, (float) 0);
		unexploreNodes.put(star, (float) 0);
		
		while (!unexploreNodes.isEmpty()) {
			Coordinate currentNode = getMinFScoreNodeInUnexplore();
			unexploreNodes.remove(currentNode);
			exploreNodes.add(currentNode);
			
			// meet the goal, return the path
			if (currentNode.equals(goal)) {		
				// need be change here
				return getThePathList(currentNode);
			}
			
//			List<Coordinate> neighbors = getNeighbors(currentNode, currentView);
			
			List<Coordinate> neighbors = getNeighbors(currentNode);
			for (Coordinate c: neighbors) {
				if (exploreNodes.contains(c)) {
					continue;
				}
				
				// put the nodes which we can see but not visited into unexploreNodes
				if (!unexploreNodes.containsKey(c)) {
					unexploreNodes.put(c, Float.MAX_VALUE);
				}
				
				float gCost = costSum.get(currentNode) + getCost(currentNode, c, currentView);
				if (costSum.containsKey(c) ){
					if (gCost >= costSum.get(c)) {
						continue;
					}
				}
				
//				System.out.print(c);
//				System.out.print(" the neighbor ");
//				System.out.println(gCost);
				previousNode.put(c, currentNode);
				costSum.put(c, gCost);

                float fCost = gCost + util.getManhattanDistance(c, goal);
				unexploreNodes.put(c, fCost);
			}
		}
		
		return null;	
	}
	

	private static List<Coordinate> getThePathList(Coordinate currentNode) {
		// TODO Auto-generated method stub
	    List<Coordinate> path = new ArrayList<>();
	    path.add(currentNode);
	    Coordinate current = currentNode;
	    while (previousNode.containsKey(current)) {
	      current = previousNode.get(current);
	      path.add(current);
	    }
	    Collections.reverse(path);
	    return path;
	}

	private static Float getCost(Coordinate currentNode, Coordinate neighbor, HashMap<Coordinate, MapTile> currentView) {
		// TODO Auto-generated method stub
		// for route, gCost = manhattanDistance
		float gCost = getManhattanDistance(currentNode, neighbor);
		// for Trap, multiple by coefficient
	    MapTile neighborTile = map.get(neighbor);
	    
	    if (currentNode.equals(previousPos)) {
			gCost *= REVERSE_COEFFICIENT;
		}

	    if (neighborTile != null && neighborTile.isType(MapTile.Type.TRAP)) {
	    	TrapTile trapTile = (TrapTile) neighborTile;
	    	if (trapTile.getTrap().equals("lava")) {
				gCost *= LAVA_COEFFICIENT;
			}
	    	
	    	if (trapTile.getTrap().equals("health")) {
	    		gCost *= HEALTH_COEFFICIENT;
	    	}
	    	
	    	if (trapTile.getTrap().equals("grass")) {
	    		gCost *= GRASS_COEFFICIENT;
	    	}
	    	
	    	if (trapTile.getTrap().equals("mud")) {
	    		gCost *= MUD_COEFFICIENT;
	    	}
	    	 	
		}
	    
	    
	    return gCost;
		
	}
	
	private static List<Coordinate> getNeighbors(Coordinate currentNode) {
		// TODO Auto-generated method stub
		List<Coordinate> neighbors = new ArrayList<>();
		for (Coordinate c: map.keySet()) {
			int distance = getManhattanDistance(c, currentNode);
			MapTile nodeType = map.get(c);
			if (distance == 1 && !(nodeType.isType(Type.WALL) || nodeType.isType(Type.EMPTY))) {
				neighbors.add(c);
			}
		}

		return neighbors;
	}

	private static Coordinate getMinFScoreNodeInUnexplore() {
		// TODO Auto-generated method stub
		return Collections.min(unexploreNodes.entrySet(), Map.Entry.comparingByValue()).getKey();
	}
	
	
	public static int getManhattanDistance(Coordinate a, Coordinate b){

		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}
	
	
	
}