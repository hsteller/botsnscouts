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

/** he deck containg all the cards.
 *  handOutCard() returns a random card from the deck
 *  @author Miriam
 */

public class Deck {

    /** Different kinds of cards */
    public static final String[] types = {"UT", "RR", "RL", "BU", "M1", "M2", "M3"};
    /** Number of each type of cards */
    private static final int[] noPerCard = {6, 18, 18, 6, 17, 13, 6};

    /** Array of all valid cards
     *  Invariant: The cards are kept sorted by priority.
     */
    private static CardImpl[] allCards = null;

    /** The real card deck */
    private Vector deck = new Vector();

    private java.util.Random random = new Random();

    /** creates a new card deck respecting which card are stuck in
     *  locked registers.
     *  @param locked - the cards that are not present in the
     *  new desk.
     */

    protected Deck(Card[] locked) {

        // Initialize allCards - only needed one time
        createAllCardsIfNeeded();
        // -----------------------------------------

        java.util.Arrays.sort(locked, Card.PRIORITY_COMPARATOR);
        int cLocked = 0;
        for (int i = 0; i < allCards.length; i++) {
            if (cLocked < locked.length && locked[cLocked].equals(allCards[i])) {
                cLocked++;
                continue;
            }
            deck.add(allCards[i]);
        }
    }


    /** creates new card deck */
    protected Deck() {
        this(new Card[0]);
    }

    /**
     * Create all CardImpl objects. Will only happen once per game.
     */
    private static void createAllCardsIfNeeded() {
        if (allCards == null) {
            int size = 0;
            for (int i = 0; i < noPerCard.length; i++) {
                size += noPerCard[i];
            }
            Global.debug("Es gibt " + size + " Karten im Spiel.");
            allCards = new CardImpl[size];
            int max = 0;
            int i=0;
            for (int n = 0; n < noPerCard.length; n++) {
                max += noPerCard[n];
                for (; i < max; i++) {

                    allCards[i] = new CardImpl(i + 1, types[n]);
                    Global.debug(allCards[i].toString() + " erzeugt.");
                }
            }
        }
    }

    /** Returns a random card from the card deck.
     * The card is removed from the deck.
     * Pre: card deck is not empty.
     */
    protected Card handOutCard() {
        int index = random.nextInt(deck.size());
        return (Card) deck.remove(index);
    }

    /** returns an array of n cards from the deck.
     * These cards are removed from the deck.
     * PRE: Es werden nicht zu viele Karten vom Server verteilt.
     *
     */
    public Card[] handOutCards(int n) {
        Card[] k = new Card[n];
        for (int i = 0; i < n; i++) {
            k[i] = handOutCard();
        }
        return k;
    }

    /* returns the Card with a given priority.
     */
    public static Card get(int prio) {
        createAllCardsIfNeeded();
        return allCards[prio - 1];
    }


    /** returns one card as a reference for a kind of cards
     *  null if String is invalid.
     */
    public static Card getRefCard(String kind) {
        createAllCardsIfNeeded();
        int pos = 0;
        for (int i = 0; i < types.length; i++) {
            if (!types[i].equals(kind))
                pos += noPerCard[i];
            else
                return allCards[pos];
        }
        return null; // String wasn't valid.
    }

    public String toString() {
        StringBuffer s = new StringBuffer("(");
        for (Iterator i = deck.iterator(); i.hasNext();) {
            s.append(i.next().toString() + " ");
        }
        s.append(")");
        return new String(s);
    }

}

