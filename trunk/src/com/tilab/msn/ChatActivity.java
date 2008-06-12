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

/**
 * Represents the activity that allows sending and receiving messages to other contacts.
 * It is launched when a user clicks a notification on the status bar (for reading a message
 * sent by a contact) or when starts a conversation himself. 
 * <p>
 * Please note that only a single activity is used also for managing multiple conversation at a time,
 * that is the user always sees a single activity also when he switches from one to another: activity is
 * simply redrawn.
 * <p>
 * Implements the ConnectionListener interface to be able to connect to the MicroRuntime service for communication 
 * with agent.  
 *  
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0    
 */
public class ChatActivity extends Activity implements ConnectionListener{

	/** Instance of Jade Logger, for debugging purpose. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	/** ListView showing participants to this chat session. */
	private ListView partsList;
	
	/** Button for sending data. */
	private ImageButton sendButton;	
	
	/** Button for closing this activity and session. */
	private ImageButton closeButton;
	
	/** List of already sent messages. */
	private ListView messagesSentList;
	
	/** Edit text for editing the message that must be sent. */
	private EditText messageToBeSent;
	
	/** Instance of jade gateway necessary to work with Jade add-on. */
	private JadeGateway gateway;
	
	/** Id of the session this activity is related to*/
	private String sessionId;
	
	/** Adapter used to fill up the message list */
	private MsnSessionAdapter sessionAdapter;
	
	/** object used to report to the main activity (we need to know when a chat activity is closed to clear
	 *  check on contacts list) 
	 */
	private ActivityPendingResult activityPendingResult;
	
	/**
	 * Retrieves the id of the chat session this activity refers to.
	 * 
	 * @return Id of the session
	 */
	public String getMsnSession(){
		return sessionId;
	}
	
	/**
	 * Initializes basic GUI components and listeners. Also performs connection to add-on's Jade Gateway
	 *  
	 * @param icicle Bundle of data if we are resuming a frozen state (not used)
	 */
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
				MsnSessionManager.getInstance().getNotificationManager().removeSessionNotification(sessionId);
				MsnSessionManager.getInstance().removeMsnSession(sessionId);
				finish();
			}
		});
		
		//fill Jade connection properties
        Properties jadeProperties = ContactListActivity.getJadeProperties();
        
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
	
	
	/**
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		myLogger.log(Logger.INFO, "onResume() was called!" );
		Intent i = getIntent();
		Uri sessionIdUri = i.getData();
		sessionId = sessionIdUri.getFragment();
		
		activityPendingResult = (ActivityPendingResult) i.getParcelableExtra(ContactListActivity.ID_ACTIVITY_PENDING_RESULT);
		MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
		setTitle(session.toString());
		List<String> participants = session.getAllParticipantIds();
		ArrayAdapter aAdapter = new ArrayAdapter(this,R.layout.participant_layout, R.id.participantName, participants);
		partsList.setAdapter(aAdapter);
		MsnSessionManager.getInstance().getNotificationManager().addNewSessionNotification(sessionId);
		messageToBeSent.setText("");
		
		//register an updater for this session
		MsnSessionManager.getInstance().registerChatActivityUpdater(new MessageReceivedUpdater(this));

		//Retrieve messages if the session already contains data
		sessionAdapter.setNewSession(session);
		messagesSentList.setAdapter(sessionAdapter);

		super.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		myLogger.log(Logger.INFO, "WOW: onNewIntent was called!! \n Intent received was: " + intent.toString());
		setIntent(intent);
		super.onNewIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
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
	
	/**
	 * Gets the instance to the add-on's JadeGateway to be able to send messages to be sent to the 
	 * Jade agent. It's a callback, called after the connection to add-on's MicroRuntimeService
	 * 
	 * @param gw Instance of the JadeGateway retrieved after the connection
	 * @see ConnectionListener
	 * @see MicroRuntimeService
	 */
	public void onConnected(JadeGateway gw) {
		this.gateway = gw;
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
	}
	
	/**
	 * Dummy implementation for the ConnectionListener's onDisconnected
	 * 
	 * @param gw Instance of the JadeGateway retrieved after the connection
	 * @see ConnectionListener
	 * @see MicroRuntimeService
	 */
	public void onDisconnected() {
		}
	
	/**
	 * Send message to participants.
	 * 
	 * @param msgContent the msg content
	 */
	private void sendMessageToParticipants(String msgContent){
		
		
		//set all participants as receivers
		MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
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
	
	/**
	 * The Class SenderBehaviour.
	 */
	private class SenderBehaviour extends OneShotBehaviour {

		/** The msg. */
		private ACLMessage theMsg;
		
		/**
		 * Instantiates a new sender behaviour.
		 * 
		 * @param convId the conv id
		 * @param content the content
		 * @param receivers the receivers
		 */
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
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			myLogger.log(Logger.INFO, "Sending msg " +  theMsg.toString());
			myAgent.send(theMsg);
		}
	}
	
	
	/**
	 * The Class MessageReceivedUpdater.
	 */
	private class MessageReceivedUpdater extends ContactsUIUpdater {
		
		/**
		 * Instantiates a new message received updater.
		 * 
		 * @param act the act
		 */
		public MessageReceivedUpdater(Activity act) {
			super(act);
			ChatActivity chatAct = (ChatActivity) act;
			String sessionId = chatAct.getMsnSession();
			data = MsnSessionManager.getInstance().retrieveSession(sessionId);
		}		
		
		//This method updates the GUI and receives the MsnSessionMessage object 
		//that should be added
		/* (non-Javadoc)
		 * @see com.tilab.msn.ContactsUIUpdater#handleUpdate(java.lang.Object)
		 */
		protected void handleUpdate(Object parameter) {
			
			if (parameter instanceof MsnSessionMessage){
				//retrieve the SessionMessage
				myLogger.log(Logger.INFO, "Received an order of UI update: updating GUI with new message");		
				MsnSession session = MsnSessionManager.getInstance().retrieveSession(sessionId);
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
