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

/**
 * interface for card.
 * No one should produce or copy cards instead of the card deck hold by the
 * server.
*/

import java.awt.Image;
import java.util.Comparator;

public abstract class Card  {
    
   
    public static final String ACTION_MOVE1      = "M1";
    public static final String ACTION_MOVE2      = "M2";
    public static final String ACTION_MOVE3      = "M3";
    public static final String ACTION_BACK        = "BU";
    public static final String ACTION_ROTATE_R = "RR";
    public static final String ACTION_ROTATE_L = "RL";
    public static final String ACTION_UTURN      = "UT";
    
   
    private Image myImage;
    
    protected int prio;
    protected String action;

    public static final Comparator INVERSE_PRIORITY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            int p1 = ((Card)o1).getprio();
            int p2 = ((Card)o2).getprio();
            return (p2-p1);
        }
    };

    public static final Comparator PRIORITY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            int p1 = ((Card)o1).getprio();
            int p2 = ((Card)o2).getprio();
            return (p1-p2);
        }
    };

    protected Card( int prio, String action ) {
        this.prio = prio;
        this.action = action;
    }

    /** returns the priority */
    public int getprio() {
        return prio;
    }

    /** returns the kind of the card in the usual form
	(M1, M2, M3, UT, BU, RR, RL)
    */
    public String getAction() {
        return action;
    }

    public int compareTo(Object o){
        Card k2 = (Card)o;
        if (this.getprio() < k2.getprio() )
            return -1;
        else if (this.getprio() > k2.getprio() )
            return 1;
        else
            return 0;
    }

    public boolean equals(Object o) {
        if( o instanceof Card )
            return getprio() == ((Card)o).getprio();

        return false;
    }

    public String toString(){
        return "("+this.prio+","+this.action+")";
    }
    
    public static Image getImageByAction (String cardActionId){    
       return ImageMan.getCardImage(cardActionId);        
    }
    
    public Image getImage() {
        if (myImage == null){
            myImage=getImageByAction(action);
        }
        return myImage;            
    }
    
    
 
    
}
