package com.tilab.msn;
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
		this.contactsAdded = new ArrayList<String>(changes.contactsAdded);
		this.contactsDeleted = new ArrayList<String>(changes.contactsDeleted);
	}
	
	/**
	 * Instantiates a new contact list changes.
	 */
	public ContactListChanges(){
		contactsAdded = new ArrayList<String>();
		contactsDeleted = new ArrayList<String>();
	}
	
	/**
	 * Clears both the lists of changes
	 */
	public void resetChanges() {
		contactsAdded.clear();
		contactsDeleted.clear();
	}
	
}
