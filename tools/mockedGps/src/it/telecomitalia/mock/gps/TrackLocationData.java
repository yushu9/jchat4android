/**
 * 
 */
package it.telecomitalia.mock.gps;

/**
 * @author  s.semeria
 */
public class TrackLocationData {
	/**
	 * Latitude in degrees
	 */
	public double latitude;
	/**
	 * Longitude in degrees
	 */
	public double longitude;

	/**
	 * 
	 */
	public TrackLocationData(double lat, double lon) {
		latitude = lat;
		longitude = lon;
	}
}