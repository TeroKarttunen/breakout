package karski.breakout.simple;

import java.io.IOException;

import karski.breakout.Databank;
import karski.breakout.viceapi.Adapter;

/**
 * IAI is an artificial intelligence for Breakout. It uses Adapter as its interface to the emulator, and Databank as its optional
 * persistent memory. The gameplay consists of starting a new game, waiting for breakpoints which represent an opportunity to make a move, 
 * and at each breakpoint, loading the machine state from the emulator, constructing a game state based on it, selecting the move and then
 * continuing the game. 
 * @author tero
 *
 */
public interface IAI {
	public void setAdapter(Adapter a);
	public void setDatabank(Databank d);
	public void play() throws IOException;	
}
