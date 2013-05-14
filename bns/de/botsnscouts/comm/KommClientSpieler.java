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

package de.botsnscouts.comm;

import de.botsnscouts.server.RegistrationException;
import de.botsnscouts.util.Encoder;

/**
 * Klasse fuer die Dinge, die nicht in 'KommClient' vorkommen und die die
 * Ausgabe nix angehen
 * 
 * @author Hendrik<BR>
 */
public class KommClientSpieler extends KommClient {

    public KommClientSpieler() {
        super();
    }

    /**
     * 'anmelden' ist eine Methode zur Anmeldung beim Server. Die Anmeldung
     * erfolgt mittels der Daten des Spielers [Server-IP-Nr,
     * Portnummer,Spielername]. Falls die Anmeldung erfolgreich war, wird true
     * zurueckgegeben, sonst false. Trat ein nicht-technischer Fehler auf (d.h.
     * beim Parsen), so wird eine Exception geworfen. NEW: Encodes the client
     * name
     * 
     * @exception KommException
     *                Tritt beim Parsen ein Fehler auf (z.B. wegen falsch
     *                aufgebauten Strings), wird eine KommException geworfen.
     */
    public boolean anmelden(String ipnr, int portnr, String name) throws KommException, RegistrationException {
        return super.anmelden(ipnr, portnr, Encoder.commEncode(name), "RGS");
    }

    /**
     * Anmeldung; eine Farbe zwischen 1 und 8 waehlen NEW: Encodes the client
     * name
     * */
    public boolean anmelden2(String ipnr, int portnr, String name, int farbe) throws KommException,
                    RegistrationException {
        if ((farbe > 0) && (farbe < 9)) {
            return super.anmelden(ipnr, portnr, (Encoder.commEncode(name) + "," + farbe), "RS2");
        }
        else
            return super.anmelden(ipnr, portnr, Encoder.commEncode(name), "RGS");
    }

    /**
     * Antwort-Methode, die uebermittelt, wie der Bot nach einer Zerstoerung
     * aufs Feld gesetzt werden soll. Sie erhaelt den Namen des Roboters und
     * einen Integer fuer die Richtung (n=0, w=3, e=1, s=2)
     * 
     * @param robName
     *            WILL BE IGNORED
     * 
     * */
    public void respZerstoert(String robName, int richtung) {

        String back = "TNR(" + encodedName + ",";
        // richtung%=4;
        // System.err.println ("CLIENT: zerstoert erhielt Richtung: "+richtung);
        if (richtung == 0)
            back += "N)";
        else
            if (richtung == 1)
                back += "E)";
            else
                if (richtung == 2)
                    back += "S)";
                else
                    back += "W)";
        try {
            this.senden(back);
        }
        catch (KommFutschException k) {
            System.err.println("Exception bei respReaktivierung: \n Message: " + k.getMessage());
        }
    }

    /**
     * Antwort-Methode, die uebermittelt, welche kaputten Register der Bot
     * repariert haben moechte. Erhaelt als Argument die Nummern der
     * gewuenschten Register.
     * 
     * @param robName
     *            WILL BE IGNORED
     * 
     * */
    public void respReparatur(String robName, int[] registerNr) {
        String back = "TRR(" + encodedName + ",";
        for (int i = 0; i < registerNr.length; i++)
            back += registerNr[i] + ",";
        back += ")";
        try {
            this.senden(back);
        }
        catch (KommFutschException k) {
            System.err.println("Exception bei respReaktivierung: \n Message: " + k.getMessage());
        }
    }

    /**
     * Methode zur Abgabe der Registerprogrammierung. Die Methode erhaelt als
     * Argumente (abgesehen vom Namen) zum einen einen Boolean, der angibt, ob
     * ein Power-Down fuer die naechste Runde geplant ist (ja=true), zum anderen
     * ein int-Array, dass die Registerprogrammierung enthaelt (z.B. wuerde
     * {3,7,4,1,9} bedeuten, dass das erste Register die dritte ausgeteilte Card
     * enthaelt, das zweite Register die siebte ausgeteilte Card usw..). Falls
     * Register gesperrt sind, wird ein entsprechend kuerzeres Array uebergeben
     * (siehe "Protokolle und Datenformate").
     * 
     * @param name
     *            WILL BE IGNORED
     */
    public void registerProg(String name, int[] programmierung, boolean powerDown) {
        String back = "TRP(" + encodedName + ",(";
        for (int i = 0; i < programmierung.length; i++)
            back += programmierung[i] + ",";
        back += "),"; // Ende des Karten-Teils
        if (powerDown)
            back += "t)";
        else
            back += "f)";
        try {
            this.senden(back);
        }
        catch (KommFutschException k) {
            System.err.println("Exception bei respReaktivierung: \n Message: " + k.getMessage());
        }
        // System.err.println
        // ("CLIENT: schicke folgende Programmierung: "+back);
    }

    /**
     * Diese Methode dient zur Antwort auf die Frage, ob der powerdown-Bot
     * deaktiviert bleiben will. Abgesehen von seinem Namen �bergibt der Bot
     * einen boolean; ist dieser true, heisst das, dass der Bot deaktiviert
     * bleiben moechte.
     * 
     * @param name
     *            WILL BE IGNORED
     */
    public void respReaktivierung(String name, boolean bleibeDeaktiv) {
        String raus = "TBD(";
        raus += encodedName;
        raus += ",";
        if (bleibeDeaktiv)
            raus += "t";
        else
            raus += "f";
        raus += ")";
        try {
            this.senden(raus);
        }
        catch (KommFutschException k) {
            System.err.println("Exception bei respReaktivierung: \n Message: " + k.getMessage());
        }
    }

    /** Schickt eine Nachricht an alle anderen Spieler */
    public void message(String id, String[] namen) {
        String send = "MSG(" + id;
        if (namen != null) {
            send += ",";
            for (int i = 0; i < namen.length; i++) {
                send += Encoder.commEncode(namen[i]) + ",";
            }
        }
        send += ")";
        try {
            this.senden(send);
        }
        catch (KommFutschException k) {
            System.err.println("Exception bei Message: \n Message: " + k.getMessage());
        }
    }
}
