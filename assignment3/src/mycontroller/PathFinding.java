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
	
	// set COEFFICIENT when calculate the g-cost
	private static final int LAVA_COEFFICIENT = 100;
	private static final float MUD_COEFFICIENT = Float.MAX_VALUE;
	private static final int GRASS_COEFFICIENT = 2;
	private static final float HEALTH_COEFFICIENT = 0.1f;
	
	// updated map according to the current view
	private static HashMap<Coordinate, MapTile> map = new HashMap<>();
	
	private static List<Coordinate> exploreNodes; // the node visited
	private static HashMap<Coordinate, Float> unexploreNodes; // the node can see, but not visited
	private static HashMap<Coordinate, Float> costSum;
	private static HashMap<Coordinate, Coordinate> previousNode;
	
	
    /**
     * Use A star to find a path.
     * @param start is the start node.
     * @param goal is the goal node.
     * @param currentMap is the map we explored and updated.
     * @param previousPos is the node we visited at last moment.
     * @return a list of Coordinate from start to goal.
     */
	public static List<Coordinate> aStarFindPath(Coordinate start, Coordinate goal, 
			HashMap<Coordinate, MapTile> currentMap) {
		
		map = currentMap;
		exploreNodes = new ArrayList<>();
		unexploreNodes = new HashMap<>();
		costSum = new HashMap<>();
		previousNode = new HashMap<>();
		
		// put the start node into the unexplored nodes list and start with it.
		costSum.put(start, (float) 0);
		unexploreNodes.put(start, (float) 0);
		
		while (!unexploreNodes.isEmpty()) {
			Coordinate currentNode = getMinFScoreNodeInUnexplore();
			unexploreNodes.remove(currentNode);
			exploreNodes.add(currentNode);
			
			// meet the goal, return the path
			if (currentNode.equals(goal)) {		
				// need be change here
				return getThePathList(currentNode);
			}
			
			// iterator all the all the neighbors (Manhattan distance = 1) for current node
			List<Coordinate> neighbors = getNeighbors(currentNode);
			for (Coordinate c: neighbors) {
				if (exploreNodes.contains(c)) {
					continue;
				}
				
				// put the nodes which we can see but not visited into unexploreNodes
				if (!unexploreNodes.containsKey(c)) {
					unexploreNodes.put(c, Float.MAX_VALUE);
				}
				// calculate the g score of each neighbors and find the smallest value
				float gCost = costSum.get(currentNode) + getCost(currentNode, c);
				if (costSum.containsKey(c) ){
					if (gCost >= costSum.get(c)) {
						continue;
					}
				}
				// get the best neighbor and record it.
				previousNode.put(c, currentNode);
				costSum.put(c, gCost);
				float fCost = gCost + getManhattanDistance(c, goal);
				unexploreNodes.put(c, fCost);
			}
		}	
		return null;	
	}
	
    /**
     * Constructed a path when give a map of nodes connections.
     * @param currentNode is the ending coordinate.
     * @return A path.
     */
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

    /**
     * Returns a heuristic cost.
     * @param currentNode is the current position.
     * @param neighbor is the neighbors of the current node.
     * @return a float value representing the gCost.
     */
	private static Float getCost(Coordinate currentNode, Coordinate neighbor) {
		
		// for route, gCost = manhattanDistance
		float gCost = getManhattanDistance(currentNode, neighbor);
		// for Trap, multiple by coefficient
	    MapTile neighborTile = map.get(neighbor);
	    
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
	
    /**
     * Returns a list of neighbors(TRAP, ROAD).
     * @param currentNode is the current position
     * @return a list of neighbors as coordinates.
     */
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

    /**
     * Get the node with lowest F-score from the unexplored nodes.
     */
	private static Coordinate getMinFScoreNodeInUnexplore() {
		// TODO Auto-generated method stub
		return Collections.min(unexploreNodes.entrySet(), Map.Entry.comparingByValue()).getKey();
	}
	
    /**
     * Get Manhattan distance between two nodes
     * @param a
     * @param b
     * @return Manhattan distance
     */
	public static int getManhattanDistance(Coordinate a, Coordinate b){
		int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		return distance;
	}
	
	
	
}