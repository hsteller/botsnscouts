package de.botsnscouts.gui;

import  de.botsnscouts.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.comm.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import  de.botsnscouts.server.KartenStapel; 
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;
/**
 * logic for the human player
 * @author Lukasz Pekacki
 */
public class HumanPlayer extends Thread {

    private HumanView humanView;
    private Ausgabe ausgabe;
    private KommClientSpieler comm;
    private View view;
    private ClientAntwort commAnswer = new ClientAntwort();
    private SpielfeldKS intelliBoard;

    private ArrayList cards = new ArrayList(9);
    private String host, name;
    private int port, myRobIndex, myColor, globalTimeout;
    private boolean gameOver = false, nosplash = false;

    public HumanPlayer (String host, int port, String name) {
	this(host,port,name,-1);
    }

    public HumanPlayer(){
	this ("localhost",8077,KrimsKrams.randomName());
    }
    public HumanPlayer(String host, int port, String name, int color) {
	this (host, port, name, color, false);
    }
    public HumanPlayer(String host, int port, String name, int color, boolean nosplash) {
	this.host = host;
	this.port = port;
	this.name = name;
	this.nosplash=nosplash;
	myColor=color;
	comm = new KommClientSpieler();
    }


    /**
     * Start des Menschlichen Spielers
     */ 
    public void run(){

	// --- registering for game ---
	if (registerAtServer()) {
	    Global.debug(this,"registered for game as new humanplayer with name: "+name);
	}
	else {
	    Global.debug(this, "could not register at the server: "+host);  
	    try {Thread.sleep(2000);} catch (Exception e) {System.err.println(e.getMessage());}
	    return;
	}

	initView();

	// ------- begin to play
	while (!gameOver) {

	    try {
		commAnswer = comm.warte();
		Global.debug(this,"Server sends : " + commAnswer.getTyp());
	    }
	    catch (KommException kE) {
		Global.debug(this,kE.getMessage());
	    }

	    switch (commAnswer.typ) {
	    case (commAnswer.MACHEZUG): {
		Global.debug(this,"I am requested to send cards");
		// card 
		showMessage(Message.say("SpielerMensch","mwartereg"));
				// ---------- gesperrte Register nachfragen
		try{
		   Global.debug(this,"Versuche, Robstatus von "+name+" zu bekommen.");
		    Roboter tempRob = comm.getRobStatus(name);
		    for (int i=0; i < tempRob.getGesperrteRegister().length; i++) {
			if (tempRob.getGesperrteRegister(i) != null) {
			    // lock Register i
			}
			else {
			    // unlock Register i
			}
		    }
		}
		catch (KommException kE) {
		    System.err.println("SpielerMenschERROR: "+kE.getMessage());
		}
		
				// ----- Karten einsortieren  -----
		    cards.clear();
		    for (int i = 0; i < commAnswer.karten.length; i++) {
			cards.add(i, new HumanCard(commAnswer.karten[i]));
		    }
		    humanView.showCards(cards);
		    //		scoutAllowed=true;

		    // --- ist der Klugscheisser aktiv?
		    //		if (klugAktiv) fragKlug();

				// ----- Abgabe der Programmierung -----

		    /*	start Timer
		if (temptimeout == 0) {
		    showMessage(Message.say("SpielerMensch","legalZug"));
		    int gesperrteRegister = 0;
		    for (int i = 0;i < 5; i++) if (uI.register[i].status == GESPERRT) gesperrteRegister++;
		    int[] prog = new int[(5-gesperrteRegister)];
		    for (int i = 0; i < prog.length; i++) prog[i] = (i+1);
		    comm.registerProg(name,prog,false);
		}
		    */
		break;
	    }

	    // start of the game
	    case (commAnswer.SPIELSTART): {
		showMessage(Message.say("SpielerMensch","spielgehtlos"));
		comm.spielstart();
		break;
	    }

	    // robot destroyed or initally set on the board
	    case (commAnswer.ZERSTOERUNG): {
		humanView.showGetDirection();
		Global.debug(this,"Habe einee Zerstörung bekommen.");
		showMessage(Message.say("SpielerMensch","roboauffeld"));
		/* TODO
		// --- Spielfeld für den Klugscheisser holen
		if (intelliBoard==null) {
		    spielfeldkreieren();
		    wirbel = new Permu(intelliBoard,0);
		}
		*/
	       	// ----- ask for timeout -------
		if (globalTimeout ==0) {
		    try {
			globalTimeout = comm.getTimeOut();
		    }
		    catch (KommException kE) {
			System.err.println("SpielerMenschKommunkationsERROR: wollte Timeout erfragen: "+kE.getMessage());
		    }
		}
		break;
	    }
	    // robot reaktivated
	    case (commAnswer.REAKTIVIERUNG): { 
		showMessage(Message.say("SpielerMensch","roboreaktiviert"));
				// -------- Nochmaliges PowerDown abfragen und senden ------
		/* ask for powerDownagain, TODO
		uICardLayout.show(userInterfaceContainer,"wiederpowerdown");
		*/
		break;
	    }

	    // repair your registers
	    case (commAnswer.REPARATUR):{ 
		    Global.debug(this,"Reparatur erhalten");
		    /* TODO
		try {
		    Global.debug(this,"Reparatur erhalten; ersuche, Status von "+name+"  zu erfragen...");
		    Roboter tempRob = comm.getRobStatus(name);

		    // Anzahl der Reparaturpunkte
		    reparaturPunkte = commAnswer.zahl;


		    for (int i=0; i < tempRob.getGesperrteRegister().length; i++) {
			if (tempRob.getGesperrteRegister(i) != null) {

			    uI.register[i].sperren();
			    Global.debug(this,"Server sagt: Register gesperrt: "+i);
			}
			else {
			    uI.register[i].status=FREI; 
			uI.register[i].resetRegister(); 
			}
		    }
		
		}
		catch (KommException kE) {
		System.err.println("SpielerMensch: "+kE.getMessage());
		}

		  for (int i = 0; i < uI.register.length; i++) 
		  Global.debug(this,"SpM: Register "+i+" hat Status: "+uI.register[i].status);	

		showMessage(Message.say("SpielerMensch","registerrep"));
		Global.debug(this,"Anzahln der Reparaturpunkte: "+reparaturPunkte);
		
		// ----- Abfrage und Senden der Register, die repariert werden sollen ---

		// ergebnisArray auf null setzen
		for (int i = 0; i < 5; i++) uI.regZuReparieren[i] = -1;

		// Anfrage einblenden
		uICardLayout.show(userInterfaceContainer,"zureparieren");
		zuRepPanel.zeigeAuswahl();
		    */
		break;
	    }

	    // removed from game
	    case (commAnswer.ENTFERNUNG): {
		// ------- Habe ich gewonnen / bin ich gestorben ----------
		boolean dead = true;
		int rating=0;
		try {
		    String[] gewinnerListe = comm.getSpielstand();
		    if(gewinnerListe != null) {
			showMessage(Message.say("SpielerMensch","spielende")); 
			for (int i = 0; i < gewinnerListe.length; i++) {
			    if (gewinnerListe[i].equals(name)) {
				dead=false;
				rating=(i+1);
			    }
			}
		    }
		    else {
			Global.debug(this,"Bin gestorben...");
			dead = true;
		    }
		}
		catch (KommException e) {Global.debug(this, e.getMessage());}
		gameOver=true;
		humanView.showGameOver(dead, rating);
		break;
	    }
	    default : {
		Global.debug(this,"Unkonown message form server.");
	    }
	    }	
	}
	
	Global.debug(this," this is my end");
	try {
	    ausgabe.join();
	} catch(InterruptedException e){e.printStackTrace();}
	return;
    }
    
    /**
     * Main-Methode, die den menschlichen Spieler von der Shell aus als Thread startet
     */
    public static void main(String[] args){
	//1. name (optional)
	//2. host (optional)
	//3. port  "
	//4. farbe "
	/*	int sPort = 0;
	SpielerMensch spM;
	if ((args.length > 0) &&(args[0] != "") && (args[1]) !="") {
	    try {sPort = Integer.parseInt(args[1]); } catch (Exception e) {System.err.println(e.getMessage());}
	    spM = new SpielerMensch(args[0],sPort,createName());
	}
	else spM = new SpielerMensch();
	spM.run();*/
	String name, host="127.0.0.1";
	int port=8077, farbe=0;
	if (args[0] == null) name = KrimsKrams.randomName();
	else   name = args[0];
	int tmpInt;
	switch(args.length){
	case 2: try{
	    tmpInt=Integer.parseInt(args[1]);
	    if (tmpInt<9){
		farbe=tmpInt;
		port=8077;
	    }else{
		port=tmpInt;
		farbe=0;
	    }
	    host="127.0.0.1";
	}catch(NumberFormatException e){
	    host=args[1];
	    port=8077;
	    farbe=0;
	}
	break;
	case 3: host=args[1];
	    try{
		tmpInt=Integer.parseInt(args[2]);
		if (tmpInt<9){
		    farbe=tmpInt;
		    port=8077;
		}else{
		    port=tmpInt;
		    farbe=0;
		}
	    }catch(NumberFormatException e){
		System.err.println(e);
	    }
	    break;
	case 4: host=args[1];
	    try{
		port=Integer.parseInt(args[2]);
		farbe=Integer.parseInt(args[3]);
	    }catch(NumberFormatException e){
		System.err.println(e);
	    }
	}
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );
	(new SpielerMensch(host,port,name,farbe)).start();
    }

    protected void sendCards(ArrayList registerCards, boolean nextTurnPowerDown) {
	int sendProg[] = new int[registerCards.size()];
	int index=0;
	
	d("meine Registerkarten: "+registerCards);
	d("die Karten, die der Server ausgeteilt hat:"+cards);
	

	for (int i=0; i < registerCards.size(); i++) {
	    for (int j=0; j < cards.size(); j++) {
		if ( ((HumanCard) registerCards.get(i)).equals((HumanCard)cards.get(j)) ) {
		    sendProg[index]=(j+1);
		    index++;
		    continue;
		}
	    }
	}
	comm.registerProg(name,sendProg,nextTurnPowerDown);
    }
    
    private boolean registerAtServer() {
	boolean anmeldungErfolg = false;
	int versuche = 0;
	
	while ((!anmeldungErfolg)&&(versuche < 3)) { 
	    try{
		anmeldungErfolg = comm.anmelden2(host,port,name,myColor); 
	    } 
	    catch (KommException kE) {
		System.err.println(kE.getMessage()); 
		versuche++; 
		try {Thread.sleep(1000);} catch (Exception e) {System.err.println(e.getMessage());}
	    }
	}
	return anmeldungErfolg;
	
    }

    /** meldet den Spieler beim Server ab und beendet diesen Thread.
    */
    private void quit() {
	Global.debug(this, "Roboter "+name+" is leaving the party.");
	comm.abmelden(name);
	//Dafuer sorgen, dass Thread aufhoert 
	System.exit(0);
    }

    protected void passUpdatedScout(int chosen, Roboter[] robs) {
	ausgabe.showScout(chosen,robs);
    }


    protected void sendRepair(ArrayList respReparatur) {
	/*
		int[] repa = new int[reparaturPunkte];
		for (int i = 0; i < repa.length; i++) {
		}
		comm.respReparatur(name,repa);
		showMessage(Message.say("SpielerMensch","sendregrep"));
	*/
	// show wait message
    }
    
    protected Roboter getRob() {
	return ausgabe.getRob(name);

    }


    protected void sendDirection(int r) {
		comm.respZerstoert(name,r); 
		// show wait message
    }


    protected void sendAgainPowerDown(boolean down) {
		comm.respReaktivierung(name,down);
    }


    /**
     * Schreibt in die Statuszeile einen Text
     */
    private void showMessage(String s){
	// Logframe instanziieren, falls nötig
	ausgabe.showActionMessage(s);
    }


    private void initIntelligentBoard()
    {
	int dimx, dimy;
	Ort dimension;
	try{
	    dimension=comm.getSpielfeldDim();
	    dimx=dimension.x;
	    dimy=dimension.y;
	    Ort[] fahnen=comm.getFahnenPos();
	    String spielfeldstring=comm.getSpielfeld();
	    try{
		intelliBoard = SpielfeldKS.getInstance(dimx,dimy,spielfeldstring,fahnen);
	    }
		
	    catch(Exception e){
		System.err.println("HumanPlayer has a problem: No Board"+e);
	    }			
	}
	catch(Exception e){
	    System.err.println("HumanPlayer has a problem: No Board!"+e);
	}
    }
    

    private void initView() {
	humanView = new HumanView(this);
	view=new View(humanView);
	ausgabe = new Ausgabe(host, port, nosplash, this, view);
	ausgabe.initialize();
	ausgabe.start();
    }


    private void d(String s) {
	Global.debug(this,s);
    }

}
