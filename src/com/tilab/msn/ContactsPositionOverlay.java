package com.tilab.msn;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;
import com.google.android.maps.Overlay.PixelCalculator;

public class ContactsPositionOverlay extends Overlay {
	
	private Bitmap positionPin;
	private Activity myActivity;
	private Paint myPaint;
	
	public ContactsPositionOverlay(ContactsPositionActivity activity){
		myActivity = activity;
		myPaint = new Paint();
	}
	
	@Override
	public void draw(Canvas canvas, PixelCalculator calculator, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, calculator, shadow);
		
		Contact myCont = ContactManager.getInstance().getMyContact();
		Location loc = myCont.getLocation();
		
		//get the x and y of the point on the map
		int[] point = new int[2];
		
		int latitudeE6 = (int)(loc.getLatitude() * 1E6);
		int longitudeE6 = (int) (loc.getLongitude() * 1E6);
		
		myPaint.setARGB(100,255, 0, 0);
		
		//convert position into on screen pixel position
		calculator.getPointXY(new Point(latitudeE6,longitudeE6), point);
		
		canvas.drawCircle(point[0], point[1], 5, myPaint);
	}

}
