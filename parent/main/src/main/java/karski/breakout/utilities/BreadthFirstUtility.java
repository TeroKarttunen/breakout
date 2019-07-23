package karski.breakout.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreadthFirstUtility {

	public static int COUNTER = 22;
	public static int CUTOFF = 2718;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
 
		OutputStreamWriter osw = null;
    	FileReader isr = new FileReader("D:/work/workspace2/breakout/breadth-"+COUNTER);
    	BufferedReader br = new BufferedReader(isr);
    	osw = new OutputStreamWriter(new FileOutputStream("D:/work/workspace2/breakout/breadth-"+(COUNTER)+"-noduplicates",false));		

    	Pattern p1 = Pattern.compile("(.*)\\[(\\d*):(\\d*) (\\d*) (\\d*)\\]\\|((\\w*):(\\w*);(\\w*))");
    	
    	HashMap<String, String> h = new HashMap<String, String>();
    	ArrayList<String> removeList = new ArrayList<String>();
    	HashMap<String, String> removeListHashMap = new HashMap<String, String>();
    	
    	while (br.ready()) {
			String line = br.readLine();
			if (line.equals("")) continue;
			Matcher m = p1.matcher(line);
			if (!m.matches()) {
				System.err.println("line "+line+" does not match pattern.");
				return;
			} else {
				System.out.println("Read line "+line);
			}
			String earlierChain = m.group(1);
			int counter = Integer.valueOf(m.group(2));
			if (counter != COUNTER) {
				System.err.println("Counter "+counter+" does not match COUNTER.");
				return;
			}
			String gametime = m.group(3);
			String score = m.group(4);
			String move = m.group(5);
			String originalPrivateKey = m.group(6);
			String key = move+" "+originalPrivateKey;
			String value = earlierChain+"["+counter+":"+gametime+" "+score+" "+move+"]";
			String e = h.get(key);
			if (Integer.valueOf(gametime) > CUTOFF) {
				// strip unnecessary moves
				removeList.add(value+"|"+originalPrivateKey);
				removeListHashMap.put(value+"|"+originalPrivateKey,"no value");			
				continue; 
			}
			if (e == null) {
				h.put(key,  value);
			} else {
				//osw.append("Earlier:"+e+" New:"+value+"\n");
				System.out.print("Earlier:"+e+" New:"+value+"\n");
				String e_last = e.substring(e.lastIndexOf('['));
				String n_last = value.substring(value.lastIndexOf('['));
				//osw.append(e_last+" "+n_last+"\n");
				if (e_last.compareTo(n_last) > 0) {
					removeList.add(e+"|"+originalPrivateKey);
					removeListHashMap.put(e+"|"+originalPrivateKey,"no value");
					//osw.append(e+"\n");
				} else {
					removeList.add(value+"|"+originalPrivateKey);
					removeListHashMap.put(value+"|"+originalPrivateKey,"no value");
					//osw.append(value+"\n");
				}
			}
    	}
    	System.out.println("removeList size "+removeList.size());
    	br.close();
    	isr.close();
    	
    	isr = new FileReader("D:/work/workspace2/breakout/breadth-"+COUNTER);
    	br = new BufferedReader(isr);
    	while (br.ready()) {
			String line = br.readLine();
			if (line.equals("")) continue;
			if (removeListHashMap.get(line) != null && removeList.contains(line)) {
				continue;
			}
			osw.append(line+"\n");
    	}
    	osw.flush();
    	osw.close();
	}

}
