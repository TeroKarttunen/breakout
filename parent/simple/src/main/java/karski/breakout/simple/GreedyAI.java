package karski.breakout.simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import karski.breakout.Chain;
import karski.breakout.Databank;
import karski.breakout.GameState;
import karski.breakout.GameStateWithTiles;
import karski.breakout.MachineState;
import karski.breakout.StateList;
import karski.breakout.StateTreeNode;
import karski.breakout.Tiles;
import karski.breakout.viceapi.Adapter;

/**
 * GreedyAI is an AI that implements the greedy search algorithm. It builds a search tree in its memory, and at each node,
 * evaluates all the children (utilizing databank if possible) and selects the child that has the highest score. Because backtracking
 * is not possible, the AI must restart the game over and over again during its evaluation. 
 * @author tero
 */
public class GreedyAI implements IAI {

	private final static Logger LOGGER = Logger.getLogger(GreedyAI.class.getName());	
	
	private Adapter adapter;
	private Databank databank = null;
	
	private StateList solutionRoot = null;
	private StateList solution = null;
	
	
	public GreedyAI() {
		
	}
	
	public void setAdapter(Adapter a) {
		this.adapter = a;
	}
	
	public void setDatabank(Databank d) {
		this.databank = d;
	}
	
	public void play() throws IOException {
		
		Loop:
		while (true) {
        	String breakpoint = adapter.waitForBreakpoint();
        	LOGGER.info("BREAKPOINT "+breakpoint);
        	       	
        	if (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD)) {
        		MachineState state = adapter.getGameStateMemdump();
        		
        		if (state.isGoingDown() && state.hitpointY >= 235) { // skip unnecessary breakpoints/states
        			Tiles tiles = adapter.getTilesMemdump();
        			if (solution != null && solution.nextNode != null) {
        				// we are following a solution chain, so this is easy
        				adapter.setPaddle(Integer.valueOf(solution.selectedMove));
        				solution = solution.nextNode;
            			adapter.continueExecution();
            			continue Loop;        				
        			}

        			if (solution == null) {
        				LOGGER.info("Starting a new solution.");
        				StateTreeNode newStateTreeNode = new StateTreeNode(null, state);
        				newStateTreeNode.tiles = tiles;
        				StateList newSolution = new StateList();
        				newSolution.stateTreeNode = newStateTreeNode;
        				solutionRoot = newSolution;
        				solution = newSolution;
        			}       			
        			
        			if (solution != null && solution.nextNode == null) {
        				// we need to determine where to go next
        				// try to find an unevaluated child
        				StateTreeNode stateTreeNode = solution.stateTreeNode;
        				Iterator<Entry<String, StateTreeNode>> iter =  stateTreeNode.children.entrySet().iterator();
        				while (iter.hasNext()) {
        					Entry<String, StateTreeNode> entry = iter.next();
        					if (entry.getValue() == StateTreeNode.UNKNOWN) {
        						// an unevaluated child, let's try to evaluate it
        						LOGGER.info("Selecting child "+entry.getKey()+ " for evaluation.");
        						if (evaluateChild(solution.stateTreeNode, entry.getKey())) {
        							// start over required
            						solution = solutionRoot;
            		        		adapter.startNewGame();
            		        		adapter.continueExecution();
            		        		continue Loop;
        						}
        					}
        				}
        				// all children evaluated, let's select one
        				String selection = selectBestChild(stateTreeNode);
        				solution.selectedMove = selection;
        				LOGGER.info("Child "+selection+" selected for solution.");
        				StateList nextStateList = new StateList();
        				nextStateList.previousNode = solution;
        				nextStateList.stateTreeNode = stateTreeNode.children.get(selection);
        				solution.nextNode = nextStateList;
        				adapter.setPaddle(Integer.valueOf(solution.selectedMove));
        				solution = solution.nextNode;
        				
        				StringBuffer sb = new StringBuffer();
						//StateList.printChain(solutionRoot, 1, sb);
						//LOGGER.warning(sb.toString()+"[new:"+solution.stateTreeNode.getGameTime()+" "+solution.stateTreeNode.state.getScore()+" xx]");
        				Chain.printChain(solutionRoot, 1, sb, null);
        				LOGGER.warning(sb.toString()+" selected.");
        				
        				adapter.continueExecution();
        				continue Loop;
        			}
        		} else {
        			if (!state.isGoingDown()) {
        				LOGGER.info("Ball direction down, ignoring breakpoint.");
        			}
        			if (state.hitpointY < 235) {
        				LOGGER.info("Ball y "+state.ball_y+", calculated hitpoint "+state.hitpointY+" < 235, ignoring breakpoint.");
        			}
        		}
        		
    			adapter.continueExecution();       		
        		
        	} else if (breakpoint.equals(Adapter.BREAKPOINT_GAME_OVER)) {
        		LOGGER.warning("Unexpected Game Over occurred.");
        		
    		} else if (breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
    			// level finished while evaluating children, assume for simplicity's sake that there's only one solution
    			MachineState state = adapter.getGameStateMemdump();
    			LOGGER.warning("Game finished.");
    			StringBuffer sb = new StringBuffer();
    			Chain.printChain(solutionRoot, 1, sb, "fi");
				LOGGER.warning(sb.toString());
    		}
		}
		

	}
	
	private boolean evaluateChild(StateTreeNode previousNode, String move) throws IOException {
		if (databank != null) {
			// load from databank
			GameStateWithTiles original = new GameStateWithTiles();
			original.gameState = previousNode.state;
			original.tiles = previousNode.tiles;
			GameStateWithTiles earlier = databank.loadMove(original, move);
			if (earlier != null) {
				StateTreeNode newNode = new StateTreeNode(previousNode, earlier.gameState);
				newNode.tiles = earlier.tiles;
				newNode.state.setGameTime(previousNode.state.getGameTime() + earlier.gameTimeDifference);
				previousNode.children.put(move, newNode);           						
				StringBuffer sb = new StringBuffer();
				Chain.printChain(solutionRoot, 1, sb, null);
				LOGGER.warning(sb.toString()+"[Child "+move+" gametime "+newNode.state.getGameTime()+" score "+newNode.state.getScore()+"]");
				return false; // start over required				
			}
		}
		
		adapter.setPaddle(Integer.valueOf(move));
		adapter.continueExecution();
		
		MachineState state = null;
		Tiles tiles = null;
		String breakpoint = null;
		
		do {
			breakpoint = adapter.waitForBreakpoint();
			LOGGER.info("BREAKPOINT "+breakpoint);
			state = adapter.getGameStateMemdump();
			tiles = adapter.getTilesMemdump();
			if (!state.isGoingDown() || state.hitpointY < 235) {
				adapter.continueExecution();
			}
		} while (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD) && (!state.isGoingDown() || state.hitpointY < 235));
    				
		if (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD)) {
			StateTreeNode newNode = new StateTreeNode(previousNode, state);
			newNode.tiles = tiles;
			previousNode.children.put(move, newNode);           						
			if (databank != null) {
				// save info to databank
				GameStateWithTiles original = new GameStateWithTiles();
				original.gameState = previousNode.state;
				original.tiles = previousNode.tiles;
				GameStateWithTiles earlier = databank.loadMove(original, move);
				if (earlier == null) {
					GameStateWithTiles result = new GameStateWithTiles();
					result.gameState = state;
					result.tiles = tiles;
					databank.saveMove(original, move, result);
				}
			}
			StringBuffer sb = new StringBuffer();
			Chain.printTentativeChain(solutionRoot, 1, sb, move, state.getGameTime(), state.getScore(), null);
			LOGGER.warning(sb.toString()+" evaluated.");
			return true; // start over required
		} else if (breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
			// level finished while evaluating children, assume for simplicity's sake that there's only one solution
			StateTreeNode newNode = new StateTreeNode(previousNode, state);
			newNode.tiles = tiles;
			previousNode.children.put(move, newNode);
			solution.selectedMove = move;
			LOGGER.info("Child "+move+" finished the level.");
 			StateList nextStateList = new StateList();
    		nextStateList.previousNode = solution;
    		nextStateList.stateTreeNode = newNode;
    		solution.nextNode = nextStateList;
			StringBuffer sb = new StringBuffer();
			Chain.printTentativeChain(solutionRoot, 1, sb, move, state.getGameTime(), state.getScore(), "fi");
			LOGGER.warning(sb.toString());
	        return true;
    	} else {
    		LOGGER.warning("This code should not be reached.");
    		return false; // this should not happen 
    	}
	}
	
	protected String selectBestChild(StateTreeNode stateTreeNode) {
		ArrayList<Entry<String, StateTreeNode>> list = new ArrayList<Entry<String, StateTreeNode>>(); 
		list.addAll(stateTreeNode.children.entrySet());
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

		
}
