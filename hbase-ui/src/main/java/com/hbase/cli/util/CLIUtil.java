package com.hbase.cli.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.hbase.cli.constants.CLIConstants;
import com.hbase.cli.exception.CLIException;

/**
 * The CLI Utility Implementation
 * 
 *
 */
public class CLIUtil {

	/**
	 * Member variables for file related operations
	 */
	private static File file = null;
	private static FileWriter fileWriter = null;
	private static BufferedWriter bufferedWriter = null;
	private static int fileCounter = 0;

	/**
	 * Dumps data to a file
	 * 
	 * @param listOfData
	 *            list of data to dump
	 * @param fileToDump
	 *            the file name where the data will dump
	 * @throws CLIException
	 */
	public static void dumpDataToFile(String header, List<String> listOfData, String fileToDump) throws CLIException {

		try {

			if (file == null) {
				file = new File(fileToDump);
				fileWriter = new FileWriter(file);
				bufferedWriter = new BufferedWriter(fileWriter);
				
				// *Dont remove sysout*
				// Sysout is used since, console writeoutput will print at last(when the thread goes to hbasecliconsole obj)
				System.out.println("Writing to file " + file.getAbsolutePath() + "...");
				
				bufferedWriter.write("Rowkey" + CLIConstants.COMMA + header + "\n");
			}

			String suffixFileName = "_";
			long maxFileSize = intToLong(convertMBtoBytes(CLIConstants.DUMP_FILE_LIMIT_IN_MB));
			for (String data : listOfData) {
				long dataLength = intToLong(data.getBytes().length);
				if ((file.length() + dataLength) > maxFileSize) {
					fileCounter++;
					// CloseWriters
					try {
						if (bufferedWriter != null) {
							bufferedWriter.close();
							bufferedWriter = null;
						}

						if (fileWriter != null) {
							fileWriter.close();
							fileWriter = null;
						}
					} catch (IOException ex) {
						throw new CLIException(ex.getMessage());
					}
					file = new File(getNewFileName(fileToDump, suffixFileName, fileCounter));
					fileWriter = new FileWriter(file);
					bufferedWriter = new BufferedWriter(fileWriter);
					bufferedWriter.write("Rowkey" + CLIConstants.COMMA + header + "\n");
					System.out.println("Writing to file " + file.getAbsolutePath() + "...");
				}

				bufferedWriter.write(data);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException ex) {
			throw new CLIException(ex.getMessage());
		}

	}

	public static void releaseResources() throws CLIException {

		// Resetting the counter for next run
		fileCounter = 0;

		// Releasing held releases
		try {
			if (bufferedWriter != null) {
				bufferedWriter.close();
				bufferedWriter = null;
			}

			if (fileWriter != null) {
				fileWriter.close();
				fileWriter = null;
				file = null;
			}
		} catch (IOException ex) {
			throw new CLIException(ex.getMessage());
		}
	}

	/**
	 * Gives the new file name by appending the current filecounter
	 * 
	 * @param fileToDump
	 * @param suffixFileName
	 * @param fileCounter
	 * @return
	 */
	private static String getNewFileName(String fileToDump, String suffixFileName, int fileCounter) {
		String newFileName = fileToDump + suffixFileName + fileCounter;
		if (fileToDump.contains(".")) {
			String fileSubString = fileToDump.substring(0, fileToDump.lastIndexOf("."));
			fileSubString = fileSubString + suffixFileName + fileCounter;
			String fileExtension = fileToDump.substring(fileToDump.lastIndexOf("."), fileToDump.length());
			newFileName = fileSubString + fileExtension;
		}
		return newFileName;
	}

	private static int convertMBtoBytes(int mb) {
		int bytes = mb * (1024 * 1024);
		return bytes;
	}

	private static long intToLong(int intValue) {
		return Integer.valueOf(intValue).longValue();
	}

	public static boolean isNull(Object data) {
		if (data == null) {
			return true;
		}

		return false;
	}

	public static boolean isNullOrEmpty(String data) {
		if (data == null || data.isEmpty()) {
			return true;
		}

		return false;
	}

	public static boolean isMapNullOrEmpty(Map<?, ?> data) {
		if (data == null || data.isEmpty()) {
			return true;
		}

		return false;
	}
}
