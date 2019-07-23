package karski.breakout;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * StateTreeNode is a node in State Tree structure; it contains GameState and Tiles objects, which represent the actual game state (machine
 * state) at a point in the game, and adds parent and children object references for navigation. This tree structure is very large and only part of it 
 * can be held in memory at a time, so leafs that are unknown or not loaded into memory from databank are marked with UNKNOWN singletons. 
 * When these singletons are used in children hashmap as values, they represent the moves the existence of which are known but that have not 
 * been evaluated or loaded from databank.
 * @author tero
 *
 */
public class StateTreeNode {

	private final static Logger LOGGER = Logger.getLogger(StateTreeNode.class.getName());
	
	// a singleton representing unknown or not evaluated node 
	public static final StateTreeNode UNKNOWN = new StateTreeNode(null, null);

	public GameState state;
	
	// a Mapping from moves (paddle position) to child StateTreeNodes
	public HashMap<String, StateTreeNode> children = new HashMap<String, StateTreeNode>();
	public StateTreeNode parent;
	public Tiles tiles;

	/**
	 * This constructor calls setState and populates children hashmap with UNKNOWN singletons. Note that this constructor does
	 * not have tiles parameter, so tiles should be set with setTiles method.
	 * @param parent the parent StateTreeNode
	 * @param state the game state  
	 */
	public StateTreeNode(StateTreeNode parent, GameState state) {
		this.parent = parent;
		if (state != null) setState(state);
	}
	
	/**
	 * This constructor calls setState and populates children hashmap with UNKNOWN singletons.
	 * @param parent the parent StateTreeNode
	 * @param state the game state
	 * @param tiles the tiles
	 */
	public StateTreeNode(StateTreeNode parent, GameState state, Tiles tiles) {
		this.parent = parent;
		if (state != null) setState(state);
		if (tiles != null) setTiles(tiles);
	}
	
	/**
	 * setState also uses PossibleMovesCalculator to set up children hashmap with the correct number of children.
	 * @param state
	 */
	public void setState(GameState state) {
		this.state = state;
		String[] moves = PossibleMovesCalculator.calculateMoves(state);
		if (moves.length == 0) {
			LOGGER.warning("No moves! HitpointY = "+state.hitpointY);
		}
		for (int i=0; i<moves.length; i++) {
			children.put(moves[i], UNKNOWN);
		}
	}
	
	public void setTiles(Tiles tiles) {
		this.tiles = tiles;
	}
	
	/**
	 * getGameTime is a helper function. It is equivalent to this.state.getGameTime()
	 * @return game time
	 */
	public long getGameTime() {
		if (state == null) return -1;
		return state.getGameTime();
	}
	
}
