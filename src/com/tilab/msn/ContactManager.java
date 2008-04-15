package com.tilab.msn;

import jade.core.AID;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;


public class ContactManager {


	private static ContactManager manager = null;
	//The key of this map is the local name (phone number)
	private Map<String, Contact> otherContactsMap; 
	private Contact myContact;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private boolean updateOngoing = false;
	
	//Adapter for the contacts list
	private ContactListAdapter contactsAdapter;
	
	//List that registers ids of all newly added contacts
	private List<String> newlyAddedContacts;
	//List that registers ids of recently removed contacts
	private List<String> contactsToDelete;
	

	public boolean updateIsOngoing(){
		return updateOngoing;
	}

	public void setOngoingUpdate() {
		updateOngoing = true;
	}

	private ContactManager() {
		updateOngoing = false; 
		otherContactsMap = new HashMap <String, Contact>();

		//FIXME: Try a better way to retrieve MyContact name
		myContact = new Contact("Me",true);
		contactsToDelete = new ArrayList<String>();
		newlyAddedContacts = new ArrayList<String>();

	}
	
	private void markAsToDelete(String id){
		synchronized (contactsToDelete) {
			contactsToDelete.add(id);
		}
	}
	
	private void markAsNewlyAdded(String id){
		synchronized (newlyAddedContacts) {
			newlyAddedContacts.add(id);
		}
	}
	
	public void resetChanges(){
		synchronized (this) {
			newlyAddedContacts.clear();
			contactsToDelete.clear();
		}
	}
		
	public List<String> getLastDeletedContacts(){
		
		List<String> contacts;
		
		synchronized (contactsToDelete) {
			contacts = new ArrayList<String>(contactsToDelete);
		}
		
		return contacts;
	}
	
	public List<String> getLastAddedContacts(){
		
		List<String> contacts;
		
		synchronized (newlyAddedContacts) {
			contacts = new ArrayList<String>(newlyAddedContacts);
		}
		
		return contacts;
	}
	
	
	public  ContactListAdapter getAdapter(){		
		
		return contactsAdapter;
	}

	public void readPhoneContacts(ContactListActivity act){
		//perform a query on contacts database returning all contacts data in name ascending order
		Cursor c = act.getContentResolver().query(People.CONTENT_URI, null, null, null, People.NAME + " DESC");

		act.startManagingCursor(c);

		int nameCol = c.getColumnIndex(People.NAME);
		int numtelCol = c.getColumnIndex(People.NUMBER);

		//Let's get contacts data
		if (c.first()){
			do {

				String numTel = c.getString(numtelCol);
				String name = c.getString(nameCol);

				myLogger.log(Logger.INFO, "Found contact "+ name + " with numtel " + numTel);

				Contact cont = new Contact(name, true);
				cont.setNumTel(numTel);
				otherContactsMap.put(numTel, cont);				
				
			} while(c.next());
			
		}
		
	}
	
 public void addAdapter(ContactListAdapter cla){	
	 contactsAdapter= cla;
 }


	//This methods adds or updates a contact 
	public void addOnlineContact(AID agentAid, Location loc){

		synchronized (otherContactsMap) {
			//Is the contact already there?
			Contact cont = otherContactsMap.get(agentAid.getLocalName());

			//If not create a new one
			if (cont == null){
				cont = new Contact(agentAid.getLocalName(), false);
				otherContactsMap.put(agentAid.getLocalName(), cont);
				markAsNewlyAdded(agentAid.getLocalName());
			} 

			cont.setAgentContact(agentAid.getName());
			cont.setLocation(loc);
		}	
	}

	public void setOffline(AID agentId) {
		synchronized (otherContactsMap) {
			Contact c  = otherContactsMap.get(agentId.getLocalName());

			//If a contact is local (It's in the phone contacts) it must be shown as offline
			//If the contact is not local, remove it when it goes offline
			if (c.isLocal()){
				c.setOffline();
			} else {
				otherContactsMap.remove(agentId.getLocalName());
				markAsToDelete(agentId.getLocalName());
			}
		}
	}

	//Agent Id is the AID.getLocalName()
	public Contact getContactByAgentId(String agentId){

		Contact c;

		synchronized (otherContactsMap) {
			c= otherContactsMap.get(agentId);
		}

		return c;
	}

	public Contact getMyContact() {
		return myContact;
	}

	public static ContactManager getInstance() {

		synchronized (ContactManager.class) {
			if (manager == null){
				manager = new ContactManager();
			}
		}
		return manager;
	}



	//We cannot modify the contacts from this list, we copy th list to avoid race conditions
	public List<Contact> getOtherContactList() {

		ArrayList<Contact> list;

		synchronized (otherContactsMap) {
			list = new ArrayList<Contact>(otherContactsMap.values());
		}
		return list;
	}
}

