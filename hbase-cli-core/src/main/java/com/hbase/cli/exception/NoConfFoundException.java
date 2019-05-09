package com.hbase.cli.exception;

public class NoConfFoundException extends Exception {
	public NoConfFoundException(String message) {
		super(message);
	}

	public NoConfFoundException(String message, Exception cause) {
		super(message, cause);
	}
}

