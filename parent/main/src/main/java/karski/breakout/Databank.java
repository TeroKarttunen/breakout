package karski.breakout;

/**
 * Databank is a data store for previously evaluated moves and solutions (solution chains). Moves are transitions from one game
 * state (original game state) to another (resulting game state).
 * <P>Note: loadMove method returns the elapsed game time by populating the gametimedifference attribute in GameStateWithTiles object.
 * The attribute does not, strictly speaking, belong there.
 * @author tero
 *
 */
public interface Databank {

	/**
	 * saveMove saves an evaluated move to databank. 
	 * @param original the original state
	 * @param move the transition from original state to resulting state
	 * @param result the resulting state
	 */
	public void saveMove(GameStateWithTiles original, String move, GameStateWithTiles result);
	
	/**
	 * loadMove reconstructs a result state from databank based on original state and move (taken transition).
	 * @param original
	 * @param move
	 * @return a resulting state, which includes information on time difference
	 */
	public GameStateWithTiles loadMove(GameStateWithTiles original, String move);
	
	/**
	 * initialize must be called to initialize databank before first use.
	 */
	public void initialize();

	/**
	 * saveSolution saves the solution chain to databank. The solutions are ordered in databank based on gameTime. 
	 * @param chain
	 * @param gameTime
	 */
	public void saveSolution(String chain, long gameTime);
	
}
