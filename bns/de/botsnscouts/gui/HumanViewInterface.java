package de.botsnscouts.gui;

import de.botsnscouts.util.*;
/**
 * all methods a view for the human player must implement
 * @author Lukasz Pekacki
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
    public void showUpdatedKlugScheisser();

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
    public void shutup();
}




