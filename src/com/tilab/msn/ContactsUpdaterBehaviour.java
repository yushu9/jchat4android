package com.tilab.msn;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.Logger;
import jade.util.leap.Iterator;
import android.location.Location;

public class ContactsUpdaterBehaviour extends OneShotBehaviour {
	
	public static final String msnDescName = "android-msn-service";
	public static final String msnDescType = "android-msn";
	
	private long msnUpdateTime;
	private ContactsUIUpdater updater;
	
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	public ContactsUpdaterBehaviour(long updateTime){
		msnUpdateTime = updateTime;
	}
	
	public void setContactsUpdater(ContactsUIUpdater up) {
		synchronized (this) {
			updater = up;
		}
	}
	
		
	@Override
	public void action()  {
		//first thing to do is to register on the df and save current location if any
		DFAgentDescription myDescription = new DFAgentDescription();
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(msnDescName);
		msnServiceDescription.setType(msnDescType);
		myDescription.addServices(msnServiceDescription);
		
		try {
			DFAgentDescription[] onlineContacts = DFService.search(myAgent, myDescription);
			
			updateContactList(onlineContacts);
		
			synchronized (ContactsUpdaterBehaviour.this) {
				if (updater != null){
					updater.postUIUpdate();
				}
			}
			
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		DFUpdaterBehaviour updater = new DFUpdaterBehaviour(myAgent,myDescription, msnUpdateTime);
		
		//subscribe to DF
		ACLMessage subscriptMsg = DFService.createSubscriptionMessage(myAgent, myAgent.getDefaultDF(), myDescription, null);
		DFSubscriptionBehaviour subBh = new DFSubscriptionBehaviour(myAgent,subscriptMsg);
		
		myAgent.addBehaviour(updater);
		myAgent.addBehaviour(subBh);
	}
	

	private void updateContactList(DFAgentDescription[] onlineContactsDescs) {
		// TODO Auto-generated method stub
		
		for (int i = 0; i < onlineContactsDescs.length; i++) {
			Iterator serviceDescIt = onlineContactsDescs[i].getAllServices();
			
			if (serviceDescIt.hasNext()){
				ServiceDescription desc = (ServiceDescription) serviceDescIt.next();
				
				Iterator propertyIt = desc.getAllProperties();
				Location loc = Helper.extractLocation(propertyIt);
				
				AID cId = onlineContactsDescs[i].getName();
				if (!cId.equals(myAgent.getAID())){
					//Create an online contact (or update it)
					ContactManager.getInstance().addOnlineContact(cId, loc);
				}
			}
		}
		
	}


	private static class Helper {	
	
		public static Location extractLocation(Iterator it){
			Location loc= new Location();
			
			while (it.hasNext()){
				Property p = (Property) it.next();
				
				String propertyName = p.getName();
				
				if (propertyName.equals(DFUpdaterBehaviour.PROPERTY_NAME_LOCATION_ALT)){
					double altitude = Double.parseDouble((String) p.getValue());		
					loc.setAltitude(altitude);
				} else if (propertyName.equals(DFUpdaterBehaviour.PROPERTY_NAME_LOCATION_LAT)){
					double latitude = Double.parseDouble((String) p.getValue());		
					loc.setLatitude(latitude);
				} else if (propertyName.equals(DFUpdaterBehaviour.PROPERTY_NAME_LOCATION_LONG)){
					double longitude = Double.parseDouble((String) p.getValue());		
					loc.setLongitude(longitude);
				} 
			}
			
			return loc;
		}
	}
	
	
	private class DFSubscriptionBehaviour extends SubscriptionInitiator 
    {

		public DFSubscriptionBehaviour(Agent agent, ACLMessage msg) {
			super(agent, msg);
		}
		
		
		protected void handleInform(ACLMessage inform) {

			myLogger.log(Logger.FINE, " Notification received from DF");

			try {

				DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());

				if (results.length > 0) {

					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
						AID contactAID = dfd.getName();
						// Do something only if the notification deals with an agent different from the current one
						if (!contactAID.equals(myAgent.getAID())){

							
							Iterator serviceIter = dfd.getAllServices();
							
							//Registered or updated
							if (serviceIter.hasNext()){
								ServiceDescription serviceDesc = (ServiceDescription) serviceIter.next();
								
								Iterator propertyIt = serviceDesc.getAllProperties(); 
								Location loc = Helper.extractLocation(propertyIt);
								
								ContactManager.getInstance().addOnlineContact(contactAID, loc);
							} else {
								ContactManager.getInstance().setOffline(contactAID);
							}
						}
					}
					
					synchronized (ContactsUpdaterBehaviour.this) {
						if (updater != null){
							updater.postUIUpdate();
						}
					}
				}
				
				
			}
			catch (FIPAException fe) {
				myLogger.log(Logger.WARNING, "See printstack for Exception.", fe);
				fe.printStackTrace();
			}
		}
	}


	
private class DFUpdaterBehaviour extends TickerBehaviour {

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
	public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
	public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";

	private boolean isFirstTime;
	private DFAgentDescription agentDescription;
	private ServiceDescription serviceDescription;
	
	public DFUpdaterBehaviour(Agent a, DFAgentDescription desc, long period) {
		super(a, period);
		// TODO Auto-generated constructor stub
		isFirstTime=true;
		agentDescription = desc;
		serviceDescription = (ServiceDescription) agentDescription.getAllServices().next();
	}

	

	
	
	@Override
	protected void onTick() {
		
		//retrieve current location
		Contact myContact = ContactManager.getInstance().getMyContact();
		
			agentDescription.setName(myAgent.getAID());
		
			Location curLoc = myContact.getLocation();
			
			Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curLoc.getLatitude()));
			serviceDescription.addProperties(p);
			p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curLoc.getLongitude()));
			serviceDescription.addProperties(p);
			p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curLoc.getAltitude()));
			serviceDescription.addProperties(p);
			
			
			try {
				//register with DF
				//FIXME: what happens if registration goes bad and an exception is thrown??
				//Must find a way to notify to the application!!! 
				if (isFirstTime) {
					DFService.register(myAgent, agentDescription);
					isFirstTime=false;
				} else {
					if (myContact.hasMoved()) {
						DFService.modify(myAgent, agentDescription);
					}
				}
			
				
				
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
		
		}

	}
}

