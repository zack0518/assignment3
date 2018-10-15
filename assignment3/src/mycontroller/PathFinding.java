package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.text.Position;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import tiles.*;

public class PathFinding {
	public Coordinate start;
	public Coordinate end;
	public HashMap<Coordinate, MapTile> currentMap;
	
	PathFinding(Coordinate start, Coordinate end, HashMap<Coordinate, MapTile> currentMap){
		
	}
	
	public ArrayList<Coordinate> getSuccosser(Coordinate curr, HashMap<Coordinate, MapTile> currentMap) {
		int x = curr.x;
		int y = curr.y;

		for (Coordinate c : currentMap.keySet()) {
			if (currentMap.get(c).getType() == MapTile.Type.ROAD) {
				System.out.println(c);
				System.out.println(MapTile.Type.ROAD);
				
			}
			
		}
		return null;
		
	}
}
