package com.dezzy.skrop2_client.main;

import com.dezzy.skrop2_client.assets.Fonts;
import com.dezzy.skrop2_client.game.GUI;

public class Main {

	public static void main(String[] args) {
		Fonts.registerFonts();
		GUI gameGUI = new GUI(1000, 1000);
	}

}
