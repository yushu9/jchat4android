package com.tilab.msn;

import java.util.List;

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
		try {
		//first thing to do is to register on the df and save current location if any
		DFAgentDescription myDescription = new DFAgentDescription();
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(MsnAgent.msnDescName);
		msnServiceDescription.setType(MsnAgent.msnDescType);
		myDescription.addServices(msnServiceDescription);

		ContactManager.getInstance().resetModifications();
		
		DFAgentDescription[] onlineContacts = DFService.search(myAgent, myDescription);

		updateContactList(onlineContacts);

		synchronized (ContactsUpdaterBehaviour.this) {
			if (updater != null){
				ContactListChanges changes = ContactManager.getInstance().getModifications();
				updater.postUIUpdate(changes);
			}
		}

		

		DFUpdaterBehaviour updater = new DFUpdaterBehaviour(myAgent,msnUpdateTime);
		MsnAgent agent = (MsnAgent) myAgent;
		DFSubscriptionBehaviour subBh = new DFSubscriptionBehaviour(myAgent,agent.getSubscriptionMessage());

		myAgent.addBehaviour(updater);
		myAgent.addBehaviour(subBh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			myLogger.log(Logger.SEVERE, "Severe error: ", e);
			e.printStackTrace();
		}

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
			ContactManager.getInstance().resetModifications();
			
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
								List<MsnSession> sessions = MsnSessionManager.getInstance().getAllSessionByParticipant(contactAID.getName());
								for (MsnSession msnSession : sessions) {
									Contact c = ContactManager.getInstance().getContactByAgentId(contactAID.getLocalName());
									if (c != null)
										MsnSessionManager.getInstance().getChatActivityUpdater().postUIUpdate(c.getName());
								}
								ContactManager.getInstance().setOffline(contactAID);

							}
						}
					}

					synchronized (ContactsUpdaterBehaviour.this) {
						if (updater != null){
							ContactListChanges changes = ContactManager.getInstance().getModifications();
							updater.postUIUpdate(changes);
						}
					}
				}


			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "See printstack for Exception.", e);
				e.printStackTrace();
			}
		}
	}



	private class DFUpdaterBehaviour extends TickerBehaviour {

		private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

		public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
		public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
		public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";


		public DFUpdaterBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {

			try {
				MsnAgent agent = (MsnAgent) myAgent;
				DFAgentDescription description = agent.getAgentDescription();

				ServiceDescription serviceDescription = (ServiceDescription) description.getAllServices().next();
				serviceDescription.clearAllProperties();

				//retrieve current location
				Contact myContact = ContactManager.getInstance().getMyContact();
				Location curLoc = myContact.getLocation();


				Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curLoc.getLatitude()));
				serviceDescription.addProperties(p);
				p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curLoc.getLongitude()));
				serviceDescription.addProperties(p);
				p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curLoc.getAltitude()));
				serviceDescription.addProperties(p);


				//update df entry
				//FIXME: what happens if registration goes bad and an exception is thrown??
				//Must find a way to notify to the application!!! 
				if (myContact.hasMoved()) {
					DFService.modify(myAgent, description);
				}

			} catch (FIPAException fe) {
				myLogger.log(Logger.SEVERE, "Error in updating DF", fe);
			}
			catch(Exception e) {
				myLogger.log(Logger.SEVERE,"***  Uncaught Exception for agent " + myAgent.getLocalName() + "  ***",e);
			}


		}

	}
}

