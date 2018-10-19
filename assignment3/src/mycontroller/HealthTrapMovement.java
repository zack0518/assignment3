package mycontroller;

import world.Car;

public class HealthTrapMovement extends Movement {
	private Car car;
	boolean stop;
	HealthTrapMovement(Car car){
		this.car = car;
		stop = false;
	}
	@Override
	public void movement() {
		car.brake();
		stop = true;
		if (car.getHealth() == 100) {
			stop = false;
		}
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean shouldStop() {
		// TODO Auto-generated method stub
		return stop;
	}
	@Override
	public boolean shouldCancelGoal() {
		// TODO Auto-generated method stub
		return false;
	}

}
