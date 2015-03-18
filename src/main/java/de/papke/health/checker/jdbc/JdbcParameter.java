package de.papke.health.checker.jdbc;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available JDBC command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum JdbcParameter implements Parameter {
	
	USERNAME("u", "username", "The username of the JDBC database", null), 
	PASSWORD("p", "password", "The username of the JDBC database", null),
	
	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the JDBC database (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the JDBC database (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),
	
	DRIVER("y", "driver", "The database driver of the JDBC database", null),
	URL("l", "url", "The JDBC database url to connect to", null),
	QUERY("q", "query", "The SQL query to execute", null),
	
	PATTERN("x", "pattern", "The regex pattern to search in the SQL query result", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private JdbcParameter(String shortName, String longName, String description, Object defaultValue) {
		this.shortName = shortName;
		this.longName = longName;			
		this.description = description;
		this.defaultValue = defaultValue;
	}
	
	public Option getOption() {
		
		String descriptionText = description;
		if (defaultValue != null) {
			descriptionText += " [default: " + defaultValue + "]";
		}
		
		return new Option(shortName, longName, true, descriptionText);
	}
	
	@Override
	public String toString() {
		return longName;
	}	
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}

	public String getDescription() {
		return description;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
}
