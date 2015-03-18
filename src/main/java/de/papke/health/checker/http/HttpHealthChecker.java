package de.papke.health.checker.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import de.papke.health.checker.HealthChecker;
import de.papke.health.checker.Main;

/**
 * Class for checking the health of an HTTP server.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class HttpHealthChecker extends HealthChecker {
	
	private static final String HTTP_SCHEME_PREFIX = "http";
	private static final String PROTOCOL_SEPARATOR = "://";
	private static final String DATA_SEPARATOR = ":";
	private static final String PATH_SEPARATOR = "/";
	
	private static Options options = new Options();

	static {
		for (HttpParameter parameter : HttpParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}
	
	public HttpHealthChecker() {
		super(options);
	}
	
	@Override
	public void check(CommandLine commandLine) throws Exception {
		
		// get username
		String username = commandLine.getOptionValue(HttpParameter.USERNAME.toString());
		
		// get password
		String password = commandLine.getOptionValue(HttpParameter.PASSWORD.toString());
		
		// get url
		String url = commandLine.getOptionValue(HttpParameter.URL.toString());
		
		// get header array
		String headers[] = commandLine.getOptionValues(HttpParameter.HEADER.toString());
		
		// get data array
		String postData[] = commandLine.getOptionValues(HttpParameter.POST_DATA.toString());

		// get connect timeout
		int connectTimeout = (Integer) HttpParameter.CONNECT_TIMEOUT.getDefaultValue();
		String connectTimeoutString = commandLine.getOptionValue(HttpParameter.CONNECT_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(connectTimeoutString)) {
			connectTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get response timeout
		int responseTimeout = (Integer) HttpParameter.RESPONSE_TIMEOUT.getDefaultValue();
		String responseTimeoutString = commandLine.getOptionValue(HttpParameter.RESPONSE_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(responseTimeoutString)) {
			responseTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get HTTP method to execute
		HttpMethod method = (HttpMethod) HttpParameter.METHOD.getDefaultValue();
		String methodString = commandLine.getOptionValue(HttpParameter.METHOD.toString());
		if (StringUtils.isNotEmpty(methodString)) {
			for (HttpMethod currentMethod : HttpMethod.values()) {
				if (currentMethod.toString().equalsIgnoreCase(methodString)) {
					method = currentMethod;
					break;
				}
			}
		}
		
		// get expected status code
		int statusCode = (Integer) HttpParameter.STATUS_CODE.getDefaultValue();
		String statusCodeString = commandLine.getOptionValue(HttpParameter.STATUS_CODE.toString());
		if (StringUtils.isNotEmpty(statusCodeString)) {
			statusCode = Integer.parseInt(statusCodeString);
		}
		
		// get pattern
		Pattern pattern = (Pattern) HttpParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(HttpParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}
		
		// create HTTP client which accepts all SSL certificates
		// (self-signed, expired, ...)
		TrustAllSSLCertHttpClient httpClient = new TrustAllSSLCertHttpClient(connectTimeout, responseTimeout);
		
		// only use authentication if username and password are given 
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			
			// create credentials provider for basic authentication
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			URI uri = new URI(url);
			AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
			Credentials credentials = new UsernamePasswordCredentials(username, password);
			credentialsProvider.setCredentials(authScope, credentials);
			
			// set credentials provider for HTTP client
			httpClient.setCredentialsProvider(credentialsProvider);
		}
		
		// create HTTP request regarding the HTTP method  
		HttpUriRequest request = null;
		switch (method) {
		case get:
			request = new HttpGet(url);
			break;
		case head:
			request = new HttpHead(url);
			break;
		case post:
			request = new HttpPost(url);
			
			// add data to POST request
			if (postData != null) {
				
				List<NameValuePair> requestData = new ArrayList<NameValuePair>();
				
				for (String postDataEntry : postData) {
					String[] postDataEntryArray = postDataEntry.split(DATA_SEPARATOR);
					if (postDataEntryArray.length == 2) {
						String postDataName = postDataEntryArray[0].trim();
						String postDataValue = postDataEntryArray[1].trim();
						requestData.add(new BasicNameValuePair(postDataName, postDataValue));	
					}
				}
				
				if (!requestData.isEmpty()) {
					HttpPost postRequest = (HttpPost) request;
					postRequest.setEntity(new UrlEncodedFormEntity(requestData));
				}
			}
			
			break;
		}
		
		// add custom user agent to request
		String userAgent = Main.getApplicationProperty("name") + "/" + Main.getApplicationProperty("version");
		request.setHeader("User-Agent", userAgent);			
		
		// add headers to HTTP request
		if (headers != null) {
			for (String header : headers) {
				String[] headerArray = header.split(DATA_SEPARATOR);
				if (headerArray.length == 2) {
					String headerName = headerArray[0].trim();
					String headerValue = headerArray[1].trim();
					request.addHeader(headerName, headerValue);	
				}
			}
		}
		
		// execute HTTP request
		HttpResponse response = httpClient.execute(request);
		
		// do we have to execute another GET request?
		if (request instanceof HttpPost) {
			
			Header[] locationArray = response.getHeaders("Location");
			
			if (locationArray.length == 1) {
				
				Header locationHeader = locationArray[0];
				String locationUrl = locationHeader.getValue();
				
				// if the location url is relative we have to build the absolute url for the GET request
				if (!locationUrl.startsWith(HTTP_SCHEME_PREFIX)) {
					String pathPrefix = locationUrl.startsWith(PATH_SEPARATOR) ? "" : PATH_SEPARATOR;
					URI uri = new URI(url);
					locationUrl = uri.getScheme() + PROTOCOL_SEPARATOR + uri.getHost() + pathPrefix + locationUrl;
				}
				
				request = new HttpGet(locationUrl);
				response = httpClient.execute(request);
			}
		}		
		
		// check if HTTP response status code matches expectations
		int responseStatusCode = response.getStatusLine().getStatusCode();
		if (responseStatusCode != statusCode) {
			throw new Exception("The status code of the HTTP response does not match");
		}
		
		// print response headers
		for (Header header: response.getAllHeaders()) {
			System.out.println(header.getName() + DATA_SEPARATOR + " " + header.getValue());
		}
		
		// check if response has an entity
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			
			// print response text
			String responseText = EntityUtils.toString(response.getEntity());
			if (StringUtils.isNotEmpty(responseText)) {
				System.out.println(responseText);
			}
			
			// check if response text matches the given pattern
			Matcher matcher = pattern.matcher(responseText);
			if (!matcher.find()) {
				throw new Exception("The pattern does not match the HTTP response text");
			}
		}
	}
}
