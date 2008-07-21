package com.tilab.msn;

import java.io.FileNotFoundException;

import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.location.LocationManager;
import android.location.LocationProvider;


/**
 * Handles the operations of starting/stopping My Contact location update by location provider. It also allows specifying 
 * a custom location provider to be used. 
 * <p>
 * Location update is issued by the LocationManager and takes place by periodically firing a broadcast Intent
 * that is caught by a customized IntentReceiver.
 * Using a mocked GPS location provider, data shall be red from a file named kml inside 
 * <code> /data/misc/location/(locProviderName) </code> where locProviderName is the name of the location provider
 * read from the strings.xml file.
 * 
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class GeoNavigator {

	/** 
	 * Custom action used for the location update Intent that is fired 
	 */
	public static final String LOCATION_UPDATE_ACTION= "com.tilab.msn.LOCATION_UPDATE";
	
	/** 
	 * Minimum distance in meters for sending new location update
	 */
	private final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 0;  
    
    /** 
     * Minimum time in milliseconds for between location updates
     */
    private final long MINIMUM_TIME_BETWEEN_UPDATE = 0;  

    /** 
     * Instance of Jade Logger for debugging
     */
    private static final Logger myLogger = Logger.getMyLogger(GeoNavigator.class.getName());
    
    /** 
     * Intent fired for location updates 
     */
    private Intent updateIntent;
    
    /** 
     * The intent filter to use when registering intent receiver. 
     */
    private IntentFilter filter;
	
	/** 
	 * The default location provider name  
	 */
	private static final String DEFAULT_PROVIDER_NAME="gps";
	
	/** 
	 * The name of the location provider to be used. 
	 */
	private static String locProviderName = DEFAULT_PROVIDER_NAME;
	
	/** 
	 * The customized intent receiver to be registered 
	 */
	private IntentReceiver locationReceiver;
	
	/** 
	 * The instance of the {@link GeoNavigator} object. 
	 */
	private static GeoNavigator navigator = null;
	
	/**
	 * Current application context 
	 */
	private Context myContext;
	
	/** 
	 * Instance of the Android location manager. 
	 */
	private LocationManager manager;
	
	/** 
	 * Instance of the current location provider. 
	 */
	private LocationProvider provider;
	
	/**
	 * Gets the single instance of GeoNavigator.
	 * 
	 * @param c the application context
	 * 
	 * @return single instance of GeoNavigator
	 * @throws FileNotFoundException 
	 */
	public static GeoNavigator getInstance(Context c) {
		if (navigator == null)
			navigator = new GeoNavigator(c);
		return navigator;
	}
	
	
	/**
	 * Instantiates a new geo navigator.
	 * Uses the static instance of the provider name (if any) or otherwise defaults to DEFAULT_PROVIDER_NAME
	 * 
	 * @param c the application context
	 */
	private GeoNavigator(Context c) {
		locationReceiver = new ContactLocationReceiver();
		manager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
		updateIntent = new Intent(LOCATION_UPDATE_ACTION);
		filter = new IntentFilter(LOCATION_UPDATE_ACTION);
		filter.addAction(SendSMSActivity.SMS_SENT_ACTION);
		filter.addAction(SendSMSActivity.SMS_ERROR_ACTION);
		myContext = c;	
		
	}
	
	/**
	 * Request the Location manager to start firing intents with location updates
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
	 * Stop the firing of broadcast intents for location update.
	 */
	public void stopLocationUpdate(){
		myLogger.log(Logger.INFO, "Stopping location updates....");
		manager.removeUpdates(updateIntent);
	}
	
	/**
	 * Performs a registration of the IntentReceiver for broadcast intents as defined by the intent filter.
	 * Please note that registration must precede the start of location updates
	 * @throws FileNotFoundException 
	 */
	public void initialize() throws FileNotFoundException{
		myLogger.log(Logger.INFO, "Registering the intent receiver....");
		myContext.registerReceiver(locationReceiver,filter);
		try {
			if (locProviderName != null){
				provider = manager.getProvider(locProviderName);
			} else {
				provider = manager.getProvider(DEFAULT_PROVIDER_NAME);
			}
		} catch (NullPointerException ex) {
			throw new FileNotFoundException("Unable to retrieve the given location provider!");
		}
		
	}
	
	/**
	 * Retracts the receiver. Please note that this method should be called after location update has been stopped.
	 * It seems an Android bug prevents this to happen correctly sometimes, because stopLocationUpdate() seems to behave 
	 * asynchronously.
	 */
	public void shutdown(){
		myLogger.log(Logger.INFO, "Unregistering the intent receiver....");
		myContext.unregisterReceiver(locationReceiver);
	}
}
