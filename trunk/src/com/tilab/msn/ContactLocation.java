package com.tilab.msn;

import android.location.Location;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactLocation.
 */
public class ContactLocation extends Location {

	/** The has moved. */
	private final boolean hasMoved;

	/**
	 * Instantiates a new contact location.
	 */
	public ContactLocation(){
		hasMoved = false;
		setLatitude(Double.POSITIVE_INFINITY);
		setLongitude(Double.POSITIVE_INFINITY);
		setAltitude(Double.POSITIVE_INFINITY);
	
	}
	
	/**
	 * Instantiates a new contact location.
	 * 
	 * @param toBeCopied the to be copied
	 */
	public ContactLocation( ContactLocation toBeCopied){
		this(toBeCopied, toBeCopied.hasMoved);
	}
	
	/**
	 * Instantiates a new contact location.
	 * 
	 * @param loc the loc
	 * @param moved the moved
	 */
	private ContactLocation(Location loc, boolean moved){
		super(loc);
		hasMoved = moved;
	}
	
	/**
	 * Instantiates a new contact location.
	 * 
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @param altitude the altitude
	 * @param moved the moved
	 */
	private ContactLocation(double latitude, double longitude, double altitude, boolean moved){
		setLatitude(latitude);
		setLongitude(longitude);
		setAltitude(altitude);
		hasMoved = moved;
	}
	
	/**
	 * Change location.
	 * 
	 * @param loc the loc
	 * 
	 * @return the contact location
	 */
	public ContactLocation changeLocation(Location loc)
	{   boolean moved = !this.equals(loc);
		return new ContactLocation(loc,moved);
	}
	
	/**
	 * Change location.
	 * 
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @param altitude the altitude
	 * 
	 * @return the contact location
	 */
	public ContactLocation changeLocation(double latitude, double longitude, double altitude)
	{   boolean moved = ( (getLatitude() != latitude) || (getLongitude() != longitude) || (getAltitude() != altitude) );
		return new ContactLocation(latitude, longitude, altitude ,moved);
	}
	
	/**
	 * Checks for moved.
	 * 
	 * @return true, if successful
	 */
	public boolean hasMoved(){
		return hasMoved;
	}
	
}
