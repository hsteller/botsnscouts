/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

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
import org.apache.log4j.Category;


public class Ausgabe extends BNSThread {
    static Category CAT = Category.getInstance(Ausgabe.class);

    // for communication with server
    private KommClientAusgabe kommClient;
    private ClientAntwort kommAntwort = new ClientAntwort();

    // for displaying the stuff
    private AusgabeView ausgabeView;
    private Splash splashScreen;
    private View view;

    // managing values of the robots
    private Hashtable robots = new Hashtable(8);

    // keeping statistic information;  "who-kills-who"-dictionary ;-)
    private StatsList stats;
    private JFrame statsWindow;

    // game constants
    private Dimension boardDimension;
    private Ort[] flags;

    private String host, name;
    private int port;
    private boolean spielEnde = false;
    private boolean nosplash = false;
    private boolean registered = false;

    /** the last phase of the current turn */
    private int lastPhase=1;

    public Ausgabe() {
	this("localhost",8077,false);
    }

    public Ausgabe(String host, int port, Object o, boolean nosplash) {
	this (host,port,nosplash);
    }

    public Ausgabe(String host, int port, boolean nosplash,/* HumanPlayer human,*/ View v) {
	this(host, port, nosplash);
	//this.human = human;
	this.view =v;
    }

    public Ausgabe(String host, int port, boolean nosplash) {
	this.nosplash=nosplash;
	this.host = host;
	this.port = port;
	name = KrimsKrams.randomName();
        super.setName("Ausgabe["+name+"]");
	showSplash(Message.say("AusgabeFrame","msplashWarte"));
	kommClient = new KommClientAusgabe();

    }


    public void run() {

	if (! registered) {initialize();}

	// ---- entering game  ---------

	while (!spielEnde) {
	    try {
		kommAntwort = kommClient.warte();
	    }
	    catch (KommFutschException kE) {
		CAT.error("KE: "+kE.getMessage());
		return;
	    }
	    catch (KommException ke) {
		CAT.error("ke: "+ke.getMessage());
		return;
	    }
	    // what did the server send?
	    switch (kommAntwort.typ) {
	    case (ClientAntwort.MESSAGE):{
		CAT.debug("Server send me: "+kommAntwort.namen[0]);

                // getting parts of the message
                String[] tmpstr=new String[kommAntwort.namen.length-1];
		for (int i=0;i<tmpstr.length;i++)
		    tmpstr[i]=kommAntwort.namen[i+1];

                // check the kind of message
                String msgId = kommAntwort.namen[0];
		if (!(msgId.substring(0,5).equals("mAusw"))) {
                    if (CAT.isDebugEnabled())
		      CAT.debug("kommAntowrtnamen[0] ist: "+msgId+ "tmpstr ist: "+tmpstr[0]);
		    // display the message in the statusbar
                    showActionMessage(Message.say("MSG",msgId,tmpstr));
		}

		if (msgId.equals("mRobLaser")){ // robots shooting
		    CAT.debug("Got message telling "+kommAntwort.namen[1]+" shot "
			      +kommAntwort.namen[2]+".");

		    Roboter r1 =(Roboter )robots.get(kommAntwort.namen[1]);
		    Roboter r2 =(Roboter )robots.get(kommAntwort.namen[2]);

		    // updating statistics
		    Stats actualStats=stats.getStats(r1.getName());
		    actualStats.incHits();
		    if (r2.getSchaden()>=10) // was the robot r2(hit) killed ny r1?
			actualStats.incKills();
		    actualStats=stats.getStats(r2.getName());
		    actualStats.incDamageByRobots();

                    // paint animation and play sounds
		    ausgabeView.showRobLaser(r1, r2);
		}

                else if (msgId.equals("mGrubenopfer")) {// robot fell into a pit
                    // play the sound for "robot fell into pit"
                    SoundMan.playSound(SoundMan.PIT);
                }

                else if (msgId.equals("mNextFlag")) {// robot fell into a pit
                    // play the sound for "robot fell into pit"
                    SoundMan.playSound(SoundMan.FLAG_REACHED);
                }

		else if (msgId.equals("mBoardLaser")){ // boardlaser shooting

		    // get damaged robot
		    Roboter r1= (Roboter )robots.get(kommAntwort.namen[1]);
		    Ort r1Pos = r1.getPos();

                    // updating statistics for the robot hit
                    Stats actualStats=stats.getStats(r1.getName());
                    actualStats.incDamageByBoard();

		    // get the origin of the laser (position and other stuff
                    // needed for animation)
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
			CAT.error("Ausgabe: BoardLaser: NumberFormatException:");
			CAT.error(nfe);
                        nfe.printStackTrace();
		    }

		    for (int i=0; i<strength; i++){
			SoundMan.playSound(SoundMan.BOARDLASER);
		    }

                    // if enough information,  show laser animation
		    if ((laserPos!=null)&&(facing>=0)&&(r1Pos!=null)&&(strength>=0)){
			ausgabeView.showBoardLaser(laserPos, facing, strength, r1Pos);
                    }
		    else {
                        if (CAT.isDebugEnabled()){
			  CAT.error("Ausgabe: unable to calculate Laseranimation: ");
			  CAT.debug("laserPos: "+laserPos);
			  CAT.debug("facing: "+facing);
			  CAT.debug("r1Pos: "+r1Pos);
			  CAT.debug("strength: "+strength);
                        }
		    }
		}
                else if (msgId.equals("mKlugSchKlick")){
                    Roboter r1= (Roboter )robots.get(kommAntwort.namen[1]);
                    Stats actualStats=stats.getStats(r1.getName());
                    actualStats.incAskWisenheimer();
                }
		try{
                    // send response to the server that we got the message
                    // and did all the stuff we wanted to do, so the server can
                    // send the next message
		    kommClient.acknowledgeMsg();
		} catch (KommFutschException ke) {
		    CAT.error("ke2: "+ke.getMessage());
		    CAT.error(ke);
                    return;
		}
		catch (KommException kE) {
		    CAT.error(kE.getMessage());
		    CAT.error(kE);
                    return;
		}

		break;
	    }

	    case (ClientAntwort.AENDERUNG): {// notify change; something happened
		CAT.debug("Server send me: change occured.");

		// ------- get changes  -----------
		CAT.debug(kommAntwort.namen.length+" robs have been updated.");
		try {
		    String[] playerNames = kommAntwort.namen;
		    for (int i=0; i < playerNames.length; i++) {
			robots.put(playerNames[i],kommClient.getRobStatus(playerNames[i]));
		    }

		    ausgabeView.showUpdatedRobots(getRoboterArray());

		    // --------- Neue Roboter-Position an Spielfeld senden ---------
		    try {
			Thread.sleep(100);
		    } // Verzögerung der Ausgabegeschwindigkeit
		    catch (Exception e) {
			System.err.println(e.getMessage());
		    }

		    // --------- get other information from the server
		    Status[] stArray = kommClient.getSpielstatus();
		    if (stArray != null) {
			if (stArray[0].aktPhase != lastPhase) {
			    showActionMessage(Message.say("AusgabeFrame","phase")+stArray[0].aktPhase);
			    lastPhase = stArray[0].aktPhase;
			}
		    }
		    // --------- has somebody already reached the final flag?
		    String[] winnerStateList = kommClient.getSpielstand();
		    ausgabeView.showWinnerState(winnerStateList);
		}
		catch (KommFutschException ke) {
		    CAT.error("ke2: "+ke.getMessage());
		    return;
		}
		catch (KommException kE) {
		    CAT.error(kE.getMessage());
		    return;
		}

		kommClient.aenderungFertig();
		break;
	    }

	    case (ClientAntwort.ENTFERNUNG): {
		CAT.info("Game over.");

		try {
		    String[] spielErgebnis = kommClient.getSpielstand();
		    if (spielErgebnis != null) {
			CAT.debug("We have "+spielErgebnis.length+" winners");
			ausgabeView.showWinnerlist(spielErgebnis);
		    }
		    else CAT.debug("No winner exists");
		}
		catch (KommException e) {
		    CAT.error(e.getMessage());
		    return;
		}
		try {
		    Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		    CAT.error("Ausgabe: Interrupted by "+e.toString());
		}
		kommClient.spielstart();
		spielEnde = true;
	    }
	    }

	}
	CAT.debug("Ausgabe reached end of its run method");
	//showActionMessage(Message.say("AusgabeFrame","spielende"));
	return;
    }


    protected void initialize() {
	registerAtServer();
	try {
	    kommAntwort = kommClient.warte();
	}
	catch (KommFutschException kE) {
	    CAT.error("KE: "+kE.getMessage());
	    return;
	}
	catch (KommException ke) {
	    CAT.error("ke: "+ke.getMessage());
	    return;
	}

	if (kommAntwort.typ == kommAntwort.SPIELSTART) {
	    Global.debug(this,"Server send me: game start.");

	    // ------- fetching the board -----
	    try {
		String[] playerNames = kommClient.getNamen();
		String[] playerColors = kommClient.getFarben();

		Hashtable playerColorHash = new Hashtable(playerColors.length);
		for (int i = 0; i < playerColors.length; i++) {
		    if (! playerColors[i].equals("0")) {
			playerColorHash.put(playerColors[i],new Integer(i));
		    }
		}
                BotVis.initBotVis( playerColorHash );

		Ort boardDim = kommClient.getSpielfeldDim();
		boardDimension = new Dimension(boardDim.x,boardDim.y);
		flags = kommClient.getFahnenPos();
		// !!HACK!!

		Color[] robotsDefaultColor = SACanvas.robocolor;
		Color[] robotsNewColor = new Color[8];

		int[] colorMap = new int[8];

		int j=0;
		for (int i=0;i<8;i++) {
		    if(!playerColors[i].equals("0")) {
			robotsNewColor[j]=robotsDefaultColor[i];
			colorMap[j]=i;
			j++;
		    }
		}

		// !! END OF HACK !!

		for (int i=0; i < playerNames.length; i++) {
		    d("Hole Roboterstatus von: "+playerNames[i]);
		    Roboter tempRob = kommClient.getRobStatus(playerNames[i]);
		    tempRob.setBotVis(((Integer)playerColorHash.get(tempRob.getName())).intValue());
		    robots.put(playerNames[i], tempRob);
		}


		SpielfeldSim sim = new SpielfeldSim(boardDimension.width,
						    boardDimension.height,
						    kommClient.getSpielfeld(),
						    flags);


                SACanvas board = new SACanvas(sim,robotsNewColor);
                board.setAutoscrolls( true );
		ausgabeView = new AusgabeView(board,getRoboterArray(),this);
                board.addMouseListener( new MouseAdapter() {
                    public void mouseReleased( MouseEvent me ) {
                        if( (me.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
                            CAT.debug("mouse clicked in canvas " + me.getPoint() );
                            Ort ort = new Ort();
                            SACanvas brd = (SACanvas) me.getSource();
                            brd.point2Ort( me.getPoint(), ort );
                            CAT.debug("corresponding ort is: " + ort );
                            ausgabeView.showPixelPos( me.getPoint().x, me.getPoint().y );
                        }
                    }
                });

		if (view == null) {
		    view=new View(ausgabeView);
		}
		else {
                    view.addAusgabeView(ausgabeView);
		    view.makeVisible();
                }

                // fetching initial stats
                try {
                  CAT.debug("fetching stats..");
                  stats = kommClient.getStats();
                  if (CAT.isDebugEnabled()){
                    CAT.debug("..done");
                    CAT.debug("stats ist: ");
                    CAT.debug(stats.toString());
                  }
                }
                catch (KommException ke) {
                  CAT.error(ke);
                  CAT.error("KommException occured!");
                  CAT.error("Failed to initialize Statistics-Menu!!!");
                  CAT.error("!!YOU BETTER DO NOT CLICK ON THE STATISTICS MENU !!!");
                }

		removeSplash();

		// send OK to server
		kommClient.spielstart();

		scrollFlag(1);

	    }
	    catch (KommException kE) {
		CAT.error("Ausgabe: Beim Versuch, die Roboter zu holen, erhalte ich: "+
				   kE.getMessage());
		return;
	    }
	    catch (FormatException e) {
		System.err.println(e.getMessage());
	    }
	    catch (FlaggenException e){
		System.err.println(e.getMessage());
	    }
	}
	else {
	    // Problem: the server sends shit
	    Global.debug(this, "server does not send a game start at game start... fui!");
	}

	registered = true;

    }



    /**
     * Fordert die View auf, eine ActionMessage anzuzeigen
     */
    protected void showActionMessage(String s){
	if (ausgabeView !=null) {
	    ausgabeView.showActionMessage(s);
	}
    }

    protected Roboter getRob(String name){
	return (Roboter) robots.get(name);
    }


    protected void showScout(int chosen, Roboter[] robs) {
	ausgabeView.showScout(chosen,robs);
    }


    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls noetig)
     */
    void showSplash(String s) {
      CAT.debug("showSplash: "+s+ "("+nosplash+")");
	if(!nosplash) {
	    if (splashScreen==null){
		splashScreen = new Splash();
	    }
	    splashScreen.showSplash();
	}

    }

    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls n÷tig)
     */
    private void removeSplash() {
	if(!nosplash) {
	    splashScreen.noSplash();
	}

    }

    private Roboter[] getRoboterArray(){
	Roboter[] robs = new Roboter[robots.size()];
	int i=0;
	Iterator iter = robots.values().iterator();
	while (iter.hasNext()) {
	    robs[i] = (Roboter) iter.next();
	    i++;
	}
	return robs;
    }


    private void registerAtServer() {
	boolean anmeldungErfolg = false;
	int versuche = 0;


	showActionMessage(Message.say("AusgabeFrame","Anmeldung"));
	while ((!anmeldungErfolg)&&(versuche < 3)) {
	    try{
		anmeldungErfolg = kommClient.anmelden2(host,port,name);
	    }
	    catch (KommException kE) {
		CAT.error(kE.getMessage());
		showSplash(Message.say("AusgabeFrame","msplashFehlerAnmeldung"));
		versuche++;
		try {Thread.sleep(3000);} catch (Exception e) {CAT.error(e.getMessage());}
	    }
	}


	if (anmeldungErfolg) {
	    Global.debug(this,"registered for game as new view with name: "+name);
	}
	else {
	    Global.debug(this, "could not register at the server: "+host);
	    showSplash(Message.say("AusgabeFrame","msplashEnde"));
	    try {Thread.sleep(2000);} catch (Exception e) {CAT.error(e.getMessage());}
	    removeSplash();
	}

    }


    /**
     * Zentriert den Robi und folgt ihm.
     */
    public void trackRob (String rName) {
	Roboter r = (Roboter) robots.get(rName);
	trackPos(r.getX(), r.getY());
    }

    public void setTracking( String name ) {
      /** @todo: track the given robot permanently */
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

    public void trackPos (int x, int y, boolean highlight) {
	ausgabeView.showPos(x,y, highlight);
    }

    public AusgabeView getAusgabeView() {
	return ausgabeView;
    }

    private void d(String s){
	Global.debug(this, s);
    }


    private void abmelden() {
      kommClient.abmelden( name );
    }

    protected void quit (boolean keepWatching) {
        CAT.debug("Ausgabe sends quit..");
        abmelden();
        CAT.debug("Ausgabe sets condition for exiting its run method..");
        spielEnde=true;
        CAT.debug("Ausgabe tells the view to propagate quitting..");
        view.quitHumanPlayer(); // Tell the view to tell the HumanPlayer to quit, if there is any
    }
/*
    protected void quit() {
        abmelden();
        spielEnde=true;
        view.setVisible(false);
    }

*/

  protected View getView() {
    return view;
  }


  protected void showStats() {
    if (statsWindow==null) {
      statsWindow = new JFrame(Message.say("AusgabeView", "stats"));
      statsWindow.getContentPane().add(new StatisticPanel(stats));
      statsWindow.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e){
                  statsWindow.setVisible(false);
                }
          });


      statsWindow.pack();
    }

    statsWindow.setVisible(true);
    statsWindow.show();
  }




}



