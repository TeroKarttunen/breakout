package karski.breakout.sqs;

import karski.breakout.queue.Request;

/**
 * SQSRequest is a Request message that is compatible with AWS SQS queues. The message body has the format
 * [instruction]:[instructionargument][chain]
 * <P>Note that there is not a separator between argument and the chain; the start of chain can be determined from '[' character
 * @author tero
 *
 */
public class SQSRequest extends Request {

	private SQSRequestReceiver receiver;
	private String receiptHandle;
	
	public SQSRequest(SQSRequestReceiver receiver, String receiptHandle) {
		this.receiver = receiver;
		this.receiptHandle = receiptHandle;
	}
	
	public void markAsReceived() {
		receiver.markAsReceived(receiptHandle);
	}

}
