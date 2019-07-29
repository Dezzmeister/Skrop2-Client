package com.dezzy.skrop2_client.game;

import com.dezzy.skrop2_client.net.tcp.Client;

public class Game {
	private Client tcpClient = null;
	private Thread tcpThread = null;
	
	private final GUI gui;
	
	public Game(final GUI _gui) {
		gui = _gui;
	}
	
	public void processServerEvent(String event) {
		
	}
}
