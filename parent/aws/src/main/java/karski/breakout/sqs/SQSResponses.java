package karski.breakout.sqs;

import java.util.List;

import karski.breakout.queue.Responses;

/**
 * SQSResponses is a Responses container that is compatible with AWS SQS queues. The first line of the message body is the chain argument
 * from a Request, and the rest of the lines are Response objects in the format of
 * [OriginalPrimaryKey]:[Move]->[ResultPrimaryKey],[GameTimeDifference],[ResultGameTime][ResultMoves]
 * @author tero
 *
 */
public class SQSResponses extends Responses {
	
	private SQSResponseReceiver receiver;
	private String receiptHandle;
	
	public SQSResponses(SQSResponseReceiver receiver, String receiptHandle) {
		this.receiver = receiver;
		this.receiptHandle = receiptHandle;
	}
	
	public void markAsReceived() {
		receiver.markAsReceived(receiptHandle);
	}
}
