package karski.breakout.utilities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import karski.breakout.Databank;
import karski.breakout.ResponseConsumer;
import karski.breakout.ResponseListener;
import karski.breakout.queue.Request;
import karski.breakout.queue.RequestSender;
import karski.breakout.queue.Response;
import karski.breakout.queue.ResponsesReceiver;

public class CollateralConsumer implements ResponseListener {
	
	private final static Logger LOGGER = Logger.getLogger(CollateralConsumer.class.getName());
	
	private Databank databank = null;
	private ResponsesReceiver receiver;
	private Thread responseConsumerThread;
	private ResponseConsumer responseConsumer;

	public CollateralConsumer(ResponsesReceiver receiver) {
		this.receiver = receiver;
	}
	
	public void setDatabank(Databank databank) {
		this.databank = databank;
	}
	
	public void notify(Response response) {
		// ResponseConsumer already saved response to DB
		LOGGER.info("Collateral response "+response.original.getPrimaryKey()+"->"+response.result.getPrimaryKey()+" consumed.");
	}
	

	public void go() {
		// start consumer and register as listener
		responseConsumer = new ResponseConsumer(receiver, databank);
		responseConsumer.registerListener(this);
		responseConsumerThread = new Thread(responseConsumer);
		responseConsumerThread.start();
	}
	
	/**
	 * @param args
	 */
    public static void main(String[] args) throws IOException {
    	if (args.length < 1) {
    		System.err.println("Provide ResponseReceiver class name as argument (example: karski.breakout.sqs.SQSRequestReceiver)");
    		return;
    	}
    	String responseReceiverClassName = args[0];
    	String databankClassName = null;
    	
    	if (args.length >= 2) {
    		databankClassName = args[1];
    	}    	     
    	
        try {
        	String collateralQueueUrl = System.getenv("COLLATERAL_QUEUE_URL");
        	Class[] cArg = new Class[1];
        	cArg[0] = String.class;
        	ResponsesReceiver receiver = (ResponsesReceiver) Class.forName(responseReceiverClassName).getDeclaredConstructor(cArg).newInstance(collateralQueueUrl);
        	CollateralConsumer consumer = new CollateralConsumer(receiver);
        	if (databankClassName != null) {
            	Databank databank = (Databank) Class.forName(databankClassName).getDeclaredConstructor().newInstance();
            	databank.initialize();
            	consumer.setDatabank(databank);
            }		
        	consumer.go();

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
