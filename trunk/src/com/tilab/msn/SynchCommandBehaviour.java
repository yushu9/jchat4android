package com.tilab.msn;

import com.google.android.maps.MyLocationOverlay;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.util.Logger;
import jade.wrapper.gateway.GatewayAgent;

public abstract class SynchCommandBehaviour  extends SequentialBehaviour {

	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	protected boolean isSuccess;
	protected Object commandResult;
	protected Exception ex;
	protected boolean exThrown;

	public SynchCommandBehaviour() throws ClassCastException  {
		
		isSuccess = true;
		exThrown = false;
		
		addSubBehaviour(new SynchBehaviour());
		GatewayAgent ag = (GatewayAgent) myAgent;
			addSubBehaviour(new OneShotBehaviour(ag){

				@Override
				public void action() {
					myLogger.log(Logger.INFO, "Calling release command of gateway agent...");
					((GatewayAgent) myAgent).releaseCommand(SynchCommandBehaviour.this);
				}
				
			});
	}
		
	
	
	protected abstract void executeCommand();
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public boolean exceptionThrown() {
		return exThrown;
	}
	
	public Object getCommandResult(){
		return commandResult;
	}
	
	public Exception getException() {
		return ex;
	}
	
	public SequentialBehaviour getBehaviour(GatewayAgent ga){
		SequentialBehaviour bh = new SequentialBehaviour();
		
		
		return bh;
	}
	
	//This implements a generic synch behaviour
	private class SynchBehaviour extends OneShotBehaviour {

		
		@Override
		public void action() {
			SynchCommandBehaviour.this.executeCommand();
		}
		
	}
}
