package com.hbase.cli.exception;

/**
 * The custom exception for invalid arguments. That will extend from CLIException
 * 
 *
 */
public class InvalidArgException extends CLIException {
private static final long serialVersionUID = 1L;
	
	public InvalidArgException(String message)
	{
		super(message);
	}
	
	public InvalidArgException(String message, Exception cause)
	{
		super(message,cause);
	}
	

}
