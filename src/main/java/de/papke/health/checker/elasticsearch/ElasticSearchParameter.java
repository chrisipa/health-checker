package de.papke.health.checker.elasticsearch;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for all available elasticsearch command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum ElasticSearchParameter implements Parameter {
	
	HOSTNAME("h", "hostname", "The hostname of the elasticsearch server", null),
	PORT("p", "port", "The port of the elasticsearch server", null),
	
	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the elasticsearch server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the elasticsearch server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),
	
	CLUSTER_NAME("g", "cluster-name", "The name of the elasticsearch cluster", "elasticsearch"),
	INDEX("i", "index", "The elasticsearch index for the query", null),
	QUERY("q", "query", "The elasticsearch query string to execute", null),
	TYPES("t", "types", "Comma separated list of elasticsearch return types", null),
	
	PATTERN("x", "pattern", "The regex pattern to search in the elasticsearch response text", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private ElasticSearchParameter(String shortName, String longName, String description, Object defaultValue) {
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
