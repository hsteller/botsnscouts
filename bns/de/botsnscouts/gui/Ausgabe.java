package de.botsnscouts.gui;

import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Diese Klasse erledigt die Ausgabe von Spielfeld und Status
 * @author Lukasz Pekacki
 */

public class Ausgabe extends Thread {


    // -----------  Konstanten -----------

    // -----------  Referenzen auf Objekte  -----------

    private KommClientAusgabe kommClient;
    private ClientAntwort kommAntwort = new ClientAntwort();
    private AusgabeView ausgabeView;
    private Splash splashScreen;
    private Hashtable robots = new Hashtable(8);
    private View view;

    // ---------- class variables ------------
    
    // game constants
    private Dimension boardDimension;
    private Ort[] flags;

    private String host, name;
    private int port;
    private boolean spielEnde = false;
    private boolean nosplash = false;

    public Ausgabe() {
	this("localhost",8077,false);
    }


    public Ausgabe(String host, int port, boolean nosplash) {
	this.nosplash=nosplash;
	// Splash-Screen anzeigen
	showSplash(Message.say("Ausgabe","msplashWarte"));

	this.host = host;
	this.port = port;
	name = KrimsKrams.randomName();

	// Kommunikationsobjekt erzeugen
	kommClient = new KommClientAusgabe();
    }


    public void run() {
	// ------- Anmeldung am Server -------
	boolean anmeldungErfolg = false;
	setStatus(Message.say("Ausgabe","Anmeldung"));
	int versuche = 0;
	while ((!anmeldungErfolg)&&(versuche < 3)) { 
	    try{
		anmeldungErfolg = kommClient.anmelden2(host,port,name); 
	    } 
	    catch (KommException kE) {
		System.err.println(kE.getMessage()); 
		showSplash(Message.say("Ausgabe","msplashFehlerAnmeldung"));
		Global.debug("Ausgabe: Anmeldung fehlgeschlagen."); } 
	    versuche++; 
	    try {Thread.sleep(3000);} catch (Exception e) {System.err.println(e.getMessage());}
	}
	if (!anmeldungErfolg) {
	    new Fehlermeldung(Message.say("Ausgabe","eAnmeldung"));
	    Global.debug(this, "Ausgabe: Beende Versuche.");  
	    // entferne das Splash-Screen
	    showSplash(Message.say("Ausgabe","msplashEnde"));
	    try {Thread.sleep(1000);} catch (Exception e) {System.err.println(e.getMessage());}
	    removeSplash();
	    return;
	}
	else {Global.debug(this,"Ausgabe: Anmeldung erfolgt.");
	}
	
	// ---- game  ---------

	while (!spielEnde) {
	    
	    // ------- Empfang einer Server-Meldung  -------
	    try {
		kommAntwort = kommClient.warte();
	    }
	    catch (KommFutschException kE) {
		System.err.println("KE: "+kE.getMessage());
		return;
	    }
	    catch (KommException ke) {
		System.err.println("ke: "+ke.getMessage());
		return;
	    }
	
	    switch (kommAntwort.typ) {
		
		// ---------- start of the game  ------
	    case (kommAntwort.SPIELSTART): { 
		Global.debug(this,"Server send me: game start.");

		// ------- Einmaliges Holen des Spielfeldes und ermitteln der Spieler -----
		try { 
		    String[] playerNames = kommClient.getNamen();
		    
		    for (int i=0; i < playerNames.length; i++) {
			robots.put(playerNames[i], Roboter.getNewInstance(playerNames[i]));
		    }

		    String[] playerColors = kommClient.getFarben();
		    Color[] roboColors = new Color[8];

		    Ort boardDim = kommClient.getSpielfeldDim();
		    boardDimension = new Dimension(boardDim.x,boardDim.y);
		    flags = kommClient.getFahnenPos();
		    SpielfeldSim sim = new SpielfeldSim(boardDimension.width,boardDimension.height,kommClient.getSpielfeld(),flags);
		    if (sim == null) Global.debug(this,"Habe kein Spielfeldsim bekommen!");
		    SACanvas sac = new SACanvas(sim);
		    if (sac == null) Global.debug(this,"Habe kein SACanvas bekommen!");
		    ausgabeView = new AusgabeView(new SACanvas(sim));
		    // ------ let the view update the robots --------
		    ausgabeView.showUpdatedRobots(getRoboterArrray());
		    // ------ create View
		    view=new View(ausgabeView);
		    Global.debug(this,"AusgabeView erzeugt.");
		    removeSplash();

		    /* 
		       TO DO
		       get color names an give it to ausgabeView
		       ,roboNcolor)
		    */
		    
		    // send OK to server
		    kommClient.spielstart(); 
		    Global.debug(this,"Bestaetigung des Spielstarts abgeschickt...");
		    
		    scrollFlag(1);

		}
		catch (KommException kE) {
		    System.err.println("Ausgabe: Beim Versuch, die Roboter zu holen, erhalte ich: "+
				       kE.getMessage());
		    return;
		}
		catch (FormatException e) {System.err.println(e.getMessage());}
		catch (FlaggenException e){System.err.println(e.getMessage());}

		break;
	    }
	    case (kommAntwort.MESSAGE):{
		Global.debug(this,"Server send me: some messsage.");
		String[] tmpstr=new String[kommAntwort.namen.length-1];
		for (int i=0;i<tmpstr.length;i++)
		    tmpstr[i]=kommAntwort.namen[i+1];

		if (!(kommAntwort.namen[0].substring(0,5).equals("mAusw")))
		    setStatus(Message.say("MSG",kommAntwort.namen[0],tmpstr));

		if (kommAntwort.namen[0].equals("mRobLaser")){
		    Roboter r1=null;
		    Roboter r2=null;
		    try {
			r1=kommClient.getRobStatus(kommAntwort.namen[1]);// schiessender Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    try {
			r2=kommClient.getRobStatus(kommAntwort.namen[2]);// getroffener Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    ausgabeView.showRobLaser(r1, r2);
		}
		else if (kommAntwort.namen[0].equals("mBoardLaser")){
		    Roboter r1=null;
		    Ort r1Pos = null;
		    // get damaged Roboter
		    try {
			r1=kommClient.getRobStatus(kommAntwort.namen[1]);
			r1Pos= new Ort (r1.getX(), r1.getY());
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    // get the Laser-Position
		    Ort laserPos = new Ort(0,0);
		    int facing=-1;
		    int strength=-1;
		    try {
	 		strength   = Integer.parseInt(kommAntwort.namen[2]);
			laserPos.x = Integer.parseInt(kommAntwort.namen[3]);
			laserPos.y = Integer.parseInt(kommAntwort.namen[4]);
			facing     = Integer.parseInt(kommAntwort.namen[5]); 
       		    }
		    catch (NumberFormatException nfe) {
			System.err.println("Ausgabe: BoardLaser: NumberFormatException:");
			nfe.printStackTrace();
		    }
		    if ((laserPos!=null)&&(facing>=0)&&(r1Pos!=null)&&(strength>=0))
			ausgabeView.showBoardLaser(laserPos, facing, strength, r1Pos);
		    else {
			System.err.println("Ausgabe: unable to calculate Laseranimation: ");
			System.err.println("laserPos: "+laserPos);
			System.err.println("facing: "+facing);
			System.err.println("r1Pos: "+r1Pos);
			System.err.println("strength: "+strength);
		    }
		}
		
		
		// send ok to server
		kommClient.bestaetigung();
		break;
	    }
	    case (kommAntwort.AENDERUNG): {
		Global.debug(this,"Server send me: change occured.");
		// ------- get changes  -----------
		Global.debug(this,kommAntwort.namen.length+" robs have been updated.");
		try { 
		    String[] playerNames = kommAntwort.namen;
		    for (int i=0; i < playerNames.length; i++) {
			robots.put(playerNames[i],kommClient.getRobStatus(playerNames[i]));
		    }
		    /* TODO: set Roboter status
		       statusLine.setRobStatus(spNamen[i],kommClient.getRobStatus(spNamen[i]));
		    */
		
		    ausgabeView.showUpdatedRobots(getRoboterArrray());

		// --------- Neue Roboter-Position an Spielfeld senden ---------
		try {
		    Thread.sleep(1000);
		} // Verzögerung der Ausgabegeschwindigkeit
		catch (Exception e) {
		    System.err.println(e.getMessage());
		} 
		
		// --------- get other information from the server
		/*
		  Global.debug("Ausgabe: Hole Spielstatus...");
		  try {
		  Status[] stArray = kommClient.getSpielstatus();
		  if (stArray != null) {
		  statusLine.weitereStati(stArray);
		    // Phase ausgeben
		    if (stArray[0].aktPhase != lastPhase) {
		    setStatus(Message.say("Ausgabe","phase")+stArray[0].aktPhase);
		    lastPhase = stArray[0].aktPhase;
		    }
		    }
		*/
		// --------- has somebody already reached the final flag
		String[] spStand = kommClient.getSpielstand();
		if (spStand != null) {
		    Global.debug(this,"Es gibt schon Spieler, die am Ziel sind; hole Gewinnerliste...");
		    // TODO show winner
		
		}
		}
		catch (KommFutschException ke) {
		    System.err.println("ke2: "+ke.getMessage());
		    return;
		}
		catch (KommException kE) {
		    System.err.println(kE.getMessage());
		    return;
		}
	
		// finish questioning the server
		Global.debug(this, "Ausgabe: Sende Aenderung fertig an Server.");
		kommClient.aenderungFertig();
		break;
	    }
	    case (kommAntwort.ENTFERNUNG): { 
		Global.debug(this,"Das Spiel is over.");

		// Gewinnerliste ausgeben
		try {
		    String[] spielErgebnis = kommClient.getSpielstand();
		    if (spielErgebnis != null) {
			Global.debug(this,"We have winners");
			ausgabeView.showWinnerlist(spielErgebnis);
		    }
		    else Global.debug(this,"No winner exists");
		}
		catch (KommException e) {
		    System.err.println(e.getMessage());
		    return;
		}
		try {
		    Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		    System.err.println("Ausgabe: Interrupted by "+e.toString());
		}
		// send OK to server
		kommClient.spielstart(); 
		spielEnde = true;
	    }
	    }
	    
	}
	Global.debug(this,"I reached the end of my run() method");
	setStatus(Message.say("Ausgabe","spielende"));
	return;
    }


    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls nötig)
     */
    private void showSplash(String s) {
	if(!nosplash) {
	    if (splashScreen==null){ 
		splashScreen = new Splash();
	    }
	    splashScreen.showSplash(s);
	}
	
    }

    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls nötig)
     */
    private void removeSplash() {
	if(!nosplash) {
	    splashScreen.noSplash();
	}
	
    }

    private Roboter[] getRoboterArrray(){
		    Roboter[] robs = new Roboter[robots.size()];
		    int i=0;
		    Iterator iter = robots.values().iterator();
		    while (iter.hasNext()) {
			robs[i] = (Roboter) iter.next();
			i++;
		    }
		    return robs;
    }


    /**
     * Fordert die View auf, eine ActionMessage anzuzeigen
     */
    protected void setStatus(String s){
	if (ausgabeView !=null) {
	ausgabeView.showActionMessage(s);
	}
    }

    /**
     * Zentriert den Robi und folgt ihm.
     */
    public void trackRob (String rName) {
	Roboter r = (Roboter) robots.get(rName);
	trackPos(r.getX(), r.getY());
    }


    /**
     * Zentriert die angegebenen Flagge
     */
    public void scrollFlag (int nr) {
	  trackPos(flags[(nr-1)].getX(),flags[(nr-1)].getY());
    }

    public void trackPos (int x, int y) {
	ausgabeView.showPos(x,y);
    }




}



