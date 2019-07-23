package karski.breakout.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import karski.breakout.GameStateWithTiles;
import karski.breakout.queue.Request;
import karski.breakout.queue.Response;
import karski.breakout.queue.ResponsesReceiver;
import karski.breakout.queue.Responses;

/**
 * SQSResponseReceiver is a ResponsesReceiver that can receive Responses from AWS SQS Queue. Typically there are different queues for normal
 * responses and collateral responses, for example:
 * <P>aws sqs create-queue --queue-name EvaluationResponses --attributes VisibilityTimeout=300
 * <P>aws sqs create-queue --queue-name CollateralResponses --attributes VisibilityTimeout=600
 * @author tero
 *
 */
public class SQSResponseReceiver implements ResponsesReceiver {

	private final static Logger LOGGER = Logger.getLogger(SQSResponseReceiver.class.getName());
	private String queueUrl;
	private AmazonSQS sqs = null;
	
	public SQSResponseReceiver(String queueUrl) {
		if (queueUrl == null) throw new RuntimeException("No queue url given.");
		this.queueUrl = queueUrl;
		sqs = AmazonSQSClientBuilder.defaultClient();
	}
	
	public Responses receiveResponses() {
		List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueUrl).withWaitTimeSeconds(10).withMaxNumberOfMessages(1)).getMessages();
		if (messages.size() == 0) return null;
		if (messages.size() > 1) LOGGER.warning("Warning: more than one message received in RequestReceiver");
		Message message = messages.get(0);

		Responses responses = new SQSResponses(this, message.getReceiptHandle());
		responses.responses = new ArrayList<Response>();
		
		// [OriginalPrimaryKey]:[Move]->[ResultPrimaryKey],[GameTimeDifference],[ResultGameTime][ResultMoves]
		Pattern p = Pattern.compile("(\\d)(\\w*):(\\d*);(\\w*):(\\d*)->(\\d)(\\w*):(\\d*);(\\w*),(\\d*),(\\d*)\\[(.*)\\]");
		
		String body = message.getBody();
		StringTokenizer st = new StringTokenizer(body, "\n");
		while (st.hasMoreTokens()) {
			String responseLine = st.nextToken();
			if (responseLine.equals("")) continue;
			Matcher m = p.matcher(responseLine);
			if (!m.matches()) { // this may be first line with originalRequestChain
				if (responseLine.startsWith("[")) {
					responses.originalRequestChain = responseLine;
				} else {
					LOGGER.warning("Response line "+responseLine+" does not match the expected pattern.");
				}
				continue;
			}
			Response response = new Response();
			responses.responses.add(response);
			String original_currentlevel = m.group(1);
			String original_hitpointx = m.group(2);
			String original_score = m.group(3);
			String original_tiles = m.group(4);
			String move = m.group(5);
			String result_currentlevel = m.group(6);
			String result_hitpointx = m.group(7);
			String result_score = m.group(8);
			String result_tiles = m.group(9);
			String gametimedifference = m.group(10);
			String result_gametime = m.group(11);
			String result_moves = m.group(12);
			ArrayList<String> resultMoves = new ArrayList<String>();
			StringTokenizer moveTokenizer = new StringTokenizer(result_moves, ",");
			while (moveTokenizer.hasMoreTokens()) {
				resultMoves.add(moveTokenizer.nextToken());
			}
			String original_privateKey = original_currentlevel + original_hitpointx + ":" + original_score + ";" + original_tiles;
			String result_privateKey = result_currentlevel + result_hitpointx + ":" + result_score + ";" + result_tiles;
			response.gameTimeDifference = Integer.valueOf(gametimedifference);
			response.original = GameStateWithTiles.buildFromPrimaryKey(original_privateKey, -1, new ArrayList<String>());
			response.result = GameStateWithTiles.buildFromPrimaryKey(result_privateKey, (int) response.gameTimeDifference, resultMoves);
			response.move = move;
			response.resultGameTime = Integer.valueOf(result_gametime);
			response.result.gameState.setGameTime(response.resultGameTime);
			response.original.gameState.setGameTime(response.resultGameTime - response.gameTimeDifference);
			response.result_moves = new String[resultMoves.size()];
			for (int i=0; i<resultMoves.size(); i++) {
				response.result_moves[i] = resultMoves.get(i);
			}
		}
		
		return responses;
	}
	
	public void markAsReceived(String handle) {
		 sqs.deleteMessage(queueUrl, handle);
	}
	
}
