package com.dezzy.skrop2_client.game;

/**
 * Skrop 2 win conditions. X represents a number of points, rectangles, or seconds.
 * 
 * @author Dezzmeister
 *
 */
public enum SkropWinCondition {
	FIRST_TO_X_POINTS(false, "the winner is the first to X points", "Point Limit"),
	FIRST_TO_X_RECTS(true, "the winner is the first to X rectangles destroyed", "Rect Limit"),
	TIMER_RECTS(true, "the winner is whoever has destroyed the most rectangles after X seconds", "Rect Timer"),
	TIMER_POINTS(false, "the winner is whoever has the most points after X seconds", "Point Timer");
	
	/**
	 * True if this win condition depends on rectangles instead of points
	 */
	public final boolean countRects;
	private final String infoString;
	public final String shortName;
	
	private SkropWinCondition(boolean _countRects, final String _infoString, final String _shortName) {
		countRects = _countRects;
		infoString = _infoString;
		shortName = _shortName;
	}
	
	public String getName() {
		return name();
	}
	
	public String getInfoString(final String winConditionArg) {
		int winGoal = Integer.parseInt(winConditionArg);
		
		return infoString.substring(0, infoString.indexOf("X")) + winGoal + infoString.substring(infoString.indexOf("X") + 1);
	}
}
