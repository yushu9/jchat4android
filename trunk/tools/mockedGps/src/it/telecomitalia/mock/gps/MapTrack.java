/**
 * 
 */
package it.telecomitalia.mock.gps;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;



/**
 * @author s.semeria
 *
 */
public class MapTrack {


	private List<TrackLocationData>  locationList;
	
	/**
	 * Builds a new track by loading data from a file
	 * Different formats can be supporting depending on the file extension 
	 * 
	 * @param file file to parse
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws  
	 */
	public MapTrack(String file) throws ParserConfigurationException, SAXException, IOException {
		LocationParser parser = null;
		
		if (isKml(file)){
			File f = new File(file);
			parser = new KmlLocationParser(f);
		} else {
			throw new IllegalArgumentException("File seems not to be kml!");
		}
		
		locationList = parser.parseLocationList();
	}
	
	private boolean isKml(String file){
		return file.endsWith(".kml");
	}
	
	/**
	 * Returns the next location in the track
	 * @return previous location
	 */
	public ListIterator<TrackLocationData> iterator(){
		return locationList.listIterator();
	}

	/**
	 * Returns the previous location in the track
	 * @return previous location
	 */
	public int getLocationNumber(){
		return locationList.size();
	}
	
}
