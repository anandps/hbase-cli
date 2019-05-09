package com.hbase.cli.validators;

import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.constants.FilterDescription;
import com.hbase.cli.exception.InvalidArgException;
import com.hbase.cli.util.CLIUtil;

public class ShowFilterValidator implements CLIConstants {

	private static final String COMMAND_NAME = "showfilter";

	/**
	 * Validate the "show filter" command.
	 * 
	 * @param argsMap
	 *            the map contains the arguments for show filter command
	 * @throws InvalidArgException
	 *             throws invalid exception
	 */
	public static void validateShowFilterCommand(Map<String, String> argsMap) throws InvalidArgException {
		if (!CLIUtil.isMapNullOrEmpty(argsMap)) {
			String[] expectedArgs = new String[] { PARAM_FILTER };

			Validator.doValidationForUnknownArguments(argsMap, expectedArgs);
			Validator.doValidationForDuplicateArguments(argsMap);
			Validator.doValidationForInvalidArgument(argsMap, expectedArgs);
			Validator.doValidationForEmptyArgs(argsMap);
			try {
				String usage = FilterDescription.valueOf(argsMap.get(PARAM_FILTER).toLowerCase()).toString();
			} catch (Exception e) {
				throw new InvalidArgException(
						"Invalid Filter Name. Choose one from the list \n \n" + FilterDescription.all.toString());
			}

		}
	}

}
