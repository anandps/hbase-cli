package com.hbase.cli.exception;

public class ValidationException extends Exception {
	public ValidationException(){
		super();
	}
	
	public ValidationException(String message){
		super(message);
	}
}
