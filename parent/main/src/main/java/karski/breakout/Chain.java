package karski.breakout;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chain contains a partial or complete solution, consisting a list of moves from the starting game state that must be taken in order to
 * reach a final game state. Examples:
 * <UL>
 * <LI>[movenumber:gametime score selectedmove]</LI> 
 * <LI>partial chain: [1:53 1 25][2:120 3 25][3:190 5 40]</LI>
 * <LI>unfinished chain: [1:53 1 25][2:120 3 25][3:190 5 xx]</LI>
 * <LI>finished chain: [1:53 1 25][2:120 3 25][3:190 5 40][4:308 8 fi]</LI>
 * <LI> game over: [1:53 1 25][2:120 3 25][3:190 5 40][4:308 8 go]</LI>
 * </UL>
 * @author tero
 *
 */
public class Chain {
	
	private final static Logger LOGGER = Logger.getLogger(Chain.class.getName());

	/**
	 * printChain prints the chain to StringBuffer, starting from solution root node and ending when next node cannot be found in 
	 * linked list. Then the last move is printed if selectedMove exists, or "specialend" if it does not. 
	 * @param solution solution root
	 * @param count starting counter, must be 1
	 * @param sb StringBuffer to construct chain to 
	 * @param specialend how to end chain if selected
	 */
	public static void printChain(StateList solution, int count, StringBuffer sb, String specialend) {
		while (solution.nextNode != null) {
			sb.append("["+count+":"+solution.stateTreeNode.getGameTime()+" "+solution.stateTreeNode.state.getScore()+" "+solution.selectedMove+"]");
			count++;
			solution = solution.nextNode;
		}
		sb.append("["+count+":"+solution.stateTreeNode.getGameTime()+" "+solution.stateTreeNode.state.getScore()+" ");
		if (specialend != null && !specialend.equals("")) { // finished chain or game over
			sb.append(specialend+"]");
		} else if (solution.selectedMove != null) { // partial chain
			sb.append(solution.selectedMove+"]");
		} else { // unfinished chain
			sb.append("xx]"); 
		}
	}

	/**
	 * printChain prints the chain to StringBuffer, starting from solution root node and ending when next node cannot be found in 
	 * linked list. Then the last move is printed if selectedMove exists, or "specialend" if it does not. 
	 * <P>An extra link is added to the chain with given gametime and score variables
	 * @param solution solution root
	 * @param count starting counter, must be 1
	 * @param sb StringBuffer to construct chain to 
	 * @param gametime the gametime to use in last link of the chain
	 * @param score the score to use in last link of the chain
	 * @param specialend how to end the chain
	 */
	public static void printTentativeChain(StateList solution, int count, StringBuffer sb, String move, long gametime, int score, String specialend) {
		while (solution.nextNode != null) {
			sb.append("["+count+":"+solution.stateTreeNode.getGameTime()+" "+solution.stateTreeNode.state.getScore()+" "+solution.selectedMove+"]");
			count++;
			solution = solution.nextNode;
		}
		sb.append("["+count+":"+solution.stateTreeNode.getGameTime()+" "+solution.stateTreeNode.state.getScore()+" "+move+"]");
		sb.append("["+(count+1)+":"+gametime+" "+score+" ");
		if (specialend != null && !specialend.equals("")) { // finished chain or game over
			sb.append(specialend+"]");
		} else { // unfinished chain
			sb.append("xx]"); 
		}
	}	
	
	/**
	 * createStateList constructs an empty StateList (linked list) with no gamestate objects; it can be used to traverse the state tree
	 * @param chain
	 * @return
	 */
	public static StateList createStateList(String chain) {
		StateTreeNode stateTree = new StateTreeNode(null, null);
		StateList solutionRoot = new StateList();
		StateList solution = solutionRoot;
		solution.stateTreeNode = stateTree;
		
		StringTokenizer st = new StringTokenizer(chain, "[]");
		Pattern pattern = Pattern.compile("(.*):(\\d*) (\\d*) (\\d*)");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			Matcher matcher = pattern.matcher(token);
			LOGGER.fine("Token: "+token);
			
			if (matcher.matches()) {
				//String gametime = matcher.group(2);
				//String score = matcher.group(3);
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
		return solutionRoot;
	}
	
}
