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

import de.botsnscouts.util.*;
/**
 * Diese Klasse erledigt die Ausgabe von Board und Status
 * @author Lukasz Pekacki
 */

public interface AusgabeViewInterface {

    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showActionMessage(String s);


    /**
     *  Zentriert das Fenster auf die angegebene Position
     */
    public void showPos (int x, int y);

    /**
     *  shows the winner state of the game
     */
    public void showWinnerState (String[] gameState);

    /**
     *  shows the winner list at game over
     */
    public void showWinnerlist (String[] winners);

    /**
     *  Behandelt das Fensterschlieﬂen
     */
    //public void shutup();


    /**
     * Statusleiste der spielenden Bot
     */
    public void showRobStatus(Bot r);


    /**
     * shows the changed, new status of the robots
     */
    public void showUpdatedRobots(Bot[] r);




}




