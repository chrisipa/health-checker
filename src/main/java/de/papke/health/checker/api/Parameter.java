package de.papke.health.checker.api;

import org.apache.commons.cli.Option;

/**
 * Interface definition for command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public interface Parameter {
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	public static final int DEFAULT_RESPONSE_TIMEOUT = 10000;
	
	public Option getOption();
	public String getShortName();
	public String getLongName();
	public String getDescription();
	public Object getDefaultValue();
}
