package com.tilab.msn;

import jade.core.AID;

import jade.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;


public class ContactManager {

	private static ContactManager manager = new ContactManager();
	//The key of this map is the local name (phone number)	
	private final ConcurrentMap<String, Contact> onLineContactsMap;
	private final ConcurrentMap<String, Contact> offLineContactsMap;
	private Contact myContact;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private boolean updateOngoing = false;
	
	//Adapter for the contacts list
	private ContactListAdapter contactsAdapter;
	private ContactListChanges modifications;	
	
	

	public boolean updateIsOngoing(){
		return updateOngoing;
	}

	public void setOngoingUpdate() {
		updateOngoing = true;
	}

	private ContactManager() {
		updateOngoing = false; 
		onLineContactsMap = new ConcurrentHashMap<String, Contact>(20, 3.0f, 1);
		offLineContactsMap = new ConcurrentHashMap<String, Contact>(20, 3.0f, 1);
		
		modifications = new ContactListChanges();
	}	
	
	public synchronized void resetModifications(){
		modifications.resetChanges();
	}
	
	public synchronized ContactListChanges getModifications() {
		return new ContactListChanges(modifications);
	}	
	
	public  ContactListAdapter getAdapter(){		
		
		return contactsAdapter;
	}

	public void readPhoneContacts(ContactListActivity act){
		//perform a query on contacts database returning all contacts data in name ascending order
		Cursor c = act.getContentResolver().query(People.CONTENT_URI, null, null, null, People.NAME + " DESC");

		act.startManagingCursor(c);

		int nameCol = c.getColumnIndex(People.NAME);
		int phonenumberCol = c.getColumnIndex(People.NUMBER);

		//Let's get contacts data
		if (c.first()){
			do {

				String phonenumber = c.getString(phonenumberCol);
				String name = c.getString(nameCol);

				myLogger.log(Logger.INFO, "Found contact "+ name + " with numtel " + phonenumber);
				Contact cont = new Contact(name, phonenumber);
				offLineContactsMap.put(phonenumber, cont);			
			} while(c.next());			
		}		
	}	
	
 public void addAdapter(ContactListAdapter cla){	
	 contactsAdapter= cla;
 }

	//This methods adds or updates a contact 
	public void addOnlineContact(PHONENUMBER phoneNumber, Location loc){
			//Is the contact already there?
			Contact cont = onLineContactsMap.get(phoneNumber.getLocalName());

			//If not create a new one
			if (cont == null){
				cont = new Contact(phoneNumber.getLocalName(), false);
				otherContactsMap.put(phoneNumber.getLocalName(), cont);
				modifications.contactsAdded.add(phoneNumber.getLocalName());
			} 
			cont.setAgentContact(phoneNumber.getName());
			cont.setLocation(loc);
	}

	public void setOffline(AID agentId) {		 
			Contact c  = otherContactsMap.get(agentId.getLocalName());

			//If a contact is local (It's in the phone contacts) it must be shown as offline
			//If the contact is not local, remove it when it goes offline
			if (c.isLocal()){
				c.setOffline();
			} else {
				otherContactsMap.remove(agentId.getLocalName());
				modifications.contactsDeleted.add(agentId.getLocalName());
			}
		}

	//Agent Id is the AID.getLocalName()
	public Contact getContactByAgentId(String agentId){
		Contact c;
		c= otherContactsMap.get(agentId);
		return c;
	}
	

	public Contact getMyContact() {
		return myContact;
	}
 
	public static ContactManager getInstance() {
		return manager;
	}


	public void shutdown() {
		onLineContactsMap.clear();
		offLineContactsMap.clear();		
		contactsAdapter.clear();
	}

	//We cannot modify the contacts from this list, we copy the list to avoid race conditions
	public List<Contact> getOnLineContactList() {
		ArrayList<Contact> list;
		list = new ArrayList<Contact>(onLineContactsMap.values());
		return list;
	}
	
	public List<Contact> getOffLineContactList() {
		ArrayList<Contact> list;
		list = new ArrayList<Contact>(offLineContactsMap.values());
		return list;
	}
	
}

