package de.botsnscouts.board;

/** Roboter für das Spielfeld */

import de.botsnscouts.util.Roboter;

public class BoardRoboter extends Roboter {

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



}
