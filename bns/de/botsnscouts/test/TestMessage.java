package de.botsnscouts.util;

import java.io.*;

/** Testklasse fuer Message.java
   */

public class TestMessage
{
    private static boolean verbose=true;
    
    public static void main(String[] args)  throws LanguageLoadException {
	String options=null;
        if (args.length==0){
            System.err.println("usage: java TestMessage <language> <foo>");
            System.exit(5);
        }
	try { 
            options= args[0]; 
        }
	catch (Exception e){
	    System.err.println("Optionsfehler:"+e.getMessage());
	    System.exit(1);
	}
	
	System.out.print("Message.setLanguage... ");
	Message.setLanguage(options.toLowerCase());
	System.out.println("OK");
	System.out.println("----------------------------------------------------");
	System.out.println("Tabelle... ");
	Message.printMessageTable();
	System.out.println("Tabelle... OK");
	System.out.println("----------------------------------------------------");
	String blafasel=args[1];
	int argsNo = args.length;
	System.out.println("Ausgabe mFirstOutMessage... ");
	System.out.println();
	System.out.println(Message.say("TestMessage","mFirstOutMessage",blafasel,argsNo));
	System.out.println();
	System.out.println("Ausgabe mFirstOutMessage... OK");
    }
}

    
