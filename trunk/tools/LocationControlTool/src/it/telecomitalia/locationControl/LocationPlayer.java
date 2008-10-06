/**
 * 
 */
package it.telecomitalia.locationControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.android.ddmlib.Device;
import com.android.ddmlib.EmulatorConsole;

/**
 * Plays a list of location on a specific device
 * @author s.semeria
 *
 */
public class LocationPlayer {

	
	private EmulatorConsole emuConsole;
	private WayPointLocation[] routeWayPoints;
    private volatile boolean isStopped;
	private Thread mPlayerThread;
	//Runnable that sends data to a given device
	private final Runnable routePlayerRunnable; 
	private volatile boolean isLooping;
	private long delayMs;
	private Object monitor;
	private String mSerialNum;
	
	
	public static final long DEFAULT_DELAY_MS=2000;
	
	LocationPlayer(Device d){
		emuConsole = EmulatorConsole.getConsole(d);
		routeWayPoints = null;
		isStopped = true;
		monitor = new Object();
		mSerialNum = d.getSerialNumber();
		isLooping = true;
		delayMs = DEFAULT_DELAY_MS;
		
		routePlayerRunnable = new Runnable(){

			public void run() {
				
				boolean goingUp = true;
				
				
				if (routeWayPoints != null) {
				
					LinkedList<WayPointLocation> locations  = new LinkedList<WayPointLocation>(Arrays.asList(routeWayPoints));
					Iterator<WayPointLocation> theIterator = locations.iterator();
					try {
						do {
							while (theIterator.hasNext()){
								
							
								if (isStopped) 
									break;
								
								
								WayPointLocation nextWayPoint = theIterator.next();
								emuConsole.sendLocation(nextWayPoint.longitude, nextWayPoint.latitude, nextWayPoint.altitude);
								Thread.sleep(delayMs);
							}
							goingUp = !goingUp;
							theIterator = (goingUp)? locations.iterator() : locations.descendingIterator();
							
						} while (!isStopped && isLooping);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		
	}
	
	public String getSerialNumber(){
		return mSerialNum;
	}
	
	public void setRouteData(WayPointLocation[] waypoints){
		routeWayPoints = waypoints;
	}
	
	public WayPointLocation[] getRouteData(){
		return routeWayPoints;
	}
	
	public synchronized void play(){
		isStopped = false;
		mPlayerThread = new Thread(routePlayerRunnable);
		mPlayerThread.start();		
	}
	
	
	public void stop(){
		isStopped = true;
		try {
			if (mPlayerThread != null)
			   mPlayerThread.join(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setLoopMode(boolean loopMode){
		isLooping = loopMode;
	}
	
	public void setDelay(long delayMs){
		this.delayMs = delayMs;
	}
	
	public boolean getLoopMode(){
		return isLooping;
	}
	
	public long getDelay(){
		return this.delayMs;
	}
	
	public boolean isPlaying(){
		return !isStopped;
	}
	
	
}
