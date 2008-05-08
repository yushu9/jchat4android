package com.tilab.msn;



import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Resources;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
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
	private final String CONTACTS_TAB_TAG="ContactsTab";
	private final String MAPVIEW_TAB_TAG="MapViewTab";
	
	
	//Array of updaters
	private Map<String, ContactsUIUpdater> updaters;
	
	public static final String OTHER_PARTICIPANTS = "com.tilab.msn.Prova";
	public static final String MESSAGE = "com.tilab.msn.Message";
	
	private void initUI(){
		//Setup the main tabhost
        setContentView(R.layout.homepage);
        mainTabHost = (TabHost) findViewById(R.id.main_tabhost);
        mainTabHost.setup();
    
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
		int[] colors1 = new int[] {res.getColor(R.color.white), res.getColor(R.color.blue)};
		GradientDrawable gd1 = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		GradientDrawable gd2 = new GradientDrawable(Orientation.LEFT_RIGHT, colors1);
		contactView.setBackground(gd1);
		mapViewTab.setBackground(gd2);
		
        
        View homeTab = (View) findViewById(R.id.content1);
        homeTab.setBackground(gd1);
        View homeTab1 = (View) findViewById(R.id.content2);
        homeTab1.setBackground(gd2);
        		
		//init the map view
		mapView = (MapView) findViewById(R.id.myMapView);
		overlayCtrl = mapView.createOverlayController();
		overlayCtrl.add(new ContactsPositionOverlay(mapView,getResources()),true);
		
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
        updaters = new HashMap<String, ContactsUIUpdater>(2);
        updaters.put(CONTACTS_TAB_TAG, new ContactListUpdater(this)); 
        updaters.put(MAPVIEW_TAB_TAG, new MapUpdater(this));
	        
		//Select default tab
		mainTabHost.setCurrentTabByTag(CONTACTS_TAB_TAG);
		
		
	     //Set the handler for the click on the tab host
		mainTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

				
				public void onTabChanged(String arg0) {
					
					myLogger.log(Logger.FINER, "Tab was switched! Current tab is "+ arg0 + " Changing the updater...");
					
					//FIXME: must understand what's wrong with this! Why do I receive null when the tab is clicked???
			            if (arg0 == null){
						try{
							gateway.execute(updaters.get(CONTACTS_TAB_TAG));
						}catch(Exception e){
							myLogger.log(Logger.SEVERE, e.getMessage());
						}
						
						contactsListView.setAdapter(ContactManager.getInstance().getAdapter());
						
					} else {
						try{
							gateway.execute(updaters.get(MAPVIEW_TAB_TAG));
						}catch(Exception e){
							myLogger.log(Logger.SEVERE, e.getMessage());
						}
					}
				}        	
	});

		contactsListView = (MultiSelectionListView) findViewById(R.id.contactsList);
		//added ContextMenu
		contactsListView.setOnPopulateContextMenuListener(
				new View.OnPopulateContextMenuListener(){
					public void  onPopulateContextMenu(ContextMenu menu, View v, Object menuInfo) {
						MultiSelectionListView myLv = (MultiSelectionListView) v;
						ContextMenuInfo info = (ContextMenuInfo) menuInfo;
						myLv.setSelection(info.position);
						
						Contact selectedC = (Contact)myLv.getSelectedItem();
						List<Contact>  checkedContacts =myLv.getAllSelectedItems();
						
						//If the selected item is also checked
						if (checkedContacts.contains(selectedC)) {
							//Let the menu appear
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
	    		Contact selC = (Contact) adapter.getItem(position);
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
    	Thread.currentThread().getId();
        myLogger.log(Logger.INFO, "onReceiveIntent called: My currentThread has this ID: " + Thread.currentThread().getId());
        super.onCreate(icicle);
        ContactListAdapter cla = new ContactListAdapter(this);
        ContactManager.getInstance().addAdapter(cla);
        
        //Initialize the UI
        initUI();
           
        //fill Jade connection properties
        Properties jadeProperties = getJadeProperties(this);
        
        GeoNavigator.setLocationProviderName(getText(R.string.location_provider_name).toString());
        GeoNavigator.getInstance(this).initialize();
        
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
		
		GeoNavigator.getInstance(this).startLocationUpdate();
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
		
		IncomingNotificationUpdater notifUpd = MsnSessionManager.getInstance().getNotificationUpdater();
		
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
		super.onDestroy();
	}
	

	protected void onStop() {
		myLogger.log(Logger.INFO, "onStop called ...");
		super.onStop();
	}


	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
	
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
		MsnSessionManager.getInstance().registerNotificationUpdater(new IncomingNotificationUpdater(this));
		
		try {
			gateway.execute(updaters.get(CONTACTS_TAB_TAG));
			//put my contact online			
			ContactManager.getInstance().getMyContact().
			setAgentContact(numTel);
		
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
				List<Contact> participantList = contactsListView.getAllSelectedItems();
				
				MsnSession session = MsnSessionManager.getInstance().getSessionByParticipantList(participantList);
				
				//If no session available, we must add a notification
				if (session == null){
					//creates a new session filling it with participants (or retrieve the existing one)
					session = MsnSessionManager.getInstance().createNewMsnSession(participantList);
					MsnSessionManager.getInstance().getNotificationUpdater().createSessionNotification(session.getSessionId());
				}			
				
				//packet an intent. We'll try to add the session ID in the intent data in URI form
				//We use intent resolution here, cause the ChatActivity should be selected cause matching ACTION and CATEGORY
				Intent it = new Intent(Intent.VIEW_ACTION);
				//set the data as an URI (content://sessionId#<sessionIdValue>)
				it.setData(session.getSessionIdAsUri());
				it.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
				it.addCategory(Intent.DEFAULT_CATEGORY);
		
				startActivity(it);
				
				break;
			case CONTEXT_MENU_ITEM_SMS:
				break;
			default:
		}
		return false;
	}
	
	private void refreshContactList(ContactListChanges changes){
		
		int selectedPos = contactsListView.getSelectedItemPosition();

		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		//FIXME: if this works we should try to use the DataSetObserver pattern 
		adapter.update(changes);
		contactsListView.setAdapter(adapter);
		contactsListView.setSelection(selectedPos);
	}
	
	private void initializeContactList(){
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		adapter.initialize();
		contactsListView.setAdapter(adapter);
	}
	
	/**
	 * This class perform the GUI update
	 * @author s.semeria
	 *
	 */

	private class ContactListUpdater extends ContactsUIUpdater{

		public ContactListUpdater(Activity act) {
			super(act);
			}

		protected void handleUpdate(Object parameter) {
				if (parameter instanceof ContactListChanges){		
				ContactListChanges changes = (ContactListChanges) parameter;
				refreshContactList(changes);	
			}
		}		
	}	
	
	private class MapUpdater extends ContactsUIUpdater{

		public MapUpdater(Activity act) {
			super(act);
			}			
			
			protected void handleUpdate(Object parameter) {
					if (ContactManager.getInstance().updateIsOngoing()){
					MapView mapView = (MapView) activity.findViewById(R.id.myMapView);
					mapView.invalidate();
				}
				
				if (parameter instanceof ContactListChanges){		
					ContactListChanges changes = (ContactListChanges) parameter;
					refreshContactList(changes);	
				}
			}		
	}

	@Override
	protected void onStart() {		
		super.onStart();
		myLogger.log(Logger.INFO, "OnStart called: This activity has Task ID: " + getTaskId());
	}	
}
