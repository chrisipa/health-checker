package de.papke.health.checker.api;

/**
 * Enum for available health checker types.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public enum Type {
	elasticsearch, 
	http, 
	jdbc, 
	ldap, 
	mongo,
	solr
}
