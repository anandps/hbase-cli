package com.hbase.cli.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The utility class to format the data to display into terminal.The table will be created as per width of the terminal.
 * The width of the columns are calculated by percentage. This will create a table and format the data as per the 
 * table and make it ready for display 
 * 
 *
 */
public class Display {

	private String[] scanTableColumns = { "Rowkey", "Columns", "value" };
	private String[] describeTableColumns = { "Column", "DataType", "Description", "IsFilterable" };
	private float[] scanTablePercentage = { 35.0f, 20.0f, 45.0f };
	private float[] describeTablePercentage = { 20.0f, 20.0f, 45.0f, 15.0f };

	private Map<String, Integer> tableColumns = new HashMap<String, Integer>();
	private String[] tableColumnNames = null;
	private float[] tableColumnLength = null;

	private int displayWidth;
	private int allocatedWidth;

	private String tableName;

	/**
	 * The parameterized constructor for Display
	 * 
	 * @param displayWidth the width of the display
	 * @param tableName the table name
	 */
	public Display(int displayWidth, String tableName) {
		this.displayWidth = displayWidth;
		// allocated width is 2 less than display width for clarity
		this.allocatedWidth = this.displayWidth - 2;
		this.tableName = tableName;
		calculateLengthForTable();
	}

	/**
	 * This is the implementation for creating Table Header
	 * @return returns the list of string
	 * @throws IOException
	 */
	public List<String> getTableHeader() throws IOException {
		List<String> listOfOutputString = new ArrayList<String>();
		listOfOutputString.add(getDelimiterLine());
		listOfOutputString.add(this.getFormattedTableData(tableColumnNames).get(1));
		listOfOutputString.add(getDelimiterLine());
		return listOfOutputString;
	}

	/**
	 * This is the implementation for Format the table data. That will form the table as per the width of the table
	 * and allocated percentage. If the column value length is more than the allocated width of the column, then that
	 * column value will be splitted to next line.
	 * 
	 * @param data the data to be formatted
	 * @return returns the list of string
	 */
	public List<String> getFormattedTableData(String[] data) {
		List<String> listOfOutputString = new LinkedList<String>();
		try {

			String formattedStringForSpace = "";
			for (int i = 0; i < tableColumnNames.length; i++) {
				formattedStringForSpace += String.format("%-" + (tableColumns.get(tableColumnNames[i])) + "s", "|");
			}
			formattedStringForSpace = formattedStringForSpace.substring(0, formattedStringForSpace.length() - 1) + "|";
			listOfOutputString.add(formattedStringForSpace);
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			for (int i = 0; i < data.length; i++) {
				List<String> formattedList = new LinkedList<String>();
				int dataLength = data[i].length();
				int dataOffset = 0;
				String formattedTmpData = "";

				int allocatedLength = tableColumns.get(tableColumnNames[i]);

				// -2 for front and back space
				if (i == data.length - 1) {
					allocatedLength = allocatedLength - 3;

				} else {
					allocatedLength = allocatedLength - 2;
				}

				if (dataLength > allocatedLength) {
					String tmpData;
					while (dataLength != 0) {
						if (dataLength > allocatedLength) {

							tmpData = data[i].substring(dataOffset, dataOffset + allocatedLength);
							dataLength = dataLength - allocatedLength;
							dataOffset = dataOffset + allocatedLength;

						} else {
							if (dataLength != 0) {
								dataLength = 0;
								tmpData = data[i].substring(dataOffset, data[i].length());
							} else {
								tmpData = "";
							}
						}

						formattedTmpData = String.format("%-" + (allocatedLength) + "s", tmpData) + " |";
						formattedList.add(formattedTmpData);
						map.put(tableColumnNames[i], formattedList);
					}
				} else {
					formattedTmpData = String.format("%-" + (allocatedLength) + "s", data[i]) + " |";
					formattedList.add(formattedTmpData);
					map.put(tableColumnNames[i], formattedList);
				}
			}

			int maxsize = getMaximumSize(map);
			paddedEmptySpaceIfNeed(map, maxsize);

			for (int i = 0; i < maxsize; i++) {
				String concatString = "";
				for (int j = 0; j < tableColumnNames.length; j++) {
					List<String> dataList = map.get(tableColumnNames[j]);
					String dataFromTmpList = dataList.get(i);
					concatString = concatString + dataFromTmpList;
				}
				concatString = "|" + concatString;
				listOfOutputString.add(concatString);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return listOfOutputString;
	}

	/**
	 * This is the implementation to create the table footer
	 * 
	 * @return returns the Table Footer
	 */
	public List<String> printTableFooter() {
		List<String> listOfOutputString = new ArrayList<String>();
		listOfOutputString.add(getDelimiterLine());
		return listOfOutputString;
	}

	/**
	 * This is the implementation to create a Row Key separator
	 * 
	 * @return returns the list of string
	 */
	public List<String> getRowKeySeparator() {
		List<String> listOfOutputString = new ArrayList<String>();
		listOfOutputString.add(getRowKeySeparatorLine());
		return listOfOutputString;
	}

	/**
	 * This is the implementation to create a Line breaker
	 * 
	 * @return returns the list of string
	 */
	public List<String> getLineBreaker() {
		List<String> listOfOutputString = new ArrayList<String>();
		listOfOutputString.add("\n");
		return listOfOutputString;
	}

	private void calculateLengthForTable() {
		int totalWidth = this.allocatedWidth;
		try {
			if (this.tableName.equals("scan")) {
				tableColumnNames = Arrays.copyOf(scanTableColumns, scanTableColumns.length);
				tableColumnLength = Arrays.copyOf(scanTablePercentage, scanTablePercentage.length);
			} else if (this.tableName.equals("describe")) {
				tableColumnNames = Arrays.copyOf(describeTableColumns, describeTableColumns.length);
				tableColumnLength = Arrays.copyOf(describeTablePercentage, describeTablePercentage.length);
			}

			int totalAllotedPercentage = 0;
			for (int i = 0; i < tableColumnNames.length; i++) {
				int columnPercentage = (int) (totalWidth * (tableColumnLength[i] / 100.0f));
				tableColumns.put(tableColumnNames[i], columnPercentage);
				totalAllotedPercentage = totalAllotedPercentage + columnPercentage;
			}
			int difference = totalWidth - totalAllotedPercentage;
			if (difference > 0) {
				tableColumns.put(tableColumnNames[(tableColumnNames.length - 1)],
						tableColumns.get(tableColumnNames[(tableColumnNames.length - 1)]) + difference);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private String getRowKeySeparatorLine() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("+");
		for (int i = 0; i < allocatedWidth - 2; i++) {
			stringBuffer.append("-");
		}
		stringBuffer.append("+");
		return stringBuffer.toString();
	}

	private String getDelimiterLine() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("+");
		for (int i = 0; i < allocatedWidth - 2; i++) {
			stringBuffer.append("=");
		}
		stringBuffer.append("+");
		return stringBuffer.toString();
	}

	private int getMaximumSize(Map<String, List<String>> map) {
		int size = 0;
		for (int i = 0; i < tableColumnNames.length; i++) {
			int listSize = map.get(tableColumnNames[i]).size();
			if (listSize > size) {
				size = listSize;
			}
		}
		return size;
	}

	private void paddedEmptySpaceIfNeed(Map<String, List<String>> map, int maxSize) {
		for (int i = 0; i < tableColumnNames.length; i++) {

			int allocatedLength = tableColumns.get(tableColumnNames[i]);
			if (i == tableColumnNames.length - 1) {
				allocatedLength = allocatedLength - 2;
			} else {
				allocatedLength = allocatedLength - 1;
			}
			int listSize = map.get(tableColumnNames[i]).size();
			if (maxSize > listSize) {
				int difference = maxSize - listSize;

				for (int j = 0; j < difference; j++) {

					String emptyFormat = String.format("%-" + (allocatedLength) + "s", "") + "|";
					map.get(tableColumnNames[i]).add(emptyFormat);
				}
			}
		}
	}
}
