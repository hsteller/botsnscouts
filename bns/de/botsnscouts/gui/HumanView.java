package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * all methods a view for the human player must implement
 * @author Lukasz Pekacki
 */

public class HumanView extends JPanel implements HumanViewInterface {
    
    private Hashtable registers = new Hashtable(5);
    private Hashtable cards = new Hashtable(9);
    private CardLayout panelSwitcher = new CardLayout();

    public HumanView() {
	GetDirection getDir = new GetDirection(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		// sendDirection(((PfeilC) e.getSource()).richt);
	    }
	}
								  
					       );
	
    }

    /**
     * display a message that is shown only to this player
     */
    public void showMessageToPlayer(String s) {}

    /**
     * display info that the robot is power down int this turn
     */
    public void showPowerDown() {}


    /**
     * display the get direction request
     */
    public void showGetDirection() {}

    /**
     * display the power down again request
     */
    public void showRePowerDown() {}

    /**
     * display the register repair request
     */
    public void showRegisterRepair() {}

    /**
     * update the position of the scout
     */
    public void showUpdatedScout() {}

    /**
     * activate the scout
     */
    public void activateScout() {}


    /**
     * remove the scout
     */
    public void removeScout() {}

    /**
     * update the position of the knowitalll
     */
    public void showUpdatedKlugScheisser() {}

    /**
     * activate the knowitall
     */
    public void activateKlugScheisser() {}


    /**
     * remove the knowitall
     */
    public void removeKlugScheisser() {}


    /**
     * show game over
     * two types: a) winner + winner no.
     *            b) dead
     */
    public void showGameOver(int typeOfGameOver, int winnerNumber) {}


    /**
     *  exit the programm
     *  eihter by game over or by user request
     */
    public void shutup() {}


}




