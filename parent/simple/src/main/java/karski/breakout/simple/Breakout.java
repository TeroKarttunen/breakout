package karski.breakout.simple;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import karski.breakout.Databank;
import karski.breakout.viceapi.Adapter;
import karski.breakout.viceapi.MonitorConnection;

/**
 * Breakout is a standalone java program that spawns a local instance of the emulator and uses it to play a game utilizing an AI.
 * This class is used just for the setup, i.e. starting the emulator and the game. The actual gameplay is taken over by the AI class.
 * <P>The program command line arguments is:
 * <UL><LI>AI class name (example: karski.breakout.RandomAI)
 * <P>The required system environment variables are:
 * <LI><B>VICE_COMMAND</B>The location of VICE emulator executive in file system; arguments are added to this
 * <LI><B>PRG_LOCATION</B></UL>The location of VICE emulator C64 Breakout game
 * <P>The possible JVM properties are:
 * <UL><LI><B>vice_port</B>The TCP/IP port to bind VICE emulator remotemonitor interface to (must be free)
 * <LI><B>vice_command</B> (takes precedence over similar environment variable)
 * <LI><B>prg_location</B> (takes precedence over similar environment variable)
 * <LI><B>console</B></UL>If set, passes -console argument to VICE emulator 
 * @author tero
 *
 */
public class Breakout {

	private final static Logger LOGGER = Logger.getLogger(Breakout.class.getName());
	
	/**
	 * @param args
	 */
    public static void main(String[] args) throws IOException {
    	if (args.length < 1) {
    		System.err.println("Provide AI class name as argument (example: karski.breakout.RandomAI)");
    		return;
    	}
    	String aiClassName = args[0];
    	String databankClassName = null;
    	
    	if (args.length >= 2) {
    		databankClassName = args[1];
    	}
    	
    	String portString = System.getProperty("vice_port");
    	int port = 6510;
    	if (portString != null && !portString.equals(""))
    		port = Integer.valueOf(portString);
    	
    	String vice_command = System.getProperty("vice_command");
    	if (vice_command == null || vice_command.equals("")) {
    		vice_command = System.getenv("VICE_COMMAND");
    	}
    	
    	String prg_location = System.getProperty("prg_location");
    	if (prg_location == null || prg_location.equals("")) {
    		prg_location = System.getenv("PRG_LOCATION");
    	}
    	
    	String consoleString = System.getProperty("console");
    	
    	String[] cmdArray = new String[] {vice_command, "-verbose", "-initbreak", "0x0825", "-remotemonitor", "-remotemonitoraddress", "localhost:"+port, prg_location};
    	if (consoleString != null && !consoleString.equals("") && consoleString.equals("true")) {
    		cmdArray[1] = "-console";
    	}
    	
        Runtime.getRuntime().exec(cmdArray);
    	       
    	MonitorConnection conn = new MonitorConnection(port);       

        Adapter adapter = new Adapter(conn);
        
        LOGGER.info("Trying to connect...");
        try {
        	adapter.connect(10000);
        } catch (IOException e) {
        	e.printStackTrace();
            return;
        }                
        LOGGER.info("Connection established.");

        // initialization              
        adapter.initialize();
       
        try {
            IAI ai = (IAI) Class.forName(aiClassName).getDeclaredConstructor().newInstance();
            ai.setAdapter(adapter);
            
            if (databankClassName != null) {
            	Databank databank = (Databank) Class.forName(databankClassName).getDeclaredConstructor().newInstance();
            	databank.initialize();
            	ai.setDatabank(databank);
            }
        	ai.play();
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (IllegalAccessException e) {
        	e.printStackTrace();
        } catch (InstantiationException e) {
        	e.printStackTrace();
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (InvocationTargetException e) {
        	e.printStackTrace();
        }        
        try {
        	adapter.terminate();
        	conn.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}
