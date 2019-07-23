package karski.breakout;

/**
 * Predictor is an AI model that can predict the score difference between OriginalState and (unknown) ResultState with a 
 * given Move. A larger score difference means that the move is better, i.e. results to more tiles being consumed.
 * @author tero
 *
 */
public interface Predictor {

	/**
	 * This version takes the primary key of OriginalState and Move as argument.
	 * @param Move move as paddle position (0-255) (not as X)
	 * @param OriginalState primary key of OriginalState
	 * @return
	 */
	public double predict(String Move, String OriginalState);
	
	/**
	 * This versio of predict takes the same set of argument that the AI model is trained with.
	 * @param movement_direction an integer from -4 to -1 (left) or +1 to +4 (right)
	 * @param hitpointX the actual X where the ball meets the bat
	 * @param score score
	 * @param bytes 30-byte hex presentation of tiles (see Tiles)
	 * @return
	 */
	public double predict(int movement_direction, int hitpointX, int score, int[] bytes);
	
}
