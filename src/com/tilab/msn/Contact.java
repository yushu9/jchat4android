package com.tilab.msn;


import jade.util.Logger;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a generic contact that can be online or offline
 * Each contact has non null location:  online contacts have a location that is updated
 * while offline contacts have a fixed location (null location)(but should not be drawn) 
 * 
 * @author s.semeria
 *
 */

public class Contact implements Parcelable {

	private static final long serialVersionUID = 1274339729698023648L;
	
	private String agentContact; //nome globale dell'agente corrisponde a numTel@platformID
	private String name; //nome come appare sulla rubrica (se non è presente è il numTel)
	private boolean isLocal; //distingue quelli che sono nella lista dei contatti
	private Location currentLocation;
	private Location lastPosition;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	public static final android.os.Parcelable.Creator CREATOR = new myContactFactory();

	public Contact(String name, boolean isLocal){
		this.name = name;
		currentLocation = new Location();
		lastPosition = new Location();
		this.isLocal = isLocal;
	}
	
	
	public String getPhoneNumber(){
		String phoneNbr = null;
		if(agentContact != null){
			int atPos = agentContact.lastIndexOf('@');
			if(atPos == -1)
				phoneNbr = agentContact;
			else
				phoneNbr = agentContact.substring(0, atPos);
		}
		return phoneNbr;
	}
	
	public boolean isOnline(){
		return (agentContact != null);
	}
	
	public boolean isLocal(){
		return isLocal;
	}
	
	
	public void setAgentContact(String agentContact){
		this.agentContact = agentContact;
		
	}
	
	public String getAgentContact(){
		return this.agentContact;
	}
	
	public void setOffline(){
		agentContact = null;
		currentLocation = new Location();
		lastPosition = currentLocation;
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
		//myLogger.log(Logger.INFO,  buf.toString());
	}
	

	public String getName() {
		return name;
	}
	

	public String toString() {
		return name;
	}
	
	
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


	public void writeToParcel(Parcel dest) {
		dest.writeString(agentContact);
		dest.writeString(name);
		boolean[] b = {isLocal};
		dest.writeBooleanArray(b);
	}
	
	static class myContactFactory implements android.os.Parcelable.Creator{
		
		public Contact createFromParcel(Parcel arg){
			String agentContact = arg.readString();
			String name = arg.readString();
			boolean[] b = new boolean[1];
			arg.readBooleanArray(b);
			Contact cc = new Contact(name, b[0]);
			cc.setAgentContact(agentContact);
			return cc;
		}

		public Contact[] newArray(int arg0) {
			return new Contact[arg0];
		}
	}
	
}
