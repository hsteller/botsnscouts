package de.botsnscouts.comm;
import java.io.*;
/** Diese Klasse enthaelt die Methoden, Objekte usw., die nicht in KommClient enthalten sind und vom Spieler nicht benoetigt werden
*@author Hendrik<BR>*/
public class  KommClientAusgabe extends KommClient {
  public KommClientAusgabe() {
  super();
  }
  /** 'anmelden' ist eine Methode zur Anmeldung beim Server. Die Anmeldung erfolgt mittels der Daten des Ausgabekanals [ Server-IP-Nr, Portnr, Clientname]. Falls die Anmeldung erfolgreich war, wird true zurueckgegeben, sonst false.
Trat ein nicht-technischer Fehler auf (d.h. beim Parsen), so wird eine Exception geworfen.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public boolean anmelden  (String ipnr, int portnr, String name) throws KommException{
  return super.anmelden (ipnr, portnr, name, "RGA");
  }

 public boolean anmelden2  (String ipnr, int portnr, String name) throws KommException{
  return super.anmelden (ipnr, portnr, name, "RA2");
  }

    

  /** Zur Bestaetigung fuer den Server, dass die Ausgaben beendet wurden und der Server weitermachen darf*/ 
  public void aenderungFertig() {
    //out.println ("ok");
    try {
      this.senden("ok");
    }
    catch (KommFutschException k) {
      System.err.println ("Exception bei KCA.aenderungfertig(von"+cn+"\nMessage: "+k.getMessage());
    }
  }
   
}
