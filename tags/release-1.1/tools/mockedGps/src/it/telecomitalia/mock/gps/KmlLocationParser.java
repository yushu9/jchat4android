/**
 * 
 */
package it.telecomitalia.mock.gps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;


/**
 * Provides parsing mechanism for parsing file using a sax parser
 * @author s.semeria
 *
 */
public class KmlLocationParser extends LocationParser  {

	/**
	 * Parses a KML file
	 * @param file file to be parsed
	 */
	public KmlLocationParser(File file) {
		super(file);
	}

	/* (non-Javadoc)
	 * @see com.telecomitalia.mockedgps.LocationParser#getLocationList()
	 */
	public List<TrackLocationData> parseLocationList() throws ParserConfigurationException, SAXException, IOException  {
		
		InputStream is = new FileInputStream(getFile());
		//create an input source for our sax parser
		InputSource isource = new InputSource(is);
		
		//create the sax parser
		SAXParserFactory  parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		
		XMLReader reader = parser.getXMLReader();
		KmlContentHandler myHandler = new KmlContentHandler();
		reader.setContentHandler(myHandler);
		//parse the file, delivering data to content handler 
		Log.v(KmlLocationParser.class.getName(), "Starting to parse kml file " +  getFileName() + " ...");
		reader.parse(isource);
		Log.v(KmlLocationParser.class.getName(), "Kml parsing finished! ");
		List<TrackLocationData> list = myHandler.getLocationList();
		
		return list;
	}
	
	


	
	
}
