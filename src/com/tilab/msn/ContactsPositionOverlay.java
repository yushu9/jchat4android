package com.tilab.msn;

import jade.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;

import android.location.Location;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;
import com.google.android.maps.MapView.DeviceType;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactsPositionOverlay.
 */
public class ContactsPositionOverlay extends Overlay {
	
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	/** The UPPE r_ threshold. */
	private int UPPER_THRESHOLD = 0;
	
	/** The LOWE r_ threshold. */
	private  int LOWER_THRESHOLD = 0;
	
	/** The map controller. */
	private MapController mapController; 
	
	/** The my paint. */
	private Paint myPaint;
	
	/** The ylw paddle. */
	private Bitmap ylwPaddle;
	
	/** The blue baloon. */
	private Bitmap blueBaloon;
	
	/** The highlight. */
	private Bitmap highlight;
	
	/** The blue paddle. */
	private Bitmap bluePaddle;
	
	/** The app res. */
	private Resources appRes;
	
	/** The contact position map. */
	private Map<String, ContactLayoutData> contactPositionMap;
	
	
	//The SCROLL area represents the area that finds when points are going out of the screen 
	//If one of them is out of the hot area we need to recenter the map to follow the point.
	//HotAreaWidth/ScreenWidth
	/** The SCROL l_ are a_ widt h_ ratio. */
	private static final float SCROLL_AREA_WIDTH_RATIO = 0.70f;
	
	/** The SCROL l_ are a_ heigh t_ ratio. */
	private static final float SCROLL_AREA_HEIGHT_RATIO= 0.70f;
	
	//This ratio is referred to SCREEN WIDTH
	/** The UPPE r_ threshol d_ ratio. */
	private static final float UPPER_THRESHOLD_RATIO = 0.46f;
	
	/** The LOWE r_ threshol d_ ratio. */
	private static final float LOWER_THRESHOLD_RATIO = 0.35f;
	
	/** The SCROL l_ are a_ width. */
	private int SCROLL_AREA_WIDTH=-1;
	
	/** The SCROL l_ are a_ height. */
	private int SCROLL_AREA_HEIGHT=-1;
	
	
	/** The my map view. */
	private MapView myMapView;

	/** The WIDTH. */
	private int WIDTH=-1;
	
	/** The HEIGHT. */
	private int HEIGHT=-1;
	
	/** The center screen x. */
	private int centerScreenX;
	
	/** The center screen y. */
	private int centerScreenY;
	

	/** The ZOO m_ max. */
	private static final int ZOOM_MAX=0;
	
	/** The RECOMPUT e_ zoom. */
	private static final int RECOMPUTE_ZOOM=1;
	
	/** The N o_ zoom. */
	private static final int NO_ZOOM=2;
	
	/** The scrolling area. */
	private Rect scrollingArea;
	
	/**
	 * Instantiates a new contacts position overlay.
	 * 
	 * @param myMapView the my map view
	 * @param ctn the ctn
	 */
	public ContactsPositionOverlay(MapView myMapView, Resources ctn){
		mapController = myMapView.getController();
		appRes= ctn;
		myPaint = new Paint();
		this.myMapView = myMapView;
		scrollingArea= new Rect();			
		contactPositionMap = new HashMap<String,ContactLayoutData>() ;
		ContactLocation myCLoc = ContactManager.getInstance().getMyContactLocation();
		Contact myCont = ContactManager.getInstance().getMyContact();
		ContactLayoutData myCData = new ContactLayoutData(myCont.getName(), myCont.getPhoneNumber(), myCLoc);
		myCData.isMyContact=true;
		contactPositionMap.put(myCont.getPhoneNumber(), myCData);
		ylwPaddle = BitmapFactory.decodeResource(appRes,R.drawable.ylw_circle); 
		highlight = BitmapFactory.decodeResource(appRes,R.drawable.checked);
		blueBaloon = BitmapFactory.decodeResource(appRes,R.drawable.bluemessage);
		bluePaddle = BitmapFactory.decodeResource(appRes,R.drawable.blu_circle);		
	}	
	
	/**
	 * Scrolling is needed.
	 * 
	 * @return true, if successful
	 */
	private boolean scrollingIsNeeded(){
		
		Collection<ContactLayoutData> pointList = contactPositionMap.values();
		
		for (Iterator<ContactLayoutData> iterator = pointList.iterator(); iterator.hasNext();) {
			ContactLayoutData contactLayoutData =  iterator.next();
			if (!scrollingArea.contains(contactLayoutData.positionOnScreen[0], contactLayoutData.positionOnScreen[1])){
				return true;
			}
		} 
			
		
		
		return false;
		
	}
	
	/**
	 * Zoom change is needed.
	 * 
	 * @param params the params
	 * 
	 * @return the int
	 */
	private int zoomChangeIsNeeded(PointClusterParams params){
		
		int retval = NO_ZOOM;
		
		int currentNumberOfPoints = contactPositionMap.size();
		
		//If we have just one point left, we need to zoom to max level
		if (currentNumberOfPoints == 1 &&  myMapView.getZoomLevel() < 21){
			retval = ZOOM_MAX;
		} else if (currentNumberOfPoints > 1){
		 
			//If we have many points compute the max squared distance from the midpoint
				int maxDistSquared = getMaxDistSquared(contactPositionMap.values(), params.midpointOnScreen);
					
			//if we are in the too far or too near range
				if (maxDistSquared < LOWER_THRESHOLD || maxDistSquared > UPPER_THRESHOLD){
					retval = RECOMPUTE_ZOOM;
				}					
		}
		return retval;
	}
	
	/**
	 * Do scrolling.
	 * 
	 * @param params the params
	 */
	private void doScrolling(PointClusterParams params){

		mapController.centerMapTo(params.midpointOnMap, true);
	}
	
	/**
	 * Do zoom.
	 * 
	 * @param params the params
	 * @param howToZoom the how to zoom
	 */
	private void doZoom(PointClusterParams params, int howToZoom){
		if (howToZoom == ZOOM_MAX)
			mapController.zoomTo(18);
		if (howToZoom == RECOMPUTE_ZOOM)
			mapController.zoomToSpan(params.coordMaxSpan[0],params.coordMaxSpan[1]);
	}	
	
	
	/**
	 * Draw online contacts.
	 * 
	 * @param c the c
	 * @param p the p
	 */
	private void drawOnlineContacts(Canvas c, Paint p){
		
		FontMetrics fm = p.getFontMetrics();	
		
		int bluePaddleOffsetY= bluePaddle.getHeight();
        int bluePaddleOffsetX= bluePaddle.getWidth()/2;
        int ylwPaddleOffsetY= ylwPaddle.getHeight();
        int ylwPaddleOffsetX= ylwPaddle.getWidth()/2;
        int blueBaloonOffsetY = 25;
        int blueBaloonOffsetX = 4;
        myPaint.setTextSize(18);
        
        Set<String> allParticipants = MsnSessionManager.getInstance().getAllParticipantIds();
		int color=0;
		int iconToTextOffsetY= 5;
		
        for (Iterator<ContactLayoutData>iterator = contactPositionMap.values().iterator(); iterator.hasNext();) {
        	ContactLayoutData cData = (ContactLayoutData) iterator.next();
           	int bitmapOriginX=0;
        	int bitmapOriginY=0;
        	Bitmap bitmapToBeDrawn = null;
        	
			if (cData.isMyContact){	
				color = Color.YELLOW;				
				bitmapOriginX = cData.positionOnScreen[0] - ylwPaddleOffsetX;
				bitmapOriginY = cData.positionOnScreen[1] - ylwPaddleOffsetY;
				bitmapToBeDrawn = ylwPaddle;
			} 
			else {			
				//Here blueBaloon for people you're chatting with				 
				if(allParticipants.contains(cData.idContact)){																
					bitmapOriginX = cData.positionOnScreen[0]-blueBaloonOffsetX;
					bitmapOriginY = cData.positionOnScreen[1]-blueBaloonOffsetY;
					bitmapToBeDrawn = blueBaloon;
				}			    
			    else{			
			    	bitmapOriginX = cData.positionOnScreen[0]-bluePaddleOffsetX;
					bitmapOriginY = cData.positionOnScreen[1]-bluePaddleOffsetY;
			    	bitmapToBeDrawn = bluePaddle;
			    }	    
				color = Color.BLUE;
			}
			
		  int textOriginX = cData.positionOnScreen[0];
		  int textOriginY = bitmapOriginY - iconToTextOffsetY;
		  
		  
		  RectF rect = new RectF(textOriginX - 2, textOriginY + (int) fm.top - 2, textOriginX +this.getStringLength(cData.name, myPaint) + 2,textOriginY + (int) fm.bottom + 2);		  
		  int width = bluePaddle.getWidth();
		  int height = bluePaddle.getHeight();
		  
		  //Draws the debugging rct for collision
          //myPaint.setAlpha(100);
		 // c.drawRect(new Rect(cData.positionOnScreen[0]- width/2, cData.positionOnScreen[1]-height, cData.positionOnScreen[0]+width/2, cData.positionOnScreen[1]),myPaint);
		  //Draw the right bitmap icon
		  
		  
		  if (cData.isChecked){
			  c.drawBitmap(highlight, bitmapOriginX, bitmapOriginY, myPaint);
		  } else {
			  c.drawBitmap(bitmapToBeDrawn, bitmapOriginX, bitmapOriginY, myPaint);  
		  }
		  
		  //Change color for background rectangle
		  myPaint.setColor(Color.argb(100, 0,0, 0));		 	
		  c.drawRoundRect(rect, 4.0f, 4.0f, myPaint);// Rect(rect, myPaint);		  
		  //ChangeColor for text
		  myPaint.setColor(color);
          c.drawText(cData.name,textOriginX, textOriginY, myPaint);	
          
         
          
          myPaint.setARGB(255, 255, 0, 0);
		}		
	}	
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.Overlay.PixelCalculator, boolean)
	 */
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {		
		super.draw(canvas, calculator, shadow);
		
		updateOnScreenPosition(calculator);
		//Compute params needed for further computations on the point cluster
		PointClusterParams params = extractParams(calculator);
		
		//Things we do just the first time
		if (WIDTH == -1){
			initialize(calculator);
			doScrolling(params);
			doZoom(params, RECOMPUTE_ZOOM);
		} else {
		
			//if any pixel is out our scrolling area
			if (scrollingIsNeeded()){
				//change map center
				doScrolling(params);
			}
			
			int howToZoom = zoomChangeIsNeeded(params);
			
			if (howToZoom != NO_ZOOM)	{
				doZoom(params,howToZoom);
			}
		}
		//Draw all the contacts
		drawOnlineContacts(canvas, myPaint);
	
	}
	
	
	
	/**
	 * Initialize.
	 * 
	 * @param calculator the calculator
	 */
	private void initialize( PixelCalculator calculator ) {
			WIDTH = calculator.getMapWidth();
			HEIGHT = calculator.getMapHeight();
			centerScreenX = WIDTH / 2;
			centerScreenY = HEIGHT / 2;

			
			myLogger.log(Logger.INFO, "WIDTH = "+WIDTH);
			myLogger.log(Logger.INFO, "HEIGHT = "+HEIGHT);
			myLogger.log(Logger.INFO, "MapViev WIDTH = "+myMapView.getWidth());
			myLogger.log(Logger.INFO, "Map View HEIGHT = "+myMapView.getHeight());
			
			SCROLL_AREA_HEIGHT = (int) (HEIGHT * SCROLL_AREA_HEIGHT_RATIO);
			SCROLL_AREA_WIDTH = (int) (WIDTH * SCROLL_AREA_WIDTH_RATIO);
			scrollingArea.top = (HEIGHT - SCROLL_AREA_HEIGHT)/2;
			scrollingArea.bottom = scrollingArea.top + SCROLL_AREA_HEIGHT;
			scrollingArea.left = (WIDTH - SCROLL_AREA_WIDTH)/2;
			scrollingArea.right = scrollingArea.left + SCROLL_AREA_WIDTH;
			int tmpThresh =  (int ) (WIDTH * UPPER_THRESHOLD_RATIO);
			UPPER_THRESHOLD = tmpThresh * tmpThresh;
			tmpThresh = (int) (WIDTH * LOWER_THRESHOLD_RATIO);
			LOWER_THRESHOLD = tmpThresh * tmpThresh;
			extractParams(calculator);
	
	}
	
	/**
	 * Update on screen position.
	 * 
	 * @param calc the calc
	 */
	private void updateOnScreenPosition(PixelCalculator calc){
		for (ContactLayoutData cData : contactPositionMap.values()) {
			calc.getPointXY(new Point(cData.latitudeE6, cData.longitudeE6), cData.positionOnScreen);
		}
	}
	
	/**
	 * Extract params.
	 * 
	 * @param calc the calc
	 * 
	 * @return the point cluster params
	 */
	private PointClusterParams extractParams(PixelCalculator calc){
		
		int maxLat;
		int minLat;
		int maxLong;
		int minLong;
		
	
		
		PointClusterParams params = new PointClusterParams();
		
		//Compute needed params for my contact
		Location myContactLoc = ContactManager.getInstance().getMyContactLocation(); 			
		maxLat = (int)(myContactLoc.getLatitude() * 1E6);
		maxLong = (int)(myContactLoc.getLongitude() * 1E6);
		minLong = (int)(myContactLoc.getLongitude() * 1E6);
		minLat = (int)(myContactLoc.getLatitude() * 1E6);
		
		
		int contactsOnLine = contactPositionMap.size();
		params.midpointOnScreen = new int[2];
		params.coordMaxSpan = new int[2];
		
		for (Iterator<ContactLayoutData> iterator = contactPositionMap.values().iterator(); iterator.hasNext();) {
			ContactLayoutData clData = (ContactLayoutData) iterator.next();
			
			params.midpointOnScreen[0] += clData.positionOnScreen[0];
			params.midpointOnScreen[1] += clData.positionOnScreen[1];
			
						

			maxLat = (clData.latitudeE6> maxLat)? clData.latitudeE6 : maxLat;
			maxLong = (clData.longitudeE6 > maxLong)? clData.longitudeE6 : maxLong;
			minLong = (clData.longitudeE6 < minLong)? clData.longitudeE6 : minLong;
			minLat = (clData.latitudeE6 < minLat)? clData.latitudeE6 : minLat;
			
			
			//we need to zoom in another way if we have a single point
			
			if (maxLat == minLat){
				params.coordMaxSpan[0] = -1;
				params.coordMaxSpan[1] = -1;
			} else {
				params.coordMaxSpan[0] = maxLat -minLat;
				params.coordMaxSpan[1] = maxLong - minLong;
			}	
		}
		
		params.midpointOnScreen[0] /= contactsOnLine;
		params.midpointOnScreen[1] /= contactsOnLine;
		
		params.midpointOnMap = screenToMap(params.midpointOnScreen);
		
		return params;
	
	}
	
	/**
	 * Gets the max dist squared.
	 * 
	 * @param points the points
	 * @param midpoint the midpoint
	 * 
	 * @return the max dist squared
	 */
	private int getMaxDistSquared(Collection<ContactLayoutData> points,int[] midpoint){
		
		int maxDist =0;
		
		//For each point
		for (Iterator <ContactLayoutData>iterator = points.iterator(); iterator.hasNext();) {
			ContactLayoutData contactLayoutData = iterator.next();
			//Compute distance squared			
			int distX = midpoint[0] - contactLayoutData.positionOnScreen[0];
			int distY = midpoint[1] - contactLayoutData.positionOnScreen[1];
			int distSq = distX*distX + distY*distY;
			
			if (distSq > maxDist)
				maxDist = distSq;
		} {
			
		}
		
		return maxDist;
	}	

	
	/* (non-Javadoc)
	 * @see com.google.android.maps.Overlay#onTap(com.google.android.maps.MapView.DeviceType, com.google.android.maps.Point, com.google.android.maps.Overlay.PixelCalculator)
	 */
	public boolean onTap(DeviceType deviceType, Point p, PixelCalculator calculator) {

		int[] pointOnScreen = new int[2];
		calculator.getPointXY(p, pointOnScreen);
		
		checkClickedPosition(pointOnScreen); 
		
        return true;
	}
	
	
	//Converts a point on screen (pixel coordinates) into  point on the map (Lat/Long Coordinated)
	
	/**
	 * Screen to map.
	 * 
	 * @param point the point
	 * 
	 * @return the point
	 */
	private Point screenToMap(int [] point) {
		//Calculate ratio 
		int latitudeSpan = myMapView.getLatitudeSpan();
		int longitudeSpan = myMapView.getLongitudeSpan();

		int microDegreePerPixelLatitude = latitudeSpan / HEIGHT;
		int microDegreePerPixelLongitude = longitudeSpan / WIDTH;
		int deltaX = centerScreenX - point[0];
		int deltaY = centerScreenY - point[1];
		int deltaLatitude = microDegreePerPixelLatitude * deltaY;
		int deltaLongitude = microDegreePerPixelLongitude * deltaX;
		int computedLocationLatitude = myMapView.getMapCenter().getLatitudeE6()
		          + deltaLatitude;
		int computedLocationLongitude = myMapView.getMapCenter().getLongitudeE6()
		          - deltaLongitude;
		
		return  new Point(computedLocationLatitude,computedLocationLongitude);
	}
	
	/**
	 * The Class PointClusterParams.
	 */
	private class PointClusterParams {
		
		/** The coord max span. */
		public int[] coordMaxSpan;
		
		/** The midpoint on map. */
		public Point midpointOnMap;
		
		/** The midpoint on screen. */
		public int[] midpointOnScreen;
	}
	
	//This class represents the data of the contact to be displayed (position, name, color)
	/**
	 * The Class ContactLayoutData.
	 */
	private class ContactLayoutData{
	
		/** The position on screen. */
		public int[] positionOnScreen;
		
		/** The latitude e6. */
		public int latitudeE6;
		
		/** The longitude e6. */
		public int longitudeE6;
		
		/** The altitude e6. */
		public int altitudeE6;
		
		/** The name. */
		public String name;
		
		/** The is checked. */
		public boolean isChecked;
		
		/** The is my contact. */
		public boolean isMyContact;
		
		/** The id contact. */
		public String idContact;
		
		//Constructor for storing midpoint data
		/**
		 * Instantiates a new contact layout data.
		 * 
		 * @param x the x
		 * @param y the y
		 * @param latitudeE6 the latitude e6
		 * @param longitudeE6 the longitude e6
		 * @param altitudeE6 the altitude e6
		 */
		public ContactLayoutData(int x, int y, int latitudeE6, int longitudeE6, int altitudeE6){
			name = "Midpoint";
			isMyContact=false;
			isChecked = false;
			positionOnScreen= new int[2];
			positionOnScreen[0] =x;
			positionOnScreen[1] = y;
			this.latitudeE6= latitudeE6;
			this.longitudeE6= longitudeE6;
			this.altitudeE6= altitudeE6;
		}
		
		
		/**
		 * Instantiates a new contact layout data.
		 * 
		 * @param cname the cname
		 * @param idcontact the idcontact
		 * @param contactLoc the contact loc
		 */
		public ContactLayoutData(String cname, String idcontact, Location contactLoc){
			this.name = cname;
			this.idContact= idcontact;			
			isMyContact = false;
			positionOnScreen = new int[2];
			latitudeE6 = (int)(contactLoc.getLatitude() * 1E6);
			longitudeE6 = (int) (contactLoc.getLongitude() * 1E6);
			altitudeE6=(int)(contactLoc.getAltitude()*1E6);
			
		}	
		
		/**
		 * Update location.
		 * 
		 * @param latitude the latitude
		 * @param longitude the longitude
		 * @param altitude the altitude
		 */
		public void updateLocation(int latitude, int longitude, int altitude){
				latitudeE6 = latitude;
				longitudeE6 = longitude;
				altitudeE6 = altitude;
		}
		
		/**
		 * Update position on screen.
		 * 
		 * @param pixCalc the pix calc
		 */
		public void updatePositionOnScreen(PixelCalculator pixCalc){
			pixCalc.getPointXY(new Point(latitudeE6, longitudeE6), positionOnScreen);
		}
	}
	
	/**
	 * Gets the string length.
	 * 
	 * @param name the name
	 * @param paint the paint
	 * 
	 * @return the string length
	 */
	private int getStringLength (String name, Paint paint) {
	   float [] widthtext= new float[name.length()];	   
	   float sumvalues=0;
	   paint.getTextWidths(name, widthtext);
	   for(int n=0; n<widthtext.length; n++){ 
		   sumvalues+= widthtext[n];		
	   }
	   return (int) sumvalues; 	    
       }
	
		
	
     /**
      * Check clicked position.
      * 
      * @param point the point
      */
     private void checkClickedPosition (int[] point)
     { 
    	 
    	 
    	 int width= bluePaddle.getWidth();
    	 int height= bluePaddle.getHeight();
    	 String myId = ContactManager.getInstance().getMyContact().getPhoneNumber();
    	 
    	 for (ContactLayoutData contact : contactPositionMap.values()){
    		Rect r= new Rect(contact.positionOnScreen[0]- width/2, contact.positionOnScreen[1]-height, contact.positionOnScreen[0]+width/2, contact.positionOnScreen[1] );
    		if(r.contains(point[0], point[1]) && !contact.idContact.equals(myId) ){    			
    		    contact.isChecked = !contact.isChecked;    		   
    		}
    		   		
    	 }
    	 	
     }
     
     /**
      * Gets the selected items.
      * 
      * @return the selected items
      */
     public List<String> getSelectedItems(){
    	 
    	 List<String> ids = new ArrayList<String>();
    	 
    	 for (ContactLayoutData cdata : contactPositionMap.values()) {
    		 if (cdata.isChecked){
    			 ids.add(cdata.idContact);
    		 }
    	 }
    	 
    	 return ids;
     }
     
     /**
      * Uncheck all contacts.
      */
     public void uncheckAllContacts(){
    	 
    	 for (ContactLayoutData data : contactPositionMap.values()) {
    		 data.isChecked=false;
    	//	 Rect r= new Rect(data.positionOnScreen[0]- width/2, data.positionOnScreen[1]-height, data.positionOnScreen[0]+width/2, data.positionOnScreen[1] );
    		 myMapView.invalidate();
    	 }
     }
     
     /**
      * Update.
      * 
      * @param changes the changes
      */
     public void update(ContactListChanges changes){ 
	     
	          
	     //Removed contacts
	     for ( String removedId : changes.contactsDeleted) {
	    	 contactPositionMap.remove(removedId);
		 }
	     
	     Map<String,ContactLocation> locationMap = ContactManager.getInstance().getAllContactLocations();
	     Map<String, Contact> contactMap = ContactManager.getInstance().getAllContacts();
	     
	   //Added contacts
	     for ( String addedId : changes.contactsAdded) {
	    	 
	    	 ContactLayoutData newData = new ContactLayoutData(contactMap.get(addedId).getName(),addedId,locationMap.get(addedId));
	    	 contactPositionMap.put(addedId, newData);
	     }
	      
	     //update all others
	     for (ContactLayoutData cData : contactPositionMap.values()) {
	    	 ContactLocation lastLocation= null;
	    	 if (cData.isMyContact){
	    	     lastLocation = ContactManager.getInstance().getMyContactLocation();
	    	 }else {
	    		 lastLocation = locationMap.get(cData.idContact);
	    	 }
	    	 
	    	 cData.updateLocation((int)(lastLocation.getLatitude()*1E6), (int)(lastLocation.getLongitude()*1E6), (int)(lastLocation.getAltitude()*1E6));
		 }
	     
     }
       
	
	}