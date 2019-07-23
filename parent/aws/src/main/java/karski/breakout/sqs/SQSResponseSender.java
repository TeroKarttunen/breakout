package karski.breakout.sqs;

import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import karski.breakout.queue.Response;
import karski.breakout.queue.ResponseSender;

/**
 * SQSResponseSender is a ResponseSender that can send Responses via AWS SQS queues. All the sent Responses must have the same originalRequestChain.
 * @author tero
 *
 */
public class SQSResponseSender implements ResponseSender {

	private final static Logger LOGGER = Logger.getLogger(SQSResponseSender.class.getName());
	private String queueUrl;
	private AmazonSQS sqs = null;
	
	public SQSResponseSender(String queueUrl) {
		if (queueUrl == null) throw new RuntimeException("No queue url given.");
		this.queueUrl = queueUrl;
		sqs = AmazonSQSClientBuilder.defaultClient();
	}
	
	public void sendResponse(List<Response> responses) {
		sendResponse(null, responses);
	}
	
	public void sendResponse(String originalRequestChain, List<Response> responses) {
		StringBuffer sb = new StringBuffer();
		if (originalRequestChain != null && !originalRequestChain.equals("")) {
			sb.append(originalRequestChain);
			sb.append('\n');
		}
		for (Response response : responses) {
			sb.append(response.original.getPrimaryKey());
			sb.append(':');
			sb.append(response.move);
			sb.append("->");
			sb.append(response.result.getPrimaryKey());
			sb.append(',');
			sb.append(response.gameTimeDifference);
			sb.append(',');
			sb.append(response.resultGameTime);
			sb.append('[');
			int ml = response.result_moves.length;
			for (int i=0; i< ml-1; i++) {
				sb.append(response.result_moves[i]);
				sb.append(',');
			}
			if (ml-1 >= 0) sb.append(response.result_moves[ml-1]);
			sb.append(']');
			sb.append('\n');
		}
		
		LOGGER.info("Sending response(s):"+sb.toString());
		
		SendMessageRequest send_msg_request = new SendMessageRequest()
		        .withQueueUrl(queueUrl)
		        .withMessageBody(sb.toString());
		sqs.sendMessage(send_msg_request);		
	}

}
