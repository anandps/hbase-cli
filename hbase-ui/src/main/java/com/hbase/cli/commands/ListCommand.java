package com.hbase.cli.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.handler.CoreHandler;
import com.hbase.cli.validators.ListValidator;

/**
 * This is the implementation for the List Command. That will list out all the configured HBase tables
 * 
 *
 */
public class ListCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = LIST_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;
	private Command.Descriptor descriptor;

	private IOConsole console;
	
	public ListCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.multiple;
	}
	
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

		console = ctx.getIoConsole();
		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);
		ListValidator.validateListCommand(argsMap);
		printList(CoreHandler.handleList(argsMap.get(PARAM_TABLENAME)), argsMap.get(PARAM_TABLENAME));
		return null;

	}

	private void printList(Set<String> tableList, String tableName) {
		
		if (tableList!=null && !tableList.isEmpty()) {
			console.writeOutput("Tables" + "\n");
			console.writeOutput("-------------------------------"+"\n");
			Set<String> orderedTable = new TreeSet<>(tableList);
			for (String table : orderedTable) {
				console.writeOutput(table + "\n");
			}

			console.writeOutput("\n");
			console.writeOutput("=>  " + tableList.toString()+"\n");
		}
		else{
			console.writeOutput(String.format("%n%s%n", "Table " + tableName + " is not configured in hbase cli!!!"));
		}
	}

	protected void printUsage(IOConsole console) {

		console.writeOutput(String.format("%nUsage:", new Object[0]));
		console.writeOutput(String.format("%n--------------------\n", new Object[0]));
		console.writeOutput(getDescriptor().getUsage() + "\n");
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
				return "Lists the configured HBase tables. List of tables can also be obtained by passing the regex in --table argument, using wildcard *";
			}

			public String getUsage() {

				return CMD_NAME + " [" + PARAM_TABLENAME + "=<TABLE_NAME>]";
			}

			public Map<String, String> getArguments() {
				Map<String, String> args = new LinkedHashMap<String, String>();
				args.put(PARAM_TABLENAME + "=<TABLE_NAME>", "List all configured tables matching the pattern. Supported wildcard is \"*\"\n"
						+ "\t Ex: list --table=test* , list --table=*test, list --table=*test*");
				return args;
			}
		});
	}
}
