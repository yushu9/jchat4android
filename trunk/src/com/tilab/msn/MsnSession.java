package com.tilab.msn;

import java.util.List;

public class MsnSession {

	//Unique identifier for each chat session
	private String sessionId;
	//This array shall contain the phone numbers of the participants
	private List<Contact> participants;
	//List of all messages in this session
	private List<MsnSessionMessage> messageList;
	//Session initiator
	private Contact sessionInitiator;
	
	
	public MsnSession(){
		
	}
	
}
