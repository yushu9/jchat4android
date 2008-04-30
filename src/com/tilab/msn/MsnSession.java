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
	public Uri getSessionIdAsUri(){
		String str = null;
		synchronized (sessionId) {
			str = sessionId;
		}
		return Uri.fromParts(SESSION_ID_URI_SCHEME, SESSION_ID_URI_SSP, str);
	}
	
	public void addParticipant(Contact c){
		synchronized (participants) {
			participants.add(c);
		}
	}
	
	public void addMessage(MsnSessionMessage msg){
		synchronized (messageList) {
			messageList.add(msg);
		}
	}
	
	
	public ArrayList<MsnSessionMessage> getMessageList(){
		ArrayList<MsnSessionMessage> list = null;
		synchronized (messageList) {
			list = new ArrayList<MsnSessionMessage>(messageList);
		}
		return list;
	}
	
	public ArrayList<Contact> getAllParticipants(){
		ArrayList<Contact> list=null;
		
		synchronized (participants) {
			list = new ArrayList<Contact>(participants);
		}
		return list;
	}

	public String getSessionId() {
		
		String id = null;
		
		synchronized (sessionId) {
			id = sessionId;
		}
		
		return id;
	}
}
