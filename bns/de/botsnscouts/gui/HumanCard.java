package de.botsnscouts.gui;

import de.botsnscouts.util.*;
/**
 * extends the Karte object with
 * state of the card like:
 * LOCKED, FREE, FILLED
 * @see HumanPlayer.java
 *@author Lukasz Pekacki
*/

public class HumanCard implements Karte{ 
    
    private int prio;
    private String action;


    //Attribute 
 
    public static final int FREE=0;
    public static final int FILLED=1;
    public static final int LOCKED=2;

    private int state = FREE;

    public HumanCard(){
    }   

    public HumanCard (int prioritaet,String action){
	prio = prioritaet;
	this.action = action;
    }

    public HumanCard (Karte k) {
	this (k.getprio(), k.getaktion());
    }
    
    public void setState (int newState) {
	state = newState;
    }


    public int getState () {
	return state;
    }

    public boolean free() {
	return (state == FREE);
    }

    public boolean locked() {
	return (state == LOCKED);
    }


    /** returns the priority */
    public int getprio() {
	return prio;
    }
       
    /** returns the kind of the card in the usual form
	(M1, M2, M3, UT, BU, RR, RL)
    */
    public String getaktion() {
	return action;
    }

    
    /** returns the compare */
    public int compareTo(Object c) {
	if ((this.getaktion() == ((Karte) c).getaktion()) && ((this.getprio() == ((Karte) c).getprio() ) ) ) {
	    return 0;
	}
	else {
	    return 1;
	}
    }


    public boolean equals(Karte c) {
	return ( compareTo(c) == 0);
    }


}
