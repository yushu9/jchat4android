package com.tilab.msn;

import jade.domain.DFService;
import jade.domain.FIPAException;

public class UnsubscribeCommand extends SynchCommandBehaviour {

	
	@Override
	protected void executeCommand() {
		// TODO Auto-generated method stub
		deregisterMsnService();
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
