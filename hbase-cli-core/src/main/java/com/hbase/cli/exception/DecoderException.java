package com.hbase.cli.exception;

public class DecoderException extends Exception {
	public DecoderException(String message) {
		super(message);
	}

	public DecoderException(String message, Exception cause) {
		super(message, cause);
	}
}

