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

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Status;

public class ClientAntwort implements Comparable {
    /**
     * Ermoeglicht die Abfrage der Kommunikationsart mittels der Konstanten dieser Klasse
     */
    public int typ;
    /**
     * Container; enthaelt den Location, der bei ROBOTERPOS oder SPIELFELDDIM zurueckgegeben wird.
     */
    public Location ort;
    /**
     * Container; enthaelt die Positionen der Flaggen bei FAHNENPOS
     */
    public Location[] positionen;
    /**
     * Container; falls ein einzelner String geschickt wurde, ist er in str abgelegt, so zum Beispiel bei 'entfernung': dann steht der Entfernungsgrund in str
     */
    public String str;
    /**
     * Container; Falls mehrere Strings (Namen) geschickt wurden, sind diese in �namen� abgelegt (bei SPIELERNAMEN, SPIELSTAND)
     */
    public String[] namen;

    /**
     * Container; enthaelt die ausgeteilten Karten bei MACHEZUG
     */
    public Card[] karten;
    /**
     * Container; wird gesetzt, falls ein bool geschickt wird
     */
    public boolean ok;
    /**
     * Container; enthaelt bei SPIELSTATUS f�r jeden Bot ein Statusobjekt
     */
    public Status[] stati;
    /**
     * Container ; enthaelt bei TIMEOUT das Timeout, bei REPARATUR die Registerzahl
     */
    public int zahl;

    /**
     * added later to fix some timing bugs in (laser-)animations;
     * might be used for synchronizing "notify change"s and
     * information messages (i.e. to avoid that a bot is removed from the
     * board (triggered by a "notify change") before a possible laser animation
     * is shown (triggered by an information message)
     */
    public int messageSequenceNumber = -1;


    /**
     * Used by the MessageSequencer
     */
    public String specialMessageId;

    public Bot[] updatedBotsForNTC;

    /**
     * Used by the Sequencer to sort received messages by their number
     */
    public int compareTo(Object o) {
        ClientAntwort ca = (ClientAntwort) o;
        int num = ca.messageSequenceNumber;
        return messageSequenceNumber-num;
    }


    public final static int SPIELFELDDIM = 1;
    public final static int FAHNENPOS = 2;
    public final static int SPIELERNAMEN = 3;
    public final static int ROBOTERPOS = 4;
    public final static int SPIELSTAND = 5;
    public final static int SPIELSTATUS = 6;
    public final static int MACHEZUG = 7;
    public final static int ANGEMELDET = 8;
    public final static int ZERSTOERUNG = 9;
    public final static int REAKTIVIERUNG = 10;
    public final static int REPARATUR = 11;
    public final static int ENTFERNUNG = 12;
    public final static int AENDERUNG = 13;
    public final static int TIMEOUT = 14;
    public final static int SPIELSTART = 15;
    public final static int MESSAGE = 16;
    public final static int BOOLEAN = 17;

    /**
     * Liefert den (Konstanten-)Namen des Typs des Antwortobjektes zur�ck
     */
    public String getTyp() {
        switch (this.typ) {
            case SPIELFELDDIM:
                return "BOARD_DIMENSION";
            case FAHNENPOS:
                return "FLAG_POSITION";
            case SPIELERNAMEN:
                return "PLAYER_NAMES";
            case ROBOTERPOS:
                return "BOT_POSITION";
            case SPIELSTAND:
                return "GAME_RESULT";
            case SPIELSTATUS:
                return "GAME_STATUS";
            case MACHEZUG:
                return "MAKE_MOVE";
            case ANGEMELDET:
                return "REGISTERED";
            case ZERSTOERUNG:
                return "DESTRUCTION";
            case REAKTIVIERUNG:
                return "REACTIVATION";
            case REPARATUR:
                return "REPAIR";
            case ENTFERNUNG:
                return "DISTANCE";
            case AENDERUNG:
                return "CHANGE";
            case TIMEOUT:
                return "TIMEOUT";
            case SPIELSTART:
                return "GAME_START";
            case MESSAGE:
                return "MESSAGE";
            case BOOLEAN:
                return "BOOLEAN";
            default:
                return "ERROR - UNKNOWN KIND OF COMMUNICATION (Typ: " + this.typ + ")";
        }
    }

}








