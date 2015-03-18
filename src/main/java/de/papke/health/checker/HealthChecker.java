package de.papke.health.checker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * Abstract health checker class.
 * 
 * This class must be overridden by a concrete health checker class.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public abstract class HealthChecker {
	
	public static final String APP_NAME = "health-checker"; 
	
	protected Options options;
	
	public HealthChecker(){}
	
	public HealthChecker(Options options) {
		this.options = options;
	}
	
	/**
	 * Method for printing all available options on the 
	 * command line.
	 */
	protected void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(APP_NAME, options);
	}
	
	/**
	 * Method for executing the health checker.
	 * This method parses the command line arguments and 
	 * passes them to the concrete health checker class.
	 * All exceptions during the health checks are printed
	 * to console here.
	 *  
	 * @param args
	 * @throws Exception
	 */
	public void execute(String[] args) throws Exception {
		
		try {
			
			// create the command line parser
			CommandLineParser parser = new PosixParser();
	
			// parse the command line arguments
			CommandLine commandLine = parser.parse(options, args);

			// do the health check
			check(commandLine);
		}
		catch (Exception e) {
			
			// print all command line options
			printHelp();
			
			// print exception stack trace
			e.printStackTrace();
			
			// set a failure exit code
			System.exit(-1);
		}
	}
	
	/**
	 * Abstract checking method which has to be overridden
	 * by the concrete checker class
	 * 
	 * @param commandLine
	 * @throws Exception
	 */
	public abstract void check(CommandLine commandLine) throws Exception;
}
