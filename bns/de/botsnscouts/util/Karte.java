package de.botsnscouts.util;

/**
 * interface for card.
 * No one should produce or copy cards instead of the card deck hold by the
 * server.
*/

public interface Karte extends java.lang.Comparable{ 

    /** returns the priority */
    public int getprio();
       
    /** returns the kind of the card in the usual form
	(M1, M2, M3, UT, BU, RR, RL)
    */
    public String getaktion();

}
