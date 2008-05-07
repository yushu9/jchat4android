package com.tilab.msn;

import java.util.ArrayList;


import android.net.Uri;

public class MsnSession {

	//This array shall contain the phone numbers of the participants
	private ArrayList<Contact> participants;
	//List of all messages in this session
	private ArrayList<MsnSessionMessage> messageList;
	
	//TODO: Must check RFC 3296 compliance!!!!!
	private final String SESSION_ID_URI_SCHEME="content";
	private final String SESSION_ID_URI_SSP="sessionId";
	
	private String sessionId;	
	
	MsnSession(String sessionId){
		participants = new ArrayList<Contact>();
		messageList = new ArrayList<MsnSessionMessage>();
		this.sessionId =sessionId;
	}	
	
	//This shall be used as intent data. 
	public synchronized Uri getSessionIdAsUri(){
		String str = null;		 
		str = sessionId;		
		return Uri.fromParts(SESSION_ID_URI_SCHEME, SESSION_ID_URI_SSP, str);
	}
	
	
	public synchronized void addParticipant(Contact c){		 
			participants.add(c);		
	}
	
	
	public synchronized void addMessage(MsnSessionMessage msg){		
			messageList.add(msg);		
	}
	
	
	public synchronized ArrayList<MsnSessionMessage> getMessageList(){
		ArrayList<MsnSessionMessage> list = null;		 
		list = new ArrayList<MsnSessionMessage>(messageList);
		return list;
	}
	
	
	public synchronized ArrayList<Contact> getAllParticipants(){
		ArrayList<Contact> list=null;		
		list = new ArrayList<Contact>(participants);
		return list;
	}
	

	public synchronized String getSessionId() {		
		String id = null;		
		id = sessionId;		
		return id;
	}
}
