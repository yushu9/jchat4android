package com.tilab.msn;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.location.LocationProvider;

public class GeoNavigator {


	public static String LOCATION_UPDATE_ACTION= "com.tilab.msn.LOCATION_UPDATE";
	private static String providerName="gps";
		
	public static void setLocationProvider(String prov) {
		providerName = prov;
	}
	
	public static boolean startLocationUpdate(Context c) {
		LocationManager manager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
		Intent i = new Intent(LOCATION_UPDATE_ACTION);
		LocationProvider provider;
		
		if (providerName != null){
			provider = manager.getProvider(providerName);
		} else {
			provider = manager.getProviders().get(0);
		}
		
		 
		if (provider == null){
			return false;
		} else {
			manager.requestUpdates(provider, 0, 0, i);
			return true;
		}
	}
	
	public static void stopLocationUpdate(Context c){
		LocationManager manager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
		Intent i = new Intent(LOCATION_UPDATE_ACTION);
		manager.removeUpdates(i);
	}
	
}
