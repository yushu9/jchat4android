package com.tilab.msn;

import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;

// TODO: Auto-generated Javadoc
/**
 * The Class MsnSessionManager.
 */
public class MsnSessionManager {
	
	/** The instance. */
	private static MsnSessionManager instance = new MsnSessionManager(); 
	
	/** The session map. */
	private Map<String,MsnSession> sessionMap; 
	
	/** The chat activity updater. */
	private ContactsUIUpdater chatActivityUpdater;
	
	/** The notification manager. */
	private ChatSessionNotificationManager notificationManager;
	
	/** The lock. */
	private Object theLock;
	
	/** The Constant MAX_MSN_SESSION_NUMBER. */
	public static final int MAX_MSN_SESSION_NUMBER =10; 
	
	/**
	 * Instantiates a new msn session manager.
	 */
	private MsnSessionManager(){		
		chatActivityUpdater = null;
	}

	/**
	 * Gets the single instance of MsnSessionManager.
	 * 
	 * @return single instance of MsnSessionManager
	 */
	public static MsnSessionManager getInstance(){
		return instance;
	}
	
	/**
	 * Initialize.
	 * 
	 * @param act the act
	 */
	public void initialize(Activity act){
		sessionMap = new HashMap<String, MsnSession>(MAX_MSN_SESSION_NUMBER);
		notificationManager = new ChatSessionNotificationManager(act);
		theLock = new Object();
	}
	
		
	/**
	 * Gets the notification manager.
	 * 
	 * @return the notification manager
	 */
	public ChatSessionNotificationManager getNotificationManager(){
		return notificationManager;
	}
	
	//This shallbe used to add an MSN session on the local phone started by the MyContact.
	//The sessionId of the new session is returned
	/**
	 * Start msn session.
	 * 
	 * @param participantIds the participant ids
	 * 
	 * @return the string
	 */
	public String startMsnSession(List<String> participantIds ){
		String sessionIdAsString = getSessionIdFromParticipants(participantIds);
		addMsnSession(sessionIdAsString, participantIds);
		return sessionIdAsString;
	}
	
	
	/**
	 * Gets the session id from participants.
	 * 
	 * @param participantIds the participant ids
	 * 
	 * @return the session id from participants
	 */
	public String getSessionIdFromParticipants(List<String> participantIds){		
		String myAgentId =	ContactManager.getInstance().getMyContact().getPhoneNumber();
		//The session id is computed by hashing agentNames
		int sessionIdAsInt= myAgentId.hashCode();
		
		for (String participantId : participantIds) {
			int tmp = participantId.hashCode();
			sessionIdAsInt ^= tmp;
		}
		
		String sessionIdAsStr = String.valueOf(sessionIdAsInt);
		return sessionIdAsStr;
	}
	
	//This will create and register a new session initiated by another contact
	/**
	 * Adds the msn session.
	 * 
	 * @param sessionId the session id
	 * @param participantIds the participant ids
	 */
	public synchronized void addMsnSession(String sessionId, List<String> participantIds){
		if (!sessionMap.containsKey(sessionId)){
			int sessionCounter= sessionMap.size()+1;
			MsnSession session = new MsnSession(sessionId, participantIds,sessionCounter); 
			//register it
			registerSession(sessionId, session);
		}
	}
	
	//This will create and register a new session initiated by another contact
	/**
	 * Adds the msn session.
	 * 
	 * @param sessionId the session id
	 * @param recvIt the recv it
	 * @param senderPhone the sender phone
	 */
	public synchronized void addMsnSession(String sessionId, Iterator recvIt, String senderPhone){
		if (!sessionMap.containsKey(sessionId)){
			int sessionCounter= sessionMap.size()+1;
			MsnSession session = new MsnSession(sessionId, recvIt, senderPhone, sessionCounter); 
			//register it
			registerSession(sessionId, session);
		}
	}
	
	
	/**
	 * Removes the msn session.
	 * 
	 * @param msnSession the msn session
	 */
	public synchronized void removeMsnSession(String msnSession){		
			sessionMap.remove(msnSession);		
	}
	
	
	/**
	 * Gets the active session number.
	 * 
	 * @return the active session number
	 */
	public  synchronized int getActiveSessionNumber(){
		
		int activeSessionNum = 0;	
		
			activeSessionNum = sessionMap.size();		
		
		return activeSessionNum;
	}
	
	/**
	 * Register session.
	 * 
	 * @param sessionId the session id
	 * @param session the session
	 */
	public void registerSession(String sessionId, MsnSession session){		
			sessionMap.put(sessionId, session);		
	}
	
	/**
	 * Register chat activity updater.
	 * 
	 * @param updater the updater
	 */
	public void registerChatActivityUpdater(ContactsUIUpdater updater){
		synchronized(theLock){
			chatActivityUpdater = updater;
		}
	}
	
	/**
	 * Gets the chat updater lock.
	 * 
	 * @return the chat updater lock
	 */
	public Object getChatUpdaterLock(){
		return theLock;
	}
	
	/**
	 * Retrieve session.
	 * 
	 * @param sessionId the session id
	 * 
	 * @return the msn session
	 */
	public MsnSession retrieveSession(String sessionId){		
	
		MsnSession session=null;
		MsnSession copyOfSession = null;
		
		synchronized (this) {
			session = sessionMap.get(sessionId);
			if (session != null)
				copyOfSession = new MsnSession(session);
		}
		
		return copyOfSession;
			
	}
	
	/**
	 * Adds the message to session.
	 * 
	 * @param sessionId the session id
	 * @param msg the msg
	 */
	public synchronized void addMessageToSession(String sessionId, MsnSessionMessage msg){
		MsnSession session = sessionMap.get(sessionId);
		
		if (session != null){
			session.addMessage(msg);
		}
	}
	
	
	/**
	 * Gets the chat activity updater.
	 * 
	 * @return the chat activity updater
	 */
	public ContactsUIUpdater getChatActivityUpdater(){
		return chatActivityUpdater;
	}
	
	/**
	 * Shutdown.
	 */
	public synchronized void shutdown(){
		sessionMap.clear();
		chatActivityUpdater = null;
	}

	
	/**
	 * Gets the all participant ids.
	 * 
	 * @return the all participant ids
	 */
	public synchronized Set<String> getAllParticipantIds() {
		//define a set to avoid duplicates
		Set<String> idSet = new HashSet<String>();
		ArrayList<MsnSession> c = null;
		
		synchronized(this){
			c= new ArrayList<MsnSession>( sessionMap.values()); 
		}
		
		for (MsnSession session : c) {
			List<String> partIds = session.getAllParticipantIds();
			idSet.addAll(partIds);
		}
		
		return idSet;
	}
	
	
}
   
