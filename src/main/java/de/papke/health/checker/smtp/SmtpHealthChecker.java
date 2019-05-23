package de.papke.health.checker.smtp;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.sun.mail.smtp.SMTPTransport;

import de.papke.health.checker.HealthChecker;
import de.papke.health.checker.ldap.LdapParameter;

/**
 * Class for checking the health of a SMTP server.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class SmtpHealthChecker extends HealthChecker {
	
	private static Options options = new Options();

	static {
		for (SmtpParameter parameter : SmtpParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}

	public SmtpHealthChecker() {
		super(options);
	}

	@Override
	public void check(CommandLine commandLine) throws Exception {
		
		// get hostname
		String hostname = commandLine.getOptionValue(SmtpParameter.HOSTNAME.toString());
		
		// get port
		int port = Integer.parseInt(commandLine.getOptionValue(SmtpParameter.PORT.toString()));

		// get encryption
		Boolean encryption = Boolean.parseBoolean(commandLine.getOptionValue(SmtpParameter.ENCRYPTION.toString()));
		
		// get username
		String username = commandLine.getOptionValue(SmtpParameter.USERNAME.toString());
		
		// get password
		String password = commandLine.getOptionValue(SmtpParameter.PASSWORD.toString());
		
		// get connect timeout
		int connectTimeout = (Integer) LdapParameter.CONNECT_TIMEOUT.getDefaultValue();
		String connectTimeoutString = commandLine.getOptionValue(SmtpParameter.CONNECT_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(connectTimeoutString)) {
			connectTimeout = Integer.parseInt(connectTimeoutString);
		}
		
		// get response timeout
		int responseTimeout = (Integer) LdapParameter.RESPONSE_TIMEOUT.getDefaultValue();
		String responseTimeoutString = commandLine.getOptionValue(SmtpParameter.RESPONSE_TIMEOUT.toString());
		if (StringUtils.isNotEmpty(responseTimeoutString)) {
			responseTimeout = Integer.parseInt(connectTimeoutString);
		}		
		
		// get pattern
		Pattern pattern = (Pattern) SmtpParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(SmtpParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}
		
		// create mail properties
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", hostname);
		properties.setProperty("mail.smtp.port", String.valueOf(port));
		properties.setProperty("mail.smtp.starttls.enable", String.valueOf(encryption));
		properties.setProperty("mail.smtp.timeout", String.valueOf(responseTimeout));
		properties.setProperty("mail.smtp.writetimeout", String.valueOf(responseTimeout));
		properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(connectTimeout));
		
		// check if authentication has to be used
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			properties.setProperty("mail.smtp.auth", String.valueOf(true));
			properties.setProperty("mail.user", username);
			properties.setProperty("mail.password", password);
		}
		
		// get mail session
		Session session = Session.getDefaultInstance(properties);
		
		// get transport
		Transport transport = session.getTransport();

		// connect to the SMTP server
		transport.connect();
		
		// get SMTP server response
		String response = "";
		if (transport instanceof SMTPTransport) {
			SMTPTransport smtpTransport = (SMTPTransport) transport;
			response = smtpTransport.getLastServerResponse();
		}
		
		// check if pattern matches the query result
		Matcher matcher = pattern.matcher(response);
		if (!matcher.find()) {
			throw new Exception("The pattern does not match the query result");
		}
		
		// close transport
		transport.close();
	}
}
