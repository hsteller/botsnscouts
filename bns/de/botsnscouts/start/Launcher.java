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

import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import de.botsnscouts.autobot.*;
import de.botsnscouts.server.Server;
import org.apache.log4j.Category;

// launches human player, output ...

public class Launcher {

    static final Category CAT = Category.getInstance(Launcher.class);

    private Server server;

    // launches output
    public Thread watchAGame(String ip, int port, boolean noSplash) {
        Thread ret;
        try {
            ret = new BNSThread(new Ausgabe(ip, port, noSplash));
            ret.start();
        } catch (Exception exp) {
            return null;
        }
        return ret;
    }

    // launches human player
    public Thread participateInAGame(String ip, int port, String name, int farbe, boolean noSplash) {
        Thread ret;
        try {
            if (CAT.isDebugEnabled()) CAT.debug("Trying to start human player...");
            ret = new HumanPlayer(ip, port, name, farbe, noSplash);
            ret.start();
        } catch (Exception u) {
            CAT.error("Error while starting the game for player " + name + ": " + u.getMessage());
            return null;
        }
        return ret;
    }

    // launch autobots
    public Thread startAutoBot(String ip, int port, int iq) {
        return startAutoBot(ip, port, iq, false);
    }

    public Thread startAutoBot(String ip, int port, int iq, boolean beltAware) {
        return startAutoBot(ip, port, iq, beltAware, KrimsKrams.randomName());
    }

    public Thread startAutoBot(String ip, int port, int iq, boolean beltAware,
                                      String botName) {
        Thread ks;
        ks = new AutoBot(ip, port, iq, beltAware, botName);
        ks.start();
        ks.setPriority(java.lang.Thread.MIN_PRIORITY);
        return ks;
    }

    public void startGame(GameOptions options, ServerObserver listener) throws OneFlagException, NonContiguousMapException {
       server = new Server(options, listener);
       server.start();
   }


    public void gameStarts(String ip, int port) {
        server.startGame();
    }

    public void stopServer() {
        // Not nice, but this is the way it was done before...
        server.interrupt();
    }

}
