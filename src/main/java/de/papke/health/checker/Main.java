package de.papke.health.checker;

import java.util.ResourceBundle;

/**
 * Starter class for health checker application.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class Main {
	
	private static ResourceBundle applicationProperties = ResourceBundle.getBundle("maven");

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		HealthCheckerFactory.create().execute(args);
	}
	
	public static String getApplicationProperty(String key) {
		return applicationProperties.getString(key);
	}
}
