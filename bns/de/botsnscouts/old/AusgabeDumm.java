package de.spline.rr;

import java.util.*;  
import java.io.*;
import java.net.*;

class RRDummyAusgabeKommError extends RuntimeException {

}

/** 
 * DummyAusgabe.
 * Kommuniziert gemaess Protokoll mit dem Server und macht textuelle
 * Ausgaben. 
 * Mit Absicht (aus Testzwecken) ist die Kommunikation zu Fuss programmiert,
 * ohne Komm-Objekte.
 * Sollte der Server mal ein Timeout oder &auml;hnliche unvorhergesehene 
 * Dinge melden, verh&auml;lt sich das Programm undefiniert.
 *
 * Status: Noch nicht ausf&uuml;hrlich getestet. Anmeldung via Sockets
 * funktioniert. 
 *
 * @author Miriam
 * @version 0.1
 */ 
public class AusgabeDumm extends Thread { 

    private String servername;
    private int portnr;
    private BufferedReader fromServer;
    private PrintWriter toServer;

    /* Konstruktor */

    /** @param host Name des Servers
	@param port Portnummer
    */

    public AusgabeDumm(String host, int port) {
	servername = host;
	portnr = port;
    }

    /** @param Zu &uuml;berpr&uuml;fende Nachricht des Servers
     * Pr&uuml;ft, ob Nachricht ein ChangeNotify war.
     * Wenn ja, wird dem Server ok zurueckgeliefert, ohne
     * weiter drauf zu reagieren, denn wir haben gerades vor,
     * als uns um Aenderungen zu kuemmern.
     * @return War die Nachricht ChangeNotify?
     */
    private boolean checkChange(String s) {
	if (s.startsWith("NTC")) {
	    toServer.println("ok");
	    return true;
	}
	else
	    return false;
    }

/** 
 * Der Dummyausgabekanal meldet sich beim Server an und macht halbwegs
 * sinnvolle textuelle Ausgaben.
 */ 
 
public  void run() { 
    
    boolean spielende = false;
    String string;

    System.out.println("---------------------------------------------------------------------------");
    System.out.println("DummyAusgabe gestartet (Server " + servername + ", Port "+ portnr + ")"); 
 
	// Verbindung aufbauen 
    Socket sock = null; 
    try { 
	sock = new Socket(servername, portnr); 
    } catch(IOException e) { 
	System.out.println("Konnte keine Verbundung mit dem Server aufbauen."); 
	System.exit(0); 
    } 
    
    try{ 
	// Streams holen: 
	InputStream istr = sock.getInputStream(); // zum Lesen vom Socket 
	OutputStream ostr = sock.getOutputStream(); // zum Schreiben auf den Socket 
	
	// auf den Streams basierende, komfortablere Objekte erzeugen 
	fromServer = new BufferedReader(new InputStreamReader(istr)); 
	toServer = new PrintWriter(new OutputStreamWriter(ostr), true); 
	
	
	// Beim Server als Ausgabekanal anmelden:
	// naechste Verbesserung: zufaelliger Name, damit mehrere gestartet
	// werden koennen....
	String meinName = new String("DummeAusgabe");
	toServer.println("RGA("+meinName+")");
	String antwort = fromServer.readLine();
	if (antwort.startsWith("error")) {
	    System.out.println("Fehler bei Registrierung unter dem Namen "+meinName+": "+antwort);
	    throw new RRDummyAusgabeKommError();
	}	
	
	System.out.println("Anmeldung beim Server war erfolgreich!");

	// Warten, bis der Server sagt, dass das Spiel begonnen hat.
	do {
	    string = fromServer.readLine(); 
	} while (!string.startsWith("NTS"));
	toServer.println("ok");

	System.out.println("Das Spiel hat begonnen.");
	// Erstmal Spielfeld holen.
	// Achtung! Evtl. kommt derweil schon das erste ChangeNotify!
	boolean change = false;
	System.out.println(" *** Das Spielfeld: ***");
	
	toServer.println("GSD");
	do {
	    antwort = fromServer.readLine();
	} while (checkChange(antwort));
	System.out.println("Spielfelddimension: "+antwort);

	toServer.println("GFL");
	do {
	    antwort = fromServer.readLine();
	} while (checkChange(antwort));
	System.out.println("Flaggenpositionen: "+antwort);

	toServer.println("GPL");
	do {
	    antwort = fromServer.readLine();
	} while (checkChange(antwort));
	
	System.out.println("Der das Spielfeld definierende String: ");
	System.out.println(antwort);
	while (!antwort.endsWith(".")) {
	    antwort = fromServer.readLine();
	    System.out.println(antwort);
	}
	
	// Jetzt gehts richtig los!

	do { //Solange das Spiel laeuft........
	    
	    // Warten, bis der Server uns was sagt:
	    string = fromServer.readLine(); 
		
	    if (string.startsWith("NTC")) {
		StringTokenizer strtok = new StringTokenizer(string.substring(4),",)", false);
		while (strtok.hasMoreTokens()){
		    // Fuer jeden Namen, der im ChangeNotify stand, 
		    // den Status ausgeben
		    String name = strtok.nextToken();
		    toServer.println("GRS("+name+")");
		    antwort = fromServer.readLine();
		    System.out.println("Status von "+name+": "+antwort);
		}

		// Check ob das Spiel noch laeuft		
		toServer.println("GSS");
		antwort = fromServer.readLine();
		if (antwort.startsWith("SS(END)"))
		    spielende = true;

		toServer.println("ok");
	    }
	    else {
		System.out.println("Unerwartete Nachricht vom Server: "+string+"\n Ggf. Ausgabe per Ctrl-C beenden...");
	    }
	    

	    
	} while (!spielende);
    }
    catch (Exception e) {
	System.out.println("Exception "+e+" ist aufgetreten!");
    }
}
}
