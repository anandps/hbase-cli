package com.hbase.cli.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.utils.blu.constants.DataType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.exception.NoConfFoundException;
import com.hbase.cli.exception.ValidationException;
import com.hbase.cli.schemaobjects.Column;
import com.hbase.cli.schemaobjects.Qualifier;
import com.hbase.cli.schemaobjects.Table;


/**
 *Utility to load the table configurations given under the conf/ directory of hbase-cli
 *
 */

public class JsonParser {

	private static final NotificationLogger jsonParserLogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = JsonParser.class.getName();

	/**
	 * Loads the Json schema from the given path. Loads even if the path
	 * contains multiple json files.
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Table> loadSchema(String path) throws Exception {

		// Initializations
		Map<String, Table> tables = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();

		File dir = new File(path);
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				return file.getName().endsWith(CoreConstants.ENDS_WITH_JSON);
			}
		});

		if (files.length == 0) {
			throw new NoConfFoundException("Table configuration files not found!!!");
		}

		for (File file : files) {
			List<Table> tableObjects = null;
			
			try{
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			tableObjects = objectMapper.readValue(file, new TypeReference<List<Table>>() {
			});
			} catch (Exception e){
				jsonParserLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR,"Incorrect Json file found!! File -"+file.getName(),
						e);
				continue;
			}
			
			for (Table table : tableObjects) {

				try {
					// Table name is mandatory
					String field = table.getTableName();
					if (field != null && !field.isEmpty()) {
						if (!validate(CoreConstants.tableRegex, field)) {
							throw new ValidationException(
									"Field : 'tableName' contains invalid characters. It can contain only alphanumeric, -, . , *");
						}
					} else {
						throw new ValidationException("Field : 'tableName' is mandatory!!!");
					}

					// Rowkey datatype is mandatory
					field = table.getRowkeyDataType();
					if (field != null && !field.isEmpty()) {
						if (!validate(field)) {
							throw new ValidationException(
									"Incompatible rowkeydatatype found for table:" + table.getTableName());
						}
					} else
						throw new ValidationException(
								"Field : 'rowkeydatatype' is missing for table:" + table.getTableName());

					// Codec is mandatory if rowkey datatype is custom
					field = table.getCodec().getClassName();
					if (table.getRowkeyDataType().equalsIgnoreCase(CoreConstants.CUSTOM)) {
						if (field != null && !field.isEmpty()) {
							if (!validate(CoreConstants.classNameRegex, field)) {
								throw new ValidationException(
										"Codec className field contains invalid character for table : "
												+ table.getTableName()
												+ " ,className should contains only the alphabets,numbers,.,-");
							}

							// validating classpath regex
							field = table.getCodec().getClassPath();
							if (field != null && !field.isEmpty()) {
								if (!validate(CoreConstants.classpathRegex, field)) {
									throw new ValidationException(
											"Codec classPath field contains invalid character for table :"
													+ table.getTableName());
								}
							}
						} else
							throw new ValidationException(
									"Field : 'className' under structure 'codec' is missing for table:"
											+ table.getTableName());
					}

					// Columns are mandatory
					if (table.getColumns() != null && !table.getColumns().isEmpty()) {
						List<Column> columns = table.getColumns();
						for (Column column : columns) {

							// validating familyName
							field = column.getFamilyName();
							if (field != null && !field.isEmpty()) {
								if (!validate(CoreConstants.familyRegex, column.getFamilyName())) {
									throw new ValidationException("familyName field for table :" + table.getTableName()
											+ " contains invalid character. FamilyName cannot contain control characters and ':'"
											+ column.getFamilyName());
								}
							} else {
								throw new ValidationException(
										"Field : 'familyName' under 'columns' is missing for table:"
												+ table.getTableName());
							}

							// validating qualifiers
							if (column.getQualifiers() != null && !column.getQualifiers().isEmpty()) {
								List<Qualifier> qualifiers = column.getQualifiers();
								for (Qualifier qualifier : qualifiers) {

									// validating qualifier name
									field = qualifier.getName();
									if (field == null || field.isEmpty()) {
										throw new ValidationException(
												"Field : 'name' under 'qualifers' is missing for table:"
														+ table.getTableName() + " with family :"
														+ column.getFamilyName());
									}

									// validating qualifier datatype
									if (!validate(qualifier.getType())) {
										throw new ValidationException(" Invalid qualifier type found for the  table : "
												+ table.getTableName() + " with family :" + column.getFamilyName());
									}
								}
							} else {
								throw new ValidationException("Field : 'qualifier' is missing for table:"
										+ table.getTableName() + " with family:" + column.getFamilyName());
							}

						}
					} else {
						throw new ValidationException("Field : 'columns' is missing for table:" + table.getTableName());
					}

					// Adding the successful table object in the map
					tables.put(table.getTableName(), table);
				} catch (ValidationException e) {
					// TODO: handle exception
					jsonParserLogger.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR,
							e.getMessage() + CoreConstants.REFER_TEMPLATE);
				}
			}
		}

		return tables;
	}

	private static boolean validate(String pattern, String value) {

		return Pattern.matches(pattern, value);
	}

	private static boolean validate(String dataType) {
		boolean result = false;
		DataType type = DataType.valueOf(dataType.toUpperCase());
		switch (type) {
		case INT:
			result = true;
			break;
		case BYTES:
			result = true;
			break;
		case FLOAT:
			result = true;
			break;
		case LONG:
			result = true;
			break;
		case STRING:
			result = true;
			break;
		case DOUBLE:
			result = true;
			break;
		case CUSTOM:
			result = true;
			break;
		case BOOLEAN:
			result = true;
			break;
		default:
			result = false;
			break;
		}
		return result;

	}

}
