package de.botsnscouts.util;

/** Hilfsklasse, deren Objekte die Informationen enthalten, die beim Info-Request 'gibSpielstatus' benoetigt werden
*@author Hendrik<BR>
*@version 12.7.99 22:40
*/
public class Status {
  
  /** Die aktuelle Phase der Registerauswertung*/
  public int aktPhase;
  /** Der Name des Roboters, zu dem der Status gehoert*/
  public String robName;
  /** Die Karten, die in den bisher ausgewerteten Registern liegen.(in der richtigen Reihenfolge*/
  public Karte [] register;
  
 
  public Status () {}
  public Status (int phase, String name, Karte [] karten){
    aktPhase=phase;
    robName=name;
    register=karten;
  }
  /** Liefert einen String, der die Attribute des Statusobjektes mit ihren Werten enthaelt*/ 
  public  String toString () {
    if (this != null)
      return ("Name= "+this.robName +" , aktPhase= "+this.aktPhase+" , Register= "+cardsToString(this.register)); 
    else 
      return "NULL";
  }
  /** Formt fuer die toString-Methode ein Kartenarray in einen String um, der die Karten in der Form PK(aktion,proritaet) durch Kommata getrennt auflistet
   */
  private static String cardsToString (Karte [] k){
    String raus="";
    for (int i=0;i<k.length;i++) {
      if (k[i]!=null)
	raus = raus+"PK("+k[i].getaktion()+","+k[i].getprio()+"), ";
      else
	raus+="null, ";
      
    }
    
    return raus;
  }
}
