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
 
package de.botsnscouts.board;

/** Bot für das Spielfeld */

import de.botsnscouts.util.Bot;

public class BoardRoboter extends Bot {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( BoardRoboter.class );
    public BoardRoboter() { super(""); };

    public BoardRoboter(String name) {
	super(name);
    }

    public BoardRoboter(Bot r) {
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
        for(int i=0; i<move.length; i++) {
            CAT.debug( "dumpZug: " + i + " " + move[i] );
        }
    }


}
