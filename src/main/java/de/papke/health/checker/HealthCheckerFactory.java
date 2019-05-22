package de.papke.health.checker;

import org.apache.commons.lang3.StringUtils;

import de.papke.health.checker.api.Type;
import de.papke.health.checker.elasticsearch.ElasticSearchHealthChecker;
import de.papke.health.checker.http.HttpHealthChecker;
import de.papke.health.checker.jdbc.JdbcHealthChecker;
import de.papke.health.checker.ldap.LdapHealthChecker;
import de.papke.health.checker.mongo.MongoHealthChecker;
import de.papke.health.checker.smtp.SmtpHealthChecker;
import de.papke.health.checker.solr.SolrHealthChecker;

/**
 * Factory class for creating concrete health checker objects
 * based on the environment variable HEALTH_CHECKER_TYPE.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class HealthCheckerFactory {
	
	public static final String HEALTH_CHECKER_TYPE = "HEALTH_CHECKER_TYPE";
	
	/**
	 * Method for creating a concrete health checker object
	 * based on the environment variable HEALTH_CHECKER_TYPE.
	 * 
	 * 
	 * @return
	 * @throws Exception
	 */
	public static HealthChecker create() throws Exception {
		
		HealthChecker healthChecker = new DefaultHealthChecker();

		String typeString = (String) System.getenv().get(HEALTH_CHECKER_TYPE);
		if (StringUtils.isNotEmpty(typeString)) {
			
			Type type = Type.valueOf(typeString);
			
			switch (type) {
			case elasticsearch:
				healthChecker = new ElasticSearchHealthChecker();
				break;
			case http:
				healthChecker = new HttpHealthChecker();
				break;
			case jdbc:
				healthChecker = new JdbcHealthChecker();
				break;
			case ldap:
				healthChecker = new LdapHealthChecker();
				break;
			case mongo:
				healthChecker = new MongoHealthChecker();
				break;
			case smtp:
				healthChecker = new SmtpHealthChecker();
				break;
			case solr:
				healthChecker = new SolrHealthChecker();
				break;
			}		
		}
		
		return healthChecker;
	}
}
