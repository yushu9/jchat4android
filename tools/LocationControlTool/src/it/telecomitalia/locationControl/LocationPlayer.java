/**
 * 
 */
package it.telecomitalia.locationControl;

import it.telecomitalia.locationControl.WayPointLocation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

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
	private String mSerialNum;
	
	
	public static final long DEFAULT_DELAY_MS=2000;
	
	LocationPlayer(Device d){
		emuConsole = EmulatorConsole.getConsole(d);
		routeWayPoints = null;
		isStopped = true;
		mSerialNum = d.getSerialNumber();
		isLooping = true;
		delayMs = DEFAULT_DELAY_MS;
		
		routePlayerRunnable = new Runnable(){

			public void run() {
				
				boolean goingUp = true;
				System.out.println("Player thread going UP for emulator " +  mSerialNum);
				
				if (routeWayPoints != null) {
				
					System.out.println("Player thread has " + routeWayPoints.length + " to play!");
					
					LinkedList<WayPointLocation> locations  = new LinkedList<WayPointLocation>(Arrays.asList(routeWayPoints));
					ListIterator<WayPointLocation> theIterator = locations.listIterator();
					try {
						do 
						{
							WayPointLocation nextWayPoint = null;
							//Get current element
							if (goingUp)
							    nextWayPoint= theIterator.next();
							else 
								nextWayPoint= theIterator.previous();
							    
							//Consume current element
							String msg = emuConsole.sendLocation(nextWayPoint.longitude, nextWayPoint.latitude, nextWayPoint.altitude);
							System.out.println("sendLocation has reported the following msg: " + msg + " \nlocation was " +  nextWayPoint.latitude + ";" + nextWayPoint.longitude + ";" + nextWayPoint.altitude + " to emulator " + mSerialNum);
							
							//Update boolean for next iteration
							goingUp =  (goingUp)? theIterator.hasNext() : !theIterator.hasPrevious();
							
							//Check if we were stopped
							if (isStopped)
								break;
							//wait a bit
							Thread.sleep(delayMs);
							
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
