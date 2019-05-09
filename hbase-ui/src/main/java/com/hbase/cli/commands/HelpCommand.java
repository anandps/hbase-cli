package com.hbase.cli.commands;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.util.CLIUtil;

/**
 * This is the implementation for Help command. That will display all the usage and command description of the all commands.
 * 
 *
 */
public class HelpCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = HELP_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;
	private Command.Descriptor descriptor;
	private static final String KEY_ARGS_COMMAND = "command";
	List<Command> clicommands;

	public HelpCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.multiple;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plug(Context plug) {
		clicommands = plug.getCommandsByNamespace(HBASE_CLI_NAME_SAPCE);

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(Context ctx) {

		Map<String, String> argsMap = null;

		argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);

		String command = getOptions(argsMap);

		if (CLIUtil.isNull(command)) {
			if (argsMap != null && argsMap.size() > 0) {
				ctx.getIoConsole().writeOutput("Invalid argument(s)\n");
				ctx.getIoConsole().writeOutput(String.format("%n%s%n", getDescriptor().getUsage()));
				return null;
			}
			printAllHelp(ctx);
		} else {
			if (argsMap != null && argsMap.size() > 1) {
				ctx.getIoConsole().writeOutput("Invalid argument(s)\n");
				ctx.getIoConsole().writeOutput(String.format("%n%s%n", getDescriptor().getUsage()));
				
				return null;
			}
			printCommandHelp(ctx, command);
		}
		ctx.getIoConsole().writeOutput(String.format("%n%n", new Object[0]));
		return null;
	}

	private void printCommandHelp(Context ctx, String cmdName) {
		Map commands = ctx.mapCommands(ctx.getCommands());
		if (commands != null) {
			Command cmd = (Command) commands.get(cmdName.trim());
			if (cmd != null) {
				printCommandHelp(ctx, cmd);
			} else {
				ctx.getIoConsole().writeOutput(
						String.format("%nHelp for Command [%s] cannot not found.", new Object[] { cmdName.trim() }));
			}
		}
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
				return "Displays help information for available commands.";
			}

			public String getUsage() {
				return "Type 'help' or 'help [command_name]'";
			}

			public Map<String, String> getArguments() {
				return Collections.emptyMap();
			}
		});

	}

	protected String getOptions(Map<String, String> argsMap) {
		return (argsMap != null && argsMap.get(KEY_ARGS_COMMAND) != null) ? argsMap.get(KEY_ARGS_COMMAND) : null;
	}

	private void printAllHelp(Context ctx) {
		IOConsole c = ctx.getIoConsole();
		c.writeOutput(String.format("%nAvailable Commands", new Object[0]));
		c.writeOutput(String.format("%n--------------------\n", new Object[0]));

		List<Command> commands = ctx.getCommandsByNamespace(HBASE_CLI_NAME_SAPCE);

		Command cmd;
		for (Iterator i = commands.iterator(); i.hasNext(); c.writeOutput(String.format("%n%30s\t%s",
				new Object[] { cmd.getDescriptor().getName(), cmd.getDescriptor().getDescription() }))) {
			cmd = (Command) i.next();
		}

	}

	private void printCommandHelp(Context ctx, Command cmd) {
		if (cmd != null && cmd.getDescriptor() != null) {
			ctx.getIoConsole().writeOutput(String.format("%nCommand: %s - %s%n",
					new Object[] { cmd.getDescriptor().getName(), cmd.getDescriptor().getDescription() }));
			ctx.getIoConsole().writeOutput(String.format("Usage: %s", new Object[] { cmd.getDescriptor().getUsage() }));
			printCommandParamsDetail(ctx, cmd);
		} else {
			ctx.getIoConsole().writeOutput(String.format("%nUnable to display help for command.", new Object[0]));
		}
	}

	private void printCommandParamsDetail(Context ctx, Command cmd) {
		org.clamshellcli.api.Command.Descriptor desc = cmd.getDescriptor();
		if (desc == null || desc.getArguments() == null) {
			return;
		}
		IOConsole c = ctx.getIoConsole();
		c.writeOutput(String.format("%n%nOptions:", new Object[0]));
		c.writeOutput(String.format("%n--------", new Object[0]));
		
		for (Map.Entry<String, String> entry : desc.getArguments().entrySet()) {
			c.writeOutput((String.format("%n%35s\t", entry.getKey())));
			String data = entry.getValue();

			if (data.contains("\n")) {
				data = data.replace("\n", "\n" + getEmptySpaces(34));
			}
			c.writeOutput(data);
		}

	}

	private static String getEmptySpaces(int numberOfSpaces) {
		String emptySpaces = "";
		for (int i = 0; i < numberOfSpaces; i++) {
			emptySpaces += " ";
		}

		return emptySpaces;
	}
}
