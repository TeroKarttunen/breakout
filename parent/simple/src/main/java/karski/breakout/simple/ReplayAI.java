package karski.breakout.simple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import karski.breakout.Databank;
import karski.breakout.MachineState;
import karski.breakout.StateList;
import karski.breakout.StateTreeNode;
import karski.breakout.viceapi.Adapter;

/**
 * GreedyAI is an AI that makes its moves based on the chain inputed by the user. At start time, it reads System.in stream for a line
 * of input and plays the game based on it.
 * <P> Example input is:  [1:54 1 24][2:121 3 22][3:193 5 40][4:311 8 245][5:410 10 0][6:473 12 35][7:549 14 36][8:629 16 20][9:728 18 165][10:834 20 210][11:1041 27 114][12:1777 59 52][13:1973 64 135][14:2319 76 18][15:2817 84 88][16:3156 87 196][17:3285 90 210][18:3397 92 198][19:3509 94 174][20:3623 96 70][21:3771 98 98][22:3870 99 114]
 * @author tero
 */
public class ReplayAI implements IAI {

	private final static Logger LOGGER = Logger.getLogger(ReplayAI.class.getName());	
	
	private Adapter adapter;
	private StateList solutionRoot;
	private StateList solution;
	private StateTreeNode stateTree;
	private Databank databank;
	
	public void setAdapter(Adapter a) {
		this.adapter = a;
	}
	
	public void setDatabank(Databank d) {
		this.databank = d;
	}

	public ReplayAI() throws IOException {
		InputStreamReader in = new InputStreamReader(System.in);
		StringBuffer sb = new StringBuffer();
		int i = -1;
		do {
			i = in.read();
			if (i != -1) sb.append((char) i);
		} while ((char) i != '\n');
		
		stateTree = new StateTreeNode(null, null);
		solutionRoot = new StateList();
		solution = solutionRoot;
		solution.stateTreeNode = stateTree;
		
		StringTokenizer st = new StringTokenizer(sb.toString(), "[]");
		Pattern pattern = Pattern.compile("(.*):(\\d*) (\\d*) (\\d*)");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			Matcher matcher = pattern.matcher(token);
			LOGGER.fine("Token: "+token);
			
			if (matcher.matches()) {
				String gametime = matcher.group(2);
				String score = matcher.group(3);
				String move = matcher.group(4);
				StateTreeNode nextStateTreeNode = new StateTreeNode(solution.stateTreeNode, null);
				stateTree.children.put(move, nextStateTreeNode);
				StateList nextStateList = new StateList();
				nextStateList.previousNode = solution;
				solution.selectedMove = move;
				solution.nextNode = nextStateList;
				nextStateList.stateTreeNode = nextStateTreeNode;
				solution = nextStateList;
				stateTree = nextStateTreeNode;
			}
		}
		
	}
	
	private int counter = 1;
	
	public void play() throws IOException {

		solution = solutionRoot;
		
		Loop:
		while (true) {
			String breakpoint = adapter.waitForBreakpoint();
        	LOGGER.info("BREAKPOINT "+breakpoint);
        	
        	if (breakpoint.equals(Adapter.BREAKPOINT_BALL_AT_THRESHOLD)) {
        		MachineState state = adapter.getGameStateMemdump();
        		if (state.isGoingDown() && state.hitpointY >= 235) { // skip unnecessary breakpoints/states
        			//if (solution != null && solution.nextNode != null) {
        			if (solution != null && solution.selectedMove != null) {
        				// we are following a solution chain, so this is easy
        				LOGGER.info("Counter "+counter+" move "+solution.selectedMove);
        				adapter.setPaddle(Integer.valueOf(solution.selectedMove));
        				solution = solution.nextNode;
            			adapter.continueExecution();
            			counter++;
            			continue Loop;        				
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
        	} else if (breakpoint.equals(Adapter.BREAKPOINT_GAME_OVER)) {
        		
        	} else if (breakpoint.equals(Adapter.BREAKPOINT_LEVEL_FINISHED)) {
    			adapter.continueExecution();
        	}
        	
		}
	}

}
