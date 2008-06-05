package com.tilab.msn;

import android.app.Activity;

public abstract class ContactsUIUpdater {
	
	protected Activity activity;
	protected Object data;
	
	public ContactsUIUpdater(Activity act){
		activity = act;
	}
	
	public Object retrieveExtraData(){
		return data;
	}
	
	public void postUIUpdate(Object obj){
		activity.runOnUIThread(new MyRunnable(obj));
	}
	
	protected abstract void handleUpdate(Object parameter);
	
	private class MyRunnable implements Runnable {

		private Object parameter;
	
		public MyRunnable(Object obj){
			parameter = obj;
		}
			
		public void run() {			
			ContactsUIUpdater.this.handleUpdate(parameter);
		}
		
	}
}
