package mycontroller;

import utilities.Coordinate;

public class util {

    public static int getManhattanDistance(Coordinate a, Coordinate b){

        int distance = Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        return distance;
    }
}
