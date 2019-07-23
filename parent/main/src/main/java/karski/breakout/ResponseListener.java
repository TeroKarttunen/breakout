package karski.breakout;

import karski.breakout.queue.Response;

/**
 * ResponseListener is a class that wants to notified on received Response objects (request responses arriving from queue). 
 * ResponseListeners are registed to a ResponseConsumer that waits on a queue. This allows received Responses to act as events.
 * @author tero
 *
 */
public interface ResponseListener {

	/**
	 * notify is called by ResponseConsumer on each Response it receives.
	 * @param response
	 */
	public void notify(Response response);
	
}
