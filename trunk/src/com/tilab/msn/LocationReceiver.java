package com.tilab.msn;


import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;


// TODO: Auto-generated Javadoc
/**
 * The Class LocationReceiver.
 */
public class LocationReceiver extends IntentReceiver {
	
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());	 
	
	/* (non-Javadoc)
	 * @see android.content.IntentReceiver#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	public void onReceiveIntent(Context context, Intent intent) {		
		Thread.currentThread().getId();
        myLogger.log(Logger.INFO, "onReceiveIntent called: My currentThread has this ID: " + Thread.currentThread().getId());
		if (intent.getAction().equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			ContactManager.getInstance().updateMyContactLocation(loc);
			this.abortBroadcast(); 
		}
		
	}
}
