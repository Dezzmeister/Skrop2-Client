package com.dezzy.skrop2_client.game;

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
	
	private volatile boolean newTCPMessage = false;
	private volatile String tcpMessage = "";
	
	private volatile boolean newUDPMessage = false;
	private volatile String udpMessage = "";
	
	ClientController(final GUI _game) {
		game = _game;
	}
	
	@Override
	public void run() {
		while (isRunning) {
			if (newTCPMessage) {
				game.processServerEvent(tcpMessage);
				newTCPMessage = false;
			}
			
			if (newUDPMessage) {
				game.processUDPServerEvent(udpMessage);
				newUDPMessage = false;
			}
		}
	}
	
	public void relayTCPMessage(final String message) {
		tcpMessage = message;
		newTCPMessage = true;
	}
	
	public void relayUDPMessage(final String message) {
		udpMessage = message;
		newUDPMessage = true;
	}
}
