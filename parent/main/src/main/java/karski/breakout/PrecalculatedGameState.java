package karski.breakout;

/**
 * PrecalculatedGameState indicates that this GameState has been re-constructed from databank by primary key; we don't have the
 * original full MachineState. (Primary key contains only minimal information, which corresponds to GameState base class.) 
 * This object also contains the list of possible moves in this state. This is necessary, because without
 * the full MachineState, PossibleMovesCalculator does not have enough information to recalculate these. 
 * @author tero
 *
 */
public class PrecalculatedGameState extends GameState {

	public String[] moves = null;

	/**
	 * getMoves returns the list of possible moves (as paddle positions).
	 * @return the list of possible moves
	 */
	public String[] getMoves() {
		return moves;
	}	
}
