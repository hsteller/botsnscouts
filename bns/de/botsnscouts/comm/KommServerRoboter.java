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

import de.botsnscouts.util.*;
import java.io.*;
// STAND: 2.7.99. 15:40 ; fertig
/**
 *Klasse für die Kommunikation des Servers mit Spielern
 *@author Hendrik
*/

public class KommServerRoboter extends KommServer {

  public KommServerRoboter (BufferedReader in, PrintWriter out){
  super (in, out);
  }

  /** Zur Aufforderung an den Spieler, seinen Zug zu machen.
   * Erhält als Argument seine Karten. Server muss sie sich merken, da später als Antwort lediglich übermittelt wird, die wievielte ausgeteilte Karte in einem Register liegt.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat 
   */
  public void zugabgabe (Karte [] k)  throws KommException{
    try {
      String raus = "MRP(";
      for (int i=0;i<k.length;i++) {
	  if (k[i]!=null)
	      raus = raus+"PK("+k[i].getaktion()+","+k[i].getprio()+")";
      }
      raus+=")";
      out.println (raus);
    }
   
    catch (Exception blafasel){
      throw new KommException ("Bei \"Zugabgabe\" trat  eine Exception auf");
    }
  }
  
  /**Diese Methode dient der Benachrichtigung des Roboters von seiner Zerstörung.
	@exception KommException wird geworfen, wenn beim Senden ein
Fehler auftritt  
   */ 
  public void zerstoert() throws KommException {
    try {
      out.println ("MNR");
    }
    catch (Exception e) {
      throw new KommException ("zerstoert: Ecxception beim Schreiben");
    }
    /*
      // Dieser Teil war für die Originalversion der Methode gedacht,die einen int liefern sollte
      try {
      ServerAntwort xyz= wait2( in.readLine());
      if (xyz.typ==xyz.AUSRICHTUNG)
	return xyz.wohin;
      else 
	throw new KommException ("zerstoert: Falsches Antwortobjekt(typ:"+getTyp(xyz.typ)+")");
    }
    catch (IOException ioe) {
      throw new KommException ("KommServerRoboter.zerstoert warf eine IOException");
    }
    */
  }
    
  /** 'reaktivierung' ist die Anfrage an einen 'power-down'-Roboter, ob er wieder mitmachen will.
   *  Falls er wieder dabei sein will, ist der Rückgabewert true, sonst false.
   *  Bei falscher Antwort wird eine Exception geworfen. 
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
   */
  public void reaktivierung () throws KommException {
    try {
      out.println ("MBD");
    }
    catch (Exception hae) {
      throw new KommException ("reaktivierung: Exception beim Senden");
    }
    /* try {
      ServerAntwort back = wait2(in.readLine());
      if (back.typ==back.REAKTIVIERUNG)
	  return back.ok; // siehe KommServer.wait2 fuer diesen Fall
      else 
	throw new KommException ("reaktivierung: Falsches Antwortobjekt(typ:"+back.getTyp()+")");
      
    }
    catch (IOException ioe) {
      throw new KommException ("reaktivieung: IOException beim Empfangen der Antwort");
    }
    */
   
  }
         /** 'regReparatur' ist zum Herausfinden da, welche(s) Register ein Roboter repariert haben will und erhält als Argument die Registeranzahl, die der Roboter reparieren darf. Es werden die Nummern der Register zurückgeliefert, die der Roboter reparieren möchte.
 Bei falscher Antwort wird eine Exception geworfen.
	  
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public void regReparatur (int Registeranzahl) throws KommException {
    try {
      String raus ="MRR("+Registeranzahl+")";
      out.println (raus);
    }
    catch (Exception e) {
      throw new KommException ("regReparatur: Exception beim Senden");  
    }
    /*try {
      ServerAntwort xyz = wait2 (in.readLine());
      if (xyz.typ==xyz.REPARATUR)
	return xyz.register;
      else 
	throw new KommException ("regReparatur: Falsches Antwortobjekt(typ:"+xyz.getTyp()+")");
      
    }
    catch (IOException ioe) {
      throw new KommException ("regReparatur: IOException beim Empfangen");
    }
    */
  } 

  /*     
  Zur Benachrichtigung der Spieler, dass das Spiel anfängt.
   (true=ok, false=error)
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
  
   public boolean spielstart() throws KommException{ 
     out.println ("NTS"); 
      try{ 
        String a = in.readLine();
        if ((a.equals("ok"))||(a.equals("OK")))
	  return true; 
      else if ((a.equals("error"))||(a.equals("ERROR")))
	return false;
      else  
	throw new KommException ("Fehler bei der Antwort auf spielstart: "+a); 
    }  
    catch  (IOException ioe) {
      throw  new KommException ("IOException bei spielstart");
     } 
    
   }  
    */
 } 







