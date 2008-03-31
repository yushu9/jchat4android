package com.tilab.msn;

import android.location.Location;

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


public class MsnAgent extends GatewayAgent {

	public static final String msnDescName = "android-msn-service";
	public static final String msnDescType = "android-msn";

	public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
	public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
	public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";

	public static final String CHAT_ONTOLOGY= "chat_ontology";
	
	private DFAgentDescription myDescription;
	private ACLMessage subscriptionMessage;
	private ContactsUpdaterBehaviour contactsUpdaterB;
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	//In this method we shall register to df and subscribe
	protected void setup() {
		super.setup();

		myDescription = new DFAgentDescription();
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(msnDescName);
		msnServiceDescription.setType(msnDescType);
		myDescription.addServices(msnServiceDescription);

		//subscribe to DF
		subscriptionMessage = DFService.createSubscriptionMessage(this, this.getDefaultDF(), myDescription, null);

		Location curLoc = ContactManager.getInstance().getMyContact().getLocation();
		
		Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curLoc.getLatitude()));
		msnServiceDescription.addProperties(p);
		p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curLoc.getLongitude()));
		msnServiceDescription.addProperties(p);
		p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curLoc.getAltitude()));
		msnServiceDescription.addProperties(p);		
		myDescription.setName(this.getAID());
		
		try {
			DFService.register(this, myDescription);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//added behaviour to dispatch chat messages
		addBehaviour(new MessageReceiverBehaviour());
		String[] args = (String[])getArguments();
		myLogger.log(Logger.INFO, "UPDATE TIME: " + args[0]);
		contactsUpdaterB = new ContactsUpdaterBehaviour(Long.parseLong(args[0]));
		addBehaviour(contactsUpdaterB);
	}
	
	public ACLMessage getSubscriptionMessage(){
		return subscriptionMessage;
	}
	
	public DFAgentDescription getAgentDescription(){
		return myDescription;
	}
	
	
	//Here we will deregister and unsubscribe
	protected void takeDown() {
		ACLMessage unsubcribeMsg = DFService.createCancelMessage(this, getDefaultDF(), getSubscriptionMessage());
		send(unsubcribeMsg);

		try {
			DFService.deregister(this, myDescription);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}	
	}
	
	//used to pass data to agent
	protected void processCommand(final Object command) {
		
		if (command instanceof SynchCommandBehaviour) {
			addBehaviour( (Behaviour) command);
		}else if (command instanceof Behaviour){
			addBehaviour( (Behaviour) command);
			releaseCommand(command);
		}else if(command instanceof ContactsUIUpdater){
			contactsUpdaterB.setContactsUpdater((ContactsUIUpdater)command);
			releaseCommand(command);
		}
	}
	
	private class MessageReceiverBehaviour extends CyclicBehaviour{

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchOntology(CHAT_ONTOLOGY);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				myLogger.log(Logger.INFO, msg.toString());
			}else{
				block();
			}
		}
	}
	
	
}

