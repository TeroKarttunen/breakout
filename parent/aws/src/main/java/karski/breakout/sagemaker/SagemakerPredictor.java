package karski.breakout.sagemaker;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;

import karski.breakout.PossibleMovesCalculator;
import karski.breakout.Predictor;

// DEPRECATED CLASS WITH HARDCODED CREDENTIALS, DO NOT SHARE
// THIS WAS USED TO MAKE PREDICTIONS BASED ON SAGEMAKER ENDPOINT, WHEREAS IN REALITY WE USED LOCAL MODEL
public class SagemakerPredictor implements Predictor {

	AmazonSageMakerRuntime runtime;
	
	public SagemakerPredictor() {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJTUSFGEDIUISLCLQ", "FXqAl92eWXeW8R54+8ClefZW7sMcJ//ECsDOkqYx");
		AmazonSageMakerRuntimeClientBuilder builder = AmazonSageMakerRuntimeClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("runtime.sagemaker.eu-west-1.amazonaws.com", "eu-west-1"));
		runtime = builder.build();		
	}
	
	public static void main_old(String[] args) {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJTUSFGEDIUISLCLQ", "FXqAl92eWXeW8R54+8ClefZW7sMcJ//ECsDOkqYx");
		AmazonSageMakerRuntimeClientBuilder builder = AmazonSageMakerRuntimeClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("runtime.sagemaker.eu-west-1.amazonaws.com", "eu-west-1"));
		AmazonSageMakerRuntime runtime = builder.build();

        String body = "4,303,59,0,0,0,0,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0";

        ByteBuffer bodyBuffer = ByteBuffer.wrap(body.getBytes());


        InvokeEndpointRequest request = new InvokeEndpointRequest()
                .withEndpointName("breakout-bigjob-endpoint")
                .withContentType("text/csv")
                .withBody(bodyBuffer);

        InvokeEndpointResult invokeEndpointResult = runtime.invokeEndpoint(request);

        String bodyResponse = new String(invokeEndpointResult.getBody().array());

        System.out.println(bodyResponse);
	}

	public double predict(int movement_direction, int hitpointX, int score, int[] bytes) {
		double[] data = new double[123];
		data[0] = movement_direction;
		data[1] = hitpointX;
		data[2] = score;
		for (int i=0; i<30; i++) {
			int b = bytes[i];
			if (b >= 8) {
				data[3+(i*4)+0] = 1;
				b = b - 8;
			}
			if (b >= 4) {
				data[3+(i*4)+1] = 1;
				b = b - 4;
			}
			if (b >= 2) {
				data[3+(i*4)+2] = 1;
				b = b - 2;
			}
			if (b >= 1) {
				data[3+(i*4)+3] = 1;
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<data.length-1; i++) {
			sb.append(data[i]);
			sb.append(',');
		}
		sb.append(data[data.length-1]);
		ByteBuffer bodyBuffer = ByteBuffer.wrap(sb.toString().getBytes());
        InvokeEndpointRequest request = new InvokeEndpointRequest()
                .withEndpointName("breakout-bigjob-endpoint")
                .withContentType("text/csv")
                .withBody(bodyBuffer);

        InvokeEndpointResult invokeEndpointResult = runtime.invokeEndpoint(request);

        String bodyResponse = new String(invokeEndpointResult.getBody().array());

        return Double.valueOf(bodyResponse);
	}
	
	public double predict(String Move, String OriginalState) {
		int originalstate_currentLevel = Integer.valueOf(OriginalState.substring(0,1));
		int index = OriginalState.indexOf(":");
		int originalstate_hitpointX = Integer.valueOf(OriginalState.substring(1, index), 16);
		int index2 = OriginalState.indexOf(";");
		int originalstate_score = Integer.valueOf(OriginalState.substring(index+1, index2));
		String tiles = OriginalState.substring(index2+1);
		int bytes[] = new int[30];
		bytes[0] = Integer.valueOf(tiles.substring(0, 1), 16);
		bytes[1] = Integer.valueOf(tiles.substring(1, 2), 16);
		bytes[2] = Integer.valueOf(tiles.substring(2, 3), 16);
		bytes[3] = Integer.valueOf(tiles.substring(3, 4), 16);
		bytes[4] = Integer.valueOf(tiles.substring(4, 5), 16);
		bytes[5] = Integer.valueOf(tiles.substring(5, 6), 16);
		bytes[6] = Integer.valueOf(tiles.substring(6, 7), 16);
		bytes[7] = Integer.valueOf(tiles.substring(7, 8), 16);
		bytes[8] = Integer.valueOf(tiles.substring(8, 9), 16);
		bytes[9] = Integer.valueOf(tiles.substring(9, 10), 16);
		bytes[10] = Integer.valueOf(tiles.substring(10, 11), 16);
		bytes[11] = Integer.valueOf(tiles.substring(11, 12), 16);
		bytes[12] = Integer.valueOf(tiles.substring(12, 13), 16);
		bytes[13] = Integer.valueOf(tiles.substring(13, 14), 16);
		bytes[14] = Integer.valueOf(tiles.substring(14, 15), 16);
		bytes[15] = Integer.valueOf(tiles.substring(15, 16), 16);
		bytes[16] = Integer.valueOf(tiles.substring(16, 17), 16);
		bytes[17] = Integer.valueOf(tiles.substring(17, 18), 16);
		bytes[18] = Integer.valueOf(tiles.substring(18, 19), 16);
		bytes[19] = Integer.valueOf(tiles.substring(19, 20), 16);
		bytes[20] = Integer.valueOf(tiles.substring(20, 21), 16);
		bytes[21] = Integer.valueOf(tiles.substring(21, 22), 16);
		bytes[22] = Integer.valueOf(tiles.substring(22, 23), 16);
		bytes[23] = Integer.valueOf(tiles.substring(23, 24), 16);
		bytes[24] = Integer.valueOf(tiles.substring(24, 25), 16);
		bytes[25] = Integer.valueOf(tiles.substring(25, 26), 16);
		bytes[26] = Integer.valueOf(tiles.substring(26, 27), 16);
		bytes[27] = Integer.valueOf(tiles.substring(27, 28), 16);
		bytes[28] = Integer.valueOf(tiles.substring(28, 29), 16);
		bytes[29] = Integer.valueOf(tiles.substring(29, 30), 16);
		// derive variables to help algorithm
		int bat_x = PossibleMovesCalculator.paddle_x_table[Integer.valueOf(Move)];
		int bat_x_overflow = PossibleMovesCalculator.paddle_x_overflow_table[Integer.valueOf(Move)];
		int batX = bat_x;
		if (bat_x_overflow == 1) batX = batX + 256;
		int diff = originalstate_hitpointX - batX;
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
			System.err.println("Unknown diff "+diff);				
		}
		return predict(real, originalstate_hitpointX, originalstate_score, bytes);
	}
	
	public static void main(String[] args)  {
		SagemakerPredictor tester = new SagemakerPredictor();
		long time = System.currentTimeMillis();
		System.out.println(tester.predict("248", "11b:70;008cf20004f60004c800000000cf73"));
		System.out.println(tester.predict("239", "124:70;004e31004f7300cccc000000000272"));
		System.out.println(tester.predict("154", "190:17;0ffff00ffff70ffffe0ff9000ffff7"));
		System.out.println(tester.predict("30", "112f:59;0ffa000f71100fff800f8000031200"));
		System.out.println(tester.predict("97", "1c6:23;0ffff20ffff90fffd90ffff90fc800"));
		System.out.println(tester.predict("236", "133:53;0332300f62000f77100ff7230fd400"));
		System.out.println(tester.predict("146", "198:39;0ffb980ff5100fff700ffe000ff900"));
		System.out.println(tester.predict("71", "1db:60;00ffb100fffb00ece4000000000053"));
		System.out.println(tester.predict("111", "1ab:45;0ffec00fdd100ffb010fb1000ffd00"));
		System.out.println(tester.predict("153", "17f:61;0fffe00fffb00c8000002310001080"));
		System.out.println(tester.predict("8", "1140:16;0fffe60ff7710fffb10ffffb0ffff8"));
		System.out.println(tester.predict("91", "1ee:59;00ff7200fffa00ece0000000000373"));
		System.out.println("Elapsed time "+(System.currentTimeMillis() - time));
	}
}
