package com.dezzy.skrop2_client.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.dezzy.skrop2_client.game.ClientController;
import com.dezzy.skrop2_client.game.GUI;

public class Client implements Runnable {
	private final Socket socket;
	private final ClientController clientController;
	private volatile boolean isRunning = true;
	
	private volatile String message = null;
	private volatile boolean sendMessage = false;
	
	public Client(final ClientController _clientController, final String ip, int port) throws UnknownHostException, IOException {
		clientController = _clientController;
		socket = new Socket(ip, port);
	}
	
	@Override
	public void run() {
		try {
			BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream dout = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
			String in = null;
			
			while (isRunning || sendMessage) {				
				if (din.ready()) {
					in = din.readLine();
					
					if (in.equals("quit")) {
						isRunning = false;
						clientController.relayMessage("quit");
						break;
					}
					
					clientController.relayMessage(in);					
				} else {
					if (sendMessage) {
						sendMessage = false;
						
						synchronized(message) { //Prevent sendString() from changing the message as it is being sent
							dout.println(message);
						}
						
						dout.flush();
					}
				}
			}
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
	public synchronized void sendString(final String _message) {
		message = _message;
		sendMessage = true;
	}
	
	public boolean sendingMessage() {
		return sendMessage;
	}
	
	public void stop() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
