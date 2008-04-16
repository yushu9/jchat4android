package com.tilab.msn;

import jade.util.Logger;

import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;
import android.util.Log;

public class LocationReceiver extends IntentReceiver {

	
	
	@Override
	public void onReceiveIntent(Context context, Intent intent) {
		if (intent.getAction().equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			Contact myContact = ContactManager.getInstance().getMyContact();
			myContact.setLocation(loc);
			ContactManager.getInstance().setOngoingUpdate();
			this.abortBroadcast(); 
		}
	}

}
