package com.tilab.msn;

import jade.core.behaviours.Behaviour;
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
