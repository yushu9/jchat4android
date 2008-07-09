/**
 * 
 */
package com.tilab.msn;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides functionalities to create and fire events and to register listeners that should handle these events.
 * Events are defined as a public. When an event is fired, its registered handler is executed, if any.
 * Currently 
 *  
 *
 * @see MsnEvent
 * @see IEventHandler
 */
public class MsnEventMgr {

	private static MsnEventMgr theInstance = new MsnEventMgr();
	private Map<String,IEventHandler> eventMap;
	

	
	private MsnEventMgr(){
		eventMap = new HashMap<String, IEventHandler>(2);
	}
	
	
	/**
	 * @return
	 */
	public static MsnEventMgr getInstance(){
		return theInstance;
	}
	
	/**
	 * @param eventName 
	 * @return
	 */
	public MsnEvent createEvent(String eventName){
		return new MsnEvent(eventName);
	}
	
	/**
	 * This methods teaches the manager which handler should be called for each kind of event
	 * Different activities should register their own handler to change the behaviour for a given event
	 * 
	 * @param eventName name of the event that could occur
	 * @param updater the handler that should be called
	 */
	public synchronized void registerEvent(String eventName, IEventHandler updater){
		eventMap.put(eventName, updater);
	}
	
	/**
	 * This method issues an event and called its handler if any. 
	 * It is called by the agent thread each time an event takes place 
	 * @param event  the event to fire
	 */
	public synchronized void fireEvent(MsnEvent event){
		String eventName = event.getName();
		
		eventMap.get(eventName).handleEvent(event);
		
	}

}


