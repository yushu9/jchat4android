package com.tilab.msn;

import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.location.LocationManager;
import android.location.LocationProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class GeoNavigator.
 */
public class GeoNavigator {

	/** The Constant LOCATION_UPDATE_ACTION. */
	public static final String LOCATION_UPDATE_ACTION= "com.tilab.msn.LOCATION_UPDATE";
	
	/** The MINIMU m_ distancechang e_ fo r_ update. */
	private final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 0; // in Meters 
    
    /** The MINIMU m_ tim e_ betwee n_ update. */
    private final long MINIMUM_TIME_BETWEEN_UPDATE = 0; // in Milliseconds 

    /** The Constant myLogger. */
    private static final Logger myLogger = Logger.getMyLogger(GeoNavigator.class.getName());
    
    /** The update intent. */
    private Intent updateIntent;
    
    /** The filter. */
    private IntentFilter filter;
	
	/** The Constant DEFAULT_PROVIDER_NAME. */
	private static final String DEFAULT_PROVIDER_NAME="gps";
	
	/** The loc provider name. */
	private static String locProviderName = DEFAULT_PROVIDER_NAME;
	
	/** The location receiver. */
	private IntentReceiver locationReceiver;
	
	/** The navigator. */
	private static GeoNavigator navigator = null;
	
	/** The my context. */
	private Context myContext;
	
	/** The manager. */
	private LocationManager manager;
	
	/** The provider. */
	private LocationProvider provider;
	
	/**
	 * Gets the single instance of GeoNavigator.
	 * 
	 * @param c the c
	 * 
	 * @return single instance of GeoNavigator
	 */
	public static GeoNavigator getInstance(Context c){
		if (navigator == null)
			navigator = new GeoNavigator(c);
		return navigator;
	}
	
	
	/**
	 * Instantiates a new geo navigator.
	 * 
	 * @param c the c
	 */
	private GeoNavigator(Context c){
		locationReceiver = new LocationReceiver();
		manager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
		updateIntent = new Intent(LOCATION_UPDATE_ACTION);
		filter = new IntentFilter(LOCATION_UPDATE_ACTION);
		myContext = c;
		
		if (locProviderName != null){
			provider = manager.getProvider(locProviderName);
		} else {
			provider = manager.getProvider(DEFAULT_PROVIDER_NAME);
		}
		
		
	}
	
	/**
	 * Start location update.
	 */
	public void startLocationUpdate(){
		myLogger.log(Logger.INFO, "Starting location update...");
		manager.requestUpdates(provider, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, updateIntent);
	}
	
	/**
	 * Sets the location provider name.
	 * 
	 * @param provName the new location provider name
	 */
	public static void setLocationProviderName(String provName) {
		if (provName != null)
			locProviderName = provName;
	}
	
		
		
	/**
	 * Stop location update.
	 */
	public void stopLocationUpdate(){
		myLogger.log(Logger.INFO, "Stopping location updates....");
		manager.removeUpdates(updateIntent);
	}
	
	/**
	 * Initialize.
	 */
	public void initialize(){
		myLogger.log(Logger.INFO, "Registering the intent receiver....");
		myContext.registerReceiver(locationReceiver,filter);
	}
	
	/**
	 * Shutdown.
	 */
	public void shutdown(){
		myLogger.log(Logger.INFO, "Unregistering the intent receiver....");
		myContext.unregisterReceiver(locationReceiver);
	}
}
