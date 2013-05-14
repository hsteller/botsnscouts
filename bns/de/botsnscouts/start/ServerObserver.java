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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Category;

import de.botsnscouts.util.BNSThread;

/**
 * Observe the game start procedure.
 */
public class ServerObserver extends BNSThread {
    private static Category CAT = Category.getInstance(ServerObserver.class);

    private PlayersPanel playersPanel;

    // private WaiterThread waiter;

    private ServerSocket srvSocket;

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private int PORTNR = 8889;

    private boolean torun = true;

    private static final String NEW_PLAYER = "NSA";

    private static final String GAME_STARTS = "SGL";

    private static final String GAME_OVER = "SZE";

    public ServerObserver(PlayersPanel r) {
        super("StartSpLiwstener");
        boolean gotit = false;
        for (int i = PORTNR; (i < PORTNR + 10) && (!gotit); i++) {
            try {
                srvSocket = new ServerSocket(i);
                playersPanel = r;
                // waiter = playersPanel.parent.wth;
                gotit = true;
                PORTNR = i;
            }
            catch (Exception e) {
                CAT.error("could not find open Server socket.", e);
            }
        }
    }

    public void run() {
        while (torun) {
            if (listen()) {
                CAT.debug("listen ok");
                out.println("OK.");
                try {
                    socket.close();
                }
                catch (Exception e) {
                    CAT.error("StSpListener: Kann die Sockets nicht schliessen!");
                }
            }
            else {
                CAT.debug("listen not ok");
                error();
            }
        }
        CAT.debug("end of run(), terminating");
    }

    private boolean listen() {
        try {
            socket = srvSocket.accept();
            socket.setSoTimeout(30000);
            CAT.debug("A client connected.");
        }
        catch (Exception e) {
            CAT.error("in srvSocket.accept", e);
            return false;
        }

        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e) {
            CAT.error("while getting Streams", e);
            return false;
        }

        try {
            String fromclt = in.readLine();
            if (fromclt.substring(0, 3).equals(NEW_PLAYER)) {
                String name = in.readLine();
                CAT.debug("New player " + name);
                fromclt = in.readLine();
                int color = Integer.parseInt(fromclt);
                if ((color >= 0) && (color <= 7)) {
                    fireNewBot(name, color);
                    return true;
                }
                else {
                    return false;
                }
            }
            else
                if (fromclt.substring(0, 3).equals(GAME_STARTS)) {
                    fireGameStarted();
                    CAT.debug("Leaving listen()");
                    return true;
                }
                else
                    if (fromclt.substring(0, 3).equals(GAME_OVER)) {
                        fireGameFinished();
                        return true;
                    }
        }
        catch (NumberFormatException e) {
            CAT.info("client error", e);
            return false;
        }
        catch (IOException e) {
            CAT.error("io exc", e);
            return false;
        }
        return false;
    }

    public void fireGameFinished() {
        if (playersPanel != null && playersPanel.parent != null) {
            WaiterThread waiter = playersPanel.parent.getWaiterThread();
            if (waiter != null) {
                waiter.quitYourself();
            }
            else {
                CAT.debug("waiter was null");
            }
        }
        torun = false;
    }

    public void fireGameStarted() {
        playersPanel.gameStarted();
        playersPanel = null; // can be collected
    }

    /**
     * Called when a new bot has entered the game.
     * 
     * @param name
     *            Name of the new bot.
     * @param color
     *            Color of the new bot.
     */
    public void fireNewBot(String name, int color) {
        playersPanel.newBotEntered(name, color);
    }

    private void closeSock() {
        try {
            if (srvSocket != null)
                srvSocket.close();
            srvSocket = null;
        }
        catch (IOException ex) {
            CAT.error("while closing srvSocket", ex);
        }
        torun = false;
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
        catch (IOException ex) {
            CAT.error("while closing socket", ex);
        }

    }

    public void doShutdown() {
        torun = false;
        fireGameFinished();
        closeSock();
    }

    private void error() {
        /*
         * if (out != null) { out.println("error."); } try { if (socket != null) { socket.close(); } else { CAT.warn
         * ("in error(): socket was already null"); } } catch (IOException ex) { CAT.error("while closing sockets", ex); }
         */
        try {
            closeSock();
            // playersPanel.parent.wth.stopAllWaitingThreads();
            // playersPanel.parent.showLastShown();
        }
        catch (Exception e) {
            CAT.warn(e);
        }
    }
}
