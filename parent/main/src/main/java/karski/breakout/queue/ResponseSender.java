package karski.breakout.queue;

import java.util.List;

public interface ResponseSender {

	public void sendResponse(List<Response> responses);
	public void sendResponse(String originalRequestChain, List<Response> responses);
}
