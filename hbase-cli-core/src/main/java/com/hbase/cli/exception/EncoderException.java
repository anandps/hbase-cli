package com.hbase.cli.exception;

public class EncoderException extends Exception {
	public EncoderException(String message) {
		super(message);
	}

	public EncoderException(String message, Exception cause) {
		super(message, cause);
	}
}

