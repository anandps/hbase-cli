package com.hbase.cli.constants;

public interface CoreConstants {
	int RECORD_BUFFER = 200;
	
	//Regex for fields in the configuration
	String tableRegex = "^[a-zA-Z_0-9-.*]*$";
	String classNameRegex = "[a-zA-Z0-9.-]+";
	String classpathRegex = "[a-zA-Z0-9:.*-\\/]+";
	String familyRegex = "[^:]+";
	String ENDS_WITH_JSON = ".json";
	String ROWKEY = "rowKey";
	String REFER_TEMPLATE = "\n"+"Please refer the template file json.template under conf/ folder for the list of compatible fields!!!";
	
	String CUSTOM = "custom";
	String HBASE_CONFIG_DIR_PROP = "hbase.config.dir";
	String ENDS_WITH_JAR = ".jar";
	
	//Character literals
	char BACKSLASH = '/';
	String COMMA = ",";
	String COLON = ":";
	String ASTERISK_STRING = "*";
	char ASTERISK = '*';
	
	//FilterParser Constants
	//pattern to match the filters and its value in the brackets
		String filterpattern ="\\b(SingleColumnValue|SingleColumnValueExclude|Row|Qualifier|Value|Family|Prefix)Filter\\s*\\((.*?)\\)";
		String isvalidpattern = ".*SingleColumnValueFilter\\s*\\((.*?)\\).*|.*SingleColumnValueExcludeFilter\\s*\\((.*?)\\).*|.*RowFilter\\s*\\((.*?)\\).*|.*QualifierFilter\\s*\\((.*?)\\).*|.*ValueFilter\\s*\\((.*?)\\).*|.*FamilyFilter\\s*\\((.*?)\\).*|.*PrefixFilter\\s*\\((.*?)\\).*";
		String notsupportedpattern = ".*KeyOnlyFilter\\s*\\((.*?)\\).*|.*FirstKeyOnlyFilter\\s*\\((.*?)\\).*";
		String SCVF_PROBLEM = "Problem in SingleColumnValueFilter : ";
		String RF_PROBLEM = "Problem in RowFilter : ";
		String PF_PROBLEM = "Problem in PrefixFilter : ";
}
