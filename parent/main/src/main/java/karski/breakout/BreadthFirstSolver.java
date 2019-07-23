package karski.breakout;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import karski.breakout.queue.RequestSender;
import karski.breakout.queue.Response;
import karski.breakout.queue.ResponsesReceiver;

/**
 * BreadthFirstSolver is a standalone java program that implements a breadth-first search algorithm for searching for solutions.
 * The program is called once for each level of the search. It reads in a text file composed of solution chains, and it generates a new
 * file that serves as input for the subsequent program run. Each run adds all possible links (2-8 new tree nodes) to solution chain, so
 * the output file is typically much larger than the input file.
 * <P>The main method assumes the input file to have name \<FILEPATH_PREFIX\>+\<COUNTER\>, where FILEPATH_PREFIX comes from environment 
 * variable and COUNTER is a static variable in the code. Set COUNTER and GAMETIME_LIMIT before launch.
 * @see Chain 
 * @author tero
 *
 */
public class BreadthFirstSolver implements ResponseListener {

	/**
	 * The last chain node must have this index.
	 */
	public static int COUNTER = 24;
	
	/**
	 * Do not add new nodes into output with gametime larger than this.
	 */
	private static int GAMETIME_LIMIT = 2718;
	
	private final static Logger LOGGER = Logger.getLogger(BreadthFirstSolver.class.getName());
	
	private Databank databank = null;
	private ResponsesReceiver receiver;
	private RequestSender sender;
	private Thread responseConsumerThread;
	private ResponseConsumer responseConsumer;
	private static OutputStreamWriter osw = null;

	private HashMap<String,String> copyOfRequests = new HashMap<String,String>();
	
	public BreadthFirstSolver(RequestSender sender, ResponsesReceiver receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public void setDatabank(Databank databank) {
		this.databank = databank;
	}
	
	public void notify(Response response) {
		String originalPrivateKey = response.original.getPrimaryKey();
		String key = String.valueOf(response.original.gameState.getGameTime())+" "+response.move+" "+originalPrivateKey;
		String request = null;
		synchronized (copyOfRequests) {
			if (copyOfRequests.get(key) == null) {
				LOGGER.warning("Response does not match to requests, key = "+key);
				return;
			}
			request = copyOfRequests.get(key);
			copyOfRequests.remove(key);
		}
		LOGGER.info("Response for request "+request+" received.");
		if (response.result.gameState.getScore() >= 100) {
			String solution = request + "[" + (COUNTER + 1) + ":" + String.valueOf(response.resultGameTime) + " " + response.result.gameState.getScore() + " fi]";
			LOGGER.warning("Solution found: "+solution);
			if (databank != null) {
				databank.saveSolution(solution, response.resultGameTime);
			}
		} else {
			if (response.resultGameTime <= GAMETIME_LIMIT) {
				for (String m : response.result_moves) {
					String newRequest = request + "[" + (COUNTER + 1) + ":" + String.valueOf(response.resultGameTime) + " " + response.result.gameState.getScore() + " " + m +"]";
					LOGGER.fine(newRequest);
					try {
						osw.append(newRequest+"|"+response.result.getPrimaryKey()+"\n");
					} catch (IOException e) {
						LOGGER.severe(e.toString());
					}				
				}
			} else {
				LOGGER.info("Response gametime exceeded ("+response.resultGameTime+" > "+GAMETIME_LIMIT);
			}
		}
		try {
			osw.flush();
		} catch (IOException e) {
			LOGGER.severe(e.toString());
		}
	}	
	
	private Pattern p1 = Pattern.compile("(.*)\\[(\\d*):(\\d*) (\\d*) (\\d*)\\]\\|((\\w*):(\\w*);(\\w*))");
	
	public void go(BufferedReader br) throws IOException {
		// start consumer and register as listener
		responseConsumer = new ResponseConsumer(receiver, null);
		responseConsumer.registerListener(this);
		responseConsumerThread = new Thread(responseConsumer);
		responseConsumerThread.start();

		int totalCounter = 0;
		
		while (br.ready() && System.in.available() == 0) {
			int count = 0;
			while (br.ready() && count < 500) {
				String line = br.readLine();
				if (line.equals("")) continue;
				Matcher m = p1.matcher(line);
				if (!m.matches()) {
					LOGGER.warning("line "+line+" does not match pattern.");
					continue;
				} else {
					LOGGER.info("Evaluating move "+line);
				}
				String earlierChain = m.group(1);
				int counter = Integer.valueOf(m.group(2));
				if (counter != COUNTER) {
					LOGGER.warning("Counter "+counter+" does not match COUNTER.");
					return;
				}
				count++;
				totalCounter++;
				String gametime = m.group(3);
				String score = m.group(4);
				String move = m.group(5);
				String originalPrivateKey = m.group(6);
				String key = gametime + " "+move+" "+originalPrivateKey;
				String value = earlierChain+"["+counter+":"+gametime+" "+score+" "+move+"]";
				copyOfRequests.put(key, value);
				sender.sendRequest(value, "evaluateall", String.valueOf(GAMETIME_LIMIT));	
			}
			int onGoing = -1;
			do {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
				synchronized (copyOfRequests) {
					onGoing = copyOfRequests.size();
				}
				LOGGER.warning("Ongoing requests "+onGoing+"/"+count+" (total sent "+totalCounter+")");
				if (onGoing < 15) {
					synchronized (copyOfRequests) {
						for (String chain : copyOfRequests.values()) {
							LOGGER.info("Ongoing request: "+chain);
						}
					}
				}
			} while (onGoing > 0);
		}
		LOGGER.warning("Total number of requests "+totalCounter);
	}
	
	/**
	 * Arguments must include requestSenderClassName and responseReceiverClassNames, and optionally a database class name for saving
	 * encountered solutions. Example: karski.breakout.sqs.SQSRequestSender karski.breakout.sqs.SQSResponseReceiver karski.breakout.dynamodb.DynamoDatabank
	 * <P>Environment variables REQUEST_QUEUE_URL and RESPONSE_QUEUE_URL are passed on to RequestSender and ResponseReceiver as constructor
	 * arguments. 
	 * @param args RequestSender and ResponseReceiver class names
	 */
    public static void main(String[] args) throws IOException {
    	if (args.length < 2) {
    		System.err.println("Provide RequestSender and ResponseReceiver class names as argument (example: karski.breakout.sqs.SQSRequestSender)");
    		return;
    	}
    	String requestSenderClassName = args[0];
    	String responseReceiverClassName = args[1];
    	String databankClassName = null;
    	
    	if (args.length >= 3) {
    		databankClassName = args[2];
    	}    	     

    	String filepathPrefix = System.getenv("FILEPATH_PREFIX");
    	if (filepathPrefix == null || filepathPrefix.equals("")) {
    		System.err.println("Provide FILEPATH_PREFIX as environment variable.");
    		return;
    	}
    	FileReader isr = new FileReader(filepathPrefix+COUNTER);
    	BufferedReader br = new BufferedReader(isr);
    	osw = new OutputStreamWriter(new FileOutputStream(filepathPrefix+(COUNTER+1),true));
    	
        try {
        	String requestQueueUrl = System.getenv("REQUEST_QUEUE_URL");
        	String responseQueueUrl = System.getenv("RESPONSE_QUEUE_URL");
        	@SuppressWarnings("rawtypes")
			Class[] cArg = new Class[1];
        	cArg[0] = String.class;
        	RequestSender sender = (RequestSender) Class.forName(requestSenderClassName).getDeclaredConstructor(cArg).newInstance(requestQueueUrl);
        	ResponsesReceiver receiver = (ResponsesReceiver) Class.forName(responseReceiverClassName).getDeclaredConstructor(cArg).newInstance(responseQueueUrl);
        	BreadthFirstSolver solver = new BreadthFirstSolver(sender, receiver);
        	if (databankClassName != null) {
            	Databank databank = (Databank) Class.forName(databankClassName).getDeclaredConstructor().newInstance();
            	databank.initialize();
            	solver.setDatabank(databank);
            }		
        	solver.go(br);

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
    }
	
}
