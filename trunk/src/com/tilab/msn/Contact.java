package com.tilab.msn;

import jade.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * This class represents a generic contact that can be online or offline
 * Each contact has non null location:  online contacts have a location that is updated
 * while offline contacts have a fixed location (null location)(but should not be drawn).
 * 
 * @author s.semeria
 */

public class Contact  {

	/** The phone number. */
	private final String phoneNumber;	
	//THIS IS MUTABLE
	/** The is online. */
	private volatile boolean isOnline;
	
	/** The name. */
	private final String name; //nome come appare sulla rubrica (se non è presente il numTel)	
	
	/** The stored on phone. */
	private final boolean storedOnPhone;
	
		
	/**
	 * Instantiates a new contact.
	 * 
	 * @param name the name
	 * @param phoneNumber the phone number
	 * @param stored the stored
	 */
	public Contact(String name, String phoneNumber, boolean stored){
		this.name = name;
		this.phoneNumber= phoneNumber;
		isOnline = false;	
		storedOnPhone = stored;
	}
	
	/**
	 * Instantiates a new contact.
	 * 
	 * @param c the c
	 */
	public Contact(Contact c){
		this.name = new String(c.name);
		this.phoneNumber = new String(c.phoneNumber);
		this.isOnline = c.isOnline;
		this.storedOnPhone = c.storedOnPhone;
	}
	
	/**
	 * Gets the phone number.
	 * 
	 * @return the phone number
	 */
	public  String getPhoneNumber(){
		return phoneNumber;
	}
	

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks if is stored on phone.
	 * 
	 * @return true, if is stored on phone
	 */
	public boolean isStoredOnPhone(){
		return storedOnPhone;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
	/**
	 * Sets the online.
	 */
	public void setOnline(){
		isOnline = true;
	}
	
	/**
	 * Sets the offline.
	 */
	public void setOffline(){
		isOnline = false;;
	}
	
	/**
	 * Checks if is online.
	 * 
	 * @return true, if is online
	 */
	public boolean isOnline(){
		return isOnline;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		
		boolean res= false;
		
		if (o instanceof Contact) {
			Contact other = (Contact) o;					
			res= phoneNumber.equals(other.phoneNumber);	
		}
		
		return res;
	}	
}
