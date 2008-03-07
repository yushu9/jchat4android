package com.tilab.msn;

import com.google.android.maps.MyLocationOverlay;

import android.location.Location;
import jade.core.AID;
import jade.util.Logger;

public class Contact {

	private AID agentContact;
	private Location position;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	public Contact(AID contact){
		agentContact = contact;
		position = new Location();
	}
	
	public Contact(Contact c){
		this.agentContact = new AID(c.agentContact.getName(),AID.ISGUID);
		this.position = new Location(c.position);
	}
	
	public void setLatitude(String latitude) {
		double lat = Double.parseDouble(latitude);
		synchronized (this) {
			position.setLatitude(lat);
		}
	}
	
	public void setLongitude(String longitude){
		double longit = Double.parseDouble(longitude);
		synchronized (this) {
			position.setLatitude(longit);
		}
	}
	
	public void setAltitude(String altitude){
		double alt = Double.parseDouble(altitude);
		synchronized (this) {
			position.setAltitude(alt);
		}
	}
	
	public boolean hasSameLocation(Location loc){
		boolean sameLoc = false;
		
		synchronized (this) {
			sameLoc = position.equals(loc);
		}
		
		return sameLoc;
	}	
		
	public Location getLocation() {
		Location tmpLoc=null;
		synchronized (this) {
			tmpLoc = new Location(position);
		}
		return tmpLoc;
	}
	
	public void setLocation(Location loc){
		boolean updated = false;
		
		synchronized (this) {
			if (!position.equals(loc)){
				updated= true;
				position = loc;
			}
		}
		
		StringBuffer buf = new StringBuffer("Position of contact " + this.agentContact.getLocalName() + " was ");
		buf.append( (updated)? "updated" : "not updated");
		myLogger.log(Logger.INFO,  buf.toString());
	}
	
	public AID getAID() {
		return agentContact;
	}
	
	@Override
	public String toString() {
		return (agentContact.getLocalName() + " (" + position.getLatitude() + ";" + position.getLongitude() +")");  
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Contact) {
			Contact other = (Contact) o;
			return agentContact.equals(other.agentContact);
		} else {
			return false;
		}
	}
}
