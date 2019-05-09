package com.hbase.cli.validators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

/**
 * The validator implementation consist of all common validation
 * 
 *
 */
public class Validator {

	/**
	 * Validator method to validate whether the argument is having empty value
	 * 
	 * @param argsMap map contains the arguments as key and value pair
	 * @throws InvalidArgException 
	 */
	public static void doValidationForEmptyArgs(Map<String, String> argsMap) throws InvalidArgException {
		  for(Map.Entry<String, String> entry : argsMap.entrySet()) {
			  if((entry.getValue() == null) || entry.getValue().isEmpty()) {
				  throw new InvalidArgException("The argument " + entry.getKey() + " has invalid value. Please provide a valid value\n");
			  }
		  }
	}
	
	/**
	 * Validator method to validate whether the actual argument and expected argument are same
	 * 
	 * @param actualArgs the actual argument needed for command to execute
	 * @param expectedArgs the user given argument for the command
	 * @throws InvalidArgException
	 */
	public static void doValidationForInvalidArgument(Map<String, String> actualArgs, String[] expectedArgs) throws InvalidArgException {
		
		for(Map.Entry<String, String> entry :  actualArgs.entrySet()) {
			if(!(Arrays.asList(expectedArgs)).contains(entry.getKey())) {
				
				throw new InvalidArgException("The argument " + entry.getKey() + " is invalid." + " " + getHelpMessage());
			}
		}
	}
	
	/**
	 * Validator method to validated the unknown arguments. Ex: if the argument is not able to form as key and value pair,
	 * that will be put into the "argMap" with the key of "command". If the key command is found in that map, then throw exception 
	 * Ex : argsMap => {command = [unknown1, unknown2]}
	 * 
	 * @param argsMap map contains the arguments as key and value pair
	 * @throws InvalidArgException
	 */
	public static void doValidationForUnknownArguments(Map<String, String> argsMap, String[] expectedArgs) throws InvalidArgException {
		
		if(argsMap != null && argsMap.get("command") != null) {
			String[] paramsArray = (argsMap.get("command")).split("\n");
			String message = "";
			for(int i=0; i<paramsArray.length; i++) {
				if(Arrays.asList(expectedArgs).contains((paramsArray[i]).trim())) {
					message = "Please provide a valid value for " + paramsArray[i] + "\n";
					break;
				}
				message += "Unknown Argument " + paramsArray[i] + "\n";
			}
			
			throw new InvalidArgException(message.trim() + ". " + getHelpMessage());
		}
	}
	
	/**
	 * Validator method to find the duplicate the arguments
	 * 
	 * @param argsMap map contains the arguments as key and value pair
	 * @throws InvalidArgException
	 */
	public static void doValidationForDuplicateArguments(Map<String, String> argsMap) throws InvalidArgException {
	   if(argsMap != null && argsMap.get(CLIConstants.ERROR_INDICATOR_IN_COMMAND_STRUCTURE) != null) {
		   String repeatedOption = argsMap.get(CLIConstants.ERROR_INDICATOR_IN_COMMAND_STRUCTURE);
		   if(repeatedOption.equals(CLIConstants.REPEATED_OPTIONS)) {
			   throw new InvalidArgException("Duplicate argument(s) were found." + " " + getHelpMessage());
		   }
	   }
	}
	
	/**
	 * Validator method to validate the table name using regex.
	 * 
	 * @param tableName the table name to validate
	 * @throws InvalidArgException
	 */
	public static void doValidationForTablePattern(String tableName) throws InvalidArgException {
		// Need to confirm the table name pattern
		Pattern pattern = Pattern.compile(CLIConstants.TABLE_NAME_REGEX);
		Matcher matcher = pattern.matcher(tableName);

		if (!matcher.matches()) {
			throw new InvalidArgException(
					"The argument --table is having the invalid value " + tableName + "."
							+ " Table Name is not following the correct pattern. That should consist of only alpha numeric, \"-\" and \"_\". " + getHelpMessage());
		}

	}
	
	/**
	 * Validator method to validate the rowkey using regex.
	 * 
	 * @param rowKey the rowkey to validate
	 * @throws InvalidArgException
	 */
	public static boolean doValidationForRowKeyPattern(String rowKey) throws InvalidArgException {
		Pattern pattern = Pattern.compile(CLIConstants.ROW_KEY_REGEX);
		Matcher matcher = pattern.matcher(rowKey);
		return matcher.matches();
		
	}
	
	/**
	 * Utility method to get the common help message based on the command name
	 *  
	 * @param command the command name
	 * @return
	 */
	public static String getHelpMessage(String command) {
		return "Type \"help " + command + "\" For usage\n";
	}
	
	private static String getHelpMessage() {
		   return "Type \"help [COMMAND_NAME]\" For usage\n";
	}
	
}
