package de.botsnscouts.start;

import java.io.*; 
import java.net.*; 
 
/** Startprogramm fuer die dumme Ausgabe, die ansonsten als Thread vom
 * Startspieler gestartet wird, falls man sie mal von der Kommandozeile 
 * starten will.
 * @author Miriam
 */ 
public class AusgabeDummProgramm { 
/** 
 * @param argv java.lang.String[] 
 * <p> 
 * main erwartet 2 Argumente: 
 * <pre> 
 *    1. den Namen (oder die IP-Adresse) des Servers 
 *    2. (optional) eine Portnummer, auf der Server läuft 
 *		 falls keine angegeben wird, wird 4712 verwendet. 
 * </pre> 
 */ 
 
public static void main(String[] argv) throws Exception{ 
		 
		// Initialisierung von Servername und Portnummer 
		int port = 4712; 
		String servername = null; 
		 
		try{ 
			servername = argv[0]; 
			 
			if(argv.length > 1) 
				port = Integer.parseInt(argv[1]); 
		}catch(Exception e) { 
			System.out.println("Aufruf: java AusgabeDummProgramm <servername> [<port>]"); 
			System.exit(0); 
		}

		Thread t = new AusgabeDumm(servername, port);
		t.start();
}
}
