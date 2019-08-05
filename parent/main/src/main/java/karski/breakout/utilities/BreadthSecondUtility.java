package karski.breakout.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BreadthSecondUtility is one of the utility classes for exhaustively exploring the solution space via breadth-first algorithm.
 * Whereas BreadthFirstUtility prunes identical game states from needless re-evaluation, BreadthSecondUtility aggressively reduces the
 * solution space based on regression analysis on game time and game score. The idea is to assume that those nodes that have the worst
 * game score : game time ratio are discarded. By adjusting SLACK variable, we can select how aggressively to prune the worst nodes.
 * <P>The output of this program, breadth-COUNTER-suggestion, is the same as BreadthFirstSolver. However, the input file analysis-move-COUNTER.txt
 * has a special format:
 * Column1	Column2	Column3	Column4	Column5
 * 2360	73	[1:54 1 25][2:120 3 23][3:190 5 19][4:264 7 38][5:409 18 202][6:480 19 143][7:862 33 157][8:1297 47 152][9:1482 49 170][10:1553 50 186][11:1628 51 155][12:1727 52 40][13:2032 71 15][14:2196 72 39][15:2360 73 64]	"1dd:73;0008730008f10008cc00000000c737"	75,07598703
 * Column1 = game time
 * Column2 = game score
 * Column3 = chain
 * Column4 = primary key
 * Column5 = game time : game score ratio
 * <P>analysis-move-COUNTER.txt can be produces by for example using Microsoft Excel. The Column5 ratio is calculated with regression analysis,
 * and it should be based on coefficients: game time * x variable coefficient + intercept. This number tells what the score should at least be at
 * this game time; if the actual score (column2) is worse, the row is discarded. 
 * @author tero
 *
 */
public class BreadthSecondUtility {

	public static int COUNTER = 19;
	public static double SLACK = 0.5;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
 
		OutputStreamWriter osw = null;
    	FileReader isr = new FileReader("analysis-move-"+COUNTER+".txt");
    	BufferedReader br = new BufferedReader(isr);
    	osw = new OutputStreamWriter(new FileOutputStream("D:/work/workspace2/breakout/breadth-"+(COUNTER)+"-suggestion",false));		

    	//Pattern p1 = Pattern.compile("(.*)\\[(\\d*):(\\d*) (\\d*) (\\d*)\\]\\|((\\w*):(\\w*);(\\w*))");
    	Pattern p2 = Pattern.compile("(\\d*)\\t(\\d*)\\t(.*)\\t\"(.*)\"\\t(.*)");
    	
    	HashMap<String, String> h = new HashMap<String, String>();
    	ArrayList<String> removeList = new ArrayList<String>();
    	
    	while (br.ready()) {
			String line = br.readLine();
			if (line.startsWith("Column")) continue;
			if (line.equals("")) continue;
			Matcher m = p2.matcher(line);
			if (!m.matches()) {
				System.err.println("line "+line+" does not match pattern.");
				return;
			} else {
				System.out.println("Read line "+line);
			}
			String gametime = m.group(1);
			String score = m.group(2);
			int s = Integer.valueOf(score);
			String chain = m.group(3);
			String originalPrivateKey = m.group(4);
			String estimate = m.group(5);
			estimate = estimate.replace(',', '.');
			double est = Double.valueOf(estimate);
			if (s > est + SLACK) {
				osw.append(chain+"|"+originalPrivateKey+"\n");
			}
    	}
    	osw.flush();
    	osw.close();
	}

}
