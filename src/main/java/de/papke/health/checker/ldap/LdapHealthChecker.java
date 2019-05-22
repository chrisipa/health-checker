package de.papke.health.checker.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import com.unboundid.util.ssl.SSLUtil;

import de.papke.health.checker.HealthChecker;

/**
 * Class for checking the health of an LDAP server.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class LdapHealthChecker extends HealthChecker {

	private static final String SECURE_SCHEME = "ldaps";
	private static Options options = new Options();

	static {
		for (LdapParameter parameter : LdapParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}
	
	public LdapHealthChecker() {
		super(options);
	}

	/**
	 * Helper method for getting a connection to an LDAP server.
	 * 
	 * @param username
	 * @param password
	 * @param url
	 * @param connectTimeout
	 * @param responseTimeout
	 * @return
	 * @throws Exception
	 */
	private LDAPConnection getConnection(String username, String password, String url, int connectTimeout, int responseTimeout) throws Exception {

		LDAPURL ldapUrl = new LDAPURL(url);
		
		SSLSocketFactory sslSocketFactory = null;
		if (ldapUrl.getScheme().equals(SECURE_SCHEME)) {
			SSLUtil sslUtil = new SSLUtil();
			sslSocketFactory = sslUtil.createSSLSocketFactory();
		}
		
		LDAPConnectionOptions ldapConnectionOptions = new LDAPConnectionOptions();
		ldapConnectionOptions.setConnectTimeoutMillis(connectTimeout);
		ldapConnectionOptions.setResponseTimeoutMillis(responseTimeout);
		
		return new LDAPConnection(sslSocketFactory, ldapConnectionOptions, ldapUrl.getHost(), ldapUrl.getPort(), username, password);
	}
	
	/**
	 * Helper method for executing an LDAP search request.
	 * 
	 * @param baseDn
	 * @param searchScope
	 * @param filter
	 * @param attributes
	 * @param pageSize
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	private List<SearchResultEntry> search(String baseDn, SearchScope searchScope, Filter filter, String[] attributes, int pageSize, LDAPConnection connection) throws Exception {
	
		List<SearchResultEntry> searchResultEntries = new ArrayList<SearchResultEntry>();
		
		// check if paging should be used
		if (pageSize != -1) {
	
			// create LDAP search request
			SearchRequest searchRequest = new SearchRequest(baseDn, searchScope, filter, attributes);
			
			// instantiate variable for paging cookie
			ASN1OctetString cookie = null;
			
			do {
				
				// set controls for LDAP search request
				Control[] controls = new Control[1];
				controls[0] = new SimplePagedResultsControl(pageSize, cookie);
				searchRequest.setControls(controls);
				
				// execute LDAP search request
				SearchResult searchResult = connection.search(searchRequest);
	
				// add search entries from page to result list
				searchResultEntries.addAll(searchResult.getSearchEntries());
				
				// get cookie for next page
			  	cookie = null;
			  	for (Control control : searchResult.getResponseControls()) {
					if (control instanceof SimplePagedResultsControl) {
						SimplePagedResultsControl simplePagedResultsControl = (SimplePagedResultsControl) control; 
						cookie = simplePagedResultsControl.getCookie();
					}
			  	}
			  	
			} 
			// do this as long as a cookie is returned
			while ((cookie != null) && (cookie.getValueLength() > 0));
		}
		else {
			// execute LDAP search request
			SearchResult searchResult = connection.search(baseDn, searchScope, filter, attributes);
			
			// set search entries as result list
			searchResultEntries = searchResult.getSearchEntries();
		}
		
		return searchResultEntries;
	}	
	
	@Override
	public void check(CommandLine commandLine) throws Exception {
		
		// get username
		String username = commandLine.getOptionValue(LdapParameter.USERNAME.toString());
		
		// get password
		String password = commandLine.getOptionValue(LdapParameter.PASSWORD.toString());
		
		// get url
		String url = commandLine.getOptionValue(LdapParameter.URL.toString());
		
		// get base DN
		String baseDn = commandLine.getOptionValue(LdapParameter.BASE_DN.toString());

		// get connect timeout
		int connectTimeout = (Integer) LdapParameter.CONNECT_TIMEOUT.getDefaultValue();
		String connectTimeoutString = commandLine.getOptionValue(LdapParameter.CONNECT_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(connectTimeoutString)) {
			connectTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get response timeout
		int responseTimeout = (Integer) LdapParameter.RESPONSE_TIMEOUT.getDefaultValue();
		String responseTimeoutString = commandLine.getOptionValue(LdapParameter.RESPONSE_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(responseTimeoutString)) {
			responseTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get search scope
		SearchScope searchScope = (SearchScope) LdapParameter.SEARCH_SCOPE.getDefaultValue();
		String searchScopeString = commandLine.getOptionValue(LdapParameter.SEARCH_SCOPE.toString());
		if (StringUtils.isNotEmpty(searchScopeString)) {
			for (SearchScope currentSearchScope : SearchScope.values()) {
				if (currentSearchScope.toString().equalsIgnoreCase(searchScopeString)) {
					searchScope = currentSearchScope;
					break;
				}
			}
		}
		
		// get search filter
		Filter filter = (Filter) LdapParameter.FILTER.getDefaultValue();
		String filterString = commandLine.getOptionValue(LdapParameter.FILTER.toString());
		if (StringUtils.isNotEmpty(filterString)) {
			filter = Filter.create(filterString);
		}
		
		// get attributes which should be returned
		String[] attributes = null;
		String attributesString = commandLine.getOptionValue(LdapParameter.ATTRIBUTES.toString());
		if (StringUtils.isNotEmpty(attributesString)) {
			attributes = attributesString.trim().split(",");
		}
		
		// get page size for query
		int pageSize = (Integer) LdapParameter.PAGE_SIZE.getDefaultValue();
		String pageSizeString = commandLine.getOptionValue(LdapParameter.PAGE_SIZE.toString());
		if (StringUtils.isNotEmpty(pageSizeString)) {
			pageSize = Integer.parseInt(pageSizeString);
		}
		
		// get pattern
		Pattern pattern = (Pattern) LdapParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(LdapParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}
		
		// get LDAP connection
		LDAPConnection connection = getConnection(username, password, url, connectTimeout, responseTimeout);
		
		// execute LDAP search request
		StringBuffer resultBuffer = new StringBuffer();
		List<SearchResultEntry> searchResultEntryList = search(baseDn, searchScope, filter, attributes, pageSize, connection);
		for (SearchResultEntry searchResultEntry : searchResultEntryList) {
			resultBuffer.append(searchResultEntry.toLDIFString());
			resultBuffer.append("\n");
		}
		
		// get result as string
		String result = resultBuffer.toString();
		if (StringUtils.isNotEmpty(result)) {
			
			System.out.println(result);
			
			// check if query result matches the given pattern
			Matcher matcher = pattern.matcher(result);
			if (!matcher.find()) {
				throw new Exception("The pattern does not match the query result");
			}
		}
	}
}
