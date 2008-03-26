package com.tilab.msn;

import jade.core.AID;
import jade.util.Logger;
import android.location.Location;

/**
 * This class represents a generic contact that can be onlin or offline
 * Each contact has non null location:  online contacts have a location that is updated
 * while offline contacts have a fixed location (null location)(but should not be drawn) 
 * 
 * @author s.semeria
 *
 */

public class Contact {

	private AID agentContact;
	private String name;
	private String numTel;
	private boolean isLocal;
	private Location currentLocation;
	private Location lastPosition;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	//This constructor builds an online contact whose name is not known.
	//Its default name shall be the agent name
	public Contact(AID contact){
		agentContact = contact;
		name = agentContact.getLocalName();
		numTel = name;
		currentLocation = new Location();
		lastPosition = new Location();
		isLocal = false;
	}
	
	
	//This is an online contact that is also in contacts on the phone
	public Contact(AID contact, String name){
		agentContact = contact;
		this.name = name;
		this.numTel = agentContact.getLocalName();
		currentLocation = new Location();
		lastPosition = new Location();
		isLocal = true;
	}
	
	//This builds an offline contact whose name is known (from phone contacts)
	public Contact(String name, String numTel){
		this.name= name;
		this.numTel = numTel;
		currentLocation = new Location();
		lastPosition = new Location();
		isLocal=true;
	}
	
	public boolean isOnline(){
		boolean online;
		
		synchronized (this) {
			online = (agentContact != null);
		}
		
		return  online;
	}
	
	public boolean isLocal(){
		return isLocal;
	}
	
	
	public void setOnline(AID agentAID){
		agentContact = agentAID;
		numTel = agentAID.getLocalName();
	}
	
	public void setOffline(){
		agentContact = null;
		currentLocation = new Location();
		lastPosition = currentLocation;
		
	}
	
	//Contact copy constructor
	public Contact(Contact c){
		if (c.agentContact != null)
			this.agentContact = new AID(c.agentContact.getName(),AID.ISGUID);
		else 
			this.agentContact = null;
		
		this.currentLocation = new Location(c.currentLocation);
		this.lastPosition = new Location(c.lastPosition);
		this.name = new String(c.name);
		this.numTel = new String(c.numTel);
	}
	
	
	
	public boolean hasMoved(){
		boolean moved = false;
		synchronized (this) {
			moved = !(currentLocation.equals(lastPosition));
		}
		return moved;
	}
	
		
	public Location getLocation() {
		Location tmpLoc=null;
		synchronized (this) {
			tmpLoc = new Location(currentLocation);
		}
		return tmpLoc;
	}
	
	public void setLocation(Location loc){
		boolean updated = false;
		
		synchronized (this) {
			if (!currentLocation.equals(loc)){
				updated= true;
				lastPosition = currentLocation;
				currentLocation = loc;
				
			}
		}
		
		StringBuffer buf = new StringBuffer("Position of contact " + name + " was ");
		buf.append( (updated)? "updated" : "not updated");
		myLogger.log(Logger.INFO,  buf.toString());
	}
	
	public AID getAID() {
		return agentContact;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNumTel(){
		return numTel;
	}
	
	@Override
	public String toString() {
		return ("Contact " + 
				name + 
				" agent " + 
				((agentContact == null)? "NULL ": agentContact.getLocalName()) +  
				"(" + currentLocation.getLatitude() + 
				";" + currentLocation.getLongitude() +
				")");  
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Contact) {
			Contact other = (Contact) o;
			boolean b;
			synchronized (this) {
				b =agentContact.equals(other.agentContact); 
			}
			return b;
		} else {
			return false;
		}
	}
}
