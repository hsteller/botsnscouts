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
import de.botsnscouts.util.Location;

/** Hilfsklasse zum String-Entschluesseln
*@author Hendrik<BR>
*/
public class ServerAntwort {

    /**Ermoeglicht mithilfe der Konstanten die Bestimmung der Kommunikationsart

     */
    public int typ;
    /** Container*/
    public String name;
    /** Container*/
    public Location ort;
    /** Container*/
    public int wohin;
    /** Container*/
    public boolean ok;
    /** Container*/
    public int [] register;
    public String[] msg;



    public static final int PROGRAMMIERUNG=1;
    public static final int AUSRICHTUNG=2;
    public static final int REAKTIVIERUNG=3;
    public static final int REPARATUR=4;
    public static final int ABMELDUNG=5;
    public static final int AENDERUNGFERTIG=6; 
    public static final int GIBSPIELFELDDIM=7; 
    public static final int GIBSPIELFELD=8; 
    public static final int GIBFAHNENPOS=9; 
    public static final int GIBNAMEN=10; 
    public static final int GIBROBOTERPOS=11; 
    public static final int GIBFELDINHALT=12; 
    public static final int GIBROBSTATUS=13;
    public static final int GIBSPIELSTAND=14; 
    public static final int GIBAUSWERTUNGSSTATUS=15;
    public static final int GIBTIMEOUT=16;
    public static final int GIBFARBEN=17;
    public static final int MESSAGE=18;
    public static final int STATS=19;
    public static final int MSG_ACK=20;
    public static final int IS_SCOUT_ALLOWED=21;
    public static final int IS_WISENHEIMER_ALLOWED=22;
    public static final int CAN_PUSHERS_PUSH_MORE_THAN_ONE_BOT=23;
    
    /** zu Debug-/Informationszwecken:
	Die Methode liefert den Namen der ServerAntwort-Konstanten (mit Wert t) als String zur�ck.*/
    public String getTyp () {
	switch (this.typ) {
	case PROGRAMMIERUNG:  return "PROGRAMMIERUNG (TRP)";
	case AUSRICHTUNG: return "AUSRICHTUNG (TNR)";
	case REAKTIVIERUNG: return "REAKTIVIERUNG (TBD)";
	case REPARATUR: return "REPARATUR (TRR)";
	case ABMELDUNG: return "ABMELDUNG (RLE)";
	case AENDERUNGFERTIG: return "AENDERUNGFERTIG (OK)";
	case GIBSPIELFELDDIM: return "GIBSPIELFELDDIM (GSD)";
	case GIBSPIELFELD: return "GIBSPIELFELD (GPL)";
	case GIBFAHNENPOS: return "GIBFAHNENPOS (GFL)";
	case GIBNAMEN: return "GIBNAMEN (GSN)";
	case GIBROBOTERPOS: return "GIBROBOTERPOS (SRO)";
	case GIBFELDINHALT: return "GIBFELDINHALT (SFI)";
	case GIBROBSTATUS: return "GIBROBSTATUS (GRS)";
	case GIBSPIELSTAND: return "GIBSPIELSTAND (GSS)";
	case GIBAUSWERTUNGSSTATUS: return "GIBAUSWERTUNGSSTATUS (GSA)";
	case GIBTIMEOUT: return "GIBTIMEOUT (GTO)";
	case GIBFARBEN: return "GIBFARBEN (GSF)";
	case MESSAGE: return "MESSAGE (MSG)";
	case STATS: return "STATS (GST)";
	case MSG_ACK: return "MSG_ACK (MOK)";
	case IS_SCOUT_ALLOWED : return "IS SCOUT ALLOWED (ISS)";
	case IS_WISENHEIMER_ALLOWED: return "IS WISENHEIMER ALLOWED (ISW)";
	case CAN_PUSHERS_PUSH_MORE_THAN_ONE_BOT: return "CAN_PUSHERS_PUSH_MORE_THAN_ONE_BOT (ISPPMB)";
	default: return "ERROR - UNKNOWN KIND OF COMMUNICATION";
	          
	}
    }


}
