package com.tilab.msn;

import android.app.Activity;

public abstract class ContactsUIUpdater {
	
	protected Activity activity;
	
	public ContactsUIUpdater(Activity act){
		activity = act;
	}
	
	public void postUIUpdate(){
		activity.runOnUIThread(new MyRunnable());
	}
	
	protected abstract void handleUpdate();
	
	private class MyRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ContactsUIUpdater.this.handleUpdate();
		}
		
	}
}
