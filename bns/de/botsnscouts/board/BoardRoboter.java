package de.botsnscouts.board;

/** Roboter für das Spielfeld */

import de.botsnscouts.util.Roboter;

public class BoardRoboter extends Roboter {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( BoardRoboter.class );
    public BoardRoboter() { super(""); };

    public BoardRoboter(String name) {
	super(name);
    }

    public BoardRoboter(Roboter r) {
	super(r);
	BoardRoboter rob = (BoardRoboter )r;
	xx = rob.xx;
	yy = rob.yy;
	aa = rob.aa;
    }

    public void fillCopy(BoardRoboter r) {
        super.fillCopy(r);
        r.xx = xx;
        r.yy = yy;
        r.aa = aa;
    }

    public void initFrom(BoardRoboter r) {
        r.fillCopy( this );
    }

    /**
     *Temporaere X-Postion des Roboters waehrend der Zugausfuerung.
     *wird nur intern von Spielfeld verwendet.
     */
    protected int xx ;
    /**
     *Temporaere Y-Postion des Roboters waehrend der Zugausfuerung.
     *wird nur intern von Spielfeld verwendet.
     */
    protected int yy ;
    /** Temporaere Ausrichtung. Spielfeld-Intern. */
    protected int aa;

    public void dumpZug() {
        for(int i=0; i<zug.length; i++) {
            CAT.debug( "dumpZug: " + i + " " + zug[i] );
        }
    }


}
