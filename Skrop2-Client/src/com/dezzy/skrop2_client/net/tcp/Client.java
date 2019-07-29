package com.dezzy.skrop2_client.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.dezzy.skrop2_client.game.GUI;

public class Client implements Runnable {
	private final Socket socket;
	private final GUI game;
	private volatile boolean isRunning = true;
	
	private volatile String message = null;
	private volatile boolean sendMessage = false;
	private volatile boolean stopped = false;
	
	public Client(final GUI _game, final String ip, int port) throws UnknownHostException, IOException {
		game = _game;
		socket = new Socket(ip, port);
	}
	
	@Override
	public void run() {
		try {			
			while (isRunning || sendMessage) {				
				BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream dout = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
				String in = null;
				
				if (din.ready()) {						
					in = din.readLine();
					
					if (in.equals("quit")) {
						isRunning = false;
						game.processServerEvent("quit");
						break;
					}
					
					game.processServerEvent(in);					
				} else {
					if (sendMessage) {
						
						synchronized(message) { //Prevent sendString() from changing the message as it is being sent
							dout.println(message);
						}
						dout.flush();
						
						sendMessage = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		isRunning = false;
		stopped = true;
	}
	
	/**
	 * Blocks until the TCP server is stopped.
	 */
	public void waitUntilStopped() {
		while (!stopped);
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
	
	public void stop() {
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
