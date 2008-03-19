package com.tilab.msn;

import jade.core.AID;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.SystemProperties;
import android.provider.Contacts.People;

public class ContactManager {

	
	private static ContactManager manager = null;
	private Map<String, Contact> otherContactsMap; 
	private String myContactKey;
	private Contact myContact;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	
	private ContactManager() {
		otherContactsMap = new HashMap <String, Contact>();
		
		String numtel = SystemProperties.get("numtel");
		
		//if number is not available
		if (numtel.equals("")){
			myLogger.log(Logger.WARNING, "Cannot access the numtel! A random number shall be used!!!");
			numtel = getRandomNumber();
		}
		
		//FIXME: Try a better way to retrieve MyContact name
		myContact = new Contact("Me",numtel);
		
	}

	public void readPhoneContacts(Activity act){
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
				
				Contact cont = new Contact(name, numTel);
				otherContactsMap.put(numTel, cont);
				
			} while(c.next());
		}
	}
	
	private String getRandomNumber(){
		Random rnd = new Random();
		int randInt  = rnd.nextInt();
		return "RND" + String.valueOf(randInt);
	}
	
	//This methods adds or updates a contact 
	public void addOnlineContact(AID agentAid, Location loc){
			
		synchronized (otherContactsMap) {
			//Is the contact already there?
			Contact cont = otherContactsMap.get(agentAid.getLocalName());
			//If not create a new one
			if (cont == null){
				cont = new Contact(agentAid);
				otherContactsMap.put(agentAid.getLocalName(), cont);
			} else {
				//if so check if it is online already or not
				//If the contact is offline, bring it online
				if (!cont.isOnline()){
					cont.setOnline(agentAid);
				}
			}
			//In any case update its loction
			cont.setLocation(loc);
		}	
	}
	
	public void setOffline(AID agentId) {
		synchronized (otherContactsMap) {
			Contact c  = otherContactsMap.get(agentId.getLocalName());
			
			//If a contact is local (It's in the phone contatcs) it must be shown as offline
			//If the contact is not local, remove it when it goes offline
			if (c.isLocal()){
				c.setOffline();
			} else {
				otherContactsMap.remove(c.getAID().getLocalName());
			}
		}
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

