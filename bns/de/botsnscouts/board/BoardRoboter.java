package de.botsnscouts.board;

/** Roboter für das Spielfeld */

import de.botsnscouts.util.Roboter;

public class BoardRoboter extends Roboter {

    public BoardRoboter(String name) {
	super(name);
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
