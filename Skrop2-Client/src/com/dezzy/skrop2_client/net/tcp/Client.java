package com.dezzy.skrop2_client.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dezzy.skrop2_client.game.ClientController;

public class Client implements Runnable {
	private final Socket socket;
	private final ClientController clientController;
	private volatile boolean isRunning = true;
	
	private final ConcurrentLinkedQueue<String> messageQueue;
	
	public Client(final ClientController _clientController, final String ip, int port) throws UnknownHostException, IOException {
		clientController = _clientController;
		
		messageQueue = new ConcurrentLinkedQueue<String>();
		
		socket = new Socket(ip, port);
	}
	
	@Override
	public void run() {
		try {
			BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream dout = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
			String in = null;
			
			while (isRunning || !messageQueue.isEmpty()) {				
				if (din.ready()) {
					in = din.readLine();
					
					if (in.equals("quit")) {
						isRunning = false;
						clientController.relayTCPMessage("quit");
						break;
					}
					
					clientController.relayTCPMessage(in);					
				} else {
					
					String message = null;
					boolean send = false;
					
					while ((message = messageQueue.poll()) != null) {
						dout.println(message);
						send = true;
					}
					
					if (send) {
						dout.flush();
					}
				}
			}
			messageQueue.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isRunning = false;
	}
	
	/**
	 * Tries to send a String to the server.
	 * 
	 * @param _message String to be sent
	 */
	public void sendString(final String _message) {
		messageQueue.add(_message);
	}
	
	public boolean sendingMessage() {
		return !messageQueue.isEmpty();
	}
	
	public void stop() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
