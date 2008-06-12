package com.tilab.msn;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.wrapper.gateway.GatewayAgent;
import android.location.Location;


// TODO: Auto-generated Javadoc
/**
 * The Class MsnAgent.
 */
public class MsnAgent extends GatewayAgent {

	/** The Constant msnDescName. */
	public static final String msnDescName = "android-msn-service";
	
	/** The Constant msnDescType. */
	public static final String msnDescType = "android-msn";

	/** The Constant PROPERTY_NAME_LOCATION_LAT. */
	public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
	
	/** The Constant PROPERTY_NAME_LOCATION_LONG. */
	public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
	
	/** The Constant PROPERTY_NAME_LOCATION_ALT. */
	public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";

	/** The Constant CHAT_ONTOLOGY. */
	public static final String CHAT_ONTOLOGY= "chat_ontology";

	/** The my description. */
	private DFAgentDescription myDescription;
	
	/** The subscription message. */
	private ACLMessage subscriptionMessage;
	
	/** The contacts updater b. */
	private ContactsUpdaterBehaviour contactsUpdaterB;
	
	/** The message recv b. */
	private MessageReceiverBehaviour messageRecvB;

	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	//In this method we shall register to df and subscribe
	/* (non-Javadoc)
	 * @see jade.wrapper.gateway.GatewayAgent#setup()
	 */
	protected void setup() {
		super.setup();
		Thread.currentThread().getId();
        myLogger.log(Logger.INFO, "setup() called: My currentThread has this ID: " + Thread.currentThread().getId());
		myDescription = new DFAgentDescription();
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(msnDescName);
		msnServiceDescription.setType(msnDescType);
		myDescription.addServices(msnServiceDescription);

		//subscribe to DF
		subscriptionMessage = DFService.createSubscriptionMessage(this, this.getDefaultDF(), myDescription, null);
		
		Location curLoc = ContactManager.getInstance().getMyContactLocation();
		
		Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curLoc.getLatitude()));
		msnServiceDescription.addProperties(p);
		p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curLoc.getLongitude()));
		msnServiceDescription.addProperties(p);
		p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curLoc.getAltitude()));
		msnServiceDescription.addProperties(p);		
		myDescription.setName(this.getAID());

		try {
			myLogger.log(Logger.INFO, "Registering to DF!");
			DFService.register(this, myDescription);
		} catch (FIPAException e) {
		  e.printStackTrace();
		}

		//added behaviour to dispatch chat messages
		messageRecvB = new MessageReceiverBehaviour(); 
		addBehaviour(messageRecvB);
		String[] args = (String[])getArguments();
		myLogger.log(Logger.INFO, "UPDATE TIME: " + args[0]);
		contactsUpdaterB = new ContactsUpdaterBehaviour(Long.parseLong(args[0]));
		addBehaviour(contactsUpdaterB);
	}

	/**
	 * Gets the subscription message.
	 * 
	 * @return the subscription message
	 */
	public ACLMessage getSubscriptionMessage(){
		return subscriptionMessage;
	}

	/**
	 * Gets the agent description.
	 * 
	 * @return the agent description
	 */
	public DFAgentDescription getAgentDescription(){
		return myDescription;
	}
	

	//Here we will deregister and unsubscribe
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	protected void takeDown() {
		myLogger.log(Logger.INFO, "Starting agent takeDown() ");

		AID defaultDf = getDefaultDF();
		
		if (defaultDf != null){
		
			ACLMessage unsubcribeMsg = DFService.createCancelMessage(this, defaultDf, getSubscriptionMessage());
			send(unsubcribeMsg);

			myLogger.log(Logger.INFO, "DS Subscription Canceling message was sent!");

			try {
				defaultDf = getDefaultDF();
				if (defaultDf != null){
					DFService.deregister(this, myDescription);
					myLogger.log(Logger.INFO, "Deregistering from DF!");
				} else {
					myLogger.log(Logger.SEVERE, "Default DF was found null the scond time!! Some error happens during jade shutdown");
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); 
			}
		} else {
			myLogger.log(Logger.SEVERE, "Default DF was found null!! Some error happens during jade shutdown");
		}
	}

	//used to pass data to agent
	/* (non-Javadoc)
	 * @see jade.wrapper.gateway.GatewayAgent#processCommand(java.lang.Object)
	 */
	protected void processCommand(final Object command) {
	    if (command instanceof Behaviour){
			addBehaviour( (Behaviour) command);
			releaseCommand(command);
		}else if(command instanceof ContactsUIUpdater){
			contactsUpdaterB.setContactsUpdater((ContactsUIUpdater)command);
			releaseCommand(command);
		} 
	}

	/**
	 * The Class MessageReceiverBehaviour.
	 */
	private class MessageReceiverBehaviour extends CyclicBehaviour{

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {

			try {
				MessageTemplate mt = MessageTemplate.MatchOntology(CHAT_ONTOLOGY);
				ACLMessage msg = myAgent.receive(mt);
				//If a message is received
				if(msg != null){
					myLogger.log(Logger.INFO, msg.toString());
					MsnSessionManager sessionManager = MsnSessionManager.getInstance();
					Contact myContact = ContactManager.getInstance().getMyContact();
					//retrieve the session id
					String sessionId = msg.getConversationId();
					myLogger.log(Logger.INFO, "Received Message... session ID is " + sessionId);
					String senderPhoneNum = msg.getSender().getLocalName();	
					Contact senderContact = ContactManager.getInstance().getContact(senderPhoneNum);
					//Create a session Message from the received ACLMessage
					MsnSessionMessage sessionMessage = new MsnSessionMessage(msg);

					//Check if we can retrieve a session. If so we should have got a copy
					MsnSession currentSession = MsnSessionManager.getInstance().retrieveSession(sessionId);

					//If we have a new session
					if (currentSession == null) {
						//Create a new session with the specified ID
						sessionManager.addMsnSession(sessionId, msg.getAllReceiver(), senderPhoneNum);						    
						sessionManager.addMessageToSession(sessionId,sessionMessage);
						sessionManager.getNotificationManager().postNewMsgNotification(sessionId, sessionMessage);
						sessionManager.getNotificationManager().showToast("New message from " + senderContact.getName(),3000);

					} else {

						 
						//We have two more possible cases: 
						//1. We have a chat activity window opened for the session this message refers to -> update chat activity
						//2. We have no opened chat activity or a chat activity for a session different from the one this message refers to
						//   -> add a notification on status bar

						Object theLock = sessionManager.getChatUpdaterLock();
						synchronized(theLock) {
							ContactsUIUpdater chatActivityUpdater = sessionManager.getChatActivityUpdater();
							handleVisibleChat( sessionId, senderContact,sessionMessage, chatActivityUpdater, sessionManager.getNotificationManager());
						}


					}
				} else{
					block();
				}				

			}catch(Throwable t) {
				myLogger.log(Logger.SEVERE,"***  Uncaught Exception for agent " + myAgent.getLocalName() + "  ***",t);
			}		

		}

		/**
		 * Handle visible chat.
		 * 
		 * @param sessionId the session id
		 * @param senderCtn the sender ctn
		 * @param sessionMessage the session message
		 * @param updater the updater
		 * @param manager the manager
		 */
		private void handleVisibleChat( String sessionId, Contact senderCtn, MsnSessionMessage sessionMessage, ContactsUIUpdater updater,
										ChatSessionNotificationManager manager) {
			
			MsnSessionManager.getInstance().addMessageToSession(sessionId, sessionMessage);
			
			//If chat windows is visible
			if ( updater != null ){
				MsnSession updatedSession = (MsnSession)updater.retrieveExtraData();
				
				//check that the updated session is the same as the mesagge's one  
				if (updatedSession.getSessionId().equals(sessionId)){
					myLogger.log(Logger.INFO, "No session found! Creating the new session");
					//update the gui
					updater.postUIUpdate(sessionMessage);
				} else {
					//In this case we just need to add a notification for the message
					manager.postNewMsgNotification(sessionId, sessionMessage);
					MsnSessionManager.getInstance().getNotificationManager().showToast("New message from " + senderCtn.getName(),3000);
				}
				
			} else {
				
				//if not visible we just add a notification
				manager.postNewMsgNotification(sessionId, sessionMessage);
				MsnSessionManager.getInstance().getNotificationManager().showToast("New message from " + senderCtn.getName(),3000);
			}
		}
	}
}



