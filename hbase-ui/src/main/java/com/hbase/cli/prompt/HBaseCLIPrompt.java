package com.hbase.cli.prompt;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.Prompt;

/**
 * The Prompt for HBase CLI
 * 
 *
 */
public class HBaseCLIPrompt implements Prompt {

	private static final String PROMPT = "hbase> ";

	
	public String getValue(Context ctx) {
		 return PROMPT;
	}
	
	public void plug(Context plug) {
		// TODO Auto-generated method stub
		
	}


}
