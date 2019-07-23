package karski.breakout;

import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * MachineState is GameState that represents and has been initialized from actual C64 memory dump and represents all the 
 * variables in game memory. For this reason, it has several variables that are unnecessary for solving the game problem.
 * The private key representation of GameStateWithTiles does not have enough information to reconstruct MachineState, which 
 * means that once the original memory dump and the MachineState object is garbage collected, the information is lost for good.    
 * @author tero
 *
 */
public class MachineState extends GameState implements Cloneable {

	private final static Logger LOGGER = Logger.getLogger(MachineState.class.getName());	
		
	@Override
	public Object clone() {
			return super.clone();
	}
	

	public int ball_x; 
	public int ball_x_overflow;
	public int ball_y;
	public int temp_variable;
	public int pos_y_dir;
	public int pos_x_dir;
	public int tiles_hit_counter;
	public int score_1;
	public int score_2;
	public int score_3;
	public int score_4;
	public int add_to_score;
	public int score_msd_temp;
	public int score_lsd_temp;
	public int bat_x;
	public int bat_y;
	public int bat_x_overflow;
	public int buffer;
	public int pdlx;
	public int pdly;
	public int btna;
	public int btnb;
	public int speed;
	public int movement_counter;
	public int movement_dir;
	public int movement_temp_a;
	public int movement_temp_x;
	public int sound_1_state;
	public int sound_2_state;
	public int sound_3_state;
	public int next_sound;
	public int game_on;		
	public int lastchar;
	public int input_method;
	public int tiles_left;

	public int getBallX() {
		return ball_x + 256 * ball_x_overflow;
	}

	public int getBallY() {
		return ball_y;
	}

	
	public int getBatX() {
		return bat_x + 256 * bat_x_overflow;
	}
	
	public int getBatY() {
		return bat_y;
	}
	
	public boolean isGoingDown() {
		return pos_y_dir == 1;
	}
	
	public boolean isGoingRight() {
		return pos_x_dir == 1;
	}
	
	
	public int getMovementDirToken() {
		if (isGoingRight()) return movement_dir;
		else return (-1 * movement_dir) - 1;
	}
		
	public int getGameState() {
		return game_on;
	}
		
	/**
	 * This constructor constructs the object from the human-readable memory dump of remotemonitor interface. 
	 * @param input the human-readable memory dump
	 */
	public MachineState(String input) {	
		StringTokenizer st = new StringTokenizer(input);
		st.nextElement(); // skip >C:02a7

		ball_x = Integer.valueOf(st.nextToken(), 16);
		ball_x_overflow = Integer.valueOf(st.nextToken(), 16);
		ball_y = Integer.valueOf(st.nextToken(), 16);
		temp_variable = Integer.valueOf(st.nextToken(), 16);
		
		pos_y_dir = Integer.valueOf(st.nextToken(), 16);
		pos_x_dir = Integer.valueOf(st.nextToken(), 16);
		tiles_hit_counter = Integer.valueOf(st.nextToken(), 16);
		score_1 = Integer.valueOf(st.nextToken(), 16);
		
		score_2 = Integer.valueOf(st.nextToken(), 16);
		score_3 = Integer.valueOf(st.nextToken(), 16);
		score_4 = Integer.valueOf(st.nextToken(), 16);
		score_lsd = Integer.valueOf(st.nextToken(), 16);
		
		score_msd = Integer.valueOf(st.nextToken(), 16);
		add_to_score = Integer.valueOf(st.nextToken(), 16);
		score_msd_temp = Integer.valueOf(st.nextToken(), 16);
		score_lsd_temp = Integer.valueOf(st.nextToken(), 16);
		
		st.nextElement(); // skip ................
		st.nextElement(); // skip >C:02b7
		
		bat_x = Integer.valueOf(st.nextToken(), 16);
		bat_y = Integer.valueOf(st.nextToken(), 16);
		bat_x_overflow = Integer.valueOf(st.nextToken(), 16);
		buffer = Integer.valueOf(st.nextToken(), 16);
		
		pdlx = Integer.valueOf(st.nextToken(), 16);
		pdly = Integer.valueOf(st.nextToken(), 16);
		btna = Integer.valueOf(st.nextToken(), 16);
		btnb = Integer.valueOf(st.nextToken(), 16);

		speed = Integer.valueOf(st.nextToken(), 16);
		movement_counter = Integer.valueOf(st.nextToken(), 16);
		movement_dir = Integer.valueOf(st.nextToken(), 16);
		movement_temp_a = Integer.valueOf(st.nextToken(), 16);
		
		movement_temp_x = Integer.valueOf(st.nextToken(), 16);
		current_level = Integer.valueOf(st.nextToken(), 16);
		sound_1_state = Integer.valueOf(st.nextToken(), 16);
		sound_2_state = Integer.valueOf(st.nextToken(), 16);

		st.nextElement(); // skip ................
		st.nextElement(); // skip >C:02c7
		
		sound_3_state = Integer.valueOf(st.nextToken(), 16);
		next_sound = Integer.valueOf(st.nextToken(), 16);
		game_on = Integer.valueOf(st.nextToken(), 16);		
		lastchar = Integer.valueOf(st.nextToken(), 16);

		input_method = Integer.valueOf(st.nextToken(), 16);
		tiles_left = Integer.valueOf(st.nextToken(), 16);
		game_time_1 = Integer.valueOf(st.nextToken(), 16);
		game_time_2 = Integer.valueOf(st.nextToken(), 16);

		game_time_3 = Integer.valueOf(st.nextToken(), 16);
		
		MachineState newState = (MachineState) this.clone();
		PossibleMovesCalculator.move(newState);
		hitpointX = newState.getBallX();
		hitpointY = newState.getBallY();
	}

	/**
	 * This constructor constructs the object from the machine-readable memory dump of remotemonitor interface. 
	 * @param input the machine-readable byte dump
	 */
	public MachineState(byte[] byteResponse) {
		ball_x = convertSignedToUnsigned(byteResponse[0]);
		ball_x_overflow = convertSignedToUnsigned(byteResponse[1]);
		ball_y = convertSignedToUnsigned(byteResponse[2]);
		temp_variable = convertSignedToUnsigned(byteResponse[3]);
		
		pos_y_dir = convertSignedToUnsigned(byteResponse[4]);
		pos_x_dir = convertSignedToUnsigned(byteResponse[5]);
		tiles_hit_counter = convertSignedToUnsigned(byteResponse[6]);
		score_1 = convertSignedToUnsigned(byteResponse[7]);
		
		score_2 = convertSignedToUnsigned(byteResponse[8]);
		score_3 = convertSignedToUnsigned(byteResponse[9]);
		score_4 = convertSignedToUnsigned(byteResponse[10]);
		score_lsd = convertSignedToUnsigned(byteResponse[11]);
		
		score_msd = convertSignedToUnsigned(byteResponse[12]);
		add_to_score = convertSignedToUnsigned(byteResponse[13]);
		score_msd_temp = convertSignedToUnsigned(byteResponse[14]);
		score_lsd_temp = convertSignedToUnsigned(byteResponse[15]);
		
		bat_x = convertSignedToUnsigned(byteResponse[16]);
		bat_y = convertSignedToUnsigned(byteResponse[17]);
		bat_x_overflow = convertSignedToUnsigned(byteResponse[18]);
		buffer = convertSignedToUnsigned(byteResponse[19]);
		
		pdlx = convertSignedToUnsigned(byteResponse[20]);
		pdly = convertSignedToUnsigned(byteResponse[21]);
		btna = convertSignedToUnsigned(byteResponse[22]);
		btnb = convertSignedToUnsigned(byteResponse[23]);

		speed = convertSignedToUnsigned(byteResponse[24]);
		movement_counter = convertSignedToUnsigned(byteResponse[25]);
		movement_dir = convertSignedToUnsigned(byteResponse[26]);
		movement_temp_a = convertSignedToUnsigned(byteResponse[27]);
		
		movement_temp_x = convertSignedToUnsigned(byteResponse[28]);
		current_level = convertSignedToUnsigned(byteResponse[29]);
		sound_1_state = convertSignedToUnsigned(byteResponse[30]);
		sound_2_state = convertSignedToUnsigned(byteResponse[31]);

		sound_3_state = convertSignedToUnsigned(byteResponse[32]);
		next_sound = convertSignedToUnsigned(byteResponse[33]);
		game_on = convertSignedToUnsigned(byteResponse[34]);		
		lastchar = convertSignedToUnsigned(byteResponse[35]);

		input_method = convertSignedToUnsigned(byteResponse[36]);
		tiles_left = convertSignedToUnsigned(byteResponse[37]);
		game_time_1 = convertSignedToUnsigned(byteResponse[38]);
		game_time_2 = convertSignedToUnsigned(byteResponse[39]);

		game_time_3 = convertSignedToUnsigned(byteResponse[40]);
		
		MachineState newState = (MachineState) this.clone();
		PossibleMovesCalculator.move(newState);
		hitpointX = newState.getBallX();
		hitpointY = newState.getBallY();
	}
	
	private int convertSignedToUnsigned(byte b) {
		if (b < 0) return b + 256;
		return b;
	}
		
}
