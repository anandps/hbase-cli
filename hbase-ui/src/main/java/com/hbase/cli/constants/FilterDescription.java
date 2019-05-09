package com.hbase.cli.constants;

public enum FilterDescription {

	all {
		public String toString() {
			return "\nPrefix filters"+ CLIConstants.LINE_SEPARATER
				    +"\nPrefixFilter \nColumnPrefixFilter \nMultipleColumnPrefixFilter \n\n"
					+ "Column based filters" + CLIConstants.LINE_SEPARATER
					+"\nSingleColumnValueFilter \nSingleColumnValueExcludeFilter  \nColumnRangeFilter \nColumnCountGetFilter \n\n"
					+"Other filters"+ CLIConstants.LINE_SEPARATER
					+"\nRowFilter \nFamilyFilter \nQualifierFilter \nValueFilter \nTimeStampsFilter \nInclusiveStopFilter \n\n";
		}
	},

	operators {

		public String toString() {
			return "\n   *<OPERATOR> accepts  =, <, <=, !=, >=, >";
		}
	},

	comparators {

		public String toString() {
			return "\n   *<COMPARATOR> accepts exact, startswith, contains, regex";
		}
	},
	
	prefixfilter {
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+" \n  This filter takes one argument - a prefix of a row key."
					+ "\n  It returns only those columns present in a row that starts with the specified row prefix.\ns"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ " \n   scan --table=<tablename> --filterquery=\"PrefixFilter('prefix_of_rowkey')\"\n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"PrefixFilter('row1')\" \n\n";

		}
	},

	columnprefixfilter {
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+" \n  This filter takes one argument - a column name prefix."
					+ "\n  It returns only those columns that starts with the specified column prefix.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ "	\n   scan --table=<tablename> --filterquery=\"ColumnPrefixFilter('prefix_of_column_qualifier')\"\n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"ColumnPrefixFilter('q1')\" \n\n";

		}
	},

	multiplecolumnprefixfilter {
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+" \n  This filter takes a list of column prefixes."
					+ "\n  It returns only those columns that starts with any of the specified column prefixes. \n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ " \n   scan --table=<tablename> --filterquery=\"MultipleColumnPrefixFilter('prefix_of_column_qualifier1','prefix_of_column_qualifier2',...)\"\n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"MultipleColumnPrefixFilter('q1','q2')\" \n\n";

		}
	},

	rowfilter {
		public String toString() {
			return  " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+ "\n  This filter compares each row key with the <COMPARATOR> using the <OPERATOR> and if the comparison returns true, it returns all the columns in that row.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ " \n   scan --table=<tablename> --filterquery=\"RowFilter(<OPERATOR>,'<COMPARATOR>:value')\"\n"
					+FilterDescription.operators.toString() + FilterDescription.comparators.toString()+"\n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"RowFilter(<= , 'startswith:row')\" \n\n";
		}
	},

	familyfilter {
		public String toString() {
			return  " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+"\n  This filter takes a <OPERATOR> and a <COMPARATOR>."
					+"\n  It compares each column family name with the <COMPARATOR> using the <OPERATOR> and if the comparison returns true, it returns all the columns in that column family.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ "\n   scan --table=<tablename> --filterquery=\"FamilyFilter(<OPERATOR>,'<COMPARATOR>:column_family_value')\"\n"
					+FilterDescription.operators.toString() + FilterDescription.comparators.toString()+"\n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"FamilyFilter(=, 'exact:f1')\" \n\n";
		}
	},

	qualifierfilter {
		public String toString() {
			return  " \nDescription : "
			        + CLIConstants.LINE_SEPARATER
					+" \n  This filter takes a <OPERATOR> and a <COMPARATOR>."
					+ "\n  It compares each qualifier name with the <COMPARATOR> using the <OPERATOR> and if the comparison returns true, it returns that column.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
				    + "\n   scan --table=<tablename> --filterquery=\"QualifierFilter(<OPERATOR>,'<COMPARATOR>:column_qualifier_name')\"\n"
				    +FilterDescription.operators.toString() + FilterDescription.comparators.toString()+"\n"
				    + " \n   Ex : scan --table=TEST --filterquery=\"QualifierFilter(<=,'startswith:q1')\" \n\n";
		}
	},

	valuefilter {
		public String toString() {
			return  " \nDescription : "
			        + CLIConstants.LINE_SEPARATER
					+" \n  This filter takes a <OPERATOR> and a <COMPARATOR>."
					+ "\n  It compares each value with the <COMPARATOR> using the <OPERATOR> and if the comparison returns true, it returns that column.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
				    + "\n   scan --table=<tablename> --filterquery=\"ValueFilter(<OPERATOR>,'<COMPARATOR>:column_value')\"\n"
				    +FilterDescription.operators.toString() + FilterDescription.comparators.toString()+"\n"
				    + " \n   Ex : scan --table=TEST --filterquery=\"ValueFilter(=,'exact:123')\" \n\n";

		}
	},

	singlecolumnvalueexcludefilter {
		public String toString() {
			return   " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
                    + "\n  If the specified column is found and the condition passes, all the columns of that row will be emitted except that specified column. \n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ " \n    scan --table=<tablename> --filterquery=\"SingleColumnValueExcludeFilter('family','qualifier',<OPERATOR>,'<COMPARATOR>:column_value')\""
					+ " \n    scan --table=<tablename> --filterquery=\"SingleColumnValueExcludeFilter('family','qualifier',<OPERATOR>,'<COMPARATOR>:column_value',filterifmissing,islatestversiononly)\"\n"
					+FilterDescription.operators.toString() + FilterDescription.comparators.toString()
					+ " \n   *filterifmissing - Default is false. set to true, to skip the rows which doesn't contains the specified column"
                    + " \n   *islatestversiononly - Default is true. set to false, to apply condition to previous versions as well \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"SingleColumnValueExcludeFilter('f1','q1',= ,'startswith:123')\" \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"SingleColumnValueExcludeFilter('f1','q1',= ,'startswith:123',true,false)\" \n\n";
		}
	},

	singlecolumnvaluefilter {
		public String toString() {
			return  " \nDescription : "
                    + CLIConstants.LINE_SEPARATER
					+ "\n  If the specified column is found and the condition passes, all the columns of that row will be emitted."
					+ "\n  If the specified column is not found, all the columns of that row will be emitted.(To avoid this, use \"filterifmissing\" flag) \n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ " \n  scan --table=<tablename> --filterquery=\"SingleColumnValueFilter('family','qualifier',<OPERATOR>,'<COMPARATOR>:column_value')\""
					+ " \n  scan --table=<tablename> --filterquery=\"SingleColumnValueFilter('family','qualifier',<OPERATOR>,'<COMPARATOR>:column_value',filterifmissing,islatestversiononly)\"\n"
					+FilterDescription.operators.toString() + FilterDescription.comparators.toString()
					+ " \n   *filterifmissing - Default is false. set to true, to skip the rows which doesn't contains the specified column"
                    + " \n   *islatestversiononly - Default is true. set to false, to apply condition to previous versions as well \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"SingleColumnValueFilter('f1','q1',= ,'startswith:123')\" \n\n";
		}
	},

	columnrangefilter {
		public String toString() {
			return  " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+"\n  To select rows with columns that are between minColumn and maxColumn."
					+"\n  To include mincolumn & maxcolumn in the result, set the flag to true after the respective variables.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
					+ "\n   scan --table=<tablename> --filterquery=\"ColumnRangeFilter('mincolumn',true,'maxcolumn',true)\" \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"ColumnRangeFilter('abc',true,'xyz',true)\" \n\n";
       }
	},

	inclusivestopfilter {
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+" \n  This filter takes one argument – a row key on which to stop scanning."
					+ "\n  It returns all the rows up to and including the specified row keys.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
				    + " \n    scan --table=<tablename> --filterquery=\"InclusiveStopFilter('row_key_value')\" \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"InclusiveStopFilter('row5')\" \n\n";

		}
	},
	
	timestampsfilter{
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+"\n  This filter takes a list of timestamps."
					+"\n  It returns those columns whose timestamps matches any of the specified timestamps.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
				    + " \n    scan --table=<tablename> --filterquery=\"TimestampsFilter(timestamp1,timestamp2,..)\" \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"TimestampsFilter(48895495, 58489845945)\" \n\n";

		}
	},
	
	columncountgetfilter{
		public String toString() {
			return " \nDescription : "
					+ CLIConstants.LINE_SEPARATER
					+"\n  This filter takes one argument - a LIMIT."
					+"\n  It returns the first 'LIMIT' number of columns in the table.\n"
					+ " \nUsage : "
					+ CLIConstants.LINE_SEPARATER
				    + "\n    scan --table=<tablename> --filterquery=\"ColumnCountGetFilter(1)\" \n"
					+ " \n   Ex : scan --table=TEST --filterquery=\"ColumnCountGetFilter(5)\" \n\n";

		}
	},
}