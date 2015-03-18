package de.papke.health.checker.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.bethecoder.ascii_table.ASCIITable;

import de.papke.health.checker.HealthChecker;
import de.papke.health.checker.elasticsearch.ElasticSearchParameter;

/**
 * Class for checking the health of a JDBC compatible database.
 * 
 * @author Christoph Papke (info@christoph-papke.de)
 *
 */
public class JdbcHealthChecker extends HealthChecker {

	private static Options options = new Options();

	static {
		for (JdbcParameter parameter : JdbcParameter.values()) {
			options.addOption(parameter.getOption());
		}
	}

	public JdbcHealthChecker() {
		super(options);
	}

	/**
	 * Helper method to converting a nested string list to a 2d string array.
	 * 
	 * @param list
	 * @return
	 */
	private String[][] listWithSubListToArray(List<List<String>> list) {

		// get x-dimension
		int numberOfColumns = getNumberOfColumns(list);
		
		// get y-dimension
		int numberOfRows = 1;
		if (list.size() > numberOfRows) {
			numberOfRows = list.size();
		}

		// create 2d string array
		String[][] array = new String[numberOfRows][numberOfColumns];

		// fill 2d array with values from nested string list
		for (int i = 0; i < list.size(); i++) {

			List<String> columnList = list.get(i);

			for (int j = 0; j < columnList.size(); j++) {
				array[i][j] = columnList.get(j);
			}
		}

		return array;
	}

	/**
	 * Helper method for getting the x-dimension of the nested string list
	 * 
	 * @param list
	 * @return
	 */
	private int getNumberOfColumns(List<List<String>> list) {

		int numberOfColumns = 0;

		for (List<String> subList : list) {
			int subListSize = subList.size();
			if (subListSize > numberOfColumns) {
				numberOfColumns = subListSize;
			}
		}

		return numberOfColumns;
	}

	@Override
	public void check(CommandLine commandLine) throws Exception {

		// get username
		String username = commandLine.getOptionValue(JdbcParameter.USERNAME.toString());
		
		// get password
		String password = commandLine.getOptionValue(JdbcParameter.PASSWORD.toString());
		
		// get full qualified driver class
		String driver = commandLine.getOptionValue(JdbcParameter.DRIVER.toString());
		
		// get JDBC url
		String url = commandLine.getOptionValue(JdbcParameter.URL.toString());
		
		// get SQL query
		String query = commandLine.getOptionValue(JdbcParameter.QUERY.toString());

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
		Pattern pattern = (Pattern) JdbcParameter.PATTERN.getDefaultValue();
		String patternString = commandLine.getOptionValue(JdbcParameter.PATTERN.toString());
		if (StringUtils.isNotEmpty(patternString)) {
			pattern = Pattern.compile(patternString);
		}

		// load JDBC driver class
		Class.forName(driver);
		
		// create connection properties
		Properties properties = new Properties();
		properties.setProperty("user", username);
		properties.setProperty("password", password);
		properties.setProperty("connectTimeout", String.valueOf(connectTimeout));
		properties.setProperty("socketTimeout", String.valueOf(responseTimeout));

		// get database connection
		Connection connection = DriverManager.getConnection(url, properties);

		// create prepared statement for SQL query
		PreparedStatement statement = connection.prepareStatement(query);
		
		// execute SQL query from prepared statement 
		ResultSet resultSet = statement.executeQuery();
		
		// get metadata from result set
		ResultSetMetaData metadata = resultSet.getMetaData();

		// get table header list with column names
		List<String> headerList = new ArrayList<String>();
		int columnCount = metadata.getColumnCount();
		for (int i = 1; i < columnCount + 1; i++ ) {
			String name = metadata.getColumnName(i);
			headerList.add(name);
		}

		// get table row list with column values 
		List<List<String>> rowList = new ArrayList<List<String>>();
		while(resultSet.next()) {
			List<String> columnList = new ArrayList<String>();
			for (String column : headerList) {
				String value = resultSet.getString(column);
				if (value == null) {
					value = "";
				}
				columnList.add(value);
			}
			rowList.add(columnList);
		}

		// get result as ASCII table
		String result = ASCIITable.getInstance().getTable(headerList.toArray(new String[headerList.size()]), listWithSubListToArray(rowList));
		if (StringUtils.isNotEmpty(result)) {

			System.out.println(result);

			// check if pattern matches the query result
			Matcher matcher = pattern.matcher(result);
			if (!matcher.find()) {
				throw new Exception("The pattern does not match the query result");
			}
		}
	}
}
