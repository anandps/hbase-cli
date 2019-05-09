package com.hbase.cli.exception;

/**
 * The custom exception for HBase cli.
 * 
 *
 */
public class CLIException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message = null;

	public CLIException() {
		super();
	}

	public CLIException(String message) {
		super(message);
		this.message = message;
	}
	
	public CLIException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	public CLIException(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
