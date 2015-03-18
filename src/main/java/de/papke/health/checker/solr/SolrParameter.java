package de.papke.health.checker.solr;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available Solr command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum SolrParameter implements Parameter {
	
	URL("l", "url", "The url to connect to", null),
	QUERY("q", "query", "The solr query to execute", null),

	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the solr server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the solr server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),
	
	PATTERN("x", "pattern", "The regex pattern to search in the HTTP response text", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private SolrParameter(String shortName, String longName, String description, Object defaultValue) {
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
