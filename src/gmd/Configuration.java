package gmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Configuration {
	
	private static boolean loaded = false;
	private static HashMap<String, String> parameters = new HashMap<String, String>();

	private static void loadParameters() {
		String workingDirectory = System.getProperty("user.dir");
		String[] split = new String[2];
		try {
			BufferedReader reader = new BufferedReader(new FileReader(workingDirectory + File.separator + ".gmd"));
			String line = "";
			while((line = reader.readLine()) != null) {
				split = line.split(": ");
				parameters.put(split[0], split[1]);
			}
			reader.close();
			loaded = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String parameter) {
		if(!loaded) {
			loadParameters();
		}
		return parameters.get(parameter);
	}
}