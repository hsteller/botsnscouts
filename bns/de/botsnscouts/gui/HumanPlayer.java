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

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.comm.ClientAntwort;
import de.botsnscouts.comm.KommClientSpieler;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.util.*;
import de.botsnscouts.widgets.GreenTheme;
import org.apache.log4j.Category;

import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * logic for the human player
 * @author Lukasz Pekacki
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
                        mode = MODE_PROGRAM;
                        Global.debug(this, "I am requested to send cards");
                        // card
                        showMessage(Message.say("SpielerMensch", "mwartereg"));

                        try {
                            Bot tempRob = comm.getRobStatus(name);
                            d("rob has the following locked registers: ");
                            for (int i = 0; i < tempRob.getLockedRegisters().length; i++) d("index: " + i + " ist " + tempRob.getLockedRegisters()[i]);
                            humanView.updateRegisters(tempRob.getLockedRegisters());
                        } catch (KommException kE) {
                            System.err.println("SpielerMenschERROR: " + kE.getMessage());
                        }

                        // ----- Karten einsortieren  -----
                        cards.clear();
                        for (int i = 0; i < commAnswer.karten.length; i++) {
                            cards.add(i, new HumanCard(commAnswer.karten[i]));
                        }
                        humanView.showCards(cards);
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
                        Global.debug(this, "Habe einee Zerst”rung bekommen.");
                        showMessage(Message.say("SpielerMensch", "roboauffeld"));
// --- Board f\uFFFDr den Klugscheisser holen
                        if (intelliBoard == null) {
                            initIntelligentBoard();
                            wisenheimer = new Wisenheimer(intelliBoard);
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
                            System.err.println("SpielerMensch: " + kE.getMessage());
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
                                showMessage(Message.say("SpielerMensch", "spielende"));
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
                        gameOver = true;
                        humanView.showGameOver(dead, rating);
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
            CAT.debug("Waiting for Ausgabe (join())..");
            ausgabe.join();
            CAT.debug("Ausgabe is now ready,,");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CAT.info("HUMANPLAYER FINISHED!");
        return;

    }

    /**
     * Main-Methode, die den menschlichen Spieler von der Shell aus als Thread startet
     */
    public static void main(String[] args) {
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
        String name, host = "127.0.0.1";
        int port = 8077, farbe = 0;
        if (args[0] == null)
            name = KrimsKrams.randomName();
        else
            name = args[0];
        int tmpInt;
        switch (args.length) {
            case 2:
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
                break;
            case 3:
                host = args[1];
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
                    System.err.println(e);
                }
                break;
            case 4:
                host = args[1];
                try {
                    port = Integer.parseInt(args[2]);
                    farbe = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    System.err.println(e);
                }
        }
        MetalLookAndFeel.setCurrentTheme(new GreenTheme());
        (new HumanPlayer(host, port, name, farbe)).start();
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
        comm.registerProg(name, sendProg, nextTurnPowerDown);
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
    protected void quit() {
        if (CAT.isDebugEnabled()) {
            CAT.debug(name + "was called to quit");
            CAT.debug("sending quit to server..");
        }
        comm.abmelden(name);
        CAT.debug("setting condition for leaving the run()-method");
        gameOver = true;

        //Dafuer sorgen, dass Thread aufhoert
        //System.exit(0);
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
        return wisenheimer.getNextPrediction(registerList, cardList);
    }

    protected int getPrediction(ArrayList registerList, ArrayList cardList) {
        return wisenheimer.getPrediction(registerList, cardList, ausgabe.getBot(name));
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


    private void d(String s) {
        Global.debug(this, s);
    }

}
