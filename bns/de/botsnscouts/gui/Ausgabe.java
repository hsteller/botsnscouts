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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFrame;

import org.apache.log4j.Category;


import de.botsnscouts.BotsNScouts;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.comm.ClientAntwort;
import de.botsnscouts.comm.KommClientAusgabe;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.comm.KommFutschException;
import de.botsnscouts.comm.MessageID;
import de.botsnscouts.server.RegistrationException;
import de.botsnscouts.start.JoiningGameFailedException;
import de.botsnscouts.util.BNSClientThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Registry;
import de.botsnscouts.util.SoundMan;
import de.botsnscouts.util.Stats;
import de.botsnscouts.util.StatsList;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.PaintPanel;

/**
 * Ausgabe("view")
 * 
 * Responsible for the view logic - the communication with the server about 
 * stuff to display; every human player needs one, as the class "HumanPlayer" per se
 * works on the same level as the AI bots and the AIs don't care about stuff that
 * happens during phase evaluation.
 * Think of an "Ausgabe" as a spectator (in fact, it can connect to a server and 
 * let you watch the game without participating); communication that requires 
 * the interaction of a participating player will be handled by a <code>HumanPlayer</code> 
 *  and its <code>HumanView</code> - though they are at the moment still more connected
 *  as one might like.
 * The communication of an Ausgabe(view) is (more or less) "one-way":
 * The server tells an "Ausgabe" about things that happen in the game and
 * the Ausgabe reacts to the server's messages by calling a big bunch of handler 
 * methods for displaying stuff. Since the server sometimes sends a simple "notify change"(NTC) 
 *  without actually telling what has happend (only which bot(s) were affected) ,
 *  the Ausgabe often sends requests to get the current state of robots. 
 * 
 * The actual displaying is done in its AusgabeView and AusgabeView's BoardView.
 * 
 * 
 * @version $Id$
 */
public class Ausgabe extends BNSClientThread {
    static Category CAT = Category.getInstance(Ausgabe.class);

    // for communication with server
    private KommClientAusgabe kommClient;
 

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

    private boolean spielEnde = false;

    private int currentPhase = -1;

    // this flag tells us to ignore a "removed from game" message if we
    // requested the removal; in this case we don't want to get any status
    // information like we are doing it in case of a regular "game over"
    // -> see last case in the run()-method and abmelden()
    private boolean quitByMyself = false;

    /**
     * A dummy that is used to map various message types to one single AbstractMessageAction
     * that does nothing else than displaying the message in the transparent chatpane
     */
    private static final String DUMMY_MESSAGE_ID_DISPLAY_STRING_ONLY = "just show this message in chatpane";

    /**
     * We will receive "notify changes" (NTC) and more detailed information messages
     * about what is happening in a f***ed up order.
     * To avoid display errors (i.e. removing a killed robot before it was shot),
     * we will stuff all messages into this sequencer that will know (because of
     * sequence numbers)  when it is time to act.
     */
    private MessageSequencer sequencer;
    
    
    /**
     * Start a new Ausgabe with a View. It will start displaying Message 1.
     * Used by the HumanPlayer. 
    */
    protected Ausgabe(String host, int port, boolean nosplash, View v) {
        this(host, port, nosplash, false);
        this.view = v;
    }

  

    /**
     * Start a new stand-alone Ausgabe. It will start displaying Messages immediately, discarding
     * earlier ones that arrive out-of-sequence.
     *
     * Used by start.Launcher to start a view.
     */
    public Ausgabe(String host, int port, boolean nosplash) {
        this(host, port, nosplash, true);
    }

    private Ausgabe(String host, int port, boolean nosplash, boolean lateInit) {
        super("View["+KrimsKrams.randomName()+"]", Registry.CLIENT_TYPE_VIEW, host, port);       
       // this.nosplash = nosplash;
        if (!nosplash) {
            showSplash(Message.say("AusgabeFrame", "msplashWarte"));
        }
        sequencer = new MessageSequencer(lateInit);                            
        kommClient = new KommClientAusgabe();
        initSpecialMessagesSet();
        initMessageToActionMapping();
    }
    
    /***  Don't call directly (it assumes the view is already registered at the server),
    *  but if you thought about calling it (or "start()"):
    *  --waves hand -- "These aren't the methods you are looking for."
    *   But you might want to call/look at @see BnsClientThread.bnsStart()
    */
    public void run() {      
        
        boolean ok = waitForGameStartAndInitGameData();
        if (!ok){
            return;
        }
        // ---- entering game  ---------
        while (!spielEnde && !isShutDown()) {
            ClientAntwort  kommAntwort=null;
            try {
                // waiting for server messages
              kommAntwort = kommClient.warte();
       
            } catch (Exception ke) { // something went wrong parsing the server's message
                CAT.error(ke.getMessage(),ke);
                if (!isShutDown()){
                    shutdown(true);
                }
            }
            // what did the server send?
            
            switch (kommAntwort.typ) {
                case (ClientAntwort.MESSAGE):
                { // (user information) messages about stuff that happend;
                    // for advanced displaying, playing sounds at the right time
                    comHandleMessages(kommAntwort);
                    break;
                }
                case (ClientAntwort.AENDERUNG):
                {// notify change; something happened (to a robot);
                    try {
                        comHandleNotifyChange(kommAntwort);
                    } catch (Exception ke) {
                        CAT.error(ke.getMessage(),ke);
                        if (!isShutDown()){
                            shutdown(true);
                        }
                        
                    }
                    break;
                }
                
                case (ClientAntwort.ENTFERNUNG):
                { // we were removed for some reason, i.e. game is over,
                    // there was a timeout, we violated some protocol rule
                    CAT.info("Game over.");
                    try {
                        comHandleWeWereRemoved(kommAntwort);
                    }
                    catch (Exception e){
                        CAT.error(e.getMessage(), e);
                        if (!isShutDown()){
                           shutdown(true);
                        }
                    }
                }
            }

        }
        CAT.debug("Ausgabe reached end of its run method");
        CAT.debug("now waiting for signal that I may leave the method");
        waitBlockingUntilShutdownCalled();
        CAT.debug("Ausgabe leaves run");
        
        
    }


    public boolean sendRegistrationRequestOnce(String host, int port) throws KommException, RegistrationException{
       return kommClient.anmelden2(host, port, getName());
    }
           
    private boolean waitForGameStartAndInitGameData(){
        ClientAntwort kommAntwort=null;
        try {
            kommAntwort = kommClient.warte();
        }
        catch (KommException ke)
        {
            CAT.error(ke.getMessage(), ke);
            return false;
        }
        
        if (kommAntwort.typ == ClientAntwort.SPIELSTART) {
            CAT.debug("Server send me: game start.");

            // ------- fetching the board -----
            try {
                String[] playerNames = kommClient.getNamen();
                String[] playerColors = kommClient.getFarben();
                Hashtable playerColorHash = new Hashtable(playerColors.length);
                // some magic for setting the robot colors
                Color[] robotsNewColor = initRobotColors(playerColorHash, playerColors);                               
                // Initializing the robots an applying the colors to their visualization
                int botCount = playerNames.length;
                Bot [] tempBots = new Bot[botCount];
                for (int i = 0; i < botCount; i++) {                    
                    d("Hole Roboterstatus von: " + playerNames[i]);
                    Bot tempRob = kommClient.getRobStatus(playerNames[i]);
                    tempBots[i]=tempRob;
                    String h_name = tempRob.getName();
                    Integer h_int = (Integer) playerColorHash.get(h_name);
                    tempRob.setBotVis(h_int.intValue());
                    robots.put(playerNames[i], tempRob);
                }
                initRegisterRowHash(tempBots);      
                // getting the flags
                Location [] flags = kommClient.getFahnenPos();
                 initBoard(robotsNewColor, flags);                
                //  fetching initial stats
                initStats();
                
                // creating the GUI
                if (view == null) {
                    view = new View(ausgabeView);
                } 
                else {
                    view.addAusgabeView(ausgabeView);
                    view.makeVisible();
                }
                // set the viewport to the first flag
                ausgabeView.jumpToFlag(1);                
                // 	we are done, removing splashscreen
                removeSplash();
                // we are done, send OK to server
                kommClient.spielstart();
            } 
            catch (KommException kE) {
                CAT.error("While trying to load the game data I got : " +
                        kE.getMessage(), kE);
                return false;
            }
            catch (FormatException e) {
                CAT.error(e.getMessage(),e);
                return false;
            } 
            catch (FlagException fe) {
                CAT.error(fe.getMessage(), fe);
                return false;               
            }
        } 
        else if (kommAntwort.typ == ClientAntwort.ENTFERNUNG){
            CAT.info("server removed me before the game started; reason="+kommAntwort.str);
            CAT.info("I'm going to kill myself..");
            shutdown();
            return false;
        }        
        else {
            // Problem: the server sends garbage
            CAT.error("server does not send a game start at game start... pfui!");
            return false;
        }
        return true;
        
    }

    private Color[] initRobotColors(Hashtable playerColorHash, String[] playerColors) throws KommException {
        for (int i = 0; i < playerColors.length; i++) {
            if (!playerColors[i].equals("0"))
                playerColorHash.put(playerColors[i], new Integer(i));
        }
        BotVis.initBotVis(playerColorHash);

        Location boardDim = kommClient.getSpielfeldDim();
        boardDimension = new Dimension(boardDim.x, boardDim.y);
        

        Color[] robotsDefaultColor = BoardView.ROBOCOLOR;
        Color[] robotsNewColor = new Color[8];

        int[] colorMap = new int[8];

        int j = 0;
        for (int i = 0; i < 8; i++) {
            if (!playerColors[i].equals("0")) {
                robotsNewColor[j] = robotsDefaultColor[i];
                colorMap[j] = i;
                j++;
            }
        }

        return robotsNewColor;
    }

    private void initBoard(Color[] robotsNewColor, Location [] flags) throws KommException, FormatException, FlagException {
        SimBoard sim = new SimBoard(boardDimension.width,
                boardDimension.height,
                kommClient.getSpielfeld(),
                flags);


        BoardView board = new BoardView(sim, robotsNewColor);
        board.setAutoscrolls(true);
        ausgabeView = new AusgabeView(board, getRoboterArray(), this);
        fireAusgabeViewInit();
        board.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent me) {
                if ((me.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
                    CAT.debug("mouse clicked in canvas " + me.getPoint());
                    Location ort = new Location();
                    BoardView brd = (BoardView) me.getSource();
                    brd.point2Ort(me.getPoint(), ort);
                    CAT.debug("corresponding ort is: " + ort);
                    ausgabeView.showPixelPos(me.getPoint().x, me.getPoint().y);
                }
            }
        });
    }


    /**
     * Fordert die View auf, eine ActionMessage anzuzeigen
     */
    protected void showActionMessage(String s) {
        if (ausgabeView != null) {
            ausgabeView.showActionMessage(s);
        }
    }

    protected Bot getBot(String botname) {
        return (Bot) robots.get(botname);
    }


    protected void showScout(int chosen, Bot[] robs) {
        ausgabeView.showScout(chosen, robs);
    }


    /**
     * Shows a text in a splashscreen;
     * creates the splashscreen if necessary
     */
    private void showSplash(String s) { 
            if (splashScreen == null) {
                splashScreen = new Splash();
            }
            splashScreen.setText(s);
            splashScreen.showSplash(true);
    }

    public void removeSplash() {
        if (splashScreen!=null) {
            splashScreen.noSplash();
            splashScreen = null;
        }       
    }

    private Bot[] getRoboterArray() {
        Bot[] robs = new Bot[robots.size()];
        int i = 0;
        Iterator iter = robots.values().iterator();
        while (iter.hasNext()) {
            robs[i] = (Bot) iter.next();
            i++;
        }
        return robs;
    }

    private synchronized  void waitForAusgabeViewInit(){
        while (ausgabeView==null) {            
                try {
                    this.wait();
                }
                catch (InterruptedException ie){
                    CAT.warn(ie.getMessage(), ie);
                }                       
        }        
        this.notifyAll();       
    }
    
    private synchronized void fireAusgabeViewInit(){
        this.notifyAll();
    }
    
/**
 * CAUTION: will block until the AusgabeView is not null 
 * (the AusgabeView will be created when the server sends the "game start" message). 
 * @return the AusagbeView that does all the displaying for this Ausgabe
 */
    public AusgabeView getAusgabeView() {
        if (ausgabeView == null){
            waitForAusgabeViewInit();
        }
        return ausgabeView;
    }

    private void d(String s) {
        Global.debug(this, s);
    }

    private void initStats() {
        try {
            CAT.debug("fetching stats..");
            stats = kommClient.getStats();
            if (CAT.isDebugEnabled()) {
                CAT.debug("..done");
                CAT.debug("stats ist: ");
                CAT.debug(stats.toString());
            }
        } catch (KommException ke) {
            CAT.error(ke.getMessage(), ke);
            CAT.error("KommException occured!");
            CAT.error("Failed to initialize Statistics-Menu!!!");
            CAT.error("!!YOU BETTER DO NOT CLICK ON THE STATISTICS MENU !!!");
        }
    }


    private void abmelden() {
        CAT.debug("deregistering from server..");
        CAT.debug("Ausgabe sets condition(s) for leaving its run() method");
        quitByMyself = true;
        spielEnde = true;
        CAT.debug("Ausgabe deregisters from server");
        kommClient.abmelden(getName());
    }

    protected void quit(boolean keepWatching, boolean quitHumanPlayer) {
        quit(keepWatching, quitHumanPlayer, false);
     }
    
    private void quit (boolean keepWatching, boolean quitHumanPlayer, boolean calledByShutdown){        
        removeSplash(); // needed cleanup in case we show a splash and fail to connect to the server..
        if (!keepWatching) {
            allowRunToFinish();
        }
        
      
     
        if (!keepWatching && kommClient!=null){
	        abmelden();      
	        try {
	            CAT.debug("killing communication..");
	            kommClient.shutdown(true);
	        }
	        catch (Exception ioe ){
	            CAT.error("during communication shutdown", ioe);
	         }
        }
        if (quitHumanPlayer && view != null) {
            CAT.debug("Ausgabe tells the view to propagate quitting..");
            view.quitHumanPlayer(true); // Tell the view to tell the HumanPlayer to quit, if there is any
            
        }
        if (!keepWatching && view != null){
            CAT.debug("removing View");
            view.setVisible(false);
            view.dispose();
            view = null;
        }
        if (!calledByShutdown){
            shutdown();           
        }       
        
        
    }
    
 
    
    public void doShutdown() {
        CAT.debug("starting shutdown..");
        if (sequencer != null) {
            sequencer.clear();
        }
        quit(false, true, true);
        allowRunToFinish();
        CAT.debug("reached end of shutdown");
    }


    protected View getView() {
        return view;
    }


    protected void showStats() {
        if (statsWindow == null) {            
            statsWindow = new JFrame(Message.say("AusgabeView", "stats"));   
            statsWindow.setContentPane(new PaintPanel(OptionPane.getBackgroundPaint(statsWindow)));
            statsWindow.getContentPane().add(new StatisticPanel(stats));
            statsWindow.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    statsWindow.setVisible(false);
                }
            });

            statsWindow.pack();
        }
        if (statsWindow.getState() == JFrame.ICONIFIED) {
            CAT.debug("iconified");
            statsWindow.setState(JFrame.NORMAL);
        }
        statsWindow.toFront();
        statsWindow.setVisible(true);
        statsWindow.show();
    }

    private void comHandleWeWereRemoved(ClientAntwort kommAntwort) throws KommException  {
        // we asked the server to be removed..
        if (quitByMyself) { // hendrik was here..
            CAT.debug("server seems to confirm our request for quitting the game");
            CAT.debug("this is great because chances are that server is not deadlocked now! :)");
            return;
        }

        spielEnde = true; // for leaving the main loop of the run()-method

       // try {
            String[] spielErgebnis = kommClient.getSpielstand();
            if (spielErgebnis != null) { // were we removed because the game is over?
                CAT.debug("We have " + spielErgebnis.length + " winners");             
                ausgabeView.showWinnerlist(spielErgebnis);
               
            } else {
                CAT.debug("No winner exists");
            }
     //   } catch (KommException e) {
     //       CAT.error(e.getMessage(), e);
       //     return;
     //   }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            CAT.error("Ausgabe: Interrupted by " + e.toString(), e);
        }
        kommClient.spielstart(); // I think that meant sending an "ok" to server
        // to confirm that we are done

    }


    private Bot getBotDataFromServer(String robotName) throws KommException {
        return kommClient.getRobStatus(robotName);
    }


    private void updateBoardView() {
        // telling the display to update itself with the new robot values
        ausgabeView.showUpdatedRobots(getRoboterArray());

    }


    private Bot [] getBotsDataFromServer(String [] botNames)throws KommException{
        return getBotsDataFromServer(botNames,0);
    }
    /**
     *  Gets current data for all Bots named in botNames from the server.
     * The startIndex is a little helper for server MSG-messages where 
     * ClientAntwort.namen[0] is the MessageId and not the first botname 
     * 
     * @param botNames Names of Bots to get updated data for
     * @param startIndex offset for botNames: only fetch data for bots at position startIndex or greater 
     *                                    MUST BE  less than botNames.length (and non-negative, for the pedants)    
     * @return Bot objects that contain the current (server-)data 
     * @throws KommException
     */
    private Bot [] getBotsDataFromServer(String [] botNames, int startIndex)throws KommException{
        	int numOfBots = botNames != null?(botNames.length-startIndex):0;
        	Bot [] updatedBots = new Bot[numOfBots];
        	for (int i=0, counter=startIndex;i<numOfBots;i++,counter++)
        	    updatedBots[i] = getBotDataFromServer(botNames[counter]);
        	return updatedBots;
    }
    
    


    private void comMsgHandleInitialFacings(ClientAntwort kommAntwort)  {
        try {
            Bot[] updated = getBotsDataFromServer(kommAntwort.namen,1); // namen[0] contains the messageId
            ausgabeView.setInitialFacings(updated);
            ausgabeView.updateRobotStatusDisplay(updated); 
        }
        catch (KommException ke ){
            CAT.error ("KommException while retrieving the robot data for the initial facing");
            CAT.error(ke.getMessage(), ke);
        }
     }


    private void comHandleNotifyChange(ClientAntwort kommAntwort) throws KommException {
        CAT.debug("NTC handler calling");
        // updating the data for the bots that have changed 
        Bot[] tmp = getBotsDataFromServer(kommAntwort.namen);



        // Using the Bot[] container (only added to ClientAntwort for this
        //    special purpose) to store the new robot values so that our
        //    HandlerAction can use them:
        kommAntwort.updatedBotsForNTC = tmp;
        // now we have done enough so that it should work..
        sequencer.invoke(MessageID.NTC, kommAntwort);

        // --------- get other information from the server
        /*
        Status[] stArray = kommClient.getSpielstatus();
        if (stArray != null) {
            CAT.debug("NTC handler working through status array");
           // updateRegisterViews(tmp, stArray);

                                        	   
            CAT.debug("aktPhase="+stArray[0].aktPhase+",lastPhase="+lastPhase);
            if (stArray[0].aktPhase != lastPhase) {                                                               
                // its ok to do the reset on each phase change,
                // will not do anything if all are reset
                ausgabeView.resetProgrammingLEDs();
                // force all robots to be visible at start of each evaluation phase,
                // as the robots will be painted during the animations - independend from
                // their visibility setting by the user
                ausgabeView.showAllRobots();
               // view.showGameStatusMessage(Message.say("AusgabeFrame", "phase") + " " + stArray[0].aktPhase);
                lastPhase = stArray[0].aktPhase;
            }
        }
        else {
            CAT.warn("status array was null");
        }
        */
        // --------- has somebody already reached the final flag?
        String[] winnerStateList = kommClient.getSpielstand();
        ausgabeView.showWinnerState(winnerStateList);

        // tell the server that we finished our stuff and he can send the next notify change
        kommClient.aenderungFertig();
    }


    // now some methods for fixing an issue that is caused by using the sequencer:
    // <SEQUENCER-FIX>
    /** contains messages that have special handler methods that also take
     *  care about displaying them in the chatpane or not displaying them at all..
     */

    private HashSet specialMessages = new HashSet();


    /** Add all message ids to <code>specialMessages</code> that have a special
     *  handler method (for displaying them in the chatpane or not displaying them at all).
     */

    private void initSpecialMessagesSet() {
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
        specialMessages.add(MessageID.INITIAL_FACINGS);
        specialMessages.add(MessageID.SOMEONE_QUIT);
        
        // some deprecated messages that should not be animated but also not
        // start with "mAusw" and not also be displayed..
        specialMessages.add(MessageID.SIGNAL_ACTION_START);
        specialMessages.add(MessageID.SIGNAL_ACTION_STOP);

    }

    private boolean isMessageSpecial(String msgid) {
        return specialMessages.contains(msgid);
    }

    private boolean isActionToBeDisplayedInInfopanelOnly(String msgid) {
        // the message must not start with MesssageID.AUSW
        boolean condition1 = !(msgid.startsWith(de.botsnscouts.comm.MessageID.AUSWERTUNG));

        return condition1 && (!isMessageSpecial(msgid));

    }

    private volatile boolean mayFinishRun = false;
  //  private Object shutdownLock = new Object();
    private void waitBlockingUntilShutdownCalled(){
        synchronized (this) {
            while (!mayFinishRun) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    CAT.warn(ie.getMessage(), ie);
                }
            }            
        }
    }
    
    private void allowRunToFinish(){
        synchronized (this) {
            mayFinishRun = true;
            this.notifyAll();
        }
    }
    
    //</SEQUENCER-FIx>

 
    private void comHandleMessages(ClientAntwort kommAntwort) {
        try {
            String msgId = kommAntwort.namen[0];
        

	        if (CAT.isDebugEnabled())
	            CAT.debug("Server sent me: " + msgId);
	
	        // check whether we have to display an information message in the transparent
	        // chat- and actionlog on the bottomline of the board:
	        // don't show if message is a "mAusw*"-message or it
	        // is a SIGNAL_ACTION_[START|STOP]-message
	        if (isActionToBeDisplayedInInfopanelOnly(msgId)) {
	            // this is a little dirty; we will override the message id so that it gets
	            // mapped to an action that does nothing else than displaying this
	            // (status) message in the transparent chatpane on the bottom of the
	            // display
	            sequencer.invoke(DUMMY_MESSAGE_ID_DISPLAY_STRING_ONLY, kommAntwort);
	        } else {
	            sequencer.invoke(kommAntwort);
	        }
		
	        
	        acknowledgeMessage();
        }
        catch (Exception e){
            CAT.error(e.getMessage(), e);
        }
    }


    /**
     * send response to the server that we got the message
     * and did all the stuff we wanted to do, so the server can
     * send the next message
     */
    private void acknowledgeMessage() {
        try {
            //  synchronized (lock) {
            kommClient.acknowledgeMsg();
            //    ++nextMsg;
            //    lock.notifyAll();
            //  }
        } catch (KommFutschException ke) {
            CAT.error("ke2: " + ke.getMessage(), ke);
        } catch (KommException kE) {
            CAT.error(kE.getMessage(), kE);
        }
    }

    private void comMsgHandleProgrammingDone(ClientAntwort kommAntwort) {
        Bot r1 = getBot(kommAntwort.namen[1]);
        ausgabeView.notifyBotProgrammingDone(r1);
    }

    private void comMsgHandleRobotRemoved(ClientAntwort kommAntwort) {
        try {
            String botname = kommAntwort.namen[1];
            Bot actual = kommClient.getRobStatus(botname);
            robots.put(kommAntwort.namen[1], actual);
            Bot [] bots = getRoboterArray();
            ausgabeView.showUpdatedRobots(bots);
            
            // stuff for updating the PhaseEvalPanel
            int size = bots.length;
            int sizeNew = Math.max(0,size-1);
            Bot [] botsWithoutRemoved = new Bot[sizeNew];
            ScalableRegisterRow [] rowsWR = new ScalableRegisterRow[sizeNew];
            int counter =0;
            for (int i=0;i<size;i++){
                Bot cur = bots[i];
                if (cur!=actual){
                    botsWithoutRemoved[counter] = cur;
                    rowsWR [counter]=getInfoRegistersForBot(cur.getName()); 
                    ++counter;
                }
            }
            ausgabeView.getPhaseEvalPanel().setContents(botsWithoutRemoved, rowsWR);
            
            
        } catch (KommFutschException ke) {
            CAT.error(ke.getMessage(), ke);
        } catch (KommException kE) {
            CAT.error(kE.getMessage(), kE);
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            CAT.error(e.getMessage(), e);
        }
    }

    private void comMsgHandleRobotLaser(ClientAntwort kommAntwort) {
        try {
            String targetName = kommAntwort.namen[2];
            String sourceName = kommAntwort.namen[1];
            Bot sourceBot = (Bot) robots.get(sourceName);
            Bot targetBot = (Bot) robots.get(targetName);
            // if it was killed and a notify changed was already received,
            // robots was not updated => targetBot.getDamage is <10
            Bot newTargetBot = getBotDataFromServer(targetName);
            Location pos = newTargetBot.getPos();
            boolean targetDestroyed = newTargetBot.getDamage() >= 10 ||
                    (pos.x == 0 && pos.y == 0);

            if (CAT.isDebugEnabled())
                CAT.debug("Got message telling " + sourceName + " shot " + targetName + ".");

            // updating statistics
            Stats sourceStats = stats.getStats(sourceName);
            sourceStats.incHits();
            if (targetDestroyed)
                sourceStats.incKills();
            Stats targetStats = stats.getStats(targetName);
            targetStats.incDamageByRobots();

            // paint animation and play sounds
            CAT.debug("LASER: " + sourceBot.getName() + " -------------> " + targetBot.getName());
            ausgabeView.animateRobLaser(sourceBot, targetBot);

            // now some management of a dirty hack to synchronize laser painting and
            // removing the killed robots from the display..
            // if (targetDestroyed)
            //   updateLaserAnimationHackMessageStuff(newTargetBot, true);
        } catch (KommException ke) {
            CAT.error(ke.getMessage(), ke);
        }
    }

    private void comMsgHandleChat(ClientAntwort kommAntwort) {
        SoundMan.playSound(SoundMan.MESSAGE);
    }

    private void comMsgHandleRobotFellIntoPit(ClientAntwort kommAntwort) {
        String botname = kommAntwort.namen[1];
        Bot bot = (Bot) robots.get(botname);
        //updateLaserAnimationHackMessageStuff(bot, false);
        ausgabeView.animatePitFall(bot);
        
    } 

    private void comMsgHandleRobotReachedFlag(ClientAntwort kommAntwort) {
        SoundMan.playSound(SoundMan.FLAG_REACHED);
    }

    private void comMsgHandleLastProgrammerFinished(ClientAntwort kommAntwort) {
        Stats actualStats = stats.getStats(kommAntwort.namen[1]);
        actualStats.incWasSlowest();
    }

    private void comMsgHandleBoardLaser(final ClientAntwort kommAntwort) {
        CAT.debug("doBoardLaser");

        //try {
        // get damaged robot
        String targetName = kommAntwort.namen[1];
        Bot targetBot = (Bot) robots.get(targetName);
        // Bot newTargetBot = (Bot) getBotDataFromServer(targetName);

        Location targetPos = targetBot.getPos();
        // due to laser hack, targetBot might not have the current damagecount
        //boolean targetKilled = newTargetBot.getDamage()>=10;

        // updating statistics for the robot hit
        Stats targetStats = stats.getStats(targetName);
        targetStats.incDamageByBoard();

        // get the origin of the laser (position and other stuff
        // needed for animation)
        Location laserPos = new Location(0, 0);
        int facing = -1;
        int strength = -1;
        try {
            strength = Integer.parseInt(kommAntwort.namen[2]);
            laserPos.x = Integer.parseInt(kommAntwort.namen[3]);
            laserPos.y = Integer.parseInt(kommAntwort.namen[4]);
            facing = Integer.parseInt(kommAntwort.namen[5]);
        } catch (NumberFormatException nfe) {
            CAT.error("Ausgabe: BoardLaser: NumberFormatException:", nfe);
        }

        for (int i = 0; i < strength; i++) {
            SoundMan.playSound(SoundMan.BOARDLASER);
        }
        // if enough information,  show laser animation
        if ((laserPos != null) && (facing >= 0) && (targetPos != null) && (strength >= 0)) {
            ausgabeView.animateBoardLaser(laserPos, facing, strength, targetPos);
        } else {
            if (CAT.isDebugEnabled()) {
                CAT.error("Ausgabe: unable to calculate Laseranimation: ");
                CAT.debug("laserPos: " + laserPos);
                CAT.debug("facing: " + facing);
                CAT.debug("r1Pos: " + targetPos);
                CAT.debug("strength: " + strength);
            }
        }
      

    }


    private void comMsgHandleRobotCrushed(ClientAntwort kommAntwort) {
        String botName = kommAntwort.namen[1];
        Bot bot = (Bot) robots.get(botName);
        ausgabeView.animateBotCrushed(bot);
    }

    private void comMsgHandleSomeoneAskedWisenheimer(ClientAntwort kommAntwort) {
        SoundMan.playSound(SoundMan.BOO);
        Bot r1 = (Bot) robots.get(kommAntwort.namen[1]);
        Stats actualStats = stats.getStats(r1.getName());
        actualStats.incAskWisenheimer();
    }

    private void comMsgHandleActionStart(ClientAntwort ca) {
        CAT.debug("got an action start");
    }

    private void comMsgHandleActionStop(ClientAntwort ca) {
        CAT.debug("got an action stop");
    }

    private void comMsgHandleRobotMove(ClientAntwort kommAntwort) {
        String robname = kommAntwort.namen[1];
        Bot r = (Bot) robots.get(robname);
        String direction = kommAntwort.namen[2];
        if (CAT.isDebugEnabled()) {
            CAT.debug("Got robot move message for robot \"" + robname + "\"");
            CAT.debug("Direction: " + direction);
        }
        try {
            int directionInt = Integer.parseInt(direction);
            ausgabeView.animateRobMove(r, directionInt);
        } catch (NumberFormatException nfe) {
            CAT.error(nfe);
            CAT.error("Failed to convert direction for robot \""
                    + robname + "\"from String to int!");
            CAT.error("String was: \"" + direction + "\"");

        }
    }

    private void comMsgHandleRobotTurn(ClientAntwort kommAntwort) {
        String robname = kommAntwort.namen[1];
        Bot r = (Bot) robots.get(robname);
        String direction = kommAntwort.namen[2];
        if (CAT.isDebugEnabled()) {
            CAT.debug("Got robot turn message for robot \"" + robname + "\"");
            CAT.debug("Direction: " + direction);
        }
        try {
            int directionInt = Integer.parseInt(direction);
            ausgabeView.animateRobTurn(r, directionInt);
        } catch (NumberFormatException nfe) {
            CAT.error(nfe.getMessage(), nfe);
            CAT.error("Failed to convert direction for robot \""
                    + robname + "\"from String to int!");
            CAT.error("String was: \"" + direction + "\"");

        }
    }

    private void comMsgHandleRobotUTurn(ClientAntwort kommAntwort) {

        String robname = kommAntwort.namen[1];
        Bot r = (Bot) robots.get(robname);
        if (CAT.isDebugEnabled()) {
            CAT.debug("Got robot U-Turn message for robot \"" + robname + "\"");
        }
        ausgabeView.animateRobUTurn(r);

    }

    private String[] extractMessage(ClientAntwort kommAntwort) {
        int size = kommAntwort.namen.length - 1;

        // getting parts of the message
        String[] tmpstr = new String[size];
        for (int i = 0; i < size; i++)
            tmpstr[i] = kommAntwort.namen[i + 1];
        return tmpstr;
    }

    private void comMsgHandleEvalPhaseStart(ClientAntwort cw) {
      
        
        
        // the following code was directly lifted from the notifyChange-handler,
        // because we now have this easy way here to figure out if there was a phase change
        //
        // its ok to do the reset on each phase change,
        // will not do anything if all are reset
        ausgabeView.resetProgrammingLEDs();
        // force all robots to be visible at start of each evaluation phase,
        // as the robots will be painted during the animations - independend from
        // their visibility setting by the user
        ausgabeView.showAllRobots();
        
        
        
        
        // cw.namen looks like:
        // cw.namen[0] = messageId
        // cw.namen[1] = for "phase number"
        // followed by: 
        // cw.namen[i] = robname #i 
        // cw.namen[i+1] = robname #i's card priority
//      	cw.namen[i+2] = robname #i's card action
        // ..for every robot 
        String phaseNumber = cw.namen[1];
        int phase = 0;
        try {
             phase = Integer.parseInt(phaseNumber);
        }
        catch (NumberFormatException ne){
            CAT.error("failed to parse phase number: "+phaseNumber);
            CAT.error(ne.getMessage(), ne);
        }
        currentPhase=phase;
        int delay = ausgabeView.getCurrentSpeedSettings().getDelayAfterRevealingCardsForPhase();
        int delay2 = Math.max(1, delay-200);
        ausgabeView.displayPhaseNumber(currentPhase+1, delay2);
        int length = cw.namen.length;
        for (int i=2;i<length;i+=3){            
            String botname = cw.namen[i];
            Bot bot = getBot(botname);
            String prioS = cw.namen[i+1];
            String action = cw.namen[i+2];
            int prio = -1;
            try {
                prio = Integer.parseInt(prioS);
            }
            catch (NumberFormatException ne){
                CAT.error("failed to parse card priority: "+prioS);
                CAT.error(ne.getMessage(), ne);
            }
            HumanCard hc = new HumanCard(prio, action);
            if (bot.getLockedRegister(phase)!= null) {
                hc.setState(HumanCard.LOCKED);
            }
            ArrayList rows = getRegisterRowsForBot(botname);
            Iterator it = rows.iterator();            
            if (phase == 0) {
                // lock locked registers if in first phase..
                Card [] lockedCards = bot.getLockedRegisters();
                HumanCard [] hcs = new HumanCard[Bot.NUM_REG];
                for (int j=0;j<Bot.NUM_REG;j++){
                    if (j == phase){ // this card is hc, already with the correct state
                        hcs[j] = hc;
                    }
                    else {
	                    Card c  = lockedCards[j];
	                    if (c != null){
	                        HumanCard hci = new HumanCard(c);
	                        hci.setState(HumanCard.LOCKED);
	                        hcs[j] = hci;
	                    }
                    }
                }
                while (it.hasNext()){
	                ScalableRegisterRow row = (ScalableRegisterRow) it.next();	                
	                row.alwayshowCardBackInsteadOfEmpty(bot.isActivated() && !bot.isInPit());	   
	                if (!bot.isActivated()|| bot.isInPit()) {	                   
	                    hcs = new HumanCard[Bot.NUM_REG];
	                }
	                row.setCards(hcs);	               
	                row.setCardVisibility(phase+1, true);               
	            }
            } 
            else {  // any other phase, we only need to set a single card           
	            while (it.hasNext()){
	                ScalableRegisterRow row = (ScalableRegisterRow) it.next();
	                row.alwayshowCardBackInsteadOfEmpty(bot.isActivated() && !bot.isInPit());	       
	                if (!bot.isActivated()|| bot.isInPit()) {	                   
	                    hc = null;
	                }
	                row.setCard(phase+1,hc );
	                row.setCardVisibility(phase+1, true);               
	            }
            }
        }    
        
        SoundMan.playSound(SoundMan.REVEAL_CARDS);
        
       
        waitSomeTime(delay);
        
    }
    private void comMsgHandleEvalPhaseEnd(ClientAntwort cw) {
        if (currentPhase < 0) {          
            // special case:
            // if we are a view that joins a game in mid-game it is likely that 
            // we get a "reveal card" message before the currentphase (set at begin of a phase)
            // was set
            return;
        }
        if (rowOfBotWhoseCardWasLastEvaluated != null) {
            rowOfBotWhoseCardWasLastEvaluated.setRegisterHighLighted(currentPhase, false);
            rowOfBotWhoseCardWasLastEvaluated = null;
        }
      /* String phaseNumber = cw.namen[1];
           try {
               int phase = Integer.parseInt(phaseNumber);
               if (phase == 4) { // last phase over
                   Iterator allRobsRowLists = robNameToRegisterRowCollection.values().iterator();
                   while (allRobsRowLists.hasNext()){
                       Iterator rows = ((Collection) allRobsRowLists.next()).iterator();
                       while (rows.hasNext()){
                           ScalableRegisterRow row = (ScalableRegisterRow)rows.next();
                           row.hideAll();
                       }
                     
                   }
                   
               }
           }
           catch (NumberFormatException ne){
               CAT.error("failed to parse phase number: "+phaseNumber);
               CAT.error(ne.getMessage(), ne);
           }
      */
       
    }
    
    private ScalableRegisterRow rowOfBotWhoseCardWasLastEvaluated = null;
    
    
    private void comMsgHandleEvalOfCard(ClientAntwort cw) {
        if (currentPhase < 0) {          
            // special case:
            // if we are a view that joins a game in mid-game it is likely that 
            // we get a "eval card" message before the currentphase (set at begin of a phase)
            // was set => do nothing until the phase is initialized 
            return;
        }
        String robName = cw.namen[1];
        ScalableRegisterRow row = getInfoRegistersForBot(robName);
        if (rowOfBotWhoseCardWasLastEvaluated != null) {
            rowOfBotWhoseCardWasLastEvaluated.setRegisterHighLighted(currentPhase, false);
        }
        row.setRegisterHighLighted(currentPhase, true);
        rowOfBotWhoseCardWasLastEvaluated = row;
        int delay = ausgabeView.getCurrentSpeedSettings().getDelayAfterHighlightingCurrentCard();
    //TODO     
        waitSomeTime(delay);
    }
    private void comMsgHandleRegisterLock(ClientAntwort ca){
        comMsgHandleRegisterLock(ca, false);
    }
    private void comMsgHandleRegisterUnLock(ClientAntwort ca){
        comMsgHandleRegisterLock(ca, true);
    }
    private void comMsgHandleRegisterLock(ClientAntwort ca, boolean unlock){
        String botname = ca.namen[1];
        String registerIndex = ca.namen[2];      
      
        try {          
            HumanCard hc = null;       
            int index = Integer.parseInt(registerIndex); // value 0-4
            index++; // value 1-5
            if (!unlock) {
	            String cardPrio = ca.namen[3];
                String cardAction = ca.namen[4];
	            int prio = 0;
	            try {
	                prio = Integer.parseInt(cardPrio);
	            }
	            catch (NumberFormatException ne2){
	                CAT.error("Failed to parse card priority:"+cardPrio);
	                CAT.error(ne2.getMessage(), ne2);
	            }
	        
	            hc = new HumanCard(prio, cardAction);	          
	            hc.setState(HumanCard.LOCKED);
            }
            else { // if unlock
                Bot bot = getBot(botname);
                Card oldLocked = bot.getLockedRegister(index -1);
                if (oldLocked!=null){
                    // this way, the locked card gets shown unlocked;
                    // otherwise (hc==null), a card's backside would be shown
                    // (and that looks stupid since all the other cards will be kept&shown
                    //  until phase one of the next round has started)
                    hc = new HumanCard(oldLocked);                  
                }
                // otherwise the locked-image is shown until the new card gets set
                // in the next phase (until then the register would be displayed as locked altough
                // it isn't)
                bot.unlockRegister(index-1);       
                
            }
            ArrayList rows = getRegisterRowsForBot(botname);
            Iterator it = rows.iterator();
            while (it.hasNext()){
                ScalableRegisterRow row = (ScalableRegisterRow) it.next();
                row.setCard(index, hc);
            }
           
            
            
        }
        catch (NumberFormatException ne){
            CAT.error("failed to parse register number: "+ca.namen[2]);
            CAT.error(ne.getMessage(), ne);
        }
    }
    
    private void initMessageToActionMapping() {
        sequencer.addActionMapping(MessageID.REGISTER_LOCKED,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleRegisterLock(msgData);
                                
                            }
                        });
        sequencer.addActionMapping(MessageID.REGISTER_UNLOCKED,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleRegisterUnLock(msgData);
                             
                            }
                        });
        sequencer.addActionMapping(MessageID.PHASE_STARTED,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleEvalPhaseStart(msgData);
                             
                            }
                        });
        sequencer.addActionMapping(MessageID.PHASE_ENDED,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleEvalPhaseEnd(msgData);
                              
                            }
                        });
        
        sequencer.addActionMapping(MessageID.PLAYING_CARD,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleEvalOfCard(msgData);
                               
                            }
                        });
        sequencer.addActionMapping(MessageID.SIGNAL_ACTION_START,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        comMsgHandleActionStart(msgData);
                      
                    }
                });
        sequencer.addActionMapping(MessageID.SIGNAL_ACTION_STOP,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        comMsgHandleActionStop(msgData);
                 
                    }
                });
        sequencer.addActionMapping(MessageID.PROG_DONE,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        comMsgHandleProgrammingDone(msgData);
                
                    }
                });

       
        //sequencer.addActionMapping(MessageID., tmp);

        sequencer.addActionMapping(MessageID.BOT_REMOVED, 
                        new AbstractMessageAction() {
				            public void invoke(ClientAntwort msgData) {
				                comMsgHandleRobotRemoved(msgData);
				            }
				        });
        
        sequencer.addActionMapping(MessageID.SOMEONE_QUIT,
                        new AbstractMessageAction(){
            				public void invoke(ClientAntwort msgData){
            				    comMsgHandleRobotRemoved(msgData);
            				    sequencer.invoke(DUMMY_MESSAGE_ID_DISPLAY_STRING_ONLY, msgData);
            				    //showActionMessage(msgData.str);
            				}
        			});
        
        
        sequencer.addActionMapping(MessageID.CHAT,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.CHAT,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleChat(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.BOT_LASER,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        comMsgHandleRobotLaser(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.BOT_IN_PIT,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.BOT_IN_PIT,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleRobotFellIntoPit(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.FLAG_REACHED,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.FLAG_REACHED,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleRobotReachedFlag(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.LAST_PROG,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.LAST_PROG,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleLastProgrammerFinished(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.BORD_LASER_SHOT,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.BORD_LASER_SHOT,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleBoardLaser(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.WISE_USED,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", MessageID.WISE_USED,
                                extractMessage(msgData));
                        showActionMessage(displayString);
                        comMsgHandleSomeoneAskedWisenheimer(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.BOT_MOVE,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        if (AnimationConfig.areMovementAnimationsEnabled())
                            comMsgHandleRobotMove(msgData);
                    }
                });

        sequencer.addActionMapping(MessageID.BOT_TURN,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        if (AnimationConfig.areMovementAnimationsEnabled())
                            comMsgHandleRobotTurn(msgData);
                    }
                });
        sequencer.addActionMapping(MessageID.BOT_UTURN,

                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                if (AnimationConfig.areMovementAnimationsEnabled())
                                    comMsgHandleRobotUTurn(msgData);
                            }
                        });
        sequencer.addActionMapping(MessageID.INITIAL_FACINGS,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleInitialFacings(msgData);
                            }
                        });
        
        sequencer.addActionMapping(MessageID.BOT_CRUSHED,
                        new AbstractMessageAction() {
                            public void invoke(ClientAntwort msgData) {
                                comMsgHandleRobotCrushed(msgData);
                            }
                        });
        
        sequencer.addActionMapping(DUMMY_MESSAGE_ID_DISPLAY_STRING_ONLY,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        String displayString = Message.say("MSG", msgData.namen[0],
                                extractMessage(msgData));
                        showActionMessage(displayString);
                    }
                });

        sequencer.addActionMapping(MessageID.NTC,
                new AbstractMessageAction() {
                    public void invoke(ClientAntwort msgData) {
                        Bot[] updatedBots = msgData.updatedBotsForNTC;
                        int l = updatedBots.length;
                        for (int i = 0; i < l; i++) {
                            Bot $_ = updatedBots[i];  // it seems someone read too many Perl books.. ;-)
                            robots.put($_.getName(), $_);
                        }
                        updateBoardView();
                    }
                });
    }
    
    
    private HashMap robNameToRegisterRowCollection = new HashMap();
    private ArrayList getRegisterRowsForBot(String botname){
        return (ArrayList) robNameToRegisterRowCollection.get(botname);
    }

    private static final int TOOLTIP_REGISTER_INDEX=0;
    private static final int INFO_REGISTER_INDEX=1;
 
    private void  initRegisterRowHash(Bot [] bots) {
        int c = bots!=null?bots.length:0;     
        if (CAT.isDebugEnabled()){
            CAT.debug("register init for: "+Global.arrayToString(bots));
        }
        
        for (int i=0;i<c;i++){
            ArrayList list = new ArrayList(2);
            if (bots [i] !=null) {
                // tooltip:
                ScalableRegisterRow row1 = new ScalableRegisterRow(0.3,false, 10);
               // row1.useCardBacksideForEmptyRegisters();
                list.add(TOOLTIP_REGISTER_INDEX,row1); 
                // info panel:
                ScalableRegisterRow row2 = new ScalableRegisterRow(0.5, false, 5);
                row2.setOpaque(false);
               // row2.useCardBacksideForEmptyRegisters();
                list.add(INFO_REGISTER_INDEX,row2);               
                robNameToRegisterRowCollection.put(bots[i].getName(),list);
        
            }
        }
        
    }         
    
    
    
        
    private ScalableRegisterRow getRegistersForBot(String botname, int regIndex){
        ArrayList list = getRegisterRowsForBot(botname);
        ScalableRegisterRow back=null;
        if (list != null){
            back = (ScalableRegisterRow) list.get(regIndex);
        }   
        return back;
      
    
    }
    protected ScalableRegisterRow getTooltipRegistersForBot(String botname){
        return getRegistersForBot(botname, TOOLTIP_REGISTER_INDEX);
    }
    protected ScalableRegisterRow getInfoRegistersForBot(String botname){
        return getRegistersForBot(botname, INFO_REGISTER_INDEX);
    }
  
    private Object waitLock = new Object();
    private void waitSomeTime(int ms){
        if (ms<= 0){
            return;
        }
        synchronized (waitLock){
            try {
                CAT.debug("waiting "+ms+"ms");
                waitLock.wait(ms);
                
                CAT.debug("done waiting "+ms+"ms");
            }
            catch (InterruptedException ie){
                CAT.error(ie.getMessage(), ie);
            }
        }
    }
    
}



