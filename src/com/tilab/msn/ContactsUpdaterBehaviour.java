package com.tilab.msn;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.MyLocationOverlay;

import android.location.Location;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentDescriptor;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;
import jade.util.leap.Iterator;

public class ContactsUpdaterBehaviour extends OneShotBehaviour {

	private String msnDescName;
	private String msnDescType;
	private long msnUpdateTime;
	
	public ContactsUpdaterBehaviour(String descName, String descType, long updateTime){
		msnDescName = descName;
		msnDescType = descType;
		msnUpdateTime = updateTime;
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		DFUpdaterBehaviour updater = new DFUpdaterBehaviour(myAgent, msnUpdateTime);
		myAgent.addBehaviour(updater);
	}
	
	
private class DFUpdaterBehaviour extends TickerBehaviour {

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	
	private final String PROPERTY_NAME_LOCATION_LAT="Latitude";
	private final String PROPERTY_NAME_LOCATION_LONG="Longitude";
	private final String PROPERTY_NAME_LOCATION_ALT="Altitude";

	private boolean isFirstTime;
	
	public DFUpdaterBehaviour(Agent a, long period) {
		super(a, period);
		// TODO Auto-generated constructor stub
		isFirstTime=true;
	}

	
	private Location extractLocation(Iterator it){
		Location loc= new Location();
		
		while (it.hasNext()){
			Property p = (Property) it.next();
			
			String propertyName = p.getName();
			
			if (propertyName.equals(PROPERTY_NAME_LOCATION_ALT)){
				double altitude = ((Double)p.getValue()).doubleValue();
				loc.setAltitude(altitude);
			} else if (propertyName.equals(PROPERTY_NAME_LOCATION_LAT)){
				double latitude = ((Double)p.getValue()).doubleValue();
				loc.setLatitude(latitude);
			} else if (propertyName.equals(PROPERTY_NAME_LOCATION_LONG)){
				double longitude = ((Double)p.getValue()).doubleValue();
				loc.setLongitude(longitude);
			} 
		}
		
		return loc;
	}
	
	private List<Contact> extractOtherContactsList(DFAgentDescription[] descriptions){
		
		List<Contact> list = new ArrayList<Contact>();
		
		//for each agent description
		for (int i = 0; i < descriptions.length; i++) {
			
			//retrieve Agent AID
			AID agentAID = descriptions[i].getName();
			Location loc = null;
			
			//skip my description
			if (!agentAID.equals(myAgent.getAID())) {
				//retrieve the list of services (should be only one!)
				Iterator serviceIt = descriptions[i].getAllServices();
				
				//iterate through the list (just to be sure)
				while (serviceIt.hasNext()){
					//check that service is ours
					ServiceDescription serviceDesc = (ServiceDescription)serviceIt.next();
					if (serviceDesc.getName().equals(msnDescName) && serviceDesc.getType().equals(msnDescType)){
						Iterator propertyIt = serviceDesc.getAllProperties(); 
						loc = extractLocation(propertyIt);
					}
				}
				
				//create a contact with the info
				Contact c = new Contact(agentAID);
				c.setLocation(loc);
				
				list.add(c);
			}
		}
		
		return list;
	}
	
	@Override
	protected void onTick() {
		
		//first thing to do is to register on the df and save current location if any
		DFAgentDescription myDescription = new DFAgentDescription();
		myDescription.setName(myAgent.getAID());
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(msnDescName);
		msnServiceDescription.setType(msnDescType);
		
		//retrieve current location
		Contact myContact = ContactManager.getInstance().getMyContact();
		Location curLoc = myContact.getLocation();
		
		Property p = new Property(PROPERTY_NAME_LOCATION_LAT,new Double(curLoc.getLatitude()));
		msnServiceDescription.addProperties(p);
		p = new Property(PROPERTY_NAME_LOCATION_LONG,new Double(curLoc.getLongitude()));
		msnServiceDescription.addProperties(p);
		p= new Property(PROPERTY_NAME_LOCATION_ALT,new Double(curLoc.getAltitude()));
		msnServiceDescription.addProperties(p);
		
		//put service desc into agent desc
		myDescription.addServices(msnServiceDescription);
		
		try {
			//register with DF
			//FIXME: what happens if registration goes bad and an exception is thrown??
			//Must find a way to notify to the application!!! 
			if (isFirstTime) {
				DFService.register(myAgent, myDescription);
				isFirstTime=false;
			} else {
				DFService.modify(myAgent, myDescription);
			}
			//Ok, now I must query the DF to get other contacts and their positions
			//first thing to do is to register on the df and save current location if any
			DFAgentDescription targetDesc = new DFAgentDescription();
			//fill a msn service description
			ServiceDescription msnTargetServiceDescription = new ServiceDescription();
			msnServiceDescription.setName(msnDescName);
			msnServiceDescription.setType(msnDescType);
			targetDesc.addServices(msnTargetServiceDescription);
			
			//FIXME: what happens if the submission fails?? Must notify in some way to the application
			DFAgentDescription[] descriptions = DFService.search(myAgent, targetDesc);
		
		
			//retrieve the contact list parsing df description
			List<Contact> newContactList = extractOtherContactsList(descriptions);
			//update contact list
			ContactManager.getInstance().updateOtherContactList(newContactList);
			
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		
	}

 }
}
