package karski.breakout.sqs;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import karski.breakout.queue.Request;
import karski.breakout.queue.RequestReceiver;

/**
 * SQSRequestSender is a RequestReceiver that can receive Requests from AWS SQS queues. Typically there is one queue, for example:
 * <P>aws sqs create-queue --queue-name EvaluationRequests --attributes VisibilityTimeout=30
 * @author tero
 *
 */
public class SQSRequestReceiver implements RequestReceiver {

	private final static Logger LOGGER = Logger.getLogger(SQSRequestReceiver.class.getName());
	private String queueUrl;
	private AmazonSQS sqs = null;
	
	public SQSRequestReceiver(String queueUrl) {
		if (queueUrl == null) throw new RuntimeException("No queue url given.");
		this.queueUrl = queueUrl;
		sqs = AmazonSQSClientBuilder.defaultClient();
	}
	
	public Request receiveRequest() {
		List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl).withWaitTimeSeconds(10).withMaxNumberOfMessages(1)).getMessages();
		if (messages.size() == 0) return null;
		if (messages.size() > 1) LOGGER.warning("Warning: more than one message received in RequestReceiver");
		Message message = messages.get(0);
		
		Request request = new SQSRequest(this, message.getReceiptHandle());
		
		String body = message.getBody();
		int index1 = body.indexOf(':');
		int index2 = body.indexOf('[');
		request.chain = body.substring(index2);
		request.instruction = body.substring(0, index1);
		request.instructionArgument = body.substring(index1+1, index2);
		int index3 = request.chain.lastIndexOf(' ');
		int index4 = request.chain.lastIndexOf(']');
		request.selectedMove = request.chain.substring(index3+1,index4);
		return request;
	}
	
	public void markAsReceived(String handle) {
		 sqs.deleteMessage(queueUrl, handle);
	}

}
