package karski.breakout.dynamodb;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

import karski.breakout.Databank;
import karski.breakout.GameStateWithTiles;
import karski.breakout.PossibleMovesCalculator;

/**
 * DynamoDatabank is a Databank that uses AWS DynamoDB as its backend (data repository). The data is stored to two tables.
 * BreakoutMoves table contains the actual stored moves, whereas BreakoutSolutions contain chains for found solutions. BreakoutMoves table consists
 * of attributes OriginalState and ResultState which store state information in primary key format, Move attribute that holds the paddle position
 * (0-255) as String, GameTimeDifference that contains the time taken to transit from one state to another and ResultMoves that is a set of Strings
 * that are different moves to take from ResultState on. BreakoutSolutions table consists of attributes SolutionChain that is the solution in chain format
 * and GameTime that is the total gametime of the solution. BreakoutSolutions may also contain a temporary attribute CollateralBody that is explained
 * in CollateralHandler.    
 * <P>DynamoDatabank is configurated through environment variables:
 * <UL><LI>DYNAMODB_ENDPOINT contains AWS endpoint URL for the database</LI>
 * <LI>AWS_REGION contains AWS region String for the database</LI></UL>
 * <P>Table creation commands:
 * <P>aws dynamodb create-table --table-name BreakoutMoves --attribute-definitions AttributeName=OriginalState,AttributeType=S AttributeName=Move,AttributeType=N --key-schema AttributeName=OriginalState,KeyType=HASH AttributeName=Move,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=20,WriteCapacityUnits=20
 * <P>aws dynamodb create-table --table-name BreakoutSolutions --attribute-definitions AttributeName=SolutionChain,AttributeType=S AttributeName=GameTime,AttributeType=N --key-schema AttributeName=SolutionChain,KeyType=HASH AttributeName=GameTime,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
 * @author tero
 */
public class DynamoDatabank implements Databank {

	private final static Logger LOGGER = Logger.getLogger(DynamoDatabank.class.getName());
	
	private Table breakout = null;
	private Table breakoutSolutions = null;

	/**
	 * initialize connects to AWS using environment variables as config information and instantiates the table objects.
	 */
	public void initialize() {
        AmazonDynamoDB client = null;
        String customEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        String region = System.getenv("AWS_REGION");
        if (customEndpoint != null && !"".equals(customEndpoint)) {
        	if (region == null || "".equals(region)) region = "eu-north-1";
        	client = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(customEndpoint, region))
                .build();
        } else {
        	client = AmazonDynamoDBClientBuilder.standard()
                    .build();
        }

            DynamoDB dynamoDB = new DynamoDB(client);

            breakout = dynamoDB.getTable("BreakoutMoves");
            breakoutSolutions = dynamoDB.getTable("BreakoutSolutions");
	}
	
	@Override
	public void saveMove(GameStateWithTiles original, String move, GameStateWithTiles result) {
		LOGGER.info("Request to save move.");
		int iMove = Integer.valueOf(move);
		String originalStatePrivateKey = original.getPrimaryKey();
		String[] resultMoves = PossibleMovesCalculator.calculateMoves(result.gameState);
		String resultStatePrivateKey = result.getPrimaryKey();
		int gametimeDifference = (int) (result.gameState.getGameTime() - original.gameState.getGameTime());
		LOGGER.info("Saving item "+originalStatePrivateKey+":"+iMove+"->"+resultStatePrivateKey+","+gametimeDifference);
		Item item = new Item().withPrimaryKey("OriginalState",originalStatePrivateKey,"Move",iMove);
		HashSet<String> resultMovesSet = new HashSet<String>();
		for (int i=0; i<resultMoves.length; i++) resultMovesSet.add(resultMoves[i]);
		item = item.withString("ResultState", resultStatePrivateKey);
		item = item.withInt("GameTimeDifference", gametimeDifference);
		if (resultMoves.length > 0) item = item.withStringSet("ResultMoves", resultMovesSet);
		LOGGER.info(item.toJSON());
		PutItemOutcome outcome = breakout.putItem(item);
		LOGGER.info("Outcome "+outcome.getPutItemResult());			
	}

	@Override
	public GameStateWithTiles loadMove(GameStateWithTiles original, String move) {
		LOGGER.info("Request to load move.");
		int iMove = Integer.valueOf(move);
		String originalStatePrivateKey = original.getPrimaryKey();
		GetItemSpec spec = new GetItemSpec().withPrimaryKey("OriginalState",originalStatePrivateKey,"Move",iMove);
		Item item = breakout.getItem(spec);
		LOGGER.info("Outcome "+item);
		if (item == null) return null;
		String resultStatePrivateKey = item.getString("ResultState"); 
		int gameTimeDifference = item.getInt("GameTimeDifference");
		List<String> resultMoves = item.getList("ResultMoves");
		GameStateWithTiles gswt = GameStateWithTiles.buildFromPrimaryKey(resultStatePrivateKey, gameTimeDifference, resultMoves);
		return gswt;
	}

	@Override
	public void saveSolution(String chain, long gameTime) {
		LOGGER.info("Request to save solution.");
		if (breakoutSolutions == null) {
			LOGGER.severe("BreakoutSolutions table does not exist.");
		} else {
			// check for already existing solution
			GetItemSpec spec = new GetItemSpec().withPrimaryKey("SolutionChain",chain,"GameTime",gameTime);
			Item item = breakoutSolutions.getItem(spec);
			if (item == null) {
				item = new Item().withPrimaryKey("SolutionChain",chain,"GameTime",gameTime);
				item = item.withString("Timestamp", (new java.util.Date()).toString());
				LOGGER.info(item.toJSON());
				PutItemOutcome outcome = breakoutSolutions.putItem(item);
				LOGGER.info("Outcome "+outcome.getPutItemResult());
			} else {
				LOGGER.info("Solution already exists in databank.");
			}
		}
	}

}
