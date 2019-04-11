
package cs131.pa2.CarsTunnels;

import java.util.Collection;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityScheduler extends Tunnel{
	private Collection<Tunnel> tunnels;
	
	private Queue<Vehicle> waitQueue;
	
	 Map<Vehicle, Tunnel> ListTunnelVehicle  = new HashMap<>();
	
	final Lock lock = new ReentrantLock();
	
	final Condition enter = lock.newCondition();
	

	public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name, log);
		
		this.tunnels = tunnels;
		
		this.waitQueue = new PriorityQueue<>(new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				return v2.getPriority() - v1.getPriority();
			}
		});
		
	}
	
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		
		
		try {
			lock.lock();
			waitQueue.add(vehicle);                                  
			
			while (!MaxPri(vehicle) ||  !maybeEnter(vehicle)) { 
				
				enter.await();
				System.out.println(waitQueue.toString());
			}
			waitQueue.poll(); 
			lock.unlock();
			return true;      
		
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
		}
		return false;
	}
	
	public boolean maybeEnter(Vehicle vehicle) {  
		for (Tunnel tunnel: tunnels) {
			if (tunnel.tryToEnterInner(vehicle)) {
				ListTunnelVehicle.put(vehicle, tunnel);        
				return true;
			}
		}
		return false;
	}
	
	public boolean MaxPri(Vehicle vehicle) {  
		return vehicle.equals(this.waitQueue.peek());
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			ListTunnelVehicle.get(vehicle).exitTunnelInner(vehicle);
			enter.signalAll(); 
		} finally {
			lock.unlock();
		}	
	}
	
}	