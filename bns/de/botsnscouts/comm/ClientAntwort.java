package de.spline.rr;

public class ClientAntwort {
    /** Ermoeglicht die Abfrage der Kommunikationsart mittels der Konstanten dieser Klasse
     *@author Hendrik<BR>*/
    public int typ;
    /** Container; enthaelt den Ort, der bei ROBOTERPOS oder SPIELFELDDIM zurueckgegeben wird.*/
    public Ort ort;
    /** Container; enthaelt die Positionen der Flaggen bei FAHNENPOS*/
    public Ort [] positionen;
    /** Container; falls ein einzelner String geschickt wurde, ist er in str abgelegt, so zum Beispiel bei 'entfernung': dann steht der Entfernungsgrund in str*/
    public String str;
    /** Container; Falls mehrere Strings (Namen) geschickt wurden, sind diese in ´namen´ abgelegt (bei SPIELERNAMEN, SPIELSTAND) */
    public String [] namen;
    /** Container; wird nicht mehr benoetigt, da wir gibFeldinhalt doch nicht brauchen.*/
    public Feld feld; 
    /** wohl doch nicht benoetigt*/
    public Roboter roboter; 
    /** Container; enthaelt die ausgeteilten Karten bei MACHEZUG*/
    public Karte [] karten;
    /** Container; wird gesetzt, falls ein bool geschickt wird*/
    public boolean ok;
    /** Container; enthaelt bei SPIELSTATUS für jeden Roboter ein Statusobjekt*/
    public Status [] stati;
    /** Container ; enthaelt bei TIMEOUT das Timeout, bei REPARATUR die Registerzahl */
    public int zahl;
    
    public final static int SPIELFELDDIM=1;
    public final static int FAHNENPOS=2;
    public final static int SPIELERNAMEN=3;
    public final static int ROBOTERPOS=4;
    public final static int SPIELSTAND=5;
    public final static int SPIELSTATUS=6;
    public final static int MACHEZUG=7;
    public final static int ANGEMELDET=8;
    public final static int ZERSTOERUNG=9;
    public final static int REAKTIVIERUNG=10;
    public final static int REPARATUR=11;
    public final static int ENTFERNUNG=12;
    public final static int AENDERUNG=13;
    public final static int TIMEOUT=14;
    public final static int SPIELSTART=15;
    public final static int MESSAGE=16; 
    /** Liefert den (Konstanten-)Namen des Typs des Antwortobjektes zurück*/ 
    public String getTyp() {
	switch (this.typ) {
	case SPIELFELDDIM: return "SPIELFELDDIM";
	case FAHNENPOS: return "FAHNENPOS";
	case SPIELERNAMEN: return "SPIELERNAMEN";
	case ROBOTERPOS: return "ROBOTERPOS";
	case SPIELSTAND: return "SPIELSTAND";
	case SPIELSTATUS: return "SPIELSTATUS";
	case MACHEZUG: return "MACHEZUG";
	case ANGEMELDET: return "ANGEMELDET";
	case ZERSTOERUNG: return "ZERSTOERUNG";
	case REAKTIVIERUNG: return "REAKTIVIERUMG";
	case REPARATUR: return "REPARATUR";
	case ENTFERNUNG: return "ENTFERNUNG";
	case AENDERUNG: return "AENDERUNG";
	case TIMEOUT: return "TIMEOUT";
	case SPIELSTART: return "SPIELSTART";
	case MESSAGE: return "MESSAGE";
	default: return "ERROR - UNKNOWN KIND OF COMMUNICATION (Typ: "+this.typ+")";
	}
    }
    
}








