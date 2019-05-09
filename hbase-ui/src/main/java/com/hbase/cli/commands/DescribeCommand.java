package com.hbase.cli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.handler.CoreHandler;
import com.hbase.cli.schemaobjects.Qualifier;
import com.hbase.cli.shell.HBaseCliConsole;
import com.hbase.cli.util.Display;
import com.hbase.cli.validators.DescribeValidator;

/**
 * This is the implementation for Describe Command. That will describe about the tables
 * The description includes Row Key details, Column Qualifiers name,  descriptions and it's data type, Whether the particular column
 * is filterable and so on. 
 *  
 *
 */
public class DescribeCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = DESCRIBE_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;

	HBaseCliConsole console;
	private Command.Descriptor descriptor;

	public DescribeCommand() {
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

		console = (HBaseCliConsole) ctx.getIoConsole();
		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);

		DescribeValidator.validateDescribeCommand(argsMap);
		List<Object> output = CoreHandler.handleDescribe(argsMap.get(PARAM_TABLENAME));
		if (!output.isEmpty()) {
			printDescribeData(output);
		} else{
			console.writeOutput("Description not found for the table!!!"+"\n");
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
				return "Get the description of Hbase table";
			}

			public String getUsage() {

				return CMD_NAME + " " + PARAM_TABLENAME + "=<TABLE_NAME>";
			}

			public Map<String, String> getArguments() {
				Map<String, String> args = new LinkedHashMap<String, String>();
				args.put(PARAM_TABLENAME + "=<TABLE_NAME>", "Name of the table to be described");
				// More arguments goes here.
				return args;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void printDescribeData(List<Object> data) throws IOException {
		int width = console.getWidth();
		Display display = new Display(width, "describe");

		String tableDescription = String.valueOf(data.get(0));
		String rowDescription = String.valueOf(data.get(1));
		Map<String, Qualifier> columnDescription = (Map<String, Qualifier>) data.get(2);
		List<String> listOfOutputString = new ArrayList<String>();
		listOfOutputString.add("\n" + "Description:");
		listOfOutputString.add("-------------------------");
		listOfOutputString.add(tableDescription);
		listOfOutputString.addAll(display.getLineBreaker());
		listOfOutputString.add("\n" + "Rowkey Description:");
		listOfOutputString.add("-------------------------");
		listOfOutputString.add(rowDescription);
		listOfOutputString.addAll(display.getLineBreaker());
		listOfOutputString.addAll(display.getTableHeader());
		for (Entry<String, Qualifier> descriptorEntry : columnDescription.entrySet()) {
			Qualifier columnDescriptor = descriptorEntry.getValue();
			listOfOutputString.addAll(
					display.getFormattedTableData(new String[] { descriptorEntry.getKey(), columnDescriptor.getType(),
							columnDescriptor.getDescription(), (String.valueOf(columnDescriptor.isFilterable())) }));
		}
		listOfOutputString.addAll(display.printTableFooter());

		print(listOfOutputString, false);
	}

	private void print(List<String> listOfString, boolean enablePagination) throws IOException {
		if (enablePagination) {
			console.printCoulumnsWithPagination(listOfString);
		} else {
			for (String data : listOfString) {
				// console.writeOutput("\n");
				console.writeOutput(data);
				console.writeOutput("\n");
			}
		}
	}
}
