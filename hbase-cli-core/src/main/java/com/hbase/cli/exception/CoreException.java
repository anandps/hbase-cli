package com.hbase.cli.exception;

public class CoreException extends Exception {
	public CoreException(String message) {
		super(message);
	}

	public CoreException(String message, Exception cause) {
		super(message, cause);
	}
}

