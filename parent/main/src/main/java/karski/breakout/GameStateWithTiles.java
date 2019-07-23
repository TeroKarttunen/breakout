package karski.breakout;

import java.util.ArrayList;
import java.util.List;

/**
 * GameStateWithTiles is, as the name implies, GameState plus Tiles. It completely represents a game state. This object can 
 * be converted into binary presentation (primary key representation) by calling getPrimaryKey, or constructed from such 
 * representation by calling buildFromPrimaryKey. 
 * <P>Note that when built from primary key, the resulting object does not have its time set. The time component is lacking from
 * the key.
 * <P>Primary key has format: <current_level as hex><hitpointX as hex>:<score as decimal>;<tiles> 
 * where tiles is a 30 byte long hex string consisting of 120 bits of information on which tile is set and which tile is clear.
 * See Tiles for format description.  
 * @author tero
 *
 */
public class GameStateWithTiles {

	public GameState gameState;
	public Tiles tiles;
	
	public long gameTimeDifference; // just used as return value in databank interface; bad programming... (a hack)

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GameStateWithTiles)) return false;
		GameStateWithTiles o = (GameStateWithTiles) obj;
		if (gameState.current_level != o.gameState.current_level) return false;
		if (gameState.getScore() != o.gameState.getScore()) return false;
		if (!tiles.equals(o.tiles)) return false;
		
		return (gameState.hitpointX == o.gameState.hitpointX);

	}

	@Override
	public int hashCode() {
		return gameState.hitpointX + (int) gameState.getGameTime() * 200;
	}
	
	public String getPrimaryKey() {
		StringBuffer sb = new StringBuffer();
		sb.append(convertByteToHex(gameState.current_level));
		sb.append(convertByteToHex(gameState.hitpointX));
		sb.append(':');
		sb.append(gameState.getScore());
		sb.append(';');
		sb.append(convertBooleansToHex(tiles.tileMatrix[0][0], tiles.tileMatrix[0][1], tiles.tileMatrix[0][2], tiles.tileMatrix[0][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[1][0], tiles.tileMatrix[1][1], tiles.tileMatrix[1][2], tiles.tileMatrix[1][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[2][0], tiles.tileMatrix[2][1], tiles.tileMatrix[2][2], tiles.tileMatrix[2][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[3][0], tiles.tileMatrix[3][1], tiles.tileMatrix[3][2], tiles.tileMatrix[3][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[4][0], tiles.tileMatrix[4][1], tiles.tileMatrix[4][2], tiles.tileMatrix[4][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[5][0], tiles.tileMatrix[5][1], tiles.tileMatrix[5][2], tiles.tileMatrix[5][3]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[0][4], tiles.tileMatrix[0][5], tiles.tileMatrix[0][6], tiles.tileMatrix[0][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[1][4], tiles.tileMatrix[1][5], tiles.tileMatrix[1][6], tiles.tileMatrix[1][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[2][4], tiles.tileMatrix[2][5], tiles.tileMatrix[2][6], tiles.tileMatrix[2][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[3][4], tiles.tileMatrix[3][5], tiles.tileMatrix[3][6], tiles.tileMatrix[3][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[4][4], tiles.tileMatrix[4][5], tiles.tileMatrix[4][6], tiles.tileMatrix[4][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[5][4], tiles.tileMatrix[5][5], tiles.tileMatrix[5][6], tiles.tileMatrix[5][7]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[0][8], tiles.tileMatrix[0][9], tiles.tileMatrix[0][10], tiles.tileMatrix[0][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[1][8], tiles.tileMatrix[1][9], tiles.tileMatrix[1][10], tiles.tileMatrix[1][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[2][8], tiles.tileMatrix[2][9], tiles.tileMatrix[2][10], tiles.tileMatrix[2][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[3][8], tiles.tileMatrix[3][9], tiles.tileMatrix[3][10], tiles.tileMatrix[3][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[4][8], tiles.tileMatrix[4][9], tiles.tileMatrix[4][10], tiles.tileMatrix[4][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[5][8], tiles.tileMatrix[5][9], tiles.tileMatrix[5][10], tiles.tileMatrix[5][11]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[0][12], tiles.tileMatrix[0][13], tiles.tileMatrix[0][14], tiles.tileMatrix[0][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[1][12], tiles.tileMatrix[1][13], tiles.tileMatrix[1][14], tiles.tileMatrix[1][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[2][12], tiles.tileMatrix[2][13], tiles.tileMatrix[2][14], tiles.tileMatrix[2][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[3][12], tiles.tileMatrix[3][13], tiles.tileMatrix[3][14], tiles.tileMatrix[3][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[4][12], tiles.tileMatrix[4][13], tiles.tileMatrix[4][14], tiles.tileMatrix[4][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[5][12], tiles.tileMatrix[5][13], tiles.tileMatrix[5][14], tiles.tileMatrix[5][15]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[0][16], tiles.tileMatrix[0][17], tiles.tileMatrix[0][18], tiles.tileMatrix[0][19]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[1][16], tiles.tileMatrix[1][17], tiles.tileMatrix[1][18], tiles.tileMatrix[1][19]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[2][16], tiles.tileMatrix[2][17], tiles.tileMatrix[2][18], tiles.tileMatrix[2][19]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[3][16], tiles.tileMatrix[3][17], tiles.tileMatrix[3][18], tiles.tileMatrix[3][19]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[4][16], tiles.tileMatrix[4][17], tiles.tileMatrix[4][18], tiles.tileMatrix[4][19]));
		sb.append(convertBooleansToHex(tiles.tileMatrix[5][16], tiles.tileMatrix[5][17], tiles.tileMatrix[5][18], tiles.tileMatrix[5][19]));
		return sb.toString();
	}
	
	private String convertByteToHex(int x) {
		return Integer.toHexString(x);
	}
	
	private String convertBooleansToHex(boolean a, boolean b, boolean c, boolean d) {
		int v = (d ? 1 : 0) + (c ? 2 : 0) + (b ? 4 : 0) + (a ? 8 : 0);
		return Integer.toHexString(v);
	}
	
	/**
	 * buildFromPrimaryKey constructs a GameStateWithTimes from primary key. GameState instance type will be PrecalculatedGameState,
	 * and it will be populated from moves list for efficiency purposes (pre-calculated moves).
	 * @param primaryKey the primary key to build from
	 * @param gameTimeDifference a hack
	 * @param moves precalculated list of possible moves
	 * @return
	 */
	public static GameStateWithTiles buildFromPrimaryKey(String primaryKey, int gameTimeDifference, List<String> moves) {
		if (moves == null) moves = new ArrayList<String>();
		GameStateWithTiles gswt = new GameStateWithTiles();
		PrecalculatedGameState gameState = new PrecalculatedGameState();
		int currentLevel = Integer.valueOf(primaryKey.substring(0,1));
		gameState.current_level = currentLevel;
		int index = primaryKey.indexOf(":");
		gameState.hitpointX = Integer.valueOf(primaryKey.substring(1, index), 16);
		int index2 = primaryKey.indexOf(";");
		int score = Integer.valueOf(primaryKey.substring(index+1, index2));
		gameState.setScore(score);
		gameState.moves = new String[moves.size()];
		for (int i=0; i<gameState.moves.length; i++) gameState.moves[i] = moves.get(i);
		Tiles tiles = new Tiles(primaryKey.substring(index2+1));
		gswt.gameState = gameState;
		gswt.gameTimeDifference = gameTimeDifference;
		gswt.tiles = tiles;
		return gswt;
	}
}
