package com.hbase.cli.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;

import com.hbase.cli.api.TableCodec;
import com.hbase.cli.constants.ComparatorAlias;
import com.hbase.cli.constants.CoreConstants;
import com.hbase.cli.exception.FilterParserException;
import com.hbase.cli.utils.TablePropertiesContext;

/**
 * HBase CLI Filter Engine
 * 
 *
 */
public class FilterParser {

	// List to hold bytes of filter expression
	private static List<byte[]> byteexpression = new ArrayList<byte[]>();
	// it holds last index value of previous matched pattern
	private static int lastindex = 0;
	private static String filterexpression = "";

	/**
	 * Parses the given filter expression. 
	 * First checks whether the given filter expression is valid or not
	 * If not valid pass the expression to ParseFilter of Hbase API.
	 * If valid match the filter and extract it from filter expression by applying filter regex patterns
	 * Parse the filters appropriately
	 * Check whether column/row key value is encoded or not
	 * If encoded replace the user entered value with appropriate encoded value 
	 * Finally pass the expression to ParseFilter of Hbase 
	 */
	public static Filter parseFilterExpression(String expression, String tableName, TableCodec tableImpl,
			TablePropertiesContext props) throws Exception {

		filterexpression = expression;
		try {
			
			if (isSupportedFilter(filterexpression, CoreConstants.notsupportedpattern)) {
				
				
			  if (isValidexpr(filterexpression, CoreConstants.isvalidpattern)) {

				Pattern pattern = Pattern.compile(CoreConstants.filterpattern);
				Matcher matcher = pattern.matcher(filterexpression);

				while (matcher.find()) {

					byte[] encodedvalue;
					String comparator = "";
					String comparator_val = "";
					String matchedval = matcher.group();
					int endindex = matcher.end();

					String filter = matchedval.substring(0, matchedval.indexOf('(')).trim();

					String[] filtervalues = matcher.group(2).split(",(?=(?:[^\']*\'[^\']*\')*[^\']*$)", -1);
					String firstindex = filtervalues[0].trim();
					
					
					
					if (matchedval.matches("\\bSingleColumnValueFilter.*")
							|| matchedval.matches("\\bSingleColumnValueExcludeFilter.*")) {

						String secondindex = filtervalues[1].trim();
						
						if (filtervalues.length == 6) {
							String fifthindex = filtervalues[4].trim();
						    String sixthindex = filtervalues[5].trim();
							if (fifthindex.startsWith("'") || fifthindex.trim().endsWith("'")
									|| sixthindex.startsWith("'") || sixthindex.endsWith("'")) {
								
								throw new FilterParserException(
										CoreConstants.SCVF_PROBLEM + "Boolean expression must not be enclosed within single quotes");
						}
						}	
						if (filtervalues.length == 4 || filtervalues.length == 6) {
							if (firstindex.startsWith("'") && firstindex.trim().endsWith("'")
									&& secondindex.startsWith("'") && secondindex.endsWith("'")) {
								
								String thirdindex = filtervalues[3].trim();
					            String columnfamily = firstindex.substring(1, firstindex.length() - 1);
								String columnqualifier = secondindex.substring(1, secondindex.length() - 1);
								String column = columnfamily + CoreConstants.COLON + columnqualifier;

								isColumnExists(props, tableName, columnfamily, column);

								if (thirdindex.startsWith("'") && thirdindex.endsWith("'")) {
									String value = thirdindex.substring(1, thirdindex.length() - 1);
									if (!value.trim().isEmpty()) {

										String[] aliasval = value.split(":", 2);
										comparator = parsealiasname(aliasval[0].trim());
										comparator_val = comparator + ":";

					
										if (isColumnEncoded(props, tableName, columnfamily, column)) {
                                            
											if (value.trim().matches("\\bstartswith:.*") || value.trim().matches("\\bcontains:.*")
													|| value.trim().matches("\\bregex:.*") || value.trim().matches("\\bexact:.*")) {

												if (aliasval[0].trim().equalsIgnoreCase("startswith")
														|| aliasval[0].trim().equalsIgnoreCase("exact")) {

													if (!aliasval[1].trim().isEmpty()) {

														encodedvalue = tableImpl.getEncodedColumns(column, aliasval[1]);

														if (encodedvalue == null || encodedvalue.length == 0) {
															throw new EncoderException(
																	"Encoded column value is empty!!!");
														}

														convertToBytes(filterexpression, encodedvalue, comparator_val,
																aliasval, endindex, filter, filtervalues,matchedval);
													} else {
														throw new FilterParserException(CoreConstants.SCVF_PROBLEM
																+ " Provide value for comparartor!!!");
													}
												} else {
													throw new FilterParserException(
															CoreConstants.SCVF_PROBLEM + aliasval[0].trim()
																	+ " Operation not supported for encoded columns");
												}
											} else {
												throw new FilterParserException(
														CoreConstants.SCVF_PROBLEM + "Invalid comparartor!");
											}
										} else {

											if (!aliasval[1].trim().isEmpty()) {

												convertToBytes(filterexpression, Bytes.toBytes(aliasval[1]),
														comparator_val, aliasval, endindex, filter, filtervalues,matchedval);
											} else {
												throw new FilterParserException(CoreConstants.SCVF_PROBLEM
														+ " Provide value for comparartor!!!");
											}
										}
									} else {
										throw new FilterParserException(
												CoreConstants.SCVF_PROBLEM + "Empty comparartor!");
									}
								}

								else {
									throw new FilterParserException(CoreConstants.SCVF_PROBLEM
											+ "Comparator must be enclosed in single quotes");
								}
							} else {
								throw new FilterParserException(CoreConstants.SCVF_PROBLEM
										+ "Column Family and Column qualifier Name must be enclosed in single bracket");
							}
						} else {

							throw new FilterParserException(CoreConstants.SCVF_PROBLEM + "Invalid no of arguments");
						}
					} else if (matchedval.matches("\\bRowFilter.*")) {

						if (filtervalues.length == 2) {

							String secondindex = filtervalues[1].trim();
							if (secondindex.startsWith("'") && secondindex.endsWith("'")) {
								String value = secondindex.substring(1, secondindex.length() - 1);
								if (!value.trim().isEmpty()) {

									String[] aliasval = value.split(":", 2);
									comparator = parsealiasname(aliasval[0].trim());
									comparator_val = comparator + ":";

									if (props.isRowkeyEncoded(tableName)) {
										
										if (aliasval[0].trim().matches("\\bstartswith") || aliasval[0].trim().matches("\\bcontains")
												|| aliasval[0].trim().matches("\\bregex") || aliasval[0].trim().matches("\\bexact")) {

											if (aliasval[0].trim().equalsIgnoreCase("startswith")
													|| aliasval[0].trim().equalsIgnoreCase("exact")) {

												encodedvalue = tableImpl.getEncodedRowkey(aliasval[1]);
												if (encodedvalue == null || encodedvalue.length == 0) {
													throw new EncoderException("Encoded rowkey value is empty!!!");
												}
												convertToBytes(filterexpression, encodedvalue, comparator_val, aliasval,
														endindex, filter, filtervalues,matchedval);

											} else {
												throw new FilterParserException(CoreConstants.RF_PROBLEM + aliasval[0]
														+ " Operation not supported for encoded rowkey");
											}
										} else {
											throw new FilterParserException(
													CoreConstants.RF_PROBLEM + "Invalid comparartor!");
										}

									} else {
										convertToBytes(filterexpression, Bytes.toBytes(aliasval[1]), comparator_val,
												aliasval, endindex, filter, filtervalues,matchedval);
									}
								} else {
									throw new FilterParserException(
											CoreConstants.RF_PROBLEM + "Empty Comparartor in rowfilter!!!");
								}
							} else {
								throw new FilterParserException(
										CoreConstants.RF_PROBLEM + "Comparator must be enclosed in single quotes");
							}
						} else {
							throw new FilterParserException(CoreConstants.RF_PROBLEM + "Invalid no of arguments");
						}
					} else if (matchedval.matches("\\bPrefixFilter.*")) {

						if (filtervalues.length == 1) {

							if (firstindex.startsWith("'") && firstindex.endsWith("'")) {
								String value = firstindex.substring(1, firstindex.length() - 1);
								if (!value.trim().isEmpty()) {
								
									if (props.isRowkeyEncoded(tableName)) {

										encodedvalue = tableImpl.getEncodedRowkey(value);
										if (encodedvalue == null || encodedvalue.length == 0) {
											throw new EncoderException(
													CoreConstants.PF_PROBLEM + "Encoded rowkey value is empty!!!");
										}
										
										convertToBytes(filterexpression, encodedvalue, comparator_val,
												new String[] { value }, endindex, filter, filtervalues,matchedval);

									} else {
									
										convertToBytes(filterexpression, Bytes.toBytes(value), comparator_val,
												new String[] { value }, endindex, filter, filtervalues,matchedval);

									}
								} else {
									throw new FilterParserException(CoreConstants.PF_PROBLEM + "Empty prefix!!!");
								}
							} else {
								throw new FilterParserException(
										CoreConstants.PF_PROBLEM + " Rowkey prefix must be enclosed in single quotes");
							}
						} else {
							throw new FilterParserException(CoreConstants.PF_PROBLEM + "Invalid no of arguments");
						}
					}

					else if ((matchedval.matches("\\bQualifierFilter.*") || matchedval.matches("\\bFamilyFilter.*")
							|| matchedval.matches("\\bValueFilter.*"))) {

						if (filtervalues.length == 2) {

							String secondindex = filtervalues[1].trim();
							if (secondindex.startsWith("'") && secondindex.endsWith("'")) {
								String value = secondindex.substring(1, secondindex.length() - 1);
								if (!value.trim().isEmpty()) {
									String[] aliasval = value.split(":", 2);
									comparator = parsealiasname(aliasval[0].trim());
									comparator_val = comparator + ":";

									convertToBytes(filterexpression, Bytes.toBytes(aliasval[1]), comparator_val,
											aliasval, endindex, filter, filtervalues,matchedval);
								} else {
									throw new FilterParserException("Problem in "+filter  + " : Empty Comparator");
								}
							} else {
								throw new FilterParserException("Problem in "+filter  + " : Comparator must be enclosed in single quotes");
							}
						} else {
							throw new FilterParserException("Problem in "+filter  + " : Invalid no of arguments");
						}
					} else {
						throw new FilterParserException("Incorrect Filter String : " + expression);
					}
				}
			}
			}
			else
			{
				throw new FilterParserException(" Expression contains Unsupported Filters" + expression);
			}
			if (filterexpression.length() > 1) {
				byteexpression.add(Bytes.toBytes(filterexpression));
			}

			return filterParser(concatbyte(byteexpression));
		} catch (Exception e) {
			 
			throw new FilterParserException("Invalid filter query provided, Reason : " + e.getMessage());
		}
	}

	/**
	 * Pattern to match the list of filters which needs to be processed True -
	 * It will be further processed and converted to bytes False - Converted to
	 * Bytes and Passed to ParseFilter of Hbase
	 */

	public static boolean isValidexpr(String expr, String regexpattern) throws FilterParserException {

		return expr.matches(regexpattern);
	}

	/**
	 * Pattern to match unsupported filters True - Exception will be thrown
	 * False - Expression will be further processed
	 */

	public static boolean isSupportedFilter(String expr, String regexpattern) throws FilterParserException {

		return !expr.matches(regexpattern);
	}

	/**
	 * Checking whether the given column exists in the table
	 */
	public static void isColumnExists(TablePropertiesContext props, String tableName, String familyName, String column)
			throws Exception {
		if (!props.getColumnList(tableName, familyName).contains(column)) {
			throw new FilterParserException("Invalid Column " + column);
		}

		if (!props.getQualifier(tableName, familyName, column).isFilterable()) {
			throw new FilterParserException(
					"Provided column cannot be used in filter search. The list of columns which has only filterable=true can be used in the filterquery!!!");
		}
	}

	/**
	 * Getting the appropriate datatype of the given column qualifier
	 */

	public static boolean isColumnEncoded(TablePropertiesContext props, String tableName, String familyName,
			String column) {

		if (props.getQualifier(tableName, familyName, column).getType().equals(CoreConstants.CUSTOM)) {
			return true;
		}
		return false;

	}

	/**
	 * Passing the filter expression as bytes to ParseFilter Method of Hbase
	 */
	public static Filter filterParser(byte[] expr) throws CharacterCodingException {
		org.apache.hadoop.hbase.filter.ParseFilter parser = new org.apache.hadoop.hbase.filter.ParseFilter();
		Filter hbasefilter = parser.parseFilterString(expr);
		return hbasefilter;

	}

	/**
	 * Releasing held resources
	 */
	public static void releaseResources() {

		byteexpression = new ArrayList<byte[]>();
		lastindex = 0;
		filterexpression = "";
	}

	/**
	 * Alias names used for hbase comparators. exact - binary startswith -
	 * binaryprefix contains - substring regex - regexstring Replacing alias
	 * names with appropriate hbase comparator name in the filter expression
	 */
	public static String parsealiasname(String alias) throws Exception {

		String hbasealias = "";

		try {
			hbasealias = ComparatorAlias.valueOf(alias.toLowerCase()).toString();
		} catch (Exception e) {

			throw new FilterParserException(" Invalid Comparator : " + alias);
		}

		return hbasealias;

	}

	/**
	 * Conversion of list of byte array which holds filter expression to single
	 * byte array
	 * 
	 */
	public static byte[] concatbyte(List<byte[]> byteexpression) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte[] content : byteexpression) {
			baos.write(content);
		}
		baos.flush();
		return baos.toByteArray();

	}

	/**
	 * Converts the matched pattern to bytes and added to a list. * matched
	 * patterns are parsed and added into the list in the following order * Add
	 * bytes of array from 0th index of filter expression to index before
	 * comparator name * Except Prefix filter Add hbase comparator which is
	 * converted from given comparator alias and add colon with that * Add
	 * comparatorvalue/value (encoded or not) Removes the matched patterns upto
	 * which the values are added into the list and updates the expression
	 * Updates the last index value to be the last index of matched pattern upto
	 * which the values are added into the list
	 */
	public static void convertToBytes(String expression, byte[] value, String comparator, String[] aliasval,
			int endindex, String filter, String[] filtervalues, String matchedexpr) {

		int start = 0;
		int lastindexexpr = matchedexpr.lastIndexOf(")") - matchedexpr.lastIndexOf("'") - 1;

		if (filter.equalsIgnoreCase("prefixfilter")) {
			start = endindex - (aliasval[0].length() + 2 + lastindexexpr) - lastindex;
			filterexpression = expression.substring(endindex - lastindex - 2 - lastindexexpr, expression.length());
			lastindex = endindex - 2 - lastindexexpr;
			byteexpression.add(Bytes.toBytes(expression.substring(0, start)));
			byteexpression.add(value);
		}

		else {

			start = endindex - (aliasval[0].length() + (3 + lastindexexpr) + aliasval[1].length()) - lastindex;
			filterexpression = expression.substring(endindex - lastindex - 2 - lastindexexpr, expression.length());
			lastindex = endindex - 2 - lastindexexpr;
			byteexpression.add(Bytes.toBytes(expression.substring(0, start)));
			byteexpression.add(Bytes.toBytes(comparator));
			byteexpression.add(value);

		}

	}

}
