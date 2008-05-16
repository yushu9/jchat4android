package com.tilab.msn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;

public class MsnSessionManager {
	
	private static MsnSessionManager instance = new MsnSessionManager(); 
	private Map<String,MsnSession> sessionMap; 
	private ContactsUIUpdater chatActivityUpdater;
	private IncomingNotificationUpdater notificationUpdater;
		
	public static final int MAX_MSN_SESSION_NUMBER =10; 
	
	private MsnSessionManager(){		
		sessionMap = new HashMap<String, MsnSession>(MAX_MSN_SESSION_NUMBER);
		chatActivityUpdater = null;
	}

	public static MsnSessionManager getInstance(){
		
		return instance;
	}
	
	public synchronized void  registerNotificationUpdater(IncomingNotificationUpdater updater){
		notificationUpdater = updater;
	}

	
	public synchronized IncomingNotificationUpdater getNotificationUpdater(){
		return notificationUpdater;
	}
	
	//This will generate a sessionId  to be used for a new session and creates or retrieves a session with that ID
	//the boolean tell us if it is an update or not
	public MsnSession createNewMsnSession(List<Contact> participants){
		
		String sessionIdAsString = getSessionIdFromParticipants(participants);
		MsnSession session = createNewMsnSession(sessionIdAsString);
			
		for (Contact participant : participants) {
			session.addParticipant(participant);
		}
			
		
		return session;
	}
	
	private String getSessionIdFromParticipants(List<Contact> participants){		
		String myAgentId =	ContactManager.getInstance().getMyContact().getPhoneNumber();
		//The session id is computed by hashing agentNames
		int sessionIdAsInt= myAgentId.hashCode();
		
		for (Contact participant : participants) {
			int tmp = participant.getPhoneNumber().hashCode();
			sessionIdAsInt ^= tmp;
		}
		
		String sessionIdAsStr = String.valueOf(sessionIdAsInt);
		return sessionIdAsStr;
	}
	
	//This will create and register a new session initiated by another contact
	public MsnSession createNewMsnSession(String sessionId){
		int sessionCounter= sessionMap.size()+1;
		MsnSession session = new MsnSession(sessionId, sessionCounter); 
		//register it
		registerSession(sessionId, session);		
		return session;
	}
	
	
	public void removeMsnSession(String msnSession){		
			sessionMap.remove(msnSession);		
	}
	
	
	public  int getActiveSessionNumber(){
		
		int activeSessionNum = 0;	
		
			activeSessionNum = sessionMap.size();		
		
		return activeSessionNum;
	}
	
	public void registerSession(String sessionId, MsnSession session){		
			sessionMap.put(sessionId, session);		
	}
	
	public void registerChatActivityUpdater(ContactsUIUpdater updater){
		chatActivityUpdater = updater;
	}
	
	public MsnSession retrieveSession(String sessionId){		
		MsnSession session = null;
		
			session = sessionMap.get(sessionId);
				
		return session;
	}
	
	public List<MsnSession> getAllSessionByParticipant(String participant){
		
		List<MsnSession> sessionList = new ArrayList<MsnSession>();
		
		
			for (MsnSession session : sessionMap.values()) {
				List<Contact> contacts = session.getAllParticipants();
				for (Contact contact : contacts) {
					if (participant.equals(contact.getPhoneNumber()))							
						sessionList.add(session);
				}
			}		
		return sessionList;
	}
	
	
	//I shall assume that there cannot be two different sessions with the same participants
	public MsnSession getSessionByParticipantList(List<Contact> participantList){
		String sessionId = getSessionIdFromParticipants(participantList);
		MsnSession session = retrieveSession(sessionId);
		return session;
	}
	
	public ContactsUIUpdater getChatActivityUpdater(){
		return chatActivityUpdater;
	}
	
	public void shutdown(){
		sessionMap.clear();
		chatActivityUpdater = null;
	}
	 public Set<Contact> getAllParticipants(){
		 
		Set<Contact> allParticipantList = new HashSet<Contact>();
		
		for	(MsnSession session : sessionMap.values()){	
			allParticipantList.addAll(session.getAllParticipants());			
		}
		return allParticipantList;
	 }
}
   
