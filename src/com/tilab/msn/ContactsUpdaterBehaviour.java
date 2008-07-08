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
 * Main behaviour executed by the MsnAgent during its setup.
 * <p>
 * It basically performs two main operations:
 * <ul>
 * 	<li> Periodically update location of phone owner contact (updated by mock GPS)on the DF (JADE Directory Facilitator)
 *  <li> Periodically update other contact's locations anytime we receive a notification from the DF
 * </ul>
 * 
 * Moreover it sends events to the gui to issue a refresh, anytime something in the contact list changes.
 *
 * @author Cristina Cuccè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */

public class ContactsUpdaterBehaviour extends OneShotBehaviour {

	/** 
	 * Time between each update of the my contact location on the DF. 
	 * Read from configuration file 
	 */
	private long msnUpdateTime;
	
	

	/** 
	 * Instance of the Jade Logger for debugging 
	 */
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	/**
	 * Instantiates a new contacts updater behaviour.
	 * 
	 * @param updateTime the update time
	 */
	public ContactsUpdaterBehaviour(long updateTime){
		msnUpdateTime = updateTime;
	}

	/**
	 * Overrides the Behaviour.action() method. This method is executed by the agent thread.
	 * It basically defines two sub behaviours, which are in charge of periodically updating the DF and receiving 
	 * DF notifications.
	 */
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
		event.addParam(MsnEventMgr.Event.VIEW_REFRESH_PARAM_LISTOFCHANGES, ContactManager.getInstance().getModifications());
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
	 * Utility method that updates the contact list extracting contact info and location from DF descriptions.
	 * 
	 * @param onlineContactsDescs array of {@link DFAgentDescription} objects that define services as results of a DF query
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
	 * Static class that provides some Helper methods useful for extracting contact data
	 */
	private static class Helper {	

		/**
		 * Extract a Location from a list of properties extracted from Service description
		 * 
		 * @param it iterator over the list of properties
		 * @return the location on the map described by this set of properties
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
	 * Subbehaviour that handles notification messages for modificatio
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
								event.addParam(MsnEventMgr.Event.CONTACT_DISCONNECT_PARAM_CONTACTNAME, c.getName());
								MsnEventMgr.getInstance().fireEvent(event);								
							}
						}
					}
					
					MsnEventMgr.Event event = MsnEventMgr.getInstance().createEvent(MsnEventMgr.Event.VIEW_REFRESH_EVENT);
					event.addParam(MsnEventMgr.Event.VIEW_REFRESH_PARAM_LISTOFCHANGES, ContactManager.getInstance().getModifications());
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

