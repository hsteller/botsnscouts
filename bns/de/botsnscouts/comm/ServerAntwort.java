package de.spline.rr;

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



  public final int PROGRAMMIERUNG=1;
  public final int AUSRICHTUNG=2;
  public final int REAKTIVIERUNG=3;
  public final int REPARATUR=4;
  public final int ABMELDUNG=5;
  public final int AENDERUNGFERTIG=6; 
  public final int GIBSPIELFELDDIM=7; 
  public final int GIBSPIELFELD=8; 
  public final int GIBFAHNENPOS=9; 
  public final int GIBNAMEN=10; 
  public final int GIBROBOTERPOS=11; 
  public final int GIBFELDINHALT=12; 
  public final int GIBROBSTATUS=13;
  public final int GIBSPIELSTAND=14; 
  public final int GIBAUSWERTUNGSSTATUS=15;
  public final int GIBTIMEOUT=16;
  public final int GIBFARBEN=17;
  public final int MESSAGE=18;

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
    default: return "ERROR - UNKNOWN KIND OF COMMUNICATION";
	          
    }
 }


}
