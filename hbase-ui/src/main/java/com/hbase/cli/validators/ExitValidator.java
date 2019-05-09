package com.hbase.cli.validators;

import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * This is an implementation of the exit validator. That will validate the exit command with arguments and throws
 * appropriate error message if the validation fails.
 * 
 *
 */
public class ExitValidator implements CLIConstants {
    
	private static final String COMMAND_NAME = EXIT_COMMAND;
	
	/**
	 * Validate the "exit" command. There should be no arguments for exit command  
	 * 
	 * @param argsMap the map contains the arguments for exit command
	 * @throws InvalidArgException throws invalid exception
	 */
	public static void validateExitCommand(Map<String, String> argsMap) throws InvalidArgException {
		if(!CLIUtil.isMapNullOrEmpty(argsMap)) {
			throw new InvalidArgException("There should be no arguments for " + COMMAND_NAME + " Command. " + Validator.getHelpMessage(COMMAND_NAME));
		}
	}
}
