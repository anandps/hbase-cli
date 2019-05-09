package com.hbase.cli.commands;

import java.util.Collections;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.validators.ExitValidator;

/**
 * This is the implementation for Exit command. That will used to exit from the cli prompt
 * 
 *
 */
public class ExitCommand extends BaseCommand implements CLIConstants {
	private static final String CMD_NAME = EXIT_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;
	private Command.Descriptor descriptor;

	public ExitCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.none;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plug(Context arg0) {

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(Context ctx) throws Exception {
		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);

		IOConsole console = ctx.getIoConsole();

		ExitValidator.validateExitCommand(argsMap);

		Integer errorCode = (Integer) ctx.getValue("error");

		if (errorCode != null) {
			System.exit(errorCode.intValue());
		} else {
			System.exit(1);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Descriptor getDescriptor() {
		return (descriptor != null) ? descriptor : (descriptor = new Command.Descriptor() {

			public String getNamespace() {
				return NAMESPACE;
			}

			public String getName() {
				return CMD_NAME;
			}

			public String getDescription() {
				return "Exits from Hbase Cli";
			}

			public String getUsage() {
				return "Type 'exit'";
			}

			public Map<String, String> getArguments() {
				return Collections.emptyMap();
			}
		});
	}

	protected void printUsage(IOConsole console) {

		console.writeOutput(String.format("%nUsage:", new Object[0]));
		console.writeOutput(String.format("%n--------------------\n", new Object[0]));
		console.writeOutput(getDescriptor().getUsage() + "\n");
	}

}
