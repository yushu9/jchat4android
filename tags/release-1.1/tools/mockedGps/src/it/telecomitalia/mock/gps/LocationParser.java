/**
 * 
 */
package it.telecomitalia.mock.gps;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


/**
 * @author s.semeria
 *
 */
public abstract class LocationParser {

	private File locationFile;
	
	protected LocationParser(File file){
		locationFile = file;
	}
	
	protected String getFileName(){
		return locationFile.getName();
	}
	
	protected File getFile(){
		return locationFile;
	}
	
	public abstract List<TrackLocationData> parseLocationList() throws ParserConfigurationException, SAXException, IOException ;
	
}
