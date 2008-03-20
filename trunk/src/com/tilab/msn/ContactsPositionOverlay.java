package com.tilab.msn;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.location.Location;
import android.pim.ContactPickerActivity.MyContentObserver;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Point;

public class ContactsPositionOverlay extends Overlay {
	
	private MapController mapController; 
	private Paint myPaint;
	
	private Resources appRes;
	
	//HotAreaWidth/ScreenWidth
	private final float HOT_AREA_WIDTH_RATIO = 0.90f;
	private final float HOT_AREA_HEIGHT_RATIO= 0.90f;
	private final int CONTACT_LOC_POINT_RADIUS = 5;
	
	public ContactsPositionOverlay(MapController controller, Resources ctn){
		mapController = controller;
		appRes= ctn;
		myPaint = new Paint();
		Contact myContact = ContactManager.getInstance().getMyContact();
		List<Contact> contactsList = ContactManager.getInstance().getOtherContactList();
		refreshViewport(contactsList, myContact);
	}
	
	
	
	private void drawHotArea(Canvas canvas, Rect hotArea){
		
		Paint p = new Paint();
		p.setARGB(60, 0, 200, 0);
		p.setStyle(Style.FILL);
		canvas.drawRect(hotArea, p);
	}
	
	private boolean refreshViewportNeeded(List<ContactLayoutData> pointList, Rect hotArea){
		
		int size = pointList.size();
		
		for (int i = 0; i < size; i++) {
			ContactLayoutData cLayout = pointList.get(i);
			
			if (!hotArea.contains(cLayout.positionOnScreen[0],cLayout.positionOnScreen[1])){
				return true;
			}
		}
		
		return false;
	}
	
	private List<ContactLayoutData> extractLayoutData(Contact myC, List<Contact> contactList, PixelCalculator calc ){
		List<ContactLayoutData> layoutDataList = new ArrayList<ContactLayoutData>();
		
		int size = contactList.size();
		
		ContactLayoutData myData = new ContactLayoutData(myC.getName(), myC.getLocation(), calc);
		myData.isMyContact = true;
		layoutDataList.add(myData);
		
		for (int i=0; i < size; i++){
			Contact c = contactList.get(i);
			if (c.isOnline()){
				ContactLayoutData data = new ContactLayoutData(c.getName(), c.getLocation(),calc);
				layoutDataList.add(data);
			}
		}
		
		
		return layoutDataList;
	}
	
	private void drawOnlineContacts(Canvas c, Paint p, List<ContactLayoutData> layoutDataList){
		
		int size = layoutDataList.size();
		int oldColor = p.getColor();
		FontMetrics fm = p.getFontMetrics();
		int offset =(int) fm.bottom;
		
		for (int i=0; i < size; i++){
			ContactLayoutData cData = layoutDataList.get(i);
			
			if (cData.isMyContact){
				p.setColor(appRes.getColor(R.color.my_contact_map_color));
			} else {
				p.setColor(appRes.getColor(R.color.other_contact_map_color));
			}
			
			c.drawCircle(cData.positionOnScreen[0], cData.positionOnScreen[1], CONTACT_LOC_POINT_RADIUS, p);
			c.drawText(cData.name, cData.positionOnScreen[0], cData.positionOnScreen[1]-offset, p);
		}
		
		p.setColor(oldColor);
	}
	
	
	
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, calculator, shadow);
		
		float WIDTH = calculator.getMapWidth();
		float HEIGHT = calculator.getMapHeight();
		
		float hotAreaWidth =  ( WIDTH* HOT_AREA_WIDTH_RATIO);
		float hotAreaHeight =  (HEIGHT * HOT_AREA_HEIGHT_RATIO);
		
		
		Rect hotArea = new Rect();
		hotArea.left = (int)((WIDTH - hotAreaWidth)/2.0f);
		hotArea.right = hotArea.left + (int) hotAreaWidth;
		hotArea.bottom = (int)((HEIGHT - hotAreaHeight)/2.0f);
		hotArea.top = hotArea.bottom + (int)hotAreaHeight;
		
		//GET the current contacts
		Contact myCont = ContactManager.getInstance().getMyContact();
		List<Contact> contacts = ContactManager.getInstance().getOtherContactList();
		
		//Get the pixel positions
		List<ContactLayoutData> positionsData = extractLayoutData(myCont, contacts, calculator);
		
		//if any pixel is out our hot area
		if (refreshViewportNeeded(positionsData, hotArea)){
			//change map center and zoom level
			refreshViewport(contacts, myCont);
			Log.w("CIAO", "Out of hot area");
		}
		
		//Draw hot area (for debugging)
		drawHotArea(canvas, hotArea);
		
		//Draw all the contacts
		drawOnlineContacts(canvas, myPaint, positionsData);
	}
	
	//This methods sets the zoom level and center of map depending on the point we need
	//to draw
	private void refreshViewport(List<Contact> contactsList, Contact myContact){
		//compute the midpont (this will be our center)
		Location center = getMidpoint(contactsList, myContact);
		
		int latitudeE6 = (int)(center.getLatitude() * 1E6);
		int longitudeE6 = (int) (center.getLongitude() * 1E6);
		
		Point p = new Point(latitudeE6, longitudeE6);
		
		//Now we compute the max span for lat and long to set zoom level
		int[] maxSpan = getMaxSpan(center, myContact.getLocation(), contactsList);
		
		mapController.zoomToSpan(maxSpan[0],maxSpan[1]);
		mapController.animateTo(p);
		
	}
	
	private Location getMidpoint(List<Contact> cList, Contact myContact){
		Location middle = new Location();
		double latitude=0.0d;
		double longitude=0.0d;
		//my contact is online for sure
		double onlineContacts=1.0d;
		Location myLocation = myContact.getLocation(); 
		
		for (Contact c : cList){
			if (c.isOnline()){
				onlineContacts++;
				latitude += c.getLocation().getLatitude();
				longitude += c.getLocation().getLongitude();
			
			}
		}
		
		latitude += myLocation.getLatitude();
		longitude += myLocation.getLongitude();
		
		middle.setLongitude(longitude / onlineContacts);
		middle.setLatitude(latitude / onlineContacts);
		
		return middle;
	}
	
	private int[] getMaxSpan(Location baric, Location myLoc, List<Contact> cList){
		
		 int[] maxSpan = new int[2];
		
		 double maxSpanLat =0.0d;
		 double maxSpanLong=0.0d; 
		 
		for (Contact c : cList){
			
			if (c.isOnline()){
				double spanLat = Math.abs(baric.getLatitude() - c.getLocation().getLatitude());
				double spanLong = Math.abs(baric.getLongitude() - c.getLocation().getLongitude());
				
				if (spanLat > maxSpanLat){
					maxSpanLat = spanLat;
				}
				
				if (spanLong > maxSpanLong){
					maxSpanLong = spanLong;
				}
			}
		}
		
		double mySpanLat = Math.abs(baric.getLatitude() - myLoc.getLatitude());
		double mySpanLong = Math.abs(baric.getLongitude() - myLoc.getLongitude()); 
		
		if (mySpanLat > maxSpanLat)
			maxSpanLat = mySpanLat;
		
		if (mySpanLong > maxSpanLong)
			maxSpanLong = mySpanLong;
		
		maxSpan[0] = (int)(2*maxSpanLat*1E6);
		maxSpan[1] = (int)(2*maxSpanLong*1E6);
		
		return maxSpan;
	}
	

	//This class represents the data of the contact to be displayed (position, name, color)
	private class ContactLayoutData{
	
		public int[] positionOnScreen;
		public String name;
		public boolean isMyContact;
		
		public ContactLayoutData(String cname, Location contactLoc, PixelCalculator pixelCalc){
			this.name = cname;
			isMyContact = false;
			positionOnScreen = new int[2];
			int latitudeE6 = (int)(contactLoc.getLatitude() * 1E6);
			int longitudeE6 = (int) (contactLoc.getLongitude() * 1E6);
			pixelCalc.getPointXY(new Point(latitudeE6,longitudeE6), positionOnScreen);
		}	
	}		
}
