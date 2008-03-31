package com.tilab.msn;


import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
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
	private EditText messageToBeSent;
	private ArrayList contacts;
	private JadeGateway gateway;
	
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		setContentView(R.layout.chat);
		contacts = (ArrayList)getIntent().getSerializableExtra(ContactListActivity.OTHER_PARTICIPANTS);
		
		partsList = (ListView) findViewById(R.id.partsList);
		String[] names = new String[contacts.size()];
		for(int i=0; i<contacts.size();i++){
			names[i] = ((Contact)contacts.get(i)).getName();
		}
		
		ArrayAdapter aAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, names);
		partsList.setAdapter(aAdapter);
		
		messageToBeSent = (EditText)findViewById(R.id.edit);
		
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
		for(int i=0; i<contacts.size(); i++){
			String agentContact = ((Contact)contacts.get(i)).getAgentContact();
			if(agentContact != null){
				msg.addReceiver(new AID(agentContact, AID.ISGUID));
			}
		}
		
		//FIXME: aggiungere il messaggio inviato nella TextBox relativa.
		
		try{
			gateway.execute(new SenderBehaviour(msg));
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
	
}
