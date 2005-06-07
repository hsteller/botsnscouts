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
    private Wisenheimer wisenheimer;

    private boolean isScoutAllowed = true;
    private boolean isWisenheimerAllowed = true;
    
    private Timer timeoutWatcher;
    private EmergencyCardSubmitter emergencyCardSubmitter;
    
    private volatile boolean cardsSent;
    /** Starting the EmergencyCardSubmitter that many seconds before the global timeout expires*/
    public static final int bufferSecondsBeforeTimeout = 25;
    /** This is the number of seconds we show to the user; after this many seconds the
     *   EmergencyCardSubmitter will kick into action, blocking the user and getting the
     *   Wisenheimer's prediction to send*/
    public static final int bufferSecondsBeforeSendingCards = bufferSecondsBeforeTimeout-5;
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
                    	    timeoutWatcher.schedule(emergencyCardSubmitter, (globalTimeout-bufferSecondsBeforeTimeout)*1000);                    	    
                    	}                    	
                        // card
                        showMessage(Message.say("SpielerMensch", "mwartereg"));

                        try {
                            Bot meAtStartOfRound = comm.getRobStatus(name);                                                       
                            if (CAT.isDebugEnabled()) {
                                CAT.debug("rob has the following locked registers: ");
                                for (int i = 0; i < meAtStartOfRound.getLockedRegisters().length; i++) { 
	                                CAT.debug("index: " + i + " ist " + meAtStartOfRound.getLockedRegister(i));
	                            }
                            }
                            humanView.updateRegisters(meAtStartOfRound.getLockedRegisters());
                        } 
                        catch (KommException kE) {
                            CAT.error("SpielerMenschERROR: " + kE.getMessage());
                        }

                        // ----- Karten einsortieren  -----
                        cards.clear();
                        for (int i = 0; i < commAnswer.karten.length; i++) {
                            cards.add(i, new HumanCard(commAnswer.karten[i]));
                        }
                        humanView.showCards(cards);
                        
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
                        humanView.showGetDirection();
                        Global.debug(this, "Habe eine Zerstoerung bekommen.");
                        showMessage(Message.say("SpielerMensch", "roboauffeld"));
// ---get board for wisenheimer
                        if (intelliBoard == null) {
                            initIntelligentBoard();
                            wisenheimer = new Wisenheimer(intelliBoard);
                            initScoutAndWisenheimerPermissions();
                        }

                        // ----- ask for timeout -------
                        if (globalTimeout == 0) {
                            try {
                                globalTimeout = comm.getTimeOut();
                            } catch (KommException kE) {
                                System.err.println("SpielerMenschKommunkationsERROR: wollte Timeout erfragen: " + kE.getMessage());
                            }
                        }
                        break;
                    }
                    // robot reaktivated
                case (ClientAntwort.REAKTIVIERUNG):
                    {
                        showMessage(Message.say("SpielerMensch", "roboreaktiviert"));
                        // ask for powerDownagain
                        humanView.showRePowerDown();


                        break;
                    }

                    // repair your registers
                case (ClientAntwort.REPARATUR):
                    {
                        Global.debug(this, "Reparatur erhalten");

                        try {
                            Global.debug(this, "Reparatur erhalten; ersuche, Status von " + name + "  zu erfragen...");
                            Bot tempRob = comm.getRobStatus(name);
                            int repPoints = commAnswer.zahl;
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
                        try {
                            String[] gewinnerListe = comm.getSpielstand();
                            if (gewinnerListe != null) {
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
                        String removalReason = commAnswer.str;
                        gameOver = true;
                        humanView.showGameOver(dead, rating, removalReason);
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
            e.printStackTrace();
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
	                timeoutWatcher.cancel();
	           
	                comm.registerProg(name, sendProg, nextTurnPowerDown);
	                cardsSent = true;
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
        CAT.debug("killing/interrupting potential EmergenyCardSubmitter:");
        if (timeoutWatcher != null){
            try {
                timeoutWatcher.cancel();
            }
            catch (Exception e){
                CAT.warn("in shutdown (timeoutWatcher)", e);
            }              
        }
        if (emergencyCardSubmitter != null){
            try {
                emergencyCardSubmitter.cancel();
            }
            catch (Exception e){
                CAT.warn("in shutdown (emergencyCardSubmitter)", e);
            }              
        }

        if (view != null){
            // in view.showGameOver() a timer Thread gets started that will call view.wait() for atm 8 seconds 
            synchronized (view){
                view.notifyAll();
            }
        }
        view = null;
        
    }


    protected void passUpdatedScout(int chosen, Bot[] robs) {
        ausgabe.showScout(chosen, robs);
    }


    protected void sendRepair(ArrayList respReparatur) {
        d("sende meinen Reparaturwunsch: " + respReparatur);
        int[] repa = new int[respReparatur.size()];
        for (int i = 0; i < respReparatur.size(); i++) {
            repa[i] = ((Integer) respReparatur.get(i)).intValue();
        }
        comm.respReparatur(name, repa);
        showMessage(Message.say("SpielerMensch", "sendregrep"));
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
        comm.respZerstoert(name, r);
        // show wait message
    }


    protected void sendAgainPowerDown(boolean down) {
        comm.respReaktivierung(name, down);
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
    
    protected int [] getCompleteWisenheimerMove(){
        // RegisterArray.resetAll(); // resetting move so far
        // RegisterArray.getWisenheimerCards(); // getting possible locked registers
        	//wisenheimer.getPrediction(cardList, ausgabe.getBot(name));
        Bot me = ausgabe.getBot(name);       
        ArrayList myRegs = new ArrayList(Bot.NUM_REG);
        int numOfCardsNeeded = Bot.NUM_REG;       
        for (int i=0;i<Bot.NUM_REG;i++){
            Object card = me.getLockedRegister(i);            
            myRegs.add(card);
            if (card != null) { // register was locked, one turn less to predict
                numOfCardsNeeded--;
            }
        }
      
        int [] predictions = new int[numOfCardsNeeded];
        if (numOfCardsNeeded > 0 ) {
            synchronized (wisenheimer) {
	            // getting initial prediction/resetting prediction, probably doing the same work
	            // twice if the wisenheimer was already called in this round 
	            predictions [0] = wisenheimer.getPrediction(myRegs, cards, me);	         
	            for (int i=1;i<numOfCardsNeeded;i++) {
	                predictions[i] = wisenheimer.getNextPrediction(myRegs, cards);	                
	            }
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
                intelliBoard = SimBoard.getInstance(dimx, dimy, spielfeldstring, fahnen);
            } catch (Exception e) {
                System.err.println("HumanPlayer has a problem: No Board" + e);
            }
        } catch (Exception e) {
            System.err.println("HumanPlayer has a problem: No Board!" + e);
        }
    }


    private void initView() {
        humanView = new HumanView(this);
        view = new View(humanView);          // adds the humanView to the JFrame
        ausgabe = new Ausgabe(host, port, nosplash, view);   //adds the AusgabeView to the JFrame
        ausgabe.initialize();
        Registry.getSingletonInstance().addClient(ausgabe, host, port);
        ausgabe.start();
//	ChatPane chatpane=new ChatPane(this);
//	view.addChatPane(chatpane);
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
        /*  while (mode!=MODE_PROGRAM) {
            CAT.debug("waiting for next turn to quit..");
            try {
              synchronized(this){
                wait();
              }
            }
            catch (InterruptedException ie){
              CAT.error(ie.getMessage(), ie);
            }

          }
          */
        comm.abmelden(name);
    }

    class EmergencyCardSubmitter extends TimerTask {
       
        public EmergencyCardSubmitter( ){
        }

        public void run() {
            showMessage(Message.say("HumanPlayer", "closeToTimeout", bufferSecondsBeforeSendingCards));
            synchronized (this){
                try {
                    wait (bufferSecondsBeforeSendingCards*1000);
                }
                catch (InterruptedException ie){
                    CAT.error("interrupted while giving user a last chance for sending cards himself", ie);
                }
            }
            synchronized (comm) {
                if (!gameOver && !cardsSent) {
                    mode = MODE_OTHER;
                    showMessage(Message.say("SpielerMensch","legalZug"));
                   
                    /*
                    Bot rob = getRob();                
                    int lockedRegisterCount =rob.countLockedRegisters();                
                    int[] prog = new int[(5-lockedRegisterCount)];
                    for (int i = 0; i < prog.length; i++) {
                        prog[i] = (i+1);
                    }
                    */
                    int [] prog = getCompleteWisenheimerMove();
                    comm.registerProg(name,prog,false);
                    cardsSent = true;
                   
                    humanView.setDialogInSidebarActive(false); // hide cardpanel
                    }                
            }
            
        }
        
    }

    private void d(String s) {
        Global.debug(this, s);
    }

}
