package karski.breakout.utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import karski.breakout.GameStateWithTiles;
import karski.breakout.PossibleMovesCalculator;
import karski.breakout.PrecalculatedGameState;
import karski.breakout.Tiles;

public class DatasetMaker {

	public static void main(String[] args) throws Exception {
		System.out.println("Program start.");
    	DatasetMaker datasetMaker = new DatasetMaker();
		FileReader isr = new FileReader("D:/work/workspace2/Breakout-AWS/fulldump-2402");
    	BufferedReader br = new BufferedReader(isr);
    	OutputStreamWriter train = new OutputStreamWriter(new FileOutputStream("D:/work/workspace2/breakout-AWS/extendedmodel/breakout-training-train.csv",false));
    	OutputStreamWriter test = new OutputStreamWriter(new FileOutputStream("D:/work/workspace2/breakout-AWS/extendedmodel/breakout-training-test.csv",false));
    	OutputStreamWriter valid = new OutputStreamWriter(new FileOutputStream("D:/work/workspace2/breakout-AWS/extendedmodel/breakout-training-valid.csv",false));
    	Pattern p1 = Pattern.compile("(\\d*),(.*),(.*),(\\d*)");
    	int count = 0;
    	while (br.ready()) {
			String line = br.readLine();
			if (line.startsWith("Move")) continue;
			if (line.equals("")) continue;
			Matcher m = p1.matcher(line);
			if (!m.matches()) {
				System.err.println("line "+line+" does not match pattern.");
				return;
			} else {
				// System.out.println("Read line "+line);
			}
			String Move = m.group(1);
			String OriginalState = m.group(2);
			String ResultState = m.group(3);
			String GameTimeDifference = m.group(4);
			try {
				String make = datasetMaker.make(Move, OriginalState, ResultState, GameTimeDifference);
				// System.out.println(make);
				count++;
				if (count % 100 == 0) {
					valid.append(make+"\n");
					valid.flush();				
				} else if (count % 10 == 0) {
					test.append(make+"\n");
					test.flush();
					
				} else {
					train.append(make+"\n");
					train.flush();
				}
				if (count == 1000) count = 0;
			} catch (IllegalArgumentException e) {
				System.err.println(e.toString());
			}
    	}
    	valid.close();
    	train.close();
    	test.close();

	}

	final public static String DELIM = ","; 
	
	public String make(String Move, String OriginalState, String ResultState, String GameTimeDifference) throws IllegalArgumentException {
		int originalstate_currentLevel = Integer.valueOf(OriginalState.substring(0,1));
		int index = OriginalState.indexOf(":");
		int originalstate_hitpointX = Integer.valueOf(OriginalState.substring(1, index), 16);
		int index2 = OriginalState.indexOf(";");
		int originalstate_score = Integer.valueOf(OriginalState.substring(index+1, index2));
		String tiles = OriginalState.substring(index2+1);
		int byte1 = Integer.valueOf(tiles.substring(0, 1), 16);
		int byte2 = Integer.valueOf(tiles.substring(1, 2), 16);
		int byte3 = Integer.valueOf(tiles.substring(2, 3), 16);
		int byte4 = Integer.valueOf(tiles.substring(3, 4), 16);
		int byte5 = Integer.valueOf(tiles.substring(4, 5), 16);
		int byte6 = Integer.valueOf(tiles.substring(5, 6), 16);
		int byte7 = Integer.valueOf(tiles.substring(6, 7), 16);
		int byte8 = Integer.valueOf(tiles.substring(7, 8), 16);
		int byte9 = Integer.valueOf(tiles.substring(8, 9), 16);
		int byte10 = Integer.valueOf(tiles.substring(9, 10), 16);
		int byte11 = Integer.valueOf(tiles.substring(10, 11), 16);
		int byte12 = Integer.valueOf(tiles.substring(11, 12), 16);
		int byte13 = Integer.valueOf(tiles.substring(12, 13), 16);
		int byte14 = Integer.valueOf(tiles.substring(13, 14), 16);
		int byte15 = Integer.valueOf(tiles.substring(14, 15), 16);
		int byte16 = Integer.valueOf(tiles.substring(15, 16), 16);
		int byte17 = Integer.valueOf(tiles.substring(16, 17), 16);
		int byte18 = Integer.valueOf(tiles.substring(17, 18), 16);
		int byte19 = Integer.valueOf(tiles.substring(18, 19), 16);
		int byte20 = Integer.valueOf(tiles.substring(19, 20), 16);
		int byte21 = Integer.valueOf(tiles.substring(20, 21), 16);
		int byte22 = Integer.valueOf(tiles.substring(21, 22), 16);
		int byte23 = Integer.valueOf(tiles.substring(22, 23), 16);
		int byte24 = Integer.valueOf(tiles.substring(23, 24), 16);
		int byte25 = Integer.valueOf(tiles.substring(24, 25), 16);
		int byte26 = Integer.valueOf(tiles.substring(25, 26), 16);
		int byte27 = Integer.valueOf(tiles.substring(26, 27), 16);
		int byte28 = Integer.valueOf(tiles.substring(27, 28), 16);
		int byte29 = Integer.valueOf(tiles.substring(28, 29), 16);
		int byte30 = Integer.valueOf(tiles.substring(29, 30), 16);
		index = ResultState.indexOf(":");		
		index2 = ResultState.indexOf(";");
		int resultstate_score = Integer.valueOf(ResultState.substring(index+1, index2));
		int score_difference = resultstate_score - originalstate_score;
		// derive variables to help algorithm
		int bat_x = PossibleMovesCalculator.paddle_x_table[Integer.valueOf(Move)];
		int bat_x_overflow = PossibleMovesCalculator.paddle_x_overflow_table[Integer.valueOf(Move)];
		int batX = bat_x;
		if (bat_x_overflow == 1) batX = batX + 256;
		int diff = originalstate_hitpointX - batX;
		//System.out.println("diff "+diff);
		int real = 0;
		switch (diff) {
		case -5:
		case -4:
		case -3:
		case -2:
		case -1:
		case 0:
			real = -4;
			break;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			real = -3;
			break;
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			real = -2;
			break;
		case 13:
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
		case 20:
			real = -1;
			break;
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
		case 26:
		case 27:
		case 28:
			real = 1;
			break;
		case 29:
		case 30:
		case 31:
		case 32:
		case 33:
		case 34:
			real = 2;
			break;
		case 35:
		case 36:
		case 37:
		case 38:
		case 39:
		case 40:
			real = 3;
			break;
		case 41:
		case 42:
		case 43:
		case 44:
		case 45:
		case 46:
			real = 4;
			break;
		default:
			throw new IllegalArgumentException("Unknown diff "+diff);				
		}
		StringBuffer sb = new StringBuffer();
		sb.append(score_difference);
		sb.append(DELIM);
		//sb.append(Move);
		//sb.append(batX);
		sb.append(real);
		sb.append(DELIM);
		sb.append(originalstate_hitpointX);
		sb.append(DELIM);
		sb.append(originalstate_score);
		sb.append(DELIM);
		sb.append(byteAsBinary(byte1));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte2));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte3));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte4));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte5));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte6));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte7));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte8));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte9));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte10));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte11));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte12));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte13));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte14));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte15));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte16));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte17));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte18));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte19));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte20));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte21));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte22));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte23));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte24));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte25));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte26));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte27));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte28));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte29));
		sb.append(DELIM);
		sb.append(byteAsBinary(byte30));
		sb.append(DELIM);
		sb.append(row(byte1, byte2, byte3, byte4, byte5, byte6));
		sb.append(DELIM);
		sb.append(row(byte7, byte8, byte9, byte10, byte11, byte12));
		sb.append(DELIM);
		sb.append(row(byte13, byte14, byte15, byte16, byte17, byte18));
		sb.append(DELIM);
		sb.append(row(byte19, byte20, byte21, byte22, byte23, byte24));
		sb.append(DELIM);
		sb.append(row(byte25, byte26, byte27, byte28, byte29, byte30));
		sb.append(DELIM);
		sb.append(line(byte1, byte7, byte13, byte19, byte25));
		sb.append(DELIM);
		sb.append(line(byte2, byte8, byte14, byte20, byte26));
		sb.append(DELIM);
		sb.append(line(byte3, byte9, byte15, byte21, byte27));
		sb.append(DELIM);
		sb.append(line(byte4, byte10, byte16, byte22, byte28));
		sb.append(DELIM);
		sb.append(line(byte5, byte11, byte17, byte23, byte29));
		sb.append(DELIM);
		sb.append(line(byte6, byte12, byte18, byte24, byte30));
		//sb.append(resultstate_score);
		return sb.toString();
	}

	private String row(int byte1, int byte2, int byte3, int byte4, int byte5, int byte6) {
		int[] bytes = new int[6];
		bytes[0] = byte1;
		bytes[1] = byte2;
		bytes[2] = byte3;
		bytes[3] = byte4;
		bytes[4] = byte5;
		bytes[5] = byte6;
		return row(bytes);
	}
	
	private String row(int[] bytes) {
		int i1 = 0;
		int i2 = 0;
		int i3 = 0;
		int i4 = 0;
		for (int i=0; i<6; i++) {
			if ((bytes[i] & 8) == 8) i1 = i1 + 1;
			if ((bytes[i] & 4) == 4) i2 = i2 + 1;
			if ((bytes[i] & 2) == 2) i3 = i3 + 1;
			if ((bytes[i] & 1) == 1) i4 = i4 + 1;
		}
		return i1+DELIM+i2+DELIM+i3+DELIM+i4;	
	}
	
	private String line(int byte1, int byte2, int byte3, int byte4, int byte5) {
		int[] bytes = new int[5];
		bytes[0] = byte1;
		bytes[1] = byte2;
		bytes[2] = byte3;
		bytes[3] = byte4;
		bytes[4] = byte5;
		return line(bytes);		
	}
	
	private String line(int[] bytes) {
		int i1 = 0;
		for (int i=0; i<5; i++) {
			if ((bytes[i] & 8) == 8) i1 = i1 + 1;
			if ((bytes[i] & 4) == 4) i1 = i1 + 1;
			if ((bytes[i] & 2) == 2) i1 = i1 + 1;
			if ((bytes[i] & 1) == 1) i1 = i1 + 1;
		}
		return String.valueOf(i1);
	}
	
	private String byteAsBinary(int b) {
		switch (b) {
		case 0:
			return "0"+DELIM+"0"+DELIM+"0"+DELIM+"0";
		case 1:
			return "0"+DELIM+"0"+DELIM+"0"+DELIM+"1";
		case 2:
			return "0"+DELIM+"0"+DELIM+"1"+DELIM+"0";
		case 3:
			return "0"+DELIM+"0"+DELIM+"1"+DELIM+"1";
		case 4:
			return "0"+DELIM+"1"+DELIM+"0"+DELIM+"0";
		case 5:
			return "0"+DELIM+"1"+DELIM+"0"+DELIM+"1";
		case 6:
			return "0"+DELIM+"1"+DELIM+"1"+DELIM+"0";
		case 7:
			return "0"+DELIM+"1"+DELIM+"1"+DELIM+"1";
		case 8:
			return "1"+DELIM+"0"+DELIM+"0"+DELIM+"0";
		case 9:
			return "1"+DELIM+"0"+DELIM+"0"+DELIM+"1";
		case 10:
			return "1"+DELIM+"0"+DELIM+"1"+DELIM+"0";
		case 11:
			return "1"+DELIM+"0"+DELIM+"1"+DELIM+"1";
		case 12:
			return "1"+DELIM+"1"+DELIM+"0"+DELIM+"0";
		case 13:
			return "1"+DELIM+"1"+DELIM+"0"+DELIM+"1";
		case 14:
			return "1"+DELIM+"1"+DELIM+"1"+DELIM+"0";
		case 15:
			return "1"+DELIM+"1"+DELIM+"1"+DELIM+"1";
		default:
			return "";
		}
	}	
}
