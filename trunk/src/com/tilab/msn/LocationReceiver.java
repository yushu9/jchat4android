package com.tilab.msn;

import jade.util.Logger;

import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;

public class LocationReceiver extends IntentReceiver {

	private Logger myLogger = Logger.getMyLogger(LocationReceiver.class.getName());

	
	@Override
	public void onReceiveIntent(Context context, Intent intent) {
		if (intent.getAction().equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			Contact myContact = ContactManager.getInstance().getMyContact();
			myContact.setLocation(loc);
			ContactManager.getInstance().setOngoingUpdate();
			
			myLogger.log(Logger.ALL, "Position of MyContact was updated");
		}
	}

}
