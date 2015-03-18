package de.papke.health.checker.mongo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;

import de.papke.health.checker.HealthChecker;
import de.papke.health.checker.elasticsearch.ElasticSearchParameter;

/**
 * Class for checking the health of a MongoDB server.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class MongoHealthChecker extends HealthChecker {
	
	private static Options options = new Options();

	static {
		for (MongoParameter parameter : MongoParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}
	
	public MongoHealthChecker() {
		super(options);
	}
	
	@Override
	public void check(CommandLine commandLine) throws Exception {
		
		// get url
		String url = commandLine.getOptionValue(MongoParameter.URL.toString());
		
		// get query
		String query = commandLine.getOptionValue(MongoParameter.QUERY.toString());
		
		// get database name
		String database = commandLine.getOptionValue(MongoParameter.DATABASE.toString());
		
		// get collection name
		String collection = commandLine.getOptionValue(MongoParameter.COLLECTION.toString());
		
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
		
		// get pattern 
		Pattern pattern = (Pattern) MongoParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(MongoParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}
		
		// create MongoDB options builder
		MongoClientOptions.Builder builder = new MongoClientOptions.Builder()
		.connectTimeout(connectTimeout)
		.socketTimeout(responseTimeout);
		
		// create client for MongoDB server
		MongoClient mongoClient = new MongoClient(new MongoClientURI(url, builder));
		
		// get MongoDB database
		DB mongoDb = mongoClient.getDB(database);
		
		// get MongoDB collection
		Jongo jongo = new Jongo(mongoDb);
		MongoCollection mongoCollection = jongo.getCollection(collection);
		
		// execute MongoDB query
		StringBuffer resultBuffer = new StringBuffer();
		MongoCursor<Object> mongoCursor = mongoCollection.find(query).as(Object.class);
		while(mongoCursor.hasNext()) {
			resultBuffer.append(mongoCursor.next());
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
