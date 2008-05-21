package com.tilab.msn;

import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;

public class MsnSessionManager {
	
	private static MsnSessionManager instance = new MsnSessionManager(); 
	private Map<String,MsnSession> sessionMap; 
	private ContactsUIUpdater chatActivityUpdater;
	private ChatSessionNotificationManager notificationManager;
	private Object theLock;
	
	public static final int MAX_MSN_SESSION_NUMBER =10; 
	
	private MsnSessionManager(){		
		chatActivityUpdater = null;
	}

	public static MsnSessionManager getInstance(){
		return instance;
	}
	
	public void initialize(Activity act){
		sessionMap = new HashMap<String, MsnSession>(MAX_MSN_SESSION_NUMBER);
		notificationManager = new ChatSessionNotificationManager(act);
		theLock = new Object();
	}
	
		
	public ChatSessionNotificationManager getNotificationManager(){
		return notificationManager;
	}
	
	//This shallbe used to add an MSN session on the local phone started by the MyContact.
	//The sessionId of the new session is returned
	public String startMsnSession(List<String> participantIds ){
		String sessionIdAsString = getSessionIdFromParticipants(participantIds);
		addMsnSession(sessionIdAsString, participantIds);
		return sessionIdAsString;
	}
	
	
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
	public synchronized void addMsnSession(String sessionId, List<String> participantIds){
		if (!sessionMap.containsKey(sessionId)){
			int sessionCounter= sessionMap.size()+1;
			MsnSession session = new MsnSession(sessionId, participantIds,sessionCounter); 
			//register it
			registerSession(sessionId, session);
		}
	}
	
	//This will create and register a new session initiated by another contact
	public synchronized void addMsnSession(String sessionId, Iterator recvIt, String senderPhone){
		if (!sessionMap.containsKey(sessionId)){
			int sessionCounter= sessionMap.size()+1;
			MsnSession session = new MsnSession(sessionId, recvIt, senderPhone, sessionCounter); 
			//register it
			registerSession(sessionId, session);
		}
	}
	
	
	public synchronized void removeMsnSession(String msnSession){		
			sessionMap.remove(msnSession);		
	}
	
	
	public  synchronized int getActiveSessionNumber(){
		
		int activeSessionNum = 0;	
		
			activeSessionNum = sessionMap.size();		
		
		return activeSessionNum;
	}
	
	public void registerSession(String sessionId, MsnSession session){		
			sessionMap.put(sessionId, session);		
	}
	
	public void registerChatActivityUpdater(ContactsUIUpdater updater){
		synchronized(theLock){
			chatActivityUpdater = updater;
		}
	}
	
	public Object getChatUpdaterLock(){
		return theLock;
	}
	
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
	
	public synchronized void addMessageToSession(String sessionId, MsnSessionMessage msg){
		MsnSession session = sessionMap.get(sessionId);
		
		if (session != null){
			session.addMessage(msg);
		}
	}
	
	
	public ContactsUIUpdater getChatActivityUpdater(){
		return chatActivityUpdater;
	}
	
	public synchronized void shutdown(){
		sessionMap.clear();
		chatActivityUpdater = null;
	}

	
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
   
