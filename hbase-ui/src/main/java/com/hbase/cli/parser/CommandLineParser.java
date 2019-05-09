package com.hbase.cli.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hbase.cli.constants.CLIConstants;

/**
 * The parser implementation for Command Line. That will parse the command line
 * input
 * 
 *
 */
public class CommandLineParser {

	/**
	 * Utility method to parse command line arguments
	 * 
	 * @param argLine
	 *            the string representation of command line arguments
	 * @return argdMap the map contains the arguments as key value pair.
	 */
	public static Map<String, Object> parseCommandLine(String argLine) {
		Map<String, Object> argsMap = new HashMap<String, Object>();

		boolean multipleCommands = false;
		boolean repeatedArguments = false;

		if ((argLine != null) && (!argLine.isEmpty())) {
			String[] argTokens = parseCommandLineInput(argLine);
			if (argTokens != null && argTokens.length > 0) {
				// Split each argument separately
				for (int i = 0; i <= argTokens.length - 1; i++) {
					if (!argTokens[i].isEmpty()) {
						if (argTokens[i].contains(CLIConstants.EQUALS)) {
							String[] individualArgument = argTokens[i].split(CLIConstants.EQUALS, 2);

							if (argsMap.get(individualArgument[0]) != null) {
								repeatedArguments = true;
							}
							argsMap.put(individualArgument[0], individualArgument[1]);
						} else {
							if (argsMap.get("command") != null) {
								multipleCommands = true;
							}
							String commandValue = argsMap.get("command") != null ? (String) argsMap.get("command") : "";
							argsMap.put("command", buildMultipleCommandParams(commandValue + argTokens[i]));
						}

					}
				}
			}

			if (multipleCommands) {
				String trimedCommandParms = ((String) argsMap.get("command")).trim();
				argsMap.put("command", trimedCommandParms);
				argsMap.put(CLIConstants.ERROR_INDICATOR_IN_COMMAND_STRUCTURE, CLIConstants.MULTIPLE_COMMANDS);
			} else if (repeatedArguments) {
				argsMap.put(CLIConstants.ERROR_INDICATOR_IN_COMMAND_STRUCTURE, CLIConstants.REPEATED_OPTIONS);
			}
		}

		return argsMap;
	}

	private static String[] parseCommandLineInput(String cmd) {
		if (cmd == null || cmd.length() == 0) {
			return new String[] {};
		}
		cmd = cmd.trim().replaceAll(CLIConstants.WHITESPACES, " ").replaceAll(CLIConstants.SPACE_NEAR_EQUALS, "=");
		Pattern pattern = Pattern.compile(CLIConstants.ARG_SPLIITER , Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(cmd);
		List<String> matches = new ArrayList<String>();
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		String[] parsedCommand = matches.toArray(new String[] {});
		return parsedCommand;
	}

	private static String buildMultipleCommandParams(String commandParam) {
		String buildedString = commandParam + "\n";
		return buildedString;
	}

}
