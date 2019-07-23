package karski.breakout.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
