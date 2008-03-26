package com.tilab.msn;

import jade.lang.acl.ACLMessage;
import android.app.Application;

public class TilabMsnApplication extends Application {
	
	public ContactsUpdaterBehaviour myBehaviour;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		myBehaviour = new ContactsUpdaterBehaviour(Long.parseLong(getString(R.string.contacts_update_time)));
	}
	
	
}
