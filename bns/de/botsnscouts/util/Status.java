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
    public Card [] register;
  
 
    public Status () {}
    public Status (int phase, String name, Card [] karten){
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
    private static String cardsToString (Card [] k){
	String raus="";
	for (int i=0;i<k.length;i++) {
	    if (k[i]!=null)
		raus = raus+"PK("+k[i].getAction()+","+k[i].getprio()+"), ";
	    else
		raus+="null, ";
      
	}
    
	return raus;
    }
}
