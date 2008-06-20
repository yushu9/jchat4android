package com.tilab.msn;
import java.util.ArrayList;
import java.util.List;


/**
 * The Class ContactListChanges.
 */
public class ContactListChanges {

	/** The contacts added. */
	public List<String> contactsAdded;
	
	/** The contacts deleted. */
	public List<String> contactsDeleted;
	
	
	/**
	 * Instantiates a new contact list changes.
	 * 
	 * @param changes the changes
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
	 * Reset changes.
	 */
	public void resetChanges() {
		contactsAdded.clear();
		contactsDeleted.clear();
	}
	
}
