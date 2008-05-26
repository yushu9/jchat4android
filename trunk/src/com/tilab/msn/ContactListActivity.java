package com.tilab.msn;



import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.net.ConnectException;

import java.util.List;

import java.util.Random;

import android.app.Activity;
import android.app.ActivityPendingResult;
import android.content.Context;
import android.content.Intent;
import android.content.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Debug;
import android.os.IServiceManager;
import android.os.ServiceManagerNative;
import android.os.SystemProperties;
import android.telephony.IPhone;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewInflate;
import android.view.Menu.Item;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.ContextMenuInfo;
import android.widget.TabHost.TabSpec;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;



public class ContactListActivity extends MapActivity implements ConnectionListener {
   
	private JadeGateway gateway;
	private static final Logger myLogger = Logger.getMyLogger(ContactListActivity.class.getName());
	private TabHost mainTabHost;
	private MultiSelectionListView contactsListView;
	private OverlayController overlayCtrl;
	private MapView mapView;
	private static String numTel;
	
	
	//MENUITEM CONSTANT
	private final int MENUITEM_ID_EXIT=Menu.FIRST;
	
	//Menu entries
	private final int CONTEXT_MENU_ITEM_CHAT = Menu.FIRST+1;
	private final int CONTEXT_MENU_ITEM_CALL = Menu.FIRST+2;
	private final int CONTEXT_MENU_ITEM_SMS = Menu.FIRST+3;
	
	//NEEDED TAGS FOR THE TABHOST (to address them)
	private final String CONTACTS_TAB_TAG="ContTab";
	private final String MAPVIEW_TAB_TAG="MapViewTab";
	
	//Return codes 
	public static final int CHAT_ACTIVITY_CLOSED = 777;
	
	
	//Array of updaters
	private  ContactListActivityUpdater activityUpdater;
	
	public static final String OTHER_PARTICIPANTS = "com.tilab.msn.Prova";
	public static final String MESSAGE = "com.tilab.msn.Message";
	public static final String ID_ACTIVITY_PENDING_RESULT = "ID_ACTIVITY_PENDING_RESULT";
	
	private void initUI(){
		//Setup the main tabhost
        setContentView(R.layout.homepage);
        mainTabHost = (TabHost) findViewById(R.id.main_tabhost);
        mainTabHost.setup();
      /*  mainTabHost.setOnTabChangedListener( new TabHost.OnTabChangeListener(){

			@Override
			public void onTabChanged(String arg0) {
				// TODO Auto-generated method stub
				int[] colors= new int[] {Color.MAGENTA, Color.RED};
				GradientDrawable gd1 = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
				gd1.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
				
				mainTabHost.add
			}
        	
        });*/
        
      //Fill the contacts tab
        TabSpec contactsTabSpecs = mainTabHost.newTabSpec(CONTACTS_TAB_TAG);
        TabSpec mapTabSpecs = mainTabHost.newTabSpec(MAPVIEW_TAB_TAG);
        View contactView; 
        View mapViewTab; 
		ViewInflate inflater;
		inflater = (ViewInflate)getSystemService(Context.INFLATE_SERVICE);			
		contactView = inflater.inflate(R.layout.contact_tab, null, null);
        contactsTabSpecs.setIndicator(contactView);
        mapViewTab = inflater.inflate(R.layout.maptab, null, null);
        mapTabSpecs.setIndicator(mapViewTab);
		contactsTabSpecs.setContent(R.id.content1);
		mapTabSpecs.setContent(R.id.content2);
		mainTabHost.addTab(contactsTabSpecs);
		mainTabHost.addTab(mapTabSpecs);
		Resources res= getResources();
		
		int[] colors= new int[] {res.getColor(R.color.white), res.getColor(R.color.dark_grey)};
		int midColor = Color.rgb((Color.red(colors[0]) + Color.red(colors[1]))/2,
							(Color.green(colors[0]) + Color.green(colors[1]))/2,
							(Color.blue(colors[0]) + Color.blue(colors[1]))/2);
		int[] colors1 = new int[] {res.getColor(R.color.white), res.getColor(R.color.blue)};
		int[] colors2 = new int[] {res.getColor(R.color.white), midColor};
		GradientDrawable gd1 = new GradientDrawable(Orientation.LEFT_RIGHT, colors2);
		gd1.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
		GradientDrawable gd2 = new GradientDrawable(Orientation.LEFT_RIGHT, colors1);
		gd2.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
		
		GradientDrawable gd3 = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		contactView.setBackground(gd1);
		GradientDrawable gd4 = new GradientDrawable(Orientation.LEFT_RIGHT, colors1);

		
		mapViewTab.setBackground(gd2);
		
        View homeTab = (View) findViewById(R.id.content1);
        homeTab.setBackground(gd3);
        View homeTab1 = (View) findViewById(R.id.content2);
        homeTab1.setBackground(gd4);
       
        		
		//init the map view
		mapView = (MapView) findViewById(R.id.myMapView);
		overlayCtrl = mapView.createOverlayController();
		overlayCtrl.add(new ContactsPositionOverlay(mapView,getResources(), this),true);
		
		//Button for switching map mode
		Button switchButton = (Button) findViewById(R.id.switchMapBtn);
		switchButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Button clickedBtn = (Button) arg0;
				
				mapView.toggleSatellite();
				mapView.getController().stopAnimation(true);
				
				if (mapView.isSatellite()){
					clickedBtn.setText(ContactListActivity.this.getText(R.string.label_toggle_map));
				} else {
					clickedBtn.setText(ContactListActivity.this.getText(R.string.label_toggle_satellite));
				}
			}
			
		});
		
		//Create the updater array
            
		//Select default tab
		mainTabHost.setCurrentTabByTag(CONTACTS_TAB_TAG);
		
		contactsListView = (MultiSelectionListView) findViewById(R.id.contactsList);
		int[] selectorColors= new int[] {res.getColor(R.color.light_green), res.getColor(R.color.dark_green)};
		GradientDrawable selectorDrawable = new GradientDrawable(Orientation.TL_BR, selectorColors);
		contactsListView.setSelector(selectorDrawable);
		
		//added ContextMenu
		contactsListView.setOnPopulateContextMenuListener(
				new View.OnPopulateContextMenuListener(){
					public void  onPopulateContextMenu(ContextMenu menu, View v, Object menuInfo) {
						MultiSelectionListView myLv = (MultiSelectionListView) v;
						ContextMenuInfo info = (ContextMenuInfo) menuInfo;
						myLv.setSelection(info.position);
						
						String selectedCId = (String)myLv.getSelectedItem();
						List<String>  checkedContacts =myLv.getAllSelectedItems();
						
						//If the selected item is also checked
						if (checkedContacts.contains(selectedCId)) {
							//Let the menu appear
							Contact selectedC = ContactManager.getInstance().getContact(selectedCId);
							if (selectedC.isOnline())
							menu.add(0, CONTEXT_MENU_ITEM_CHAT, R.string.menu_item_chat);
							menu.add(0, CONTEXT_MENU_ITEM_CALL, R.string.menu_item_call);
							menu.add(0, CONTEXT_MENU_ITEM_SMS, R.string.menu_item_sms);
						}
					}
				}
		);		
	    contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	    	public void  onItemClick(AdapterView parent, View v, int position, long id) {
	    		CheckBox cb= (CheckBox)v.findViewById(R.id.contact_check_box);	
	    		ContactListAdapter  adapter = (ContactListAdapter) parent.getAdapter();
	    		String selCId = (String) adapter.getItem(position);
	    		Contact selC = ContactManager.getInstance().getContact(selCId);
	    		if (selC.isOnline())
	    			cb.setChecked(!cb.isChecked());
	    	}
	    });
		

		
		ContactManager.getInstance().readPhoneContacts(this);
		contactsListView.setAdapter(ContactManager.getInstance().getAdapter());
		initializeContactList();
	}		
    
	
	private static String getRandomNumber(){
		Random rnd = new Random();
		int randInt  = rnd.nextInt();
		return "RND" + String.valueOf(randInt);
	}
	
    public void onCreate(Bundle icicle) {
    	Debug.startMethodTracing("/tmp/profile");
    	Thread.currentThread().getId();
        myLogger.log(Logger.INFO, "onReceiveIntent called: My currentThread has this ID: " + Thread.currentThread().getId());
        super.onCreate(icicle);
        ContactListAdapter cla = new ContactListAdapter(this);
        ContactManager.getInstance().addAdapter(cla);
        MsnSessionManager.getInstance().initialize(this);
        
      //fill Jade connection properties
        Properties jadeProperties = getJadeProperties(this);
      //Add my contact
        ContactManager.getInstance().addMyContact(jadeProperties.getProperty(JICPProtocol.MSISDN_KEY));
        
        //start updating myContact
        GeoNavigator.setLocationProviderName(getText(R.string.location_provider_name).toString());
        GeoNavigator.getInstance(this).initialize();
        GeoNavigator.getInstance(this).startLocationUpdate();
     
        activityUpdater = new ContactListActivityUpdater(this);
        
        //Initialize the UI
        initUI();
        
         
        //try to get a JadeGateway
        try {
			JadeGateway.connect(MsnAgent.class.getName(), new String[]{getText(R.string.contacts_update_time).toString()}, jadeProperties, this, this);
		} catch (Exception e) {
			//troubles during connection
			Toast.makeText(this, 
						   getString(R.string.error_msg_jadegw_connection), 
						   Integer.parseInt(getString(R.string.toast_duration))
						   ).show();
			myLogger.log(Logger.SEVERE, "Error in onCreate",e);
			e.printStackTrace();
		}
		
		
    }

    private static Properties jadeProperties;
    
	public static Properties getJadeProperties(Activity act){
		 //fill Jade connection properties
        jadeProperties = new Properties(); 
        jadeProperties.setProperty(Profile.MAIN_HOST, act.getString(R.string.jade_platform_host));
        jadeProperties.setProperty(Profile.MAIN_PORT, act.getString(R.string.jade_platform_port));
        //Get the phone number of my contact
        numTel = SystemProperties.get("numtel");
		
		//if number is not available
		if (numTel.equals("")){
			myLogger.log(Logger.WARNING, "Cannot access the numtel! A random number shall be used!!!");
			numTel = getRandomNumber();
		}
       
        jadeProperties.setProperty(JICPProtocol.MSISDN_KEY, numTel);
        return jadeProperties;
	}
		
	protected void onDestroy() {
		
		GeoNavigator.getInstance(this).stopLocationUpdate();
		
		myLogger.log(Logger.INFO, "onDestroy called ...");
		GeoNavigator.getInstance(this).shutdown();
		
		ChatSessionNotificationManager notifUpd = MsnSessionManager.getInstance().getNotificationManager();
		
		if (notifUpd != null)	
			notifUpd.removeAllNotifications();
		
		if (gateway != null) {
			try {
				gateway.shutdownJADE();
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gateway.disconnect(this);
		}
		
		MsnSessionManager.getInstance().shutdown();
		ContactManager.getInstance().shutdown();
		Debug.stopMethodTracing();
		super.onDestroy();
	}
	

	protected void onStop() {
		myLogger.log(Logger.INFO, "onStop called ...");
		super.onStop();
	}


	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
	
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
		
		try {
			gateway.execute(activityUpdater);
			//put my contact online			
		} catch(Exception e){
			Toast.makeText(this, e.toString(), 1000).show();
			myLogger.log(Logger.SEVERE, "Exception in onConnected",e);
			e.printStackTrace();
		}
	}


	protected void onResume() {
		myLogger.log(Logger.INFO, "onResume called...");	
		super.onResume();
	}

	
	
	protected void onPause() {
		myLogger.log(Logger.INFO, "onPause called...");
		
		super.onPause();	}

	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	

	protected void onFreeze(Bundle outState) {
		// TODO Auto-generated method stub
		myLogger.log(Logger.INFO, "onFreeze called...");
		super.onFreeze(outState);
	}




	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENUITEM_ID_EXIT, R.string.menuitem_exit);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		
		switch(item.getId()) {
			case MENUITEM_ID_EXIT:
				finish();
			break;			
		}
		return true;
	}

	public boolean onContextItemSelected(Item item) {
		
		switch(item.getId()) {
			case CONTEXT_MENU_ITEM_CALL:
				IPhone phoneService = null; 
		          try { 
		               IServiceManager sm = ServiceManagerNative.getDefault(); 
		               phoneService = IPhone.Stub.asInterface(sm.getService("phone")); 
		          } catch (Exception e) { 
		          } 
		          
		           
		          try { 
		        	  Contact selectedC = (Contact) contactsListView.getSelectedItem();
		              phoneService.call(selectedC.getPhoneNumber());
		          } catch (Exception e) { 
		          }
		
				break;
			case CONTEXT_MENU_ITEM_CHAT:
				List<String> participantIds = contactsListView.getAllSelectedItems();
				//start a new session or retrieve it If the session already exists. its Id is retrieved
				String sessionId = MsnSessionManager.getInstance().startMsnSession(participantIds);
				
				//retrieve a copy of the session
				MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
				//Add a notification for the new session
				MsnSessionManager.getInstance().getNotificationManager().addNewSessionNotification(sessionId);
							
				//Add to the intent a mean to return a result back to the start activity
				ActivityPendingResult activityResult = createActivityPendingResult(CHAT_ACTIVITY_CLOSED, false);
				
				//packet an intent. We'll try to add the session ID in the intent data in URI form
				//We use intent resolution here, cause the ChatActivity should be selected cause matching ACTION and CATEGORY
				Intent it = new Intent(Intent.VIEW_ACTION);
				//set the data as an URI (content://sessionId#<sessionIdValue>)
				it.setData(session.getSessionIdAsUri());
				it.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
				it.addCategory(Intent.DEFAULT_CATEGORY);
				it.putExtra(ID_ACTIVITY_PENDING_RESULT, activityResult);
				startActivity(it);
				
				break;
			case CONTEXT_MENU_ITEM_SMS:
				Toast.makeText(this, R.string.missing_feature_sms, 3000).show();
				break;
			default:
		}
		return false;
	}
	
	private void updateListAdapter(ContactListChanges changes){
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		//FIXME: if this works we should try to use the DataSetObserver pattern 
		adapter.update(changes);		
	}
	
	private void initializeContactList(){
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		adapter.initialize();
		contactsListView.setAdapter(adapter);
	}
	
		
	@Override
	protected void onStart() {		
		super.onStart();
		myLogger.log(Logger.INFO, "OnStart called: This activity has Task ID: " + getTaskId());
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		// TODO Auto-generated method stub
		myLogger.log(Logger.INFO, "onActivityResult() was called! ChatActivity should have been closed!!");
		
		switch (requestCode){
			case CHAT_ACTIVITY_CLOSED:
				this.contactsListView.uncheckAllSelectedItems();
			break;
		}
	}	
	
	
	private class ContactListActivityUpdater extends ContactsUIUpdater{

		public ContactListActivityUpdater(Activity act) {
			super(act);
		}			
			
		protected void handleUpdate(Object parameter) {
				
				boolean anyChanges = false;
				
				if (parameter instanceof ContactListChanges){		
					ContactListChanges changes = (ContactListChanges) parameter;
					anyChanges = true;
					updateListAdapter(changes);	
				}
				
				//refresh the screen: if the map is visible refresh it
				//It seems that using the tab tag does not work
			//	if (mainTabHost.getCurrentTab() > 0){
					//if any contact has moved
					if (ContactManager.getInstance().movingContacts()){
						//redraw the map
						mapView.invalidate();
					}
			//	} else {
					if (anyChanges || ContactManager.getInstance().movingContacts()){
						// if here the contact list is visible
						int selPos = contactsListView.getSelectedItemPosition();
						ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
						contactsListView.setAdapter(adapter);
						contactsListView.setSelection(selPos);
					}
			//	}
		}		
		
	}
}
