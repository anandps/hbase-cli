package com.hbase.cli.validators;

import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * This is the implementation of the describe validator. That will validate the describe command with arguments and throws
 * appropriate error message if the validation fails.
 * 
 *
 */
public class DescribeValidator implements CLIConstants {

	private static final String COMMAND_NAME = DESCRIBE_COMMAND;

	/**
	 * Validate the arguments for "describe" command
	 * 
	 * @param argsMap
	 *            the map contains the arguments for describe command
	 * @throws InvalidArgException
	 *             throws invalid exception
	 */
	public static void validateDescribeCommand(Map<String, String> argsMap) throws InvalidArgException {
		if(CLIUtil.isMapNullOrEmpty(argsMap)) {
			throw new InvalidArgException("No arguments found for command \"" + COMMAND_NAME + "\"."
					+ "Please provide a valid argument." + "\n" + Validator.getHelpMessage(COMMAND_NAME));
		}

		String[] expectedArgs = new String[] { PARAM_TABLENAME };

		Validator.doValidationForUnknownArguments(argsMap, expectedArgs);
		Validator.doValidationForDuplicateArguments(argsMap);
		Validator.doValidationForInvalidArgument(argsMap, expectedArgs);
		Validator.doValidationForEmptyArgs(argsMap);
		doValidationForMandatoryArguments(argsMap);// For describe command table
													// name is mandatory field.
	}

	private static void doValidationForMandatoryArguments(Map<String, String> argsMap) throws InvalidArgException {
		if (CLIUtil.isNull(argsMap.get(PARAM_TABLENAME))) {
			throw new InvalidArgException(
					"Please provide mandatory argument " + PARAM_TABLENAME + ". " + Validator.getHelpMessage(COMMAND_NAME));
		}

		doTableNameValidation(argsMap.get(PARAM_TABLENAME));
	}

	private static void doTableNameValidation(String tableName) throws InvalidArgException {
		Validator.doValidationForTablePattern(tableName);

		// To do : Check any pending validation for tablename
	}
}