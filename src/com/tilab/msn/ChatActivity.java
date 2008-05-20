package com.tilab.msn;


import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Properties;



import java.util.List;

import android.app.Activity;
import android.app.ActivityPendingResult;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity implements ConnectionListener{

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private ListView partsList;
	private ImageButton sendButton;	
	private ImageButton closeButton;
	private ListView messagesSentList;
	private EditText messageToBeSent;
	private JadeGateway gateway;
	private MsnSession session;
	private MsnSessionAdapter sessionAdapter;
	private ActivityPendingResult activityPendingResult;
	
	public MsnSession getMsnSession(){
		return session;
	}
	
	protected void onCreate(Bundle icicle) {
		Thread.currentThread().getId();
        myLogger.log(Logger.INFO, "onReceiveIntent called: My currentThread has this ID: " + Thread.currentThread().getId());
		super.onCreate(icicle);
	    requestWindowFeature(Window.FEATURE_LEFT_ICON); 
	    setContentView(R.layout.chat);
	    setFeatureDrawable(Window.FEATURE_LEFT_ICON, getResources().getDrawable(R.drawable.chat));		
		myLogger.log(Logger.INFO, "onCreate called ...");
		sessionAdapter = new MsnSessionAdapter(getViewInflate(), getResources());	
		sendButton = (ImageButton) findViewById(R.id.sendBtn);		
		sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	String msgContent = messageToBeSent.getText().toString().trim();
            	myLogger.log(Logger.INFO,"onClick(): send message:" + msgContent);
            	if(msgContent.length()>0){
            		sendMessageToParticipants(msgContent);
            		}	
            }
        });		
		//retrieve the list
		partsList = (ListView) findViewById(R.id.partsList);		
		messageToBeSent = (EditText)findViewById(R.id.edit);
		messagesSentList = (ListView) findViewById(R.id.messagesListView);
		

		closeButton = (ImageButton) findViewById(R.id.closeBtn);
		closeButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				MsnSessionManager.getInstance().getNotificationManager().removeSessionNotification(session.getSessionId());
				MsnSessionManager.getInstance().removeMsnSession(session.getSessionId());
				finish();
			}
		});
		
		//fill Jade connection properties
        Properties jadeProperties = ContactListActivity.getJadeProperties(this);
        
        //try to get a JadeGateway
        try {
			JadeGateway.connect(MsnAgent.class.getName(), jadeProperties, this, this);
		} catch (Exception e) {
			//troubles during connection
			Toast.makeText(this, 
						   getString(R.string.error_msg_jadegw_connection), 
						   Integer.parseInt(getString(R.string.toast_duration))
						   ).show();
			myLogger.log(Logger.SEVERE, "Error in chatActivity", e);
			e.printStackTrace();
		}
	}
	
	protected void onStart() {
		myLogger.log(Logger.INFO, "OnStart was called! This Activity has task ID: " + getTaskId());
		super.onStart();		
	}

	@Override
	protected void onPause() {
		myLogger.log(Logger.INFO, "onPause() was called!" );
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle icicle) {
		myLogger.log(Logger.INFO, "onPostCreate() was called!" );
		super.onPostCreate(icicle);
	}

	@Override
	protected void onPostResume() {
		myLogger.log(Logger.INFO, "onPostResume() was called!" );
		super.onPostResume();
	}

	@Override
	protected void onRestart() {
		myLogger.log(Logger.INFO, "onRestart() was called!" );
		super.onRestart();
	}

	@Override
	protected void onResume() {
		myLogger.log(Logger.INFO, "onResume() was called!" );
		Intent i = getIntent();
		Uri sessionIdUri = i.getData();
		String sessionId = sessionIdUri.getFragment();
		
		activityPendingResult = (ActivityPendingResult) i.getParcelableExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT);
		
		session = MsnSessionManager.getInstance().retrieveSession(sessionId);
		setTitle(session.toString());
		List<String> participants = session.getAllParticipantIds();
		ArrayAdapter aAdapter = new ArrayAdapter(this,R.layout.participant_layout, R.id.participantName, participants);
		partsList.setAdapter(aAdapter);
		MsnSessionManager.getInstance().getNotificationUpdater().removeSessionNotification(sessionId);
		MsnSessionManager.getInstance().getNotificationUpdater().createSessionNotification(sessionId);
		
		messageToBeSent.setText("");
		
		//register an updater for this session
		MsnSessionManager.getInstance().registerChatActivityUpdater(new MessageReceivedUpdater(this));

		//Retrieve messages if the session already contains data
		sessionAdapter.setNewSession(session);
		messagesSentList.setAdapter(sessionAdapter);

		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		myLogger.log(Logger.INFO, "WOW: onNewIntent was called!! \n Intent received was: " + intent.toString());
		setIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();		
		
		
		
		if (gateway != null){
			gateway.disconnect(this);
			myLogger.log(Logger.FINER, "ChatActivity.onDestroy() : disconnected from MicroRuntimeService");
		}		
		MsnSessionManager.getInstance().registerChatActivityUpdater(null);
		activityPendingResult.sendResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, null, null);
		MsnSessionManager.getInstance().registerChatActivityUpdater(null);
		
	}
	
	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
	}
	
	public void onDisconnected() {
		}
	
	private void sendMessageToParticipants(String msgContent){
		
		
		//set all participants as receivers
		List<String> receivers = session.getAllParticipantIds();
		
		try{
			gateway.execute(new SenderBehaviour(session.getSessionId(), msgContent, receivers));
			Contact myContact = ContactManager.getInstance().getMyContact();
    		MsnSessionMessage message = new MsnSessionMessage(msgContent,myContact.getName(),myContact.getPhoneNumber());
    		MsnSessionManager.getInstance().addMessageToSession(session.getSessionId(), message);
    		//Add a new view to the adapter
    		sessionAdapter.addMessageView(message);
    		//refresh the list
    		messagesSentList.setAdapter(sessionAdapter);
		}catch(Exception e){
			myLogger.log(Logger.WARNING, e.getMessage());
		}
	}
	
	private class SenderBehaviour extends OneShotBehaviour {

		private ACLMessage theMsg;
		
		public SenderBehaviour(String convId, String content, List<String> receivers) {
			theMsg = new ACLMessage(ACLMessage.INFORM);
			theMsg.setContent(content);
			theMsg.setOntology(MsnAgent.CHAT_ONTOLOGY);
			theMsg.setConversationId(convId);
			
			for(int i=0; i<receivers.size(); i++){
				String cId = receivers.get(i);
				theMsg.addReceiver(new AID(cId, AID.ISLOCALNAME));
			}
			
		}
		
		public void action() {
			myLogger.log(Logger.INFO, "Sending msg " +  theMsg.toString());
			myAgent.send(theMsg);
		}
	}
	
	
	private class MessageReceivedUpdater extends ContactsUIUpdater {
		
		public MessageReceivedUpdater(Activity act) {
			super(act);
			ChatActivity chatAct = (ChatActivity) act;
			data = chatAct.getMsnSession();
		}		
		
		//This method updates the GUI and receives the MsnSessionMessage object 
		//that should be added
		protected void handleUpdate(Object parameter) {
			
			if (parameter instanceof MsnSessionMessage){
				//retrieve the SessionMessage
				myLogger.log(Logger.INFO, "Received an order of UI update: updating GUI with new message");		
				sessionAdapter.setNewSession(session);
				messagesSentList.setAdapter(sessionAdapter);
			} 
			
			if (parameter instanceof String ){
				myLogger.log(Logger.INFO, "Received an order of UI update: adding Toast notification");
				String contactGoneName = (String) parameter;
				Toast.makeText(ChatActivity.this, "Contact " +  contactGoneName + " went offline!", 3000).show();
			}				
		}
		
	}
}
