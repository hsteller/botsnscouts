package de.spline.rr;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
/**
 * Diese Klasse repr&auml;esentiert die Ausgabe.
 * @author Daniel Holtz, Lukasz Pekacki
 * @see AusgabeFrame
 * @see SpielerMensch
 */
public class Ausgabe extends Thread {

    protected AusgabeFrame f;

    public Thread frameThread;

    protected String host, name;
    protected int port;
    protected SpielerMensch spieler;

    public Ausgabe(String host,int port) {
	this(host,port,false);
    }


    public Ausgabe(String host,int port, boolean nosplash) {
	// Parameter übernehmen
	this.host = host;
	this.port = port;

	// Ausgabe-Fenster erzeugen
	//	if (this.getClass().getName().endsWith("SpielerMensch"))
	//     (So nicht, sondern via ClassCastException.)
	try{
	    f = new AusgabeFrame(host,port,(SpielerMensch )this,nosplash);
	    Global.debug(this, "Der neue AusgabeFrame gehoert zu einem SpielerMensch!?");
	} catch (ClassCastException e) {
	    //War gar kein SpielerMensch, war also nur 'ne Ausgabe
	    Global.debug("Nein, war nur eine Ausgabe!");
	    f = new AusgabeFrame(host,port,null,nosplash);
	}
    }

    public Ausgabe(){
	this ("localhost",8077);
    }

    //    public Ausgabe(String host,int port){
    //	this (host,port,null);
    //}

     /**
     * Schreibt in die Statuszeile einen Text
     */
    protected void setStatus(String s){f.setStatus(s);}

    /**
     * Methode zur Ausgabe von Debug-Meldungen auf STDOUT
	 * solage nicht alle Sourcen Global.debug verwenden,
	 * dient diese alte Methode als Wrapper
     */
    protected static void printDebug(String s){
	Global.debug(s);
    }

    public void run(){
	frameThread = new Thread(f);
	frameThread.start();
    }

    /**
     * Main-Methode, die die Ausgabe als Thread startet
     */
    public static void main(String[] argv){
	Ausgabe au = new Ausgabe(argv[0],Integer.parseInt(argv[1]));
	au.run();
    }
}
