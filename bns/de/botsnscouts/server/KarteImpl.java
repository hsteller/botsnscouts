package de.botsnscouts.server;

import de.botsnscouts.util.Karte;

/**
* Implementation of the card interface.
* Goal: Nobody produces Cards except the card desk.
*/

public class KarteImpl implements Karte{ 
 
    private int prio; 
    private String aktion; 
    

    protected KarteImpl(int prioritaet,String action){
	prio = prioritaet;
	aktion = action;
    }


    /** 
     *Liefert die Prioritaet zurueck     
     *@return Prioritaet der Karte als Integer 
     */
   
    public int getprio(){ 
	return prio; 
    } 
       
    /** 
     *<p> 
     *Liefert die Aktion zurueck.       
     *@return Aktion als String laut "Protokolle und Datenformate" 
     */ 
    public String getaktion(){ 
	return aktion; 
    } 


    /* We don't have to overwrite equals because it's okay to compare by 
     *  refernce 
     */

    public int compareTo(Object o){
	if (o instanceof KarteImpl){
	    KarteImpl k2 = (KarteImpl )o;
	    if (this.prio < k2.prio)
		return -1;
	    else if (this.prio > k2.prio) 
		return 1;
	    else
		return 0;
	}
	throw new ClassCastException("You cannot compare cards with apples.");
    }

    public String toString(){
	return "("+prio+","+aktion+")";
    }

}
