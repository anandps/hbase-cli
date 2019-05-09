package com.hbase.cli.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.constants.FilterDescription;
import com.hbase.cli.validators.ShowFilterValidator;

public class ShowFilterCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = "showfilter";
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;

	private Command.Descriptor descriptor;
	private IOConsole console;

	public ShowFilterCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.multiple;
	}

	@Override
	public void plug(Context arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object executeCommand(Context ctx) throws Exception {

		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);
		console = ctx.getIoConsole();

		ShowFilterValidator.validateShowFilterCommand(argsMap);
		if (argsMap.get(PARAM_FILTER) == null || argsMap.get(PARAM_FILTER).isEmpty()) {
			console.writeOutput(FilterDescription.all.toString());
		} else {

			console.writeOutput(FilterDescription.valueOf(argsMap.get(PARAM_FILTER).toLowerCase()).toString());

		}

		return null;
	}

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
				return "Shows list of filters and its usage if filtername is given";
			}

			public String getUsage() {
				return CMD_NAME + " [" + PARAM_FILTER + "=<FILTER_NAME>]";
			}

			public Map<String, String> getArguments() {
				Map<String, String> args = new LinkedHashMap<String, String>();
				args.put(PARAM_FILTER + "=<FILTER_NAME>", "Prints the usage of given filter");
				return args;
			}
		});
	}

}
