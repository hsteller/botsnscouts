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
 
package de.botsnscouts.gui;

import de.botsnscouts.util.*;
/**
 * extends the Karte object with
 * state of the card like:
 * LOCKED, FREE, FILLED
 * @see HumanPlayer.java
 *@author Lukasz Pekacki
*/

public class HumanCard extends Karte{

//    private int prio;
//    private String action;


    //Attribute

    public static final int FREE=0;
    public static final int FILLED=1;
    public static final int LOCKED=2;

    private int state = FREE;

//    public HumanCard(){
//    }

    public HumanCard (int prioritaet,String action){
        super( prioritaet, action );
//	prio = prioritaet;
//	this.action = action;
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


//    /** returns the priority */
//    public int getprio() {
//	return prio;
//    }
//
//    /** returns the kind of the card in the usual form
//	(M1, M2, M3, UT, BU, RR, RL)
//    */
//    public String getaktion() {
//	return action;
//    }
//

//    /** returns the compare */
//    // ENNO:nobody (except equals) ever used this, so I redefined it to do s.th. sensible, see below
//    public int compareTo(Object c) {
//	if ((this.getaktion() == ((Karte) c).getaktion()) && ((this.getprio() == ((Karte) c).getprio() ) ) ) {
//	    return 0;
//	}
//	else {
//	    return 1;
//	}
//    }

//    public int compareTo(Object o){
//        Karte k2 = (Karte)o;
//        if (this.prio < k2.getprio() )
//            return -1;
//        else if (this.prio > k2.getprio() )
//            return 1;
//        else
//            return 0;
//    }
//
//
//    public boolean equals(Karte c) {
//	return ( compareTo(c) == 0);
//    }



    public String toString() {
	return ("Karte hat prio "+prio+", Aktion:"+action+" und status (0:free, 2:locked ):"+state);
    }

}
