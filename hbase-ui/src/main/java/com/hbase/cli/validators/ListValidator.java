package com.hbase.cli.validators;

import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * This is an implementation of the list validator. That will validate the list command with arguments and throws
 * appropriate error message if the validation fails.
 * 
 *
 */
public class ListValidator implements CLIConstants {
	
   private static final String COMMAND_NAME = LIST_COMMAND;
	
	/**
	 * Validate the "list" command. There should be no arguments for list command  
	 * 
	 * @param argsMap the map contains the arguments for list command
	 * @throws InvalidArgException throws invalid exception
	 */
	public static void validateListCommand(Map<String, String> argsMap) throws InvalidArgException {
		if(!CLIUtil.isMapNullOrEmpty(argsMap)) {
			String[] expectedArgs = new String[] { PARAM_TABLENAME };
			
			Validator.doValidationForUnknownArguments(argsMap, expectedArgs);
			Validator.doValidationForDuplicateArguments(argsMap);
			Validator.doValidationForInvalidArgument(argsMap, expectedArgs);
			Validator.doValidationForEmptyArgs(argsMap);
			doValidationForOptionalArguments(argsMap);
			    
		}
	}

	private static void doValidationForOptionalArguments(Map<String, String> argsMap) throws InvalidArgException {
		if(CLIUtil.isNullOrEmpty(argsMap.get(PARAM_TABLENAME))) {
			throw new InvalidArgException(
					"Please provide optional argument " + PARAM_TABLENAME + " if need. " + Validator.getHelpMessage(COMMAND_NAME));
		}
		Validator.doValidationForTablePattern(argsMap.get(PARAM_TABLENAME));
		
	}
}
