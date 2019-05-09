package com.hbase.cli.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.parser.JsonParser;
import com.hbase.cli.schemaobjects.Codec;
import com.hbase.cli.schemaobjects.Column;
import com.hbase.cli.schemaobjects.Qualifier;
import com.hbase.cli.schemaobjects.Table;

import java.util.Set;

/**
 *
 */
public class TablePropertiesContext {

	private static TablePropertiesContext instance;
	Map<String, Table> tableMap;

	/**
	 * Singleton instance of TablePropertiesContext
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static TablePropertiesContext loadProperties(String filePath) throws Exception {
		if (instance == null) {
			instance = new TablePropertiesContext(filePath);
		}
		return instance;
	}

	/**
	 * Returns the already loaded TablePropertiesContext instance
	 * 
	 * @return
	 */
	public static TablePropertiesContext getProperties() {
		return instance;
	}

	private TablePropertiesContext(String filePath) throws Exception {
		tableMap = JsonParser.loadSchema(filePath);
	}

	/**
	 * Gets all the table names given in the configuration file
	 * 
	 * @return
	 */
	public Set<String> getAllTableNames() {
		Set<String> tableNameList = new HashSet<String>();
		for (String tableName : tableMap.keySet()) {
			tableNameList.add(tableName);
		}
		return tableNameList;
	}

	/**
	 * Returns rowkey datatype
	 * 
	 * @param tableName
	 * @return
	 */
	public String getRowkeyType(String tableName) {

		return tableMap.get(tableName).getRowkeyDataType();
	}

	/**
	 * Tells whether rowkey is encoded or not
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean isRowkeyEncoded(String tableName) {

		if (tableMap.get(tableName).getRowkeyDataType().equals(CoreConstants.CUSTOM)) {
			return true;
		}
		return false;
	}

	/**
	 * Gives Codec object of specifed table
	 * 
	 * @param tableName
	 * @return
	 */
	public Codec getTableCodec(String tableName) {
		return tableMap.get(tableName).getCodec();
	}

	/**
	 * Gets all the familynames of specified table
	 * 
	 * @param tableName
	 * @return
	 */
	public Set<String> getFamilyList(String tableName) {
		Set<String> familyList = new HashSet<String>();

		for (Column column : tableMap.get(tableName).getColumns()) {
			familyList.add(column.getFamilyName());
		}
		return familyList;
	}

	/**
	 * Gets all the columns of specified table in the format [ columnfamily :
	 * columnqualifier]
	 * 
	 * @param tableName
	 * @return
	 */
	public Set<String> getColumnList(String tableName) {
		return getColumns(tableName, null);
	}

	/**
	 * Gets all the columns of specified table & familyin the format [
	 * columnfamily : columnqualifier]
	 * 
	 * @param tableName
	 * @param familyName
	 * @return
	 */
	public Set<String> getColumnList(String tableName, String familyName) {
		return getColumns(tableName, familyName);
	}

	private Set<String> getColumns(String tableName, String familyName) {

		Set<String> columnList = new HashSet<String>();
		boolean found = false;

		for (Column column : tableMap.get(tableName).getColumns()) {
			String family = column.getFamilyName();

			if (familyName != null) {
				if (family.equals(familyName)) {
					found = true;
				} else
					continue;
			}
			for (Qualifier qualifier : column.getQualifiers()) {
				columnList.add(family + CoreConstants.COLON + qualifier.getName());
			}

			if (found) {
				break;
			}
		}

		return columnList;
	}

	/**
	 * Gives qualifier object for all the columns
	 * 
	 * @param tableName
	 * @return
	 */
	public Map<String, Qualifier> getQualifierMap(String tableName) {

		return getQualifierObject(tableName, null);
	}

	/**
	 * Gives qualifier object for all the columns in a specified family
	 * 
	 * @param tableName
	 * @param familyName
	 * @return
	 */
	public Map<String, Qualifier> getQualifierMap(String tableName, String familyName) {

		return getQualifierObject(tableName, familyName);
	}

	private Map<String, Qualifier> getQualifierObject(String tableName, String familyName) {

		Map<String, Qualifier> qualifierMap = new HashMap<String, Qualifier>();
		boolean found = false;

		for (Column column : tableMap.get(tableName).getColumns()) {
			String family = column.getFamilyName();

			if (familyName != null) {
				if (family.equals(familyName)) {
					found = true;
				} else
					continue;
			}

			for (Qualifier qualifier : column.getQualifiers()) {
				qualifierMap.put(family + CoreConstants.COLON + qualifier.getName(), qualifier);
			}

			if (found) {
				break;
			}
		}
		return qualifierMap;
	}

	/**
	 * 
	 * Gives qualifier object for the specified table & column
	 * 
	 * @param tableName
	 * @param familyName
	 * @param qualifierName
	 * @return
	 */
	public Qualifier getQualifier(String tableName, String familyName, String column) {

		Map<String, Qualifier> qualifierMap = getQualifierObject(tableName, familyName);
		return qualifierMap.get(column);
	}
}
