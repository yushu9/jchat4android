/**
 * 
 */
package it.telecomitalia.mock.gps;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import android.util.Log;

/**
 * @author s.semeria
 *
 */
public class KmlContentHandler extends DefaultHandler {

	private final String KMLPARSER_COORD_TAGNAME= "coordinates";
	private boolean inCoordinatesTag;
	private List<TrackLocationData> locationDataList;
	
	public KmlContentHandler(){
		inCoordinatesTag = false;
	}
	
	public List<TrackLocationData> getLocationList(){
		return locationDataList;
	}
	
	 @Override 
     public void startDocument() throws SAXException { 
		 locationDataList = new ArrayList<TrackLocationData>();
		 
     } 

     @Override 
     public void endDocument() throws SAXException { 
          // Nothing to do 
     } 

     /** Gets be called on opening tags like: 
      * <tag> 
      * Can provide attribute(s), when xml was like: 
      * <tag attribute="attributeValue">
      * 
      * */
     @Override 
     public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
          if (localName.equals(KMLPARSER_COORD_TAGNAME)) { 
        	  Log.v(KmlContentHandler.class.getName(), "Entering " + localName + " tag..."); 
        	  this.inCoordinatesTag = true; 
          }
     } 
      
     /** Gets be called on closing tags like: 
      * </tag> */ 
     @Override 
     public void endElement(String namespaceURI, String localName, String qName) 
               throws SAXException { 
          if (localName.equals(KMLPARSER_COORD_TAGNAME)) { 
        	  Log.v(KmlContentHandler.class.getName(), "Exiting from " + localName + " tag...");
        	  this.inCoordinatesTag=false;
          }
     } 
      
     /** Gets be called on the following structure: 
      * <tag>characters</tag> */ 
     @Override 
    public void characters(char ch[], int start, int length) { 
          if(this.inCoordinatesTag){ 
        	  //extract a list of locations
        	  Log.v(KmlContentHandler.class.getName(), "Retrieved string:  " + ch + ". Beginning string parsing to extract locations....");
        	  List<TrackLocationData> partialList = extractLocations(new String(ch));
        	  //add the location list to the big one
        	  locationDataList.addAll(partialList);
          } 
    } 
	
     /**
 	 * Parse the content of <coordinates> tag
 	 * @param locListStr
 	 * @return list of location
 	 */
 	private List<TrackLocationData> extractLocations(String locListStr){
 		List<TrackLocationData> locationList = new ArrayList<TrackLocationData>();
 		StringTokenizer tokenizer = new StringTokenizer(locListStr);
 		
 		//<coordinates> contains a list of location separated by whitespaces
 		while (tokenizer.hasMoreTokens()){
 			String token = tokenizer.nextToken();
 			  Log.v(KmlContentHandler.class.getName(), "Found token " + token);
 			//Each token is a location. Locations are a list of coordinates separated with comma
 			StringTokenizer commaTokenizer = new StringTokenizer(token,",");
 			
 			String longitude=null;
 			String latitude = null;
 			
 			//We have three comma separated items
 			if (commaTokenizer.hasMoreTokens()) {
 				longitude = commaTokenizer.nextToken();
 				if (commaTokenizer.hasMoreTokens()) {
	 				latitude = commaTokenizer.nextToken();
	 				Log.v(KmlContentHandler.class.getName(), "Found location with latitude " +  latitude + " and longitude " + longitude);
	 	 			TrackLocationData location = new TrackLocationData(Double.parseDouble(latitude), Double.parseDouble(longitude));
	 	 			locationList.add(location);
 				}
 			}
 		}
 		
 		return locationList;
 	}

	
}
