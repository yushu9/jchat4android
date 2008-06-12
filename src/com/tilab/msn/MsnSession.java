package com.tilab.msn;

import jade.core.AID;
import jade.util.Logger;
import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

// TODO: Auto-generated Javadoc
/**
 * The Class MsnSession.
 */
public class MsnSession {

	//Logger
	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	//This array shall contain the IDs (phone numbers) of the participants
	/** The participant id list. */
	private List<String> participantIdList;
	//List of all messages in this session
	/** The message list. */
	private ArrayList<MsnSessionMessage> messageList;
	//TODO: Must check RFC 3296 compliance!!!!!
	/** The SESSIO n_ i d_ ur i_ scheme. */
	private final String SESSION_ID_URI_SCHEME="content";
	
	/** The SESSIO n_ i d_ ur i_ ssp. */
	private final String SESSION_ID_URI_SSP="sessionId";
	
	/** The NOTIFICATIO n_ title. */
	private final String NOTIFICATION_TITLE ="Conversation ";
	//This is the "Conversation X" displayed for this session
	/** The session title. */
	private String sessionTitle;
	//Unique ID of this session
	/** The session id. */
	private String sessionId;
	
	
	/**
	 * Instantiates a new msn session.
	 * 
	 * @param sessionId the session id
	 * @param recvIt the recv it
	 * @param senderPhone the sender phone
	 * @param sessionCounter the session counter
	 */
	MsnSession(String sessionId, Iterator recvIt, String senderPhone, int sessionCounter){
		this.sessionId = sessionId;
		this.participantIdList = new ArrayList<String>();
		this.messageList = new ArrayList<MsnSessionMessage>();
		fillParticipantList(recvIt, senderPhone);
		//prepare the session title
		StringBuffer buffer= new StringBuffer(NOTIFICATION_TITLE);
		buffer.append(sessionCounter);
		this.sessionTitle= buffer.toString();
	}
	
	/**
	 * Instantiates a new msn session.
	 * 
	 * @param sessionId the session id
	 * @param participantsIds the participants ids
	 * @param sessionCounter the session counter
	 */
	MsnSession(String sessionId, List<String> participantsIds, int sessionCounter) {
		this.sessionId = sessionId;
		this.participantIdList = participantsIds;
		this.messageList = new ArrayList<MsnSessionMessage>();
		//prepare the session title
		StringBuffer buffer= new StringBuffer(NOTIFICATION_TITLE);
		buffer.append(sessionCounter);
		this.sessionTitle= buffer.toString();
	}
	
	/**
	 * Instantiates a new msn session.
	 * 
	 * @param session the session
	 */
	public MsnSession(MsnSession session) {
		this.sessionTitle = session.sessionTitle;
		this.sessionId = session.sessionId;
		this.messageList = new ArrayList<MsnSessionMessage>();
		
		for (MsnSessionMessage msg : session.messageList) {
			this.messageList.add( new MsnSessionMessage(msg));
		}
		
		this.participantIdList = new ArrayList<String>(session.participantIdList);
	}
	 
	
	//Fill the list of participants (Me is not included)
	/**
	 * Fill participant list.
	 * 
	 * @param receiversIt the receivers it
	 * @param senderPhoneNum the sender phone num
	 */
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
	/**
	 * Gets the session id as uri.
	 * 
	 * @return the session id as uri
	 */
	public Uri getSessionIdAsUri(){
		return Uri.fromParts(SESSION_ID_URI_SCHEME, SESSION_ID_URI_SSP, sessionId);
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	/**
	 * Adds the message.
	 * 
	 * @param msg the msg
	 */
	public void addMessage(MsnSessionMessage msg){		
			messageList.add(msg);		
	}
	
	//This method is not thread safe but it is synchronized by MsnSessionManager
	/**
	 * Gets the message list.
	 * 
	 * @return the message list
	 */
	public ArrayList<MsnSessionMessage> getMessageList(){
		ArrayList<MsnSessionMessage> list = null;		 
		list = new ArrayList<MsnSessionMessage>(messageList);
		return list;
	}
	
	
	/**
	 * Gets the all participant ids.
	 * 
	 * @return the all participant ids
	 */
	public List<String> getAllParticipantIds(){	
		return participantIdList;
	}
	
	/**
	 * Gets the participant count.
	 * 
	 * @return the participant count
	 */
	public int getParticipantCount(){
		return participantIdList.size()+1;
	}

	/**
	 * Gets the session id.
	 * 
	 * @return the session id
	 */
	public String getSessionId() {			
		return sessionId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){		
		return sessionTitle;	
	}
}