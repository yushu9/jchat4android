package it.telecomitalia.mock.gps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class LocationProviderLauncher extends Activity {

  private static String LOCATION_UPDATE_TAG = "LOCATION UPDATE";
  private Spinner trackSpinner;
  private File trackDir;
  private Button startBtn;
  private Button stopBtn;
  private EditText delayEdt;

  
//Key for retrieving file path from the intent
  public static final String TRACK_FILE_PATH="TRACK_FILE_PATH";
//Key for retrieving delay (speed) between locations in ms
  public static final String LOCATION_DELAY_MS="LOCATION_DELAY_MS";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.empty);
    
    trackDir = new File(getText(R.string.default_tracks_dir).toString());
    
    LocationManager lm = (LocationManager)this.getSystemService(LOCATION_SERVICE);
    
    
    /*
     * Check to see if our provider is already registered, it is, don't register
     * it again
     */
    boolean testProviderFound = false;
    Log.v(LocationProviderLauncher.class.getName(), "Checking list of providers..."  );
    for (String s : lm.getProviders(false)) {
      if (MockLocationService.PROVIDER_ID.equals(s)) {
        testProviderFound = true;
        Log.v(LocationProviderLauncher.class.getName(), "Provider " + MockLocationService.PROVIDER_ID + " is already registered... skipping registration");
      }
    }

    if (!testProviderFound) {
      Log.v(LocationProviderLauncher.class.getName(), "Registering provider " + MockLocationService.PROVIDER_ID );
      lm.addTestProvider(MockLocationService.PROVIDER_ID, false, false, false, false, false, false, false, 0, 1);
      lm.setTestProviderEnabled(MockLocationService.PROVIDER_ID, true);
      lm.updateProviders();
    }
    
    TextView providerName = (TextView)this.findViewById(R.id.providername_textview);
    providerName.setText("This location provider will be used: " + MockLocationService.PROVIDER_ID);
 
    //Track spinner setup
    List<String> tracks = getTrackList();
    trackSpinner = (Spinner) findViewById(R.id.track_spinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    
    for (String track : tracks){
    	adapter.add(track);
    }
    trackSpinner.setAdapter(adapter);
    
    //EditText setup
    delayEdt = (EditText) findViewById(R.id.delay_edittext);
    
    //Start button setup
    startBtn = (Button) findViewById(R.id.start_service_btn);
    startBtn.setOnClickListener( new View.OnClickListener(){

		public void onClick(View v) {
			// TODO Auto-generated method stub
			String selectedTrack = (String) trackSpinner.getSelectedItem();
			Intent mockProvider = new Intent(LocationProviderLauncher.this, MockLocationService.class);
			mockProvider.putExtra(LocationProviderLauncher.TRACK_FILE_PATH,trackDir.getAbsolutePath() + File.separator + selectedTrack);
			Editable delayStr = delayEdt.getText();
			if (delayStr != null && delayStr.length() > 0){
				try{
					double delay = Double.parseDouble(delayStr.toString());
					mockProvider.putExtra(LocationProviderLauncher.LOCATION_DELAY_MS, delay );
					startBtn.setEnabled(false);
					startService(mockProvider);
					stopBtn.setEnabled(true);
				} catch(NumberFormatException ex) {
					Toast.makeText(LocationProviderLauncher.this, "Format of delay is not valid!",2000).show();
				}
			} else {
				Toast.makeText(LocationProviderLauncher.this, "Please, choose a value for delay!",2000).show();
			}
			
		}
    	
    });   
    
    

    //Stop button setup
    stopBtn = (Button) findViewById(R.id.stop_service_btn);
    stopBtn.setEnabled(false);
    stopBtn.setOnClickListener( new View.OnClickListener(){

		public void onClick(View v) {
			Intent mockProvider = new Intent(LocationProviderLauncher.this, MockLocationService.class);
			stopBtn.setEnabled(false);
			stopService(mockProvider);
			startBtn.setEnabled(true);
		}
    });   

    
  }
 
  /**
   * Destroy the service on close
   */
  protected void onDestroy() {
		super.onDestroy();
		Intent mockProvider = new Intent(LocationProviderLauncher.this, MockLocationService.class);
		stopService(mockProvider);	
  }
  
  private List<String> getTrackList(){
	  List<String> trackList = new ArrayList<String>();
	  String[] files = trackDir.list();

	  if (files != null){
	  
		  for (int i=0; i < files.length; i++){
			  File file = new File(trackDir.getAbsolutePath() + File.separator + files[i] );
			  if (file.isFile()){
				  trackList.add(file.getName());
			  }
		  }
	  } else {
		Log.w(LocationProviderLauncher.class.getName(), "No track files found!");  
	  }
	  
	  return trackList;
  }
  
 
}
