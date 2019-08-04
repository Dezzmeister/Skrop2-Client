package com.dezzy.skrop2_client.main;

import java.util.Timer;
import java.util.TimerTask;

import com.dezzy.skrop2_client.assets.Config;
import com.dezzy.skrop2_client.assets.Fonts;
import com.dezzy.skrop2_client.game.GUI;

public class Main {
	
	public static void main(String[] args) {
		Config.loadConfig("assets/config/config.txt");
		Fonts.registerFonts();
		
		GUI gameGUI = new GUI(1000, 1000);
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gameGUI.notifyServer();
			}
		}, 0, 1500);		
	}
}
