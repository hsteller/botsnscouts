package de.botsnscouts.comm;
import de.botsnscouts.util.*;

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
    public Ort ort;
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

    /** zu Debug-/Informationszwecken:
	Die Methode liefert den Namen der ServerAntwort-Konstanten (mit Wert t) als String zurück.*/
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
	default: return "ERROR - UNKNOWN KIND OF COMMUNICATION";
	          
	}
    }


}
