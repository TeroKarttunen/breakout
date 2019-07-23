package karski.breakout.queue;

import karski.breakout.StateList;

public interface RequestSender {

	public void sendRequest(StateList node, String selectedMove, String instruction, String instructionArgument);
	public void sendRequest(String chain, String instruction, String instructionArgument);	
	
}
