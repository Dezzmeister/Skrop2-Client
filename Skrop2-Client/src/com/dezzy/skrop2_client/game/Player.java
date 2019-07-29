package com.dezzy.skrop2_client.game;

import java.awt.Color;

public class Player {
	public int points = 0;
	public int rects = 0;
	
	public final String name;
	public final Color color;
	
	public Player(final String _name, final Color _color) {
		name = _name;
		color = _color;
	}
}
