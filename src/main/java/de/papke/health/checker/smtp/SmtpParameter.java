package de.papke.health.checker.smtp;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available SMTP command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum SmtpParameter implements Parameter {
	
	HOSTNAME("h", "hostname", "The hostname of the SMTP server", null),
	PORT("p", "port", "The port of the SMTP server", null),
	ENCRYPTION("e", "encryption", "Set if SMTP server needs TLS encryption", false),
	
	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the SMTP server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the SMTP server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),	
	
	USERNAME("u", "username", "The username of the SMTP server", null), 
	PASSWORD("p", "password", "The password of the SMTP server", null),

	PATTERN("x", "pattern", "The regex pattern to search in the SMTP server response", Pattern.compile("250.*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private SmtpParameter(String shortName, String longName, String description, Object defaultValue) {
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