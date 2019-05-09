package com.hbase.cli.shell;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.Shell;

import com.hbase.cli.handler.CoreHandler;

import java.io.File;
import org.clamshellcli.api.Configurator;

/**
 * The shell implementation for HBase CLI
 * 
 *
 */
public class HBaseCLIShell implements Shell {

	/*static {
		// Setting logging properties
		String logFile = System.getProperty(CLIConstants.LOG_FILE_NAME);
		if (logFile != null) {
			MDC.put(CLIConstants.LOG_FILE_NAME, logFile);
		}
	}*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plug(Context plug) {

		HBaseCliConsole console = ((HBaseCliConsole) plug.getIoConsole());

		if (console == null) {
			throw new RuntimeException(String.format(
					"%nUnable to find required IOConsole component in" + " plugins directory [%s]." + "Exiting...%n",
					((File) plug.getValue(Configurator.KEY_CONFIG_PLUGINSDIR)).getName()));
		}

		// Setting pre-requisites for handling hbase commands
		try {
			CoreHandler.loadRequisites();

			// Checking if all the tables provided in the configuration file
			// exists in the database
			CoreHandler.lookForInvalidTables();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			console.writeOutput(String.format("%n%s%n", "Internal error occurred!!! See the logs for more information !!!"));
		}
		// launch console
		console.plug(plug);
		console.setPaginationEnabled(true);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exec(Context arg0) {
		// TODO Auto-generated method stub

	}

}
