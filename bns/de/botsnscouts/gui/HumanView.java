package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * view for the human player
 * @author Lukasz Pekacki
 */

public class HumanView extends JPanel implements HumanViewInterface {
    
    private ArrayList registers = new ArrayList(5);
    private CardLayout panelSwitcher = new CardLayout();
    private JPanel scoutNFriends = new JPanel();
    private RepairRegisters repairRegisters;
    
    public HumanView() {
	setLayout(panelSwitcher);
	GetDirection getDir = new GetDirection(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		sendDirection(Integer.parseInt(ae.getActionCommand()));
	    }
	    }
					       );
	AgainPowerDown againPowerDown = new AgainPowerDown(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		sendAgainPowerDown(ae.getActionCommand().equals("againpowerdown"));
	    }
	}
					       );
	repairRegisters = new RepairRegisters(new ActionListener(){
	    public void actionPerformed(ActionEvent ae) {
		sendRepairRegisters();
	    }
	    });
	
	
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


    private void sendDirection (int d) {
	// TODO
    }

    private void sendAgainPowerDown (boolean again) {
	// TODO
    }

    private void sendRepairRegisters () {
	ArrayList a = repairRegisters.getSelection();
	// TODO
    }

}




