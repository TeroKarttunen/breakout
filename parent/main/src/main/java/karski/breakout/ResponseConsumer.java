package karski.breakout;

import java.util.ArrayList;
import java.util.logging.Logger;

import karski.breakout.queue.Response;
import karski.breakout.queue.ResponsesReceiver;
import karski.breakout.queue.Responses;

/**
 * ResponseConsumer is a Runnable that constantly waits on response queue, saves all responses to databank and then notifies all its listeners 
 * on new responses. It implements a pub-sub design pattern, so many listeners may register to it.
 * <P>A single queue item may contain several Response objects, each object containing information on one game move. ResponseConsumer
 * notifies listeners for each one. 
 * @author tero
 *
 */
public class ResponseConsumer implements Runnable {

	private ResponsesReceiver receiver;
	private Databank databank;
	private final static Logger LOGGER = Logger.getLogger(ResponseConsumer.class.getName());
	
	/**
	 * Constructor.
	 * @param receiver the instantiated ResponseReceiver to use
	 * @param databank the databank to save all responses to; may be null
	 */
	public ResponseConsumer(ResponsesReceiver receiver, Databank databank) {
		this.receiver = receiver;
		this.databank = databank;
	}
	
	private ArrayList<ResponseListener> listeners = new ArrayList<ResponseListener>();

	/**
	 * registerListener registers a listener to be notified for all responses. The listener may be notified several times per
	 * queue item if the item contains several Responses.
	 * @param listener
	 */
	public void registerListener(ResponseListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * unregisterListener unregisters a listener.
	 * @param listener
	 */
	public void unregisterListener(ResponseListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Run starts consuming queue items. Runs forever if not Interrupted.
	 */
	public void run() {
		try {
			while (true) {
				if (consumeResponse() == false) {
					Thread.currentThread().sleep(2000);			
				}
			}
		} catch (InterruptedException e) {
			LOGGER.info("ResponseConsumer interrupted");
		}
	}
	
	/**
	 * consumeResponse handles one response queue item. Each response item (move) in queue item is saved to databank and then
	 * all registered listeners are notified on it. Note that the optional "originalRequestChain" attribute is not utilized at the moment.
	 * @return
	 */
	public boolean consumeResponse() {
		Responses responses = receiver.receiveResponses();
		if (responses == null || responses.responses.size() == 0) return false;
		for (Response response : responses.responses) {
			GameStateWithTiles original = response.original;
			String move = response.move;
			if (databank != null) {
				// note: doing a load instruction before save can be wasteful as it is normally unnecessary
				GameStateWithTiles result = databank.loadMove(original, move);
				if (result != null) {
					if (result.equals(response.result)) {
						LOGGER.info("Response received for "+original.getPrimaryKey()+","+move+" but result "+result.getPrimaryKey()+" already in DB.");
					} else {
						LOGGER.warning("Response received for "+original.getPrimaryKey()+","+move+" but result "+response.result.getPrimaryKey()+" different than DB ("+result.getPrimaryKey()+").");
					}
				} else {
					databank.saveMove(response.original, response.move, response.result);
					LOGGER.info("Response received for "+original.getPrimaryKey()+","+move+"->"+response.result.getPrimaryKey()+" and saved in DB.");
				}
			}
		}
		responses.markAsReceived();
		for (Response response : responses.responses) {
			for (ResponseListener listener : listeners) {
				listener.notify(response);
			}
		}
		
		return true;
	}

}
