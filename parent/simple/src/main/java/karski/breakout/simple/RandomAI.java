package karski.breakout.simple;

import java.io.IOException;
import java.util.logging.Logger;

import karski.breakout.Chain;
import karski.breakout.Databank;
import karski.breakout.MachineState;
import karski.breakout.StateList;
import karski.breakout.StateTreeNode;
import karski.breakout.viceapi.Adapter;

/**
 * GreedyAI is an AI that selects the next move at random. 
 * @author tero
 */
public class RandomAI implements IAI {

	private final static Logger LOGGER = Logger.getLogger(RandomAI.class.getName());
	
	private Adapter adapter;
	private Databank databank;
	
	public RandomAI() {
		
	}
	
	public void setAdapter(Adapter a) {
		this.adapter = a;
	}
	
	public void setDatabank(Databank d) {
		this.databank = d;
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
        		//Tiles tiles = adapter.getTiles(); // there is no need for tile data as we select the next move at random
         		
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
            					LOGGER.warning("PreviousNode did not have a matching, unevaluated child --  this is not typical for RandomAI");
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
            			int selected = (int) (Math.random()*number);
            			String moves[] = move.children.keySet().toArray(new String[number]);
            			solution.selectedMove = moves[selected];
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
   					LOGGER.warning("PreviousNode did not have a matching, unevaluated child --  this is not typical for RandomAI");
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
