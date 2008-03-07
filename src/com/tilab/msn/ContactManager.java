package com.tilab.msn;

import jade.util.leap.Collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContactManager {

	
	private static ContactManager manager = null;
	private Map<String, Contact> otherContactsMap; 
	private String myContactKey;
	private Contact myContact;
	
	private ContactManager() {
		otherContactsMap = new HashMap <String, Contact>();
	}
	
	public void addMyContact(Contact c) {
		myContact = c;
	}
	

	public void updateOtherContactList(List<Contact> clist){
		synchronized (otherContactsMap) {
			otherContactsMap.clear();
			for (Contact contact : clist) {
				otherContactsMap.put(contact.getAID().getName(),contact);
			}
		}
	}
	
	public Contact getMyContact() {
		return myContact;
	}
	
	public static ContactManager getInstance() {
		
		if (manager == null){
			manager = new ContactManager();
		}
		
		return manager;
	}

	
	
	//we return an iterator to allow working on the map... but we shall copy the map contents to avoid 
	//race conditions
	public List<Contact> getOtherContactList() {
		
		ArrayList<Contact> list;
		
		synchronized (otherContactsMap) {
			list = new ArrayList<Contact>(otherContactsMap.values());
		}		
		
		return list;
	}
	
	public Contact getReadOnlyContactByName( String key){
		Contact c=null;
		synchronized (otherContactsMap) {
			if (otherContactsMap.containsKey(key)) {
				c = new Contact(otherContactsMap.get(key));
			}
		}		
		return c;
	}
	
	
}

