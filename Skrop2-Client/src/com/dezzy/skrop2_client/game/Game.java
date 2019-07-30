package com.dezzy.skrop2_client.game;

public class Game {
	public Player[] players;
	public final String gameName;
	public final SkropWinCondition winCondition;
	public final int winConditionArg;
	public final int maxPlayers;
	
	public Game(final String gameCreationInfo) {
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
	}
}
