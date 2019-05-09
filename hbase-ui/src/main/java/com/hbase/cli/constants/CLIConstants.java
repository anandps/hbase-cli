package com.hbase.cli.constants;

public interface CLIConstants {
	
	String LOG_FILE_NAME = "logFileName";
	String HBASE_CLI_NAME_SAPCE = "hbasecli";
	String PARAM_TABLENAME = "--table";
	String PARAM_START_ROWKEY = "--startrowkey";
	String PARAM_END_ROWKEY = "--endrowkey";
	String PARAM_COLUMN = "--columns";
	String PARAM_FILTER_QUERY = "--filterquery";
	String PARAM_FILTER = "--filter";
	String PARAM_LIMIT = "--limit";
	String PARAM_VERSION = "--version";
	String PARAM_DUMP = "--dump";
	String PARAM_ROW_KEY = "--rowkey";
	String COLUMN_PARAMS_REGEX = "^[A-Za-z0-9_,-:]*$";
	String ERROR_INDICATOR_IN_COMMAND_STRUCTURE = "ERROR_INDICATOR_IN_COMMAND_STRUCTURE";
	String TABLE_NAME_REGEX = "^[a-zA-Z_0-9-.*]*$";
	String REPEATED_OPTIONS = "REPEATED_OPTIONS";
	String MULTIPLE_COMMANDS = "MULTIPLE_COMMANDS";
	String ROW_KEY_REGEX = "^[A-Za-z0-9-_,.\\/:+*]*$";
	String DELETE_COMMAND = "delete";
	String DESCRIBE_COMMAND = "describe";
	String EXIT_COMMAND = "exit";
	String HELP_COMMAND = "help";
	String LIST_COMMAND = "list";
	String SCAN_COMMAND = "scan";
	String LINE_SEPARATER = "\n---------------------------";
	String EQUALS = "=";
	char[] allowedChars = {'y','n','Y','N'};
	
	//***Scan command constants
	String ROWKEY = "rowKey";
	String DOUBLE_QOUTES = "\"";
	String COMMA = ",";
	String TAB = "\t";
	String CONTINUE = "CONTINUE";
	String QUIT = "QUIT";
	int DUMP_FILE_LIMIT_IN_MB = 10;
	
	//***Regex***
	//Matches whitespaces
	String WHITESPACES = "\\s+";
	
	//Matches whitespaces near '='
	String SPACE_NEAR_EQUALS="\\s*=\\s*";
	
	//Splits the whole command line argument into array of arguments pattern --key="value" 
	String ARG_SPLIITER = "[^\\s]*\"(\\\\+\"|[^\"])*?\"|[^\\s]*|(\\\\\\s|[^\\s])+";
	
	//Matches control characters in the string
	String UNICODES = "\\p{Cc}";
}
