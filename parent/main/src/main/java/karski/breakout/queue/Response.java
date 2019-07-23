package karski.breakout.queue;

import karski.breakout.GameStateWithTiles;


/**
 * Response is a message that is a response to an earlier Request. The Response declares that taking a "move" in a specific 
 * game state "original" results in new state "result". 
 * @author tero
 */
public class Response {

	/**
	 * the game time of the new state
	 */
	public long resultGameTime;
	/**
	 * the difference between "original" and "result" game times
	 */
	public long gameTimeDifference;
	/**
	 * the "before" game state
	 */
	public GameStateWithTiles original;
	/**
	 * The move selected to be taken in "before" game state
	 */
	public String move;
	/**
	 * the "after" game state
	 */
	public GameStateWithTiles result;
	/**
	 * the new moves that are possible in "after" game state
	 */
	public String[] result_moves;

}
