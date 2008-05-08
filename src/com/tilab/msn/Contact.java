package com.tilab.msn;


import java.util.concurrent.atomic.AtomicBoolean;

import jade.util.Logger;
import android.location.Location;
import android.util.AttributeSet;


/**
 * This class represents a generic contact that can be online or offline
 * Each contact has non null location:  online contacts have a location that is updated
 * while offline contacts have a fixed location (null location)(but should not be drawn) 
 * 
 * @author s.semeria
 *
 */

public class Contact  {

	private final String phoneNumber;	
	//THIS IS MUTABLE
	private volatile boolean isOnline;
	private final String name; //nome come appare sulla rubrica (se non è presente il numTel)	
	private final boolean storedOnPhone;
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
		
	public Contact(String name, String phoneNumber, boolean stored){
		this.name = name;
		this.phoneNumber= phoneNumber;
		isOnline = false;	
		storedOnPhone = stored;
	}
		
	public  String getPhoneNumber(){
		return phoneNumber;
	}
	

	public String getName() {
		return name;
	}
	
	public boolean isStoredOnPhone(){
		return storedOnPhone;
	}

	public String toString() {
		return name;
	}
	
	public void setOnline(){
		isOnline = true;
	}
	
	public void setOffline(){
		isOnline = false;;
	}
	
	public boolean isOnline(){
		return isOnline;
	}

	
	
	public boolean equals(Object o) {
		
		boolean res= false;
		
		if (o instanceof Contact) {
			Contact other = (Contact) o;					
			res= phoneNumber.equals(other.phoneNumber);	
		}
		
		return res;
	}	
}
