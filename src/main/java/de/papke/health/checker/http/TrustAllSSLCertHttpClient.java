package de.papke.health.checker.http;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

/**
 * HTTP client class which accepts all kinds of SSL certificates
 * (self-signed, expired, ...). 
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
@SuppressWarnings("deprecation")
public class TrustAllSSLCertHttpClient extends DefaultHttpClient {

	private int connectTimeout;
	private int responseTimeout;
	
	public TrustAllSSLCertHttpClient(int connectTimeout, int responseTimeout) {
		this.connectTimeout = connectTimeout;
		this.responseTimeout = responseTimeout;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.http.impl.client.AbstractHttpClient#createClientConnectionManager()
	 */
	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		
		HttpParams params = getParams();
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, responseTimeout);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new TrustAllSSLCertSocketFactory(), 443));
    	return new ThreadSafeClientConnManager(params, schemeRegistry);
	}
}