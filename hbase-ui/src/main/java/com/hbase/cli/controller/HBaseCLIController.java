package com.hbase.cli.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.core.AnInputController;

import com.hbase.cli.commands.BaseCommand;
import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.parser.CommandLineParser;

/**
 * This is an implementation of the InputController for the HBase CLI. This
 * implementation parses command-line input from Context(KEY_INPUT_LINE) value.
 * It parses the input using JSon notation. The command-line input is expected
 * to be of the form: <code> </code> The controller parses the input line and
 * dispatches the parsed command and params to a Command instance on the
 * classpath.
 */
public class HBaseCLIController extends AnInputController implements CLIConstants {

	private Map<String, Command> commands;
	private static final NotificationLogger controllerlogger = NotificationLoggerFactory.getLogger();

	private static final String LOGGING_CLASS_NAME = HBaseCLIController.class.getName();

	static {
		// Setting logging properties
		String logFile = System.getProperty(CLIConstants.LOG_FILE_NAME);
		if (logFile != null) {
			MDC.put(CLIConstants.LOG_FILE_NAME, logFile);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handle(Context ctx) {
		boolean handled = false;

		String cmdLine = (String) ctx.getValue(Context.KEY_COMMAND_LINE_INPUT);
		controllerlogger.postNotification(LOGGING_CLASS_NAME, LogLevel.INFO, "hbase> "+ cmdLine);
		
		if (cmdLine != null && !cmdLine.isEmpty()) {
			cmdLine = cmdLine.trim();
			String[] tokens = cmdLine.split(WHITESPACES, 2);
			String cmdName = tokens[0];
			String argLine = tokens.length == 1 ? "" : tokens[1].trim();

			Command cmd = null;

			if ((this.commands != null) && ((cmd = (Command) this.commands.get(cmdName)) != null)) {

				Map<String, Object> argsMap;

				// To parse a command line arguments
				argsMap = CommandLineParser.parseCommandLine(argLine);
				ctx.putValue(Context.KEY_COMMAND_LINE_ARGS, argsMap);
				
				try {
					cmd.execute(ctx);
				} catch (Exception se) {
					ctx.getIoConsole().writeOutput(se.getMessage());
				}
				handled = true;
			} else {
				handled = false;
			}
		}
		return handled;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plug(Context plug) {
		// Load all the Dependencies that needed to fetch the data from HBase
		// Table.
		// Load all the commands and and corresponding Command Instance and put
		// into a Map
		super.plug(plug);

		List<Command> cliCommands = plug.getCommandsByNamespace(HBASE_CLI_NAME_SAPCE);

		if (cliCommands.size() > 0) {
			commands = plug.mapCommands(cliCommands);
			Set<String> cmdHints = new TreeSet<String>();
			// plug each Command instance and collect input hints
			for (Command cmd : cliCommands) {

				cmd.plug(plug);
				cmdHints.addAll(collectInputHints(cmd));

			}

			// save expected command input hints
			 setExpectedInputs(cmdHints.toArray(new String[0]));

		} else {
			plug.getIoConsole().writeOutput(String
					.format("%nNo commands were found for input controller" + " [%s].%n%n", this.getClass().getName()));
		}
	}


	public boolean regexValidation(String regex) {
		// Validate the regex
		return true;
	}
	
	/**
	 * This method collects the hints that are attached to a command and format
	 * them as "cmdName", "cmdName option1", "cmdName option2", etc.
	 * 
	 * @param cmd
	 *            the command to document
	 * @return a Set<String> containing the hints.
	 */
	protected Set<String> collectInputHints(Command cmd) {
		Command.Descriptor desc = cmd.getDescriptor();

		if (desc == null)
			return null;

		Set<String> result = new TreeSet<String>();
		String cmdName = desc.getName();

		Map<String, String> args = desc.getArguments();

		boolean isSingle = false;
		boolean isMultiple = false;

			isSingle = ((BaseCommand) cmd).getCommandHintType()
					.equals(BaseCommand.CommandHintType.single);
			isMultiple = ((BaseCommand) cmd).getCommandHintType()
					.equals(BaseCommand.CommandHintType.multiple);

		if (args != null) {

			if (isSingle) {
				StringBuilder sb = new StringBuilder();
				sb.append(cmdName);

				for (String hint : args.keySet()) {
					sb.append(" ");
					sb.append(hint);
				}

				result.add(String.format("%s", sb.toString()));

			} else if (isMultiple) {
				result.add(desc.getName());

				for (String hint : args.keySet()) {
					// split hints in case they are as "option1, option2, etc"
					String[] hintSet = hint.split("\\s*,\\s*");

					for (String hintVal : hintSet) {
						result.add(String.format("%s %s", cmdName, hintVal));
					}
				}
			} else {
				result.add(desc.getName());
			}
		}

		return result;
	}

}
