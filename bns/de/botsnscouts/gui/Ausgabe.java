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

    // no comment here ;)
    //LaserHack laserHack = new LaserHack();

    /** Turns animation of robot movement on or off;
     *  visibility is proteced (not private), because BoardView needs to know about that, too.
     */
    protected static boolean enableRobMoveAnimation = false;

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
        initSpecialMessagesSet();

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


  private Bot getBotDataFromServer (String robotName) throws KommException{
    return kommClient.getRobStatus(robotName);
  }



  private void updateBoardView() {
      // telling the display to update itself with the new robot values
      ausgabeView.showUpdatedRobots(getRoboterArray());

  }


  /** returns true if the state of the robot <code>robotNew</code>
   *  has changed from alive to dead or dead to alive.
   */
  /*private boolean hasCrossedAcheron (Bot robotNew) {
    Bot old = (Bot) robots.get(robotNew.getName());
    int oldD = old.getDamage();
    int newD = robotNew.getDamage();
    if (CAT.isDebugEnabled()) {
      CAT.debug("checking whether "+robotNew.getName()+" has crossed the border "
                +"between life and death:");
      CAT.debug("old damagepoints: "+oldD+"\tnew damage points: "+newD);

    }
    if (oldD>9 && newD<10)
      return true;
    else if (newD>9 && oldD<10)
      return true;
    else
      return false;

  }
*/



  private void comHandleNotifyChange (ClientAntwort kommAntwort) throws KommException {
          boolean updateDisplayMakesSense = false;
       // getting the names of the players that have some changed values
          String[] playerNames = kommAntwort.namen;
          Bot [] tmp = new Bot[playerNames.length];
          for (int i=0; i < playerNames.length; i++) {
              Bot newBotValues = getBotDataFromServer(playerNames[i]);
              tmp [i] = newBotValues;
          }
          processNTC(tmp, kommAntwort.messageSequenceNumber);
              /*if (hasCrossedAcheron(newBotValues)
                  || laserHack.isDeathUnhandled(newBotValues)) { // check whether this notify change
                                                                 // indicates that a robot was destroyed

                  if (laserHack.receivedDeathReasonMessage(newBotValues)) { // we already got a message
                                                             // about the death reason of this
                                                             // bot and we might have shown a
                                                             // possible animation; so we can
                                                             // remove the bot from the
                                                             // display now
                     robots.put(playerNames[i],newBotValues);
                     // and finally: don't forget to reset the information for
                     // the next destruction of the bot
                     laserHack.resetDeathState(newBotValues);
                  }
                  else { // We know that "newBotValues" was destroyed, but we did not
                         // yet get a message for a possible animation of the destruction.
                         // So we will not update the bot, but save this information so
                         // that the message handling methods know that they have to
                         // update the display if they get a message that indicates
                         // the destruction of this robot
                    laserHack.setDeathNotificationArrived(newBotValues);
                    if (CAT.isDebugEnabled()) {
                      CAT.debug("LASER HACK ACTIVE:");
                      CAT.debug("\tGot notify change for a destroyed robot, "
                                +"but no message about it");
                      CAT.debug("=> I will skip updating this robot until a message"
                               +" about the reason for the destruction arrives");
                    }
                  }

              }
              else { // reason for notify change was not the destruction of the bot,
                     // so we can safely update it
                robots.put(playerNames[i],newBotValues);//updating the my internal robots

                // something that we are allowed to show happend:
                updateDisplayMakesSense = true;
              }
        }

        // if we got an information that we are allowed to display we will
        // do so; otherwise we don't have to waste time upating no changes
        if (updateDisplayMakesSense)
          updateBoardView();

        */

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


  // now some methods for fixing an issue that is caused by using the sequencer:
  // <SEQUENCER-FIX>
  /** contains messages that have special handler methods that also take
   *  care about displaying them in the chatpane.
   */
  private HashSet specialMessages = new HashSet();

  /** Add all message ids to <code>specialMessages</code> that have a special
   *  handler method
   */
  private void initSpecialMessagesSet(){
     specialMessages = new HashSet();

     specialMessages.add(MessageID.BORD_LASER_SHOT);
     specialMessages.add(MessageID.CHAT);
     specialMessages.add(MessageID.LAST_PROG);
     specialMessages.add(MessageID.PROG_DONE);
     specialMessages.add(MessageID.BOT_CRUSHED);
     specialMessages.add(MessageID.BOT_IN_PIT);
     specialMessages.add(MessageID.BOT_LASER);
     specialMessages.add(MessageID.FLAG_REACHED);
     specialMessages.add(MessageID.BOT_REMOVED);
     specialMessages.add(MessageID.WISE_USED);

     // some deprecated messages that should not be animated but also not
     // start with "mAusw" and not also be displayed..
      specialMessages.add(MessageID.SIGNAL_ACTION_START);
      specialMessages.add(MessageID.SIGNAL_ACTION_STOP);

  }

  private boolean isMessageSpecial(String msgid) {
    return specialMessages.contains(msgid);
  }

  private boolean isActionToBeDisplayedInInfopanelOnly(String msgid){
    // the message must not start with MesssageID.AUSW
    boolean condition1 = !(msgid.substring(0,5).equals(de.botsnscouts.comm.MessageID.AUSWERTUNG));

    return condition1 && (!isMessageSpecial(msgid));

  }

  //</SEQUENCCER-FIx>

  /** @todo ?caching of message-Actions?
   *
   */
  private void comHandleMessages(final ClientAntwort kommAntwort){
      CAT.debug("Server send me: "+kommAntwort.namen[0]);
      int size = kommAntwort.namen.length;

      // getting parts of the message
      String[] tmpstr=new String[size-1];
      for (int i=0;i<tmpstr.length;i++)
          tmpstr[i]=kommAntwort.namen[i+1];
      // check the kind of message
      String msgId = kommAntwort.namen[0];

      // check whether we have to display an information message in the transparent
      // chat- and actionlog on the bottomline of the board:
      // don't show if message is a "mAusw*"-message or it
      // is a SIGNAL_ACTION_[START|STOP]-message


      final String displayString;
      if (msgId.equals(MessageID.SIGNAL_ACTION_STOP) // to avoid resource-not-found warning
         ||msgId.equals(MessageID.SIGNAL_ACTION_START)){
        displayString = "";
      }
      else
       displayString = Message.say("MSG",msgId,tmpstr);


      if (isActionToBeDisplayedInInfopanelOnly(msgId)) {
        //  if (CAT.isDebugEnabled())
         //    CAT.debug("kommAntowrtnamen[0] ist: "+msgId+ "tmpstr ist: "+tmpstr[0]);
          // display the message in the statusbar

          sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                  showActionMessage(displayString);
              }
          });
     }
     else if (msgId.equals(MessageID.SIGNAL_ACTION_START)) {
       sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                  comMsgHandleActionStart(kommAntwort);
              }
        });
     }
     else if (msgId.equals(MessageID.SIGNAL_ACTION_STOP)) {
        sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                 comMsgHandleActionStop(kommAntwort);
              }
        });
     }
     else if(msgId.startsWith(MessageID.PROG_DONE)) {
        sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                  showActionMessage(displayString);
                  comMsgHandleProgrammingDone(kommAntwort);
              }
         });

     }
     else if (msgId.equals(MessageID.SOMEONE_QUIT) || (msgId.startsWith(MessageID.BOT_REMOVED))){
         sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                  showActionMessage(displayString);
                  comMsgHandleRobotRemoved(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_LASER)){ // robots shooting
      sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                  showActionMessage(displayString);
                  comMsgHandleRobotLaser(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.CHAT)){
        sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                 showActionMessage(displayString);
                 comMsgHandleChat(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_IN_PIT)) {// robot fell into a pit
         sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleRobotFellIntoPit(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.FLAG_REACHED)) {
      sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleRobotReachedFlag(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.LAST_PROG)) {
       sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleLastProgrammerFinished(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BORD_LASER_SHOT)){ // boardlaser shooting
        sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleBoardLaser(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.BOT_CRUSHED)){
         sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleRobotCrushed(kommAntwort);
              }
        });

     }
     else if (msgId.equals(de.botsnscouts.comm.MessageID.WISE_USED)){
        sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                showActionMessage(displayString);
                comMsgHandleSomeoneAskedWisenheimer(kommAntwort);
              }
        });

     }
     else if (msgId.equals(MessageID.BOT_MOVE)) {
      sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
                if (enableRobMoveAnimation)
                  comMsgHandleRobotMove(kommAntwort);
              }
        });

     }
     else { // all the stuff that starts with "mAusw" (==MessageId.AUSWERTUNG)
       sequenzer.addAndDoAction(new UpdateActionAdapter(kommAntwort.messageSequenceNumber){
              public void invoke(){
               CAT.debug("got a message without specifc action; nothing to invoke");
              }
        });
     }

    acknowledgeMessage();
  }
  /** send response to the server that we got the message
   and did all the stuff we wanted to do, so the server can
   send the next message
  */
  private void acknowledgeMessage(){
    try{
      //  synchronized (lock) {
          kommClient.acknowledgeMsg();
      //    ++nextMsg;
      //    lock.notifyAll();
      //  }
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
    try {
      String targetName = kommAntwort.namen[2];
      String sourceName = kommAntwort.namen[1];
      Bot sourceBot =(Bot )robots.get(sourceName);
      Bot targetBot =(Bot )robots.get(targetName);
      // if it was killed and a notify changed was already received,
      // robots was not updated => targetBot.getDamage is <10
      Bot newTargetBot = (Bot) getBotDataFromServer(targetName);
      Location pos = newTargetBot.getPos();
      boolean targetDestroyed = newTargetBot.getDamage()>=10 ||
                                (pos.x==0 && pos.y==0);

      if (CAT.isDebugEnabled())
          CAT.debug("Got message telling "+sourceName+" shot "+targetName+".");

      // updating statistics
      Stats sourceStats=stats.getStats(sourceName);
      sourceStats.incHits();
      if (targetDestroyed)
          sourceStats.incKills();
      Stats targetStats=stats.getStats(targetName);
      targetStats.incDamageByRobots();

      // paint animation and play sounds
      CAT.debug("LASER: "+sourceBot.getName()+" -------------> "+targetBot.getName());
      ausgabeView.showRobLaser(sourceBot, targetBot);

      // now some management of a dirty hack to synchronize laser painting and
      // removing the killed robots from the display..
     // if (targetDestroyed)
     //   updateLaserAnimationHackMessageStuff(newTargetBot, true);
    }
    catch (KommException ke) {
      CAT.error(ke.getMessage(), ke);
    }
  }

  /** Has to be called if we receive a message about a bot being destroyed.
   *  As far as I know this is may concern messages for the following events:
   *  a) bot fell into pit       [done]
   *  b) bot shot another bot    [done]
   *  c) boardlaser shot a bot   [done]
   *  d) bot was crushed         [done]
   *  e) (???) robot removed from game (???) [--]
   */
  /*private void updateLaserAnimationHackMessageStuff(Bot bot, boolean botIsUpdated){

    // did we already get a notify change for this destruction?
    if (laserHack.receivedDeathNotification(bot)) {
        // reset state for next destruction
        laserHack.resetDeathState(bot);
        try {
          // updating the gui, because comHandleNotifyChange() skipped the updating
          // of this robot as it noticed that the bot was killed but no message
          // about the destruction of the bot was found;
          // => now it's up to this method to care for removing the destroyed bot
          //    from the board (or, in general, update it) , because NOW we can be
          //    sure that all animations (laser shooting!) were shown using the last
          //    valid board position of the bot
          String botname = bot.getName();
          Bot newBot;
          if (botIsUpdated)
            newBot = bot;
          else
            newBot = getBotDataFromServer(botname);
          robots.put(botname, newBot);
          updateBoardView();
        }
        catch (KommException ke){
          CAT.error(ke.getMessage(), ke);
        }
     }
     else { // if we do not yet have a notify change about this destruction,
            // we will save the information that we got a message about it and
            // have shown all animation we wanted to show.
            // This will give a hint to comHandleNotifyChange() that it can
            // update (remove) the destroyed robot.
      if (CAT.isDebugEnabled()) {
        CAT.debug("LASER HACK ACTIVATED the other way around:");
        CAT.debug("got message about destruction of a bot before notify change");
        CAT.debug("=> will indicate to notify change handler method that it is"
                  +" allowed to update the bot, because all possible animations"
                  +" about this event have been shown");
      }
      laserHack.setDeathReasonArrived(bot);
     }

  }
  */
  private void comMsgHandleChat(ClientAntwort kommAntwort) {
     SoundMan.playSound(SoundMan.MESSAGE);
  }

  private void comMsgHandleRobotFellIntoPit(ClientAntwort kommAntwort) {
     String botname = kommAntwort.namen[1];
     Bot bot = (Bot) robots.get(botname);
     //updateLaserAnimationHackMessageStuff(bot, false);

     SoundMan.playSound(SoundMan.PIT);
  }

  private void comMsgHandleRobotReachedFlag(ClientAntwort kommAntwort) {
      SoundMan.playSound(SoundMan.FLAG_REACHED);
  }

  private void comMsgHandleLastProgrammerFinished (ClientAntwort kommAntwort) {
      Stats actualStats = stats.getStats(kommAntwort.namen[1]);
      actualStats.incWasSlowest();
  }

   private void comMsgHandleBoardLaser(final ClientAntwort kommAntwort) {
        CAT.debug("doBoardLaser");

     //try {
         // get damaged robot
          String targetName = kommAntwort.namen[1];
        Bot targetBot = (Bot )robots.get(targetName);
       // Bot newTargetBot = (Bot) getBotDataFromServer(targetName);

        Location targetPos = targetBot.getPos();
        // due to laser hack, targetBot might not have the current damagecount
        //boolean targetKilled = newTargetBot.getDamage()>=10;

        // updating statistics for the robot hit
        Stats targetStats=stats.getStats(targetName);
        targetStats.incDamageByBoard();

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
        if ((laserPos!=null)&&(facing>=0)&&(targetPos!=null)&&(strength>=0)){
            ausgabeView.showBoardLaser(laserPos, facing, strength, targetPos);
        }
        else {
            if (CAT.isDebugEnabled()){
              CAT.error("Ausgabe: unable to calculate Laseranimation: ");
              CAT.debug("laserPos: "+laserPos);
              CAT.debug("facing: "+facing);
              CAT.debug("r1Pos: "+targetPos);
              CAT.debug("strength: "+strength);
            }
        }
       // if (targetKilled) {
       //   updateLaserAnimationHackMessageStuff(newTargetBot, true);
        //}
   //  }
   //  catch (KommException ke) { // if that happens, crappy robot data and animations
   //                             // aren't our biggest problems..
   //   CAT.error(ke.getMessage(), ke);
   //  }


  }


  private void comMsgHandleRobotCrushed (ClientAntwort kommAntwort) {
      String botName = kommAntwort.namen[1];
      Bot bot = (Bot) robots.get(botName);
      //updateLaserAnimationHackMessageStuff(bot, false);

      SoundMan.playSound(SoundMan.CRUSHED);
  }

  private void comMsgHandleSomeoneAskedWisenheimer(ClientAntwort kommAntwort) {
      SoundMan.playSound(SoundMan.BOO);
      Bot r1= (Bot )robots.get(kommAntwort.namen[1]);
      Stats actualStats=stats.getStats(r1.getName());
      actualStats.incAskWisenheimer();
  }


  //private Object lock = new Object();
  //private int delay = 300;
  //private boolean updating = false;
  //private Vector history = new Vector();
  //private boolean inAction = false;

  private void comMsgHandleActionStart(ClientAntwort ca) {

        CAT.debug("got an action start");

  /*  synchronized (lock) {
      inAction=true;
      //lock.notifyAll();
    }
    */
  }

  private void comMsgHandleActionStop(ClientAntwort ca) {
        CAT.debug("got an action stop");

  /*  CAT.debug("action stop");
    updating = true;
     synchronized (lock) {

      int l = history.size();
      CAT.debug("history size: "+l);
       for (int i=0;i<l;i++){
        CAT.debug("doing history: "+i);
         Bot[] tmp = (Bot []) history.elementAt(0);
         for (int j=0;j<tmp.length;j++){
          robots.put(tmp[j].getName(), tmp[j]);
         }
         history.removeElementAt(0);
         updateBoardView();
         synchronized (this) {
            try {sleep(300);}catch(InterruptedException ie){CAT.error(ie.getMessage(), ie);}
          }
       }
       updating = false;
       inAction = false;
       lock.notifyAll();
     }
     */
  }

    private void comMsgHandleRobotMove(ClientAntwort kommAntwort) {

      String robname = kommAntwort.namen[1];
      Bot r = (Bot) robots.get(robname);
      String direction = kommAntwort.namen[2];
      if (CAT.isDebugEnabled()){
        CAT.debug("Got robot move message for robot \""+robname+"\"");
        CAT.debug("Direction: "+direction);
      }
      try {
        int directionInt = Integer.parseInt(direction);
        ausgabeView.animateRobMove(r, directionInt);
      }
      catch (NumberFormatException nfe){
        CAT.error("Failed to convert direction for robot \""
                  +robname+"\"from String to int!");
        CAT.error("String was: \""+direction+"\"");

      }
  }

 // private int lastMsgProcessed = -1;
 // private int nextMsg=0;

  private Sequenzer sequenzer = new Sequenzer();
  private void processNTC(final Bot [] updates, int msgNum){
    CAT.debug("process NTC id="+msgNum);
    sequenzer.addAndDoAction(new UpdateActionAdapter(msgNum){
      public void invoke(){
         CAT.debug("NTC invoked!");
         for (int j=0;j<updates.length;j++)
              robots.put(updates[j].getName(), updates[j]);
         updateBoardView();

      }
    });
    /**
   synchronized (lock) {
      if (inAction) {
        CAT.debug("in action");
        while (updating)
          try {wait();}catch(InterruptedException ie){CAT.error(ie.getMessage(),ie);}
        history.add(updates);
      }
      else {
         CAT.debug("not in action");
         if (msgNum>-1 && msgNum == nextMsg) {
           for (int j=0;j<updates.length;j++){
              robots.put(updates[j].getName(), updates[j]);
           }
           updateBoardView();
           nextMsg++;
         }
         else
           history.add(updates);

      }
    }
  */
  }

  class Sequenzer {
    private int nextMsg;
    private TreeSet messages = new TreeSet();

    public Sequenzer() {
      nextMsg=1;
    }

    public synchronized void addAndDoAction(UpdateActionAdapter action){
      int num = action.seqNum;

      if (num<0){
        CAT.debug("invoking action without a sequence number");
        action.invoke();
        return;
      }
      else if (num<nextMsg) {
        // this case might look quite senseless as it can't happen as far as I can
        // see - but one time one of the actions appeared again in messages and
        // so all execution stopped because messages.firsts() hat sequence number
        // that was smaller than numSeq;
        CAT.error("got an action that should have been executed already: #"+num);
        CAT.error("invoking it again, but don't add it to the queue!");
        action.invoke();
      }
      else {
        CAT.debug("adding action #"+num+" to queue");
        messages.add(action);
      }
      if (CAT.isDebugEnabled()) {
        CAT.debug("queue before invokeAsLongAsPossible:");
        dump();
        invokeAsLongAsPossible();
        CAT.debug("invoked as long as possible; actions left: "+messages.size());
        CAT.debug("queue after invokeAsLongAsPossible:");
        dump();
      }
      else {
        invokeAsLongAsPossible();
      }
    }

    private synchronized void invokeAsLongAsPossible(){
      if (messages.isEmpty())
        return;
      UpdateActionAdapter smallest = (UpdateActionAdapter) messages.first();
      int actualId = smallest.seqNum;
      while (actualId == nextMsg) {
        CAT.debug("INVOKING MESSAGEACTION #"+actualId);
        smallest.invoke();
        ++nextMsg;
        messages.remove(smallest);
        if (!messages.isEmpty()){
          smallest= (UpdateActionAdapter)messages.first();
          actualId=smallest.seqNum;
        }
        CAT.debug("actual="+actualId+"\tnext="+nextMsg);
        // else nextMsg!=actualId -> exiting loop
      }


    }

    private synchronized void dump() {
      CAT.debug("nextMesg="+nextMsg);
      CAT.debug("ids: ");
      StringBuffer sb= new StringBuffer();
      Iterator it = messages.iterator();
      while (it.hasNext())
        sb.append(((UpdateActionAdapter) it.next()).seqNum).append(", ");
      CAT.debug(sb.toString());
    }

  }

  class UpdateActionAdapter implements Comparable {
    int seqNum;

    public UpdateActionAdapter(int sequenzNumber){
      seqNum = sequenzNumber;
    }

    public void invoke(){

    }

    public int compareTo(Object o){
      UpdateActionAdapter t = (UpdateActionAdapter) o;
      int tnum = t.seqNum;
      if (seqNum==t.seqNum) // not needed for sorting as seqnums will be unique;
                            // but this method is used for general comparision, i.e.
                            // without this case Sequenzer's messages.remove(Object) won't work
        return 0;
      else if (seqNum<t.seqNum)
        return -1;
      else
        return 1;
    }

  }

}



