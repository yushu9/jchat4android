package com.tilab.msn;

public class GetAIDCommandBehaviour extends SynchCommandBehaviour {

	@Override
	protected void executeCommand() {
		commandResult = myAgent.getAID();
		isSuccess = true;
		this.exThrown = false;
	}

}
