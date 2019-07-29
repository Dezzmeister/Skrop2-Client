package com.dezzy.skrop2_client.assets;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Config {
	
	public static String name;
	public static int color;
	
	public static void loadConfig(final String configPath) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(configPath));
			
			for (String entry : lines) {
				String name = entry.substring(0, entry.indexOf("="));
				String value = entry.substring(entry.indexOf("=") + 1);
				
				Field field = Config.class.getDeclaredField(name);
				
				if (field.getType() == int.class) {
					field.set(null, Integer.parseInt(value));
				} else if (field.getType() == float.class) {
					field.set(null, Float.parseFloat(value));
				} else if (field.getType() == String.class) {
					field.set(null, value);
				}
			}
			
			for (Field field : Config.class.getDeclaredFields()) {
				
				if (field.get(null) == null) {
					System.err.println("Config field \"" + field.getName() + "\" has not been initialized!");
					System.exit(-1);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
