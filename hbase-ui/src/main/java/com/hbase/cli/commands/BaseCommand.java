package com.hbase.cli.commands;

import java.util.Map;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.constants.ExceptionType;

/**
 * This is the Basic Implementation for all commands. 
 * All the commands will be executed via this BaseCommands
 * 
 */
public abstract class BaseCommand implements Command {

	protected static NotificationLogger LOGGER = NotificationLoggerFactory.getLogger();
	private static final String LOGGING_CLASS_NAME = BaseCommand.class.getName();
	private IOConsole console;
	
	public enum CommandHintType {
		single, multiple, none
	}

	protected CommandHintType myCommandType = CommandHintType.single;
	
	public CommandHintType getCommandHintType() {
		return myCommandType;
	}

	protected void printError(IOConsole c, String message) {
		c.writeOutput(String.format("%n%s%n", "Internal Error occured while executing the command."));
		c.writeOutput(String.format("%n%s%n", "Details : " + message));
	}

	protected void printUsage() {

		console.writeOutput(String.format("%nUsage:", new Object[0]));
		console.writeOutput(String.format("%n--------------------\n", new Object[0]));
		console.writeOutput(getDescriptor().getUsage() + "\n");
	}

	abstract public void plug(Context arg0);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(Context context) {
		Object obj = null;
		console = context.getIoConsole();
		try {
			Map<String, String> argsMap = (Map<String, String>) context.getValue(Context.KEY_COMMAND_LINE_ARGS);
			if (argsMap != null && argsMap.get(CLIConstants.ERROR_INDICATOR_IN_COMMAND_STRUCTURE) != null) {
				context.getIoConsole().writeOutput("Invalid argument(s)\n");
				context.getIoConsole().writeOutput(getDescriptor().getUsage() + "\n");
				return null;
			}
			obj = executeCommand(context);
		} catch (Exception e) {
			ExceptionType exceptionType = null;
			try {
				exceptionType = ExceptionType.valueOf(e.getClass().getSimpleName());

			} catch (IllegalArgumentException exception) {
				LOGGER.postNotification(LOGGING_CLASS_NAME, LogLevel.ERROR,
						e.getMessage(), e);
				e.printStackTrace();
				console.writeOutput(
						String.format("%n%s%n", "Internal error occurred!!! See the logs for more information !!!"));
			}
			if (exceptionType != null) {
				switch (exceptionType) {
				
				case InvalidTableException:
					console.writeOutput(String.format("%n%s%n",
							"Invalid Table :" + e.getMessage()));
					break;
					
				case CodecLoaderException:
					console.writeOutput(String.format("%n%s%n",
							"Problem while loading TableCodec implementation :" + e.getMessage()));
					break;

				case EncoderException:
					console.writeOutput(
							String.format("%n%s%n", "Problem while getting the encoded data, Reason :" + e.getMessage()));
					break;

				case DecoderException:
					console.writeOutput(
							String.format("%n%s%n", "Problem while getting the decoded data, Reason :" + e.getMessage()));
					break;

				case NoConfFoundException:
					console.writeOutput(String.format("%n%s%n", "No table configuration files found!!!"));
					break;

				case FilterParserException:
					console.writeOutput(
							String.format("%n%s%n", "Filter search failed!\n" + e.getMessage()));
					break;

				case CLIException:
					printUsage();
					console.writeOutput(String.format("%n%s%n", "Invalid Arguments, Reason : " + e.getMessage()));
					break;

				case InvalidArgException:
					printUsage();
					console.writeOutput(String.format("%n%s%n", "Invalid Arguments, Reason : " + e.getMessage()));
					break;
				
				case ValidationException:
					printUsage();
					console.writeOutput(String.format("%n%s%n", "Query Validation failed: " + e.getMessage()));
					break;

				default:
					break;
				}

			}

		}

		return obj;
	}

	abstract public Object executeCommand(Context arg0) throws Exception;

	abstract public Descriptor getDescriptor();

}
