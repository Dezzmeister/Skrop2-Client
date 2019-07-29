package com.dezzy.skrop2_client.net.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import com.dezzy.skrop2_client.game.Game;

public class Client implements Runnable {
	private final Socket socket;
	private final Game game;
	private volatile boolean isRunning = true;
	
	private volatile String message = null;
	private volatile boolean sendMessage = false;
	
	public Client(final Game _game, final String ip, int port) throws UnknownHostException, IOException {
		game = _game;
		socket = new Socket(ip, port);
	}
	
	@Override
	public void run() {
		try {
			while (isRunning) {
				BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
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
							dout.writeChars(message);
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
	}
	
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
