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

package de.botsnscouts.start;

import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Global;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Observe the game start procedure.
 */
public class StSpListener extends BNSThread {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(StSpListener.class);
    public PlayersPanel par = null;
    private WaiterThread waiter;

    private ServerSocket srv = null;
    private Socket clt = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;

    int PORTNR = 8889;
    private boolean torun = true;
    private String fromclt = null;

    public StSpListener(PlayersPanel r) {
        super("StartSpListener");
        boolean gotit = false;
        for (int i = PORTNR; (i < PORTNR + 10) && (!gotit); i++)
            try {
                srv = new ServerSocket(i);
                par = r;
                waiter = par.parent.wth;
                gotit = true;
                PORTNR = i;
            } catch (Exception e) {
                System.err.println("StSpListener: Kann ServerSocket nicht öffnen:" + e + "\nprobiert: " + i);
            }

    }

    public void run() {
        try {
            while (torun) {
                if (listen()) {
                    CAT.debug("listen ok");
                    ok();
                } else {
                    CAT.debug("listen not ok");
                    error();
                }
            }
        } catch (Exception e) {
            CAT.debug(e.getMessage(), e);
        }
        CAT.debug("Habe ende meiner run() methode erreicht");
    }


    public boolean listen() {
        try {
            clt = srv.accept();
            clt.setSoTimeout(30000);
            //System.out.println("Ein Klient!");
        } catch (Exception e) {
            System.err.println("StSpListener: Kann nicht ACCEPT!");
        }

        try {
            pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
        } catch (Exception e) {
            System.err.println("StSpListener: Kann nicht getOutputStream!");
        }

        try {
            br = new BufferedReader(new InputStreamReader(clt.getInputStream()));
        } catch (Exception e) {
            System.err.println("StSpListener: Kann nicht getInputStream!");
        }

        try {
            pw.println("StartSpielerListener ist bereit.");
            //if(br.ready())
            fromclt = br.readLine();
        } catch (Exception e) {
            System.err.println("StSpListener: Kann nicht println/readLine!");
        }
        //System.out.println(fromclt.substring(0,4));
        Global.debug(this, "empfange " + fromclt);
        try {
            if (fromclt.substring(0, 3).equals("NSA")) {//neuerSpielerAngemeldet
                String name = br.readLine(); //SpielerName
                Global.debug(this, "Neuer Spieler! " + name);//!!!!!
                fromclt = br.readLine();
                int farbe = Integer.parseInt(fromclt); //Farbe als Zahl zw. 1 u. 7
                if ((farbe >= 0) && (farbe <= 7)) {
                    updateNewBot(name, farbe);
                    return true; //alles war OK
                } else
                    return false;
            } else if (fromclt.substring(0, 3).equals("SGL")) { //SpielGehtLos
                updateStartGame();
                CAT.debug("Leaving listen()");
                return true;
            } else if (fromclt.substring(0, 3).equals("SZE")) { //SpielZuEnde
                updateGameFinished();
                return true;
            }

        } catch (Exception e) {
            System.err.println("StSpListener: Klientenfehler!" + e);
        }
        return false; //fehler ist aufgetreten
    }

    public void updateGameFinished() {
        if (waiter != null)
            waiter.beende();
        else
            CAT.debug("waiter was null");
        torun = false;
    }

    public void updateStartGame() {
        par.spGL();
        par = null; // can be collected
    }

    /**
     * Called when a new bot has entered the game.
     * @param name   Name of the new bot.
     * @param color  Color of the new bot.
     */
    public void updateNewBot(String name, int color) {
        par.neurob(name, color);
    }


    public void ok() {
        pw.println("OK.");
        try {
            //   srv.close();
            clt.close();
        } catch (Exception e) {
            CAT.error("StSpListener: Kann die Sockets nicht schliessen!");
        }
    }

    void closeSock() {
        try {
            srv.close();
            torun = false;
            clt.close();
        } catch (Exception e) {
            try {
                clt.close();
            } catch (Exception ex) {
                System.err.println("StSpListener: Kann die Sockets nicht schliessen!" + ex);
            }
            System.err.println("StSpListener: Kann die Sockets nicht schliessen!" + e);
        }
    }


    public void error() {
        if (pw != null) {
            pw.println("error.");
        }
        try {
            //   srv.close();
            clt.close();
        } catch (Exception e) {
            System.err.println("StSpListener: Kann die Sockets nicht schliessen!");
        }
    }
}
