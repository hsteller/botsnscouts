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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Category;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.comm.ClientAntwort;
import de.botsnscouts.comm.KommClientSpieler;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Registry;
import de.botsnscouts.widgets.GreenTheme;

/**
 * logic for the human player
 * @author Lukasz Pekacki
 *  @version $Id$
 */
public class HumanPlayer extends BNSThread {
    static Category CAT = Category.getInstance(HumanPlayer.class);

    
    protected final static int MODE_PROGRAM = 0;
    protected final static int MODE_OTHER = 1;

    protected int mode = MODE_OTHER;
    private HumanView humanView;
    private Ausgabe ausgabe;
    private KommClientSpieler comm;
    private View view;
    private ClientAntwort commAnswer = new ClientAntwort();
    private SimBoard intelliBoard;

    private ArrayList cards = new ArrayList(9);
    private String host, name;
    private int port, myColor, globalTimeout;
    private boolean gameOver = false, nosplash = false;
    /** The Wisenheimer the user can use */
    private Wisenheimer wisenheimer;
    /** The Wisenheimer the EmergencyCardSubmitter uses */
    private Wisenheimer emergencyWisenheimer; 
    
    
    private boolean isScoutAllowed = true;
    private boolean isWisenheimerAllowed = true;
    
    private Timer timeoutWatcher;
    private EmergencyCardSubmitter emergencyCardSubmitter;
    private EmergencyDirectionSubmitter emergencyDirectionSubmitter;
    private EmergencyStayPowerDownSubmitter emergencyPowerDownSubmitter;
    private EmergencyRegRepairSubmitter emergencyRegRepairSubmitter;
    private Bot emergencyBotCopy = Bot.getNewInstance("EmergencySubmitterCopy");
    private volatile boolean cardsSent;    
    private volatile boolean newDirectionSent = false;
    private volatile boolean stayPowerDownAnswered = false;
    private volatile boolean repairRegistersAnswered = false;
    
    /** We start our emergency submit for cards/direction/power down state/register numbers
     *   that many seconds before the global timeout expires. 
     *   This should compensate for network lag, Timer-management overhead,
     *    Wisenheimer-prediction for a good answer etc. 
     */
    private static final int COVER_MY_ASS_BUFFER = 5;
    
    /** Starting the EmergencyCardSubmitter that many seconds before the global timeout expires*/
    public static final int BUFFER_SECONDS_BEFORE_TIMEOUT = 25;
    /** This is the number of seconds we show to the user; after this many seconds the
     *   EmergencyCardSubmitter will kick into action, blocking the user and getting the
     *   Wisenheimer's prediction to send*/
    public static final int BUFFER_SECONDS_BEFORE_SENDING_CARDS = BUFFER_SECONDS_BEFORE_TIMEOUT-COVER_MY_ASS_BUFFER;
    /** If set to true, we will do a System.exit(0) on the end of the run() method*/
    private boolean killJVMonceFinished = false;
       
    public HumanPlayer(String host, int port, String name) {
        this(host, port, name, -1);
    }

    public HumanPlayer() {
        this("localhost", 8077, KrimsKrams.randomName());
    }

    public HumanPlayer(String host, int port, String name, int color) {
        this(host, port, name, color, false);
    }

    public HumanPlayer(String host, int port, String name, int color, boolean nosplash) {
        super("HP:" + name);
        this.host = host;
        this.port = port;
        this.name = name;
        this.nosplash = nosplash;
        myColor = color;
        comm = new KommClientSpieler();
    }


    	public String getPlayerName(){
    	    return name;
    	}
    
    	
    	
    /**
     * Start des Menschlichen Spielers
     */
    public void run() {

        // --- registering for game ---
        if (registerAtServer()) {
            Global.debug(this, "registered for game as new humanplayer with name: " + name);
        } else {
            ErrorView.show(Message.say("HumanPlayer", "eNoServerRunning", host, port));
            CAT.fatal("No server running on host " + host + " at port " + port);
            return;
        }

        initView();

        // ------- begin to play
        while (!gameOver) {

            try {
                commAnswer = comm.warte();
                Global.debug(this, "Server sends : " + commAnswer.getTyp());
            } catch (KommException kE) {
                Global.debug(this, kE.getMessage());
            }

            switch (commAnswer.typ) {
                case (ClientAntwort.MACHEZUG):
                    {
                    	//                  ----- insert cards  -----
	                    cards.clear();
	                    for (int i = 0; i < commAnswer.karten.length; i++) {
	                        cards.add(i, new HumanCard(commAnswer.karten[i]));
	                    }
	                  
	                    
	                    try {
	                        // the following makes-not-so-much-sense copying&stuff is done so
	                        // that the EmergencySubmitters Wisenheimer prediction will work in
	                        // some not-so-obvious cases (locked registers with register-repair by the
	                        // repair emergency submitter); it turned out that even if the bot's locked registers 
	                        // are ok here, they might be "magically" missing at the time of prediction, leading
	                        // to errors
	                        // => EmergencySubmitter gets copy of robot that noone else will mess up
	                        Bot meAtStartOfRound = comm.getRobStatus(name);   
	                    
	                        /* Bot meCopy*/	                       
	                        emergencyBotCopy.copyRob(meAtStartOfRound);
	                        humanView.updateRegisters(meAtStartOfRound.getLockedRegisters());
	                        humanView.showCards(cards);	                      
	                        synchronized(comm) {
	                    	    cardsSent = false;
		                    	mode = MODE_PROGRAM;
		                    	Global.debug(this, "I am requested to send cards");
		                    	if (timeoutWatcher != null){
		                    	    try {
		                    	        timeoutWatcher.cancel();
		                    	    }
		                    	    catch (Exception e){
		                    	        CAT.error("exception canceling the old timeout watcher", e);
		                    	    }
		                    	}                    	   
		                    
		                    	timeoutWatcher = new Timer();    
		                    	emergencyCardSubmitter = new EmergencyCardSubmitter();
	                    	    timeoutWatcher.schedule(emergencyCardSubmitter, (globalTimeout-BUFFER_SECONDS_BEFORE_TIMEOUT)*1000);                    	    
	                    	}  
	                    
	                    }  
	                    catch (KommException kE) {
	                        CAT.error(kE.getMessage(), kE);
	                    }
	                    
                        showMessage(Message.say("SpielerMensch", "mwartereg"));
                        
                       
                        
                        break;
                    }

                    // start of the game
                case (ClientAntwort.SPIELSTART):
                    {
                        showMessage(Message.say("SpielerMensch", "spielgehtlos"));
                        comm.spielstart();
                        break;
                    }

                    // robot destroyed or initally set on the board
                case (ClientAntwort.ZERSTOERUNG):
                    {
	                    if (globalTimeout == 0) {
	                        try {
	                            globalTimeout = comm.getTimeOut();
	                        } catch (KommException kE) {
	                            CAT.error("during request for global timeout value", kE);	                            
	                        }
	                    }
	                    synchronized (comm) {
	                        killEmergencySubmitters();
	                        newDirectionSent = false;
	                        timeoutWatcher = new Timer();    
	                        emergencyDirectionSubmitter = new EmergencyDirectionSubmitter();
	                        timeoutWatcher.schedule(emergencyDirectionSubmitter, 
	                                        (globalTimeout-COVER_MY_ASS_BUFFER)*1000);
	                    }
                        humanView.showGetDirection();
                        CAT.debug("received desctruction");
                        showMessage(Message.say("SpielerMensch", "roboauffeld"));
// ---get board for wisenheimer
                        if (intelliBoard == null) {
                            initIntelligentBoard();
                            wisenheimer = new Wisenheimer(intelliBoard);
                            initScoutAndWisenheimerPermissions();
                        }

                        // ----- ask for timeout -------
                       
                        break;
                    }
                    // robot reaktivated
                case (ClientAntwort.REAKTIVIERUNG):
                    {
	                    synchronized (comm) {
	                        stayPowerDownAnswered = false;
	                        timeoutWatcher = new Timer();    
	                        emergencyPowerDownSubmitter = new EmergencyStayPowerDownSubmitter();
	                        timeoutWatcher.schedule(emergencyPowerDownSubmitter, 
	                                        (globalTimeout-COVER_MY_ASS_BUFFER)*1000);
	                    }
                        showMessage(Message.say("SpielerMensch", "roboreaktiviert"));
                        // ask for powerDownagain
                        humanView.showRePowerDown();


                        break;
                    }

                    // repair your registers
                case (ClientAntwort.REPARATUR):
                    {
                        CAT.debug("may repair some register(s)");

                        try {                            
                            Bot tempRob = comm.getRobStatus(name);
                            emergencyBotCopy.copyRob(tempRob);                            
                            int repPoints = commAnswer.zahl;
                            synchronized (comm) {
                                repairRegistersAnswered = false;
    	                        timeoutWatcher = new Timer();    
    	                        emergencyRegRepairSubmitter = new EmergencyRegRepairSubmitter(/*me,*/repPoints);
    	                        timeoutWatcher.schedule(emergencyRegRepairSubmitter, 
    	                                        (globalTimeout-COVER_MY_ASS_BUFFER)*1000);
    	                    }
                            
                            humanView.showRegisterRepair(tempRob.getLockedRegisters(), repPoints);             
                        } catch (KommException kE) {
                            CAT.error(kE);
                        }

                        break;
                    }

                    // removed from game
                case (ClientAntwort.ENTFERNUNG):
                    {
                        // ------- Habe ich gewonnen / bin ich gestorben ----------
                        boolean dead = true;
                        int rating = 0;
                        boolean gameIsFinished = false;
                        if (commAnswer.zahl == ClientAntwort.REMOVAL_REASON_GAMEOVER ||
                                        commAnswer.zahl == ClientAntwort.REMOVAL_REASON_LOSTLIVES) {	                        
	                        try {
	                            String[] gewinnerListe = comm.getSpielstand();
	                            gameIsFinished = gewinnerListe != null;
	                            if (gameIsFinished) {
	                                //showMessage(Message.say("SpielerMensch", "spielende"));
	                                for (int i = 0; i < gewinnerListe.length; i++) {
	                                    if (gewinnerListe[i].equals(name)) {
	                                        dead = false;
	                                        rating = (i + 1);
	                                    }
	                                }
	                            } else {
	                                Global.debug(this, "Bin gestorben...");
	                                dead = true;
	                            }
	                        } catch (KommException e) {
	                            Global.debug(this, e.getMessage());
	                        }
                        }
                        String removalReason = commAnswer.str;
                        gameOver = true;
                        
                        humanView.showGameOver(dead, rating, removalReason, gameIsFinished);
                        break;
                    }
                default :
                    {
                        Global.debug(this, "Unkonown message form server.");
                    }
            }
        }

        CAT.debug("Human Player reached end of run-method");
        //view.removeChatPane();

        try {
            CAT.debug("Waitingfor Ausgabe (join())..");
            ausgabe.join();
           
        } catch (InterruptedException e) {
            CAT.error(e);
        }
       
       // we don't want to kill our communication in every case;
        // for example: we would lose shutdown(true);
        shutdown(true);
        if (killJVMonceFinished){
            System.exit(0);
        }
        CAT.info("HUMANPLAYER FINISHED!");
        return;

    }

    /**
     * Main-Methode, die den menschlichen Spieler von der Shell aus als Thread startet
     */
    public static void main(String[] args) {
        //1. name
        //2. host 
        //3. port
        //4. farbe 
        // 5. killJVM 
        String name  = KrimsKrams.randomName();
        String host = "127.0.0.1";
        int port = 8077, farbe = 0; 
        boolean killJVM = true;       
        int tmpInt;
        switch (args.length) {
            case 5: { // JVM
                killJVM = new Boolean(args[4]).booleanValue();
            }
            case 4: { // color
                try {
                    farbe = Integer.parseInt(args[3]);
                }
                catch (NumberFormatException nfe){
                    System.err.println("illegal color value:\"" +args[3]+"\"; using '0' instead");
                    farbe = 0;
                }
            }
            case 3: { // serverPort 
                try {
                    tmpInt = Integer.parseInt(args[2]);
                    if (tmpInt < 9) {
                        farbe = tmpInt;
                        port = 8077;
                    } else {
                        port = tmpInt;
                        farbe = 0;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("number parse error on \""+args[2]+"\"");
                }
            }
            case 2: {// serverIp 
                try {
                    tmpInt = Integer.parseInt(args[1]);
                    if (tmpInt < 9) {
                        farbe = tmpInt;
                        port = 8077;
                    } else {
                        port = tmpInt;
                        farbe = 0;
                    }
                    host = "127.0.0.1";
                } catch (NumberFormatException e) {
                    host = args[1];
                    port = 8077;
                    farbe = 0;
                }
            }
           case 1: { // name     
               if (args[0] != null){                  
                   name = args[0];
               }
           }
        }
                                
        MetalLookAndFeel.setCurrentTheme(new GreenTheme());
        HumanPlayer hp = new HumanPlayer(host, port, name, farbe);
        hp.killJVMonceFinished = killJVM;
        hp.start();
    }

    protected void sendCards(ArrayList registerCards, boolean nextTurnPowerDown) {
            mode = MODE_OTHER;
	        int sendProg[] = new int[registerCards.size()];
	        int index = 0;
	
	        d("meine Registerkarten: " + registerCards);
	        d("die Karten, die der Server ausgeteilt hat:" + cards);
	
	        
	
	        for (int i = 0; i < registerCards.size(); i++) {
	            for (int j = 0; j < cards.size(); j++) {
	                if (((HumanCard) registerCards.get(i)).equals((HumanCard) cards.get(j))) {
	                    sendProg[index] = (j + 1);
	                    index++;
	                    continue;
	                }
	            }
	        }
	        synchronized (comm){	            
	            if (!cardsSent) {
	                emergencyCardSubmitter.cancel();
	                	    
	                try {
	                    timeoutWatcher.cancel();
	                }
	                catch (Exception e){
	                    CAT.warn("while doing an additional, prob. unnecessary cancel:"+e.getMessage(),e);
	                }
	                comm.registerProg(name, sendProg, nextTurnPowerDown);	                
	                cardsSent = true;
	                humanView.hidePhaseInfoCards();
	            }
	        }
    }

    private boolean registerAtServer() {
        boolean anmeldungErfolg = false;
        int versuche = 0;

        while ((!anmeldungErfolg) && (versuche < 3)) {
            try {
                anmeldungErfolg = comm.anmelden2(host, port, name, myColor);
            } catch (KommException kE) {
                CAT.warn(kE.getMessage());
                versuche++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    CAT.debug(e.getMessage());
                }
            }
        }
        return anmeldungErfolg;

    }

    /** meldet den Spieler beim Server ab und beendet diesen Thread.
     */
    public void doShutdown() {
        if (CAT.isDebugEnabled()) {
            CAT.debug(name + "was called to quit");
            CAT.debug("sending quit to server..");
        }
        try {
            comm.abmelden(name); // notify server
        }
        catch (Exception e){
            CAT.warn("in shutdown (deregistering)", e);
        }
        try {
            comm.shutdown(true); // close socket and streams
        }
        catch (Exception e){
            CAT.warn("in shutdown (killing communication)", e);
        }
        CAT.debug("setting condition for leaving the run()-method");
        gameOver = true;               
        
        killEmergencySubmitters();
        
        if (view != null){
            // in view.showGameOver() a timer Thread gets started that will call view.wait() for atm 8 seconds 
            synchronized (view){
                view.notifyAll();
            }
        }
        view = null;
        
    }

    private void killEmergencySubmitters(){
        CAT.debug("killing/interrupting potential EmergenySubmitter:");
        if (timeoutWatcher != null){
            try {
                timeoutWatcher.cancel();
                timeoutWatcher = null;
            }
            catch (Exception e){
                CAT.warn("in shutdown (timeoutWatcher)", e);
            }              
        }
        if (emergencyCardSubmitter != null){
            try {
                emergencyCardSubmitter.cancel();
                emergencyCardSubmitter = null;
            }
            catch (Exception e){
                CAT.warn("in shutdown (emergencyCardSubmitter)", e);
            }              
        }
        if (emergencyDirectionSubmitter != null) {
            try {
                emergencyDirectionSubmitter.cancel();
                emergencyDirectionSubmitter = null;
            }
            catch (Exception e){
                CAT.warn("in shutdown (emergencyDirectionSubmitter)", e);
            }         
        }
        if (emergencyPowerDownSubmitter != null) {
            try {
                emergencyPowerDownSubmitter.cancel();
                emergencyPowerDownSubmitter = null;
            }
            catch (Exception e){
                CAT.warn("in shutdown (emergencyPowerDownSubmitter)", e);
            }         
        }
        if (emergencyRegRepairSubmitter != null) {
            try {
                emergencyRegRepairSubmitter.cancel();
                emergencyRegRepairSubmitter = null;
            }
            catch (Exception e){
                CAT.warn("in shutdown (emergencyRegRepairSubmitter)", e);
            }         
        }
        
    }
    

    protected void passUpdatedScout(int chosen, Bot[] robs) {
        ausgabe.showScout(chosen, robs);
    }


    protected void sendRepair(ArrayList respReparatur) {
        synchronized (comm) {
	        if (!repairRegistersAnswered) {
	            d("sende meinen Reparaturwunsch: " + respReparatur);
		        int[] repa = new int[respReparatur.size()];
		        for (int i = 0; i < respReparatur.size(); i++) {
		            repa[i] = ((Integer) respReparatur.get(i)).intValue();
		        }
		        comm.respReparatur(name, repa);
		        showMessage(Message.say("SpielerMensch", "sendregrep"));		       
		        emergencyRegRepairSubmitter.cancel();
		        repairRegistersAnswered = true;
	        }
	        else {
	            CAT.debug("ignored request to send register repair ("
	                            +Global.collectionToString(respReparatur)+
	            ") because it looks like a direction got already sent..");
	        }
	        
        }
    }

    /** Sends a ChatMessage to the server */
    protected void sendChat(String msg) {
        String[] tmp = new String[2];
        tmp[0] = name;
        tmp[1] = msg;
        sendMessage("mChat", tmp);
    }

    /** Sends Message to the server */
    protected void sendMessage(String code, String[] args) {
        comm.message(code, args);
    }

    /** Sends the wisenheimer-activated-msg */
    protected void sendWisenheimerMsg() {
        String[] tmp = {name};
        sendMessage(de.botsnscouts.comm.MessageID.WISE_USED, tmp);
    }

    protected Bot getRob() {
        return ausgabe.getBot(name);

    }

   
    protected void sendDirection(int r) {
        synchronized (comm) {
            if (!newDirectionSent) {
                CAT.debug("sending direction"+r);
                comm.respZerstoert(name, r);             
                emergencyDirectionSubmitter.cancel();
                newDirectionSent = true;
           }
            else {
                CAT.debug("ignored request to send direction "+r+
                				" because it looks like a direction was already sent..");
            }
        // show wait message
        }
    }


    protected void sendAgainPowerDown(boolean down) {
        synchronized (comm){
            if (!stayPowerDownAnswered) {                
                comm.respReaktivierung(name, down);               
                emergencyPowerDownSubmitter.cancel();     
                stayPowerDownAnswered = true;
            }
            else {
                CAT.debug("ignored request to send stay powerdown answer ("+down+
				") because it looks like an answer was already sent..");
            }
        }
    }

    protected int getNextPrediction(ArrayList registerList, ArrayList cardList) {
        synchronized (wisenheimer) {
            return wisenheimer.getNextPrediction(registerList, cardList);
        }
    }

    protected int getPrediction(ArrayList registerList, ArrayList cardList) {
        synchronized (wisenheimer) {
            return wisenheimer.getPrediction(registerList, cardList, ausgabe.getBot(name));
        }
    }
    
    /** 
     * NOTE: THIS METHOD USES NOT(!) THE USER'S WISENHEIMER!
     * 
     * @param registersWithLockedHumanCards  A Bot.NUM_REG-long list containing locked HumanCards or null as values
     * @return An array with  the indices of the cards the Wisenheimer has recommended
     *               (array values are of [1,2,..,9]  ) 
     */
    private int [] getCompleteWisenheimerMove(Bot somebot){
        // RegisterArray.resetAll(); // resetting move so far
        // RegisterArray.getWisenheimerCards(); // getting possible locked registers
        	//wisenheimer.getPrediction(cardList, ausgabe.getBot(name));
       
        ArrayList myRegs = new ArrayList(Bot.NUM_REG);        
        int numOfCardsNeeded = Bot.NUM_REG;       
        for (int i=0;i<Bot.NUM_REG;i++){   
            Card c = somebot.getLockedRegister(i);
            //HumanCard card = (HumanCard) registersWithLockedHumanCards.get(i);
            // somebot.getLockedRegister(i);      
            if (c == null) {
                myRegs.add((HumanCard)null);
            }
            else {
                HumanCard hc = new HumanCard(c);
                hc.setState(HumanCard.LOCKED);
                myRegs.add(hc);
                // register was locked, one phase less to predict
                numOfCardsNeeded--;
            }                     
        }            
            
        if (CAT.isDebugEnabled()) {
            CAT.debug("numOfCardsNeeded="+numOfCardsNeeded);
            CAT.debug("****** myRegs:\n"+Global.collectionToString(myRegs));      
            CAT.debug("****** cards:\n"+Global.collectionToString(cards));
              
        }
        int [] predictions = new int[numOfCardsNeeded];
        if (numOfCardsNeeded > 0 ) {
            synchronized (wisenheimer) {
	            // since the wisenheimer is not stateless and shared with the user,
                // we will deactivate its button so that the user can't interfere with our predicting
                CAT.debug("disabling wisenheimer");
                humanView.wisenheimerView.setSelected(false);
                humanView.wisenheimerView.setEnabled(false);
                humanView.removeWisenheimer();
                // reason for the following loop:
                // if the user has already selected some cards, our wisenheimer won't use them
                // and might have not enough cards to create a as many predictions as we want
                // to have from him; this might lead to a suboptimal move or even to a NullPointerException
                // in wisenheimer.getPrediction()

                try {
	                int cardCount = cards.size();
	                for (int i=0;i<cardCount;i++){
	                    HumanCard card = (HumanCard) cards.get(i);
	                    if (card.getState() != HumanCard.LOCKED) {
	                        card.setState(HumanCard.FREE); 
	                    }
	                }        
	               
	                // getting initial prediction/resetting prediction, probably doing the same work
		            // twice if the wisenheimer was already called in this round 
	                Card [] move = emergencyWisenheimer.getPredictionCards(myRegs, cards, somebot);
	                CAT.debug("wise returned: ");
	                CAT.debug(Global.arrayToString(move,'\n'));
	                int numOfCards = move!=null?move.length:0;
               
                    int predCounter = 0;
	                for (int i=0;i<numOfCards;i++){
	                    HumanCard hc = (HumanCard) move[i];
	                    if (!hc.locked()) {
	                        int prio = hc.getprio();
	                        predictions[predCounter++] = Wisenheimer.getIndex(prio, cards)+1;
	                    }
	                }
                }
                catch (Exception e){
                    CAT.error(e.getMessage(), e);     
                    for (int i=0;i<numOfCardsNeeded;i++){
                        predictions[i] = i+1;
                    }
                }             /*
               predictions [0] = emergencyWisenheimer.getPrediction(myRegs, cards, emergencyBotCopy);
               Object card = cards.get(predictions[0])
               myRegs.set(0, card);
	            for (int i=1;i<numOfCardsNeeded;i++) {
	                predictions[i] = emergencyWisenheimer.getNextPrediction(myRegs, cards);	  
	                card = cards.get(predictions[i]);
	                myRegs.set(i, card);
	            }	
	            */           
	            CAT.debug("card indices I will submit: "+Global.arrayToString(predictions));
	            CAT.debug("reenabling winsenheimer");
                humanView.wisenheimerView.setEnabled(true);
            }
        }
        return predictions;
	            
        
        	
        
        
    }
    
    

    private void initScoutAndWisenheimerPermissions() {
        try {
            isScoutAllowed = comm.getIsScoutAllowed();
            isWisenheimerAllowed = comm.getIsWisenheimerAllowed();
           
            if(!isScoutAllowed){
                humanView.scoutView.setEnabled(false);
                humanView.scoutView.setSelected(false);
            }
            if (!isWisenheimerAllowed) {
                humanView.wisenheimerView.setEnabled(false);
                humanView.wisenheimerView.setSelected(false);
            }
        }
        catch (KommException ke){
            // neither  likely nor critical, we will go on with the default values
            CAT.error("Exception getting scout/wisenheimer permissions", ke);
        }
    }
    
    private void initIntelligentBoard() {
        int dimx, dimy;
        Location dimension;
        try {
            dimension = comm.getSpielfeldDim();
            dimx = dimension.x;
            dimy = dimension.y;
            Location[] fahnen = comm.getFahnenPos();
            String spielfeldstring = comm.getSpielfeld();
            try {
                boolean canPushersPushMultipleBots = comm.getCanPushersPushMultipleBots();
                intelliBoard = SimBoard.getInstance(dimx, dimy, spielfeldstring, fahnen);
                intelliBoard.setPusherCanPushMoreThanOneBot(canPushersPushMultipleBots);
                SimBoard tmp = SimBoard.getInstance(dimx, dimy, spielfeldstring, fahnen); // to be on the safe side,
                                                                                   // don't know if wisenhheimers may share a board
                tmp.setPusherCanPushMoreThanOneBot(canPushersPushMultipleBots);
                emergencyWisenheimer = new Wisenheimer(tmp);                																                     
            } catch (Exception e) {
                CAT.error("HumanPlayer has a problem: No Board" + e.getMessage(),e);
            }
        } catch (Exception e) {
            CAT.error("HumanPlayer has a problem: No Board!" + e.getMessage(),e);
        }
    }


    private void initView() {
        humanView = new HumanView(this);
        view = new View(humanView);          // adds the humanView to the JFrame
        ausgabe = new Ausgabe(host, port, nosplash, view);   //adds the AusgabeView to the JFrame
        if (ausgabe.initialize()) {
            Registry.getSingletonInstance().addClient(ausgabe, host, port);
            ausgabe.start();
        }
        else {
            CAT.warn("view.initialize returned false");
        }
        try {
            String [] names = comm.getNamen();
            int nameCount = names!=null?names.length:0;
            Bot [] bots = new Bot[nameCount];
            ScalableRegisterRow [] rows = new ScalableRegisterRow[nameCount];
            for (int i=0;i<nameCount;i++){
                String botname = names[i];
                bots[i] = ausgabe.getBot(botname);
                rows[i] = ausgabe.getInfoRegistersForBot(botname);
            }
            humanView.fillPhaseInfoPanel(bots, rows);
        }
        catch (KommException ke){
            CAT.error(ke.getMessage(), ke);
        }
        
    }

    /** Returns the size of the main JFrame or null if the JFrame
     *  is null for any reason.
     *  #*/
//    protected Dimension getViewSize() {
//      if (view!=null)
//        return view.getSize();
//
//      else
//        return null;
//    }



    private void showMessage(String foo) {
        ausgabe.getAusgabeView().showActionMessage(foo);
     }


 

    static class RoboTrackListener implements ActionListener {
        Bot r;

        RoboTrackListener(Bot r) {
            this.r = r;
        }

        public void actionPerformed(ActionEvent e) {
//	    ausgabe.setTracking( r.getName() );
//	    ausgabe.trackRob( r.getName() );
        }
    }

    private void abmelden() {
        comm.abmelden(name);
    }

    class EmergencyDirectionSubmitter extends TimerTask{
        public void run(){
            showMessage(Message.say("HumanPlayer","legalDirection"));
            if (humanView != null) {
                // calling the send method of HumanView (that will call the
                // corresponding send method in this class) so that we don't have to take 
                // care about removing the dialog window and telling the HumanView what
                // should be shown instead.
                humanView.setDialogInSidebarActive(false);
                humanView.sendDirection(Directions.NORTH);
            }
        }
    }

    class EmergencyStayPowerDownSubmitter extends TimerTask{
        public void run(){
            showMessage(Message.say("HumanPlayer","legalPowerDownResponse"));
            if (humanView != null) {
                // calling the send method of HumanView (that will call the
                // corresponding send method in this class) so that we don't have to take 
                // care about removing the dialog window and telling the HumanView what
                // should be shown instead.
                boolean again = getRob().getDamage() > 3;
                humanView.setDialogInSidebarActive(false);
                humanView.sendAgainPowerDown(again);
                
            }
        }
    }
    //
    class EmergencyRegRepairSubmitter extends TimerTask{
        
      //  Bot me;
        int repairPoints;
        public EmergencyRegRepairSubmitter (/*Bot me,*/ int repairPoints){
            //this.me = me;
            this.repairPoints = repairPoints;
        }
        
        public void run(){
            showMessage(Message.say("HumanPlayer","legalRegisterRepair"));
            if (humanView != null) {
                // calling the send method of HumanView (that will call the
                // corresponding send method in this class) so that we don't have to take 
                // care about removing the dialog window and telling the HumanView what
                // should be shown instead.
                ArrayList registerIndices = new ArrayList(repairPoints);
                for (int i=0;i<Bot.NUM_REG && repairPoints>0;i++){                    
                    if (emergencyBotCopy.getLockedRegister(i) != null ) {                       
                        // send repair method starts counting register at 1 (not 0)
                        registerIndices.add(new Integer(i+1));
                        repairPoints--;
                    }
                }
                humanView.setDialogInSidebarActive(false);
                humanView.sendRepairRegisters(registerIndices);      
                
            }
        }
    }
    
   
    private volatile static int idCounter=0;
    class EmergencyCardSubmitter extends TimerTask {
       
        //private Bot me;
        private int id; 
        private boolean gotCancel = false;
        public EmergencyCardSubmitter(/* Bot bot*/){
            //me = bot;
            id = idCounter++;
        }

        public void run() {
           // showMessage(Message.say("HumanPlayer", "closeToTimeout", BUFFER_SECONDS_BEFORE_SENDING_CARDS));
           int secondsLeft = BUFFER_SECONDS_BEFORE_SENDING_CARDS;
            synchronized (this){
                try {
                    CAT.debug(id+": waiting..");
                    while (secondsLeft>5){
                        if (gotCancel || cardsSent || gameOver) {
                            CAT.debug(id+": return as requested");
                            return;
                            
                        }
                        showMessage(Message.say("HumanPlayer", "closeToTimeout", secondsLeft));
                        secondsLeft-=5;
                        
                        wait(5000);                        
                    }
                    while (secondsLeft>0){
                        if (gotCancel || cardsSent || gameOver) {
                            CAT.debug(id+": return as requested");
                            return;
                        }
                        showMessage(Message.say("HumanPlayer", "closeToTimeout", secondsLeft));
                        secondsLeft--;                                                                         
                        wait(1000);
                    }
                   // wait (BUFFER_SECONDS_BEFORE_SENDING_CARDS*1000);
                }
                catch (InterruptedException ie){
                    CAT.warn(id+":interrupted while giving user a last chance for sending cards himself", ie);
                    if (gotCancel || cardsSent || gameOver) {
                        CAT.debug(id+": return as requested");
                        return;
                    }
                }
            }
            synchronized (comm) {
                CAT.debug(id+": in sync comm block");
                if (!gameOver && !cardsSent && !gotCancel) {
                    mode = MODE_OTHER;
                    CAT.debug(id+":going to send..");
                   
                    /*
                    Bot rob = getRob();                
                    int lockedRegisterCount =rob.countLockedRegisters();                
                    int[] prog = new int[(5-lockedRegisterCount)];
                    for (int i = 0; i < prog.length; i++) {
                        prog[i] = (i+1);
                    }
                    */
                   
                    humanView.setDialogInSidebarActive(false); // hide cardpanel
                    humanView.hidePhaseInfoCards();
                    int [] prog = getCompleteWisenheimerMove(emergencyBotCopy);                  
                    comm.registerProg(name,prog,false);                    
                   
                    humanView.setPanelToShow(HumanView.PANEL_PHASE_EVAL);
                    humanView.showPanel(HumanView.PANEL_PHASE_EVAL);
                    cardsSent = true;
                    showMessage(Message.say("SpielerMensch","legalZug"));
                   
                    }                
            }
            CAT.debug(id+": bye");
        }
        public boolean cancel(){
            gotCancel = true;
            CAT.debug(id+": CANCEL");
            boolean back = super.cancel();
            CAT.debug(id+": super.cancel call returned: "+back);
            synchronized (this) {
                CAT.debug(id+": notifyAll.");                
                this.notifyAll();                                
            }
            CAT.debug(id+": cancel end");
            return back;
           
        }
        
    }

    private void d(String s) {
        CAT.debug(s);
    }

}
