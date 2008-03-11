package com.tilab.msn;

import android.app.Application;

public class TilabMsnApplication extends Application {
	
	public ContactsUpdaterBehaviour myBehaviour;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		myBehaviour = new ContactsUpdaterBehaviour(getString(R.string.msn_service_desc_name), 
				 getString(R.string.msn_service_desc_type), 
				 Long.parseLong(getString(R.string.contacts_update_time)));
	}
	
	
}
