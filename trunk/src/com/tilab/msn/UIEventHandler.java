package com.tilab.msn;

import jade.util.Logger;
import android.os.Handler;


/**
 * The Class ContactsUIUpdater.
 */
public abstract class UIEventHandler {
	
	private Logger myLogger= Logger.getMyLogger(UIEventHandler.class.getName());
	
	/** The local handler. */
	protected Handler localHandler;
	
	/**
	 * Instantiates a new contacts {@link UIEventHandler}
	 * 
	 */
	public UIEventHandler() {
		localHandler = new Handler();
		myLogger.log(Logger.FINE, "Handler created by thread " + Thread.currentThread().getId());
	}
	
	
	
	/**
	 * Post ui update.
	 * 
	 * @param obj the obj
	 */
	public void postEvent(MsnEventMgr.Event event){
		localHandler.post((new MyRunnable(event)));
		myLogger.log(Logger.FINE, "Event posted by thread " + Thread.currentThread().getId());
	}
	
	/**
	 * Handle update.
	 * 
	 * @param parameter the parameter
	 */
	protected abstract void handleEvent(MsnEventMgr.Event parameter);
	
	/**
	 * The Class MyRunnable.
	 */
	private class MyRunnable implements Runnable {

		/** The parameter. */
		private MsnEventMgr.Event parameter;
	
		/**
		 * Instantiates a new my runnable.
		 * 
		 * @param obj the obj
		 */
		public MyRunnable(MsnEventMgr.Event event){
			parameter = event;
		}
			
	
		public void run() {			
			UIEventHandler.this.handleEvent(parameter);
		}
		
	}
}
