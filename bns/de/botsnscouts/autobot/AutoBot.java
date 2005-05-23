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

package de.botsnscouts.autobot;

import org.apache.log4j.Category;

import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.comm.ClientAntwort;
import de.botsnscouts.comm.KommClientSpieler;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Location;

/** AutoBot implements the computer-controlled robots.
 */

public class AutoBot extends BNSThread  {
  
    static final Category CAT = Category.getInstance(AutoBot.class);
       
    public AutoBot(String ip, int port) {
        this(ip, port, 0);
    }

    public AutoBot(String ip, int port, int malus) {
        this(ip, port, malus, false, KrimsKrams.randomName());
    }
    /**
     * This constructor also gets a playing strength: the higher malus
     * is, the worse the 'bot will be. 0 is best, therefore.
     */
    public AutoBot(String i, int p, int malus, boolean beltAware, String name) {
        super("AutoBot:" + name);
        realname = name;
        ip = i;
        port = p;
        this.malus = malus;
        this.beltAware=beltAware;        
        
    }

    String ip;
    int port;
    int malus;
    boolean beltAware;

    final String realname;
    DistanceCalculator calc;
    SearchRecursively wirbel;
    String fieldAsString;
    Location[] flags;

    Bot myBot = Bot.getNewInstance("AutoBotRobbi");
    KommClientSpieler myComm = new KommClientSpieler();
    ClientAntwort answer = new ClientAntwort();

    SimBoard myMap;
    private boolean gameRunning;
    /**
     * run-Methode erzeugt zufaelligen Namen fuer den kuenstlichen Spieler, meldet ihn an
     * und wartet dann auf Nachrichten vom Server, die entsprechend beantwortet werden
     */
    public void run() {
      try {

        try {
            CAT.debug("sending registration");
            boolean ok = myComm.anmelden(ip, port, realname);
            if (!ok) {
                CAT.error("AutoBot '"+realname+"' failed to register :-(");
                shutdown();
                return;
            }
        } catch (KommException e) {
            CAT.error("Could not connect", e);
            return;
        }

        try {
            answer = myComm.warte();
            if (answer.typ == ClientAntwort.SPIELSTART) {
                myComm.spielstart();
            }
            else {
                CAT.warn("was expecting gamestart message but got: "+answer.getTyp());
            }
        } catch (KommException e) {
            CAT.error("Didn't get game start signal", e);
            return;
        }
        gameRunning = true;

        while (gameRunning) {
            CAT.debug("Waiting...");
            try {
                answer = myComm.warte();
            } catch (KommException kE) {
                CAT.error("Got an exception while waiting", kE);               
                return;
            }

            CAT.debug("Got an answer, type: " + answer.getTyp());

            switch (answer.typ) {
                case (ClientAntwort.ZERSTOERUNG):
                    if (myMap == null) {
                        initField();
                        if (beltAware){
                            calc=AdvDistanceCalculator.getInstance(myMap);
                            CAT.debug("got adv dist calc");
                        } else {
                            calc=SimpleDistanceCalculator.getInstance(myMap);
                            CAT.debug("got simple dist calc");
                        }
                        wirbel = new SearchRecursively(myMap, malus, calc);
                        myMap.setDebug(false);
                    }
                    updateBot();

                    CAT.debug("handling destroyed request...");
                    handleDestroyedRequest();
                    break;

                case (ClientAntwort.REPARATUR):
                    CAT.debug("handling repair request...");
                    updateBot();
                    handleRepairRequest(answer.zahl);
                    break;

                case (ClientAntwort.MACHEZUG):
                    boolean powerdown = false;
                    for (int i = 0; i < answer.karten.length; i++)
                        CAT.debug("Card " + i + " is " + answer.karten[i].getprio() + "|" + answer.karten[i].getAction());
                    updateBot();
                    Bot simRob = Bot.getCopy(myBot);  // Kopieren fuer spaetere Powerdown-Simulationen

                    Card[] maxCards = new Card[9];

                    for (int i = 0; i < answer.karten.length; i++)
                        maxCards[i] = answer.karten[i];
                    Bot pRob = Bot.getCopy(myBot);
                    // copy locked registers
                    for (int i = 0; i < pRob.getLockedRegisters().length; i++)
                        pRob.setMove(i, pRob.getLockedRegister(i));
                    pRob.debug();

                    Card[] vonPermut = wirbel.findBestMove(maxCards, pRob);

                    // Absende-Karten vorbereiten
                    int kartenZahl;
                    if (answer.karten.length > 5)
                        kartenZahl = 5;
                    else
                        kartenZahl = answer.karten.length;

                    int[] anServer = new int[kartenZahl];
                    int k = 0;
                    int l = 0;
                    for (int i = 0; i < answer.karten.length; i++) {
                        int j = 0;
                        while ((j < answer.karten.length) && (k < 5)) {
                            if (answer.karten[j].getprio() == vonPermut[k].getprio()) {
                                anServer[l++] = (j + 1);
                            }
                            j++;
                        }
                        k++;
                        if (j == answer.karten.length) {
                            i--;
                        }
                    }

                    Bot[] simRobs = new Bot[1];         // Powerdown !?
                    simRobs[0] = simRob;
                    int schadenAlt = simRob.getDamage();
                    for (int i = 1; i < 6; i++) {
                        simRobs[0].setMove(i - 1, vonPermut[i - 1]);
                        myMap.doPhase(i, simRobs);       // geplante Belegung simulieren
                        simRobs[0].setMove(i - 1, null);
                    }
                    if ((simRobs[0].getDamage() > 5) || (java.lang.Math.random() < (((double) simRobs[0].getDamage() - 1) * 0.1))) {
                        simRobs[0].setActivated(false);
                        simRobs[0].setDamage(0);
                        for (int i = 1; i < 6; i++) {                    // die Phase mit powerdown simulieren
                            myMap.doPhase(i, simRobs);
                        }
                        if (simRobs[0].getDamage() <= schadenAlt)
                            powerdown = true;
                    }

                    // Send cards
                    myComm.registerProg(realname, anServer, powerdown);
                    break;

                case (ClientAntwort.REAKTIVIERUNG):
                    myComm.respReaktivierung(realname, false); // Anwort: Bot wiedereinsetzen
                    break;

                case (ClientAntwort.ENTFERNUNG):
                    CAT.info("Was removed! Reason:" + answer.str + "\nSending ack.");
                    gameRunning = false;
                    myComm.bestaetigung();
                    break;

                default:
                    CAT.warn("Illegal msg from server. Type:" + answer.getTyp());
                    break;
            }     //Ende switch
        }         //Ende while
      } // Ende of first "try" 
      finally {
        CAT.debug("End of run()...calling shutdown in case there is sime IO left to clean up..");
        doShutdown();
        CAT.info("Autobot "+realname+" finished");
      }
     }


    /**
     * get current status from server into myBot
     */
    public Bot updateBot() {
        try {
            myBot = myComm.getRobStatus(realname);
        } catch (KommException e) {
            CAT.error("Could not update myself", e);
        }
        return myBot;
    }

    /**
     * erzeugt mit der Spielfelddimension, den Fahnenpositionen und dem
     * Spielfeldstring das Board des kuenstlichen Spielers, ruft
     * ausserdem die Entfernungsberechnung in DistanceCalculator auf
     */
    public void initField() {
        CAT.debug("initializing field...");
        int dimx, dimy;
        Location dimension;

        try {
            dimension = myComm.getSpielfeldDim();
            dimx = dimension.x;
            dimy = dimension.y;

            flags = myComm.getFahnenPos();

            fieldAsString = myComm.getSpielfeld();
            //d(spielfeldstring);

            try {
                myMap = SimBoard.getInstance(dimx, dimy, fieldAsString, flags);
            } catch (FlagException fe) {
                CAT.warn("Flag on pit", fe);
            } catch (FormatException e) {
                CAT.error("Malformed field", e);
            }
        } catch (KommException e) {
            CAT.error("Did not get a field", e);
        }
    }

    public void handleRepairRequest(int reparatur) {
        CAT.debug("repairing "+reparatur+" registers.");
        int[] regsToUnlock = new int[reparatur];
        int regsFound = 0;
        for (int i = 0; i < 5; i++) {
            if (myBot.getLockedRegister(i) != null) {
                if (regsFound < reparatur) {
                    regsToUnlock[regsFound] = i + 1;
                    regsFound++;
                }
            }
        }
        myComm.respReparatur(realname, regsToUnlock);
    }

    public void handleDestroyedRequest() {
        int direction = 0;
        Bot testRobbi = Bot.getCopy(myBot);

        testRobbi.toArchive();

        int bestDistance = 9999;
        int newDistance;

        CAT.debug("Bot destroyed. Looking for new facing.");
        for (int i = 0; i < 4; i++) {
            testRobbi.setFacing(i);
            newDistance = calc.getGoodness(testRobbi);
            if (newDistance < bestDistance) {
                bestDistance = newDistance;
                direction = i;
            }
        }
        myComm.respZerstoert(realname, direction);
    }

    public static void main(String[] args) {
        int sPort = 0;
        AutoBot spK;
        int malus = 0;
        String host = "";

        // Kommandozeilenparameter auswerten

        try {
            switch (args.length) {
                case 3:
                    malus = Integer.parseInt(args[2]);
                case 2:
                    sPort = Integer.parseInt(args[1]);
                case 1:
                    host = args[0];
                    break;
                default:
                    throw new IllegalArgumentException();
            } // switch
        } catch (Exception e) {
            System.err.println("Usage: java de.botsnscouts.autobot.AutoBot <host> <port>");
        }

        if (host.equals(""))
            host = "localhost";
        if (sPort == 0)
            sPort = 8077;

        spK = new AutoBot(host, sPort, malus);

        spK.start();
    }
    
    public void doShutdown() {
        	CAT.debug("shutting down..");
        	gameRunning = false;
        	try {
        	    CAT.debug("deregistering from server if still possible..");
        	    myComm.abmelden(realname);
        	}
        	catch (Exception e){
        	    CAT.debug("during deregister", e);
        	}
        	try {
        	    CAT.debug("killing communication..");
        	    myComm.shutdown();
        	}
        	catch (Exception e){
        	    CAT.debug(e);
        	}        
        	
    }
     
   
    
    
}