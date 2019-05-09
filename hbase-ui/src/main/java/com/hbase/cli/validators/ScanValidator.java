package com.hbase.cli.validators;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * This is the implementation of the scan validator. That will validate the scan command with arguments and throws
 * appropriate error message if the validation fails.
 * 
 *
 */
public class ScanValidator implements CLIConstants {

	private static final String COMMAND_NAME = SCAN_COMMAND;

	/**
	 * Validation method to validate the Scan command arguments
	 * 
	 * @param argsMap
	 *            maps containing the arguments for scan command
	 * @throws InvalidArgException
	 */
	public static void validateScanCommand(Map<String, String> argsMap) throws InvalidArgException {
		if (CLIUtil.isMapNullOrEmpty(argsMap)) {
			throw new InvalidArgException("No arguments found for command \"" + COMMAND_NAME + "\"."
					+ " Please provide valid argument." + "\n" + Validator.getHelpMessage(COMMAND_NAME));
		}

		String[] expectedArgs = getArgumentsAsArray();

		Validator.doValidationForUnknownArguments(argsMap, expectedArgs);
		Validator.doValidationForDuplicateArguments(argsMap);
		Validator.doValidationForInvalidArgument(argsMap, expectedArgs);
		Validator.doValidationForEmptyArgs(argsMap);

		doValidationForMandatoryArguments(argsMap);// For scan command, table
													// name is mandatory field.
		doValidationForDependentArguments(argsMap);
		doValidationForStartRowKey(argsMap.get(PARAM_START_ROWKEY));
		doValidationForEndRowKey(argsMap.get(PARAM_END_ROWKEY));
		doValidationForColumns(argsMap.get(PARAM_COLUMN));
		doValidationForLimit(argsMap.get(PARAM_LIMIT));
		doValidationForVersion(argsMap.get(PARAM_VERSION));
		doValidationForFilterQuery(argsMap.get(PARAM_FILTER_QUERY));
		doValidationForDump(argsMap.get(PARAM_DUMP));
	}

	private static void doValidationForDump(String dumpFilePath) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(dumpFilePath)) {
			try {
				File file = new File(dumpFilePath);

				if (!file.exists()) {
					file.createNewFile();
				} else {

					if (file.isDirectory()) {
						throw new InvalidArgException("The given path " + dumpFilePath + " is a directory");
					}
					// Check the given file is having the basic attribute to be
					// a file
					// @See the Files.readAttributes method
					if (!file.isFile()) {
						throw new InvalidArgException("The given file " + dumpFilePath + " is not a valid file");
					}
					if (!file.canWrite()) {
						throw new InvalidArgException(
								"Unable to write to a file " + dumpFilePath + " : Permission denied");
					}
				}
			} catch (IOException ex) {
				String exceptionMessage = ex.getMessage();
				if (exceptionMessage.contains("Permission denied")) {
					exceptionMessage = "Unable to create a file " + dumpFilePath + " : Permission denied";
				}
				throw new InvalidArgException(exceptionMessage);
			}
		}
	}

	private static void doValidationForFilterQuery(String filterQuery) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(filterQuery)) {
			filterQuery = filterQuery.trim();
			if (!(filterQuery.indexOf("\"") == 0) || !(filterQuery.lastIndexOf("\"") == filterQuery.length() - 1)) {
				throw new InvalidArgException(
						"Please provide valid filter query. " + Validator.getHelpMessage(COMMAND_NAME));
			}
		}

	}

	private static void doTableNameValidation(String tableName) throws InvalidArgException {
		Validator.doValidationForTablePattern(tableName);

		// To do : Check any pending validation for tablename
	}

	private static void doValidationForVersion(String versionValue) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(versionValue)) {
			int version;
			try {
				version = Integer.parseInt(versionValue);
			} catch (NumberFormatException ex) {
				throw new InvalidArgException("The version value should be numbers only");
			}

			if (version <= 0) {
				throw new InvalidArgException("The version should be greater than zero");
			}

			// Todo : Need validation for Maximium value of version. Need to
			// check whether it' is configuration
		}
	}

	private static void doValidationForLimit(String limitValue) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(limitValue)) {
			int limit;
			try {
				limit = Integer.parseInt(limitValue);
			} catch (NumberFormatException ex) {
				throw new InvalidArgException("The limit value should be numbers only");
			}
			if (limit <= 0) {
				throw new InvalidArgException("The limit should be greater than zero");
			}

		}
	}

	private static void doValidationForMandatoryArguments(Map<String, String> argsMap) throws InvalidArgException {
		if (CLIUtil.isNullOrEmpty(argsMap.get(PARAM_TABLENAME))) {
			throw new InvalidArgException(
					"Please provide a mandatory argument tablename. " + Validator.getHelpMessage(COMMAND_NAME));
		}

		doTableNameValidation(argsMap.get(PARAM_TABLENAME));
	}

	private static void doValidationForDependentArguments(Map<String, String> argsMap) throws InvalidArgException {
		if (!CLIUtil.isNull(argsMap.get(PARAM_END_ROWKEY))) {
			if (CLIUtil.isNull(argsMap.get(PARAM_START_ROWKEY))) {
				throw new InvalidArgException("The argument " + PARAM_START_ROWKEY + " is mandatory for the argument "
						+ PARAM_END_ROWKEY + ". " + Validator.getHelpMessage(COMMAND_NAME));
			}
		}
	}

	private static void doValidationForEndRowKey(String endRowKey) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(endRowKey)) {
			if (!Validator.doValidationForRowKeyPattern(endRowKey)) {
				throw new InvalidArgException("The argument" + PARAM_END_ROWKEY + "  is having the invalid value "
						+ endRowKey + "." + " Row key is not following the correct pattern. "
						+ Validator.getHelpMessage(COMMAND_NAME));
			}
		}

	}

	private static void doValidationForStartRowKey(String startRowKey) throws InvalidArgException {
		if (!CLIUtil.isNullOrEmpty(startRowKey)) {
			if (!Validator.doValidationForRowKeyPattern(startRowKey)) {
				throw new InvalidArgException("The argument" + PARAM_START_ROWKEY + "  is having the invalid value "
						+ startRowKey + "." + " Row key is not following the correct pattern. "
						+ Validator.getHelpMessage(COMMAND_NAME));
			}
		}

		// Todo, Need validation based on how the row key is formed and what
		// are all the combonents that used to form row key.
	}

	private static void doValidationForColumns(String columnArgs) throws InvalidArgException {
		try {
			if (!CLIUtil.isNullOrEmpty(columnArgs)) {
				// The column argument should be like :
				// --columns=fam1,fam2:qua1,fam2:qua2
				Pattern pattern = Pattern.compile(COLUMN_PARAMS_REGEX);

				Matcher matcher = pattern.matcher(columnArgs);

				if (!matcher.matches()) {
					throw new InvalidArgException("The argument " + PARAM_COLUMN + "  is having the invalid value" + " "
							+ columnArgs + ". " + " Not following the correct pattern for" + "argument. "
							+ Validator.getHelpMessage(COMMAND_NAME));
				}

				if (columnArgs.contains(",")) {

					String[] columns = columnArgs.split(",");

					for (int i = 0; i < columns.length; i++) {
						// Check the individual column qualifier with column
						// family
						String columQualifersWithColumnFamily = columns[i].trim();
						if (columQualifersWithColumnFamily.contains(":")) {

							String[] columnWithQualifier = columQualifersWithColumnFamily.split(":");

							if (columnWithQualifier.length != 2) {// Check if
																	// the
																	// columnfamily
																	// is given
																	// like
																	// family:qual1:qual2
																	// or
																	// family:,family:qaul2,family:,:,family:qaul2
								throw new InvalidArgException("The argument in " + PARAM_COLUMN
										+ "  is having the invalid value" + " " + columQualifersWithColumnFamily);
							}

							for (int j = 0; j < columnWithQualifier.length; j++) {

								String column = columnWithQualifier[j].trim();
								if (column.isEmpty()) {
									throw new InvalidArgException("The argument " + PARAM_COLUMN
											+ "  is having the invalid value" + " " + columQualifersWithColumnFamily);
								}
							}

						}
					}

				}
			}
		} catch (Exception ex) {
			throw new InvalidArgException(ex.getMessage());
		}
	}

	private static String[] getArgumentsAsArray() {
		return new String[] { PARAM_TABLENAME, PARAM_START_ROWKEY, PARAM_END_ROWKEY, PARAM_COLUMN, PARAM_FILTER_QUERY,
				PARAM_LIMIT, PARAM_VERSION, PARAM_DUMP };
	}
}
