package de.botsnscouts.util;

/**
 * interface for card.
 * No one should produce or copy cards instead of the card deck hold by the
 * server.
*/

import java.util.Comparator;

public abstract class Karte  {
    protected int prio;
    protected String action;

    public static final Comparator INVERSE_PRIORITY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            int p1 = ((Karte)o1).getprio();
            int p2 = ((Karte)o2).getprio();
            return (p2-p1);
        }
    };

    public static final Comparator PRIORITY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            int p1 = ((Karte)o1).getprio();
            int p2 = ((Karte)o2).getprio();
            return (p1-p2);
        }
    };

    protected Karte( int prio, String action ) {
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
    public String getaktion() {
        return action;
    }

    public int compareTo(Object o){
        Karte k2 = (Karte)o;
        if (this.getprio() < k2.getprio() )
            return -1;
        else if (this.getprio() > k2.getprio() )
            return 1;
        else
            return 0;
    }

    public boolean equals(Object o) {
        if( o instanceof Karte )
            return getprio() == ((Karte)o).getprio();

        return false;
    }

    public String toString(){
	return "("+this.prio+","+this.action+")";
    }


}
