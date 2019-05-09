package com.hbase.cli.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jline.CandidateListCompletionHandler;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.IOConsole;
import org.clamshellcli.api.InputController;
import org.clamshellcli.api.Prompt;
import org.clamshellcli.api.Shell;
import org.clamshellcli.api.SplashScreen;

import com.hbase.cli.constants.CLIConstants;

/**
 * This is the implementation of HBase CLI Console. 
 * That will create the console and make it available for entire commands.
 * 
 *
 */
public class HBaseCliConsole implements IOConsole {
	private Context context;
	private Configurator config;
	private Shell shell;
	private Prompt prompt;
	private ConsoleReader console;
	private List<InputController> controllers;
	private boolean controllersAreValid;
	private InputStream input;
	private OutputStream output;
	private Thread consoleThread;
	private Map<String, String[]> inputHints;

	static {
		// Setting logging properties
		String logFile = System.getProperty(CLIConstants.LOG_FILE_NAME);
		if (logFile != null) {
			MDC.put(CLIConstants.LOG_FILE_NAME, logFile);
		}
	}
	
	public InputStream getInputStream() {
		return this.input;
	}

	public String askMoreData() {
		System.out.print("The table has more data, do you want to continue to iterate (y) or (n)? : ");
		String status = "CONTINUE";
		try {

			int userOption = this.console.readCharacter(CLIConstants.allowedChars);
			if (userOption == 121 || userOption == 89) {
				status = "CONTINUE";
			} else if (userOption == 110 || userOption == 78){
				status = "QUIT";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}
	
	public void setPaginationEnabled(boolean isPaginationEnabled) {
		this.console.setUsePagination(isPaginationEnabled);
	}

	public void printCoulumnsWithPagination(Collection<?> data) throws IOException {
		this.console.printColumns(data);
	}

	public int getHeight() {
		return this.console.getTermheight();
	}

	public int getWidth() {
		return this.console.getTermwidth();
	}

	public OutputStream getOutputStream() {
		return this.output;
	}

	public void plug(Context plug) {
		this.context = plug;
		this.config = plug.getConfigurator();
		this.shell = plug.getShell();
		this.prompt = plug.getPrompt();
		this.input = ((this.input = (InputStream) this.context.getValue("key.InputStream")) != null ? this.input
				: System.in);
		this.output = ((this.output = (OutputStream) this.context.getValue("key.OutputStream")) != null ? this.output
				: System.out);
		this.inputHints = new HashMap();
		try {
			this.console = new ConsoleReader(this.input, new OutputStreamWriter(this.output));
		} catch (IOException ex) {
			throw new RuntimeException("Unable to initialize the console.  Program will stop now.", ex);
		}
		this.controllers = plug.getPluginsByType(InputController.class);
		if (this.controllers.size() > 0) {
			for (InputController ctrl : this.controllers) {
				ctrl.plug(plug);
			}
			aggregateExpectedInputs();
			this.console.setCompletionHandler(new CandidateListCompletionHandler());
		} else {
			throw new RuntimeException(
					"Unable to initialize Clamshell-Cli.  No InputController instances found on classpath. Exiting...");
		}
		List<SplashScreen> screens = plug.getPluginsByType(SplashScreen.class);
		if ((screens != null) && (screens.size() > 0)) {
			for (SplashScreen sc : screens) {
				sc.plug(plug);
				sc.render(plug);
			}
		}
		this.consoleThread = createConsoleThread();
		this.consoleThread.start();
	}

	public void writeOutput(String val) {
		try {
			this.console.printString(val);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to invoke print on console: ", ex);
		}
	}

	public String readInput(String prompt) {
		String result = null;
		try {
			result = this.console.readLine(prompt);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to read input: ", ex);
		}
		return result;
	}

	private Thread createConsoleThread() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (!Thread.interrupted()) {
					HBaseCliConsole.this.context.putValue("key.commandParams", null);

					boolean handled = false;
					String line = HBaseCliConsole.this
							.readInput(HBaseCliConsole.this.prompt.getValue(HBaseCliConsole.this.context));
					if ((line != null) && (!line.trim().isEmpty())) {
						HBaseCliConsole.this.context.putValue("key.commandlineInput", line);
						if (HBaseCliConsole.this.controllersExist()) {
							for (InputController controller : HBaseCliConsole.this.controllers) {
								Pattern pattern = controller.respondsTo();
								Boolean enabled = controller.isEnabled();
								if ((pattern != null) && (pattern.matcher(line).matches())
										&& (enabled.booleanValue())) {
									boolean ctrlResult = controller.handle(HBaseCliConsole.this.context);
									handled = (handled) || (ctrlResult);
								}
							}
							if (!handled) {
								HBaseCliConsole.this.writeOutput(String.format(
										"%nCommand unhandled. %nNo controller found to respond to [%s].%n%n",
										new Object[] { line }));
							}
						} else {
							HBaseCliConsole.this
									.writeOutput(String.format("Warning: no controllers(s) found.%n", new Object[0]));
						}
					}
				}
			}
		});
		return t;
	}

	private boolean controllersExist() {
		return (this.controllers != null) && (this.controllers.size() > 0);
	}

	private void aggregateExpectedInputs() {
		List<String> inputs = new ArrayList();
		for (InputController ctrl : this.controllers) {
			String[] expectedInputs = ctrl.getExpectedInputs();
			if (expectedInputs != null) {
				Collections.addAll(inputs, expectedInputs);
			}
		}
		this.console.addCompletor(new SimpleCompletor((String[]) inputs.toArray(new String[0])));
	}

}
