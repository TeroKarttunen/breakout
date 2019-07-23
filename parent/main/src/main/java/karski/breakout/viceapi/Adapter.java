package karski.breakout.viceapi;

import java.io.IOException;
import java.util.logging.Logger;

import karski.breakout.MachineState;
import karski.breakout.PossibleMovesCalculator;
import karski.breakout.Tiles;

/**
 * Adapter is an interface between emulator and AI-controlled game (Breakout or DistributedBreakout program). 
 * Whereas MonitorConnect represents a TCP/IP connection (transport layer), Adapter handles the application-level
 * communication and communicates with the remote debugging monitor. As C-64 program memory addresses are passed on
 * during this communication, Adapter requires intimate knowledge of the game program.
 * @author tero
 *
 */
public class Adapter {

	private final static Logger LOGGER = Logger.getLogger(Adapter.class.getName());
	
	public static final String BREAKPOINT_INITIALIZATION = "0825"; // breakpoint in program code
	public static final String BREAKPOINT_BALL_AT_THRESHOLD = "091a"; // breakpoint in program code
	public static final String BREAKPOINT_GAME_OVER = "082d"; // breakpoint in program code
	public static final String BREAKPOINT_LEVEL_FINISHED = "0cc5"; // breakpoint in program code
	public static final String START_NEW_GAME_ADDR = "083a"; // a routine in game code to jump to when staring a new game
	public static final String GAME_VARIABLES_START = "02a7"; // memory location for start of game variables
	public static final String GAME_VARIABLES_END = "02cf"; // memory location for the end of game variables
	public static final String GAME_VARIABLES_END_PLUS_ONE = "02d0"; // memory location for the end of game variables plus one
	public static final String GAME_TILES_START = "13d9"; // memory location for start of tiles information
	public static final String GAME_TILES_END = "14c9"; // memory location for end of tiles information
	public static final String GAME_TILES_END_PLUS_ONE = "14ca"; // memory location for end of tiles information plus one
	public static final String BAT_X_VARIABLE = "02b7"; // memory location for bat position, this is the input from AI for the selected move
	public static final String BAT_X_OVERFLOW_VARIABLE = "02b9"; // memory location for bat position, this is the input from AI for the selected move
	
	private MonitorConnection conn;
	
	public Adapter(MonitorConnection conn) {
		this.conn = conn;
	}
	
	public void setMonitorConnection(MonitorConnection conn) {
		this.conn = conn;
	}
	
	public void connect() throws IOException {
		conn.connect();
	}
	
	/**
	 * Connect makes a TCP/IP connection attempt to emulator monitor socket
	 * @param timeout_in_millis
	 * @throws IOException
	 */
	public void connect(long timeout_in_millis) throws IOException {
		if (timeout_in_millis > 0) {
			conn.connect(timeout_in_millis);
		} else {
			conn.connect();
		}
	}
	
	/**
	 * initialize sets up the breakpoints (asks the monitor to stop when they are reached) and starts the game
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		if (!conn.isConnected()) conn.connect();
       	String breakpoint = conn.waitForBreakpoint();
       	LOGGER.info("BREAKPOINT "+breakpoint);
       	LOGGER.info("Sending command break "+BREAKPOINT_BALL_AT_THRESHOLD+".");
        conn.sendCommand("break "+BREAKPOINT_BALL_AT_THRESHOLD); // setup breakpoint
        LOGGER.fine("Command sent."); 

        conn.waitUntilInput();
        String response1 = conn.readInput("(C:$"+BREAKPOINT_INITIALIZATION+") ", true);
        LOGGER.fine("" + response1 + "");

        LOGGER.info("Sending command break "+BREAKPOINT_GAME_OVER+".");
        conn.sendCommand("break "+BREAKPOINT_GAME_OVER); // setup breakpoint
        LOGGER.fine("Command sent."); 

        conn.waitUntilInput();
        String response2 = conn.readInput("(C:$"+BREAKPOINT_INITIALIZATION+") ", true);
        LOGGER.fine("" + response2 + "");        

        LOGGER.info("Sending command break "+BREAKPOINT_LEVEL_FINISHED+".");
        conn.sendCommand("break "+BREAKPOINT_LEVEL_FINISHED); // setup breakpoint
        LOGGER.fine("Command sent."); 

        conn.waitUntilInput();
        String response3 = conn.readInput("(C:$"+BREAKPOINT_INITIALIZATION+") ", true);
        LOGGER.fine("" + response3 + "");                
        
        LOGGER.info("Sending command > 02cb 00 (select paddle)."); 
        conn.sendCommand("> 02cb 00"); // select paddle
        LOGGER.fine("Command sent.");
        
        conn.waitUntilInput();
        String response4 = conn.readInput("(C:$"+BREAKPOINT_INITIALIZATION+") ", true);
        LOGGER.fine("" + response4 + "");        

        LOGGER.info("Sending command > 02bf 03 (maximum speed)."); 
        conn.sendCommand("> 02bf 03"); // select maximum speed
        LOGGER.fine("Command sent.");
        
        conn.waitUntilInput();
        String response5 = conn.readInput("(C:$"+BREAKPOINT_INITIALIZATION+") ", true);
        LOGGER.fine("" + response5 + "");        
        
        LOGGER.info("Sending command g "+START_NEW_GAME_ADDR+"."); 
        conn.sendCommand("g "+START_NEW_GAME_ADDR); // new game
        LOGGER.fine("Command sent.");
	}
	
	/**
	 * waitForBreakpoint blocks until a breakpoint is reached
	 * @return
	 * @throws IOException
	 */
	public String waitForBreakpoint() throws IOException {
		return conn.waitForBreakpoint();
	}
	
	/**
	 * getGameState asks the monitor for memory dump of emulator game state. This method is deprecated in favor of getGameStateMemdump.
	 * @return MachineState object containing the game state
	 * @throws IOException
	 */
	public MachineState getGameState() throws IOException {
		LOGGER.info("Sending command m "+GAME_VARIABLES_START+" "+GAME_VARIABLES_END+".");
		conn.sendCommand("m "+GAME_VARIABLES_START+" "+GAME_VARIABLES_END);
		LOGGER.fine("Command sent."); 

		conn.waitUntilInput();
		String input = conn.readInput("(C:$"+GAME_VARIABLES_END_PLUS_ONE+") ", true);
		LOGGER.fine("" + input + "");
		LOGGER.info("Game state received.");
		
		MachineState state = new MachineState(input);
		return state;
	}
	
	/**
	 * getGameStateMemdump asks the monitor for memory dump of emulator game state. More efficient than getGameState as the memory
	 * is dumped as byte code instead of text.
	 * @return MachineState object containing the game state
	 * @throws IOException
	 */
	public MachineState getGameStateMemdump() throws IOException {
		LOGGER.info("Sending memdump byte command for "+GAME_VARIABLES_START+"-"+GAME_VARIABLES_END+".");
		// this is a magic command string
		byte[] byteCommand = new byte[8];
		byteCommand[0] = 0x02;
		byteCommand[1] = 0x05;
		byteCommand[2] = 0x01;
		byteCommand[3] = myParseByte(GAME_VARIABLES_START.substring(2));
		byteCommand[4] = myParseByte(GAME_VARIABLES_START.substring(0,2));
		byteCommand[5] = myParseByte(GAME_VARIABLES_END.substring(2));
		byteCommand[6] = myParseByte(GAME_VARIABLES_END.substring(0,2));
		byteCommand[7] = 0x00; // memspace = computer
		conn.sendByteCommand(byteCommand);
		LOGGER.fine("Byte command sent."); 

		conn.waitUntilInput();
		
		byte[] byteResponse = conn.readByteCommandResponse();
		
		MachineState state = new MachineState(byteResponse);
		return state;		
	}
	
	private byte myParseByte(String s) {
		int i = Integer.parseInt(s, 16);
		if (i >= 127) {
			return (byte) (i - 256);
		} else {
			return (byte) i;
		}
	}
	

	/**
	 * getTilesMemdump asks the monitor for memory dump of tiles. 
	 * @return Tiles object containing the game state of tiles
	 * @throws IOException
	 */
	public Tiles getTilesMemdump() throws IOException {
		LOGGER.info("Sending memdump byte command for "+GAME_TILES_START+"-"+GAME_TILES_END+".");
		byte[] byteCommand = new byte[8];
		byteCommand[0] = 0x02;
		byteCommand[1] = 0x05;
		byteCommand[2] = 0x01;
		byteCommand[3] = myParseByte(GAME_TILES_START.substring(2));
		byteCommand[4] = myParseByte(GAME_TILES_START.substring(0,2));
		byteCommand[5] = myParseByte(GAME_TILES_END.substring(2));
		byteCommand[6] = myParseByte(GAME_TILES_END.substring(0,2));
		byteCommand[7] = 0x00; // memspace = computer
		conn.sendByteCommand(byteCommand);
		LOGGER.fine("Byte command sent."); 

		conn.waitUntilInput();
		
		byte[] byteResponse = conn.readByteCommandResponse();
		
		Tiles tiles = new Tiles(byteResponse);
		return tiles;		
	}

	
	/**
	 * setPaddle selects the selected move by instructing the debug monitor to change memory contents. Note that despite method name, 
	 * we are not sending over the actual paddle input (range 0-255) but changing the bat location (0-320) based on conversion table.
	 * @param paddle (0-255)
	 * @throws IOException
	 */
	public void setPaddle(int paddle) throws IOException {
		String hexString = Integer.toHexString(PossibleMovesCalculator.paddle_x_table[paddle]);
		LOGGER.info("Sending command > "+BAT_X_VARIABLE+" "+hexString+" (paddle).");
		conn.sendCommand("> "+BAT_X_VARIABLE+" "+hexString);
		LOGGER.fine("Command sent."); 
		conn.waitForBreakpoint();		
		
		hexString = Integer.toHexString(PossibleMovesCalculator.paddle_x_overflow_table[paddle]);
		LOGGER.info("Sending command > "+BAT_X_OVERFLOW_VARIABLE+" "+hexString+" (paddle overflow).");
		conn.sendCommand("> "+BAT_X_OVERFLOW_VARIABLE+" "+hexString);
		LOGGER.fine("Command sent."); 
		conn.waitForBreakpoint();				
	}
	
	/**
	 * continueExecution tells the monitor to continue execution until next breakpoint.
	 * @throws IOException
	 */
	public void continueExecution() throws IOException {
		LOGGER.info("Sending command x.");
		conn.sendCommand("x");
		LOGGER.fine("Command sent."); 
	}

	/**
	 * startNewGame emulates the user pressing F1 key to start a new game.
	 * @throws IOException
	 */
	public void startNewGame() throws IOException {
		LOGGER.info("Sending command > 0277 85 (F1 key pressed).");
		conn.sendCommand("> 0277 85"); // place F1 in keyboard buffer
		LOGGER.fine("Command sent."); 
		conn.waitForBreakpoint();
		LOGGER.info("Sending command > 00c6 01 (keyboard buffer length = 1).");
		conn.sendCommand("> 00c6 01"); // set keyboard buffer length = 1
		LOGGER.fine("Command sent."); 
		conn.waitForBreakpoint();
		// also put ball_y into a safe position to prevent an immediate breakpoint
		LOGGER.info("Sending command > 02a9 b4 (ball_y = #180).");
		conn.sendCommand("> 02a9 b4"); // set keyboard buffer length = 1
		LOGGER.fine("Command sent."); 
		conn.waitForBreakpoint();
	}

	/**
	 * terminate sends the quit command to monitor.
	 * @throws IOException
	 */
	public void terminate() throws IOException {
		LOGGER.info("Sending command quit."); 
        conn.sendCommand("quit"); // terminate emulator
        LOGGER.info("Command sent.");
	}
	
	
}
