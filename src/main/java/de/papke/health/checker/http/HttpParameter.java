package de.papke.health.checker.http;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available HTTP server command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum HttpParameter implements Parameter {
	
	URL("l", "url", "The url of the HTTP server", null),
	METHOD("m", "method", "The HTTP method to use", HttpMethod.get),
	
	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the HTTP server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the HTTP server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),
	
	USERNAME("u", "username", "The username for the HTTP server", null), 
	PASSWORD("p", "password", "The username for the HTTP server", null),
	HEADER("h", "header", "The header to add for the HTTP request", null),
	POST_DATA("d", "post-data", "The data to add for the HTTP POST request", null),
	
	STATUS_CODE("s", "status-code", "The expected status code of the HTTP response", 200),
	PATTERN("x", "pattern", "The regex pattern to search in the HTTP response text", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private HttpParameter(String shortName, String longName, String description, Object defaultValue) {
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
