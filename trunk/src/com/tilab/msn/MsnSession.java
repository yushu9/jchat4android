package com.tilab.msn;

import java.util.ArrayList;
import java.util.List;

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
		return Uri.fromParts(SESSION_ID_URI_SCHEME, SESSION_ID_URI_SSP, sessionId);
	}
	
	public void addParticipant(Contact c){
		participants.add(c);
	}
	
	public void addMessage(MsnSessionMessage msg){
		messageList.add(msg);
	}
	
	
	public ArrayList<MsnSessionMessage> getMessageList(){
		return messageList;
	}
	
	public ArrayList<Contact> getAllParticipants(){
		return participants;
	}

	public String getSessionId() {
		return sessionId;
	}
}
