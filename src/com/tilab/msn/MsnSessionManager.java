package com.tilab.msn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MsnSessionManager {
	
	private static MsnSessionManager instance; 
	private Map<String,MsnSession> sessionMap; 
	private Map<String,ContactsUIUpdater> updatersMap;
	private IncomingNotificationUpdater notificationUpdater;
	
	public static final int MAX_MSN_SESSION_NUMBER =10; 
	
	private MsnSessionManager(){
		sessionMap = new HashMap<String, MsnSession>(MAX_MSN_SESSION_NUMBER);
		updatersMap = new HashMap<String, ContactsUIUpdater>(MAX_MSN_SESSION_NUMBER);
	}

	public static MsnSessionManager getInstance(){
		if (instance == null){
			instance = new MsnSessionManager();
		}
		return instance;
	}
	
	public void registerNotificationUpdater(IncomingNotificationUpdater updater){
		notificationUpdater = updater;
	}

	
	public IncomingNotificationUpdater getNotificationUpdater(){
		return notificationUpdater;
	}
	
	//This will generate a sessionId  to be used for a new session
	public MsnSession createNewMsnSession(List<Contact> participants){
	
		String myAgentId =	ContactManager.getInstance().getMyContact().getAgentContact();
		//The session id is computed by hashing agentNames
		int sessionId= myAgentId.hashCode();
		
		for (Contact participant : participants) {
			int tmp = participant.hashCode();
			sessionId ^= tmp;
		}
		
		String sessionIdAsString = String.valueOf(sessionId);
		MsnSession session = createNewMsnSession(sessionIdAsString);
	
		for (Contact participant : participants) {
			session.addParticipant(participant);
		}
				
		return session;
	}
	
	//This will create and register a new session initiated by the  
	public MsnSession createNewMsnSession(String sessionId){
		
		MsnSession session = new MsnSession(sessionId); 
		//register it
		registerSession(sessionId.toString(), session);
		
		return session;
	}
	
	
	public void removeMsnSession(String msnSession){
		synchronized (this) {
			sessionMap.remove(msnSession);
			updatersMap.remove(msnSession);
		}
	}
	
	
	public int getActiveSessionNumber(){
		
		int activeSessionNum = 0;
		
		synchronized (sessionMap) {
			activeSessionNum = sessionMap.size();
		}
		
		return activeSessionNum;
	}
	
	public void registerSession(String sessionId, MsnSession session){
		synchronized (sessionMap) {
			sessionMap.put(sessionId, session);
		}
	}
	
	public void registerMsgReceivedUpdater(String sessionId, ContactsUIUpdater updater){
		synchronized (updatersMap) {
			updatersMap.put(sessionId, updater);
		}
	}
	
	public MsnSession retrieveSession(String sessionId){
		
		MsnSession session = null;
		
		synchronized (sessionMap) {
			session = sessionMap.get(sessionId);
		}
		
		return session;
	}
	
	public List<MsnSession> getAllSessionByParticipant(String participant){
		
		List<MsnSession> sessionList = new ArrayList<MsnSession>();
		
		synchronized (sessionMap) {
			for (MsnSession session : sessionMap.values()) {
				List<Contact> contacts = session.getAllParticipants();
				for (Contact contact : contacts) {
					if (participant.equals(contact.getAgentContact()))
						sessionList.add(session);
				}
			}
		}
		
		return sessionList;
	}
	
	public ContactsUIUpdater retrieveMsgReceivedUpdater(String sessionId){
		ContactsUIUpdater updater=null;
		
		synchronized (instance) {
			updater =  updatersMap.get(sessionId);
		}
		
		return updater;
	}
}
