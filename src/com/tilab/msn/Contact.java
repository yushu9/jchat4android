package com.tilab.msn;


import jade.util.Logger;
import android.location.Location;


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
	private final String name; //nome come appare sulla rubrica (se non è presente il numTel)	
	private Location currentLocation;
	private Location lastPosition;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
		
	public Contact(String name, String phoneNumber){
		this.name = name;
		this.phoneNumber= phoneNumber;
		currentLocation = new Location();
		lastPosition = new Location();		
	}
		
	public  String getPhoneNumber(){
		return phoneNumber;
	}		
	
	public synchronized boolean hasMoved(){
		boolean moved = false;		 
		moved = !(currentLocation.equals(lastPosition));
		return moved;
	}
	
		
	public synchronized Location getLocation() {
		Location tmpLoc=null;		 
		tmpLoc = new Location(currentLocation);		
		return tmpLoc;
	}
	
	public synchronized void setLocation(Location loc){
		
			if (!currentLocation.equals(loc)){				
				lastPosition = currentLocation;
				currentLocation = loc;			
			}			
	}
	

	public String getName() {
		return name;
	}
	

	public String toString() {
		return name;
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
