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
 
package de.botsnscouts.gui;

import  de.botsnscouts.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.comm.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import de.botsnscouts.server.KartenStapel; 

/**
 * scout logic
 * @author Lukasz Pekacki
 */
public class Scout {

    private boolean active = false;

    public Scout() {
    }


    protected boolean active() {
	return active;
    }

    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    Bot[] doPhaseRob = new Bot[1];




    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    private void removeScout() {
/*
	// -------- entferne Scout
	Bot[] doPhaseRob = new Bot[1];

	doPhaseRob[0] = Bot.getCopy(f.statusLine.sC[myRobIndex].r);
	doPhaseRob[0].zeige_Roboter();
	int moeglichePhasen = 0;
	f.spielFeld.vorschau(moeglichePhasen,doPhaseRob);
	*/
    }
    


    
}
