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
    public void showRegisterRepair();

    /**
     * display the fresh cards that the server has sent
     */
    public void showRegisterRepair();

    /**
     * display the fresh cards that the server has sent
     */
    public void showRegisterRepair();


}




