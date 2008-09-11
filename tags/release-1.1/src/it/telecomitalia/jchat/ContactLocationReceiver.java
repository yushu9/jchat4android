/*****************************************************************
 jChat is a  chat application for Android based on JADE
  Copyright (C) 2008 Telecomitalia S.p.A. 
 
 GNU Lesser General Public License

 This is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this software; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.telecomitalia.jchat;


import jade.util.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
public class ContactLocationReceiver extends BroadcastReceiver {
	
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
	public void onReceive(Context context, Intent intent) {		
		
        String action = intent.getAction();
       
        
		if (action.equals(GeoNavigator.LOCATION_UPDATE_ACTION)){
			Location loc = (Location) intent.getParcelableExtra("location");
			
			this.abortBroadcast(); 
		}
	}


}
