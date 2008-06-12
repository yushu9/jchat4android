package com.tilab.msn;

import android.app.Activity;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactsUIUpdater.
 */
public abstract class ContactsUIUpdater {
	
	/** The activity. */
	protected Activity activity;
	
	/** The data. */
	protected Object data;
	
	/**
	 * Instantiates a new contacts ui updater.
	 * 
	 * @param act the act
	 */
	public ContactsUIUpdater(Activity act){
		activity = act;
	}
	
	/**
	 * Retrieve extra data.
	 * 
	 * @return the object
	 */
	public Object retrieveExtraData(){
		return data;
	}
	
	/**
	 * Post ui update.
	 * 
	 * @param obj the obj
	 */
	public void postUIUpdate(Object obj){
		activity.runOnUIThread(new MyRunnable(obj));
	}
	
	/**
	 * Handle update.
	 * 
	 * @param parameter the parameter
	 */
	protected abstract void handleUpdate(Object parameter);
	
	/**
	 * The Class MyRunnable.
	 */
	private class MyRunnable implements Runnable {

		/** The parameter. */
		private Object parameter;
	
		/**
		 * Instantiates a new my runnable.
		 * 
		 * @param obj the obj
		 */
		public MyRunnable(Object obj){
			parameter = obj;
		}
			
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {			
			ContactsUIUpdater.this.handleUpdate(parameter);
		}
		
	}
}
