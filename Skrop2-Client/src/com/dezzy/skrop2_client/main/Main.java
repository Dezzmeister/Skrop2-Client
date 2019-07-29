package com.dezzy.skrop2_client.main;

import com.dezzy.skrop2_client.assets.Config;
import com.dezzy.skrop2_client.assets.Fonts;
import com.dezzy.skrop2_client.game.GUI;

public class Main {
	
	public static void main(String[] args) {
		Config.loadConfig("assets/config/config.txt");
		Fonts.registerFonts();
		
		@SuppressWarnings("unused")
		GUI gameGUI = new GUI(1000, 1000);
	}

}
