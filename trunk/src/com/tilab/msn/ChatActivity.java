package com.tilab.msn;


import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.util.Calendar;

import android.util.DateUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity implements ConnectionListener{

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private ListView partsList;
	private Button sendButton;
	private EditText messagesSent;
	private EditText messageToBeSent;
	private JadeGateway gateway;
	private MsnSession session;
	
	
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		myLogger.log(Logger.INFO, "onCreate called ...");
		setContentView(R.layout.chat);
	
		Intent i = getIntent();
		Uri sessionIdUri = i.getData();
		String sessionId = sessionIdUri.getFragment();
		
		//register an updater for this session
		MsnSessionManager.getInstance().registerMsgReceivedUpdater(sessionId, new MessageReceivedUpdater(this));
		
		session = MsnSessionManager.getInstance().retrieveSession(sessionId);
		
		//retrieve the list
		partsList = (ListView) findViewById(R.id.partsList);
		List<Contact> participants = session.getAllParticipants();
		
		ArrayAdapter aAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, participants);
		partsList.setAdapter(aAdapter);
		
		messageToBeSent = (EditText)findViewById(R.id.edit);
		
		messagesSent = (EditText) findViewById(R.id.sentMsg);

		//Retrieve messages if the session already contains data
		List<MsnSessionMessage> messages = session.getMessageList();
		for (MsnSessionMessage msg : messages) {
			messagesSent.append(msg.toString());
		}
		
		sendButton = (Button) findViewById(R.id.sendBtn);
		sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	String msgContent = messageToBeSent.getText().toString().trim();
            	myLogger.log(Logger.INFO,"onClick(): send message:" + msgContent);
            	if(msgContent.length()>0){
            		sendMessageToParticipants(msgContent);
				}	
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
		}
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		MsnSessionManager.getInstance().removeMsnSession(session.getSessionId());
		IncomingNotificationUpdater updater = MsnSessionManager.getInstance().getNotificationUpdater();
		
		updater.removeSessionNotification(session.getSessionId());
		
		if (gateway != null){
			gateway.disconnect(this);
			myLogger.log(Logger.FINER, "ChatActivity.onDestroy() : disconnected from MicroRuntimeService");
		}
	}
	
	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");
	}
	
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	
	private void sendMessageToParticipants(String msgContent){
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(msgContent);
		msg.setOntology(MsnAgent.CHAT_ONTOLOGY);
		msg.setConversationId(session.getSessionId());
		
		//set all participants as receivers
		List<Contact> contacts = session.getAllParticipants();
		
		for(int i=0; i<contacts.size(); i++){
			Contact c = ((Contact)contacts.get(i));
			if (!c.isOnline()){
				messagesSent.append("Contact " + c.getName() + " is no more available\n\n");
			} else {
				String agentContact = c.getAgentContact();
				if(agentContact != null){
					msg.addReceiver(new AID(agentContact, AID.ISGUID));
				}
			}
		}		
		
		
		try{
			gateway.execute(new SenderBehaviour(msg));
  
    		MsnSessionMessage message = new MsnSessionMessage(msg.getContent(),"Me",false);
    		session.addMessage(message);
    		
    		messagesSent.append(message.toString());
		}catch(Exception e){
			myLogger.log(Logger.WARNING, e.getMessage());
		}
	}

	
	private class SenderBehaviour extends OneShotBehaviour {

		private ACLMessage theMsg;
		
		public SenderBehaviour(ACLMessage msg) {
			theMsg = msg;
		}
		
		public void action() {
			myAgent.send(theMsg);
		}
	}
	
	
	private class MessageReceivedUpdater extends ContactsUIUpdater {

		
		public MessageReceivedUpdater(Activity act) {
			super(act);
		}

		//This method updates the GUI and receives the MsnSessionMessage object 
		//that should be added
		protected void handleUpdate(Object parameter) {
			
			if (parameter instanceof MsnSessionMessage){
				//retrieve the SessionMessage
				MsnSessionMessage msg = (MsnSessionMessage) parameter;
				messagesSent.append(msg.toString());
				messageToBeSent.setText("");
			} 
			if (parameter instanceof String ){
				String contactGoneName = (String) parameter;
				Toast.makeText(ChatActivity.this, "Contact " +  contactGoneName + " went offline!", 3000).show();
			}
				
				
				
		}
		
	}
}
