package com.tilab.msn;

import android.location.Location;


import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.gateway.GatewayAgent;


public class MsnAgent extends GatewayAgent {

	public static final String msnDescName = "android-msn-service";
	public static final String msnDescType = "android-msn";

	public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
	public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
	public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";

	
	private DFAgentDescription myDescription;
	private ACLMessage subscriptionMessage;
	
	
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

	}
	
	public ACLMessage getSubscriptionMessage(){
		return subscriptionMessage;
	}
	
	public DFAgentDescription getAgentDescription(){
		return myDescription;
	}
	
	
	//Here we will deregister and unsubscribe
	protected void takeDown() {
	
		
	}
	
	//used to pass data to agent
	protected void processCommand(final Object command) {
		
		if (command instanceof SynchCommandBehaviour) {
			addBehaviour( (Behaviour) command);
		}else if (command instanceof Behaviour){
			addBehaviour( (Behaviour) command);
			releaseCommand(command);
		}
	}
	
}

