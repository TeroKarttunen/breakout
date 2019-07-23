package karski.breakout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import karski.breakout.queue.Request;
import karski.breakout.queue.RequestSender;
import karski.breakout.queue.Response;
import karski.breakout.queue.ResponsesReceiver;

/**
 * DistributedBreakout implements greedy algorithm to play a game of breakout. In each state, it evaluates all child moves and then
 * selects the best performing move. The program tries to evaluate children firstly by consulting the databank, and secondly by 
 * putting asynchronous evaluation requests to distributed request queue (usually AWS SQS) and waiting on all responses on response 
 * queue. 
 * <P>DistributedBreakout spawns a ResponseConsumer thread to continuously wait for responses on the queue. The requests are put onto a "bucket",
 * and when all requests have responses, the consumer wakes up the main thread to continue execution. 
 * @author tero
 *
 */
public class DistributedBreakout implements ResponseListener {
	
	private final static Logger LOGGER = Logger.getLogger(DistributedBreakout.class.getName());
	
	private Databank databank = null;
	private ResponsesReceiver receiver;
	private RequestSender sender;
	private Thread responseConsumerThread;
	private ResponseConsumer responseConsumer;
	private ArrayList<Bucket> buckets = new ArrayList<Bucket>();

	public DistributedBreakout(RequestSender sender, ResponsesReceiver receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public void setDatabank(Databank databank) {
		this.databank = databank;
	}
	
	/**
	 * notify is called by ResponseConsumer thread on any new response.
	 * @param response
	 */
	public void notify(Response response) {
		String privateKeyPlusMove = response.original.getPrimaryKey() + response.move;
		LOGGER.info("DistributedBreakout got a notification for "+privateKeyPlusMove);
		synchronized (buckets) {
			for (Bucket b : buckets) {
				if (b.notify(privateKeyPlusMove, response)) {
					LOGGER.fine("Positive match for a bucket.");
				} else {
					LOGGER.fine("Negative match for a bucket.");
				}
			}
		}
	}
	
	/**
	 * Hard coded starting game state.
	 * @return
	 */
	private GameStateWithTiles getStartGameState() {
		String privateKey = "1118:1;0fffff0fffff0fffff0ffffb0fffff";
		int gameTimeDifference = 0;
		ArrayList<String> moves = new ArrayList<String>();
		moves.add("14");
		moves.add("19");
		moves.add("25");
		moves.add("33");
		moves.add("40");
		moves.add("46");
		moves.add("52");
		moves.add("8");
		GameStateWithTiles start =  GameStateWithTiles.buildFromPrimaryKey(privateKey, gameTimeDifference, moves);
		start.gameState.setGameTime(53);
		return start;
	}
	
	/**
	 * The main loop
	 */
	public void go() {
		// start consumer and register as listener
		responseConsumer = new ResponseConsumer(receiver, databank);
		responseConsumer.registerListener(this);
		responseConsumerThread = new Thread(responseConsumer);
		responseConsumerThread.start();

		GameStateWithTiles currentGameStateWithTiles = getStartGameState();
		
		LOGGER.info("Starting a new solution.");
		
		StateList solutionRoot = new StateList();
		solutionRoot.stateTreeNode = new StateTreeNode(null, currentGameStateWithTiles.gameState);
		solutionRoot.stateTreeNode.tiles = currentGameStateWithTiles.tiles;
		StateList solution = solutionRoot;
		
		while (true) {
			// we need to determine where to go next
			// try to find an unevaluated child
			StateTreeNode stateTreeNode = solution.stateTreeNode;
			Iterator<Entry<String, StateTreeNode>> iter =  stateTreeNode.children.entrySet().iterator();
			ArrayList<String> bucketMoves = new ArrayList<String>();
			
			while (iter.hasNext()) {
				Entry<String, StateTreeNode> entry = iter.next();
				if (entry.getValue() == StateTreeNode.UNKNOWN) {
					GameStateWithTiles childState = null;
					String move = entry.getKey();
					if (databank != null) {
						LOGGER.info("Trying to load child "+move+" from databank.");
						childState = databank.loadMove(currentGameStateWithTiles, move);
					}
					if (childState == null) {
						LOGGER.info("Putting child "+move+ " into evaluation bucket.");
						bucketMoves.add(move);
					} else {
						StateTreeNode newNode = new StateTreeNode(solution.stateTreeNode, childState.gameState);
						newNode.tiles = childState.tiles;
						newNode.state.setGameTime(solution.stateTreeNode.state.getGameTime() + childState.gameTimeDifference);
						solution.stateTreeNode.children.put(move, newNode);           						
						StringBuffer sb = new StringBuffer();
						Chain.printChain(solutionRoot, 1, sb, null);
						LOGGER.info(sb.toString()+"[Child "+move+" gametime "+newNode.state.getGameTime()+" score "+newNode.state.getScore()+"]");
					}
				}
			}
			
			// evaluate any needed moves
			if (!bucketMoves.isEmpty()) {
				LOGGER.info("Sending evaluation requests for "+bucketMoves+" move(s).");
				String originalPrivateKey = currentGameStateWithTiles.getPrimaryKey();
				ArrayList<String> bucketKeys = new ArrayList<String>();
				for (String m : bucketMoves) {
					bucketKeys.add(originalPrivateKey+m);
				}
				Bucket bucket = new Bucket(bucketKeys);
				synchronized (buckets) {
					buckets.add(bucket);
				}
				for (String m : bucketMoves) {
					// sender.sendRequest(solution, m, Request.EVALUATE_MOVE_WITH_COLLATERAL_INSTRUCTION, null);
					sender.sendRequest(solution, m, Request.EVALUATE_MOVE_INSTRUCTION, null);
				}
				try {
					synchronized (bucket) {
						bucket.wait();
					}
				} catch (InterruptedException e) {
					LOGGER.warning("Interrupted while waiting for bucket.");
				}
				synchronized (buckets) {
					buckets.remove(bucket);
				}
				LOGGER.info("Bucket filled up, handling responses.");
				for (Response response : bucket.bucket.values()) {
					if (response == bucket.NULL) {
						LOGGER.warning("Bucket has NULL content, not every request was fulfilled.");
					} else {
						StateTreeNode newNode = new StateTreeNode(solution.stateTreeNode, response.result.gameState);
						newNode.tiles = response.result.tiles;
						newNode.state.setGameTime(response.resultGameTime);
						solution.stateTreeNode.children.put(response.move, newNode);
						StringBuffer sb = new StringBuffer();
						Chain.printChain(solutionRoot, 1, sb, null);
						LOGGER.info(sb.toString()+"[Child "+response.move+" gametime "+newNode.state.getGameTime()+" score "+newNode.state.getScore()+"]");
					}
				}
			}
			
			// selecting the best move from children
			String selection = selectBestChild(stateTreeNode);
			solution.selectedMove = selection;
			LOGGER.info("Child "+selection+" selected for solution.");
			StateList nextStateList = new StateList();
			nextStateList.previousNode = solution;
			nextStateList.stateTreeNode = stateTreeNode.children.get(selection);
			solution.nextNode = nextStateList;
			solution = solution.nextNode;
			
			currentGameStateWithTiles.gameState = nextStateList.stateTreeNode.state;
			currentGameStateWithTiles.tiles = nextStateList.stateTreeNode.tiles;
			
			StringBuffer sb = new StringBuffer();
			Chain.printChain(solutionRoot, 1, sb, null);
			LOGGER.warning(sb.toString()+" selected.");
			
			if (currentGameStateWithTiles.gameState.getScore() >= 100) {
				LOGGER.warning("Solution found.");
				sb = new StringBuffer();
				Chain.printChain(solutionRoot, 1, sb, "fi");
				LOGGER.warning("Solution: "+sb.toString());
				if (databank != null) {
					databank.saveSolution(sb.toString(), currentGameStateWithTiles.gameState.getGameTime());
				}
				System.exit(0);
				return;
			}
		}
		
		// the program exists by calling System.exit(0)
		// responseConsumerThread.stop();

	}

	/**
	 * selectBestChild selects the best performing child of the stateTreeNode. All children are assumed to be evaluated.
	 * Currently, the list of children are sorted by score, number of children and game time. A small random variable is added
	 * to game time to hopefully break some deadlock situations. 
	 * @param stateTreeNode
	 * @return selected Move
	 */
	protected String selectBestChild(StateTreeNode stateTreeNode) {
		ArrayList<Entry<String, StateTreeNode>> list = new ArrayList<Entry<String, StateTreeNode>>(); 
		list.addAll(stateTreeNode.children.entrySet());
		for (Entry<String, StateTreeNode> e : list) { // remove still unknown children
			if (e.getValue() == StateTreeNode.UNKNOWN) list.remove(e);
		}
		Collections.sort(list, new Comparator<Entry<String, StateTreeNode>>() {
			public int compare(Entry<String, StateTreeNode> o1,
					Entry<String, StateTreeNode> o2) {
				GameState state1 = o1.getValue().state;
				GameState state2 = o2.getValue().state;
				// bigger score is better
				if (state1.getScore() > state2.getScore()) return -1;
				if (state1.getScore() < state2.getScore()) return 1;
				// more children is better
				if (o1.getValue().children.size() > o2.getValue().children.size()) return -1;
				if (o1.getValue().children.size() < o2.getValue().children.size()) return 1;
				// less game time is better
				int random1 = (int) (Math.random() * 10);
				int random2 = (int) (Math.random() * 10);
				if (state1.getGameTime() + random1 < state2.getGameTime() + random2) return -1;
				if (state1.getGameTime() + random1 > state2.getGameTime() + random2) return 1;
				return 0;
			}
		});

		StringBuffer sb = new StringBuffer();
		sb.append("Children:");
		for (int i=0; i<list.size(); i++) {
			Entry<String, StateTreeNode> entry = list.get(i);
			sb.append("["+entry.getKey()+" "+entry.getValue().state.getScore()+" "+entry.getValue().state.getGameTime()+"]");
		}
		LOGGER.warning(sb.toString());
		
		return list.get(0).getKey();
	}
	
	
	/**
	 * Bucket is a static inner class that holds a HashMap of requests keys (original state private key + move) and 
	 * corresponding Response token objects. Token objects are replaced with real Response objects once available.
	 * If the bucket has no tokens, ie. all Responses have arrived, the listening parent thread is awakened. 
	 * @author tero
	 */
	static class Bucket {
		Response NULL = new Response();
		HashMap<String, Response> bucket; // there may be threads waiting for the bucket to fill up
		
		Bucket(ArrayList<String> privateKeysPlusMove) {
			bucket = new HashMap<String, Response>();
			for (String privateKeyPlusMove : privateKeysPlusMove) {
				bucket.put(privateKeyPlusMove, NULL);
			}
		}
		
		boolean notify(String privateKeyPlusMove, Response response) {
			boolean returnValue = false;
			Response r = bucket.get(privateKeyPlusMove);
			if (r != null) {
				if (r == NULL) {
					bucket.put(privateKeyPlusMove, response);
					returnValue = true;
					if (!bucket.containsValue(NULL)) {
						// bucket has filled up
						synchronized (this) {
							this.notifyAll();
						}
					}
				}
			}
			return returnValue;
		}
	}
	
	/**
	 * Arguments must include requestSenderClassName and responseReceiverClassNames, and optionally a database class name for saving
	 * encountered solutions. Example: karski.breakout.sqs.SQSRequestSender karski.breakout.sqs.SQSResponseReceiver karski.breakout.dynamodb.DynamoDatabank
	 * <P>Environment variables REQUEST_QUEUE_URL and RESPONSE_QUEUE_URL are passed on to RequestSender and ResponseReceiver as constructor
	 * arguments. 
	 * @param args RequestSender and ResponseReceiver class names
	 */
    public static void main(String[] args) throws IOException {
    	if (args.length < 2) {
    		System.err.println("Provide RequestSender and ResponseReceiver class names as argument (example: karski.breakout.sqs.SQSRequestSender)");
    		return;
    	}
    	String requestSenderClassName = args[0];
    	String responseReceiverClassName = args[1];
    	String databankClassName = null;
    	
    	if (args.length >= 3) {
    		databankClassName = args[2];
    	}    	     
    	
        try {
        	String requestQueueUrl = System.getenv("REQUEST_QUEUE_URL");
        	String responseQueueUrl = System.getenv("RESPONSE_QUEUE_URL");
        	@SuppressWarnings("rawtypes")
			Class[] cArg = new Class[1];
        	cArg[0] = String.class;
        	RequestSender sender = (RequestSender) Class.forName(requestSenderClassName).getDeclaredConstructor(cArg).newInstance(requestQueueUrl);
        	ResponsesReceiver receiver = (ResponsesReceiver) Class.forName(responseReceiverClassName).getDeclaredConstructor(cArg).newInstance(responseQueueUrl);
        	DistributedBreakout breakout = new DistributedBreakout(sender, receiver);
        	if (databankClassName != null) {
            	Databank databank = (Databank) Class.forName(databankClassName).getDeclaredConstructor().newInstance();
            	databank.initialize();
            	breakout.setDatabank(databank);
            }		
        	breakout.go();

        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (IllegalAccessException e) {
        	e.printStackTrace();
        } catch (InstantiationException e) {
        	e.printStackTrace();
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (InvocationTargetException e) {
        	e.printStackTrace();
        }        
    }
	
}
