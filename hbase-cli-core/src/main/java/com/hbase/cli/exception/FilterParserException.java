package com.hbase.cli.exception;

@SuppressWarnings("serial")
public class FilterParserException extends Exception {

	public FilterParserException(String message) {
		super(message);
	}
	
	public FilterParserException(Exception cause) {
		super(cause);
	}
	
	public FilterParserException(String message, Exception cause) {
		super(message, cause);
	}
}
