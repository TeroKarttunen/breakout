package karski.breakout.simple;

import java.io.IOException;
import java.util.logging.Logger;

import karski.ai.xgboost.BreakoutPredictor;
import karski.ai.xgboost.LocalPredictor;
import karski.breakout.Chain;
import karski.breakout.Databank;
import karski.breakout.GameStateWithTiles;
import karski.breakout.MachineState;
import karski.breakout.Predictor;
import karski.breakout.StateList;
import karski.breakout.StateTreeNode;
import karski.breakout.Tiles;
import karski.breakout.viceapi.Adapter;

/**
 * BoostedAI is an IAI that uses an XGBoost model to select the best move from the set of moves by predicting the best outcome. 
 * <P>BoostedAI reads the file system location of the model from system property "model_location" or environment variable 
 * MODEL_LOCATION. 
 * @author tero
 *
 */
public class BoostedAI implements IAI {

	private final static Logger LOGGER = Logger.getLogger(BoostedAI.class.getName());
	
	private Adapter adapter;
	private Databank databank;
	private Predictor predictor;
	
	public BoostedAI() throws IOException {
    	String model_location = System.getProperty("model_location");
    	if (model_location == null || model_location.equals("")) {
    		model_location = System.getenv("MODEL_LOCATION");
    	}

		setPredictor(new LocalPredictor(model_location));		
	}
	
	public void setAdapter(Adapter a) {
		this.adapter = a;
	}
	
	public void setDatabank(Databank d) {
		this.databank = d;
	}
	
	public void setPredictor(Predictor p) {
		this.predictor = p;
		// initialize AI
		LOGGER.info("Initializing AI...");
		p.predict("248", "11b:70;008cf20004f60004c800000000cf73");
	}
	
	public void play() throws IOException {

		StateTreeNode rootNode = null;
		StateTreeNode previousNode = null;
		StateList solutionRoot = null;
		StateList solution = null;
		
        Loop:
        while (true) {

        	String breakpoint2 = adapter.waitForBreakpoint();
        	LOGGER.info("BREAKPOINT "+breakpoint2);

        	if (breakpoint2.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD)) {        		
        		MachineState state = adapter.getGameStateMemdump();
        		Tiles tiles = adapter.getTilesMemdump();
         		
        		if (state.isGoingDown() && state.hitpointY >= 235) { // skip unnecessary breakpoints/states
            		StateTreeNode move = new StateTreeNode(previousNode, state);
            		if (move.children.size() > 0) {
            			if (solution != null && !solution.selectedMove.equals("")) {
            				if (previousNode.children.get(solution.selectedMove) == StateTreeNode.UNKNOWN) {
            					previousNode.children.put(solution.selectedMove, move);
            					StateList newSolution = new StateList();
            					newSolution.previousNode = solution;
            					solution.nextNode = newSolution;
            					newSolution.stateTreeNode = move;
            					solution = newSolution;
            				} else {
            					LOGGER.warning("PreviousNode did not have a matching, unevaluated child --  this is not typical.");
            				}
            			}
            			if (rootNode == null) rootNode = move;
            			if (solution == null) {
            				solution = new StateList();
            				solution.previousNode = null;
            				solution.nextNode = null;
            				solution.stateTreeNode = move;
            				solutionRoot = solution;
            			}           		
            			int number = move.children.size();
            			LOGGER.info(number + " moves calculated.");
            			// select one move at random
           				int selected = (int) (Math.random()*number);
           				String random_moves[] = move.children.keySet().toArray(new String[number]);
           				solution.selectedMove = random_moves[selected];
           				// change selection if we are able to make a sufficiently good prediction (threshold 0.8)
           				if (predictor != null) {
            				String bestMove = "";
            				double best = -1;
            				String moves[] = move.children.keySet().toArray(new String[number]);
            				GameStateWithTiles gswt = new GameStateWithTiles();
            				gswt.gameState = state;
            				gswt.tiles = tiles;
            				String privateKey = gswt.getPrimaryKey();
            				for (int i=0; i<moves.length; i++) {
            					double prediction = predictor.predict(moves[i], privateKey);
            					LOGGER.info("Prediction: "+prediction+" ["+moves[i]+" "+privateKey+"]");
            					if (prediction > best) {
            						best = prediction;
            						bestMove = moves[i];
            					}
            				}
            				if (best >= 0.8) {
            					LOGGER.warning("Move "+bestMove+" selected (prediction "+best+").");
            					solution.selectedMove = bestMove;
            				} else {
            					LOGGER.warning("Best move "+bestMove+" (prediction "+best+"); too bad, not selected");
            				}
            			}
            			adapter.setPaddle(Integer.valueOf(solution.selectedMove));
            			previousNode = move;
            			adapter.continueExecution();
            			StringBuffer sb = new StringBuffer();
        				Chain.printChain(solutionRoot, 1, sb, null);
        				LOGGER.warning(sb.toString());
            		} else {
            			adapter.continueExecution();
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
        	}
        	
        	if (breakpoint2.equals(Adapter.BREAKPOINT_GAME_OVER)) {
    			StringBuffer sb = new StringBuffer();
				Chain.printChain(solutionRoot, 1, sb, "go");
				LOGGER.warning(sb.toString());
        		break Loop;
        	}
        	
        	if (breakpoint2.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
        		MachineState state = adapter.getGameStateMemdump();
        		StateTreeNode move = new StateTreeNode(previousNode, state);

   				if (previousNode.children.get(solution.selectedMove) == StateTreeNode.UNKNOWN) {
   					previousNode.children.put(solution.selectedMove, move);
   					StateList newSolution = new StateList();
   					newSolution.previousNode = solution;
   					solution.nextNode = newSolution;
   					newSolution.stateTreeNode = move;
   					solution = newSolution;
   				} else {
   					LOGGER.warning("PreviousNode did not have a matching, unevaluated child --  this is not typical.");
   				}       		
        		
        		StringBuffer sb = new StringBuffer();
				Chain.printChain(solutionRoot, 1, sb, "fi");
				LOGGER.warning(sb.toString());
        		rootNode = null; 
        		previousNode = null;
        		solutionRoot  = null;
        		solution = null;
        		adapter.startNewGame();
        		adapter.continueExecution();
        	}
        }
	}
	
}
