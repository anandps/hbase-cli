package com.hbase.cli.splashscreen;

import java.io.OutputStream;
import java.io.PrintStream;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.SplashScreen;

/**
 * The Splash screen Implementation for HBase CLI 
 * 
 *
 */
public class HBaseCLISplashScreen implements SplashScreen {

	private static StringBuilder screen;
	 
    static{
        screen = new StringBuilder();
        screen
            .append(String.format("%n"))  
            .append(String.format("\t\t\t\t\t\t\t\t\t"))
            .append("").append(String.format("%n"))
        .append("_|      _|   _|_|_|_|         _|_|_        _|_|_|_|_|   _|_|_|_|_|     _|_|_|_|   _|        _|_|_|_|").append(String.format("%n"))
        .append("_|      _|   _|      _|      _|   _|      _|            _|            _|          _|            _|").append(String.format("%n"))
        .append("_|      _|   _|      _|     _|     _|     _|            _|           _|           _|            _|" ).append(String.format("%n"))
        .append("_|_|_|_|_|   _|_|_|_|      _|       _|      |_|_|_|_    _|_|_|_|_|   _|           _|            _|").append(String.format("%n"))
        .append("_|      _|   _|      _|   _|_|_|_|_| _|            _|   _|           _|           _|            _|").append(String.format("%n"))
        .append("_|      _|   _|      _|  _|           _|           _|   _|            _|          _|     |      _|").append(String.format("%n"))
        .append("_|      _|   _|_|_|_|   _|             _|  _|_|_|_|     _|_|_|_|_|     _|_|_|_|   _|_|_|_|  _|_|_|_|").append(String.format("%n"))               
        .append(String.format("%n"));

    }


	public void render(Context ctx) {
		 
		 PrintStream out = new PrintStream ((OutputStream)ctx.getValue(Context.KEY_OUTPUT_STREAM));
	     out.println(screen);
		
	}

	public void plug(Context arg0) {
		// TODO Auto-generated method stub
		
	}

}
