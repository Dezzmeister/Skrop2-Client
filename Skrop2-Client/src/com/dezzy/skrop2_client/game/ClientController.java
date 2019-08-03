package com.dezzy.skrop2_client.game;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class relays messages from the TCP and UDP clients to the game so that the game can process messages while the TCP client is closing a connection (sending "quit").
 * Without a way to process server messages asynchronously, the client needs to wait for {@link GUI#processServerEvent} to return
 * before closing a connection. The problem is that there are cases when {@link GUI#processServerEvent} will try to shut down the client
 * and must do so before returning (so that it can start a new connection). Without this relay system, a a deadlock occurs and the client thread can't
 * terminate.
 * 
 * @author Dezzmeister
 *
 */
public class ClientController implements Runnable {
	private final GUI game;
	private volatile boolean isRunning = true;
	
	private final ConcurrentLinkedQueue<String> tcpQueue;
	
	private final ConcurrentLinkedQueue<String> udpQueue;
	
	ClientController(final GUI _game) {
		game = _game;
		tcpQueue = new ConcurrentLinkedQueue<String>();
		udpQueue = new ConcurrentLinkedQueue<String>();
	}
	
	@Override
	public void run() {
		while (isRunning) {
			if (!tcpQueue.isEmpty()) {
				game.processServerEvent(tcpQueue.poll());
			}
			
			if (!udpQueue.isEmpty()) {
				game.processUDPServerEvent(udpQueue.poll());
			}
		}
	}
	
	public void relayTCPMessage(final String message) {
		tcpQueue.add(message);
	}
	
	public void relayUDPMessage(final String message) {
		udpQueue.add(message);
	}
}
