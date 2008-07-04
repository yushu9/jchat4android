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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.tilab.msn.MsnEventMgr.Event;

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
	
	private ChatActivityHandler activityHandler;
	
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
	 * Initializes basic GUI components and listeners. Also performs connection to add-on's Jade Gateway.
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
            	messageToBeSent.setText("");
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
		
		activityHandler = new ChatActivityHandler();
		
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
	 * Populates the GUI retrieving the sessionId from the intent that initiates the activity itself.
	 * The session Id is saved in the intent as an URI, whose fragment is the part we are interested in.
	 * <p>
	 * Please note that this method shall be called both when the activity is created for the first time and 
	 * when it is resumed from the background (that is, when it is in the foreground and the user switches to a new session
	 * by clicking the status bar notifications)
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
		//List<String> participants = session.getAllParticipantIds();
		List<String> participants = session.getAllParticipantNames();
		ArrayAdapter aAdapter = new ArrayAdapter(this,R.layout.participant_layout, R.id.participantName, participants);
		partsList.setAdapter(aAdapter);
		MsnSessionManager.getInstance().getNotificationManager().addNewSessionNotification(sessionId);
		messageToBeSent.setText("");
		
		//Retrieve messages if the session already contains data
		sessionAdapter.setNewSession(session);
		messagesSentList.setAdapter(sessionAdapter);
		MsnEventMgr.getInstance().registerEvent(MsnEventMgr.Event.INCOMING_MESSAGE_EVENT, activityHandler);
		
		super.onResume();
	}
	
	/**
	 * Called only when resuming  an activity by clicking the status bar, just before <code> onResume() </code>
	 * <p>
	 * Sets the retrieved intent (containing info about the new session selected by the user) as the current one, to make 
	 * <code> onResume() </code> able to populate the GUI with the new data.
	 * 
	 * @param intent the intent launched when clicking on status bar notification (no new activity is created but the new intent is passed anyway)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		myLogger.log(Logger.INFO, "WOW: onNewIntent was called!! \n Intent received was: " + intent.toString());
		setIntent(intent);
		super.onNewIntent(intent);
	}

	

	/**
	 * Called only when destroying  the chat activity when closing the chat window (both when clicking the close button or when going back 
	 * in activity stack with the back arrow).
	 * <p>
	 * It basically performs a disconnection from the service, sends the closing message to the main activity and resets the ChatActivityUpdater
	 * to null (so the agent is aware that the chat activity is not visible). 
	 * 
	 * @param intent the intent launched when clicking on status bar notification (no new activity is created but the new intent is passed anyway)
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();		
				
		if (gateway != null){
			gateway.disconnect(this);
			myLogger.log(Logger.FINER, "ChatActivity.onDestroy() : disconnected from MicroRuntimeService");
		}		
		
		activityPendingResult.sendResult(ContactListActivity.CHAT_ACTIVITY_CLOSED, null, null);
	}
	
	/**
	 * Gets the instance to the add-on's JadeGateway to be able to send messages to be sent to the 
	 * Jade agent. It's a callback, called after the connection to add-on's <code>MicroRuntimeService</code>
	 * 
	 * @param gw Instance of the JadeGateway retrieved after the connection
	 * @see ConnectionListener
	 */
	public void onConnected(JadeGateway gw) {
		this.gateway = gw;
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
	}
	
	/**
	 * Dummy implementation for the ConnectionListener's onDisconnected
	 * 
	 * @see ConnectionListener
	 */
	public void onDisconnected() {
		}
	
	/**
	 * Sends a message to all participants of this session.
	 * <p>
	 * Instantiates a new SenderBehaviour object and sends it to the agent, together with message contents and receiver list, 
	 * then updates the message ListView.  
	 * 
	 * @param msgContent content of the message to be sent 
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
	 * Contains the actual code executed by the agent to send the message.
	 */
	private class SenderBehaviour extends OneShotBehaviour {

		/** ACLMessage to be sent */
		private ACLMessage theMsg;
		
		/**
		 * Instantiates a new sender behaviour. Fills up the ACLMessage with data provided.
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
		
		/**
		 * Sends the message. Executed by JADE agent.
		 */
		public void action() {
			myLogger.log(Logger.INFO, "Sending msg " +  theMsg.toString());
			myAgent.send(theMsg);
		}
	}
	
	
	/**
	 * Inner class that allows to the agent thread to modify the message list, any time a message is received.
	 * Uses the ContactsUIUpdater functionalities to post a Runnable on the UI thread
	 * 
	 */
	private class ChatActivityHandler extends UIEventHandler {
		
		/**
		 * Instantiates a new message received updater.
		 *  
		 * @param act instance of the activity that shall be updated (stored in superclass)
		 */
		public ChatActivityHandler() {
		
		}		
		
		/**
		 * Performs the update of the GUI. 
		 * It handles both the arrival of a new message and the disconnection of an online contact.
		 *  
		 * @param parameter a generic object sent as a parameter. It can be a <code>MsnSessionMessage</code> in case of a new incoming 
		 * message or a String with the disconnected contact Id to display a toast		 
		 */
		protected void handleUpdate(Object parameter) {
			
			if (parameter instanceof MsnSessionMessage){
				//retrieve the SessionMessage
				myLogger.log(Logger.INFO, "Received an order of UI update: updating GUI with new message");		
				
			} 
			
			if (parameter instanceof String ){
				myLogger.log(Logger.INFO, "Received an order of UI update: adding Toast notification");
				String contactGoneName = (String) parameter;
				Toast.makeText(ChatActivity.this, "Contact " +  contactGoneName + " went offline!", 3000).show();
			}				
		}

		/**
		 * 
		 */
		protected void handleEvent(Event event) {
			String eventName = event.getName();
			
			//Handle case of new message
			if (eventName.equals(MsnEventMgr.Event.INCOMING_MESSAGE_EVENT)){
				MsnSessionMessage msnMsg = (MsnSessionMessage) event.getParam("IncomingMessage");
				String sessionId = (String) event.getParam("SessionId");
				
				//check if the message is related to the same session we are currently in.
				//If so, add a new message to session udapter and update it
				if (sessionId.equals(ChatActivity.this.sessionId)){
					sessionAdapter.addMessageView(msnMsg);
					messagesSentList.setAdapter(sessionAdapter);
				} else {
					//if the incoming msg is not for our session, post a notification
					MsnSessionManager.getInstance().getNotificationManager().addNewMsgNotification(sessionId, msnMsg);
					Toast.makeText(ChatActivity.this, msnMsg.getSenderName() + " says: " + msnMsg.getMessageContent(), 3000).show();
				}
			} 
			
		}
		
	}
}
