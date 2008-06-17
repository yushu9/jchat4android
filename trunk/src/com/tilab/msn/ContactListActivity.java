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



// TODO: Auto-generated Javadoc
/**
 * The Class ContactListActivity.
 */
public class ContactListActivity extends MapActivity implements ConnectionListener {
   
	/** The gateway. */
	private JadeGateway gateway;
	
	/** The Constant myLogger. */
	private static final Logger myLogger = Logger.getMyLogger(ContactListActivity.class.getName());
	
	/** The main tab host. */
	private TabHost mainTabHost;
	
	/** The contacts list view. */
	private MultiSelectionListView contactsListView;
	
	/** The overlay ctrl. */
	private OverlayController overlayCtrl;
	
	/** The overlay. */
	private ContactsPositionOverlay overlay;
	
	/** The map view. */
	private MapView mapView;
	
	/** The contact view. */
	private View contactView; 
    
    /** The map view tab. */
    private View mapViewTab; 
	
	/** The parameter dialog. */
	private static JadeParameterDialog parameterDialog;
	
	//MENUITEM CONSTANT
	/** The MENUITE m_ i d_ exit. */
	private final int MENUITEM_ID_EXIT=Menu.FIRST;
	
	/** The MENUITE m_ i d_ settings. */
	private final int MENUITEM_ID_SETTINGS=Menu.FIRST+1;
	
	/** The MENUITE m_ i d_ connect. */
	private final int MENUITEM_ID_CONNECT=Menu.FIRST+2;
	
	//Context Menu entries
	/** The CONTEX t_ men u_ ite m_ cha t_ list. */
	private final int CONTEXT_MENU_ITEM_CHAT_LIST = Menu.FIRST+3;
	
	/** The CONTEX t_ men u_ ite m_ cal l_ list. */
	private final int CONTEXT_MENU_ITEM_CALL_LIST = Menu.FIRST+4;
	
	/** The CONTEX t_ men u_ ite m_ sm s_ list. */
	private final int CONTEXT_MENU_ITEM_SMS_LIST = Menu.FIRST+5;
	
	/** The CONTEX t_ men u_ ite m_ cha t_ map. */
	private final int CONTEXT_MENU_ITEM_CHAT_MAP = Menu.FIRST+6;
	
	/** The CONTEX t_ men u_ ite m_ cal l_ map. */
	private final int CONTEXT_MENU_ITEM_CALL_MAP = Menu.FIRST+7;
	
	/** The CONTEX t_ men u_ ite m_ sm s_ map. */
	private final int CONTEXT_MENU_ITEM_SMS_MAP = Menu.FIRST+8;
	
	//NEEDED TAGS FOR THE TABHOST (to address them)
	/** The CONTACT s_ ta b_ tag. */
	private final String CONTACTS_TAB_TAG="ContTab";
	
	/** The MAPVIE w_ ta b_ tag. */
	private final String MAPVIEW_TAB_TAG="MapViewTab";
	
	//Return codes 
	/** The Constant CHAT_ACTIVITY_CLOSED. */
	public static final int CHAT_ACTIVITY_CLOSED = 777;
	
	/** The out of focus tab gradient. */
	private GradientDrawable outOfFocusTabGradient;
//	private GradientDrawable rightTabGradient;
	/** The selected tab gradient. */
private GradientDrawable selectedTabGradient;
	
	//Array of updaters
	/** The activity updater. */
	private  ContactListActivityUpdater activityUpdater;
	
	/** The Constant OTHER_PARTICIPANTS. */
	public static final String OTHER_PARTICIPANTS = "com.tilab.msn.Prova";
	
	/** The Constant MESSAGE. */
	public static final String MESSAGE = "com.tilab.msn.Message";
	
	/** The Constant ID_ACTIVITY_PENDING_RESULT. */
	public static final String ID_ACTIVITY_PENDING_RESULT = "ID_ACTIVITY_PENDING_RESULT";
	
	/**
	 * Inits the ui.
	 */
	private void initUI(){
		
		//Setup the main tabhost
        setContentView(R.layout.homepage);
        mainTabHost = (TabHost) findViewById(R.id.main_tabhost);
        mainTabHost.setup();
        
      //Fill the contacts tab
        TabSpec contactsTabSpecs = mainTabHost.newTabSpec(CONTACTS_TAB_TAG);
        TabSpec mapTabSpecs = mainTabHost.newTabSpec(MAPVIEW_TAB_TAG);
        
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
		
		int[] colors= new int[] {res.getColor(R.color.white), res.getColor(R.color.blue)};
/*		int midColor = Color.rgb((Color.red(colors[0]) + Color.red(colors[1]))/2,
							(Color.green(colors[0]) + Color.green(colors[1]))/2,
							(Color.blue(colors[0]) + Color.blue(colors[1]))/2);*/
		int[] selectedTabColors = new int[] {res.getColor(R.color.white), res.getColor(R.color.blue)};
		int[] outOfFocusTabColors = new int[] {res.getColor(R.color.white), res.getColor(R.color.dark_grey)}; 
		 
		GradientDrawable contentGradient= new GradientDrawable(Orientation.LEFT_RIGHT, colors);
		
	    //leftTabGradient = new GradientDrawable(Orientation.LEFT_RIGHT, leftTabColors);
		//leftTabGradient.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
		
		outOfFocusTabGradient  = new GradientDrawable(Orientation.LEFT_RIGHT, outOfFocusTabColors);
		outOfFocusTabGradient.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
		
		selectedTabGradient = new GradientDrawable(Orientation.LEFT_RIGHT, selectedTabColors);
		selectedTabGradient.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});
		
		mapViewTab.setBackground(outOfFocusTabGradient);
		contactView.setBackground(selectedTabGradient);
		
        View homeTab = (View) findViewById(R.id.content1);
        homeTab.setBackground(contentGradient);
        View homeTab1 = (View) findViewById(R.id.content2);
        homeTab1.setBackground(contentGradient);
       
        
        ///NEW PIECE OF CODE HEREEEE///
        mainTabHost.setOnTabChangedListener( new TabHost.OnTabChangeListener(){

			@Override
			public void onTabChanged(String arg0) {
				// TODO Auto-generated method stub
				if (arg0 == null){
					contactView.setBackground(ContactListActivity.this.selectedTabGradient);
					mapViewTab.setBackground(ContactListActivity.this.outOfFocusTabGradient);
				} else {
					mapViewTab.setBackground(ContactListActivity.this.selectedTabGradient);
					contactView.setBackground(ContactListActivity.this.outOfFocusTabGradient);
				}
			}
        	
        });
        
        ///NEW PIECE ENDS HERE!!!!!!////
        		
		//init the map view
		mapView = (MapView) findViewById(R.id.myMapView);
		mapView.setOnLongPressListener(new MapView.OnLongPressListener(){
			  
			public boolean  onLongPress(View v, float x, float y) {
				  boolean retVal = false;
				  
				  if (overlay.getSelectedItems().size() > 0){
					  v.getParent().showContextMenuForChild(v);
					  retVal=true;
				  }  
					
				  return retVal;
			  }
		});
	
		mapView.setOnPopulateContextMenuListener(new View.OnPopulateContextMenuListener(){
			public void  onPopulateContextMenu(ContextMenu menu, View v, Object menuInfo) {
				//Let the menu appear
				menu.add(0, CONTEXT_MENU_ITEM_CHAT_MAP, R.string.menu_item_chat);
				menu.add(0, CONTEXT_MENU_ITEM_CALL_MAP, R.string.menu_item_call);
				menu.add(0, CONTEXT_MENU_ITEM_SMS_MAP, R.string.menu_item_sms);
			}
		});
		
		overlayCtrl = mapView.createOverlayController();
		overlay = new ContactsPositionOverlay(mapView,getResources());
		overlayCtrl.add(overlay,true);

		
		//Button for switching map mode
		Button switchButton = (Button) findViewById(R.id.switchMapBtn);
		switchButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Button clickedBtn = (Button) arg0;
				
			
				
				if (mapView.isSatellite()){
					clickedBtn.setText(ContactListActivity.this.getText(R.string.label_toggle_map));
				} else {
					clickedBtn.setText(ContactListActivity.this.getText(R.string.label_toggle_satellite));
				}
				
				mapView.toggleSatellite();
			
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
							menu.add(0, CONTEXT_MENU_ITEM_CHAT_LIST, R.string.menu_item_chat);
							menu.add(0, CONTEXT_MENU_ITEM_CALL_LIST, R.string.menu_item_call);
							menu.add(0, CONTEXT_MENU_ITEM_SMS_LIST, R.string.menu_item_sms);
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
		parameterDialog = new JadeParameterDialog(this);
		initializeContactList();

	}		
    
	
	/**
	 * Gets the random number.
	 * 
	 * @return the random number
	 */
	private String getRandomNumber(){
		Random rnd = new Random();
		int randInt  = rnd.nextInt();
		return "RND" + String.valueOf(randInt);
	}
	
    /* (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle icicle) {
   
        myLogger.log(Logger.INFO, "onReceiveIntent called: My currentThread has this ID: " + Thread.currentThread().getId());
        super.onCreate(icicle);
        ContactListAdapter cla = new ContactListAdapter(this);
        ContactManager.getInstance().addAdapter(cla);
        MsnSessionManager.getInstance().initialize(this);
        
        String phoneNumber = getMyPhoneNumber();
        ContactManager.getInstance().addMyContact(phoneNumber);
        
        //start updating myContact
        GeoNavigator.setLocationProviderName(getText(R.string.location_provider_name).toString());
        GeoNavigator.getInstance(this).initialize();
        GeoNavigator.getInstance(this).startLocationUpdate();
     
        activityUpdater = new ContactListActivityUpdater(this);
        
        //Initialize the UI
        initUI();
        disableUI();
        
    }
    
    /**
     * Enable ui.
     */
    private void enableUI(){
    	View v = findViewById(R.id.main_view);
    	contactsListView.setEnabled(true);
    	contactView.setEnabled(true);
    	mapViewTab.setEnabled(true);
    	
        v.setEnabled(true);
    }
    
    /**
     * Disable ui.
     */
    private void disableUI(){
    	View v = findViewById(R.id.main_view);
    	contactsListView.setEnabled(false);
    	contactView.setEnabled(false);
    	mapViewTab.setEnabled(false);
    	
        v.setEnabled(false);
    }

   //Retrieve the JADE properties from Dialog or configuration file
	/**
    * Gets the jade properties.
    * 
    * @return the jade properties
    */
   public static Properties getJadeProperties(){
		 //fill Jade connection properties
        Properties jadeProperties = new Properties(); 
        
        jadeProperties.setProperty(Profile.MAIN_HOST, parameterDialog.getJadeAddress());
        jadeProperties.setProperty(Profile.MAIN_PORT, parameterDialog.getJadePort());
       
       
        jadeProperties.setProperty(JICPProtocol.MSISDN_KEY, ContactManager.getInstance().getMyContact().getPhoneNumber());
        return jadeProperties;
	}
	
	/**
	 * Gets the my phone number.
	 * 
	 * @return the my phone number
	 */
	private String getMyPhoneNumber(){
		 //Get the phone number of my contact
        String numTel = SystemProperties.get("numtel");
		//if number is not available
		if (numTel.equals("")){
			myLogger.log(Logger.WARNING, "Cannot access the numtel! A random number shall be used!!!");
			numTel = getRandomNumber();
		}
		return numTel;
	}
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 */
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
		//Debug.stopMethodTracing();
		super.onDestroy();
	}
	

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onStop()
	 */
	protected void onStop() {
		myLogger.log(Logger.INFO, "onStop called ...");
		super.onStop();
	}


	/* (non-Javadoc)
	 * @see jade.android.ConnectionListener#onConnected(jade.android.JadeGateway)
	 */
	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
	
		enableUI();
		
		
		
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


	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	protected void onResume() {
		myLogger.log(Logger.INFO, "onResume called...");	
		super.onResume();
	}

	
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	protected void onPause() {
		myLogger.log(Logger.INFO, "onPause called...");
		
		super.onPause();	
	}

	/* (non-Javadoc)
	 * @see jade.android.ConnectionListener#onDisconnected()
	 */
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	

	/* (non-Javadoc)
	 * @see android.app.Activity#onFreeze(android.os.Bundle)
	 */
	protected void onFreeze(Bundle outState) {
		// TODO Auto-generated method stub
		myLogger.log(Logger.INFO, "onFreeze called...");
		super.onFreeze(outState);
	}




	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENUITEM_ID_CONNECT, R.string.menuitem_connect, R.drawable.connect);
		menu.add(0, MENUITEM_ID_SETTINGS, R.string.menuitem_settings, R.drawable.settings);
		menu.add(0, MENUITEM_ID_EXIT, R.string.menuitem_exit,R.drawable.stop);
		return true;
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Item menuItemConnect = menu.findItem(MENUITEM_ID_CONNECT);
		menuItemConnect.setShown((gateway == null));
		Item menuItemSettings = menu.findItem(MENUITEM_ID_SETTINGS);
		menuItemSettings.setShown((gateway == null));
		return super.onPrepareOptionsMenu(menu);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.Menu.Item)
	 */
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		
		switch(item.getId()) {
			case MENUITEM_ID_EXIT:
				finish();
			break;			
			case MENUITEM_ID_SETTINGS:
				parameterDialog.show();
			break;
			case MENUITEM_ID_CONNECT:
				 //try to get a JadeGateway
		        try {
		        	//fill Jade connection properties
		            Properties jadeProperties = getJadeProperties();    
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

			break;
				
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.Menu.Item)
	 */
	public boolean onContextItemSelected(Item item) {
		
		switch(item.getId()) {
			case CONTEXT_MENU_ITEM_CALL_LIST:
			{
				List<String> selectedIds= contactsListView.getAllSelectedItems();
				if (selectedIds.size() == 1){
					callContact(selectedIds.get(0));
				} else {
					Toast.makeText(this, R.string.error_msg_multiple_phonecalls, 2000).show();
				}
				
			}
			break;
			
			case CONTEXT_MENU_ITEM_CALL_MAP:
			{
				List<String> selectedIds= overlay.getSelectedItems();
				if (selectedIds.size() == 1){
					callContact(selectedIds.get(0));
				} else {
					Toast.makeText(this, R.string.error_msg_multiple_phonecalls, 2000);
				}
			}
			break;
			
			case CONTEXT_MENU_ITEM_CHAT_LIST:
			{
				List<String> participantIds = contactsListView.getAllSelectedItems();
				launchChatSession(participantIds);
			}
			break;
			
			case CONTEXT_MENU_ITEM_CHAT_MAP:
			{
				List<String> participantIds = overlay.getSelectedItems();
				launchChatSession(participantIds);
			}
			break;
			
			case CONTEXT_MENU_ITEM_SMS_LIST:
			case CONTEXT_MENU_ITEM_SMS_MAP:
				Toast.makeText(this, R.string.missing_feature_sms, 3000).show();
				break;
			default:
		}
		return false;
	}


	/**
	 * Call contact.
	 * 
	 * @param selectedCPhoneNumber the selected c phone number
	 */
	private void callContact(String selectedCPhoneNumber) {
		IPhone phoneService = null; 
		  try { 
		      IServiceManager sm = ServiceManagerNative.getDefault(); 
		      phoneService = IPhone.Stub.asInterface(sm.getService("phone")); 
		      phoneService.call(selectedCPhoneNumber);
		  } catch (Exception e) { 
			  myLogger.log(Logger.SEVERE, e.getMessage(), e);
		  }
	}
	
	/**
	 * Launch chat session.
	 * 
	 * @param participantIds the participant ids
	 */
	private void launchChatSession(List<String> participantIds){
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

	}
	
	/**
	 * Update list adapter.
	 * 
	 * @param changes the changes
	 */
	private void updateListAdapter(ContactListChanges changes){
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		//FIXME: if this works we should try to use the DataSetObserver pattern 
		adapter.update(changes);		
	}
	
	/**
	 * Initialize contact list.
	 */
	private void initializeContactList(){
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		adapter.initialize();
		contactsListView.setAdapter(adapter);
	}
	
		
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {		
		super.onStart();
		myLogger.log(Logger.INFO, "OnStart called: This activity has Task ID: " + getTaskId());
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, java.lang.String, android.os.Bundle)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		// TODO Auto-generated method stub
		myLogger.log(Logger.INFO, "onActivityResult() was called! ChatActivity should have been closed!!");
		
		switch (requestCode){
			case CHAT_ACTIVITY_CLOSED:
				this.overlay.uncheckAllContacts();
				this.contactsListView.uncheckAllSelectedItems();
			//	this.overlay.uncheckAllContacts();
			break;
		}
	}	
	
	
	/**
	 * The Class ContactListActivityUpdater.
	 */
	private class ContactListActivityUpdater extends ContactsUIUpdater{

		/**
		 * Instantiates a new contact list activity updater.
		 * 
		 * @param act the act
		 */
		public ContactListActivityUpdater(Activity act) {
			super(act);
		}			
			
		/* (non-Javadoc)
		 * @see com.tilab.msn.ContactsUIUpdater#handleUpdate(java.lang.Object)
		 */
		protected void handleUpdate(Object parameter) {
				
				boolean anyChanges = false;
				
				if (parameter instanceof ContactListChanges){		
					ContactListChanges changes = (ContactListChanges) parameter;
					anyChanges = true;
					updateListAdapter(changes);
					overlay.update(changes);
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
