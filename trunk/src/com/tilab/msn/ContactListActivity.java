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

/**
 * The main activity. Shows two tabs: one with contact list (with distance from current contact)
 * and the second showing contacts locations on map.
 * <p>
 * It basically manages application initialization and shutdown, UI, receivers and prepares Jade Android add-on sending data to the
 * agent for GUI update
 * 
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class ContactListActivity extends MapActivity implements
		ConnectionListener {

	/** 
	 * Id of the menu exit item. 
	 */
	private final int MENUITEM_ID_EXIT = Menu.FIRST;

	/**
	 *  Id of the menu settings item.  
	 */
	private final int MENUITEM_ID_SETTINGS = Menu.FIRST + 1;

	/** 
	 * Id of the menu connect item. 
	 */
	private final int MENUITEM_ID_CONNECT = Menu.FIRST + 2;

	/** 
	 * Id of the context menu chat item. 
	 */
	private final int CONTEXT_MENU_ITEM_CHAT_LIST = Menu.FIRST + 3;

	/** 
	 * Id of the context menu call item. 
	 */
	private final int CONTEXT_MENU_ITEM_CALL_LIST = Menu.FIRST + 4;

	/**
	 *  Id of the context menu sms item. 
	 */
	private final int CONTEXT_MENU_ITEM_SMS_LIST = Menu.FIRST + 5;

	/**
	 *  Id of the map context menu chat item. 
	 */
	private final int CONTEXT_MENU_ITEM_CHAT_MAP = Menu.FIRST + 6;

	/** 
	 * Id of the map context menu call item. 
	 */
	private final int CONTEXT_MENU_ITEM_CALL_MAP = Menu.FIRST + 7;

	/** 
	 * Id of the map context menu sms item. 
	 */
	private final int CONTEXT_MENU_ITEM_SMS_MAP = Menu.FIRST + 8;

	/** 
	 * Id of contact tab
	 */
	private final String CONTACTS_TAB_TAG = "ContTab";

	/** 
	 * Id of mapview tab 
	 */
	private final String MAPVIEW_TAB_TAG = "MapViewTab";

	/**
	 *  Value returned by the <code>ChatActivity</code> when closed. 
	 */
	public static final int CHAT_ACTIVITY_CLOSED = 777;

	/** 
	 * Key for retrieving the ActivityPendingResults from Intent
	 */
	public static final String ID_ACTIVITY_PENDING_RESULT = "ID_ACTIVITY_PENDING_RESULT";

	/** 
	 * The Jade gateway instance (retrieved after call to <code>JadeGateway.connect()</code>)
	 */
	private JadeGateway gateway;

	/** 
	 * Jade Logger for logging purpose 
	 */
	private static final Logger myLogger = Logger
			.getMyLogger(ContactListActivity.class.getName());

	/** 
	 * The main tab host. 
	 */
	private TabHost mainTabHost;

	/** 
	 * The customized contacts list view. 
	 */
	private MultiSelectionListView contactsListView;

	/** 
	 * Overlay controller instance. 
	 */
	private OverlayController overlayCtrl;

	/** 
	 * The customized overlay we use to draw on the map. 
	 */
	private ContactsPositionOverlay overlay;

	/** 
	 * The map view 
	 */
	private MapView mapView;

	/** 
	 * The view inside the contact tab. 
	 */
	private View contactView;

	/** 
	 * The view inside map tab. 
	 */
	private View mapViewTab;

	/**
	 *  Custom dialog containing Jade connection parameters entered by the user. 
	 */
	private static JadeParameterDialog parameterDialog;

	/** 
	 * GradientDrawable for the out of focus tab 
	 */
	private GradientDrawable outOfFocusTabGradient;

	/** 
	 * GradientDrawable for the in focus tab
	 */
	private GradientDrawable selectedTabGradient;

	/** 
	 * Updater for this activity. 
	 */
	private ContactListActivityUpdater activityUpdater;

	/**
	 * Initializes the activity's UI interface.
	 */
	private void initUI() {

		//Setup the main tabhost
		setContentView(R.layout.homepage);
		mainTabHost = (TabHost) findViewById(R.id.main_tabhost);
		mainTabHost.setup();

		//Fill the contacts tab
		TabSpec contactsTabSpecs = mainTabHost.newTabSpec(CONTACTS_TAB_TAG);
		TabSpec mapTabSpecs = mainTabHost.newTabSpec(MAPVIEW_TAB_TAG);

		ViewInflate inflater;
		inflater = (ViewInflate) getSystemService(Context.INFLATE_SERVICE);
		contactView = inflater.inflate(R.layout.contact_tab, null, null);
		contactsTabSpecs.setIndicator(contactView);
		mapViewTab = inflater.inflate(R.layout.maptab, null, null);
		mapTabSpecs.setIndicator(mapViewTab);
		contactsTabSpecs.setContent(R.id.content1);
		mapTabSpecs.setContent(R.id.content2);
		mainTabHost.addTab(contactsTabSpecs);
		mainTabHost.addTab(mapTabSpecs);
		Resources res = getResources();

		int[] colors = new int[] { res.getColor(R.color.white),
				res.getColor(R.color.blue) };
		/*		int midColor = Color.rgb((Color.red(colors[0]) + Color.red(colors[1]))/2,
		 (Color.green(colors[0]) + Color.green(colors[1]))/2,
		 (Color.blue(colors[0]) + Color.blue(colors[1]))/2);*/
		int[] selectedTabColors = new int[] { res.getColor(R.color.white),
				res.getColor(R.color.blue) };
		int[] outOfFocusTabColors = new int[] { res.getColor(R.color.white),
				res.getColor(R.color.dark_grey) };

		GradientDrawable contentGradient = new GradientDrawable(
				Orientation.LEFT_RIGHT, colors);

		//leftTabGradient = new GradientDrawable(Orientation.LEFT_RIGHT, leftTabColors);
		//leftTabGradient.setCornerRadii(new float[]{10.0f,10.0f,10.0f,10.0f,0.0f,0.0f,0.0f,0.0f});

		outOfFocusTabGradient = new GradientDrawable(Orientation.LEFT_RIGHT,
				outOfFocusTabColors);
		outOfFocusTabGradient.setCornerRadii(new float[] { 10.0f, 10.0f, 10.0f,
				10.0f, 0.0f, 0.0f, 0.0f, 0.0f });

		selectedTabGradient = new GradientDrawable(Orientation.LEFT_RIGHT,
				selectedTabColors);
		selectedTabGradient.setCornerRadii(new float[] { 10.0f, 10.0f, 10.0f,
				10.0f, 0.0f, 0.0f, 0.0f, 0.0f });

		mapViewTab.setBackground(outOfFocusTabGradient);
		contactView.setBackground(selectedTabGradient);

		View homeTab = (View) findViewById(R.id.content1);
		homeTab.setBackground(contentGradient);
		View homeTab1 = (View) findViewById(R.id.content2);
		homeTab1.setBackground(contentGradient);

		mainTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				if (arg0 == null) {
					contactView
							.setBackground(ContactListActivity.this.selectedTabGradient);
					mapViewTab
							.setBackground(ContactListActivity.this.outOfFocusTabGradient);
				} else {
					mapViewTab
							.setBackground(ContactListActivity.this.selectedTabGradient);
					contactView
							.setBackground(ContactListActivity.this.outOfFocusTabGradient);
				}
			}

		});

		//init the map view
		mapView = (MapView) findViewById(R.id.myMapView);
		mapView.setOnLongPressListener(new MapView.OnLongPressListener() {

			public boolean onLongPress(View v, float x, float y) {
				boolean retVal = false;

				if (overlay.getSelectedItems().size() > 0) {
					v.getParent().showContextMenuForChild(v);
					retVal = true;
				}

				return retVal;
			}
		});

		mapView
				.setOnPopulateContextMenuListener(new View.OnPopulateContextMenuListener() {
					public void onPopulateContextMenu(ContextMenu menu, View v,
							Object menuInfo) {
						//Let the menu appear
						menu.add(0, CONTEXT_MENU_ITEM_CHAT_MAP,
								R.string.menu_item_chat);
						menu.add(0, CONTEXT_MENU_ITEM_CALL_MAP,
								R.string.menu_item_call);
						menu.add(0, CONTEXT_MENU_ITEM_SMS_MAP,
								R.string.menu_item_sms);
					}
				});

		overlayCtrl = mapView.createOverlayController();
		overlay = new ContactsPositionOverlay(mapView, getResources());
		overlayCtrl.add(overlay, true);

		//Button for switching map mode
		Button switchButton = (Button) findViewById(R.id.switchMapBtn);
		switchButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Button clickedBtn = (Button) arg0;

				if (mapView.isSatellite()) {
					clickedBtn.setText(ContactListActivity.this
							.getText(R.string.label_toggle_map));
				} else {
					clickedBtn.setText(ContactListActivity.this
							.getText(R.string.label_toggle_satellite));
				}

				mapView.toggleSatellite();

			}

		});

		//Create the updater array

		//Select default tab
		mainTabHost.setCurrentTabByTag(CONTACTS_TAB_TAG);

		contactsListView = (MultiSelectionListView) findViewById(R.id.contactsList);
		int[] selectorColors = new int[] { res.getColor(R.color.light_green),
				res.getColor(R.color.dark_green) };
		GradientDrawable selectorDrawable = new GradientDrawable(
				Orientation.TL_BR, selectorColors);
		contactsListView.setSelector(selectorDrawable);

		//added ContextMenu
		contactsListView
				.setOnPopulateContextMenuListener(new View.OnPopulateContextMenuListener() {
					public void onPopulateContextMenu(ContextMenu menu, View v,
							Object menuInfo) {
						MultiSelectionListView myLv = (MultiSelectionListView) v;
						ContextMenuInfo info = (ContextMenuInfo) menuInfo;
						myLv.setSelection(info.position);

						String selectedCId = (String) myLv.getSelectedItem();
						List<String> checkedContacts = myLv
								.getAllSelectedItems();

						//If the selected item is also checked
						if (checkedContacts.contains(selectedCId)) {
							//Let the menu appear
							Contact selectedC = ContactManager.getInstance()
									.getContact(selectedCId);
							if (selectedC.isOnline())
								menu.add(0, CONTEXT_MENU_ITEM_CHAT_LIST,
										R.string.menu_item_chat);
							menu.add(0, CONTEXT_MENU_ITEM_CALL_LIST,
									R.string.menu_item_call);
							menu.add(0, CONTEXT_MENU_ITEM_SMS_LIST,
									R.string.menu_item_sms);
						}
					}
				});

		contactsListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						CheckBox cb = (CheckBox) v
								.findViewById(R.id.contact_check_box);
						ContactListAdapter adapter = (ContactListAdapter) parent
								.getAdapter();
						String selCId = (String) adapter.getItem(position);
						Contact selC = ContactManager.getInstance().getContact(
								selCId);
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
	 * Retrieves a string as a replacement for the phone number if the phone number is not available 
	 * (no <code>/data/local.prop</code> on emulator).
	 * 
	 * @return String formatted as "RND&ltRandom Number&gt"
	 */
	private String getRandomNumber() {
		Random rnd = new Random();
		int randInt = rnd.nextInt();
		return "RND" + String.valueOf(randInt);
	}

	/** 
	 * Executed at activity creation. Performs UI initialization, ContactManager initialization, 
	 * start of contact location update.
	 * 
	 * @param icicle Saved state if the application has been frozen (not used)
	 */
	public void onCreate(Bundle icicle) {

		myLogger.log(Logger.INFO,
				"onReceiveIntent called: My currentThread has this ID: "
						+ Thread.currentThread().getId());
		super.onCreate(icicle);
		ContactListAdapter cla = new ContactListAdapter(this);
		ContactManager.getInstance().addAdapter(cla);
		MsnSessionManager.getInstance().initialize(this);

		String phoneNumber = getMyPhoneNumber();
		ContactManager.getInstance().addMyContact(phoneNumber);

		//start updating myContact
		GeoNavigator.setLocationProviderName(getText(
				R.string.location_provider_name).toString());
		GeoNavigator.getInstance(this).initialize();
		GeoNavigator.getInstance(this).startLocationUpdate();

		activityUpdater = new ContactListActivityUpdater(this);

		//Initialize the UI
		initUI();
		disableUI();

	}

	/**
	 * Enables the main view after the application is connected to JADE
	 */
	private void enableUI() {
		View v = findViewById(R.id.main_view);
		contactsListView.setEnabled(true);
		contactView.setEnabled(true);
		mapViewTab.setEnabled(true);

		v.setEnabled(true);
	}

	/**
	 * Disables the UI at the beginning
	 */
	private void disableUI() {
		View v = findViewById(R.id.main_view);
		contactsListView.setEnabled(false);
		contactView.setEnabled(false);
		mapViewTab.setEnabled(false);

		v.setEnabled(false);
	}

	//Retrieve the JADE properties from Dialog or configuration file
	/**
	 * Retrieve the jade properties, needed to connect to the JADE main container. 
	 * <p>
	 * These properties are:
	 * <ul>
	 * 	<li> <code>MAIN_HOST</code>: hostname or IP address of the machine on which the main container is running. 
	 * 			   Taken from resource file or settings dialog.
	 *   <li> <code>MAIN_PORT</code>: port used by the JADE main container. Taken from resource file or settings dialog.
	 *   <li> <code>MSISDN_KEY</code>: name of the JADE agent (the phone number)
	 * </ul>
	 * 
	 * @return the jade properties
	 */
	public static Properties getJadeProperties() {
		//fill Jade connection properties
		Properties jadeProperties = new Properties();

		jadeProperties.setProperty(Profile.MAIN_HOST, parameterDialog
				.getJadeAddress());
		jadeProperties.setProperty(Profile.MAIN_PORT, parameterDialog
				.getJadePort());

		jadeProperties.setProperty(JICPProtocol.MSISDN_KEY, ContactManager
				.getInstance().getMyContact().getPhoneNumber());
		return jadeProperties;
	}

	/**
	 * Gets the phone number of the contact. It does not read the "real" phone number because there's no way to change it among 
	 * different emulators. Reads the numtel property from <code>/data/local.prop</code> file on the emulator or uses a random number.
	 * 
	 * @return the phone number
	 */
	private String getMyPhoneNumber() {
		//Get the phone number of my contact
		String numTel = SystemProperties.get("numtel");
		//if number is not available
		if (numTel.equals("")) {
			myLogger
					.log(Logger.WARNING,
							"Cannot access the numtel! A random number shall be used!!!");
			numTel = getRandomNumber();
		}
		return numTel;
	}

	/**
	 * Handles the shutdown of the application when exiting. Stops location update, 
	 * removes all notifications from status bar, shuts Jade down and clears managers  
	 */
	protected void onDestroy() {

		GeoNavigator.getInstance(this).stopLocationUpdate();

		myLogger.log(Logger.INFO, "onDestroy called ...");
		GeoNavigator.getInstance(this).shutdown();

		ChatSessionNotificationManager notifUpd = MsnSessionManager
				.getInstance().getNotificationManager();

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

	/**
	 * Callback methods called when connection to Android add-on's MicroRuntimeService is completed
	 * Provides an instance of the {@link JadeGateway} that is stored and sends the updater to the agent using 
	 * JadeGateway.execute() making it able to update the GUI.
	 * 
	 * @param gw instance of the JadeGateway returned after the call to <code>JadeGateway.connect()</code>
	 */
	public void onConnected(JadeGateway gw) {
		this.gateway = gw;

		enableUI();

		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");

		try {
			gateway.execute(activityUpdater);
			//put my contact online			
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), 1000).show();
			myLogger.log(Logger.SEVERE, "Exception in onConnected", e);
			e.printStackTrace();
		}
	}

	/**
	 * Method of {@link ConnectionListener} interface, called in case the service got disconnected.
	 * Currently not implemented.
	 */
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	/**
	 * Creates the application main menu
	 * 
	 * @see Activity
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENUITEM_ID_CONNECT, R.string.menuitem_connect,
				R.drawable.connect);
		menu.add(0, MENUITEM_ID_SETTINGS, R.string.menuitem_settings,
				R.drawable.settings);
		menu.add(0, MENUITEM_ID_EXIT, R.string.menuitem_exit, R.drawable.stop);
		return true;
	}

	/**
	 * Called any time the application main menu is displayed
	 * 
	 * @see Activity
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Item menuItemConnect = menu.findItem(MENUITEM_ID_CONNECT);
		menuItemConnect.setShown((gateway == null));
		Item menuItemSettings = menu.findItem(MENUITEM_ID_SETTINGS);
		menuItemSettings.setShown((gateway == null));
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Handles the selection on items in main menu
	 * 
	 * @see Activity
	 */
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);

		switch (item.getId()) {
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
				JadeGateway.connect(MsnAgent.class.getName(),
						new String[] { getText(R.string.contacts_update_time)
								.toString() }, jadeProperties, this, this);
			} catch (Exception e) {
				//troubles during connection
				Toast.makeText(this,
						getString(R.string.error_msg_jadegw_connection),
						Integer.parseInt(getString(R.string.toast_duration)))
						.show();
				myLogger.log(Logger.SEVERE, "Error in onCreate", e);
				e.printStackTrace();
			}

			break;

		}

		return true;
	}

	/**
	 * Handles the context menu selection both in map and in contact view
	 * 
	 * @see Activity
	 */
	public boolean onContextItemSelected(Item item) {

		switch (item.getId()) {
		case CONTEXT_MENU_ITEM_CALL_LIST: {
			List<String> selectedIds = contactsListView.getAllSelectedItems();
			if (selectedIds.size() == 1) {
				callContact(selectedIds.get(0));
			} else {
				Toast.makeText(this, R.string.error_msg_multiple_phonecalls,
						2000).show();
			}

		}
			break;

		case CONTEXT_MENU_ITEM_CALL_MAP: {
			List<String> selectedIds = overlay.getSelectedItems();
			if (selectedIds.size() == 1) {
				callContact(selectedIds.get(0));
			} else {
				Toast.makeText(this, R.string.error_msg_multiple_phonecalls,
						2000);
			}
		}
			break;

		case CONTEXT_MENU_ITEM_CHAT_LIST: {
			List<String> participantIds = contactsListView
					.getAllSelectedItems();
			launchChatSession(participantIds);
		}
			break;

		case CONTEXT_MENU_ITEM_CHAT_MAP: {
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
	 * Performs a fake phone call to a given contact.
	 * Android phone service is used 
	 * 
	 * @param selectedCPhoneNumber phone number of desired contact
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
	 * Start a new chat session with the given participants. The session is registered with session manager
	 * and a new chat activity is launched giving the sessionId of the session as a parameter
	 * 
	 * @param participantIds list of phone numbers of the participants to this conversation
	 */
	private void launchChatSession(List<String> participantIds) {
		//start a new session or retrieve it If the session already exists. its Id is retrieved
		String sessionId = MsnSessionManager.getInstance().startMsnSession(
				participantIds);

		//retrieve a copy of the session
		MsnSession session = MsnSessionManager.getInstance().retrieveSession(
				sessionId);
		//Add a notification for the new session
		MsnSessionManager.getInstance().getNotificationManager()
				.addNewSessionNotification(sessionId);

		//Add to the intent a mean to return a result back to the start activity
		ActivityPendingResult activityResult = createActivityPendingResult(
				CHAT_ACTIVITY_CLOSED, false);

		//packet an intent. We'll try to add the session ID in the intent data in URI form
		//We use intent resolution here, cause the ChatActivity should be selected matching ACTION and CATEGORY
		Intent it = new Intent(Intent.VIEW_ACTION);
		//set the data as an URI (content://sessionId#<sessionIdValue>)
		it.setData(session.getSessionIdAsUri());
		it.setLaunchFlags(Intent.NEW_TASK_LAUNCH | Intent.SINGLE_TOP_LAUNCH);
		it.addCategory(Intent.DEFAULT_CATEGORY);
		it.putExtra(ID_ACTIVITY_PENDING_RESULT, activityResult);
		startActivity(it);

	}

	/**
	 * Updates the adapter associated with this contact list view.
	 * This method is run on the main thread but called periodically by the agent, giving a list of changes 
	 * (new contacts or removed ones).
	 * 
	 * @param changes the list of changes (new contacts or contacts that went offline and must be removed)
	 */
	private void updateListAdapter(ContactListChanges changes) {
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		adapter.update(changes);
	}

	/**
	 * Initialize the list filling up its adapter with all the available data.
	 * After initialization all changes shall be incremental (contacts added, contacts removed)
	 */
	private void initializeContactList() {
		ContactListAdapter adapter = ContactManager.getInstance().getAdapter();
		adapter.initialize();
		contactsListView.setAdapter(adapter);
	}

	/**
	 * Called when closing a chat activity, to remove the check from all previously selected contacts
	 * 
	 * @see Activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		// TODO Auto-generated method stub
		myLogger
				.log(Logger.INFO,
						"onActivityResult() was called! ChatActivity should have been closed!!");

		switch (requestCode) {
		case CHAT_ACTIVITY_CLOSED:
			this.overlay.uncheckAllContacts();
			this.contactsListView.uncheckAllSelectedItems();
			break;
		}
	}

	/**
	 * Updater class that performs allows the agent to modify the GUI (both the contacts view and the map view)
	 */
	private class ContactListActivityUpdater extends ContactsUIUpdater {

		/**
		 * Instantiates a new contact list activity updater.
		 * 
		 * @param act reference to the activity
		 */
		public ContactListActivityUpdater(Activity act) {
			super(act);
		}

		/**
		 * Method of {@link ContactsUIUpdater}, perform the update using functionalities provided by the parent class
		 * 
		 * @param parameter list of changes to the contact list
		 */
		protected void handleUpdate(Object parameter) {

			boolean anyChanges = false;

			if (parameter instanceof ContactListChanges) {
				ContactListChanges changes = (ContactListChanges) parameter;
				anyChanges = true;
				updateListAdapter(changes);
				overlay.update(changes);
			}

			//refresh the screen: if the map is visible refresh it
			//It seems that using the tab tag does not work
			if (ContactManager.getInstance().movingContacts()) {
				//redraw the map						
				mapView.invalidate();
			}

			if (anyChanges || ContactManager.getInstance().movingContacts()) {
				// if here the contact list is visible
				int selPos = contactsListView.getSelectedItemPosition();
				ContactListAdapter adapter = ContactManager.getInstance()
						.getAdapter();
				contactsListView.setAdapter(adapter);
				contactsListView.setSelection(selPos);
			}

		}

	}
}
