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

/**
 * all methods a view for the human player must implement
 * @author Lukasz Pekacki 
 *
 */

public interface HumanViewInterface {

    /**
     * display a message that is shown only to this player
     */
    public void showMessageToPlayer(String s);

    /**
     * display info that the robot is power down int this turn
     */
    public void showPowerDown();


    /**
     * display the get direction request
     */
    public void showGetDirection();

    /**
     * display the power down again request
     */
    public void showRePowerDown();

    /**
     * display the register repair request
     */
    //    public void showRegisterRepair();

    /**
     * update the position of the scout
     */
    public void showUpdatedScout();
    //    public void showRegisterRepair();

    /**
     * activate the scout
     */
    public void activateScout();


    /**
     * remove the scout
     */
    public void removeScout();

    /**
     * update the position of the knowitalll
     */
    //    public void showUpdatedKlugScheisser();

    /**
     * activate the knowitall
     */
    public void activateKlugScheisser();


    /**
     * remove the knowitall
     */
    public void removeKlugScheisser();


    /**
     * show game over
     * two types: a) winner + winner no.
     *            b) dead
     */
    public void showGameOver(boolean dead, int winnerNumber);


    /**
     *  exit the programm
     *  eihter by game over or by user request
     */
   // public void shutup();
}




