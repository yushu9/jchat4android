/**
 * 
 */
package com.tilab.msn;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an event. An event is basically a container object that carries
 * arbitrary data and has an unique name.
 * It is filled by the entity that fires the event.
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0
 *
 */
public class MsnEvent {
	
		
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
		
		/**
		 * Maps that stores event parameters
		 */
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
		public MsnEvent(String name){
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


