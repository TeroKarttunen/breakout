package karski.breakout;

/**
 * StateList is an ordered list of nodes in State Tree that represents a (partial) solution to the game; that is, a path from the
 * beginning state to some later state in the game that can be followed by making a series of moves. 
 * @author tero
 *
 */
public class StateList implements Cloneable {

	public StateList previousNode;
	public StateList nextNode;
	public StateTreeNode stateTreeNode;
	
	public String selectedMove; // selected paddle position
		
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
