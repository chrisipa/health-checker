package de.papke.health.checker;

import org.apache.commons.cli.CommandLine;

import de.papke.health.checker.api.Type;

/**
 * Default health checker class.
 * 
 * This class is chosen if no health checker could be found
 * for the given type.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class DefaultHealthChecker extends HealthChecker {

	public DefaultHealthChecker() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see de.papke.health.checker.HealthChecker#execute(java.lang.String[])
	 */
	@Override
	public void execute(String[] args) throws Exception {
		check(null);
	}

	/* (non-Javadoc)
	 * @see de.papke.health.checker.HealthChecker#check(org.apache.commons.cli.CommandLine)
	 */
	@Override
	public void check(CommandLine commandLine) throws Exception {
		
		Type[] types = Type.values();
		
		String availableTypeString = "";
		for (int i = 0; i < types.length; i++) {
			
			Type type = types[i];
			availableTypeString += type.toString();
			
			if (i < (types.length - 1)) {
				availableTypeString += ", ";
			}
		}
		
		throw new Exception("Please specify a supported type [" + availableTypeString + "] as environment variable '" + HealthCheckerFactory.HEALTH_CHECKER_TYPE + "'");		
	}
}
