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

import de.botsnscouts.board.FlaggenException;
import de.botsnscouts.board.SpielfeldKS;
import de.botsnscouts.comm.ClientAntwort;
import de.botsnscouts.comm.KommClientSpieler;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.util.*;
import org.apache.log4j.Category;

/** AutoBot implements the computer-controlled robots.
 */
public class AutoBot extends BNSThread {

    static final Category cat = Category.getInstance(AutoBot.class);

    public AutoBot(String ip, int port) {
        this(ip, port, 0);
    }

    /**
     * This constructor also gets a playing strength: the higher malus
     * is, the worse the 'bot will be. 0 is best, therefore.
     */
    public AutoBot(String i, int p, int malus) {
        ip = i;
        port = p;
        this.malus = malus;
        realname = KrimsKrams.randomName();
        super.setName("AutoBot:" + realname);
    }

    String ip;
    int port;
    int malus;

    final String realname;
    SearchRecursively wirbel;
    String fieldAsString;
    Location[] flags;

    Bot myBot = Bot.getNewInstance("OrgRobbi");
    KommClientSpieler myComm = new KommClientSpieler();
    ClientAntwort answer = new ClientAntwort();

    SpielfeldKS myMap;

    /**
     * run-Methode erzeugt zufaelligen Namen fuer den kuenstlichen Spieler, meldet ihn an
     * und wartet dann auf Nachrichten vom Server, die entsprechend beantwortet werden
     */
    public void run() {
        boolean gameRunning;

        try {
            myComm.anmelden(ip, port, realname);
        } catch (KommException e) {
            cat.error("Could not connect", e);
            return;
        }

        try {
            answer = myComm.warte();
            if (answer.typ == answer.SPIELSTART) {
                myComm.spielstart();
            }
        } catch (KommException e) {
            cat.error("Didn't get game start signal", e);
            return;
        }
        gameRunning = true;

        while (gameRunning) {
            cat.debug("Waiting...");
            try {
                answer = myComm.warte();
            } catch (KommException kE) {
                cat.error("Got an exception while waiting", kE);
                return;
            }

            cat.debug("Got an answer, type: " + answer.getTyp());

            switch (answer.typ) {
                case (ClientAntwort.ZERSTOERUNG):
                    if (myMap == null) {
                        initField();
                        wirbel = new SearchRecursively(myMap, malus);
                        myMap.setDebug(false);
                    }
                    updateBot();

                    cat.debug("handling destroyed request...");
                    handleDestroyedRequest();
                    break;

                case (ClientAntwort.REPARATUR):
                    cat.debug("handling repair request...");
                    updateBot();
                    handleRepairRequest(answer.zahl);
                    break;

                case (ClientAntwort.MACHEZUG):
                    boolean powerdown = false;
                    for (int i = 0; i < answer.karten.length; i++)
                        cat.debug("Card " + i + " is " + answer.karten[i].getprio() + "|" + answer.karten[i].getaktion());
                    updateBot();
                    Bot simRob = Bot.getCopy(myBot);  // Kopieren fuer spaetere Powerdown-Simulationen

                    Card[] maxCards = new Card[9];

                    for (int i = 0; i < answer.karten.length; i++)
                        maxCards[i] = answer.karten[i];
                    Bot pRob = Bot.getCopy(myBot);
                    // copy locked registers
                    for (int i = 0; i < pRob.getLockedRegisters().length; i++)
                        pRob.setZug(i, pRob.getLockedRegister(i));
                    pRob.zeige_Roboter();

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
                        simRobs[0].setZug(i - 1, vonPermut[i - 1]);
                        myMap.doPhase(i, simRobs);       // geplante Belegung simulieren
                        simRobs[0].setZug(i - 1, null);
                    }
                    if ((simRobs[0].getDamage() > 5) || (java.lang.Math.random() < (((double) simRobs[0].getDamage() - 1) * 0.1))) {
                        simRobs[0].setAktiviert(false);
                        simRobs[0].setSchaden(0);
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
                    cat.info("Was removed! Reason:" + answer.str + "\nSending ack.");
                    gameRunning = false;
                    myComm.bestaetigung();
                    break;

                default:
                    cat.warn("Illegal msg from server. Type:" + answer.getTyp());
                    break;
            }     //Ende switch
        }         //Ende while
        cat.debug("End of run()...");
    }


    /**
     * get current status from server into myBot
     */
    public Bot updateBot() {
        try {
            myBot = myComm.getRobStatus(realname);
        } catch (KommException e) {
            cat.error("Could not update myself", e);
        }
        return myBot;
    }

    /**
     * erzeugt mit der Spielfelddimension, den Fahnenpositionen und dem
     * Spielfeldstring das Spielfeld des kuenstlichen Spielers, ruft
     * ausserdem die Entfernungsberechnung in SpielfeldKS auf
     */
    public void initField() {
        cat.debug("initializing field...");
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
                myMap = SpielfeldKS.getInstance(dimx, dimy, fieldAsString, flags);

            } catch (FlaggenException fe) {
                cat.warn("Flag on pit", fe);
            } catch (FormatException e) {
                cat.error("Malformed field", e);
            }
        } catch (KommException e) {
            cat.error("Did not get a field", e);
        }
    }

    public void handleRepairRequest(int reparatur) {
        cat.debug("repairing "+reparatur+" registers.");
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

        testRobbi.zumArchiv();

        int bestDistance = 9999;
        int newDistance;

        cat.debug("Bot destroyed. Looking for new facing.");
        for (int i = 0; i < 4; i++) {
            testRobbi.setAusrichtung(i);
            newDistance = myMap.getEntfernung(testRobbi);
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
}