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
 
package de.botsnscouts.server;

import de.botsnscouts.util.Karte;

/**
* Implementation of the card interface.
* Goal: Nobody produces Cards except the card desk.
*/

public class KarteImpl extends Karte {

//    private int prio;
//    private String aktion;
//
//
    protected KarteImpl(int prioritaet,String action){
        super( prioritaet, action );
//	prio = prioritaet;
//	aktion = action;
    }
//
//
//    /**
//     *Liefert die Prioritaet zurueck
//     *@return Prioritaet der Karte als Integer
//     */
//
//    public int getprio(){
//	return prio;
//    }
//
//    /**
//     *<p>
//     *Liefert die Aktion zurueck.
//     *@return Aktion als String laut "Protokolle und Datenformate"
//     */
//    public String getaktion(){
//	return aktion;
//    }


    /* We don't have to overwrite equals because it's okay to compare by
     *  refernce
     */

//    public int compareTo(Object o){
//	if (o instanceof KarteImpl){
//	    KarteImpl k2 = (KarteImpl )o;
//	    if (this.prio < k2.prio)
//		return -1;
//	    else if (this.prio > k2.prio)
//		return 1;
//	    else
//		return 0;
//	}
//	throw new ClassCastException("You cannot compare cards with apples.");
//    }


}
