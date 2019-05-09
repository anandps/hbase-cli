package com.hbase.cli.exception;

public class InvalidTableException extends Exception {
	public InvalidTableException(String message) {
		super(message);
	}

	public InvalidTableException(String message, Exception cause) {
		super(message, cause);
	}
}

