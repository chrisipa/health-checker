package de.papke.health.checker.solr;

import de.papke.health.checker.HealthChecker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for checking the health of a Solr server.
 *
 * @author Christoph Papke (info@christoph-papke.de)
 */
public class SolrHealthChecker extends HealthChecker {

  private static Options options = new Options();

  static {
    for (SolrParameter parameter : SolrParameter.values()) {
      options.addOption(parameter.getOption());
    }
  }

  public SolrHealthChecker() {
    super(options);
  }

  @Override
  public void check(CommandLine commandLine) throws Exception {

    // get url
    String url = commandLine.getOptionValue(SolrParameter.URL.toString());

    // get query
    String query = commandLine.getOptionValue(SolrParameter.QUERY.toString());

    // get connect timeout
    int connectTimeout = (Integer) SolrParameter.CONNECT_TIMEOUT.getDefaultValue();
    String connectTimeoutString = commandLine.getOptionValue(SolrParameter.CONNECT_TIMEOUT.toString());
    if (StringUtils.isNotEmpty(connectTimeoutString)) {
      connectTimeout = Integer.parseInt(connectTimeoutString);
    }

    // get response timeout
    int responseTimeout = (Integer) SolrParameter.RESPONSE_TIMEOUT.getDefaultValue();
    String responseTimeoutString = commandLine.getOptionValue(SolrParameter.RESPONSE_TIMEOUT.toString());
    if (StringUtils.isNotEmpty(responseTimeoutString)) {
      responseTimeout = Integer.parseInt(connectTimeoutString);
    }

    // get pattern
    Pattern pattern = (Pattern) SolrParameter.PATTERN.getDefaultValue();
    String patternString = commandLine.getOptionValue(SolrParameter.PATTERN.toString());
    if (StringUtils.isNotEmpty(patternString)) {
      pattern = Pattern.compile(patternString);
    }

    // create solr server client
    HttpSolrServer solrServer = new HttpSolrServer(url);
    solrServer.setConnectionTimeout(connectTimeout);
    solrServer.setSoTimeout(responseTimeout);

    // create solr query
    SolrQuery solrQuery = new SolrQuery(query);

    // execute solr query
    QueryResponse queryResponse = solrServer.query(solrQuery);

    // get query result
    StringBuffer resultBuffer = new StringBuffer();
    for (SolrDocument document : queryResponse.getResults()) {
      resultBuffer.append(ClientUtils.toXML(ClientUtils.toSolrInputDocument(document)));
      resultBuffer.append("\n");
    }

    // get result as string
    String result = resultBuffer.toString();
    if (StringUtils.isNotEmpty(result)) {

      System.out.println(result);

      // check if query result matches the given pattern
      Matcher matcher = pattern.matcher(result);
      if (!matcher.find()) {
        throw new Exception("The pattern does not match the solr result text");
      }
    }
  }
}
