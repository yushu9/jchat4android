package com.tilab.msn;

import jade.util.Logger;
import android.os.Handler;


/**
 * Provides an implementation of an handler that needs to post stuff on the UI thread to modify the GUI
 * Subclasses simply needs to implement the abstract method processEvent() that shall be automatically 
 * executed on the GUI thread.
 * Please note that this object has an handler inside and to work correctly MUST be created inside the UI
 * thread (if it is created in a different thread, messages shall be posted on its message queue with unpredictable 
 * results).
 * 
 *
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 * @see IEventHandler
 */

public abstract class GuiEventHandler implements IEventHandler {
	
	/**
	 * Instance of JADE Logger for debugging
	 */
	private Logger myLogger= Logger.getMyLogger(GuiEventHandler.class.getName());
	
	/** 
	 * Handler used for posting Runnables on the UI thread  
	 */
	protected Handler localHandler;
	
	/**
	 * Instantiates a new contacts {@link GuiEventHandler}
	 * 
	 */
	public GuiEventHandler() {
		localHandler = new Handler();
		myLogger.log(Logger.FINE, "Handler created by thread " + Thread.currentThread().getId());
	}
	
	
	
	/**
	 * Implements the IEventHandler.handleEvent() by posting a runnable on UI thread. 
	 * The event is passed to the Runnable for further processing.
	 * 
	 * @param event event that needs to be processed
	 * 
	 */
	public void handleEvent(MsnEvent event){
		if (event.getName().equals(MsnEvent.INCOMING_MESSAGE_EVENT)){
			localHandler.post((new MyRunnable(event)));
		} else {
			localHandler.postDelayed(new MyRunnable(event),1000);
		}
		
		myLogger.log(Logger.FINE, "Event " + event.getName() + "posted by thread " + Thread.currentThread().getId());
	}
	
	/**
	 * This abstract method provides the code that shall be executed on the UI thread inside the Runnable.
	 * Subclasses simply needs to implements this to handle UI events 
	 * 
	 * @param event the event that should be handled
	 */
	protected abstract void processEvent(MsnEvent event);
	
	/**
	 * Provides the Runnable object that shall be posted on UI thread by handleEvent() using template method pattern
	 */
	private class MyRunnable implements Runnable {

		/** 
		 * Event to be stored 
		 */
		private MsnEvent parameter;
	
		/**
		 * Instantiates a new my runnable.
		 * 
		 * @param event the event to be handled
		 */
		public MyRunnable(MsnEvent event){
			parameter = event;
		}
			
	
		/**
		 * Code to be executed on the UI thread (simply calls the abstract method defined by subclasses)
		 */
		public void run() {			
			GuiEventHandler.this.processEvent(parameter);
		}
		
	}
}
