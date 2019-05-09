package com.hbase.cli.handler.utils;

import java.util.HashSet;
import java.util.Set;

import com.hbase.cli.api.TableCodec;
import com.hbase.cli.constants.ComparatorAlias;
import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.exception.CodecLoaderException;
import com.hbase.cli.exception.InvalidTableException;
import com.hbase.cli.loaders.CodecImplLoader;
import com.hbase.cli.utils.TablePropertiesContext;

public class HandlerUtility {

	private static final NotificationLogger HandlerUtilityLogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = HandlerUtility.class.getName();

	private static Set<String> fullList = new HashSet<>();
	private static Set<String> startsWith = new HashSet<>();
	private static Set<String> endsWith = new HashSet<>();

	/**
	 * Classifies the tables as startswith list and endswith list
	 * 
	 * @param props
	 */
	public static void classifyTables(TablePropertiesContext props) {

		// Classifying tables
		for (String table : props.getAllTableNames()) {
			int len = table.length();

			if (table.charAt(len - 1) == CoreConstants.ASTERISK) {
				startsWith.add(table.substring(0, len - 1));
			} else if (table.charAt(0) == CoreConstants.ASTERISK) {
				endsWith.add(table.substring(1, len));
			} else {
				fullList.add(table);
			}
		}

		HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				fullList.toString() + ";" + startsWith.toString() + ";" + endsWith.toString());
	}

	/**
	 * Searches the given table in the wildcard list(startswith & endswith list)
	 * 
	 * @param tableName
	 * @return
	 */
	public static String searchInWildcardList(String tableName) {

		// Search in startswith list
		for (String table : startsWith) {
			if (tableName.startsWith(table)) {
				return table + CoreConstants.ASTERISK_STRING;
			}
		}
		// Search in endswith list
		for (String table : endsWith) {
			if (tableName.endsWith(table)) {
				return CoreConstants.ASTERISK_STRING + table;
			}
		}

		return null;
	}

	/**
	 * Gives the valid tablename after searching in fullist, startswith and
	 * endswith list
	 * 
	 * @param tableName
	 * @param hbaseTables
	 * @return
	 * @throws InvalidTableException
	 */
	public static String getValidTable(String tableName, Set<String> hbaseTables) throws InvalidTableException {

		String table;
		if (fullList.contains(tableName)) {
			table = tableName;
		} else {
			table = searchInWildcardList(tableName);
			if (table == null) {
				throw new InvalidTableException("Table:" + tableName + " is not configured in hbase cli!!!");
			}
		}
		
		if (!hbaseTables.contains(tableName)) {
			throw new InvalidTableException("Table :" + tableName + " not found in hbase!!!");
		}
		return table;
	}

	/**
	 * Wildcard tables are expanded and all the tables in the configuration file
	 * are validated with the tables in the database
	 * 
	 * @param props
	 * @param hbaseTables
	 * @return
	 * @throws Exception
	 */
	public static Set<String> expandConfigurationTablesIfAny(TablePropertiesContext props, Set<String> hbaseTables)
			throws Exception {

		Set<String> expandedList = new HashSet<>();
		Set<String> confTablesList = props.getAllTableNames();
		Set<String> invalidTables = new HashSet<>();

		for (String table : confTablesList) {

			if (table.contains(CoreConstants.ASTERISK_STRING)) {

				int len = table.length();
				String searchString;
				Set<String> searchResult = new HashSet<>();

				if (table.charAt(len - 1) == CoreConstants.ASTERISK) {
					searchString = table.substring(0, len - 1);
					searchResult = searchTable(searchString, ComparatorAlias.startswith, hbaseTables);
				} else if (table.charAt(0) == CoreConstants.ASTERISK) {
					searchString = table.substring(1, len);
					searchResult = searchTable(searchString, ComparatorAlias.endswith, hbaseTables);
				}

				if (!searchResult.isEmpty()) {
					expandedList.addAll(searchResult);
				} else
					invalidTables.add(table);
			} else {
				if (hbaseTables.contains(table)) {
					expandedList.add(table);
				} else
					invalidTables.add(table);
			}
		}

		HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				"Expanded table list >>>" + expandedList.toString() + "   invalid list>>" + invalidTables.toString());
		if (!invalidTables.isEmpty()) {
			HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR,
					"Following tables " + invalidTables.toString()
					+ " provided in the configuration file does not exist in the database!!!");
		}

		return expandedList;

	}

	/**
	 * Searches the table in the given list based on the search criteria
	 * 
	 * @param searchString
	 * @param search
	 * @param tableList
	 * @return
	 */
	public static Set<String> searchTable(String searchString, ComparatorAlias search, Set<String> tableList) {
		Set<String> searchList = new HashSet<String>();
		HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				"Inside Handlerutility.searchTable() >>" + searchString + search.toString() + tableList.toString());
		switch (search) {
		case startswith:
			for (String table : tableList) {
				if (table.startsWith(searchString)) {
					searchList.add(table);
				}
			}
			break;

		case endswith:
			for (String table : tableList) {
				if (table.endsWith(searchString)) {
					searchList.add(table);
				}
			}
			break;
		case contains:
			for (String table : tableList) {
				if (table.contains(searchString)) {
					searchList.add(table);
				}
			}
			break;

		default:
			break;
		}

		return searchList;
	}

	/**
	 * Loads the TableCodec implementation of the specified tableName
	 * 
	 * @param tableName
	 * @param props
	 * @return
	 * @throws CodecLoaderException
	 */
	public static TableCodec loadCodec(String tableName, TablePropertiesContext props) throws CodecLoaderException {
		// load Codec implementation
		HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Loading table implementation...");
		try {
			return CodecImplLoader.load(tableName, TableCodec.class, props.getTableCodec(tableName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			HandlerUtilityLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR,
					"Problem while loading table codec implementation :", e);
			throw new CodecLoaderException("Problem while loading table codec implementation :" + e.getMessage());
		}
	}

}
