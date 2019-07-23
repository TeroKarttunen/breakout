package karski.breakout;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * PossibleMovesCalculator is a utility class for calculating the ball's current trajectory and deciding where the ball
 * will be by next frame. It can also calculate a set of paddle positions that result in differing ball trajectory, basically
 * enumerating the possible moves to consider in each situation.
 * <P>The calculation is actually only possible for MachineState objects, but PrecalculatedGameState objects by their nature
 * already contain the moves object, so no re-calculation is necessary. 
 * <P>The algorithm here has actually been ported from 6502 assembly code.
 * @author tero
 *
 */
public class PossibleMovesCalculator {
	
	private final static Logger LOGGER = Logger.getLogger(PossibleMovesCalculator.class.getName());
	
	/**
	 * calculateMoves evaluates the possible moves in a situation (game state).
	 * @param state the game state to evaluate
	 * @return the set of paddle position that result to different ball trajectory, basically the possible moves to make
	 */
	public static String[] calculateMoves(GameState state) {
		if (state instanceof MachineState) {
			MachineState _state = (MachineState) state;
			ArrayList<String> moves = new ArrayList<String>();
			ArrayList<Integer> seen = new ArrayList<Integer>();
			for (int i=0; i<256; i++) {
				MachineState newState = calculateMove(_state, i);
				LOGGER.fine(i+","+newState.getBatX()+","+newState.pos_y_dir+","+newState.getMovementDirToken());
				if (newState.pos_y_dir == 0 && !seen.contains(newState.getMovementDirToken())) {
					moves.add(String.valueOf(i));
					seen.add(newState.getMovementDirToken());
				}
			}
			return moves.toArray(new String[moves.size()]);
		}
		if (state instanceof PrecalculatedGameState) {
			PrecalculatedGameState _state = (PrecalculatedGameState) state;
			return _state.getMoves();
		}
		throw new RuntimeException("Unknown GameState variant encountered.");
	}
	
	private static MachineState calculateMove(MachineState state, int paddlePosition) {
		MachineState newState = (MachineState) state.clone();
		// first, let's look up actual bat_x
		newState.bat_x = paddle_x_table[paddlePosition];
		newState.bat_x_overflow = paddle_x_overflow_table[paddlePosition];
		move(newState);
		check_ball_bat_collision(newState);
		return newState;
	}
	
	/**
	 * move updates MachineState by one frame, continuing current trajectory, ignoring possible bat collisions
	 * @param state the MachineState to update by one frame
	 */
	public static void move(MachineState state) {
		if (state.speed == 0) { // untested!
			// move_speed_2
			int add = 0;
			if (state.movement_dir == 1) {
				add = 6;
			} else if (state.movement_dir == 2) {
				add = 12;
			} else if (state.movement_dir == 3) {
				add = 18;
			}
			int move = speed2_table[state.movement_counter + add];
			process_move(state, move);
			state.movement_counter = state.movement_counter +1;
			if (state.movement_counter == 6) state.movement_counter = 0;
			return;						
		}
		if (state.speed == 1) { // untested!
			// move_speed_3
			int add = 0;
			if (state.movement_dir == 1) {
				add = 4;
			} else if (state.movement_dir == 2) {
				add = 8;
			} else if (state.movement_dir == 3) {
				add = 12;
			}
			int move = speed3_table[state.movement_counter + add];
			process_move(state, move);
			state.movement_counter = state.movement_counter +1;
			if (state.movement_counter == 4) state.movement_counter = 0;
			return;						
		}
		if (state.speed == 2) { // untested!
			// move_speed_4
			int add = 0;
			if (state.movement_dir == 1) {
				add = 3;
			} else if (state.movement_dir == 2) {
				add = 6;
			} else if (state.movement_dir == 3) {
				add = 9;
			}
			int move = speed4_table[state.movement_counter + add];
			process_move(state, move);
			state.movement_counter = state.movement_counter +1;
			if (state.movement_counter == 3) state.movement_counter = 0;
			return;						
		}
		if (state.speed == 3) {
			// move_speed_6
			int add = 0;
			if (state.movement_dir == 1) {
				add = 2;
			} else if (state.movement_dir == 2) {
				add = 4;
			} else if (state.movement_dir == 3) {
				add = 6;
			}
			int move = speed6_table[state.movement_counter + add];
			process_move(state, move);
			state.movement_counter = state.movement_counter +1;
			if (state.movement_counter == 2) state.movement_counter = 0;
			return;
		}
		throw new RuntimeException("Unknown speed "+state.speed + " encountered.");
	}
	
	private static void process_move(MachineState state, int move) {
		int x = 0;
		while (move < 256) {
			move = move << 1;
			x++;
		}
		move = move - 256;
		while (x != 8) {
			move = move << 1;
			x++;
			if (move >= 256) { // 1
				move = move - 256;
				move_y(state);
			} else { // 0
				move_x(state);
			}
		}
		return;
	}
	
	private static void move_y(MachineState state) {
		if (state.pos_y_dir == 1) {
			if (inc_ball_y(state)) {
				state.pos_y_dir = 0;
			}
		} else {
			if (dec_ball_y(state)) {
				state.pos_y_dir = 1;
			}
		}
	}

	// does not check for tile collisions; checks for border collision
	private static void move_x(MachineState state) {
		if (state.pos_x_dir == 1) {
			if (inc_ball_x(state)) {
				state.pos_x_dir = 0;
			}
		} else {
			if (dec_ball_x(state)) {
				state.pos_x_dir = 1;
			}
		}
	}

	private static boolean inc_ball_x(MachineState state) {
		if (state.ball_x_overflow == 1) {
			if (state.ball_x == 0x52) {
				return true;
			} else {
				state.ball_x = state.ball_x + 1;
				return false;
			}
		} else {
			if (state.ball_x == 255) {
				state.ball_x_overflow = 1;
				state.ball_x = 0;
			} else {
				state.ball_x = state.ball_x + 1;
			}
			return false;
		}		
	}
	
	private static boolean dec_ball_x(MachineState state) {
		if (state.ball_x_overflow == 1) {
			if (state.ball_x == 0) {
				state.ball_x = 255;
				state.ball_x_overflow = 0;
			} else {
				state.ball_x = state.ball_x - 1;
			}
			return false;
		} else {
			if (state.ball_x == 0x19) {
				return true;
			} else {
				state.ball_x = state.ball_x - 1;
				return false;
			}
		}
	}

	private static boolean inc_ball_y(MachineState state) {
		if (state.ball_y == 0xfb) {
			return true;
		} else {
			state.ball_y = state.ball_y + 1;
			return false;
		}	
	}
	
	private static boolean dec_ball_y(MachineState state) {
		if (state.ball_y == 0x3a) {
			return true;
		} else {
			state.ball_y = state.ball_y - 1;
			return false;
		}
	}	
	
	private static void check_ball_bat_collision(MachineState state) {
		int diff = state.bat_y - state.ball_y;
		if (diff > 5 || diff < -5) return; // collision not possible, y axis differ
		diff = state.getBallX() - state.getBatX();
		if (diff >= 47) return; // collision not possible
		if (diff >= 41) {
			state.pos_x_dir = 1;
			state.movement_dir = 3;
			bat_collision(state);
			return;
		}
		if (diff >= 35) {
			state.pos_x_dir = 1;
			state.movement_dir = 2;
			bat_collision(state);
			return;			
		}
		if (diff >= 29) {
			state.pos_x_dir = 1;
			state.movement_dir = 1;
			bat_collision(state);
			return;			
		}		
		if (diff >= 21) {
			state.pos_x_dir = 1;
			state.movement_dir = 0;
			bat_collision(state);
			return;			
		}		
		if (diff >= 13) {
			state.pos_x_dir = 0;
			state.movement_dir = 0;
			bat_collision(state);
			return;			
		}		
		if (diff >= 7) {
			state.pos_x_dir = 0;
			state.movement_dir = 1;
			bat_collision(state);
			return;			
		}		
		if (diff >= 1) {
			state.pos_x_dir = 0;
			state.movement_dir = 2;
			bat_collision(state);
			return;			
		}		
		if (diff >= -5) {
			state.pos_x_dir = 0;
			state.movement_dir = 3;
			bat_collision(state);
			return;					
		}		
	}
	
	private static void bat_collision(MachineState state) {
		state.pos_y_dir = 0;
		state.ball_y = state.bat_y - 6;
	}

	// this table is used in converting paddle potentiometer value (0-255) to bat position on screen
	public final static int[] paddle_x_table = {39,37,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,18,17,16,15,
			14,13,12,11,10,9,8,7,6,5,4,3,2,0,255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,
			240,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,220,219,218,217,216,
			215,214,213,212,211,210,209,208,207,206,205,204,202,201,200,199,198,197,196,195,194,193,192,
			191,190,189,188,187,186,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,
			166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,148,147,146,145,144,143,
			142,141,140,139,138,137,136,135,134,133,132,130,129,128,127,126,125,124,123,122,121,120,119,
			118,117,116,115,114,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,94,93,92,
			91,90,89,88,87,86,85,84,83,82,81,80,79,78,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,
			58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,
			27,26,25 };
	
	// this table is used in converting paddle potentiometer value (0-255) to bat position on screen
	public final static int[] paddle_x_overflow_table = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
			1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };
	
/*  
speed4:
 */
	
	private static int[] speed2_table = { 7, 5, 7, 5, 3, 6, 6, 6, 6, 7, 5, 5, 5, 4, 6, 6, 5, 5, 4, 5, 2, 5, 4, 5};
/*
  %00000111,%00000101,%00000111,%00000101,%00000011,%00000110
  %00000110,%00000110,%00000110,%00000111,%00000101,%00000101
  %00000101,%00000100,%00000110,%00000110,%00000101,%00000101
  %00000100,%00000101,%00000010,%00000101,%00000100,%00000101
*/
	
	private static int[] speed3_table = { 14, 7, 13, 14, 13, 10, 14, 13, 10, 10, 12, 13, 8, 6, 10, 9 };
/*
  %00001110,%00000111,%00001101,%00001110
  %00001101,%00001010,%00001110,%00001101
  %00001010,%00001010,%00001100,%00001101
  %00001000,%00000110,%00001010,%00001001
*/
	
	private static int[] speed4_table = { 29, 29, 14, 26, 27, 21, 20, 26, 21, 17, 9, 9 };
/*
  %00011101,%00011101,%00001110
  %00011010,%00011011,%00010101
  %00010100,%00011010,%00010101
  %00010001,%00001001,%00010001	
 */
	
	private static int[] speed6_table = { 59, 110, 106, 117, 82, 101, 34, 81 };
/*			  %00111011,%01101110,
			  %01101010,%01110101,
			  %01010010,%01100101,
			  %00100010,%01010001*/
	
	
}
