package com.tilab.msn;



import jade.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;

/**
 * Manages the list of contacts. 
 * This class is a singleton and manages a list of all contacts and their location.
 * It is responsible for adding and removing contacts and also for location update in a thread safe way.
 * <p> 
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class ContactManager {

	/** 
	 * Static instance of the contact manager. 
	 */
	private static ContactManager manager = new ContactManager();
		
	/** 
	 * The contacts map. 
	 */
	private final Map<String, Contact> contactsMap;
	
	/** The contact location map. */
	private final Map<String, ContactLocation> contactLocationMap;
	
	/** The my contact. */
	private Contact myContact;
	
	/** The my contact location. */
	private volatile ContactLocation myContactLocation;
	
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	//Adapter for the contacts list
	/** The contacts adapter. */
	private ContactListAdapter contactsAdapter;
	
	/** The modifications. */
	private ContactListChanges modifications;	
	
	/** The M y_ contac t_ name. */
	private final String MY_CONTACT_NAME="Me";
	
	

	/**
	 * Moving contacts.
	 * 
	 * @return true, if successful
	 */
	public boolean movingContacts(){
		boolean moving= true;
		
		synchronized (this) {
			List<ContactLocation> locs = new ArrayList<ContactLocation>(contactLocationMap.values());
			for (ContactLocation contactLocation : locs) {
				moving = moving && contactLocation.hasMoved();
			}
		}
		
		return moving;
	}



	
	/**
	 * Instantiates a new contact manager.
	 */
	private ContactManager() { 
		contactsMap = new HashMap<String, Contact>();
		contactLocationMap = new HashMap<String, ContactLocation>();
		modifications = new ContactListChanges();
	}	
	
	/**
	 * Reset modifications.
	 */
	public synchronized void resetModifications(){
		modifications.resetChanges();
	}
	
	/**
	 * Gets the modifications.
	 * 
	 * @return the modifications
	 */
	public synchronized ContactListChanges getModifications() {
		return new ContactListChanges(modifications);
	}	
	
	/**
	 * Gets the adapter.
	 * 
	 * @return the adapter
	 */
	public  ContactListAdapter getAdapter(){		
		
		return contactsAdapter;
	}

	/**
	 * Read phone contacts.
	 * 
	 * @param act the act
	 */
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
	
	/**
	 * Adds the adapter.
	 * 
	 * @param cla the cla
	 */
	public void addAdapter(ContactListAdapter cla){	
		 contactsAdapter= cla;
	}

	
	//This methods adds or updates a contact 
	/**
	 * Adds the or update online contact.
	 * 
	 * @param phoneNumber the phone number
	 * @param loc the loc
	 */
	public void addOrUpdateOnlineContact(String phoneNumber, Location loc){
			//Is the contact already there?
		synchronized(this){	
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
					
						cont.setOnline();						
						contactLocationMap.put(phoneNumber,newLocation);
				 }
				
			}else {
				cont= new Contact(phoneNumber, phoneNumber,false);
				cont.setOnline();
				ContactLocation oldLocation= new ContactLocation();
				ContactLocation newLocation= oldLocation.changeLocation(loc);
		
					contactLocationMap.put(phoneNumber,newLocation);
					contactsMap.put(phoneNumber, cont);
					modifications.contactsAdded.add(phoneNumber);
			}
		}
	}
				
				
	/**
	 * Sets the contact offline.
	 * 
	 * @param phoneNumber the new contact offline
	 */
	public void setContactOffline(String phoneNumber) {		 
			

			//If a contact is local (It's in the phone contacts) it must be shown as offline
			//If the contact is not local, remove it when it goes offline
			synchronized (this) {
				Contact c  = contactsMap.get(phoneNumber);
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
	/**
	 * Gets the contact.
	 * 
	 * @param phoneNumber the phone number
	 * 
	 * @return the contact
	 */
	public Contact getContact(String phoneNumber){
		return contactsMap.get(phoneNumber);
	}
	
	/**
	 * Gets the contact location.
	 * 
	 * @param phoneNumber the phone number
	 * 
	 * @return the contact location
	 */
	public ContactLocation getContactLocation(String phoneNumber){
		return contactLocationMap.get(phoneNumber);
	}

	/**
	 * Gets the my contact.
	 * 
	 * @return the my contact
	 */
	public Contact getMyContact() {
		return myContact;
	}
 
	/**
	 * Gets the single instance of ContactManager.
	 * 
	 * @return single instance of ContactManager
	 */
	public static ContactManager getInstance() {
		return manager;
	}


	/**
	 * Shutdown.
	 */
	public void shutdown() {
		contactsMap.clear();
		contactsAdapter.clear();
	}

	//We cannot modify the contacts from this list, we copy the list to avoid race conditions
	/**
	 * Gets the contact list.
	 * 
	 * @return the contact list
	 */
	public List<Contact> getContactList() {
		ArrayList<Contact> list;
		list = new ArrayList<Contact>();
		for (Contact contact : contactsMap.values()) {
			list.add(new Contact(contact));
		}
		return list;
	}

	//FIXME: Discuss if here we should return the map or a copy
	/**
	 * Gets the all contact locations.
	 * 
	 * @return the all contact locations
	 */
	public Map<String, ContactLocation> getAllContactLocations(){
		Map<String,ContactLocation> location = new HashMap<String, ContactLocation>();
		
		synchronized (this) {
			for (String s : contactLocationMap.keySet()) {
				location.put(new String(s), new ContactLocation(contactLocationMap.get(s)));
			}
		}
		
		
		return location;
	}
	
	
	/**
	 * Gets the all contacts.
	 * 
	 * @return the all contacts
	 */
	public Map<String, Contact> getAllContacts(){
		Map<String,Contact> cMap = new HashMap<String, Contact>();

		synchronized (this) {
			for (String s : contactsMap.keySet()) {
				cMap.put(new String(s), new Contact(contactsMap.get(s)));
			}
		}
		
		return cMap;
	}
	
	
	/**
	 * Adds the my contact.
	 * 
	 * @param phoneNumber the phone number
	 */
	public void addMyContact(String phoneNumber) {
		// TODO Auto-generated method stub
		myContact = new Contact(MY_CONTACT_NAME,phoneNumber,true);
		myContactLocation = new ContactLocation();
	}
	
	/**
	 * Update my contact location.
	 * 
	 * @param loc the loc
	 */
	public void updateMyContactLocation(Location loc) {
		myContactLocation = myContactLocation.changeLocation(loc);
	}
	
	/**
	 * Gets the my contact location.
	 * 
	 * @return the my contact location
	 */
	public ContactLocation getMyContactLocation(){
		return new ContactLocation(myContactLocation);
	}
	
}

