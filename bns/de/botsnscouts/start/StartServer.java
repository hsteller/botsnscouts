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

import de.botsnscouts.server.*;
import de.botsnscouts.util.*;
import de.botsnscouts.autobot.*;

/**
 *  StartServer startet neuen Spielserver oder künstliche Spieler auf Requests
 *  von StartSpieler
 *
 *@author     Ludmila und Leo Scharf
 * created    22. April 2001
 */
public class StartServer extends BNSThread {
    private KommStSrv com;
    private KommStSrvPass comPass;
    private int spcnt = 0;

    private Server[] servArr = new Server[255];
    private int[] servPort = new int[255];
    private String[] cltIP = new String[255];
    //IP des Klienten, der diesen Server startete
    private int[] cltPort = new int[255];
    private int servNum = 0;

    StartServer() {
        super("StartServer");
    }

    public void run() {
        try {
            com = new KommStSrv();
            comPass = new KommStSrvPass();
        } catch(Exception e) {
            System.err.println("StartServer: Socket ist möglicherweise belegt!");
            return;
        }
        Global.debug(this, "Bots_n_Scouts StartServer ist bereit.");
        while(true) {
            spcnt = com.listen();
            if(spcnt == 0) {
                System.err.println("StartServer: Ein Fehler ist bei der Verbindung aufgetreten");
                com.closeSock();
            } else if(spcnt == 9999) {
                Global.debug(this, "Beende mich");
                for(int i = 0; i < servArr.length; i++) {
                    if(servArr[i] != null) {
                        Global.debug(this, "server #" + i + " is alive:" + ((Thread) servArr[i]).isAlive());
                    }
                }
                com.ok();
                com.closeSock();
                comPass.closeSock();
                Global.debug(this, "Beendet!");
                return;
            } else if(spcnt == 9) {
                //Spiel geht los
                int i = 0;
                int prt = com.port;
                for(i = 0; i < 255; i++) {
                    if(servPort[i] == prt) {
                        break;
                    }
                }
                if(i < 255) {
                    Global.debug("Server " + i);
                    servArr[i].startGame();
                    /*
                     * synchronized( servArr[i] )
                     * {
                     * servArr[i].notify();
                     * }
                     */
                    Global.debug("Auf Port " + com.port + " geht's los!");
                    com.ok();
                } else {
                    Global.debug("Kein Server mit Port " + com.port);
                    com.error();
                }
            } else if(spcnt < 0) {
                //Künstliche Spieler starten
                for(int i = 0; i < (-spcnt); i++) {
                    AutoBot tmp = new AutoBot("127.0.0.1", com.port, com.iq);
                    tmp.start();
                    tmp.setPriority(Thread.MIN_PRIORITY);
                    //     Global.debug(""+spcnt+" künstliche Spieler, "+com.port+" Port");
                }
                com.ok();
            } else if(spcnt > 10000) {
                //Spiel abbrechen
                Global.debug(this, "Breche das Spiel ab!");
                int port = spcnt - 10000;
                int srvindex = searchServ(port);
                if(srvindex >= 0 && srvindex < servArr.length && servArr[srvindex] != null) {
                    ((Server) servArr[srvindex]).interrupt();
                    Global.debug(this, "Spiel abgebrochen!");
                    servArr[srvindex] = null;
                    servPort[srvindex] = 0;
                } else {
                    Global.debug(this, "Kein Server mit port #" + port);
                }
                com.ok();
            }
            else {
                //neuen Server starten
                boolean exists = false;
                for(int i = 0; i < servNum; i++) {
                    if(com.port == servPort[i]) {
                        exists = true;
                    }
                }
                if(!exists) {
                    Global.debug(this, "Neues Spiel mit " + spcnt + " Mitspieler, Port " + com.port);

                    servArr[servNum++] = new Server(
                            new GameOptions(spcnt, com.port, com.zugTimeout,
                                            com.feld, com.flags, com.feldX, com.feldY),
                            this);
                    ((Server) servArr[servNum - 1]).start();
                    servPort[servNum - 1] = com.port;
                    cltIP[servNum - 1] = com.cltIP;
                    cltPort[servNum - 1] = com.cltPort;
                    Global.debug(this, "Server nr." + (servNum - 1) + ":" + servPort[servNum - 1]);
                    com.ok();
                } else {
                    com.error();
                }

            }
        }

    }

    /**
     *@param  s  Description of Parameter
     */

    public void spielGehtLos(Server s) {
        int si = searchServ(s);
        //SpielGehtLos
        if(!comPass.sendString("SGL\n", cltIP[si], cltPort[si])) {
            System.err.println("StartServer: Kann nicht mit dem StartSpielerListener kommunizieren!(SGL)");
        }
    }

    public boolean neuerSpieler(String name, int farbe, ThreadMaintainer s) {
        if(!comPass.sendString("NSA\n" + name + "\n" + farbe, cltIP[searchServ((Server) s)], cltPort[searchServ((Server) s)])) {
            System.err.println("StartServer: Kann nicht mit dem StartSpielerListener kommunizieren!(NSA)" + cltPort[searchServ((Server) s)] + " " + cltIP[searchServ((Server) s)]);
            return false;
        } else {
            return true;
        }
    }

    public boolean spielZuEnde(Server s) {
        boolean ret = false;
        Global.debug(this, "sende SZE an:" + cltIP[searchServ(s)] + ":" + cltPort[searchServ(s)]);

        if(!comPass.sendString("SZE\n", cltIP[searchServ(s)], cltPort[searchServ(s)])) {
            System.err.println("StartServer: Kann nicht mit dem StartSpielerListener kommunizieren!(NSA)" + cltPort[searchServ(s)] + " " + cltIP[searchServ(s)]);
            ret = false;
        } else {
            Global.debug(this, "ok, SZE");
            ret = true;
        }
        int si = searchServ(s);
        servArr[si] = null;

        int k = 0;
        for(int i = servNum - 1; i >= 0; i--) {
            if(servArr[i] == null) {
                k++;
            }
        }

        for(int z = 0; z < k; z++) {
            for(int i = 0; i < servNum; i++) {
                if(servArr[i] == null) {
                    Global.debug(this, "Server nr." + i + "wird gelöscht:" + servArr[i]);
                    for(int j = i; j < servNum - 1; j++) {
                        servArr[j] = servArr[j + 1];
                        servPort[j] = servPort[j + 1];
                        cltIP[j] = cltIP[j + 1];
                        cltPort[j] = cltPort[j + 1];
                    }
                    servPort[servNum] = 0;
                    servArr[servNum] = null;
                    cltIP[servNum] = null;
                    cltPort[servNum] = 0;
                    i = servNum;
                    servNum--;
                }
            }
        }
        return ret;
    }

    private int searchServ(Server s) {
        for(int i = 0; i < servNum; i++) {
            if((Server) servArr[i] == s) {
                return i;
            }
        }
        return -1;
    }

    private int searchServ(int port) {
        for(int i = 0; i < servNum; i++) {
            if(servPort[i] == port) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] argv) {
        (new StartServer()).start();
    }
}
