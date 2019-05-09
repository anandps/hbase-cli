package com.hbase.cli.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.handler.CoreHandler;
import com.hbase.cli.validators.DeleteValidator;

/**
 * This is an implementation for Delete Command. This will delete the rows as per the input given
 * It gets the argument from Argument Map and process the delete operation
 * 
 *
 */
public class DeleteCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = DELETE_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;

	private Command.Descriptor descriptor;

	public DeleteCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.single;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plug(Context arg0) {

		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(Context ctx) throws Exception {

		IOConsole console = ctx.getIoConsole();
		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);

		DeleteValidator.validateDeleteCommand(argsMap);
		CoreHandler.handleDelete(argsMap.get(PARAM_TABLENAME), argsMap.get(PARAM_ROW_KEY));
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
				return "Deletes the specified row from HBase table";
			}

			public String getUsage() {

				return CMD_NAME + " " + PARAM_TABLENAME + "=<TABLE_NAME>" + " " + PARAM_ROW_KEY + "=<ROW_KEY>";
			}

			public Map<String, String> getArguments() {
				Map<String, String> args = new LinkedHashMap<String, String>();
				args.put(PARAM_TABLENAME + "=<TABLE_NAME>", "Name of the table");
				args.put(PARAM_ROW_KEY + "=<ROW_KEY>", "Row key that has to be deleted");
				// More arguments goes here.
				return args;
			}
		});
	}

	private void printUsage(IOConsole console) {

		console.writeOutput(String.format("%nUsage:", new Object[0]));
		console.writeOutput(String.format("%n--------------------\n", new Object[0]));
		console.writeOutput(getDescriptor().getUsage() + "\n");
	}
}
