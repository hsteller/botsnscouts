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

package de.botsnscouts.autobot;

/**
 * SearchRecursively implements the algorithm to recursively look for
 * the best move with a given set of cards.
 *
 * @author Dirk, Lukasz
 * Id: $Id$
 */

import java.util.Arrays;

import de.botsnscouts.board.BoardBot;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.server.Deck;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;

public class SearchRecursively {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(SearchRecursively.class);

    private SimBoard sf;
    private DistanceCalculator calc;
    private Card[] bestCards;
    private int bestScore;
    private int malus;

    private static final Card[] malusCards = {Deck.getRefCard("RL"),
                                               Deck.getRefCard("M1"),
                                               Deck.getRefCard("M2"),
                                               Deck.getRefCard("M3"),
                                               Deck.getRefCard("BU")};
    private static final int[] mali = {25, 15, 15, 15, 10};
    private static final int maliSum = 80;

    public SearchRecursively(SimBoard s, int malus){
        this(s, malus, SimpleDistanceCalculator.getInstance(s));
    }

    public SearchRecursively(SimBoard s, int malus, DistanceCalculator calc) {
        sf = s;
        this.calc = calc;
        this.malus = malus;
    }

    public Card[] findBestMove(Card[] ka, final Bot r) {
        int j = 0;
        bestCards = new Card[5];
        for (int i = 0; i < 5; i++) {
            if (r.getMove(i) == null) {
                bestCards[i] = ka[j++];
            } else {
                bestCards[i] = r.getMove(i);
            }
        }
        if (j == 0) {
            return bestCards;
        }
        bestScore = 1000;
        j = 0;
        while (r.getMove(j) != null) {
            Bot[] ra = new Bot[1];
            ra[0] = r;
            sf.doPhase(j + 1, ra);
            j++;
        }

        int len = 0;
        while ((len < 9) && (ka[len] != null))
            len++;
        Arrays.sort(ka, 0, len, Card.INVERSE_PRIORITY_COMPARATOR);

        recurse((BoardBot) r, ka, 0);
        return bestCards;
    }

    /** We need one temp per level of recursion, however we don't want to
     create a new one on each call. */
    private BoardBot[] tmp = {new BoardBot(), new BoardBot(),
                                  new BoardBot(), new BoardBot(),
                                  new BoardBot(), new BoardBot()};

    private void recurse(final BoardBot r, Card[] ka, int recursionLevel) {
        if (r.getDamage() == 10) return;
        int anzahl = 0;
        for (int i = 0; i < 5; i++)
            if (r.getMove(i) != null) anzahl++;
        if (anzahl == 5) {   // end of recursion reached, 5 cards selected
            int diemalus = 0;

            // If we are standing on a conveyor belt, check what cards we need
            // to not die next phase
            if (sf.floor(r.getX(), r.getY()).isBelt()) { // Belt
                for (int i = 0; i < malusCards.length; i++) {
                    tmp[recursionLevel].initFrom(r);
                    tmp[recursionLevel].setMove(0, malusCards[i]);
                    sf.doPhase(1, tmp[recursionLevel]);
                    if (tmp[recursionLevel].getDamage() == 10)
                        diemalus += mali[i];
                }
                if (diemalus == maliSum)
                    return; // we die surely, discard this choice
            }

            int score = calc.getGoodness(r, malus) + diemalus;
            if (score <= bestScore) {
                bestScore = score;
                for (int i = 0; i < 5; i++)
                    bestCards[i] = r.getMove(i);
            }
            return;
        }

        for (int i = 0; i < 9; i++) {
            if (ka[i] == null)
                continue;
            Card cardTemp = ka[i];
            ka[i] = null; // play that card
            tmp[recursionLevel].initFrom(r);

            int j = 0;
            while (tmp[recursionLevel].getMove(j) != null) j++;
            tmp[recursionLevel].setMove(j, cardTemp);
            while ((j < 5) && (tmp[recursionLevel].getMove(j) != null)) {
                sf.doPhase(j + 1, tmp[recursionLevel]);
                j++;
            }
            recurse(tmp[recursionLevel], ka, recursionLevel + 1);
            ka[i] = cardTemp;
            // Skip cards with identical action
            while ((i < 8) && ((ka[i + 1] == null) || (ka[i + 1].getAction().equals(cardTemp.getAction()))))
                i++;

        }
    }
}
