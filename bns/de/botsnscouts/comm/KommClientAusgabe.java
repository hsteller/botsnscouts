package de.botsnscouts.comm;

import de.botsnscouts.util.Stats;
import de.botsnscouts.util.StatsList;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

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
      try {
	  this.senden("ok");
      }
      catch (KommFutschException k) {
	  System.err.println ("Exception bei KCA.aenderungfertig(von"+cn+"\nMessage: "+k.getMessage());
	  k.printStackTrace();
      }
  }

    /** Acknowledges a Message has been received */
    public void acknowledgeMsg() throws KommException{
	this.senden("MOK");
    }

    /** This methods asks for the actual game stats (laserhits).
	@exception ..if an error occurs
	@return A sorted list of Stats-objects.
    */
    
    public StatsList getStats() throws KommException {
	// format: GST(name,int,int,int,int,name,..,name,int,int,int,int)
	senden("GST");
	String rein = einlesen();
	if (rein.startsWith("GST")) {
	    Vector stats = new Vector();
	    StringTokenizer st = new StringTokenizer (rein.substring(4,rein.length()-1),",");
	    while (st.hasMoreElements()) {
		Stats s = new Stats (st.nextToken());
	    try {
		s.setHits(Integer.parseInt(st.nextToken()));
		s.setKills(Integer.parseInt(st.nextToken()));
		s.setDamageByBoard(Integer.parseInt(st.nextToken()));
		s.setDamageByRobots(Integer.parseInt(st.nextToken()));
		stats.addElement(s);
	    }
	    catch (NumberFormatException nfe) {
		nfe.printStackTrace();
	    }
	    }
	    return new StatsList(stats);
	}
	else 
	    throw new KommException ("getStats: Wrong answer: "+rein);
    }
	

}
    
   

