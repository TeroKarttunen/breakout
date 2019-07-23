package karski.breakout.lambda.collateral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

/**
 * CollateralHandler is a Lambda function that processes response message in Collateral queue. Each message may contain several 
 * Response objects (one line per object). The first line contains the original request chain and is skipped.
 * Each response is saved to DynamoDB BreakoutMoves table, and if the move completes a solution,
 * also to BreakoutSolutions table.  
 * <P>Note: When CollateralHandler detects a solution, it saves the solution to BreakoutSolutions in an incomplete format. Instead of 
 * a proper "chain", it saves the whole message (all lines) as chain (the last element is not a piece of chain). This results in an erronous entry in the
 * table that must be fixed with "fixsolutions" method in DynamoUtilities. The only reason for this is to keep CollateralHandler simple (a single
 * Java file) and not add any class dependencies. This is a lame excuse and the implementation should be fixed.
 * @author tero
 */
public class CollateralHandler implements RequestHandler<SQSEvent, Void> {

	// the lines in message body have the following syntax:
	// [OriginalPrimaryKey]:[Move]->[ResultPrimaryKey],[GameTimeDifference],[ResultGameTime][ResultMoves]
	private Pattern p = Pattern.compile("((\\d)(\\w*):(\\d*);(\\w*)):(\\d*)->((\\d)(\\w*):(\\d*);(\\w*)),(\\d*),(\\d*)\\[(.*)\\]"); 
	private Table breakout = null;
	private Table breakoutSolutions = null;
	
	@Override
	public Void handleRequest(SQSEvent event, Context context) {
        for(SQSMessage msg : event.getRecords()){
        	String body = msg.getBody();
        	StringTokenizer st = new StringTokenizer(body, "\n");
        	while (st.hasMoreTokens()) {
        		String line = st.nextToken();
        		if (line.equals("")) continue;
        		handleCollateralResponse(body, line, context);
        	}
        }
        return null;
    }
	
	private void handleCollateralResponse(String body, String line, Context context) {
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			if (line.startsWith("[")) {
				context.getLogger().log("Collateral for chain "+line);
			} else {
				context.getLogger().log("Warning: unknown line "+line);
			}
			return;
		}
		String original = m.group(1);
		String move = m.group(6);
		String result = m.group(7);
		String gametimedifference = m.group(12);
		String result_gametime = m.group(13);
		String result_moves = m.group(14);
		ArrayList<String> resultMoves = new ArrayList<String>();
		StringTokenizer moveTokenizer = new StringTokenizer(result_moves, ",");
		while (moveTokenizer.hasMoreTokens()) {
			resultMoves.add(moveTokenizer.nextToken());
		}
		Item item = new Item().withPrimaryKey("OriginalState",original,"Move",Integer.valueOf(move));
		HashSet<String> resultMovesSet = new HashSet<String>();
		for (int i=0; i<resultMoves.size(); i++) resultMovesSet.add(resultMoves.get(i));
		item = item.withString("ResultState", result);
		item = item.withInt("GameTimeDifference", Integer.valueOf(gametimedifference));
		if (resultMoves.size() > 0) item = item.withStringSet("ResultMoves", resultMovesSet);
		if (breakout == null) {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
			DynamoDB dynamoDB = new DynamoDB(client);
			breakout = dynamoDB.getTable("BreakoutMoves");
			breakoutSolutions = dynamoDB.getTable("BreakoutSolutions");
		}
		PutItemOutcome outcome = breakout.putItem(item);
		// check for finish
		if (result.contains(":100;")) {
			item = new Item().withPrimaryKey("SolutionChain",result,"GameTime",Integer.valueOf(result_gametime));
			item = item.withString("Timestamp", (new java.util.Date()).toString());
			item = item.withString("CollateralBody", body);
			outcome = breakoutSolutions.putItem(item);
		}
	}
}
