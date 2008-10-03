/**
 * 
 */
package it.telecomitalia.locationControl;

/**
 * @author s.semeria
 *
 */
public class WayPointLocation {

	public int index;
	public double latitude;
	public double longitude;
	public double altitude;
	
	public WayPointLocation(int index, double lat, double longit, double alt){
		this.index = index;
		this.latitude = lat;
		this.longitude = longit;
		this.altitude = alt;
	}
	
}
