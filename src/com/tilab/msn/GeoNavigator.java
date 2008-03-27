package com.tilab.msn;

import jade.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.pim.ContactPickerActivity.MyContentObserver;

public class GeoNavigator {


	public static final String LOCATION_UPDATE_ACTION= "com.tilab.msn.LOCATION_UPDATE";
	private final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 0; // in Meters 
    private final long MINIMUM_TIME_BETWEEN_UPDATE = 0; // in Milliseconds 

    private static final Logger myLogger = Logger.getMyLogger(GeoNavigator.class.getName());
    private Intent updateIntent;
    private IntentFilter filter;
	private static final String DEFAULT_PROVIDER_NAME="gps";
	private static String locProviderName = DEFAULT_PROVIDER_NAME;
	private IntentReceiver locationReceiver;
	private static GeoNavigator navigator = null;
	private Context myContext;
	private LocationManager manager;
	private LocationProvider provider;
	
	public static GeoNavigator getInstance(Context c){
		if (navigator == null)
			navigator = new GeoNavigator(c);
		return navigator;
	}
	
	
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
	
	public void startLocationUpdate(){
		manager.requestUpdates(provider, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, updateIntent);
		myLogger.log(Logger.INFO, "Registering the intent receiver");	
		myContext.registerReceiver(locationReceiver, filter);
	}
	
	public static void setLocationProviderName(String provName) {
		if (provName != null)
			locProviderName = provName;
	}
	
		
		
	public void stopLocationUpdate(){
		myLogger.log(Logger.INFO, "Unregistering the intent receiver");
		myContext.unregisterReceiver(locationReceiver);
		LocationManager manager = (LocationManager)myContext.getSystemService(Context.LOCATION_SERVICE);
		manager.removeUpdates(updateIntent);
	}
	
}
