package mycontroller;

import tiles.MapTile;
import tiles.TrapTile;
import world.Car;

public class MudMovement extends Movement{
	
	Car car;
	MudMovement(Car car){
		this.car = car;
	}
	@Override
	public void movement() {
		if(car.getView().get(car.getPosition()).getType() == MapTile.Type.TRAP) {
			TrapTile currTrap = (TrapTile) car.getView().get(car.getPosition());
			if(currTrap.getTrap().equals("mud")) {
				car.applyReverseAcceleration();	
			}
		}
	}

	@Override
	public boolean shouldStop() {
		return false;
	}

	@Override
	public boolean shouldCancelGoal() {
		return true;
	}

}
