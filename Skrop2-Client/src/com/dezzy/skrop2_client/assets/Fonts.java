package com.dezzy.skrop2_client.assets;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;

public final class Fonts {
	public static final Font HARAMBE_8;
	
	static {
		Font harambe8Temp = null;
		
		try {
			harambe8Temp = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/harambe9/harambe8.ttf")).deriveFont(Font.PLAIN, 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HARAMBE_8 = harambe8Temp;
	}
	
	public static void registerFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(HARAMBE_8);
	}
}
