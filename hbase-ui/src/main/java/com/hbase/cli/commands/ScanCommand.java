package com.hbase.cli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.clamshellcli.api.Command;
import org.clamshellcli.api.Context;

import com.utils.blu.datastructures.DataMap;
import com.utils.blu.datastructures.DataStore;
import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.CLIException;
import com.hbase.cli.handler.CoreHandler;
import com.hbase.cli.shell.HBaseCliConsole;
import com.hbase.cli.util.CLIUtil;
import com.hbase.cli.util.Display;
import com.hbase.cli.validators.ScanValidator;

/**
 * This is the implementation for Scan command. That will scan the hbase tables
 * as per the given arguments and prints the extracted data to terminal
 * 
 *
 */
public class ScanCommand extends BaseCommand implements CLIConstants {

	private static final String CMD_NAME = SCAN_COMMAND;
	private static final String NAMESPACE = HBASE_CLI_NAME_SAPCE;

	private Command.Descriptor descriptor;
	Display display;

	public ScanCommand() {
		// TODO Auto-generated constructor stub
		this.myCommandType = CommandHintType.multiple;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object executeCommand(Context ctx) throws Exception {

		int version;
		Set<String> columns = null;
		HBaseCliConsole console = (HBaseCliConsole) ctx.getIoConsole();
		Map<String, String> argsMap = (Map<String, String>) ctx.getValue(Context.KEY_COMMAND_LINE_ARGS);
		String fileToDump = argsMap.get(PARAM_DUMP);

		ScanValidator.validateScanCommand(argsMap);
		if (argsMap.get(PARAM_VERSION) != null) {
			version = Integer.parseInt(argsMap.get(PARAM_VERSION));
		} else
			version = 1;

		DataStore datastore = CoreHandler.handleScan(argsMap.get(PARAM_TABLENAME), argsMap.get(PARAM_START_ROWKEY),
				argsMap.get(PARAM_END_ROWKEY), argsMap.get(PARAM_COLUMN), argsMap.get(PARAM_FILTER_QUERY), version);

		if (!datastore.getDataMaps().isEmpty()) {

			// Dumps data to file
			if (fileToDump != null) {
				columns = CoreHandler.getColumnListForDumpFile(argsMap.get(PARAM_TABLENAME));
				dumpData(columns, datastore, fileToDump);

				// Checks for more data in the datastore & flushes to the
				// file(Optimization to occupy less memory at a time)
				while (!(datastore = CoreHandler.getDataStore()).getDataMaps().isEmpty()) {
					dumpData(columns, datastore, fileToDump);
				}

				// Releasing file resources from memory
				CLIUtil.releaseResources();
				console.writeOutput(String.format("%n%s%n",
						"Data Dump completed sucessfully. The generated content will be in CSV format!!!"));
			} else {

				// Prints data in the console
				printScanedData(console, version, datastore);

				// More data will be encoded/decoded based on the user's need
				while (true) {
					datastore = CoreHandler.getDataStore();
					if (!datastore.getDataMaps().isEmpty()) {
						String userOption = console.askMoreData();
						if (userOption.equals(CONTINUE)) {
							// datastore.toString());
							console.writeOutput("\n");
							printScanedData(console, version, datastore);
						} else if (userOption.equals(QUIT)) {
							console.writeOutput("\n");
							break;
						}
					} else {
						break;
					}
				}
			}

		} else {
			console.writeOutput("\n" + "No records found for the given scan query!!!" + "\n");
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
				return "Scan the Hbase tables";
			}

			public String getUsage() {

				return CMD_NAME + " " + PARAM_TABLENAME + "=<TABLE_NAME>" + " " + "[ " + PARAM_START_ROWKEY
						+ "=<START_ROW_KEY>" + " " + PARAM_END_ROWKEY + "=<END_ROW_KEY>" + " " + PARAM_COLUMN
						+ "=<COLUMNS>" + " " + PARAM_FILTER_QUERY + "=<FILTERQUERY>" + " " + PARAM_VERSION
						+ "=<VERSION>" + " " + PARAM_DUMP + "=<DUMP_FILE_NAME_WITH_PATH>" + " ]";
			}

			public Map<String, String> getArguments() {
				Map<String, String> args = new LinkedHashMap<String, String>();
				args.put(PARAM_TABLENAME + "=<TABLE_NAME>", "Name of the table to be scanned");
				args.put(PARAM_START_ROWKEY + "=<START_ROW_KEY>",
						"Rowkey to start the scan." + " Returns all the rows that contains the given startkey");
				args.put(PARAM_END_ROWKEY + "=<END_ROW_KEY>",
						"Boundary condition to stop the scan." + " Return all the rows between startKey & endKey, excluding endKey");
				args.put(PARAM_COLUMN + "=<COLUMNS>",
						"List of columns to be fetched from hbase. " + "Accepts list of columns seperated by comma. \n"
								+ "\t If qualifier has to be mentioned it should be given"
								+ " in the format \"ColumnFamily:Columnqualifier\", \n"
								+ "\t with the family & qualifier seperated by delimiter \":\". \n"
								+ "\t Ex : --columns = family1:qualifier1, family1:qualifier2");

				args.put(PARAM_FILTER_QUERY + "=<FILTERQUERY>",
						"List of filter queries to fine tune the search. \n"
								+ "\t Two or more filtertypes can be combined together using logical operators"
								+ " to form a query. \n"
								+ "\t Ex : --filterQuery=\"RowFilter(=, 'startswith:row') AND FamilyFilter(=, 'exact:family1')\" \n"
								+ "\t Type \"help showfilter\" to get more information on the supported filters.");
				args.put(PARAM_VERSION + "=<VERSION>",
						"Number of versions of data to be returned.\n\t Incase of multiple versions, the latest value will be listed first highlighted with the text (latest).");
				args.put(PARAM_DUMP + "=<DUMP_FILE_NAME_WITH_PATH>",
						"Writes the scanned output to the file mentioned in this field. \n"
								+ "\t (Make sure the user has write permission for the mentioned path.)");

				// More arguments goes here.
				return args;
			}
		});
	}

	/**
	 * Prints data in the console
	 * 
	 * @param console
	 * @param version
	 * @param datastore
	 * @throws CLIException
	 */
	@SuppressWarnings("unchecked")
	private void printScanedData(HBaseCliConsole console, int version, DataStore datastore) throws CLIException {
		try {
			int width = console.getWidth();
			display = new Display(width, "scan");
			List<String> listOfOutputString = new ArrayList<String>();
			List<DataMap> dataMapList = datastore.getDataMaps();

			listOfOutputString.addAll(display.getTableHeader());
			for (int i = 0; i < dataMapList.size(); i++) {
				String rowKey = String.valueOf(dataMapList.get(i).get(ROWKEY));
				for (Map.Entry<Object, Object> resutSet : dataMapList.get(i).entrySet()) {
					String key = (String) resutSet.getKey();
					if (key.equals(ROWKEY)) {
						continue;
					}

					String column = key;
					if (version > 1) {
						List<Object> valueList = (List<Object>) resutSet.getValue();
						for (int j = 0; j < valueList.size(); j++) {

							if (j == 0) {
								column = column + " " + "(Latest)";
							} else {
								column = key;
							}

							listOfOutputString.addAll(display.getFormattedTableData(new String[] { rowKey, column,
									String.valueOf(valueList.get(j)).replaceAll(UNICODES, "") }));
							rowKey = "";
						}

					} else {
						listOfOutputString.addAll(display.getFormattedTableData(new String[] { rowKey, column,
								String.valueOf(resutSet.getValue()).replaceAll(UNICODES, "") }));
						rowKey = "";
					}
				}
			}

			listOfOutputString.addAll(display.printTableFooter());
			print(console, listOfOutputString, true);
		} catch (IOException ex) {
			throw new CLIException(ex.getMessage());
		}
	}

	/**
	 * Arranges the data columnwise & dumps to the file in csv format
	 * 
	 * @param columnSet
	 * @param datastore
	 * @param fileToDump
	 * @throws CLIException
	 */
	private void dumpData(Set<String> columnSet, DataStore datastore, String fileToDump) throws CLIException {
		List<String> listOfOutputString = new ArrayList<String>();
		List<DataMap> dataMapList = datastore.getDataMaps();
		Map<String, String> sortedMap = new TreeMap<>();
		String columns = new TreeSet<>(columnSet).toString();
		String header = columns.substring(1, columns.length() - 1).replaceAll(WHITESPACES, "");
		for (int i = 0; i < dataMapList.size(); i++) {
			// Forming a sorted map
			for (String column : columnSet) {
				sortedMap.put(column, "");
			}
			String completeRow = DOUBLE_QOUTES + String.valueOf(dataMapList.get(i).get(ROWKEY)) + DOUBLE_QOUTES;
			for (Map.Entry<Object, Object> resutSet : dataMapList.get(i).entrySet()) {
				String key = (String) resutSet.getKey();
				if (key.equals(ROWKEY)) {
					continue;
				}

				sortedMap.put(key, String.valueOf(resutSet.getValue()).replaceAll(UNICODES, ""));
			}

			// Forming one row
			for (String x : sortedMap.values()) {
				completeRow += COMMA + DOUBLE_QOUTES + x + DOUBLE_QOUTES;
			}
			listOfOutputString.add(completeRow);
		}

		CLIUtil.dumpDataToFile(header, listOfOutputString, fileToDump);
	}

	private static void print(HBaseCliConsole console, List<String> listOfString, boolean enablePagination)
			throws IOException {
		if (enablePagination) {
			console.printCoulumnsWithPagination(listOfString);
		} else {
			for (String data : listOfString) {
				console.writeOutput(data);
				console.writeOutput("\n");
			}
		}
	}

	@Override
	public void plug(Context arg0) {
		// TODO Auto-generated method stub

	}
}
