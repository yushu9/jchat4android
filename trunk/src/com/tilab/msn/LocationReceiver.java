package com.tilab.msn;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.location.Location;

public class LocationReceiver extends IntentReceiver {

	@Override
	public void onReceiveIntent(Context context, Intent intent) {
		if (intent.getAction().equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			
			Contact myContact = ContactManager.getInstance().getMyContact();
			myContact.setLocation(loc);
		}
	}

}
