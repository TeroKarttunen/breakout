package karski.breakout;

import java.util.logging.Logger;

/**
 * GameState contains the basic information about the game state; at the minimum, game level, score, time, and the X hit point.
 * This much is always present, even when constructed from the DB presentation (PrecalculatedGameState). Note that when the breakpoint
 * occurs, the ball has not yet travelled so far as to potentially hit the bat, but _it has crossed the threshold where it may hit the
 * bat depending on the player's move_. This means that hitpointX and hitpointY are speculative based on ball's current trajectory. 
 * Note that tiles are not part of this object.
 * @see Tiles
 * @author tero
 */
public class GameState implements Cloneable {

	private final static Logger LOGGER = Logger.getLogger(MachineState.class.getName());	
	
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
	
	public int hitpointX; // the actual X where the ball meets the bat
	public int hitpointY; // the actual Y where the ball meets the bat; may be unknown
	
	public int score_lsd;
	public int score_msd;
	public int current_level;
	public int game_time_1;
	public int game_time_2;
	public int game_time_3;

	public int getScore() {
		return score_lsd + 256*score_msd;
	}

	public int getCurrentLevel() {
		return current_level;
	}
	
	public long getGameTime() {
		return game_time_1 + (256 * game_time_2) + (256*256*game_time_3);
	}
	
	public void setGameTime(long newGameTime) {
		game_time_1 = (int) (newGameTime % 256);
		game_time_2 = (int) ((newGameTime / 256) % 256);
		game_time_3 = (int) (newGameTime / (256 * 256));		
	}

	public void setScore(int score) {
		score_lsd = score % 256;
		score_msd = (score / 256);
	}
	
}
