package com.tilab.msn;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;

public class ContactsPositionOverlay extends Overlay {
	
	
	public ContactsPositionOverlay(){

	}
	
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, calculator, shadow);
		
		Contact myCont = ContactManager.getInstance().getMyContact();
		Location loc = myCont.getLocation();
		List<Contact> contacts = ContactManager.getInstance().getOtherContactList();
		
		//get the x and y of the point on the map
		int[] point = locationToPoint(calculator, loc);
		
		Paint myPaint = new Paint();
		myPaint.setARGB(100,255, 0, 0);
		
		
		canvas.drawCircle(point[0], point[1], 5, myPaint);
		canvas.drawText(myCont.getName(), point[0], point[1]-7, myPaint);
		
		drawOtherContacts(canvas, calculator, contacts);
	}
	
	private int[] locationToPoint(PixelCalculator pixelCalc, Location loc){
		int[] point = new int[2];
		int latitudeE6 = (int)(loc.getLatitude() * 1E6);
		int longitudeE6 = (int) (loc.getLongitude() * 1E6);
		pixelCalc.getPointXY(new Point(latitudeE6,longitudeE6), point);
		return point;
	}	
		
	private void drawOtherContacts(Canvas canvas , PixelCalculator calculator, List<Contact> otherContactList){
		
	
		Paint myPaint = new Paint();
		myPaint.setARGB(100,0, 255, 0);
		
		for (Contact c : otherContactList){
			if (c.isOnline()){
				Location loc = c.getLocation();	
				//get the x and y of the point on the map
				int [] point = locationToPoint(calculator, loc);
				canvas.drawCircle(point[0], point[1], 5, myPaint);
				canvas.drawText(c.getName(), point[0], point[1]-7, myPaint);
			}
		}
	}

}
