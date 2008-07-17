package com.tilab.msn;


import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;


/**
 * Custom intent receiver that shall capture broadcast intent fired at each location update by mocked GPS location provider.
 * <p>
 * This receiver shall be instantiated and registered to Android runtime in GeoNavigator class, using a suitable Intent filter
 * 
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 * @see GeoNavigator
 */
public class ContactLocationReceiver extends IntentReceiver {
	
	/**
	 * Instance of JADE Logger for debugging  
	 */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());	 
	
	/**
	 * Overrides IntentReceiver.onReceiveIntent() updating directly the myContact location 
	 * from contact manager.
	 * 
	 * @param context application context
	 * @param intent broadcasted intent
	 */
	public void onReceiveIntent(Context context, Intent intent) {		
		
        String action = intent.getAction();
       
        
		if (action.equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			ContactManager.getInstance().updateMyContactLocation(loc);
			this.abortBroadcast(); 
		}
	}
}
