package de.papke.health.checker.elasticsearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import de.papke.health.checker.HealthChecker;

/**
 * Class for checking the health of an elasticsearch server.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class ElasticSearchHealthChecker extends HealthChecker {

	private static Options options = new Options();

	static {
		for (ElasticSearchParameter parameter : ElasticSearchParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}

	public ElasticSearchHealthChecker() {
		super(options);
	}

	@Override
	public void check(CommandLine commandLine) throws Exception {

		// get hostname
		String hostname = commandLine.getOptionValue(ElasticSearchParameter.HOSTNAME.toString());
		
		// get port
		int port = Integer.parseInt(commandLine.getOptionValue(ElasticSearchParameter.PORT.toString()));
		
		// get index name
		String index = commandLine.getOptionValue(ElasticSearchParameter.INDEX.toString());
		
		// get query
		String query = commandLine.getOptionValue(ElasticSearchParameter.QUERY.toString());
		
		// get cluster name
		String clusterName = commandLine.getOptionValue(ElasticSearchParameter.CLUSTER_NAME.toString());
		
		// get search types
		String types = commandLine.getOptionValue(ElasticSearchParameter.TYPES.toString());

		// get connect timeout
		int connectTimeout = (Integer) ElasticSearchParameter.CONNECT_TIMEOUT.getDefaultValue();
		String connectTimeoutString = commandLine.getOptionValue(ElasticSearchParameter.CONNECT_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(connectTimeoutString)) {
			connectTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get response timeout
		int responseTimeout = (Integer) ElasticSearchParameter.RESPONSE_TIMEOUT.getDefaultValue();
		String responseTimeoutString = commandLine.getOptionValue(ElasticSearchParameter.RESPONSE_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(responseTimeoutString)) {
			responseTimeout = Integer.parseInt(connectTimeoutString);
		}

		// get pattern to search
		Pattern pattern = (Pattern) ElasticSearchParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(ElasticSearchParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}

		// build elasticsearch client settings
		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name", clusterName)
				.put("client.transport.ping_timeout", connectTimeout)
				.build();
		
		// get elasticsearch transport client
		Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostname, port));
		
		// build elasticsearch search request
		SearchRequestBuilder builder = client.prepareSearch(index);
		builder.setQuery(query);
		if (StringUtils.isNotEmpty(types)) {
			builder.setTypes(types);
		}

		// execute elasticsearch search request with timeout
		SearchResponse response = builder.execute().actionGet(responseTimeout);
		
		// get response as string
		String responseText = response.toString();
		if (StringUtils.isNotEmpty(responseText)) {
			
			System.out.println(responseText);
			
			// check if response string matches the pattern
			Matcher matcher = pattern.matcher(responseText);
			if (!matcher.find()) {
				throw new Exception("The pattern does not match the HTTP response text");
			}
		}
	}
}
