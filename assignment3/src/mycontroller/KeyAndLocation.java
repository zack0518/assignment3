package mycontroller;

import utilities.Coordinate;

public class KeyAndLocation {
	public int key;
	public Coordinate location;
	
	KeyAndLocation(int key, Coordinate location){
		this.key = key;
		this.location = location;
	}
	
	public int getKey() {
		return key;
	}
	
	public Coordinate getLocaiton() {
		return location;
	}
}
