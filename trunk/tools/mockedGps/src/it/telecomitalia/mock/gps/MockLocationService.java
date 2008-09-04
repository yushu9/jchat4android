package it.telecomitalia.mock.gps;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;

import android.util.Log;
import android.widget.Toast;

public class MockLocationService extends Service {
  
  public final static String PROVIDER_ID = "gps";
  private MapTrack trackToBeExecuted;
  private NotificationManager manager;
  private PositionProvider updateThread = null;
  private final int SERVICE_NOTIFICATION_ID=7;
  private long locationDelay;
  
  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    LocationManager lm = (LocationManager)this.getSystemService(LOCATION_SERVICE);
    
    // retrieve the track name
    String trackPath = intent.getStringExtra(LocationProviderLauncher.TRACK_FILE_PATH);
    
    //get the delay
    locationDelay = intent.getLongExtra(LocationProviderLauncher.LOCATION_DELAY_MS, 2000);
    
    // okay, now we're ready to start sending updates, make sure we're enabled
    lm.setTestProviderEnabled(PROVIDER_ID, true);
    
    
    
	
	try {
			trackToBeExecuted = new MapTrack(trackPath);
			updateThread = new PositionProvider(lm);
			Log.i(MockLocationService.class.getName(), "Starting position sender thread.");
			updateThread.start();
			Notification serviceActiveNotif = new Notification(R.drawable.map,"Mocked Location Provider service is running!",System.currentTimeMillis());
			PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_DEFAULT), PendingIntent.FLAG_ONE_SHOT);
			serviceActiveNotif.setLatestEventInfo(this, "Mocked Location Provider service is running", "", pi);
			manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);	
			manager.notify(SERVICE_NOTIFICATION_ID, serviceActiveNotif);
	} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(MockLocationService.class.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
			
			Toast.makeText(this, "Errors while reading track data! Service shall stop...", 3000).show();
			stopSelf();
	} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(MockLocationService.class.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
			
			Toast.makeText(this, "Errors while reading track data! Service shall stop...", 3000).show();
			stopSelf();
	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(MockLocationService.class.getName(), e.toString() + "\n" + e.getStackTrace()[0].toString());
			
			Toast.makeText(this, "Errors while reading track data! Service shall stop...", 3000).show();
			stopSelf();
	}
    
  }
  
  @Override
  public void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    Log.i(MockLocationService.class.getName(), "Location provider service being killed");
    
    // allow the provider time to shut down
    if (updateThread != null ) {
      updateThread.stop = true;
	    try {
			updateThread.join(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    manager.cancel(SERVICE_NOTIFICATION_ID);
  }


  @Override
  public boolean onUnbind(Intent intent) {
    // TODO Auto-generated method stub
    return super.onUnbind(intent);
  }


  private class PositionProvider extends Thread {
    public volatile boolean stop = false;
    public volatile boolean isStopped = true;
    private LocationManager locationService = null;
    private boolean forward;
    
    public PositionProvider (LocationManager lm) {
      super();
      forward = true;
      locationService = lm;
    }
    
    public void run () {
      isStopped = false;
      Location l = new Location(PROVIDER_ID);
      
      ListIterator<TrackLocationData> locationIterator = trackToBeExecuted.iterator();
      
      // loop around our location dataset until we're told to stop
      while (!stop) {
    	  TrackLocationData data = null;
    	  
    	  if (forward) {
    		  data = locationIterator.next();
    		  forward = locationIterator.hasNext();
    	  } else {
    		  data = locationIterator.previous();
    		  forward = !locationIterator.hasPrevious();
    	  }
    	  
    	  //fill location data
    	  l.setLatitude(data.latitude);
    	  l.setLongitude(data.longitude);
    	  l.setTime(System.currentTimeMillis());
    	  
    	  locationService.setTestProviderLocation(PROVIDER_ID, l);
    	  try {
			Thread.sleep(locationDelay);
    	  } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	  }
      }
      isStopped = true;
    }
  }
}
