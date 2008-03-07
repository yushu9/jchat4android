package com.tilab.msn;



import java.net.ConnectException;

import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.Profile;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.TelephonyProperties;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class ContactListActivity extends ListActivity implements ConnectionListener {
   
	private JadeGateway gateway;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        
        //fill Jade connection properties
        Properties jadeProperties = new Properties(); 
        jadeProperties.setProperty(Profile.MAIN_HOST, getString(R.string.jade_platform_host));
        jadeProperties.setProperty(Profile.MAIN_PORT, getString(R.string.jade_platform_port));
        
    
        GeoNavigator.setLocationProvider("gps");
     
        //try to get a JadeGateway
        try {
			JadeGateway.connect(MsnAgent.class.getName(), jadeProperties, this, this);
		} catch (Exception e) {
			//troubles during connection
			Toast.makeText(this, 
						   getString(R.string.error_msg_jadegw_connection), 
						   Integer.parseInt(getString(R.string.toast_duration))
						   ).show();
		}
    
    }


	public List<String> contactsToString(){

		List<String> strList = new ArrayList<String>();
		
		List<Contact> contactsList = ContactManager.getInstance().getOtherContactList();
		
 		for (Contact aid : contactsList){
			strList.add(aid.toString());
		}
 		
 		return strList;
	}
	
	
		
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		
		GeoNavigator.stopLocationUpdate(this);
		
		if (gateway != null) {
			UnsubscribeCommand cmd = new UnsubscribeCommand();
			try {
				gateway.execute(cmd);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ControllerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!cmd.isSuccess()){
				Toast.makeText(this, 
						   cmd.getException().toString(), 
						   Integer.parseInt(getString(R.string.toast_duration))
						   ).show();
			}
			
			
			try {
				gateway.shutdownJADE();
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			gateway.disconnect(this);
		}
		
		
	}
	
	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
	
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
		
		//FIXME: there is probably a better way to get agent's AID!!!!
		GetAIDCommandBehaviour cmd = new GetAIDCommandBehaviour();
	
		try {
			gateway.execute(cmd);
			if (!cmd.isSuccess()){
				Toast.makeText(this, cmd.getException().toString(), 1000).show(); 
			} else {
				//FIXME: it would be great if the "myContact" was built by the manager and accessible through a 
				//getMyContact method....
				AID agentAID = (AID) cmd.getCommandResult();
				ContactManager.getInstance().addMyContact(new Contact(agentAID));
				
				GeoNavigator.startLocationUpdate(this);
			        
				
				ContactsUpdaterBehaviour updateBh = new ContactsUpdaterBehaviour(getString(R.string.msn_service_desc_name), 
																				 getString(R.string.msn_service_desc_type), 
																				 Long.parseLong(getString(R.string.contacts_update_time)));
				updateBh.setContactsUpdater(new ContactListUpdater(this));
				gateway.execute(updateBh);
				
			
			}
		} catch(Exception e){
			Toast.makeText(this, e.toString(), 1000).show();
		}
	}



	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}


	private class ContactListUpdater extends ContactsUIUpdater{

		public ContactListUpdater(ContactListActivity act) {
			super(act);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void handleUpdate() {
			// TODO Auto-generated method stub
			List<String> strList = contactsToString();
			ArrayAdapter<String> aa = new ArrayAdapter<String>(ContactListActivity.this,android.R.layout.simple_list_item_1,strList);
			//FIXME: find if there's a way to avoid this cast...
			ListActivity act = (ListActivity) activity;
			act.setListAdapter(aa);
		}
		
	}
	  
}