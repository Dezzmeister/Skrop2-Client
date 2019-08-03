package com.dezzy.skrop2_server.game.skrop2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PassiveWorld implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3909871252377706303L;
	
	public List<Rectangle> rects = new ArrayList<Rectangle>();
	private final transient int maxRects;
	
	/**
	 * The time at which this game world was the latest game world
	 */
	public int timeFrame = 0;
	
	public PassiveWorld(int _maxRects) {
		maxRects = _maxRects;
	}
	
	public synchronized void addRectangle(final Rectangle rect) {
		rects.add(rect);
	}
	
	public synchronized void destroyRectangle(final Rectangle rect) {
		for (int i = rects.size() - 1; i >= 0; i--) {
			Rectangle r = rects.get(i);
			
			if (rect.x == r.x && rect.y == r.y && rect.color == r.color) {
				rects.remove(i);
			}
		}
	}
	
	public synchronized void update() {
		for (int i = rects.size() - 1; i >= 0; i--) {
			if (rects.get(i).isDead()) {
				rects.remove(i);
			} else {
				rects.get(i).grow();
			}
		}
	}
	
	/**
	 * Produces a deep copy of this game world.
	 * 
	 * @return a deep copy of the game world
	 */
	synchronized PassiveWorld copy() {
		PassiveWorld out = new PassiveWorld(maxRects);
		out.timeFrame = timeFrame;
		rects.forEach(r -> out.rects.add(r.copy()));
		
		return out;
	}
}
