package karski.breakout.sqs;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import karski.breakout.Chain;
import karski.breakout.StateList;
import karski.breakout.queue.Request;
import karski.breakout.queue.RequestSender;

/**
 * SQSRequestSender is a RequestSender that can send Requests via AWS SQS queues.
 * @author tero
 *
 */
public class SQSRequestSender implements RequestSender{

	private final static Logger LOGGER = Logger.getLogger(SQSRequestSender.class.getName());
	private String queueUrl;
	private AmazonSQS sqs = null;
	
	public SQSRequestSender(String queueUrl) {
		if (queueUrl == null) throw new RuntimeException("No queue url given.");
		this.queueUrl = queueUrl;
		sqs = AmazonSQSClientBuilder.defaultClient();
	}
	
	// node's selectedMove is the request to be evaluated
	/**
	 * sendRequest constructs and sends a Request via AWS SQS queue. This version is a helper method that constructs a chain from 
	 * StateList.
	 * @param node a StateList in memory, with the given node being the last evaluated element in chain
	 * @param selectedMove the selected move as String, which is an unevaluated node
	 * @param instruction "evaluate" or "evaluateall"
	 * @param instructionArgument in case of "evaluateall", the maximum gametime to evaluate to
	 */
	public void sendRequest(StateList node, String selectedMove, String instruction, String instructionArgument) {
		// deep clone and discover rootNode
		StateList cl = (StateList) node.clone();
		cl.nextNode = null;
		cl.selectedMove = selectedMove;
		StateList rootNode = cloneStateList(cl);
		StringBuffer sb = new StringBuffer();
		Chain.printChain(rootNode, 1, sb, null);
		sendRequest(sb.toString(), instruction, instructionArgument);
	}
	
	/**
	 * sendRequest constructs and sends a Request via AWS SQS queue. This version allows the chain to be given directly.
	 * @param chain the selected move as chain
	 * @param instruction "evaluate" or "evaluateall"
	 * @param instructionArgument in case of "evaluateall", the maximum gametime to evaluate to
	 */
	public void sendRequest(String chain, String instruction, String instructionArgument) {	
		String start = (instruction == null ? "" : instruction) + ":" + (instructionArgument == null ? "" : instructionArgument);
		
		LOGGER.info("Sending request "+start+ chain);
		SendMessageRequest send_msg_request = new SendMessageRequest()
		        .withQueueUrl(queueUrl)
		        .withMessageBody(start + chain);
		sqs.sendMessage(send_msg_request);		
	}
	
	
	// returns root node; this is a deep clone
	private StateList cloneStateList(StateList last) {
		if (last.previousNode == null) return last;
		StateList cl = (StateList) last.previousNode.clone();
		last.previousNode = cl;
		cl.nextNode = last;
		return cloneStateList(cl);
	}
	
}
