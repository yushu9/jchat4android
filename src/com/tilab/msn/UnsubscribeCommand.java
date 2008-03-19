package com.tilab.msn;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class UnsubscribeCommand extends SynchCommandBehaviour {

	
	@Override
	protected void executeCommand() {
		cancelSubscription();
		deregisterMsnService();
	}
	
	
	private void cancelSubscription(){
		//we rebuild the subscription message here
		//TODO: Try to find a better way to perform this cancel
		DFAgentDescription myDescription = new DFAgentDescription();
		myDescription.setName(myAgent.getAID());
		//fill a msn service description
		ServiceDescription msnServiceDescription = new ServiceDescription();
		msnServiceDescription.setName(ContactsUpdaterBehaviour.msnDescName);
		msnServiceDescription.setType(ContactsUpdaterBehaviour.msnDescType);
		myDescription.addServices(msnServiceDescription);
		
		ACLMessage subcribeMsg = DFService.createSubscriptionMessage(myAgent, myAgent.getDefaultDF(), myDescription, null);
		ACLMessage unsubcribeMsg = DFService.createCancelMessage(myAgent, myAgent.getDefaultDF(), subcribeMsg);
		myAgent.send(unsubcribeMsg);
	}
	
	private void deregisterMsnService()  {
		
		
		try {
			DFService.deregister(myAgent);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isSuccess = false;
			exThrown = true;
			ex = e;
		}
	}
}
