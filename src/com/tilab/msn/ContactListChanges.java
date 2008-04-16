package com.tilab.msn;
import java.util.ArrayList;
import java.util.List;


public class ContactListChanges {

	public List<String> contactsAdded;
	public List<String> contactsDeleted;
	
	
	public ContactListChanges(ContactListChanges changes){
		this.contactsAdded = new ArrayList<String>(changes.contactsAdded);
		this.contactsDeleted = new ArrayList<String>(changes.contactsDeleted);
	}
	
	public ContactListChanges(){
		contactsAdded = new ArrayList<String>();
		contactsDeleted = new ArrayList<String>();
	}
	
	public void resetChanges() {
		contactsAdded.clear();
		contactsDeleted.clear();
	}
	
}
