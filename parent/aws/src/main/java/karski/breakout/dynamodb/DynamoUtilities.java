package karski.breakout.dynamodb;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.PageIterable;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

/**
 * DynamoUtilities is a standalone Java class that contains a number of methods that can be called to examine or adjust the breakout tables.
 * The possible methods (commands) are: "examine" which returns the number of moves in BreakoutMoves (probably broken?), "highscore" which will
 * print out all the solutions ordered by gametime, "fixsolutions" which will fix the solutions found by CollateralHandler to proper syntax, and
 * "dumptofile" which will scan BreakoutMoves and dump its contents to CSV file.  
 * @author tero
 *
 */
public class DynamoUtilities {

	private final static Logger LOGGER = Logger.getLogger(DynamoUtilities.class.getName());
	
	private AmazonDynamoDB client = null;
	private Table breakout = null;
	private Table breakoutSolutions = null;
	private static String customEndpoint = null;
	private static String region = null;
	
	public static void main(String[] args) throws Exception {
    	if (args.length < 1) {
    		System.err.println("Provide command as argument.");
    		return;
    	}
    	
        customEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        region = System.getenv("AWS_REGION");
    	
    	DynamoUtilities util = new DynamoUtilities();
    	
    	switch(args[0]) {
    	case "examine":
    		util.examineTable();
    		break;
    	case "highscore":
    		util.examineSolutions();
    		break;
    	case "fixsolutions":
    		util.fixSolutions();
    		break;
    	case "dumptofile":
    		util.dumptofile(args[1]);
    	}
	}
	
	public void initialize() {
		if (customEndpoint == null || customEndpoint.equals("")) {
			client = AmazonDynamoDBClientBuilder.standard().build();
		} else {
			client = AmazonDynamoDBClientBuilder.standard()
					.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(customEndpoint, region))
					.build();
		}

            DynamoDB dynamoDB = new DynamoDB(client);

            breakout = dynamoDB.getTable("BreakoutMoves");
            breakoutSolutions = dynamoDB.getTable("BreakoutSolutions");
	}

	// this is likely broken as it does not implement paging
	public void examineTable() {
		if (breakout == null) initialize();
				
		ScanRequest scanRequest = new ScanRequest()
				.withTableName("BreakoutMoves");
		
		ScanResult result = client.scan(scanRequest);
		LOGGER.info("Table scan resulted in "+result.getCount()+" items.");
		for (Map<String, AttributeValue> item : result.getItems()) {
		    printItem(item);
		}
		
	}
	
	public void examineSolutions() {
		if (breakoutSolutions == null) initialize();
		
		ArrayList<Map<String, AttributeValue>> items = new ArrayList<Map<String, AttributeValue>>();
		Map<String, AttributeValue> lastKeyEvaluated = null;
		
		do {
			ScanRequest scanRequest = new ScanRequest()
				.withTableName("BreakoutSolutions")
				.withExclusiveStartKey(lastKeyEvaluated);
		
			ScanResult result = client.scan(scanRequest);
			LOGGER.info("Table scan resulted in "+result.getCount()+" items.");
			
			for (Map<String, AttributeValue> item : result.getItems()) {
				items.add(item);
			}
			lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		Collections.sort(items, new Comparator<Map<String, AttributeValue>>() {
			@Override
			public int compare(Map<String, AttributeValue> o1, Map<String, AttributeValue> o2) {
				int gametime1 = Integer.valueOf(o1.get("GameTime").getN());
				int gametime2 = Integer.valueOf(o2.get("GameTime").getN());
				return gametime2 - gametime1;
			}
		});
		for (Map<String, AttributeValue> item : items) printItem(item);
		
	}
	
	public static final int DUMP_COUNT = 20000000; // twenty million records; dumptofile will dump at most this number of items
	
	public void dumptofile(String filename) throws Exception {
		if (breakout == null) initialize();
		
		ArrayList<Map<String, AttributeValue>> items = new ArrayList<Map<String, AttributeValue>>();
		Map<String, AttributeValue> lastKeyEvaluated = null;
		
    	OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename,false));
		
    	int count = 0;
    	
		do {
			ScanRequest scanRequest = new ScanRequest()
				.withTableName("BreakoutMoves")
				.withExclusiveStartKey(lastKeyEvaluated);
		
			ScanResult result = client.scan(scanRequest);
			int newRecords = result.getCount();
			count = count + newRecords;
			LOGGER.info("Table scan resulted in "+newRecords+" items.");
			
			for (Map<String, AttributeValue> item : result.getItems()) {
				osw.append(dump(item)+"\n");
			}
			osw.flush();
			lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null && count < DUMP_COUNT);
		osw.close();
	}
	
	private String dump(Map<String, AttributeValue> item) {
		StringBuffer sb = new StringBuffer();
		String Move = item.get("Move").getN();
		String OriginalState = item.get("OriginalState").getS();
		String ResultState = item.get("ResultState").getS();
		String GameTimeDifference = item.get("GameTimeDifference").getN();
		sb.append(Move);
		sb.append(",");
		sb.append(OriginalState);
		sb.append(",");
		sb.append(ResultState);
		sb.append(",");
		sb.append(GameTimeDifference);
		return sb.toString();
	}
	
	private void printItem(Map<String, AttributeValue> item) {
		Iterator<String> keys = item.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (keys.hasNext()) {
			String key = keys.next();
			sb.append(key);
			sb.append('=');
			sb.append(item.get(key).toString());
			sb.append('|');
		}
		LOGGER.info(sb.toString());
	}

	// the problem to fix: in BreakoutSolutions table, there are entries with chain consisting of ResultPrimaryKey and
	// an extra CollateralBody attribute containing the collateral message object contents. CollateralBody contains a number of lines
	// that represent how the evaluation has been, and that taken in order will complete the solution. Thus, we are able to construct
	// a proper solution chain by adding a piece of chain for each line. For an example of a CollateralBody, see 
	private void fixSolutions() {
		int count = 0;
		if (breakoutSolutions == null) initialize();

		ArrayList<Map<String, AttributeValue>> items = new ArrayList<Map<String, AttributeValue>>();
		Map<String, AttributeValue> lastKeyEvaluated = null;
		
		do {
			ScanRequest scanRequest = new ScanRequest()
				.withTableName("BreakoutSolutions")
				.withExclusiveStartKey(lastKeyEvaluated);
		
			ScanResult result = client.scan(scanRequest);
			LOGGER.info("Table scan resulted in "+result.getCount()+" items.");
			
			for (Map<String, AttributeValue> item : result.getItems()) {
				items.add(item);
			}
			lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		for (Map<String, AttributeValue> item : items) {
			String solutionChain = item.get("SolutionChain").getS();
			// we are expecting a chain, not a primary key
			if (!solutionChain.startsWith("[")) {
				count++;
				fixSolution(item);
				System.out.print(".");
			}
		}
		
		System.out.println(count + " solutions fixed.");
	}
	
	private void fixSolution(Map<String, AttributeValue> item) {
		String solutionChain = item.get("SolutionChain").getS();  // this is not a chain but result primary key
		long gameTime = Long.valueOf(item.get("GameTime").getN());
		String timeStamp = null;
		AttributeValue time = item.get("Timestamp");
		if (time != null) {
			timeStamp = time.getS();
		} else {
			timeStamp = item.get("Timestamp ").getS(); //stupid legacy bug
		}
		if (item.get("CollateralBody") == null) {
			System.err.println("Warning: temporary solution "+solutionChain+" "+gameTime+" has no CollateralBody.");
		} else {
			String collateralBody = item.get("CollateralBody").getS();
			String solution = convertCollateralBodyToChain(collateralBody);
			if (solution != null) {
				// insert new solution
				Item newItem = new Item().withPrimaryKey("SolutionChain",solution,"GameTime",gameTime);
				newItem = newItem.withString("Timestamp", (new java.util.Date()).toString());
				// no CollateralBody
				PutItemOutcome outcome = breakoutSolutions.putItem(newItem);
				// delete old solution
				DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("SolutionChain",solutionChain,"GameTime",gameTime);
				breakoutSolutions.deleteItem(deleteItemSpec);
			}
		}
	}

	// selects the last piece of chain [movenumber:gametime score selectedmove]
	private Pattern p1 = Pattern.compile("(.*)\\[(\\d*):(\\d*) (\\d*) (\\d*)\\]");
	// [OriginalPrimaryKey]:[Move]->[ResultPrimaryKey],[GameTimeDifference],[ResultGameTime][ResultMoves]
	private Pattern p2 = Pattern.compile("((\\d)(\\w*):(\\d*);(\\w*)):(\\d*)->((\\d)(\\w*):(\\d*);(\\w*)),(\\d*),(\\d*)\\[(.*)\\]");
	// legacy format: selects the last piece of chain [movenumber:gametime score selectedmove] when followed by |[PrimaryKey]
	private Pattern p3 = Pattern.compile("(.*)\\[(\\d*):(\\d*) (\\d*) (\\d*)\\]\\|((\\w*):(\\w*);(\\w*))");
	private String convertCollateralBodyToChain(String collateralBody) {
		// we know that the first line of responses message is the original request chain, so we extract the last piece of it
		StringTokenizer st = new StringTokenizer(collateralBody, "\n");
		String chain = st.nextToken();
		Matcher m = p1.matcher(chain);
		if (!m.matches()) { // the first line may have an alternative format (a legacy feature)
			m = p3.matcher(chain);
			if (!m.matches()) { // another bug
				System.err.println("Warning: collateralbody chain "+chain+" does not match pattern.");
				return null;			
			}
		}
		String earlierChain = m.group(1);
		int counter = Integer.valueOf(m.group(2)); // movenumber
		String gametime = m.group(3);
		StringBuffer sb = new StringBuffer(earlierChain);
		while (st.hasMoreTokens()) { // now lets iterate the collateral response messages, adding one piece of chain for each response
			String line = st.nextToken();
			if (line.equals("")) continue;
			m = p2.matcher(line);
			if (!m.matches()) {
				System.err.println("Warning: collateral line"+line+" does not match pattern.");
				return null;
			}
			//String _original = m.group(1);
			String score = m.group(4);
			String _move = m.group(6);
			//String _result = m.group(7);
			sb.append('[');
			sb.append(counter);
			sb.append(':');
			sb.append(gametime);
			sb.append(' ');
			sb.append(score);
			sb.append(' ');
			sb.append(_move);
			sb.append(']');	
			String _result_gametime = m.group(13);
			gametime = _result_gametime;
			counter++;
		}
		// we know that the last line completed the solution, so...
		sb.append('[');
		sb.append(counter);
		sb.append(':');
		sb.append(gametime);
		sb.append(' ');
		sb.append("100 fi]");
		return sb.toString();
	}
		
	
// an example of a CollateralBody, which is a complete responses message
/*	
[1:53 1 14]
1118:1;0fffff0fffff0fffff0ffffb0fffff:14->172:2;0fffff0ffffb0fffff0ffffb0fffff,99,152[165,171,176,182,189,197,203,208]
172:2;0fffff0ffffb0fffff0ffffb0fffff:182->11a:3;0ffffd0ffffb0fffff0ffffb0fffff,62,214[249,255]
11a:3;0ffffd0ffffb0fffff0ffffb0fffff:249->12f:4;0ffffd0ffffb0fffff0ffffb0ffffe,164,378[228,234,240,246,254]
12f:4;0ffffd0ffffb0fffff0ffffb0ffffe:234->114b:5;0ffffd0ffffb0ffffb0ffffb0ffffe,99,477[0,3]
114b:5;0ffffd0ffffb0ffffb0ffffb0ffffe:0->12b:8;0ffffd0ffffb0fffb10ffffb0ffffe,105,582[232,238,244,250]
12b:8;0ffffd0ffffb0fffb10ffffb0ffffe:244->1b5:9;0ffffd0ffff30fffb10ffffb0ffffe,71,653[102,107,113,119,126,134,139,145]
1b5:9;0ffffd0ffff30fffb10ffffb0ffffe:113->1b6:11;0ffffc0fff730fffb10ffffb0ffffe,75,728[101,106,112,118,125,133,138,144]
1b6:11;0ffffc0fff730fffb10ffffb0ffffe:125->1113:12;0ffffc0fff730fffb10ffff30ffffe,62,790[13,18,24,30,37,45,51,56]
1113:12;0ffffc0fff730fffb10ffff30ffffe:37->1135:13;0ffffc0fff730fffb10ffff30ffffc,62,852[0,5,13,18,24]
1135:13;0ffffc0fff730fffb10ffff30ffffc:5->110d:14;0ffffc0fff730fffb10ffff30fffdc,66,918[18,24,30,35,43,51,56,62]
110d:14;0ffffc0fff730fffb10ffff30fffdc:30->110a:16;0ffffc0fff730ffb310ffff30fffdc,80,998[21,27,33,38,46,53,59,65]
110a:16;0ffffc0fff730ffb310ffff30fffdc:27->194:17;0fffec0fff730ffb310ffff30fffdc,105,1103[133,138,144,150,157,165,171,176]
194:17;0fffec0fff730ffb310ffff30fffdc:133->19d:19;0fffec0fff730ffb310fffb10fffdc,175,1278[124,130,136,141,149,156,162,168]
19d:19;0fffec0fff730ffb310fffb10fffdc:124->182:23;0fffec0fff730ffb310ffb100fffdc,185,1463[150,155,161,167,174,182,188,193]
182:23;0fffec0fff730ffb310ffb100fffdc:182->114a:24;0fffec0fff730ffb310ff3100fffdc,80,1543[0,4]
114a:24;0fffec0fff730ffb310ff3100fffdc:4->1141:25;0fffe40fff730ffb310ff3100fffdc,164,1707[0,2,7,13]
1141:25;0fffe40fff730ffb310ff3100fffdc:13->1138:27;0fff600fff730ffb310ff3100fffdc,175,1882[0,2,10,16,21]
1138:27;0fff600fff730ffb310ff3100fffdc:0->1d5:28;0fff600fff730ffb310ff3000fffdc,66,1948[71,77,83,88,96,103,109,115]
1d5:28;0fff600fff730ffb310ff3000fffdc:83->124:29;0fff600fff710ffb310ff3000fffdc,71,2019[239,245,251]
124:29;0fff600fff710ffb310ff3000fffdc:245->121:31;0fff600fff710ffb100ff3000fffdc,105,2124[242,248,254]
121:31;0fff600fff710ffb100ff3000fffdc:254->1c9:32;0fff600fff310ffb100ff3000fffdc,75,2199[83,88,94,100,107,115,120,126]
1c9:32;0fff600fff310ffb100ff3000fffdc:115->112a:33;0fff600fff310ffb100ff3000fffd4,71,2270[0,2,8,16,23,29,35]
112a:33;0fff600fff310ffb100ff3000fffd4:16->1118:34;0fff600fff310ffb100ff3000fffc4,66,2336[8,14,19,25,33,40,46,52]
1118:34;0fff600fff310ffb100ff3000fffc4:8->1141:36;0fff600ff7110ffb100ff3000fffc4,185,2521[0,2,7,13]
1141:36;0fff600ff7110ffb100ff3000fffc4:13->111e:37;0fff200ff7110ffb100ff3000fffc4,175,2696[2,8,14,19,27,35,40,46]
111e:37;0fff200ff7110ffb100ff3000fffc4:27->111e:38;0fff200ff7110ffb100ff3000ffec4,70,2766[2,8,14,19,27,35,40,46]
111e:38;0fff200ff7110ffb100ff3000ffec4:46->1119:39;0ff7200ff7110ffb100ff3000ffec4,185,2951[7,13,18,24,32,39,45,51]
1119:39;0ff7200ff7110ffb100ff3000ffec4:32->111d:40;0ff7200ff7110ffb100ff3000feec4,74,3025[3,9,15,20,28,35,41,47]
111d:40;0ff7200ff7110ffb100ff3000feec4:28->1113:41;0ff7200ff7110ffb100ff3000eeec4,78,3103[13,18,24,30,37,45,51,56]
1113:41;0ff7200ff7110ffb100ff3000eeec4:13->112a:43;0ff7200ff5010ffb100ff3000eeec4,186,3289[0,2,8,16,23,29,35]
112a:43;0ff7200ff5010ffb100ff3000eeec4:0->18d:44;0ff7200ff1010ffb100ff3000eeec4,112,3401[139,145,151,156,164,171,177,183]
18d:44;0ff7200ff1010ffb100ff3000eeec4:183->1a6:46;0ff7200ff1000ff3100ff3000eeec4,186,3587[116,121,127,133,140,148,154,159]
1a6:46;0ff7200ff1000ff3100ff3000eeec4:127->1b1:48;0ff6000ff1000ff3100ff3000eeec4,80,3667[105,111,117,122,130,137,143,149]
1b1:48;0ff6000ff1000ff3100ff3000eeec4:122->142:49;0ff6000fd1000ff3100ff3000eeec4,74,3741[210,216,222,227,235,243,249,255]
142:49;0ff6000fd1000ff3100ff3000eeec4:235->1b7:50;0ff6000dd1000ff3100ff3000eeec4,78,3819[100,105,111,117,124,132,137,143]
1b7:50;0ff6000dd1000ff3100ff3000eeec4:132->1aa:52;0ff6000dd1000ff3100ff2000eee44,80,3899[112,118,123,129,137,144,150,155]
1aa:52;0ff6000dd1000ff3100ff2000eee44:144->1ad:54;0ff6000dd1000ff3100fe2000ee644,84,3983[109,115,120,126,134,141,147,153]
1ad:54;0ff6000dd1000ff3100fe2000ee644:115->110a:55;0ff4000dd1000ff3100fe2000ee644,112,4095[21,27,33,38,46,53,59,65]
110a:55;0ff4000dd1000ff3100fe2000ee644:33->138:56;0ff4000dd1000f73100fe2000ee644,84,4179[220,225,231,237,245,253]
138:56;0ff4000dd1000f73100fe2000ee644:225->125:58;0ff4000dd1000f71000fe2000ee644,112,4291[238,244,250]
125:58;0ff4000dd1000f71000fe2000ee644:244->1113:59;0ff4000dd1000f70000fe2000ee644,112,4403[13,18,24,30,37,45,51,56]
1113:59;0ff4000dd1000f70000fe2000ee644:56->1112:61;0f70000dd1000f70000fe2000ee644,196,4599[14,19,25,31,38,46,52,57]
1112:61;0f70000dd1000f70000fe2000ee644:38->112a:62;0f70000dd1000f70000fe2000ee444,70,4669[0,2,8,16,23,29,35]
112a:62;0f70000dd1000f70000fe2000ee444:35->1e1:63;0f50000dd1000f70000fe2000ee444,196,4865[60,66,71,77,85,92,98,103]
1e1:63;0f50000dd1000f70000fe2000ee444:98->13c:64;0f50000dd1000f70000fe0000ee444,112,4977[216,222,227,233,241,249,255]
13c:64;0f50000dd1000f70000fe0000ee444:216->121:68;0f50000dd1000f70000fe0000ec000,196,5173[242,248,254]
121:68;0f50000dd1000f70000fe0000ec000:242->1ba:71;0f50000dd1000f70000ec0000e4000,207,5380[97,103,108,114,121,129,135,140]
1ba:71;0f50000dd1000f70000ec0000e4000:114->151:72;0f50000dd0000f70000ec0000e4000,70,5450[196,202,207,213,221,228,234,240]
151:72;0f50000dd0000f70000ec0000e4000:196->16e:74;0f50000dd0000f70000ec0000c0000,207,5657[169,174,180,186,193,201,206,212]
16e:74;0f50000dd0000f70000ec0000c0000:201->1140:76;0f50000dd0000f60000e40000c0000,84,5741[0,2,8,14]
1140:76;0f50000dd0000f60000e40000c0000:0->1f0:77;0f50000dd0000f60000e4000080000,78,5819[46,52,57,63,70,78,84,89]
1f0:77;0f50000dd0000f60000e4000080000:46->156:78;0f50000dd0000f40000e4000080000,196,6015[191,197,203,208,216,223,229,235]
156:78;0f50000dd0000f40000e4000080000:235->149:80;0f50000dd0000d00000e4000080000,207,6222[204,209,215,221,228,236,242,248]
149:80;0f50000dd0000d00000e4000080000:209->186:85;0f500008c0000400000e4000080000,152,6374[146,152,157,163,171,178,184,189]
186:85;0f500008c0000400000e4000080000:184->147:88;0f500008c000040000084000000000,162,6536[205,211,217,222,230,238,244,250]
147:88;0f500008c000040000084000000000:244->16a:88;0f500008c000040000084000000000,143,6679[172,178,184,189,197,205,210,216]
16a:88;0f500008c000040000084000000000:210->146:88;0f500008c000040000084000000000,143,6822[206,212,218,223,231,239,245,251]
146:88;0f500008c000040000084000000000:206->13e:89;0f500008c000000000084000000000,237,7059[214,220,225,231,239,247,253]
13e:89;0f500008c000000000084000000000:253->172:89;0f500008c000000000084000000000,143,7202[165,171,176,182,189,197,203,208]
172:89;0f500008c000000000084000000000:165->191:91;0f500008c000000000000000000000,264,7466[136,141,147,153,160,168,173,179]
191:91;0f500008c000000000000000000000:153->122:92;0f5000084000000000000000000000,74,7540[241,247,253]
122:92;0f5000084000000000000000000000:241->19a:93;0f5000080000000000000000000000,237,7777[127,133,138,144,152,159,165,171]
19a:93;0f5000080000000000000000000000:171->129:94;0f5000000000000000000000000000,238,8015[234,240,246,252]
129:94;0f5000000000000000000000000000:234->1148:94;0f5000000000000000000000000000,238,8253[0,1,6]
1148:94;0f5000000000000000000000000000:0->15e:94;0f5000000000000000000000000000,102,8355[184,189,195,201,208,216,222,227]
15e:94;0f5000000000000000000000000000:201->140:95;0f1000000000000000000000000000,74,8429[212,218,223,229,237,245,251]
140:95;0f1000000000000000000000000000:251->170:95;0f1000000000000000000000000000,143,8572[167,172,178,184,191,199,205,210]
170:95;0f1000000000000000000000000000:167->1fd:95;0f1000000000000000000000000000,237,8809[34,39,45,51,58,66,71,77]
1fd:95;0f1000000000000000000000000000:77->1e2:96;071000000000000000000000000000,207,9016[59,65,70,76,84,91,97,103]
1e2:96;071000000000000000000000000000:70->1cc:97;061000000000000000000000000000,102,9118[80,86,91,97,104,112,118,123]
1cc:97;061000000000000000000000000000:97->146:97;061000000000000000000000000000,89,9207[206,212,218,223,231,239,245,251]
146:97;061000000000000000000000000000:218->1bb:98;060000000000000000000000000000,84,9291[96,102,107,113,120,128,134,139]
1bb:98;060000000000000000000000000000:113->136:98;060000000000000000000000000000,89,9380[222,227,233,239,247,255]
136:98;060000000000000000000000000000:255->1135:98;060000000000000000000000000000,102,9482[0,5,13,18,24]
1135:98;060000000000000000000000000000:0->1af:98;060000000000000000000000000000,89,9571[107,113,119,124,132,139,145,151]
1af:98;060000000000000000000000000000:151->154:99;040000000000000000000000000000,238,9809[193,199,205,210,218,225,231,237]
154:99;040000000000000000000000000000:199->1d5:99;040000000000000000000000000000,143,9952[71,77,83,88,96,103,109,115]
1d5:99;040000000000000000000000000000:103->1d1:99;040000000000000000000000000000,102,10054[75,81,86,92,100,107,113,119]
1d1:99;040000000000000000000000000000:86->15d:99;040000000000000000000000000000,102,10156[185,190,196,202,209,217,222,228]
15d:99;040000000000000000000000000000:228->13f:100;000000000000000000000000000000,133,10289[]
*/
}
