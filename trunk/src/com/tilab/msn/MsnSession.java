package com.tilab.msn;

import jade.core.AID;
import jade.util.Logger;
import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

public class MsnSession {

	//Logger
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	//This array shall contain the IDs (phone numbers) of the participants
	private List<String> participantIdList;
	//List of all messages in this session
	private ArrayList<MsnSessionMessage> messageList;
	//TODO: Must check RFC 3296 compliance!!!!!
	private final String SESSION_ID_URI_SCHEME="content";
	private final String SESSION_ID_URI_SSP="sessionId";
	private final String NOTIFICATION_TITLE ="Conversation ";
	//This is the "Conversation X" displayed for this session
	private String sessionTitle;
	//Unique ID of this session
	private String sessionId;
	
	
	MsnSession(String sessionId, Iterator recvIt, String senderPhone, int sessionCounter){
		this.sessionId = sessionId;
		this.participantIdList = new ArrayList<String>();
		fillParticipantList(recvIt, senderPhone);
		//prepare the session title
		StringBuffer buffer= new StringBuffer(NOTIFICATION_TITLE);
		buffer.append(sessionCounter);
		this.sessionTitle= buffer.toString();
	}
	
	MsnSession(String sessionId, List<String> participantsIds, int sessionCounter) {
		this.sessionId = sessionId;
		this.participantIdList = participantsIds;
		
		//prepare the session title
		StringBuffer buffer= new StringBuffer(NOTIFICATION_TITLE);
		buffer.append(sessionCounter);
		this.sessionTitle= buffer.toString();
	}
	
	public MsnSession(MsnSession session) {
		this.sessionTitle = session.sessionTitle;
		this.sessionId = session.sessionId;
		this.messageList = new ArrayList<MsnSessionMessage>(session.messageList);
		this.participantIdList = new ArrayList<String>(session.participantIdList);
	}
	
	//Fill the list of participants (Me is not included)
	private void fillParticipantList(Iterator receiversIt, String senderPhoneNum){
		while( receiversIt.hasNext() ) {
			AID contactAID = (AID) receiversIt.next();
			//In this application the agent local name is the contact phone number
			String contactPhoneNum = contactAID.getLocalName();
			String myPhoneNum = ContactManager.getInstance().getMyContact().getPhoneNumber();
			
			//Check that this is not me
			if (!myPhoneNum.equals(contactPhoneNum)){
				//add as a new participant
				participantIdList.add(contactPhoneNum);
			}
		}
		
		participantIdList.add(senderPhoneNum);
	}
	
	//This shall be used as intent data. 
	public Uri getSessionIdAsUri(){
		return Uri.fromParts(SESSION_ID_URI_SCHEME, SESSION_ID_URI_SSP, sessionId);
	}
	
	
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		
		boolean retval=false;
		
		if (o instanceof MsnSession){
			MsnSession otherSession = (MsnSession) o;
			retval = this.sessionId.equals(otherSession.sessionId);
		}
		
		return retval;
	}

	//This method is not thread safe but it is synchronized by MsnSessionManager
	public void addMessage(MsnSessionMessage msg){		
			messageList.add(msg);		
	}
	
	//This method is not thread safe but it is synchronized by MsnSessionManager
	public ArrayList<MsnSessionMessage> getMessageList(){
		ArrayList<MsnSessionMessage> list = null;		 
		list = new ArrayList<MsnSessionMessage>(messageList);
		return list;
	}
	
	
	public List<String> getAllParticipantIds(){	
		return participantIdList;
	}
	
	public int getParticipantCount(){
		return participantIdList.size();
	}

	public String getSessionId() {			
		return sessionId;
	}

	public String toString(){		
		return sessionTitle;	
	}
}