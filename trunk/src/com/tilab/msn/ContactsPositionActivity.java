package com.tilab.msn;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.ArrayAdapter;
import android.widget.ZoomDialog;
import android.widget.ZoomSlider;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

public class ContactsPositionActivity extends MapActivity {

	private MapView mapView;
	private MapController mapController;
	private OverlayController overlayCtrl;
	private ZoomDialog zoomDialog;
	
	private final int MENUITEM_ZOOM= Menu.FIRST;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		TilabMsnApplication app = (TilabMsnApplication) getApplication();
		Log.d("Application is instance of ", app.getClass().getName() );
		app.myBehaviour.setContactsUpdater(new MapUpdater(this));
		
		mapView = new MapView(this);
		overlayCtrl = mapView.createOverlayController();
		overlayCtrl.add(new ContactsPositionOverlay(this), true);
		mapController = mapView.getController();
		
		Location loc = ContactManager.getInstance().getMyContact().getLocation();
		int latitudeE6 = (int)(loc.getLatitude() * 1E6);
		int longitudeE6 = (int)(loc.getLongitude() * 1E6);
		setContentView(mapView);
		mapController.animateTo(new Point(latitudeE6,longitudeE6));
	
		zoomDialog = new ZoomDialog(this);
		ZoomSlider slider = new ZoomSlider(this);
		slider.setParams(0, 
						21, 
						10, 
						new ZoomSlider.OnZoomChangedListener(){
						 	
							public void onZoomOut(ZoomSlider zoomSlider,
									int oldZoom, int newZoom) {
								// TODO Auto-generated method stub
								ContactsPositionActivity.this.mapController.zoomTo(newZoom);
							}
							
						 	public void onZoomIn(ZoomSlider zoomSlider,
						 			int oldZoom, int newZoom) {
						 		// TODO Auto-generated method stub
						 		ContactsPositionActivity.this.mapController.zoomTo(newZoom);
						 	}
						 	
						 	public void onZoomChanged(ZoomSlider zoomSlider,
						 			int oldZoom, int newZoom) {
						 		// TODO Auto-generated method stub
						 		ContactsPositionActivity.this.mapController.zoomTo(newZoom);
						 	}
						 	
						 	@Override
						 	public void onZoomCompleted() {
						 		// TODO Auto-generated method stub
						 
						 	}
						 	
						 	
						}, 
						true);
	
		DisplayMetrics metrics = new DisplayMetrics();
		slider.bringToFront();
		zoomDialog.setContentView(slider);
		zoomDialog.setPosition(mapView, 0, 0);
		zoomDialog.show();
		
		
	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENUITEM_ZOOM, R.string.menuitem_zoom);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		
		switch(item.getId()) {
			case MENUITEM_ZOOM:
				zoomDialog.show();
			break;
			
		}
		return true;
	}

	public void refreshMap(){
		mapView.invalidate();
	}
	
	private class MapUpdater extends ContactsUIUpdater{

		public MapUpdater(ContactsPositionActivity act) {
			super(act);
			// TODO Auto-generated constructor stub
		}

		protected void handleUpdate() {
			// TODO Auto-generated method stub
			ContactsPositionActivity posAct = (ContactsPositionActivity) activity; 
			posAct.refreshMap();
		}
	}
}
