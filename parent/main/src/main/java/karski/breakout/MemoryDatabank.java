package karski.breakout;

import java.util.HashMap;

/**
 * MemoryDatabank is a HashMap-based implementation of Databank that does not actually persist the information. The information is
 * lost once this object is garbage-collected.
 * @author tero
 *
 */
public class MemoryDatabank implements Databank {
	
	private HashMap<GameStateWithTiles,HashMap<String,GameStateWithTiles>> databank = new HashMap<GameStateWithTiles,HashMap<String,GameStateWithTiles>>(); 
	public HashMap<Long, String> solutions = new HashMap<Long, String>();
	
	public void saveMove(GameStateWithTiles original, String move, GameStateWithTiles result) {
		HashMap<String, GameStateWithTiles> m = databank.get(original);
		if (m == null) {
			m = new HashMap<String, GameStateWithTiles>();
			databank.put(original, m);
		}
		result.gameTimeDifference = result.gameState.getGameTime() - original.gameState.getGameTime();
		m.put(move, result);
		
	}

	public GameStateWithTiles loadMove(GameStateWithTiles original, String move) {
		HashMap<String, GameStateWithTiles> m = databank.get(original);
		if (m == null) return null;
		return m.get(move);
	}

	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	public void saveSolution(String chain, long gameTime) {
		solutions.put(gameTime, chain);		
	}
}
