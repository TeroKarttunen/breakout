package karski.breakout.viceapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Logger;

/**
 * MonitorConnection wraps a TCP/IP socket and encapsulated the network connection. System property or environment variable
 * STARTUP_DELAY specifies how long we should be waiting or retrying the socket connection before giving up; this prevents
 * racing conditions from occurring.
 * @author tero
 *
 */
public class MonitorConnection {

	private final static Logger LOGGER = Logger.getLogger(MonitorConnection.class.getName());
	
    private Socket mon = null;
    private PrintWriter out = null;
    private InputStreamReader in = null;
    private InetSocketAddress socketAddress;
    private boolean connected = false;
    private int port;
    
    public MonitorConnection(int port) {
    	this.port = port;
    }
    
    public boolean isConnected() {
    	return connected;
    }
    
    public void connect() throws IOException {
    	String startup_delay = System.getProperty("startup_delay");
    	if (startup_delay == null || startup_delay.equals("")) {
    		startup_delay = System.getenv("STARTUP_DELAY");
    	}
    	
    	int delay = -1;
    	if (startup_delay != null && !startup_delay.equals("")) {
    		delay = Integer.valueOf(startup_delay).intValue();
    	}
    	long current = new Date().getTime();
    	
    	socketAddress = new InetSocketAddress("localhost", port);
        try {
        	mon = new Socket();
        	mon.connect(socketAddress, 0);
        } catch (IOException e) {
        	LOGGER.info("IOException while establishing connection: "+e.toString());
        	boolean established = false;
        	while (!established && new Date().getTime() - current < delay) {
        		try {
        			Thread.currentThread().sleep(500);
        			mon = new Socket();
                	mon.connect(socketAddress, 0);
                	established = true;
        		} catch (InterruptedException e2) {
        			
        		}
        		catch (IOException e3) {
        			LOGGER.info("IOException while establishing connection: "+e3.toString());
        		}
        	}
        	if (!established) throw e;
        }
        out = new PrintWriter(mon.getOutputStream(), true);
        in = new InputStreamReader(mon.getInputStream());
        connected = true;
    }

    public void connect(long timeout_in_millis) throws IOException {
    	long startTime = new Date().getTime();
    	while (!connected && new Date().getTime() - startTime < timeout_in_millis) {
    		try {    			
    			connect();
    		} catch (java.net.ConnectException e) {
    			if (new Date().getTime() - startTime >= timeout_in_millis) {
    				throw e;
    			}
    			try {
    				Thread.currentThread().sleep(100);
    			} catch (InterruptedException e2) {}
    		}
    	}
    }
    
    public void waitUntilInput() throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	while (!in.ready()) { 
    		try {
    			Thread.sleep(1); 
    		} catch (InterruptedException e) {}
    	}
    	return;
    }
    
    public String readInput() throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	StringBuffer sb = new StringBuffer();
    	while (in.ready()) {
    		sb.append((char) in.read());
    	}
    	return sb.toString();
    }

    public String readInput(String stringTerminator, boolean alsorest) throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	if (stringTerminator == null || stringTerminator.equals("")) return readInput();
    	StringBuffer sb = new StringBuffer();
    	while (sb.lastIndexOf(stringTerminator) == -1) { // loop until stringTerminator is encountered
    		if (in.ready()) {
    			sb.append((char) in.read());
    		} else {
        		try {
        			Thread.sleep(1); 
        		} catch (InterruptedException e) {}   			
    		}
    	}
    	if (alsorest) {
        	while (in.ready()) {
        		sb.append((char) in.read());
        	}   		
    	}
    	return sb.toString();
    }

    public String waitForBreakpoint() throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	waitUntilInput();
        String input = readInput("(C:$", false);
        String input2 = readInput();
        return input2.substring(0, 4);
    }
    
    public void sendCommand(String command) throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	out.println(command);
    	out.flush();
    }
    
    public void close() {
        try {
        	out.close();
        	in.close();
        	mon.close();
        } catch (IOException e) {} finally { connected = false; }
    }
 
    public void sendByteCommand(byte[] byteCommand) throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	java.io.OutputStream os = mon.getOutputStream();
    	os.write(byteCommand);
    	os.flush();
    }
    
    /**
     * readByteCommandResponse strips some expected bytes from input (basically the answer length) and returns the answer as byte array.
     * Notice that these are signed Java bytes, whereas C-64 usually uses unsigned bytes.
     * @return
     * @throws IOException
     * @throws NotConnectedException
     */
    public byte[] readByteCommandResponse() throws IOException, NotConnectedException {
    	if (!connected) throw new NotConnectedException();
    	java.io.InputStream is = mon.getInputStream();
    	int b = is.read();
    	if (b != 0x02) throw new RuntimeException("Byte value "+b+" received as byte command response.");
    	int answerLength = is.read();
    	b = is.read(); // answer length bits 8-15 
    	if (b != 0x00) throw new RuntimeException("Byte value "+b+" received as byte command response.");
    	b = is.read();// answer length bits 16-23
    	if (b != 0x00) throw new RuntimeException("Byte value "+b+" received as byte command response.");
    	b = is.read();// answer length bits 24-31
    	if (b != 0x00) throw new RuntimeException("Byte value "+b+" received as byte command response.");
    	b = is.read(); // error code
    	if (b != 0x00) throw new RuntimeException("Byte value "+b+" received as byte command response.");
    	byte[] response = new byte[answerLength];
    	int i = 0;
    	while (i != answerLength) {
    		b = is.read();
    		if (b == -1) continue;
    		response[i] = (byte) b;
    		i++;
    	}    	
    	return response;
    }
    
}
