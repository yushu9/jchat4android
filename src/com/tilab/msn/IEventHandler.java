/**
 * 
 */
package com.tilab.msn;

/**
 * Generic interface for an event handler
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 *
 */
public interface IEventHandler {

	/**
	 * Handles the event
	 * @param event the event that should be handled
	 */
	public abstract void handleEvent(MsnEvent event);

}