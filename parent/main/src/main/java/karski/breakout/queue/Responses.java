package karski.breakout.queue;

import java.util.List;

/**
 * Responses is a container that can hold multiple Response objects. If collateral gameplay is enabled, each request may result in multiple
 * Response messages. This container enables their handling as one collection unit.
 * @author tero
 *
 */
public abstract class Responses {

	public List<Response> responses;
	public abstract void markAsReceived();
	public String originalRequestChain;
	
}
