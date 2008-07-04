package com.tilab.msn;


import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;


/**
 * The Class LocationReceiver.
 */
public class LocationReceiver extends IntentReceiver {
	
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());	 
	
	public void onReceiveIntent(Context context, Intent intent) {		
		Thread.currentThread().getId();
        
		if (intent.getAction().equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			myLogger.log(Logger.INFO, "New location received from provider! New location is: " + 
												(int)(loc.getLatitude()*1E6) + ";" + 
												(int)(loc.getLongitude()*1E6) + 
												" microdegrees");
			ContactManager.getInstance().updateMyContactLocation(loc);
			this.abortBroadcast(); 
		}
		
	}
}
