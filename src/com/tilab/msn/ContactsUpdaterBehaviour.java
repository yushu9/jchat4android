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


/**
 * The Class ContactsUpdaterBehaviour.
 */
public class ContactsUpdaterBehaviour extends OneShotBehaviour {

	/** The msn update time. */
	private long msnUpdateTime;
	
	

	/** The my logger. */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	/**
	 * Instantiates a new contacts updater behaviour.
	 * 
	 * @param updateTime the update time
	 */
	public ContactsUpdaterBehaviour(long updateTime){
		msnUpdateTime = updateTime;
	}

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

		MsnEventMgr.Event event = MsnEventMgr.getInstance().createEvent(MsnEventMgr.Event.VIEW_REFRESH_EVENT);
		event.addParam("ListOfChanges", ContactManager.getInstance().getModifications());
		MsnEventMgr.getInstance().fireEvent(event);

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


	/**
	 * Update contact list.
	 * 
	 * @param onlineContactsDescs the online contacts descs
	 */
	private void updateContactList(DFAgentDescription[] onlineContactsDescs) {

		for (int i = 0; i < onlineContactsDescs.length; i++) {
			Iterator serviceDescIt = onlineContactsDescs[i].getAllServices();

			if (serviceDescIt.hasNext()){
				ServiceDescription desc = (ServiceDescription) serviceDescIt.next();

				Iterator propertyIt = desc.getAllProperties();
				Location loc = Helper.extractLocation(propertyIt);

				AID cId = onlineContactsDescs[i].getName();
				if (!cId.equals(myAgent.getAID())){
					//Create an online contact (or update it)
					String phoneNumber = cId.getLocalName();
				    ContactManager.getInstance().addOrUpdateOnlineContact(phoneNumber, loc);
					
				}
			}
		}

	}


	/**
	 * The Class Helper.
	 */
	private static class Helper {	

		/**
		 * Extract location.
		 * 
		 * @param it the it
		 * 
		 * @return the location
		 */
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


	/**
	 * The Class DFSubscriptionBehaviour.
	 */
	private class DFSubscriptionBehaviour extends SubscriptionInitiator 
	{

		/**
		 * Instantiates a new dF subscription behaviour.
		 * 
		 * @param agent the agent
		 * @param msg the msg
		 */
		public DFSubscriptionBehaviour(Agent agent, ACLMessage msg) {
			super(agent, msg);
		}


		/* (non-Javadoc)
		 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
		 */
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
								
								if (loc.getAltitude() != Double.POSITIVE_INFINITY && 
									loc.getLongitude() != Double.POSITIVE_INFINITY && 
									loc.getLatitude()!=Double.POSITIVE_INFINITY){
									String phoneNum = contactAID.getLocalName();                                 
									ContactManager.getInstance().addOrUpdateOnlineContact(phoneNum, loc);								
								}
								
							} else {
								String phoneNumber = contactAID.getLocalName();
								Contact c = ContactManager.getInstance().getContact(phoneNumber);
								ContactManager.getInstance().setContactOffline(phoneNumber);
								MsnEventMgr.Event event = MsnEventMgr.getInstance().createEvent(MsnEventMgr.Event.CONTACT_DISCONNECT_EVENT);
								event.addParam("ContactName", c.getName());
								MsnEventMgr.getInstance().fireEvent(event);								
							}
						}
					}
					
					MsnEventMgr.Event event = MsnEventMgr.getInstance().createEvent(MsnEventMgr.Event.VIEW_REFRESH_EVENT);
					event.addParam("ListOfChanges", ContactManager.getInstance().getModifications());
					MsnEventMgr.getInstance().fireEvent(event);
				}


			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "See printstack for Exception.", e);
				e.printStackTrace();
			}
		}
	}



	/**
	 * The Class DFUpdaterBehaviour.
	 */
	private class DFUpdaterBehaviour extends TickerBehaviour {

		/** The my logger. */
		private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

		/** The Constant PROPERTY_NAME_LOCATION_LAT. */
		public static final String PROPERTY_NAME_LOCATION_LAT="Latitude";
		
		/** The Constant PROPERTY_NAME_LOCATION_LONG. */
		public static final String PROPERTY_NAME_LOCATION_LONG="Longitude";
		
		/** The Constant PROPERTY_NAME_LOCATION_ALT. */
		public static final String PROPERTY_NAME_LOCATION_ALT="Altitude";


		/**
		 * Instantiates a new dF updater behaviour.
		 * 
		 * @param a the a
		 * @param period the period
		 */
		public DFUpdaterBehaviour(Agent a, long period) {
			super(a, period);
		}

		
		protected void onTick() {

			try {
				MsnAgent agent = (MsnAgent) myAgent;
				DFAgentDescription description = agent.getAgentDescription();

				ServiceDescription serviceDescription = (ServiceDescription) description.getAllServices().next();
				serviceDescription.clearAllProperties();

				//retrieve
				ContactLocation curMyLoc = ContactManager.getInstance().getMyContactLocation();				

				Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curMyLoc.getLatitude()));
				serviceDescription.addProperties(p);
				p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curMyLoc.getLongitude()));
				serviceDescription.addProperties(p);
				p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curMyLoc.getAltitude()));
				serviceDescription.addProperties(p);

				//update df entry				
				if (curMyLoc.hasMoved()){						
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

