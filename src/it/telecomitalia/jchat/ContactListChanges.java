package it.telecomitalia.jchat;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains a list of new contacts added and a list of removed contacts
 * <p> 
 * Used by the agent to store all modifications to the contacts lists that should be done also to the GUI
 * 
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */

public class ContactListChanges {

	/**
	 * Instance of the Jade Logger for debugging
	 */
	private Logger myLogger = Logger.getMyLogger(ContactListChanges.class.getName());
	
	/** 
	 * List of the contacts phone number that have been added since last update
	 */
	public List<String> contactsAdded;
	
	/** 
	 * List of the contacts phone number that have been removed since last update
	 */
	public List<String> contactsDeleted;
	
	
	/**
	 * Copy constructor.
	 * <p>
	 * Makes a deep copy of the two inner lists
	 * 
	 * @param changes the {@link ContactListChanges} that should be copied
	 */
	public ContactListChanges(ContactListChanges changes){
		contactsAdded = new ArrayList<String>(changes.contactsAdded);
		contactsDeleted = new ArrayList<String>(changes.contactsDeleted);
		myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ": Copy constructor of ContactListChanges called!");
	}
	
	/**
	 * Instantiates a new contact list changes.
	 */
	public ContactListChanges(){
		contactsAdded = new ArrayList<String>();
		contactsDeleted = new ArrayList<String>();
		myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Main constructor of ContactListChanges called!");
	}
	
	
	/**
	 * Clears both the lists of changes
	 */
	public void resetChanges() {
		contactsAdded.clear();
		contactsDeleted.clear();
		myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Reset changes of ContactListChanges was called!");
	}
	
	/**
	 * Overrides Object.toString() and provides a representation of a {@link ContactListChanges} by printing 
	 * both the list of added and removed contacts 
	 */
	public String toString(){
		return "Thread "+ Thread.currentThread().getId() + ":ContactListChanges: ADDED => " + contactsAdded.toString() + "\n REMOVED => " +contactsDeleted.toString();
	}
}
