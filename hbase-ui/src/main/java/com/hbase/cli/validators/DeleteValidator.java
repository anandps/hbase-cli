package com.hbase.cli.validators;

import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * This is the implementation of the delete validator. That will validate the delete command with arguments and throws
 * appropriate error message if the validation fails.
 * 
 *
 */
public class DeleteValidator implements CLIConstants {

    public static final String COMMAND_NAME = DELETE_COMMAND;
	
	/**
	 * Validate the arguments for "delete" command 
	 * 
	 * @param argsMap the map contains the arguments for delete command 
	 * @throws InvalidArgException throws invalid exception
	 */
	public static void validateDeleteCommand(Map<String, String> argsMap) throws InvalidArgException {
		if(CLIUtil.isMapNullOrEmpty(argsMap)) {
			throw new InvalidArgException("No arguments found for command \"" + COMMAND_NAME + "\"."
					+ "Please provide a valid argument." + "\n" + Validator.getHelpMessage(COMMAND_NAME));
		}

		String[] expectedArgs = new String[] { PARAM_TABLENAME, PARAM_ROW_KEY };

		Validator.doValidationForUnknownArguments(argsMap, expectedArgs);
		Validator.doValidationForDuplicateArguments(argsMap);
		Validator.doValidationForInvalidArgument(argsMap, expectedArgs);
		Validator.doValidationForEmptyArgs(argsMap);
		doValidationForMandatoryArguments(argsMap);// For delete command  tablename and row key is mandatory field.
		doTableNameValidation(String.valueOf(argsMap.get(PARAM_TABLENAME)));
		doRowKeyValidation(String.valueOf(argsMap.get(PARAM_ROW_KEY)));
	}
	
	
	private static void doTableNameValidation(String tableName) throws InvalidArgException {
		Validator.doValidationForTablePattern(tableName);
		// To do : Check any pending validation for tablename
	}
	
	private static void doRowKeyValidation(String rowKey) throws InvalidArgException {
		if (!Validator.doValidationForRowKeyPattern(rowKey)) {
			throw new InvalidArgException("The argument " + PARAM_ROW_KEY + "  is having the invalid value " + rowKey + "."
					+ " Row key is not following the correct pattern. " + Validator.getHelpMessage(COMMAND_NAME));
		}
	}
	
	private static void doValidationForMandatoryArguments(Map<String, String> argsMap) throws InvalidArgException {
		// Check the table name 
		if(CLIUtil.isNull(argsMap.get(PARAM_TABLENAME))) {
			throw new InvalidArgException(
					"Please provide mandatory argument " + PARAM_TABLENAME + ". " + Validator.getHelpMessage(COMMAND_NAME));
		}
		
		//Check the row key
		if (CLIUtil.isNull(argsMap.get(PARAM_ROW_KEY))) {
			throw new InvalidArgException(
					"Please provide mandatory argument " + PARAM_ROW_KEY + ". " + Validator.getHelpMessage(COMMAND_NAME));
		}
	}
}
