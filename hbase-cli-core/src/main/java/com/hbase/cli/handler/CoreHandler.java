package com.hbase.cli.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

import com.hbase.cli.api.TableCodec;
import com.hbase.cli.constants.ComparatorAlias;
import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.exception.CoreException;
import com.hbase.cli.exception.FilterParserException;
import com.hbase.cli.exception.InvalidTableException;
import com.hbase.cli.exception.ValidationException;
import com.hbase.cli.handler.utils.HandlerUtility;
import com.hbase.cli.parser.FilterParser;
import com.hbase.cli.schemaobjects.Qualifier;
import com.hbase.cli.utils.TablePropertiesContext;
import com.utils.blu.constants.DataType;
import com.utils.blu.dal.utils.DALUtility;
import com.utils.blu.datastructures.DataMap;
import com.utils.blu.datastructures.DataStore;
import com.utils.blu.db.DB;
import com.utils.blu.db.DBFactory;
import com.utils.blu.exception.UnsupportedDataTypeException;
import com.utils.blu.po.DeletePO;
import com.utils.blu.po.ScanPO;
import com.utils.blu.schema.Field;

/**
 * Handles all the command's core logic
 * 
 *
 */
public class CoreHandler {

	private static DB dbInstance;

	private static TablePropertiesContext props;

	private static final NotificationLogger coreHandlerlogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = CoreHandler.class.getName();

	private static Set<String> expandedList = new HashSet<>();

	private static int noOfVersions;
	private static TableCodec tableImpl;
	private static boolean isRowkeyEncoded;
	private static Map<String, Qualifier> qualifierMap;

	static {
		// Setting logging properties
		String logFile = System.getProperty("logFileName");
		if (logFile != null) {
			MDC.put("logFileName", logFile);
		}
	}

	/**
	 * loads the necessary properties and instances for command execution
	 * 
	 * @throws Exception
	 */
	public static void loadRequisites() throws Exception {

		// Parsing Table json schema
		String jsonPath = System.getProperty(CoreConstants.HBASE_CONFIG_DIR_PROP);
		props = TablePropertiesContext.loadProperties(jsonPath);

		/*
		 * Creating Hbase connection instance Since connection creation is a
		 * time taking process, creating it in the start of the cli session
		 * start
		 */
		coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.INFO, "Creating Hbase connection instance!!!");
		dbInstance = DBFactory.getInstance();
	}

	/**
	 * Validates the schema file for valid table names by comparing with the
	 * hbase tables list
	 * 
	 * @throws Exception
	 */
	public static void lookForInvalidTables() throws Exception {
		coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.INFO,
				"Looking for invalid tables in the schema!!!");
		expandedList = HandlerUtility.expandConfigurationTablesIfAny(props, dbInstance.list());
		coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
				"Expanded table list : " + expandedList.toString());
		HandlerUtility.classifyTables(props);
	}

	public static Set<String> getColumnListForDumpFile(String tableName) throws InvalidTableException, IOException {
		String table = HandlerUtility.getValidTable(tableName, dbInstance.list());
		return props.getColumnList(table);
	}

	/**
	 * Handles scan command from hbase-ui
	 * 
	 * @param tableName
	 * @param startRowkey
	 * @param endRowkey
	 * @param columns
	 * @param filterQuery
	 * @param limit
	 * @param version
	 * @param dump
	 * @return
	 * @throws Exception
	 */
	public static DataStore handleScan(String tableName, String startRowkey, String endRowkey, String columns,
			String filterQuery, int version) throws Exception {

		clearHeldResources();

		// Initializations
		byte[] startKey = null;
		byte[] endKey = null;
		String family = null;
		Set<String> familySet = new HashSet<String>();
		Set<Field> fields = new HashSet<Field>();
		noOfVersions = version;
		
		String table = HandlerUtility.getValidTable(tableName, dbInstance.list());
		isRowkeyEncoded = props.isRowkeyEncoded(table);

		// Validating & populating columnfamilies & fields
		qualifierMap = props.getQualifierMap(table);
		Set<String> familyList = props.getFamilyList(table);
		Set<String> invalidColumns = new HashSet<>();
		String[] tokens = null;
		if (columns != null && !columns.isEmpty()) {
			if (columns.contains(CoreConstants.COMMA)) {
				tokens = columns.split(CoreConstants.COMMA);

				// Populating columnfamilies
				coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Populating family set...");
				for (String token : tokens) {
					if (!token.contains(CoreConstants.COLON)) {
						if (familyList.contains(token)) {
							familySet.add(token);
						} else
							invalidColumns.add(token);
					}
				}

				// Populating fields
				coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Populating column set...");
				for (String token : tokens) {
					if (token.contains(CoreConstants.COLON)) {
						String[] columnToken = token.split(CoreConstants.COLON);
						if (!familySet.contains(columnToken[0])) {
							if (qualifierMap.containsKey(token)) {
								fields.add(new Field(columnToken[0], columnToken[1],
										DataType.valueOf(qualifierMap.get(token).getType().toUpperCase())));
							} else
								invalidColumns.add(token);
						}
					}
				}

			} else {
				if (columns.contains(CoreConstants.COLON)) {
					if (qualifierMap.containsKey(columns)) {
						String[] columnToken = columns.split(CoreConstants.COLON);
						fields.add(new Field(columnToken[0], columnToken[1],
								DataType.valueOf(qualifierMap.get(columns).getType().toUpperCase())));
					} else
						invalidColumns.add(columns);

				} else {
					if (familyList.contains(columns)) {
						familySet.add(columns);
					} else
						invalidColumns.add(columns);

				}
			}

			if (!invalidColumns.isEmpty()) {
				coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
						"Found Invalid columns:" + invalidColumns.toString());
				throw new ValidationException("Provided columns:" + invalidColumns.toString() + "are not valid!!!");
			}
		} else {
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
					"Fetching data for all the columns...");
			Set<String> allColumns = props.getColumnList(table);
			for (String column : allColumns) {
				String[] columnToken = column.split(CoreConstants.COLON);
				fields.add(new Field(columnToken[0], columnToken[1],
						DataType.valueOf(qualifierMap.get(column).getType().toUpperCase())));
			}

		}

		// load Codec implementation
		tableImpl = HandlerUtility.loadCodec(table, props);

		// populating startkey
		if (startRowkey != null) {
			if (isRowkeyEncoded) {
				try {
					startKey = tableImpl.getEncodedRowkey(startRowkey);
				} catch (Exception e) {
					// TODO: handle exception
					throw new EncoderException("Unable to encode the provided startkey!!!");
				}
				
				if (startKey == null || startKey.length == 0) {
					throw new EncoderException("Encoded startrowkey value is empty!!!");
				}
			} else
				startKey = Bytes.toBytes(startRowkey);
		}

		// populating endkey
		if (endRowkey != null) {
			if (isRowkeyEncoded) {
				try {
					endKey = tableImpl.getEncodedRowkey(endRowkey);
				} catch (Exception e) {
					// TODO: handle exception
					throw new EncoderException("Unable to encode the provided endkey!!!");
				}
				
				if (endKey == null || endKey.length == 0) {
					throw new EncoderException("Encoded endrowkey value is empty!!!");
				}
			} else
				endKey = Bytes.toBytes(endRowkey);
		}

		// Call filterparser
		FilterList filters = null;
		if (filterQuery != null && !filterQuery.isEmpty()) {
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
					"calling filter parser with the query:" + filterQuery);
			Filter filter = null;
			try {
				filter = FilterParser.parseFilterExpression(filterQuery.substring(1, filterQuery.length() - 1), table,
						tableImpl, props);
				FilterParser.releaseResources();
			} catch (Exception e) {
				FilterParser.releaseResources();
				throw new FilterParserException(e.getMessage());
			}

			filters = new FilterList();
			filters.addFilter(filter);
		}

		coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Building scan params...");
		ScanPO scanParams = new ScanPO.PARAMBUILDER().table(tableName).startKey(startKey).endKey(endKey)
				.columnFamilies(familySet).fields(fields).filters(filters).noOfVersions(noOfVersions).build();

		try {
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Calling scan api...");
			dbInstance.scan(scanParams);
			return getDataStore();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR, "Error occurred : ", e);
			throw new CoreException("Internal error occurred!!!", e);
		}
	}

	public static DataStore getDataStore() throws Exception {
		DataMap resultDataMap;
		DataMap outputDataMap;
		DataStore resultDataStore = new DataStore();
		boolean resultExists = dbInstance.hasNextRecord();

		coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "scan completed...");
		if (resultExists) {
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Got resultset...");
			int pointer = 0;
			do {
				resultDataMap = dbInstance.getNextRecord();
				outputDataMap = new DataMap();
				for (Entry<Object, Object> iterator : resultDataMap.entrySet()) {

					String key = (String) iterator.getKey();
					String decodedRow;
					Object decodedColumn = null;

					// Decoding rowkey
					if (key.equals(CoreConstants.ROWKEY)) {
						if (isRowkeyEncoded) {
							decodedRow = tableImpl.getDecodedRowkey((byte[]) resultDataMap.get(key));
							if (decodedRow == null || decodedRow.isEmpty()) {
								throw new DecoderException("Decoded rowkey value is empty");
							}
						} else
							decodedRow = Bytes.toString((byte[]) resultDataMap.get(key));

						outputDataMap.put(key, decodedRow);
					} else {
//						int version = 0;
						List<Object> values = null;
						// Decoding columns
						if (qualifierMap.containsKey(key)) {
							if (qualifierMap.get(key).getType().equals(CoreConstants.CUSTOM)) {
								if (noOfVersions == 1) {
									outputDataMap.put(key, decodeColumn(key, iterator.getValue()));
								} else {
									values = new ArrayList<>();
									for (Object value : (List<Object>) iterator.getValue()) {
										values.add(decodeColumn(key, value));
									}
									outputDataMap.put(key, values);
								}
							} else {
								if (noOfVersions == 1) {
									decodedColumn = iterator.getValue();
									// Should be possibly moved to BLU later
									outputDataMap.put(key, decodeColumnUsingFieldObject(decodedColumn, key));
								} else {
									values = new ArrayList<>();
									for (Object value : (List<Object>) iterator.getValue()) {
										values.add(decodeColumnUsingFieldObject(value, key));
									}
									outputDataMap.put(key, values);
								}

							}
						}
					}

				}

				// Filling the datastore
				resultDataStore.setDataMapList(outputDataMap);
				pointer++;
			} while (pointer < CoreConstants.RECORD_BUFFER && dbInstance.hasNextRecord());
		}

		return resultDataStore;
	}

	private static Object decodeColumn(String key, Object value) throws Exception {
		Object decodedColumn = tableImpl.getDecodedColumns(key, (byte[]) value);
		if (decodedColumn == null) {
			throw new DecoderException("Decoded column value is empty for column :" + key);
		}
		return decodedColumn;
	}

	private static Object decodeColumnUsingFieldObject(Object decodedColumn, String key)
			throws UnsupportedDataTypeException {
		if (decodedColumn instanceof byte[] && !qualifierMap.get(key).getType().equals(CoreConstants.CUSTOM)) {
			String[] tokens = key.split(CoreConstants.COLON);
			decodedColumn = DALUtility.getFieldValueFromByteArray(
					new Field(tokens[0], tokens[1], DataType.valueOf(qualifierMap.get(key).getType().toUpperCase())),
					(byte[]) decodedColumn);
		}
		return decodedColumn;
	}

	private static void clearHeldResources() {
		noOfVersions=0;
		tableImpl = null;
		isRowkeyEncoded = false;
		qualifierMap = null;
	}

	/**
	 * Handles List command from hbase-ui
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception 
	 * @throws InvalidTableException
	 */
	public static Set<String> handleList(String tableName) throws Exception {

		expandedList = HandlerUtility.expandConfigurationTablesIfAny(props, dbInstance.list());
		
		if (tableName == null || tableName.isEmpty()) {
			
			return expandedList;

		} else {
			
			Set<String> searchList = new HashSet<>();
			
			if (tableName.contains(CoreConstants.ASTERISK_STRING) && tableName.length() > 1) {
				String searchString;
				
				int len = tableName.length();

				if (tableName.charAt(0) == CoreConstants.ASTERISK
						&& tableName.charAt(len - 1) == CoreConstants.ASTERISK) {
					searchString = tableName.substring(1, len - 1);
					searchList = HandlerUtility.searchTable(searchString, ComparatorAlias.contains, expandedList);
				} else if (tableName.charAt(len - 1) == CoreConstants.ASTERISK) {
					searchString = tableName.substring(0, len - 1);
					searchList = HandlerUtility.searchTable(searchString, ComparatorAlias.startswith, expandedList);
				} else if (tableName.charAt(0) == CoreConstants.ASTERISK) {
					searchString = tableName.substring(1, len);
					searchList = HandlerUtility.searchTable(searchString, ComparatorAlias.endswith, expandedList);
				}

				coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG,
						"Search result : " + searchList.toString());
			} else {
				if (expandedList.contains(tableName)) {
					searchList.add(tableName);
				}
			}
			return searchList;
		}
	}

	/**
	 * Handles describe command from hbase-ui
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static List<Object> handleDescribe(String tableName) throws Exception {

		List<Object> output = new LinkedList<Object>();
		String table = HandlerUtility.getValidTable(tableName, dbInstance.list());
		TableCodec tableImpl = HandlerUtility.loadCodec(table, props);

		if (tableImpl != null) {
			output.add(tableImpl.getTableDescription());
			output.add(tableImpl.getRowkeyDescription());
			output.add(props.getQualifierMap(table));
		}

		return output;
	}

	/**
	 * Handles delete command from hbase-ui
	 * 
	 * @param tableName
	 * @param rowkey
	 * @throws Exception
	 */
	public static void handleDelete(String tableName, String rowkey) throws Exception {

		byte[] encodedRowkey = null;
		String table = HandlerUtility.getValidTable(tableName, dbInstance.list());
		boolean isRowkeyEncoded = props.isRowkeyEncoded(table);
		TableCodec tableImpl = null;

		if (isRowkeyEncoded) {
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Rowkey is encoded!!!");
			tableImpl = HandlerUtility.loadCodec(table, props);
			try {
				encodedRowkey = tableImpl.getEncodedRowkey(rowkey);
			} catch (Exception e) {
				// TODO: handle exception
				throw new EncoderException("Unable to encode the provided rowkey!!!");
			}
			
			if (encodedRowkey == null || encodedRowkey.length == 0) {
				throw new EncoderException("Encoded rowkey is empty!!!");
			}
		} else {
			encodedRowkey = Bytes.toBytes(rowkey);
		}

		try {
			DeletePO deleteParams = new DeletePO.PARAMBUILDER().table(tableName).rowKey(encodedRowkey).build();
			coreHandlerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.DEBUG, "Calling delete api...");
			dbInstance.delete(deleteParams);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CoreException("Error occurred!!!", e);
		}
	}

}
