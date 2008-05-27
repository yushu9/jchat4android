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

import android.location.Location;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;
import com.google.android.maps.MapView.DeviceType;

public class ContactsPositionOverlay extends Overlay {
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private int UPPER_THRESHOLD = 0;
	private  int LOWER_THRESHOLD = 0;
	private PointClusterParams params;
	private MapController mapController; 
	private Paint myPaint;
	private Bitmap ylwPaddle;
	private Bitmap blueBaloon;
	private Bitmap bluePaddle;
	private Resources appRes;
	private Map<String, ContactLayoutData> contactPositionMap;
	
	
	//The SCROLL area represents the area that finds when points are going out of the screen 
	//If one of them is out of the hot area we need to recenter the map to follow the point.
	//HotAreaWidth/ScreenWidth
	private final float SCROLL_AREA_WIDTH_RATIO = 0.70f;
	private final float SCROLL_AREA_HEIGHT_RATIO= 0.70f;
	
	//This ratio is referred to SCREEN WIDTH
	private final float UPPER_THRESHOLD_RATIO = 0.46f;
	private final float LOWER_THRESHOLD_RATIO = 0.35f;
	
	private int SCROLL_AREA_WIDTH=-1;
	private int SCROLL_AREA_HEIGHT=-1;
	
	
	private MapView myMapView;

	private int WIDTH=-1;
	private int HEIGHT=-1;
	private int centerScreenX;
	private int centerScreenY;
	

	private final int ZOOM_MAX=0;
	private final int RECOMPUTE_ZOOM=1;
	private final int NO_ZOOM=2;
	
	private Rect scrollingArea;
	
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
		blueBaloon = BitmapFactory.decodeResource(appRes,R.drawable.bluemessage);
		bluePaddle = BitmapFactory.decodeResource(appRes,R.drawable.blu_circle);
		
	}	
	
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
	
	private void doScrolling(PointClusterParams params){

		mapController.centerMapTo(params.midpointOnMap, true);
	}
	
	private void doZoom(PointClusterParams params, int howToZoom){
		if (howToZoom == ZOOM_MAX)
			mapController.zoomTo(18);
		if (howToZoom == RECOMPUTE_ZOOM)
			mapController.zoomToSpan(params.coordMaxSpan[0],params.coordMaxSpan[1]);
	}	
	
	
	private void drawOnlineContacts(Canvas c, Paint p){
		
		int size = contactPositionMap.size();
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
		  if (cData.isChecked)
			  myPaint.setColor(Color.GREEN);
		  else 
			  myPaint.setColor(Color.BLUE);
          myPaint.setAlpha(100);
        
		  c.drawRect(new Rect(cData.positionOnScreen[0]- width/2, cData.positionOnScreen[1]-height, cData.positionOnScreen[0]+width/2, cData.positionOnScreen[1]),myPaint);
		  //Draw the right bitmap icon
		  c.drawBitmap(bitmapToBeDrawn, bitmapOriginX, bitmapOriginY, myPaint);
		  //Change color for background rectangle
		  myPaint.setColor(Color.argb(100, 0,0, 0));		 	
		  c.drawRoundRect(rect, 4.0f, 4.0f, myPaint);// Rect(rect, myPaint);		  
		  //ChangeColor for text
		  myPaint.setColor(color);
          c.drawText(cData.name,textOriginX, textOriginY, myPaint);	
          
         
          
          myPaint.setARGB(255, 255, 0, 0);
		}{        			
    		
 
         }
		
	}	
	
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {		
		super.draw(canvas, calculator, shadow);
		
		updateOnScreenPosition(calculator);
		//Compute params needed for further computations on the point cluster
		extractParams(calculator);
		
		//Things we do just the first time
		if (WIDTH == -1){
			initialize(params, calculator);
		} 
		
		//if any pixel is out our scrolling area
		if (scrollingIsNeeded()){
			//change map center
			doScrolling(params);
		}
		
		int howToZoom = zoomChangeIsNeeded(params);
		
		if (howToZoom != NO_ZOOM)	{
			doZoom(params,howToZoom);
		}
	
		//Draw all the contacts
		drawOnlineContacts(canvas, myPaint);
	
	}
	
	
	
	private void initialize(PointClusterParams params, PixelCalculator calculator) {
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
			doScrolling(params);
			doZoom(params, RECOMPUTE_ZOOM);
	
	}
	
	private void updateOnScreenPosition(PixelCalculator calc){
		for (ContactLayoutData cData : contactPositionMap.values()) {
			calc.getPointXY(new Point(cData.latitudeE6, cData.longitudeE6), cData.positionOnScreen);
		}
	}
	
	private PointClusterParams extractParams(PixelCalculator calc){
		
		int maxLat;
		int minLat;
		int maxLong;
		int minLong;
		
		int midPointX=0;
		int midPointY=0;
		
		PointClusterParams params = new PointClusterParams();
	
		
		//Compute needed params for my contact
		Location myContactLoc = ContactManager.getInstance().getMyContactLocation(); 			
		maxLat = (int)(myContactLoc.getLatitude() * 1E6);
		maxLong = (int)(myContactLoc.getLongitude() * 1E6);
		minLong = (int)(myContactLoc.getLongitude() * 1E6);
		minLat = (int)(myContactLoc.getLatitude() * 1E6);
	
	
	/*	ContactLayoutData myPointData = new ContactLayoutData(myContact.getName(), myContact.getPhoneNumber(),myContactLoc ,calc );
		myPointData.isMyContact = true;
		
		//Compute mid x and y
		midPointX += myPointData.positionOnScreen[0];
		midPointY += myPointData.positionOnScreen[1];
		
		params.contactPoints.add(myPointData);*/
		
		
		//compute params for all the others
//		for (int i = 0; i < contacts.size(); i++) {
	//		Contact ctn = contacts.get(i);
			//only online contacts are drawn on the map
	//		if (ctn.isOnline()){
			
		//		contactsOnLine++;
				
		//		ContactLocation contactLoc = locationMap.get(ctn.getPhoneNumber());
		
		//if (contactLoc != null) {
		/*			int tmpLat = (int)(contactLoc.getLatitude() * 1E6);
					int tmpLong = (int)(contactLoc.getLongitude() * 1E6);
					
					
					
					ContactLayoutData pointData = new ContactLayoutData(ctn.getName(),ctn.getPhoneNumber(),contactLoc ,calc );
					
					midPointX += pointData.positionOnScreen[0];
					midPointY += pointData.positionOnScreen[1];
					
					params.contactPoints.add(pointData);
				}
			}
		}*/
		
		
		int contactsOnLine = contactPositionMap.size();
		
		for (Iterator<ContactLayoutData> iterator = contactPositionMap.values().iterator(); iterator.hasNext();) {
			ContactLayoutData clData = (ContactLayoutData) iterator.next();
			params.midpointOnScreen = new int[2];
			params.midpointOnScreen[0] += clData.positionOnScreen[0];
			params.midpointOnScreen[1] += clData.positionOnScreen[1];
			
			//test
			
			params.coordMaxSpan = new int[2];			

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

	
	public boolean onTap(DeviceType deviceType, Point p, PixelCalculator calculator) {

		int[] pointOnScreen = new int[2];
		calculator.getPointXY(p, pointOnScreen);
		
		checkClickedPosition(pointOnScreen); 
		
        return true;
	}
	
	
	//Converts a point on screen (pixel coordinates) into  point on the map (Lat/Long Coordinated)
	
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
	
	private class PointClusterParams {
		public int[] coordMaxSpan;
		public Point midpointOnMap;
		public int[] midpointOnScreen;
	}
	
	//This class represents the data of the contact to be displayed (position, name, color)
	private class ContactLayoutData{
	
		public int[] positionOnScreen;
		public int latitudeE6;
		public int longitudeE6;
		public int altitudeE6;
		public String name;
		public boolean isChecked;
		public boolean isMyContact;
		public String idContact;
		
		//Constructor for storing midpoint data
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
		
		
		public ContactLayoutData(String cname, String idcontact, Location contactLoc){
			this.name = cname;
			this.idContact= idcontact;			
			isMyContact = false;
			positionOnScreen = new int[2];
			latitudeE6 = (int)(contactLoc.getLatitude() * 1E6);
			longitudeE6 = (int) (contactLoc.getLongitude() * 1E6);
			altitudeE6=(int)(contactLoc.getAltitude()*1E6);
			
		}	
		
		public void updateLocation(int latitude, int longitude, int altitude){
				latitudeE6 = latitude;
				longitudeE6 = longitude;
				altitudeE6 = altitude;
		}
		
		public void updatePositionOnScreen(PixelCalculator pixCalc){
			pixCalc.getPointXY(new Point(latitudeE6, longitudeE6), positionOnScreen);
		}
	}
	
	private int getStringLength (String name, Paint paint) {
	   float [] widthtext= new float[name.length()];	   
	   float sumvalues=0;
	   paint.getTextWidths(name, widthtext);
	   for(int n=0; n<widthtext.length; n++){ 
		   sumvalues+= widthtext[n];		
	   }
	   return (int) sumvalues; 	    
       }
	
		
	
     private void checkClickedPosition (int[] point)
     { 
    	 int width= bluePaddle.getWidth();
    	 int height= bluePaddle.getHeight();
    	 for (ContactLayoutData contact : contactPositionMap.values()){
    		Rect r= new Rect(contact.positionOnScreen[0]- width/2, contact.positionOnScreen[1]-height, contact.positionOnScreen[0]+width/2, contact.positionOnScreen[1] );
    		if(r.contains(point[0], point[1])){    			
    		    ContactLayoutData cdata= contactPositionMap.get(contact.idContact); 
    		    cdata.isChecked=true;
    		}
    		   		
    	 }
    	 	
     }
     
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