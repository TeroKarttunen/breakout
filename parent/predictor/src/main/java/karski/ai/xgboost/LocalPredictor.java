package karski.ai.xgboost;

import java.io.IOException;

import karski.breakout.Predictor;

/**
 * LocalPredictor is a wrapper class for karski.ai.xgboost.BreakoutPredictor. It represents a Predictor that runs locally
 * and has no external dependencies.
 * @author tero
 *
 */
public class LocalPredictor implements Predictor {

	private BreakoutPredictor predictor;
	
	/**
	 * Constructor.
	 * @param path file path to XGBoost AI model
	 * @throws IOException
	 */
	public LocalPredictor(String path) throws IOException {
		this.predictor = new BreakoutPredictor(path);
	}

	/**
	 * main function runs some tests on AI model given as argument.
	 * @param args path to AI model
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String path = args[0];
		LocalPredictor tester = new LocalPredictor(path);
		long time = System.currentTimeMillis();
		System.out.println(tester.predict("248", "11b:70;008cf20004f60004c800000000cf73")); // correct result: 1
		System.out.println(tester.predict("239", "124:70;004e31004f7300cccc000000000272")); // correct result: 2
		System.out.println(tester.predict("154", "190:17;0ffff00ffff70ffffe0ff9000ffff7")); // correct result: 1
		System.out.println(tester.predict("30", "112f:59;0ffa000f71100fff800f8000031200")); // correct result: 3
		System.out.println(tester.predict("97", "1c6:23;0ffff20ffff90fffd90ffff90fc800")); // correct result: 2
		System.out.println(tester.predict("236", "133:53;0332300f62000f77100ff7230fd400")); // correct result: 1
		System.out.println(tester.predict("146", "198:39;0ffb980ff5100fff700ffe000ff900")); // correct result: 1
		System.out.println(tester.predict("71", "1db:60;00ffb100fffb00ece4000000000053")); // correct result: 1
		System.out.println(tester.predict("111", "1ab:45;0ffec00fdd100ffb010fb1000ffd00")); // correct result: 1
		System.out.println(tester.predict("153", "17f:61;0fffe00fffb00c8000002310001080")); // correct result: 1
		System.out.println(tester.predict("8", "1140:16;0fffe60ff7710fffb10ffffb0ffff8")); // correct result: 3
		System.out.println(tester.predict("91", "1ee:59;00ff7200fffa00ece0000000000373")); // correct result: 3
		System.out.println("Elapsed time "+(System.currentTimeMillis() - time));
	}

	public double predict(String Move, String OriginalState) {
		return predictor.predict(Move, OriginalState);
	}

	public double predict(int movement_direction, int hitpointX, int score, int[] bytes) {
		return predictor.predict(movement_direction, hitpointX, score, bytes);
	}
	
}
