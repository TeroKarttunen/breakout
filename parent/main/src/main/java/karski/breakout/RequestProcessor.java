package karski.breakout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import karski.breakout.queue.Request;
import karski.breakout.queue.RequestReceiver;
import karski.breakout.queue.Response;
import karski.breakout.queue.ResponseSender;
import karski.breakout.viceapi.Adapter;
import karski.breakout.viceapi.MonitorConnection;

/**
 * RequestProcessor is a distributed Breakout move evaluator, which receives move evaluation requests from RequestReceiver,
 * processes them by playing a game in VICE emulation, and sends the result state via ResponseSender. The request may contain
 * instructions to keep playing (making random moves) until a certain amount of gametime has passed; in this case, the encountered
 * extra game moves are sent through another ResponseSender object (collateralSender).
 * <P>The program command line arguments are:
 * <UL><LI>RequestReceiver class name (example: karski.breakout.sqs.SQSRequestReceiver)
 * <LI>ResponseSender class names (example: karski.breakout.sqs.SQSResponseSender)</UL>
 * <P>The required system environment variables are:
 * <UL><LI><B>REQUEST_QUEUE_URL</B>The RequestReceiver request queue location
 * <LI><B>RESPONSE_QUEUE_URL</B>The ResponseSender response queue location
 * <LI><B>COLLATERAL_QUEUE_URL</B>The ResponseSender collateral queue location
 * <LI><B>VICE_COMMAND</B>The location of VICE emulator executive in file system; arguments are added to this
 * <LI><B>PRG_LOCATION</B></UL>The location of VICE emulator C64 Breakout game
 * <P>The possible JVM properties are:
 * <UL><LI><B>vice_port</B>The TCP/IP port to bind VICE emulator remotemonitor interface to (must be free)
 * <LI><B>vice_command</B> (takes precedence over similar environment variable)
 * <LI><B>prg_location</B> (takes precedence over similar environment variable)
 * <LI><B>console</B></UL>If set, passes -console argument to VICE emulator
 * @author tero
 *
 */
public class RequestProcessor {

	private final static Logger LOGGER = Logger.getLogger(RequestProcessor.class.getName());

	private final static int STARTUP_WAIT_TIME = -1;
	
	private RequestReceiver receiver; // request receiver for move evaluation requests
	private ResponseSender sender; // response sender for move evaluation responses
	private ResponseSender collateralSender; // response sender for collateral gameplay

	private Adapter adapter; // the adapter for VICE emulator
	private StateList solutionRoot;
	private StateList solution;
	
	private boolean gameOn = false;
	private int counter = 1;
	
	/**
	 * Process receives a request and handles it. Can block if there are no requests on queue.
	 * @throws IOException
	 */
	public void process() throws IOException {
		if (receiver == null) throw new RuntimeException("No request processor defined.");
		
		Request request = null;
		while (request == null) {
			request = receiver.receiveRequest();
			if (request == null) continue;
			if (request.instruction.equals(Request.EVALUATE_MOVE_INSTRUCTION) ||
				request.instruction.equals(Request.EVALUATE_MOVE_WITH_COLLATERAL_INSTRUCTION)) {
				process(request);
			} else {
				throw new IllegalArgumentException("Request instruction "+request.instruction+" unknown.");
			}
		}
	}

	/**
	 * Process handles a received request. 
	 * @param request the received request
	 * @throws IOException
	 */
	public void process(Request request) throws IOException {
		LOGGER.info("Request received, selectedMove="+request.selectedMove+",instruction="+request.instruction+",instruction_argument="+request.instructionArgument+",chain="+request.chain);
		// solutionRoot contains a preprogrammed list of moves to make
		solutionRoot = Chain.createStateList(request.chain); 
		
		// a new game must be started, aborting an earlier game
		if (gameOn) {
			LOGGER.info("RESTART GAME");
			adapter.startNewGame();
			adapter.continueExecution();
		} else {
        	String breakpoint = adapter.waitForBreakpoint();
        	LOGGER.info("BREAKPOINT "+breakpoint);
			// this extra restart is necessary to fix game time issue - the very first gametime is otherwise off by one [frame]
			LOGGER.info("Extra restart.");
			adapter.startNewGame();
			adapter.continueExecution();
		}

		solution = solutionRoot;
		counter = 1;
		gameOn = true;
		
		Loop: // the main game loop; we always wait for the next breakpoint and make a move based on the request chain
		while (true) {
        	String breakpoint = adapter.waitForBreakpoint();
        	LOGGER.info("BREAKPOINT "+breakpoint);
        	if (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD)) {
        		MachineState state = adapter.getGameStateMemdump();
        		if (state.isGoingDown() && state.hitpointY >= 235) { // skip unnecessary breakpoints/states
        			Tiles tiles = adapter.getTilesMemdump();
        			if (solution != null) {
        				solution.stateTreeNode.state = state;
        				solution.stateTreeNode.tiles = tiles;
        				String[] moves = PossibleMovesCalculator.calculateMoves(state);
        				for (String m : moves) {
        					if (solution.stateTreeNode.children.get(m) == null) 
        						solution.stateTreeNode.children.put(m, StateTreeNode.UNKNOWN);
        				}
        			}
        			if (solution != null && solution.selectedMove != null) {
        				// we are following a solution chain, so this is easy
        				LOGGER.info("Counter "+counter+" move "+solution.selectedMove);
        				adapter.setPaddle(Integer.valueOf(solution.selectedMove));
        				solution = solution.nextNode;
            			adapter.continueExecution();
            			counter++;
            			continue Loop;        				
        			} else {
        				LOGGER.info("End of chain."); // we have reached the requested end state
        				sender.sendResponse(makeResponse(solution));
        				request.markAsReceived();
           				if (request.instruction.equals(Request.EVALUATE_MOVE_WITH_COLLATERAL_INSTRUCTION)) {
           					// continue with collateral gameplay
           					long gameTimeLimit = 0;
           					if (request.instructionArgument != null && !request.instructionArgument.equals("")) 
           						gameTimeLimit = Long.valueOf(request.instructionArgument).longValue(); 
           					if (gameTimeLimit == 0 || state.getGameTime() < gameTimeLimit) {
           						evaluateCollaterals(gameTimeLimit, counter, request.chain);
           					} else {
           						// time is up
           						LOGGER.info("Collateral not evaluated as gametime is "+state.getGameTime()+" and gameTimeLimit = "+gameTimeLimit);
           					}
           				}
        				return;
        			}
        		} else {
        			if (!state.isGoingDown()) {
        				LOGGER.info("Ball direction down, ignoring breakpoint.");
        			}
        			if (state.hitpointY < 235) {
        				LOGGER.info("Ball y "+state.ball_y+", calculated hitpoint "+state.hitpointY+" < 235, ignoring breakpoint.");
        			}
        			adapter.continueExecution();
        		}
        	} else if (breakpoint.equals(Adapter.BREAKPOINT_GAME_OVER)) { // unexpected
        		LOGGER.warning("Warning: game over encountered.");
        		return;
        	} else if (breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) { // special case
    			LOGGER.info("Level finished.");
    			MachineState state = adapter.getGameStateMemdump();
    			Tiles tiles = adapter.getTilesMemdump();
   				solution.stateTreeNode.state = state;
   				solution.stateTreeNode.tiles = tiles;
   				String[] moves = new String[0];
   				solution.selectedMove = null;
   				sender.sendResponse(makeResponse(solution));
   				request.markAsReceived();
   				// no collateral gameplay is necessary
   				return;
        	}

		}
	}

	// this method constructs a response for evaluation; there is always exactly one Response object in the list as the result state is
	// unambiguous. 
	private List<Response> makeResponse(StateList solutionNode) { 
		ArrayList<Response> responseList = new ArrayList<Response>();
		Response response = new Response();
		GameStateWithTiles original = new GameStateWithTiles();
		original.gameState = solutionNode.previousNode.stateTreeNode.state;
		original.tiles = solutionNode.previousNode.stateTreeNode.tiles;
		GameStateWithTiles result = new GameStateWithTiles();
		result.gameState = solutionNode.stateTreeNode.state;
		result.tiles = solutionNode.stateTreeNode.tiles;
		response.gameTimeDifference = solutionNode.stateTreeNode.state.getGameTime() - solutionNode.previousNode.stateTreeNode.state.getGameTime();
		response.resultGameTime = solutionNode.stateTreeNode.state.getGameTime();
		response.move = solutionNode.previousNode.selectedMove;
		response.original = original;
		response.result  = result;
		response.result_moves = PossibleMovesCalculator.calculateMoves(result.gameState);
		responseList.add(response);
		return responseList;
	}
	
	private void evaluateCollaterals(long gameTimeLimit, int counter, String originalRequestChain) throws IOException {
		LOGGER.info("Evaluating collateral. gameTimeLimit = "+gameTimeLimit);
		solutionRoot = solution; // discard earlier moves; start from the evaluation result
		
		Loop:
		while (gameTimeLimit == 0 || solution.stateTreeNode.state.getGameTime() < gameTimeLimit) {
			// select next move randomly
			int noChildren = solution.stateTreeNode.children.size();
			int selectedChild = (int) (Math.random() * noChildren);
			String moves[] = solution.stateTreeNode.children.keySet().toArray(new String[noChildren]);
			solution.selectedMove = moves[selectedChild];
			adapter.setPaddle(Integer.valueOf(solution.selectedMove));
			StringBuffer sb = new StringBuffer();
			Chain.printChain(solutionRoot, counter, sb, null);
			LOGGER.warning(sb.toString());
			MachineState state = null;
			String breakpoint = null;
			do {
				adapter.continueExecution();
				breakpoint = adapter.waitForBreakpoint();
				LOGGER.info("BREAKPOINT "+breakpoint);
				state = adapter.getGameStateMemdump();
			} while (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD) && (!state.isGoingDown() || state.hitpointY < 235));
			if (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD) || breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
				Tiles tiles = adapter.getTilesMemdump();
				StateTreeNode newMove = new StateTreeNode(solution.stateTreeNode, state);
				newMove.setTiles(tiles);
				if (solution.stateTreeNode.children.get(solution.selectedMove) == StateTreeNode.UNKNOWN) {
					solution.stateTreeNode.children.put(solution.selectedMove, newMove);
					StateList newSolution = new StateList();
					newSolution.previousNode = solution;
					solution.nextNode = newSolution;
					newSolution.stateTreeNode = newMove;
					solution = newSolution;
				} else {
					LOGGER.warning("PreviousNode did not have a matching, unevaluated child --  this is not typical");
				}
			} else if (breakpoint.equals(Adapter.BREAKPOINT_GAME_OVER)) {
    			sb = new StringBuffer();
				Chain.printChain(solutionRoot, counter, sb, "go");
				LOGGER.warning(sb.toString());
				return;
			} else {
				LOGGER.severe("Unknown breakpoint "+breakpoint);
				return;
			}
			if (breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
				break Loop;
			}
		}
		
		// send collateral responses
		List<Response> collateral = makeCollateral(solutionRoot);
		LOGGER.info("Collateral consists of "+collateral.size()+" responses.");
		collateralSender.sendResponse(originalRequestChain, collateral);
	}
	
	private List<Response> makeCollateral(StateList solutionRoot) {
		ArrayList<Response> responseList = new ArrayList<Response>();
		for (StateList solutionNode = solutionRoot; solutionNode != null; solutionNode = solutionNode.nextNode) {
			Response response = new Response();
			GameStateWithTiles original = new GameStateWithTiles();
			original.gameState = solutionNode.previousNode.stateTreeNode.state;
			original.tiles = solutionNode.previousNode.stateTreeNode.tiles;
			GameStateWithTiles result = new GameStateWithTiles();
			result.gameState = solutionNode.stateTreeNode.state;
			result.tiles = solutionNode.stateTreeNode.tiles;
			response.gameTimeDifference = solutionNode.stateTreeNode.state.getGameTime() - solutionNode.previousNode.stateTreeNode.state.getGameTime();
			response.resultGameTime = solutionNode.stateTreeNode.state.getGameTime();
			response.move = solutionNode.previousNode.selectedMove;
			response.original = original;
			response.result  = result;
			response.result_moves = PossibleMovesCalculator.calculateMoves(result.gameState);
			responseList.add(response);
		}
		return responseList;
	}

	/**
	 * main method is the entry point to a stand-alone java program
	 * @param args RequestReceiver and ResponseSender class names
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
    	if (args.length < 2) {
    		System.err.println("Provide RequestReceiver and ResponseSender class names as argument (example: karski.breakout.sqs.SQSRequestReceiver)");
    		return;
    	}

    	String receiverClassName = args[0];
    	String senderClassName = args[1];

		RequestProcessor processor = new RequestProcessor();
    	
        try {
        	String requestQueueUrl = System.getenv("REQUEST_QUEUE_URL");
        	String responseQueueUrl = System.getenv("RESPONSE_QUEUE_URL");
        	String collateralQueueUrl = System.getenv("COLLATERAL_QUEUE_URL");
        	Class[] cArg = new Class[1];
        	cArg[0] = String.class;
            RequestReceiver receiver = (RequestReceiver) Class.forName(receiverClassName).getDeclaredConstructor(cArg).newInstance(requestQueueUrl);
            ResponseSender sender = (ResponseSender) Class.forName(senderClassName).getDeclaredConstructor(cArg).newInstance(responseQueueUrl);
            ResponseSender collateral = (ResponseSender) Class.forName(senderClassName).getDeclaredConstructor(cArg).newInstance(collateralQueueUrl);
            processor.setRequestReceiver(receiver);     
            processor.setResponseSender(sender);
            processor.setCollateralResponseSender(collateral);
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

    	String portString = System.getProperty("vice_port");
    	int port = 6510;
    	if (portString != null && !portString.equals(""))
    		port = Integer.valueOf(portString);
    	
    	
    	String vice_command = System.getProperty("vice_command");
    	if (vice_command == null || vice_command.equals("")) {
    		vice_command = System.getenv("VICE_COMMAND");
    	}
    	
    	String prg_location = System.getProperty("prg_location");
    	if (prg_location == null || prg_location.equals("")) {
    		prg_location = System.getenv("PRG_LOCATION");
    	}
    	
    	String consoleString = System.getProperty("console");
    	
    	String[] cmdArray = new String[] {vice_command, "-verbose", "-initbreak", "0x0825", "-remotemonitor", "-remotemonitoraddress", "localhost:"+port, prg_location};
    	if (consoleString != null && !consoleString.equals("") && consoleString.equals("true")) {
    		cmdArray[1] = "-console";
    	}
    	final Process viceProcess = Runtime.getRuntime().exec(cmdArray);
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		public void run() {
    			viceProcess.destroy();
    		}
    	});
    	
    	try { // give the emulator a little time (some milliseconds) to start
    		if (STARTUP_WAIT_TIME > 0) 
    			Thread.currentThread().sleep(STARTUP_WAIT_TIME);
    	} catch (InterruptedException e) {}
    	
    	MonitorConnection conn = new MonitorConnection(port);       

        Adapter adapter = new Adapter(conn);
        
        LOGGER.info("Trying to connect...");
        try {
        	adapter.connect(10000);
        } catch (IOException e) {
        	e.printStackTrace();
            return;
        }                
        LOGGER.info("Connection established.");

        // initialization              
        adapter.initialize();
        
        processor.setAdapter(adapter);
        
        try {
        	while (System.in.available() == 0) {
        		processor.process();
        	}
        } finally {		
        	adapter.terminate();
        	try {
        		Thread.currentThread().sleep(2000);
        	} catch (Exception e) {}
        }
	}
		
	public void setRequestReceiver(RequestReceiver receiver) {
		this.receiver = receiver;
	}

	public void setResponseSender(ResponseSender sender) {
		this.sender = sender;
	}

	public void setAdapter(Adapter a) {
		this.adapter = a;
	}
	
	public void setCollateralResponseSender(ResponseSender collateral) {
		this.collateralSender = collateral;
	}
}
