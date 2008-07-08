/**
 * 
 */
package com.tilab.msn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author s.semeria
 *
 */
public class MsnEventMgr {

	private static MsnEventMgr theInstance = new MsnEventMgr();
	private Map<String,UIEventHandler> eventMap;
	

	
	private MsnEventMgr(){
		eventMap = new HashMap<String, UIEventHandler>(2);
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
	public Event createEvent(String eventName){
		return new Event(eventName);
	}
	
	/**
	 * This methods teaches the manager which handler should be called for each kind of event
	 * Different activities should register their own handler to change the behaviour for a given event
	 * 
	 * @param eventName name of the event that could occur
	 * @param updater the handler that should be called
	 */
	public synchronized void registerEvent(String eventName, UIEventHandler updater){
		eventMap.put(eventName, updater);
	}
	
	/**
	 * This method issues an event and called its handler if any. 
	 * It is called by the agent thread each time an event takes place 
	 * @param event  the event to fire
	 */
	public synchronized void fireEvent(Event event){
		String eventName = event.getName();
		
		eventMap.get(eventName).postEvent(event);
		
	}

	/**
	 * This is an event. An event is basically a container object that carries
	 * arbitrary data and has an unique name.
	 * It is filled by the entity that fires the event
	 * 
	 * @author s.semeria
	 *
	 */
	public class Event {
		
		/**
		 * Event that is fired each time an update should be performed
		 */
		public static  final String TICK_EVENT="TICK_EVENT";
		/**
		 * Event that is fired when a new message arrives
		 */
		public static  final String INCOMING_MESSAGE_EVENT="INCOMING_MESSAGE_EVENT";
		/**
		 * Event that is fired when a refresh of the view is needed ()
		 */
		public static final String VIEW_REFRESH_EVENT="VIEW_REFRESH_EVENT";
		/**
		 * Event that is fired when a contact disconnects
		 */
		public static final String CONTACT_DISCONNECT_EVENT="CONTACT_DISCONNECT_EVENT";
		
		
		//Parameters defined for INCOMING MSG EVENT
		/**
		 * Name of parameter 
		 */
		public static final String INCOMING_MESSAGE_PARAM_MSG="INCOMING_MESSAGE_PARAM_MSG";
		/**
		 * Name of parameter 
		 */
		public static final String INCOMING_MESSAGE_PARAM_SESSIONID="INCOMING_MESSAGE_PARAM_SESSIONID";
		
		//Parameters defined for VIEW REFRESH EVENT
		/**
		 * Name of parameter 
		 */
		public static final String VIEW_REFRESH_PARAM_LISTOFCHANGES="VIEW_REFRESH_PARAM_LISTOFCHANGES";
		
		//Parameters defined for CONTACT DISCONNECT EVENT
		/**
		 * Name of parameter 
		 */
		public static final String CONTACT_DISCONNECT_PARAM_CONTACTNAME="CONTACT_DISCONNECT_PARAM_CONTACTNAME";
		
		/**
		 * Name of the event
		 */
		private  String name;
		private Map<String, Object> paramsMap;
		
		/**
		 * Returns the name of the event
		 * @return event name
		 */
		public final String getName() {
			return name;
			
		}
		
		/**
		 * Builds a new event
		 * @param name name of the event
		 */
		public Event(String name){
			this.name = name;
		}
		
		/**
		 * Adds a parameter to the event using the given name
		 * 
		 * @param name name of the parameter
		 * @param value value to be added
		 */
		public void addParam(String name, Object value){
			if (paramsMap == null){
				paramsMap = new HashMap<String, Object>();
			}
			
			paramsMap.put(name, value);
		}
		
		/**
		 * Retrieves a parameter from an event
		 * 
		 * @param name of the parameter to retrieve
		 * @return value of the parameter
		 */
		public Object getParam(String name){
			return paramsMap.get(name);
		}
	}
}


