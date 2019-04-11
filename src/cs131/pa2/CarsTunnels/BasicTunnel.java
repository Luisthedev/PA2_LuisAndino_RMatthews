package cs131.pa2.CarsTunnels;

import java.util.ArrayList;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
	
	 public  ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	public BasicTunnel(String name) {
		super(name);
		 
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
	// at most 3 vichles going the same direction
	// 1 sled at a time 
	//Cars and sleds cannot share a tunnel
		
		
		
		
		if (vehicleList.size() == 0){
			vehicleList.add(vehicle);
			return true;
			
		}else if (vehicleList.size() < 3 ||vehicle instanceof Ambulance ){
			for(int i = 0; i < vehicleList.size(); i++) {
				
				if(vehicleList.get(i) instanceof Sled) {
					return false;
				}else if(vehicle instanceof Sled ) {
					return false;
				}else if(vehicleList.get(i).getDirection().equals(vehicle.getDirection())){
					vehicleList.add(vehicle);
					return true;		
				}else {
					
					return false;
				}
			}
		}
		return false;
	
		
		
	}

	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		
		
		if (vehicleList.indexOf(vehicle) >= 0)
			vehicleList.remove(vehicle);
		
	}
	
}
