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

import java.util.Random;
import java.util.*;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Global;

/** Card deck
 *  gibKarte() returns a random card from the deck
 *  @author Miriam
 */

public class KartenStapel {

    /** Different kinds of cards */
    public static final String[] types = {"UT","RR","RL","BU","M1","M2","M3"};
    /** Number of each type of cards */
    public static final int[] nr = {6,18,18,6,17,13,6};

    /** Array of all valid cards
     *  Invariant: The cards are kept sorted by priority.
     */
    private static KarteImpl[] allCards = null;

    //    private static KarteImpl[] ktypes;

    /** The real card deck */
    private Vector deck = new Vector();

    private java.util.Random random = new Random();

    /** creates a new card deck respecting which card are stuck in
     *  locked registers.
     *  @param Vector locked - the cards that are not present in the
     *  new desk.
     */

    protected KartenStapel (Card[] locked) {

	// Initialize allCards - only needed one time
	createAllCardsIfNeeded();
	// -----------------------------------------

	java.util.Arrays.sort(locked, Card.PRIORITY_COMPARATOR);
	int cLocked = 0;
	for (int i=0; i < allCards.length; i++){
	    if (cLocked < locked.length && locked[cLocked].equals(allCards[i])){
		cLocked++;
		continue;
	    }
	    deck.add(allCards[i]);
	}
    }



    /** creates new card deck */
    protected KartenStapel() {
	this(new Card[0]);
    }


    private static void createAllCardsIfNeeded() {
	if (allCards==null){
	    int size = 0;
	    for (int i=0; i<nr.length; i++) {
		size += nr[i];
	    }
	    Global.debug("Es gibt "+size+" Karten im Spiel.");
	    allCards = new KarteImpl[size];
	    int i=0;
	    int max = 0;
	    for (int n=0; n<nr.length; n++){
		max += nr[n];
		for(; i<max; i++){

		    allCards[i] = new KarteImpl(i+1, types[n]);
		    Global.debug(allCards[i].toString()+" erzeugt.");
		}
	    }
	    //debug - wieder raus

	}
    }

    /** returns a card from the card deck
     * Pre: card deck is not empty.
     */
    protected Card gibKarte() {
	int index = random.nextInt(deck.size());
	Card k = (Card )deck.remove(index);
	return k;
    }

    /** returns an array of n cards from the deck.
     * PRE: Es werden nicht zu viele Karten vom Server verteilt.
     *
     */
    public Card[] gibKarte(int n){
	Card[] k = new Card[n];
	for (int i=0; i < n; i++){
	    k[i] = gibKarte();
	}
	return k;
    }

    /* returns the Card with a given priority.
     * We ignore the string... (depricated)
     */

    public static Card get(int prio, String action){
	createAllCardsIfNeeded();
	return allCards[prio-1];
    }


    /** returns one card as a reference for a kind of cards
     *  null if String is invalid.
     */
    public static Card getRefCard(String kind){
	createAllCardsIfNeeded();
	int pos = 0;
	for (int i=0; i < types.length; i++) {
	    if (!types[i].equals(kind))
		pos += nr[i];
	    else
		return allCards[pos];
	}
	return null; // String wasn't valid.
    }

    public String toString(){
	StringBuffer s = new StringBuffer("(");
	for (Iterator i=deck.iterator(); i.hasNext();){
	    s.append(((KarteImpl )i.next()).toString()+" ");
	}
	s.append(")");
	return new String(s);
    }

}

