package com.tilab.msn;



import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;


public class ContactManager {

	private static ContactManager manager = new ContactManager();
	//The key of this map is the local name (phone number)	
	private final ConcurrentMap<String, Contact> contactsMap;
	private final ConcurrentMap<String, ContactLocation> contactLocationMap;
	
	private Contact myContact;
	private volatile ContactLocation myContactLocation;
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	//Adapter for the contacts list
	private ContactListAdapter contactsAdapter;
	private ContactListChanges modifications;	
	private final String MY_CONTACT_NAME="Me";
	
	

	public boolean movingContacts(){
		boolean moving= true;
		List<ContactLocation> locs = new ArrayList<ContactLocation>(contactLocationMap.values());
		
		for (ContactLocation contactLocation : locs) {
			moving = moving && contactLocation.hasMoved();
		}
		
		return moving;
	}



	
	private ContactManager() { 
		contactsMap = new ConcurrentHashMap<String, Contact>(20, 3.0f, 1);
		contactLocationMap = new ConcurrentHashMap<String, ContactLocation>(20, 3.0f, 1);
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
				Contact cont = new Contact(name, phonenumber,true);
				contactsMap.put(phonenumber, cont);
				
			} while(c.next());			
		}		
	}	
	
	public void addAdapter(ContactListAdapter cla){	
		 contactsAdapter= cla;
	}

	
	//This methods adds or updates a contact 
	public void addOrUpdateOnlineContact(String phoneNumber, Location loc){
			//Is the contact already there?
			Contact cont = contactsMap.get(phoneNumber);

			//the new contact is available
			if (cont != null){
				
				if (cont.isOnline()){
					ContactLocation oldloc= contactLocationMap.get(phoneNumber);
					ContactLocation newloc= oldloc.changeLocation(loc);				
					contactLocationMap.put(phoneNumber, newloc);				
				}
				else {
					ContactLocation oldLocation= new ContactLocation();
					ContactLocation newLocation= oldLocation.changeLocation(loc);
					synchronized(this){
						cont.setOnline();						
						contactLocationMap.put(phoneNumber,newLocation);
				    }
				}
			}else {
				cont= new Contact(phoneNumber, phoneNumber,false);
				cont.setOnline();
				ContactLocation oldLocation= new ContactLocation();
				ContactLocation newLocation= oldLocation.changeLocation(loc);
				synchronized(this){
					contactLocationMap.put(phoneNumber,newLocation);
					contactsMap.put(phoneNumber, cont);
					modifications.contactsAdded.add(phoneNumber);
				}	
			}
	}
				
				
	public void setContactOffline(String phoneNumber) {		 
			Contact c  = contactsMap.get(phoneNumber);

			//If a contact is local (It's in the phone contacts) it must be shown as offline
			//If the contact is not local, remove it when it goes offline
			synchronized (this) {
				if (c.isStoredOnPhone()){
					c.setOffline();	
				} else {
					contactsMap.remove(phoneNumber);
					modifications.contactsDeleted.add(phoneNumber);
				}		
				contactLocationMap.remove(phoneNumber);
			}
	}

	//Agent Id is the AID.getLocalName()
	public Contact getContact(String phoneNumber){
		return contactsMap.get(phoneNumber);
	}
	
	public ContactLocation getContactLocation(String phoneNumber){
		return contactLocationMap.get(phoneNumber);
	}

	public Contact getMyContact() {
		return myContact;
	}
 
	public static ContactManager getInstance() {
		return manager;
	}


	public void shutdown() {
		contactsMap.clear();
		contactsAdapter.clear();
	}

	//We cannot modify the contacts from this list, we copy the list to avoid race conditions
	public List<Contact> getContactList() {
		ArrayList<Contact> list;
		list = new ArrayList<Contact>(contactsMap.values());
		return list;
	}

	//FIXME: Discuss if here we should return the map or a copy
	public Map<String, ContactLocation> getAllContactLocations(){
		return new HashMap<String, ContactLocation>(contactLocationMap);
	}
	
	public void addMyContact(String phoneNumber) {
		// TODO Auto-generated method stub
		myContact = new Contact(MY_CONTACT_NAME,phoneNumber,true);
		myContactLocation = new ContactLocation();
	}
	
	public void updateMyContactLocation(Location loc) {
		myContactLocation = myContactLocation.changeLocation(loc);
	}
	
	public ContactLocation getMyContactLocation(){
		return new ContactLocation(myContactLocation);
	}
	
}

