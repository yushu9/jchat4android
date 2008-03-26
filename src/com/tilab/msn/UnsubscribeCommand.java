package com.tilab.msn;

import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class UnsubscribeCommand extends SynchCommandBehaviour {

	private Behaviour behaviourToRemove;
	
	public UnsubscribeCommand(Behaviour bh){
		behaviourToRemove = bh;
	}
	
	@Override
	protected void executeCommand() {
		myAgent.removeBehaviour(behaviourToRemove);
		MsnAgent agent = (MsnAgent) myAgent;
		ACLMessage unsubcribeMsg = DFService.createCancelMessage(myAgent, myAgent.getDefaultDF(), agent.getSubscriptionMessage() );
		myAgent.send(unsubcribeMsg);

		try {
			DFService.deregister(myAgent);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
		
	}
	
	
	
}
