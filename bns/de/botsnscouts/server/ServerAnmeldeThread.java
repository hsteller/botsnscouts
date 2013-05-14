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

package de.botsnscouts.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Category;

import de.botsnscouts.comm.KommException;
import de.botsnscouts.comm.KommServerAusgabe;
import de.botsnscouts.comm.KommServerRoboter;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Global;

/**
 * Handhabt eine Connection nebenlaeufig. modified for 2.0 by Dirk
 */

public class ServerAnmeldeThread extends Thread {
    static final Category CAT = Category.getInstance(ServerAnmeldeThread.class);

    private ThreadMaintainer server;

    private Socket socket;

    private ServerAnmeldeOberThread oberThread;

    private static final int ILLEGAL = -1;

    private static final int SPIELER = 0;

    private static final int AUSGABE = 1;

    private static final int SPIELERV2 = 2;

    private static final int AUSGABEV2 = 3;

    public ServerAnmeldeThread(Server se, Socket so, ServerAnmeldeOberThread saot) {
        super("ServerAnmeldeThread");
        server = se;
        socket = so;
        oberThread = saot;
    }

    /**
     * Wartet server.anmeldeto auf eine Aktion, kreiert ggf. neue ServerRoboterThread- bzw ServerAusgabeThread-Objekte und haengt diese in die
     * richtigen Vektoren ein.
     */
    public void run() {
        CAT.debug("SERVER ANMELDTHREAD STARTED");
        try {
            PrintWriter out = null;
            BufferedReader in = null;
            int clienttype = ILLEGAL;
            String clientname = "";
            int farbe = -1;
            boolean exception = false;

            try { // Globales Try fuer's String Einlesen und Parsen.
                d("out = new PrintWriter    ...");
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                d("in  = new BufferedReader ...");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                d("... verbunden!");
                d("schaue nach ob Client etwas geschickt hat.");

                String erhalten;
                socket.setSoTimeout(server.getSignUpTimeout());
                erhalten = in.readLine();
                d("erhalten = " + erhalten);
                socket.setSoTimeout(0);

                int len = erhalten.length();

                if (len < 6) {
                    d("Zeichenkette kleiner 6, z.B. 'RGS(X)' min");
                    throw new FormatException();
                }

                if (erhalten.charAt(0) == 'R' && erhalten.charAt(1) == 'G' && erhalten.charAt(2) == 'S') {
                    d("Spieler");
                    clienttype = SPIELER;
                }
                else
                    if (erhalten.charAt(0) == 'R' && erhalten.charAt(1) == 'G' && erhalten.charAt(2) == 'A') {
                        d("Ausgabekanal");
                        clienttype = AUSGABE;
                    }
                    else
                        if (erhalten.charAt(0) == 'R' && erhalten.charAt(1) == 'S' && erhalten.charAt(2) == '2') {
                            d("Spieler V2");
                            clienttype = SPIELERV2;
                        }
                        else
                            if (erhalten.charAt(0) == 'R' && erhalten.charAt(1) == 'A' && erhalten.charAt(2) == '2') {
                                d("Ausgabe V2");
                                clienttype = AUSGABEV2;
                            }
                            else {
                                d("Falscher String");
                                throw new FormatException();
                            }

                if (erhalten.charAt(3) == '(' && erhalten.charAt(len - 1) == ')') {
                    for (int n = 4; n < (len - 1); n++)
                        clientname = clientname + erhalten.charAt(n);
                }
                else {
                    d("Klammerung falsch.");
                    throw new FormatException();
                }

                // Farbe parsen
                int kommapos = clientname.indexOf(',');
                if ((clienttype == SPIELERV2) && (kommapos != -1)) {
                    farbe = java.lang.Character.digit(clientname.charAt(clientname.length() - 1), 10);
                    clientname = clientname.substring(0, clientname.length() - 2);
                    d("farbe=" + farbe + "; clientname=" + clientname);
                }

                if (!nurLatein(clientname)) {
                    d("Name darf nur aus <a-z,A-Z>+ bestehen");
                    throw new FormatException();
                }

                d("Parsen erfolgreich. Clientname = " + clientname);
            }
            catch (SocketException e) {
                exception = true;
                d("SocketException ist aufgetreten.");
            }
            catch (IOException e) {
                exception = true;
                d("IOException ist aufgetreten.");
            }
            catch (FormatException e) {
                exception = true;
                d("FormatException ist aufgetreten.");
            }

            if (exception) {
                if (in != null)
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                    }
                out.close();
                return;
            }

            // OK, nun geeignete Objekte kreieren und einsortieren

            if ((clienttype == AUSGABE) || (clienttype == AUSGABEV2)) {
                KommServerAusgabe ksa = new KommServerAusgabe(in, out, "ServerComm_" + clientname);
                // sende 'ok' zur anmeldebestaetigung
                try {
                    ksa.anmeldeBestaetigung(true);
                }
                catch (KommException ke) {
                    d("ok konnte nicht an Ausgabekanal gesendet werden");
                    return;
                }
                ServerAusgabeThread neu = new ServerAusgabeThread(ksa, server.getOKListener(), server.getMOKListener(),
                                server.getInfoRequestAnswerer(), server.getOutputThreadMaintainer());

                if (clienttype == AUSGABEV2)
                    neu.setVersion(2);
                else
                    neu.setVersion(1);

                synchronized (oberThread.namen) {
                    if (oberThread.isLegalName(clientname)) {
                        oberThread.addName(" ( " + clientname + " ) ");
                        server.addOutput(neu);
                        d("neuen Ausgabethread erzeugt");
                    }
                    else {
                        d("Name schon vorhanden. Kille die Verbindung..");
                        out.println("REN(SO(SchonAngemeldeterName))");
                        try {
                            in.close();
                        }
                        catch (IOException e) {
                        }
                        out.close();
                        return;
                    }
                }
                return;
            }

            if ((clienttype == SPIELER) || (clienttype == SPIELERV2)) {
                // darf ein Spieler sich anmelden?
                d("Ein Spieler versucht sich an der Anmeldung.");
                synchronized (oberThread.roboterAnmeldung) {
                    if (oberThread.roboterAnmeldung == Boolean.FALSE) {
                        d("Keine Roboteranmeldungen jetzt. Kille Verbindung");
                        out.println("REN(SO(SpielLaeuftSchon))");
                        try {
                            in.close();
                        }
                        catch (IOException e) {
                        }
                        out.close();
                        return;
                    } // oberThread.roboterAnmeldung false
                    d("Er darf jedenfalls, vom Server aus.");

                    synchronized (oberThread.namen) {
                        if (!oberThread.isLegalName(clientname)) {
                            d("Name schon vergeben. Kille Verbindung");
                            out.println("REN(SO(SchonVergebenerName))");
                            try {
                                in.close();
                            }
                            catch (IOException e) {
                            }
                            out.close();
                            return;
                        } // Name illegal
                        d("Der Name ist jedenfalls noch nicht vergeben.");

                        synchronized (oberThread.anzSpieler) {
                            if (oberThread.anzSpieler.intValue() == server.getMaxPlayers()) {
                                d("Zuviele Spieler. Kille Verbindung");
                                out.println("REN(ZS)");
                                try {
                                    in.close();
                                }
                                catch (IOException e) {
                                }
                                out.close();
                                return;
                            } // Zuviele Spieler
                            d("Noch nicht zuviele Spieler.");

                            farbe = server.allocateColor(farbe, clientname);
                            d("Farbe Nr. " + farbe + " zugeteilt.");

                            Bot h = Bot.getNewInstance(clientname);
                            h.setBotVis(farbe);
                            KommServerRoboter komm = new KommServerRoboter(in, out, "ServerComm_" + clientname);
                            try {
                                komm.anmeldeBestaetigung(true);
                            }
                            catch (KommException ke) {
                                d("ok konnte nicht an roboter gesendet werden");
                                return;
                            }
                            d("ok an Spieler geschickt.");

                            oberThread.anzSpieler = new Integer(oberThread.anzSpieler.intValue() + 1);
                            d("" + oberThread.anzSpieler + ". Bot mit Name " + clientname + " erzeugt.");

                            ServerRoboterThread neu = new ServerRoboterThread(h, server.getOKListener(),
                                            server.getInfoRequestAnswerer(), server.getRobThreadMaintainer(), komm);
                            server.addRobotThread(neu);
                            d("ServerRoboterThread erzeugt und einsortiert.");
                            oberThread.addName(" " + clientname + " ");
                            server.updateNewBot(clientname, farbe);

                            if (oberThread.anzSpieler.intValue() >= server.getMaxPlayers()) { // alle
                                                                                              // da
                                try {
                                    sleep(5000);
                                }
                                catch (InterruptedException ex) {
                                    d("InterruptedException " + ex);
                                }
                                synchronized (server) {
                                    d("server.notify()");
                                    server.notify();
                                }
                            } // if maxspieler angemeldet
                        } // synchronized oberThread.anzSpieler
                    } // synchronized oberThread.namen
                } // synchronized oberThread.roboterAnmeldung
            } // clienttype==spieler
        }
        catch (Throwable t) {
            CAT.fatal("Exception:", t);
        }
        CAT.debug("SERVER ANMELDETHREAD REACHED END OF RUN");
    } // run

    private boolean nurLatein(String s) {
        if (s == null)
            return false;
        else {
            int l = s.length();
            String su = new String(s.toUpperCase());
            for (int i = 0; i < l; i++)
                if ((su.charAt(i) > 'Z') || (su.charAt(i) < 'A'))
                    return false;
            return true;
        }
    }

    private void d(String a) {
        Global.debug(this, a);
    }
}