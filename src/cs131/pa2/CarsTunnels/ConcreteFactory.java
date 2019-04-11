package cs131.pa2.CarsTunnels;

import java.util.Collection;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Factory;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class ConcreteFactory implements Factory {

    @Override
    public Tunnel createNewBasicTunnel(String label){
    		BasicTunnel bTunnel = new BasicTunnel(label);
    		return bTunnel;
    		//throw new UnsupportedOperationException("Not supported yet.");    
    }

    @Override
    public Vehicle createNewCar(String label, Direction direction){
    		Vehicle newCar = new Car(label,direction);
    		return newCar;
    		//throw new UnsupportedOperationException("Not supported yet.");    
    }

    @Override
    public Vehicle createNewSled(String label, Direction direction){
    	Vehicle newSled = new Sled(label, direction);
    	return newSled;
    	
    	
    	//throw new UnsupportedOperationException("Not supported yet.");    
    }

    @Override
    public Tunnel createNewPriorityScheduler(String label, Collection<Tunnel> tunnels, Log log){
    	PriorityScheduler newcNewPriorityScheduler = new PriorityScheduler(label, tunnels, log);
    	System.out.println(log.toString());
    	return newcNewPriorityScheduler;
    	
    	
    	//throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public Vehicle createNewAmbulance(String label, Direction direction) {
		return new Ambulance(label,direction);
		
		
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Tunnel createNewPreemptivePriorityScheduler(String label, Collection<Tunnel> tunnels, Log log) {
		return new PreemptivePriorityScheduler(label, tunnels, log);
		
		
		
		//throw new UnsupportedOperationException("Not supported yet.");
	}
}
