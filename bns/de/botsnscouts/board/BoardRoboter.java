package de.botsnscouts.board;

/** Roboter für das Spielfeld */

import de.botsnscouts.util.Roboter;

public class BoardRoboter extends Roboter {

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
