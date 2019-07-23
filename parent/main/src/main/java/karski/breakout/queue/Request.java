package karski.breakout.queue;

/**
 * Request is a message that instructs the processor to evaluate the new state resulting from starting a new game and playing it to
 * a certain previously known state (by following the chain) and then taking the "selected move".
 * <P>This method contains markAsReceived method that the receiver (processor) calls to indicate it has received the message with its
 * instructions and the sender can expect a reply and need not resend the message.
 * @author tero
 * @see karski.breakout.Chain
 */
public abstract class Request {

	/**
	 * evaluate just the selected move
	 */
	public final static String EVALUATE_MOVE_INSTRUCTION = "evaluate"; 
	/**
	 * evaluate the selected move and collateral gameplay
	 */
	public final static String EVALUATE_MOVE_WITH_COLLATERAL_INSTRUCTION = "evaluateall"; 
	
	/**
	 * the chain to follow to the previously known state
	 */
	public String chain;
	/**
	 * the selected move
	 */
	public String selectedMove;
	/**
	 * one of the instructions, above
	 */
	public String instruction;
	/**
	 * extra arguments for evaluation
	 */
	public String instructionArgument;
	
	/**
	 * the receiver calls this to acknowledge the message
	 */
	public abstract void markAsReceived();
}
