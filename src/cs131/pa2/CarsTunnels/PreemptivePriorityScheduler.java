package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.EventType;
import cs131.pa2.Abstract.Log.Log;

public class PreemptivePriorityScheduler extends Tunnel{
	
	// locks and condition variable;
	final Lock lock = new ReentrantLock();
	final Condition enter = lock.newCondition();
	public  Condition ambulance = lock.newCondition();
	public  Condition pullover = lock.newCondition();
	
	public Collection<Tunnel> tunnels;
	
	// The WaitQueue for Vehicles
	private Queue<Vehicle> waitQueue;
	
	// Keeps track of cars in tunnels and tunnels with cars
	Map<Vehicle, Tunnel> ListTunnelVehicle  = new HashMap<>();
	Map<Tunnel, Vehicle> TunToCAr  = new HashMap<>();
	
	// Keeps track of ambulances in tunnels and tunnels with ambulances
	Map<Tunnel, Vehicle> AmbulanceLogMap = new HashMap<Tunnel, Vehicle>();
	Map<Vehicle, Tunnel> AMToTunn = new HashMap<Vehicle, Tunnel>();
	
	Map<Tunnel, Vehicle> TunnelLogMap = new HashMap<Tunnel, Vehicle>();
	// Keeps track of waiting vehicles 
	Map<Vehicle, Tunnel> waiting = new HashMap<Vehicle, Tunnel>();
	Map<Tunnel, Condition> withAM = new HashMap<Tunnel, Condition>();
	
	Map<Tunnel, Condition> noAM = new HashMap<Tunnel, Condition>();
	
	Map<Tunnel, List<Vehicle>> map = new HashMap<>();
	
	// List of all the vehicles
	public ArrayList<Vehicle> vehicleList = new ArrayList<Vehicle>();
	ArrayList<Vehicle> test = new ArrayList<Vehicle>();
	
	// Queue specific to the ambulances
	Queue<Vehicle> ambulanceQ = new LinkedList<Vehicle>();

	Log log;
	
	public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name, log);
		this.tunnels = tunnels;
		this.waitQueue = new PriorityQueue<>(new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				return v2.getPriority() - v1.getPriority();
			}
		});
		 addTun();
		 
		 
		
	}
 
	// Maps tunnels to lists of vehicles
	private void addTun() {
		for(Tunnel t: tunnels) {
			ArrayList<Vehicle> arr = new ArrayList<Vehicle>();
			map.put(t, arr);
		}
		
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		try {
			System.out.println("vehicle that is trying to enter " + vehicle + " with direction " + vehicle.getDirection());
			if(vehicle instanceof Ambulance) {
				ambulanceQ.add(vehicle);
			}else {
				waitQueue.add(vehicle);
			}
			// Calls a method to get an available tunnel for the vehicle ot enter
			Tunnel t = checkAllT(vehicle);
			
			if(!(vehicle instanceof Ambulance)) {
				while (!MaxPri(vehicle) ||  t == null && ambulanceQ.isEmpty()) { 
						enter.await();	
						t = checkAllT(vehicle);
				}
				lock.unlock();
				waitQueue.remove();
				return true;
			}
			
			// Dealing with ambulances
			if(vehicle instanceof Ambulance && t != null) {
				// Get a list of all the vehicles in the tunnel t
				List<Vehicle> list = map.get(t);
				
				// Loop thru all the vehicles in the tunnel 
				for(int i = 0;i < list.size()-1; i++) {
					list.get(i).lock.lock();
					list.get(i).pullOver.signalAll();
					list.get(i).lock.unlock();
				}
					
			} else if (vehicle instanceof Ambulance){
				while(checkAllT(vehicle) == null){
					// Await the ambulance here 
					ambulance.await();
				}
			}
			
	
			if (!(vehicle instanceof Ambulance)) {
				waitQueue.poll(); 
			} else {
				if(ambulanceQ.isEmpty()) {
					//do nothing 
				}else {
					ambulanceQ.remove();
				}
			}
			
			lock.unlock();
			return true;  
	
		} catch (InterruptedException  e) {
			e.printStackTrace();
		}

		return false;
	}
	
	
	
	
	
	// Checks to find a possible tunnel for entry of a vehicle
	private Tunnel checkAllT(Vehicle vehicle) {
		for(Tunnel t: tunnels) {
			if(canEnter(vehicle, t)) {
				return t;
			}
		}
		return null;	
	}

	public boolean MaxPri(Vehicle vehicle) {  
		return vehicle.equals(this.waitQueue.peek());
	}
	
	// Checks if a vehicle can successfully enter a tunnel
	public boolean canEnter(Vehicle v, Tunnel t) {
		// List of vehicles in the tunnel
		List<Vehicle> vList = map.get(t);
		if(vList.size() == 0) {
			if(v instanceof Ambulance) {
				vList.add(v);
				AmbulanceLogMap.put(t, v);
				AMToTunn.put(v,t);
				return true;
			}else {
				vList.add(v);
				ListTunnelVehicle.put(v, t) ;
				TunToCAr.put(t, v);
				System.out.println("adding " + v + " to tunnel " + t);
				return true;
			}
		}
		
		for(int i = 0; i < vList.size(); i++) {
			if(AmbulanceLogMap.containsKey(t)) {
				System.out.println("there is a amb in tunnel");
				System.out.println();
				System.out.println("we are waiting here for the abulance");
				return false;
			}
			
			
			if(vList.size() == 3 && !(v instanceof Ambulance)) {
				System.out.println("we are full");
				return false;
			}else if(vList.get(i) instanceof Sled && v instanceof Ambulance) {
				if(vList.size() == 0 || vList.get(i).getDirection().equals(v.getDirection()) && AmbulanceLogMap.containsKey(t) ) {
				   vList.add(v);
				   AmbulanceLogMap.put(t, v);
				   AMToTunn.put(v,t);
				   withAM.put(t, pullover);
				   return true;
				}
			}else if(vList.get(i) instanceof Sled) {
				System.out.println("there is a sled");
				return false;
			}else if(vList.get(i).getDirection().equals(v.getDirection())) {
				if(v instanceof Ambulance) {
					vList.add(v);
					AmbulanceLogMap.put(t, v);
					AMToTunn.put(v,t);
					return true;
				}
				vList.add(v);
				ListTunnelVehicle.put(v, t) ;
				TunToCAr.put(t, v);
				return true;
			}
			
		}
		return false;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		try {
			lock.lock();
			test.add(vehicle);
			if(vehicle instanceof Ambulance) {
				Tunnel t = AMToTunn.get(vehicle);
				AMToTunn.remove(vehicle);
				List<Vehicle> list = map.get(t);
				
				for(int i = 0; i <list.size();i++) {
					if(list.get(i).equals(vehicle)) {
						System.out.println("removing ambulance");
						list.remove(i);
					}
				}
				
				for(int i = 0; i < list.size();i++) {
					list.get(i).lock.lock();
					list.get(i).pullOver.signalAll();
					list.get(i).amb.signalAll();
					list.get(i).lock.unlock();

				}
				System.out.println("Left " + vehicle);
				if(!ambulanceQ.isEmpty()) {
					ambulance.signalAll();
				}
				
				enter.signalAll(); 
				
			}else {
				Tunnel t = ListTunnelVehicle.get(vehicle);
				ListTunnelVehicle.remove(vehicle);
				TunToCAr.remove(t);
				
				List<Vehicle> list = map.get(t);
				
				for(int i = 0; i < list.size();i++) {
					if(list.get(i).equals(vehicle)) {
						list.remove(i);
					}
				}
				System.out.println("enter sig all");
				enter.signalAll(); 
				
				if(!ambulanceQ.isEmpty())
					ambulance.signalAll();
				
			}
		} finally {
			lock.unlock();
			//enter.signalAll(); 
		}
	}
}
	
	

