package de.spline.rr;

/**
*Die Klasse Karte.
*Sie stellt das Kartenobjekt dar.
*Eine Karte wird durch ihre prioritaet und ihre Aktionszeichen 
*gekennzeichnet
*@author Mohammad, Alex, Holger
*@version 1.0
*/

public class Karte{ 
        //Attribute 
 
        	private int prio; 
   	private String aktion; 
    
/** 
*Der Konstruktor erwartet 2 Argumente.
*<p> 
*<pre> 
*  1.die Nummer der Prioritaet dieser Karte.
*  2.das Zeichen der Aktion dieser Karte 
*<p> 
*@param prioritaet Prioritaet der Karte als Integer
*@param action Aktion der Karte als String lauf "Protokolle und Datenformate" 
*/ 
	public Karte(){
		prio = 0;
		aktion = "";
	}   
	public Karte(int prioritaet,String action){
 	    	prio = prioritaet;
            	aktion = action;
      	}  
    
      // 3 Objektmethoden 
/**
 * Mohammad hat gerade gesagt, da� er sp�ter noch einen Komentar zu dieser Methode schreiben m�chte!!!
 */
public Karte ToKarte(){
	Karte k = new Karte(this.prio,this.aktion);
	return k;
}
	



/** 
*Eine Objektmethode. 
*Liefert die Prioritaet zurueck     
*@return Prioritaet der Karte als Integer 
*/
   
   public int getprio(){ 
      return prio; 
   } 
       
/** 
*<p> 
*Eine Objektmethode. 
*Liefert die Aktion zurueck.       
*@return Aktion als String laut "Protokolle und Datenformate" 
*/ 
        public String getaktion(){ 
      return aktion; 
   } 
/** 
*<p> 
*Eine Objektmethode. 
*Kopiert den Inhalt des �bergebenen Karten-Objektes k in die auf das 
*Karten-Objekt, auf das die Methode angewandt wird. 
*/  
   public void copy(Karte k) 
   { 
      prio=k.getprio(); 
      aktion=k.getaktion(); 
   } 
}
