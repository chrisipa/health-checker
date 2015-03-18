package de.papke.health.checker.mongo;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available MongoDB command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum MongoParameter implements Parameter {
	
	URL("l", "url", "The url to connect to", null),
	
	CONNECT_TIMEOUT("s", "connect-timeout", "The connection timeout of the MongoDB server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the MongoDB server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),
	
	DATABASE("d", "database", "The database name for the query", null),
	COLLECTION("c", "collection", "The database collection for the query", null),
	QUERY("q", "query", "The database query to execute", null),
	
	PATTERN("x", "pattern", "The regex pattern to search in the query result", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private MongoParameter(String shortName, String longName, String description, Object defaultValue) {
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
