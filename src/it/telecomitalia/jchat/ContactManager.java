/*****************************************************************
 jChat is a  chat application for Android based on JADE
  Copyright (C) 2008 Telecomitalia S.p.A. 
 
 GNU Lesser General Public License

 This is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this software; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.telecomitalia.jchat;



import jade.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.location.Location;
import android.provider.Contacts.People;
import android.telephony.PhoneNumberUtils;

/**
 * Manages the list of contacts. 
 * This class is a singleton and manages a list of all contacts and their location.
 * It is responsible for adding and removing contacts and also for location update in a thread safe way.
 * 
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
	 * Map containing all the available contacts (except for the my contact), in particular all contacts stored on the phone (offline) 
	 * and online contacts 
	 */
	private final Map<String, Contact> contactsMap;
	
	/** 
	 * Map containing all the available locations (online contacts only except the my contact). 
	 */
	private final Map<String, ContactLocation> contactLocationMap;
	
	/**
	 * Instance of the my contact.
	 */
	private Contact myContact;
	
	/**  
	 * Location of the myContact
	 */
	private volatile ContactLocation myContactLocation;
	
	/** 
	 * Instance of the Jade logger for debugging 
     */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	/** 
	 * The adapter describing the contact list. 
	 */
	private ContactListAdapter contactsAdapter;
	
	/** 
	 * Instance of {@link ContactListChanges} containing the list of newly added contacts and of recently removed contacts. 
	 */
	private ContactListChanges modifications;	
	
	/** 
	 * Id of the my Contact
	 */
	private final String MY_CONTACT_NAME="Me";
	
	/**
	 * Name of the location provider
	 */
	private String providerName;

	/**
	 * Checks if any contact is currently moving.
	 * 
	 * @return true if any contact is moving, false otherwise
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
	 * Reset tracking of modifications to contact list
	 */
	public synchronized void resetModifications(){
		modifications.resetChanges();
	}
	
	/**
	 * Retrieves the list of modifications to contact map since last reset
	 * 
	 * @return {@link ContactListChanges} 
	 */
	public synchronized ContactListChanges getModifications() {
		return new ContactListChanges(modifications);
	}	
	
	/**
	 * Retrieves the adapter associated to the Contact list
	 * 
	 * @return the adapter
	 */
	public  ContactListAdapter getAdapter(){		
		
		return contactsAdapter;
	}

	/**
	 * Read contacts stored on phone database and populates the contact map with all offline contacts.
	 * 
	 * @param act reference to an activity
	 */
	public void readPhoneContacts(ContactListActivity act){
		
		providerName = act.getText(R.string.location_provider_name).toString();
		
		//perform a query on contacts database returning all contacts data in name ascending order
		Cursor c = act.getContentResolver().query(People.CONTENT_URI, null, null, null, People.NAME + " DESC");

		act.startManagingCursor(c);

		int nameCol = c.getColumnIndexOrThrow(People.NAME);
		int phonenumberCol = c.getColumnIndexOrThrow(People.NUMBER);

		//Let's get contacts data
		if (c.moveToFirst()){
			do {

				String phonenumber =   PhoneNumberUtils.stripSeparators( c.getString(phonenumberCol) );
				String name = c.getString(nameCol);

				myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Found contact "+ name + " with numtel " + phonenumber);
				Contact cont = new Contact(name, phonenumber,true);
				contactsMap.put(phonenumber, cont);
				
			} while(c.moveToNext());	
		}		
		
		c.close();
	}	
	
	/**
	 * Adds an adapter for the contact list.
	 * 
	 * @param cla the {@link ContactListAdapter}
	 */
	public void addAdapter(ContactListAdapter cla){	
		 contactsAdapter= cla;
	}

	
	/**
	 * Adds a new {@link Contact} with the given phoneNumber and Location or updates the Location of an existing one.
	 * 
	 * @param phoneNumber of the new contact or of the contact that should be updated
	 * @param loc current location of the new contact or new location if this is an update
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
					
					ContactLocation oldLocation= new ContactLocation(providerName);
					ContactLocation newLocation= oldLocation.changeLocation(loc);
					
						cont.setOnline();						
						contactLocationMap.put(phoneNumber,newLocation);
				 }
				
			}else {
				cont= new Contact(phoneNumber, phoneNumber,false);
				cont.setOnline();
				ContactLocation oldLocation= new ContactLocation(providerName);
				ContactLocation newLocation= oldLocation.changeLocation(loc);
		
				myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":New contact " +  cont.getName() + " was added with location " + newLocation.toString() );
					contactLocationMap.put(phoneNumber,newLocation);
					contactsMap.put(phoneNumber, cont);
					modifications.contactsAdded.add(phoneNumber);
					myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Contact map is now: " + contactsMap.toString() );
			}
		}
	}
				
				
	/**
	 * Set the given contact status to offline (without removing it from the map if it is stored on the phone contacts database)
	 * 
	 * @param phoneNumber number of the contact 
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
					myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Removing contact with phone number " + phoneNumber);
					modifications.contactsDeleted.add(phoneNumber);
				}		
				contactLocationMap.remove(phoneNumber);
				myLogger.log(Logger.INFO, "Thread "+ Thread.currentThread().getId() + ":Contact map is now: " + contactsMap.toString() );
			}
	}


	/**
	 * Retrieves a Contact given its phone number
	 * 
	 * @param phoneNumber the phone number of the contact that should be retrieved
	 * @return the contact having the given phoneNumber
	 */
	public Contact getContact(String phoneNumber){
		return contactsMap.get(phoneNumber);
	}
	
	/**
	 * Gets the location of a contact given its phone number.
	 * 
	 * @param phoneNumber the phone number of the contact whose location should be retrieved
	 * @return location of the required contact
	 */
	public ContactLocation getContactLocation(String phoneNumber){
		return contactLocationMap.get(phoneNumber);
	}

	/**
	 * Retrieve instance of the main contact
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
	 * Cleans up both the contact map and the location map
	 */
	public void shutdown() {
		contactsMap.clear();
		contactsAdapter.clear();
	}



	/**
	 * Returns a map containing contact to location mapping. This map is a copy of the inner location map
	 * so this should be thread safe
	 * 
	 * @return map with online contact locations
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
	 * Retrieves a map containing mappings between contacts and phone numbers
	 * 
	 * @return copy of the inner contact map 
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
	 * Adds the my contact, given its phone number.
	 * 
	 * @param phoneNumber my contact's phone number
	 */
	public void addMyContact(String phoneNumber) {
		// TODO Auto-generated method stub
		myContact = new Contact(MY_CONTACT_NAME,phoneNumber,true);
		myContact.setOnline();
		myContactLocation = new ContactLocation(providerName);
		
	}
	
	/**
	 * Update location of my contact
	 * 
	 * @param loc new location of my contact
	 */
	public void updateMyContactLocation(Location loc) {
		myContactLocation = myContactLocation.changeLocation(loc);
	}
	
	/**
	 * Gets my contact location.
	 * 
	 * @return the current my contact's location
	 */
	public ContactLocation getMyContactLocation(){
		return new ContactLocation(myContactLocation);
	}
	
}

