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
    private Location[] flags;

    private String host, name;
    private int port;
    private boolean spielEnde = false;
    private boolean nosplash = false;
    private boolean registered = false;

    /** the last phase of the current turn */
    private int lastPhase=1;


    // this flag tells us to ignore a "removed from game" message if we
    // requested the removal; in this case we don't want to get any status
    // information like we are doing it in case of a regular "game over"
    // -> see last case in the run()-method and abmelden()
    private boolean quitByMyself = false;
    private boolean mayNotLeave = false;


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
                // waiting for server messages
		kommAntwort = kommClient.warte();
	    }
	    catch (KommFutschException kE) {  // lost network communication with server
		CAT.error("KE: "+kE.getMessage()); return;
	    }
	    catch (KommException ke) { // something went wrong parsing the server's message
		CAT.error("ke: "+ke.getMessage()); return;
	    }
	    // what did the server send?
	    switch (kommAntwort.typ) {
	    case (ClientAntwort.MESSAGE):{ // (user information) messages about stuff that happend;
                                           // for advanced displaying, playing sounds at the right time
                comHandleMessages(kommAntwort);
		break;
	    }
	    case (ClientAntwort.AENDERUNG): {// notify change; something happened (to a robot);
	        try {
                  comHandleNotifyChange(kommAntwort);
	        }
                catch (KommFutschException ke) {
                    CAT.error("ke2: "+ke.getMessage(), ke);
                    return;
                }
                catch (KommException kE) {
                    CAT.error(kE.getMessage(), kE);
                    return;
                }
                break;
	    }

	    case (ClientAntwort.ENTFERNUNG): { // we were removed for some reason, i.e. game is over,
                                               // there was a timeout, we violated some protocol rule
		CAT.info("Game over.");
                comHandleWeWereRemoved(kommAntwort);
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
	   CAT.debug("Server send me: game start.");

	    // ------- fetching the board -----
	    try {
		String[] playerNames = kommClient.getNamen();
		String[] playerColors = kommClient.getFarben();
		Hashtable playerColorHash = new Hashtable(playerColors.length);

                // some magic for setting the robot colors
                Color [] robotsNewColor = initRobotColors(playerColorHash, playerColors) ;

                // Initializing the robots an applying the colors to their visualization
		for (int i=0; i < playerNames.length; i++) {
		    d("Hole Roboterstatus von: "+playerNames[i]);
		    Bot tempRob = kommClient.getRobStatus(playerNames[i]);
                    String h_name = tempRob.getName();
                    Integer h_int = (Integer)playerColorHash.get(h_name);
		    tempRob.setBotVis(h_int.intValue());
		    robots.put(playerNames[i], tempRob);
		}


		initBoard(robotsNewColor);


		if (view == null) {
		    view=new View(ausgabeView);
		}
		else {
                    view.addAusgabeView(ausgabeView);
		    view.makeVisible();
                }

                // fetching initial stats
                initStats();
                // we are done, removing splashscreen
		removeSplash();

		// send OK to server
		kommClient.spielstart();
                // set the viewport to the first flag
		scrollFlag(1);

	    }
	    catch (KommException kE) {
		CAT.error("Ausgabe: Beim Versuch, die Bot zu holen, erhalte ich: "+
				   kE.getMessage());
		return;
	    }
	    catch (FormatException e) {
		System.err.println(e.getMessage());
	    }
	    catch (FlagException e){
		System.err.println(e.getMessage());
	    }
	}
	else {
	    // Problem: the server sends garbage
	    CAT.debug("server does not send a game start at game start... pfui!");
	}

	registered = true;

    }

    private Color [] initRobotColors (Hashtable playerColorHash, String [] playerColors) throws KommException{
       for (int i = 0; i < playerColors.length; i++) {
          if (! playerColors[i].equals("0"))
              playerColorHash.put(playerColors[i],new Integer(i));
       }
       BotVis.initBotVis( playerColorHash );

       Location boardDim = kommClient.getSpielfeldDim();
       boardDimension = new Dimension(boardDim.x,boardDim.y);
       flags = kommClient.getFahnenPos();

       Color[] robotsDefaultColor = BoardView.robocolor;
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

       return robotsNewColor;
    }

    private void initBoard(Color [] robotsNewColor) throws KommException, FormatException, FlagException{
      SimBoard sim = new SimBoard(boardDimension.width,
						    boardDimension.height,
						    kommClient.getSpielfeld(),
						    flags);


      BoardView board = new BoardView(sim,robotsNewColor);
      board.setAutoscrolls( true );
      ausgabeView = new AusgabeView(board,getRoboterArray(),this);
      board.addMouseListener( new MouseAdapter() {
          public void mouseReleased( MouseEvent me ) {
              if( (me.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
                  CAT.debug("mouse clicked in canvas " + me.getPoint() );
                  Location ort = new Location();
                  BoardView brd = (BoardView) me.getSource();
                  brd.point2Ort( me.getPoint(), ort );
                  CAT.debug("corresponding ort is: " + ort );
                  ausgabeView.showPixelPos( me.getPoint().x, me.getPoint().y );
              }
          }
      });
    }


    /**
     * Fordert die View auf, eine ActionMessage anzuzeigen
     */
    protected void showActionMessage(String s){
	if (ausgabeView !=null) {
	    ausgabeView.showActionMessage(s);
	}
    }

    protected Bot getBot(String name){
	return (Bot) robots.get(name);
    }


    protected void showScout(int chosen, Bot[] robs) {
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

    private void removeSplash() {
	if(!nosplash) {
	    splashScreen.noSplash();
	}

    }

    private Bot[] getRoboterArray(){
	Bot[] robs = new Bot[robots.size()];
	int i=0;
	Iterator iter = robots.values().iterator();
	while (iter.hasNext()) {
	    robs[i] = (Bot) iter.next();
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
	    CAT.debug("registered for game as new view with name: "+name);
	}
	else {
	    CAT.debug("could not register at the server: "+host);
	    showSplash(Message.say("AusgabeFrame","msplashEnde"));
	    try {Thread.sleep(2000);} catch (Exception e) {CAT.error(e.getMessage());}
	    removeSplash();
	}

    }


    /**
     * Zentriert den Robi und folgt ihm.
     */
    public void trackRob (String rName) {
	Bot r = (Bot) robots.get(rName);
	trackPos(r.getX(), r.getY());
    }

    public void setTracking( String name ) {
      /** @todo: track the given robot permanently */
    }


    /**
     * Zentriert die angegebenen Flagge
     */
    public void scrollFlag (int nr) {
	if (nr > 0 && nr <= flags.length) {
	    trackPos(flags[(nr-1)].getX(),flags[(nr-1)].getY());
	}
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

    private void initStats() {
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
         CAT.error(ke.getMessage(), ke);
         CAT.error("KommException occured!");
         CAT.error("Failed to initialize Statistics-Menu!!!");
         CAT.error("!!YOU BETTER DO NOT CLICK ON THE STATISTICS MENU !!!");
       }
    }


    private void abmelden() {
     /* while (mayNotLeave) {
        synchronized (this) {
          try {
            wait();
          }
          catch(InterruptedException ie) {
            CAT.error("interrupted");
            CAT.error(ie.getMessage(), ie);
          }
        }
      }*/
      CAT.debug("Ausgabe sets condition(s) for leaving its ru() method");
      quitByMyself = true;
      spielEnde=true;
      CAT.debug("Ausgabe deregisters from server");
      kommClient.abmelden( name );
    }

    protected void quit (boolean keepWatching) {
        abmelden();
        CAT.debug("Ausgabe tells the view to propagate quitting..");
        view.quitHumanPlayer(); // Tell the view to tell the HumanPlayer to quit, if there is any
    }


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
     if (statsWindow.getState()==JFrame.ICONIFIED) {
        CAT.debug("iconified");
        statsWindow.setState(JFrame.NORMAL);
      }
    statsWindow.toFront();
    statsWindow.setVisible(true);
    statsWindow.show();
  }

  private void comHandleWeWereRemoved(ClientAntwort kommAntwort) {
    // we asked the server to be removed..
    if (quitByMyself) { // hendrik was here..
      CAT.debug("server seems to confirm our request for quitting the game");
      CAT.debug("this is great because chances are that server is not deadlocked now! :)");
      return;
    }

    spielEnde = true; // for leaving the main loop of the run()-method

    try {
        String[] spielErgebnis = kommClient.getSpielstand();
        if (spielErgebnis != null) { // were we removed because the game is over?
            CAT.debug("We have "+spielErgebnis.length+" winners");
            ausgabeView.showWinnerlist(spielErgebnis);
        }
        else
            CAT.debug("No winner exists");
    }
    catch (KommException e) {
        CAT.error(e.getMessage(), e);
        return;
    }
    try {
        Thread.sleep(2000);
    }
    catch (InterruptedException e) {
        CAT.error("Ausgabe: Interrupted by "+e.toString(), e);
    }
    kommClient.spielstart(); // I think that meant sending an "ok" to server
                             // to confirm that we are done

  }

  private void comHandleNotifyChange (ClientAntwort kommAntwort) throws KommException {
      if (CAT.isDebugEnabled()) {
        CAT.debug("Server send me: change occured.");
        // ------- get changes  -----------
        CAT.debug(kommAntwort.namen.length+" robs have been updated.");
      }
       // getting the names of the players that have some changed values
          String[] playerNames = kommAntwort.namen;
          for (int i=0; i < playerNames.length; i++) {
              robots.put(playerNames[i],kommClient.getRobStatus(playerNames[i]));//updating the my internal robots
        }
        // telling the display to update itself with the new robot values
        ausgabeView.showUpdatedRobots(getRoboterArray());

        try { // wait a little for the display to update
            Thread.sleep(100);
        }
        catch (Exception e) {
            CAT.error(e.getMessage(), e);
        }

        // --------- get other information from the server
        Status[] stArray = kommClient.getSpielstatus();
        if (stArray != null) {
            if (stArray[0].aktPhase != lastPhase) {
                // its ok to do the reset on each phase change,
                // will not do anything if all are reset
                ausgabeView.resetProgrammingLEDs();
                view.showGameStatusMessage(Message.say("AusgabeFrame","phase")+ " " + stArray[0].aktPhase);
                lastPhase = stArray[0].aktPhase;
            }
        }
        // --------- has somebody already reached the final flag?
        String[] winnerStateList = kommClient.getSpielstand();
        ausgabeView.showWinnerState(winnerStateList);

      // tell the server that we finished our stuff and he can send the next notify change
      kommClient.aenderungFertig();
  }

  private void comHandleMessages(ClientAntwort kommAntwort){
      CAT.debug("Server send me: "+kommAntwort.namen[0]);
      // getting parts of the message
      String[] tmpstr=new String[kommAntwort.namen.length-1];
      for (int i=0;i<tmpstr.length;i++)
          tmpstr[i]=kommAntwort.namen[i+1];
      // check the kind of message
      String msgId = kommAntwort.namen[0];

      // check whether we have to display an information message in the transparent
      // chat- and actionlog on the bottomline of the board
      if (!(msgId.substring(0,5).equals(de.botsnscouts.comm.MessageID.AUSWERTUNG))) {
        if (CAT.isDebugEnabled())
          CAT.debug("kommAntowrtnamen[0] ist: "+msgId+ "tmpstr ist: "+tmpstr[0]);
        // display the message in the statusbar
        showActionMessage(Message.say("MSG",msgId,tmpstr));
     }

     if(msgId.startsWith(MessageID.PROG_DONE)) {
        comMsgHandleProgrammingDone(kommAntwort);
     }
     else if (msgId.equals(MessageID.SOMEONE_QUIT) || (msgId.startsWith(MessageID.BOT_REMOVED))){
        comMsgHandleRobotRemoved(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_LASER)){ // robots shooting
        comMsgHandleRobotLaser(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.CHAT)){
        comMsgHandleChat(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_IN_PIT)) {// robot fell into a pit
        comMsgHandleRobotFellIntoPit(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.FLAG_REACHED)) {
        comMsgHandleRobotReachedFlag(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.LAST_PROG)) {
        comMsgHandleLastProgrammerFinished(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BORD_LASER_SHOT)){ // boardlaser shooting
        comMsgHandleBoardLaser(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_CRUSHED)){
        comMsgHandleRobotCrushed(kommAntwort);
     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.WISE_USED)){
        comMsgHandleSomeoneAskedWisenheimer(kommAntwort);
     }

    acknowledgeMessage();
  }
  /** send response to the server that we got the message
   and did all the stuff we wanted to do, so the server can
   send the next message
  */
  private void acknowledgeMessage(){
    try{
        kommClient.acknowledgeMsg();
    }
    catch (KommFutschException ke) {
        CAT.error("ke2: "+ke.getMessage(), ke);
    }
    catch (KommException kE) {
        CAT.error(kE.getMessage(), kE);
    }
  }

  private void comMsgHandleProgrammingDone(ClientAntwort kommAntwort) {
    Bot r1 = getBot(kommAntwort.namen[1]);
    ausgabeView.notifyBotProgrammingDone(r1);
  }

   private void comMsgHandleRobotRemoved(ClientAntwort kommAntwort) {
    try {
      Bot actual = kommClient.getRobStatus(kommAntwort.namen[1]);
      robots.put(kommAntwort.namen[1], actual);
      ausgabeView.showUpdatedRobots(getRoboterArray());

    }
    catch (KommFutschException ke) {
      CAT.error(ke.getMessage(), ke);
    }
    catch (KommException kE){
      CAT.error(kE.getMessage(), kE);
    }
    try {
      Thread.sleep(100);
    }
    catch (Exception e) {
      CAT.error(e.getMessage(), e);
    }
  }

  private void comMsgHandleRobotLaser(ClientAntwort kommAntwort) {
   CAT.debug("Got message telling "+kommAntwort.namen[1]+" shot "
			      +kommAntwort.namen[2]+".");
    Bot r1 =(Bot )robots.get(kommAntwort.namen[1]);
    Bot r2 =(Bot )robots.get(kommAntwort.namen[2]);
    // updating statistics
    Stats actualStats=stats.getStats(r1.getName());
    actualStats.incHits();
    if (r2.getDamage()>=10) // was the robot r2(hit) killed by r1?
        actualStats.incKills();
    actualStats=stats.getStats(r2.getName());
    actualStats.incDamageByRobots();
    // paint animation and play sounds
    ausgabeView.showRobLaser(r1, r2);
  }

  private void comMsgHandleChat(ClientAntwort kommAntwort) {
     SoundMan.playSound(SoundMan.MESSAGE);
  }

  private void comMsgHandleRobotFellIntoPit(ClientAntwort kommAntwort) {
     SoundMan.playSound(SoundMan.PIT);
  }

  private void comMsgHandleRobotReachedFlag(ClientAntwort kommAntwort) {
      SoundMan.playSound(SoundMan.FLAG_REACHED);
  }

  private void comMsgHandleLastProgrammerFinished (ClientAntwort kommAntwort) {
      Stats actualStats = stats.getStats(kommAntwort.namen[1]);
      actualStats.incWasSlowest();
  }

   private void comMsgHandleBoardLaser(ClientAntwort kommAntwort) {
       // get damaged robot
      Bot r1= (Bot )robots.get(kommAntwort.namen[1]);
      Location r1Pos = r1.getPos();

      // updating statistics for the robot hit
      Stats actualStats=stats.getStats(r1.getName());
      actualStats.incDamageByBoard();

      // get the origin of the laser (position and other stuff
      // needed for animation)
      Location laserPos = new Location(0,0);
      int facing=-1;
      int strength=-1;
      try {
          strength   = Integer.parseInt(kommAntwort.namen[2]);
          laserPos.x = Integer.parseInt(kommAntwort.namen[3]);
          laserPos.y = Integer.parseInt(kommAntwort.namen[4]);
          facing     = Integer.parseInt(kommAntwort.namen[5]);
      }
      catch (NumberFormatException nfe) {
          CAT.error("Ausgabe: BoardLaser: NumberFormatException:", nfe);
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


  private void comMsgHandleRobotCrushed (ClientAntwort kommAntwort) {
      SoundMan.playSound(SoundMan.CRUSHED);
  }

  private void comMsgHandleSomeoneAskedWisenheimer(ClientAntwort kommAntwort) {
      SoundMan.playSound(SoundMan.BOO);
      Bot r1= (Bot )robots.get(kommAntwort.namen[1]);
      Stats actualStats=stats.getStats(r1.getName());
      actualStats.incAskWisenheimer();
  }



}



