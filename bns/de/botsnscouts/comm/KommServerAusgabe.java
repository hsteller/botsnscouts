package de.spline.rr;

import java.io.*;
// STAND : fertig 
/**  
Klasse fuer die Komunikation des Servers mit Ausgabekanälen
*@author Hendrik<BR> */
 public class KommServerAusgabe extends KommServer {

 public KommServerAusgabe (BufferedReader in, PrintWriter out) {
 super (in, out);
 }

  /** Zur Benachrichtigung der Ausgabekanäle von Änderungen.
* Teilt (Ausgabe) mit, bei welchen Robotern Änderungen eingetreten sind.
* Erhält die Namen als Argument.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
*/
   public void aenderung (String [] roboternamen)  throws KommException {
     try {
       String raus = "NTC(";
       for (int i=0;i<roboternamen.length;i++)
	 raus = raus + roboternamen[i]+",";
       raus +=")";
       out.println(raus);
       }
     catch (Exception not_existant) {
       throw new KommException ("Bei \"Aenderung\" trat eine Exception auf(Message: "+not_existant.getMessage()+")");
     }
   }
     /** Zum &uuml;bermitteln von Nachrichten/Ereignissen f&uuml; die Ausgabe
	 @param id Die Art der Nachricht bzw ihre id f&uuml;r die Message-Klasse */
     public void message (String id) {
	 out.println("MSG("+id+")");
     }
     /**  Zum &uuml;bermitteln von Nachrichten/Ereignissen f&uuml; die Ausgabe
	  @param id Die Art der Nachricht bzw ihre id f&uuml;r die Message-Klasse 
	  @param name1 Ein zus&auml;tzliches Argument, f&uuml;r die Ausgabe von Nachrichten*/
     public void message (String id, String name1) {
	 out.println("MSG("+id+","+name1+")");
     }
     public void message (String id, String name1, String name2) { 
	 out.println("MSG("+id+","+name1+","+name2+")"); 
     }
     public void message (String id, String name1, String name2, String name3) { 
	 out.println("MSG("+id+","+name1+","+name2+","+name3+")"); 
     }
     public void message (String id, String name1, String name2, String name3, String name4) { 
	 out.println("MSG("+id+","+name1+","+name2+","+name3+","+name4+")"); 
     } 
     public void message (String id, String [] namen) {
	 String send="MSG("+id;
	 if (namen!=null) {
	     send+=",";
	     for (int i=0;i<namen.length;i++) {
		 send+=namen[i]+",";
	     }
	 }
	 send+=")";
	 
	 out.println(send);
     }
 }
