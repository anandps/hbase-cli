package com.hbase.cli.exception;

public class CodecLoaderException extends Exception {
	public CodecLoaderException(String message) {
		super(message);
	}

	public CodecLoaderException(String message, Exception cause) {
		super(message, cause);
	}
}

