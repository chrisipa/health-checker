package de.papke.health.checker.ldap;

import java.util.regex.Pattern;

import org.apache.commons.cli.Option;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.SearchScope;

import de.papke.health.checker.api.Parameter;

/**
 * Enum for available LDAP command line arguments.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum LdapParameter implements Parameter {
	
	USERNAME("u", "username", "The username of the LDAP server", null), 
	PASSWORD("p", "password", "The password of the LDAP server", null),
	
	CONNECT_TIMEOUT("c", "connect-timeout", "The connection timeout of the LDAP server (in milliseconds)", DEFAULT_CONNECT_TIMEOUT),
	RESPONSE_TIMEOUT("r", "response-timeout", "The response timeout of the LDAP server (in milliseconds)", DEFAULT_RESPONSE_TIMEOUT),

	URL("l", "url", "The LDAP url to connect to", null),
	
	BASE_DN("b", "base-dn", "The base dn of the LDAP query", null),
	SEARCH_SCOPE("s", "search-scope", "The search scope of the LDAP query", SearchScope.BASE),
	FILTER("f", "filter", "The filter string of the LDAP query", Filter.createPresenceFilter("objectClass")),
	ATTRIBUTES("a", "attributes", "The returned attributes of the LDAP query", null),
	PAGE_SIZE("x", "page-size", "The page size of the LDAP query", 1000),
	
	PATTERN("y", "pattern", "The regex pattern to search in the LDAP search result", Pattern.compile(".*"));
	
	private final String shortName;
	private final String longName;
	private final String description;
	private final Object defaultValue;
	
	private LdapParameter(String shortName, String longName, String description, Object defaultValue) {
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
