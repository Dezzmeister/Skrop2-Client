package com.dezzy.skrop2_client.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

import javax.swing.JPanel;

import com.dezzy.skrop2_server.game.skrop2.Rectangle;
import com.dezzy.skrop2_server.game.skrop2.PassiveWorld;

public class Game extends JPanel implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2618455596875194889L;
	
	public Player[] players;
	public final String gameName;
	public final SkropWinCondition winCondition;
	public final int winConditionArg;
	public final int maxPlayers;
	
	public PassiveWorld gameWorld;
	
	private volatile boolean isRunning = true;
	
	public Game(final String gameCreationInfo) {
		setLayout(null);
		
		String tempGameName = "Skrop 2 Game";
		SkropWinCondition tempWinCondition = SkropWinCondition.FIRST_TO_X_POINTS;
		int tempWinConditionArg = 500;
		int tempMaxPlayers = 2;
		
		for (String entry : gameCreationInfo.split(" ")) {
			String header = entry.substring(0, entry.indexOf(":"));
			String body = entry.substring(entry.indexOf(":") + 1);
			
			if (header.equals("name")) {
				tempGameName = body.replace('_', ' ');
			} else if (header.equals("win-condition")) {
				for (var cond : SkropWinCondition.values()) {
					if (cond.getName().equals(body)) {
						tempWinCondition = cond;
					}
				}
			} else if (header.equals("win-condition-arg")) {
				tempWinConditionArg = Integer.parseInt(body);
			} else if (header.equals("max-players")) {
				tempMaxPlayers = Integer.parseInt(body);
			}
		}
		
		gameName = tempGameName;
		winCondition = tempWinCondition;
		winConditionArg = tempWinConditionArg;
		maxPlayers = tempMaxPlayers;
		
		gameWorld = new PassiveWorld(10);
	}
	
	/**
	 * Measured in Hz
	 */
	private static final int LOCAL_GAME_TICK_FREQUENCY = 30;
	private long lastGameTick = 0;
	
	@Override
	public void run() {
		while (isRunning) {
			if (System.currentTimeMillis() - lastGameTick > 1000/LOCAL_GAME_TICK_FREQUENCY) {
				gameTick();
				lastGameTick = System.currentTimeMillis();
			}
		}
	}
	
	private void gameTick() {
		if (gameWorld != null) {
			gameWorld.update();
		}
	}
	
	public void start() {
		isRunning = true;
	}
	
	public void stop() {
		isRunning = false;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if (gameWorld != null) {
			for (Rectangle r : gameWorld.rects) {
				int xPos = (int)(r.x() * getWidth());
				int yPos = (int)(r.y() * getHeight());
				
				g2.setColor(new Color(r.color()));
				g2.fillRect(xPos - (int)(r.size()/2.0f * getWidth()), yPos - (int)(r.size()/2.0f * getHeight()), (int)(r.size() * getWidth()), (int)(r.size() * getHeight()));
			}
		}
	}
	
	public void addRectangle(final String encodedRectangle) {
		if (gameWorld != null) {
			gameWorld.addRectangle(Rectangle.decode(encodedRectangle));
		}
	}
}
