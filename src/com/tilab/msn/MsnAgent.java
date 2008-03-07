package com.tilab.msn;

import java.util.ArrayList;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.wrapper.gateway.GatewayAgent;


public class MsnAgent extends GatewayAgent {

	
	
	protected void setup() {
		super.setup();

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
