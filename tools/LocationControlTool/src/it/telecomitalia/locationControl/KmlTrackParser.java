/**
 * 
 */
package it.telecomitalia.locationControl;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.android.ddmuilib.location.WayPoint;

/**
 * @author s.semeria
 *
 */
public class KmlTrackParser {

	 private String mFileName;
	private KmlHandler mHandler;
	 private static SAXParserFactory sParserFactory = SAXParserFactory.newInstance();
	 
	 private static class KmlHandler extends DefaultHandler
	    {
		 	private final int OUT=0;
		 	private final int INSIDE_DOCUMENT_TAG=1;
		 	private final int INSIDE_DOCUMENT_NAME_TAG=2;
		 	private final int INSIDE_PLACEMARK_TAG=3;
		 	private final int INSIDE_MULTI_GEOMETRY_TAG=4;
		 	private final int INSIDE_COORDINATES_PLACEMARK_TAG=5;
		 	private int where;
		 	private String mRouteName;
		 	private int counter;
		 	
		    List<WayPointLocation> mWayPoints;
	        WayPoint mCurrentWayPoint;
	        final StringBuilder mStringAccumulator;
	        boolean mSuccess;

	        public void startElement(String uri, String localName, String name, Attributes attributes)
	            throws SAXException
	        {
	        	if ("Document".equals(name)){
	        		where = INSIDE_DOCUMENT_TAG;
	        	}
	            else if ("name".equals(name) && where == INSIDE_DOCUMENT_TAG)
	            {
	            	where = INSIDE_DOCUMENT_NAME_TAG;
	            }
	            if("Placemark".equals(name))
	            {
	                where=INSIDE_PLACEMARK_TAG;
	            }  
	            else if ("MultiGeometry".equals(name) && where == INSIDE_PLACEMARK_TAG  )
	            {
	            	where = INSIDE_MULTI_GEOMETRY_TAG;
	            }
	            else if ("coordinates".equals(name) && where == INSIDE_MULTI_GEOMETRY_TAG)
	            {
	            	where = INSIDE_COORDINATES_PLACEMARK_TAG;	
	            }
	            
	        }

	        public void characters(char ch[], int start, int length)
	            throws SAXException
	        {
	        	if (where == INSIDE_COORDINATES_PLACEMARK_TAG || where == INSIDE_DOCUMENT_NAME_TAG){
	        		mStringAccumulator.append(ch, start, length);
	        	}
	        }

	        public void endElement(String uri, String localName, String name)
	            throws SAXException
	        {
	                if("coordinates".equals(name) && where == INSIDE_COORDINATES_PLACEMARK_TAG)
	                {
	                	parseLocation(mStringAccumulator.toString());
		            	mStringAccumulator.setLength(0);
		            	where = OUT;
	                } 
	                else if ("name".equals(name) && where == INSIDE_DOCUMENT_NAME_TAG)
	                {
	                	mRouteName = mStringAccumulator.toString();
	                	mStringAccumulator.setLength(0);
	                	where = OUT;
	                }
	                
	                    
	        }

	        public void error(SAXParseException e)
	            throws SAXException
	        {
	            mSuccess = false;
	        }

	        public void fatalError(SAXParseException e)
	            throws SAXException
	        {
	            mSuccess = false;
	        }

	        private void parseLocation(String location)
	        {
	        	StringTokenizer coordBlockTokenizer = new StringTokenizer(location);
	        	
	        	while (coordBlockTokenizer.hasMoreTokens()){
	        		counter++;
	        		String coordinateBlock = coordBlockTokenizer.nextToken();
	        		
	        		StringTokenizer coordinateTokenizer = new StringTokenizer(coordinateBlock,",");
	        		
	        		double longitude =  Double.parseDouble(coordinateTokenizer.nextToken());
	        		double latitude = Double.parseDouble(coordinateTokenizer.nextToken());
	        		double altitude = Double.parseDouble(coordinateTokenizer.nextToken());
	        		
	        		WayPointLocation waypoint = new WayPointLocation(counter,  latitude, longitude, altitude);
	        		mWayPoints.add(waypoint);
	        	}
	        }

	        public String getRouteName()
	        {
	        	return mRouteName;
	        }
	        
	        public WayPointLocation[] getWayPoints()
	        {
	            if(mWayPoints != null)
	                return (WayPointLocation[])mWayPoints.toArray(new WayPointLocation[mWayPoints.size()]);
	            else
	                return null;
	        }

	        public boolean getSuccess()
	        {
	            return mSuccess;
	        }

	        
	        private KmlHandler()
	        {
	            mStringAccumulator = new StringBuilder();
	            mSuccess = true;
	            where=OUT;
	            counter=0;
	            mWayPoints = new ArrayList<WayPointLocation>();
	            mRouteName = "";
	        }

	    }
	
	    public KmlTrackParser(String fileName)
	    {
	        mFileName = fileName;
	    }

	    public boolean parse()
	    {
	        boolean flag=true;
	        try
	        {
	            SAXParser parser = sParserFactory.newSAXParser();
	            mHandler = new KmlHandler();
	            parser.parse(new InputSource(new FileReader(mFileName)), mHandler);
	            flag = mHandler.getSuccess();
	        }
	        catch(Exception e)
	        {
	        	flag = false;
	        	e.printStackTrace();
	        }       
	       
	        return flag;
	    }

	    public String getRouteName(){
	    	return mHandler.getRouteName();
	    }
	    
	    public WayPointLocation[] getWayPoints()
	    {
	        if(mHandler != null)
	            return mHandler.getWayPoints();
	        else
	            return null;
	    }
}
