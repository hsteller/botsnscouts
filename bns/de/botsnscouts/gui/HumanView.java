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
    
    private HumanPlayer human;
    private CardLayout panelSwitcher = new CardLayout();
    private JPanel scoutNFriends = new JPanel();
    private RepairRegisters repairRegisters;
    private UserInfo userInfo;
    private CardArray cards;
    private RegisterArray registers;
    ScoutVertiefung scoutView;
    private JPanel switcherPanel;

    public HumanView(HumanPlayer hp) {
	human = hp;
	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	switcherPanel = new JPanel();
	JPanel regsAndCards = new JPanel();
	JPanel wiseAndScout = new JPanel();

	switcherPanel.setLayout(panelSwitcher);

	GetDirection getDir = new GetDirection(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    int direct = Integer.parseInt(ae.getActionCommand());
		    Global.debug(this,"I have choosen direction: "+direct);
		    sendDirection(direct);
		}
	    }
					       );
	AgainPowerDown againPowerDown = new AgainPowerDown(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    sendAgainPowerDown(ae.getActionCommand().equals("againpowerdown"));
		}
	    }
							   );
	ZielfahneErreicht reachedEndDead = new ZielfahneErreicht(Message.say("SpielerMensch","mkilled"),true);
	ZielfahneErreicht reachedEndWinner = new ZielfahneErreicht(Message.say("SpielerMensch","mflagreached"),false);

	KlugscheisserLatte wisenheimerView = new KlugscheisserLatte(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (( (KlugscheisserLatte) e.getSource()).selected()) {
			//			d(this,"wisenheimer, wake up!");
			klugscheisserClicked(true);
		    }
		    else {
			//			d(this,"wisenheimer, go home!");
			klugscheisserClicked(false);
		    }
		}});

	scoutView = new ScoutVertiefung(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if ( ((ScoutVertiefung) e.getSource()).isSelected()) {
			// d("Scout, wake up!");
			scoutClicked(true);
		    }
		    else {
			// d("Scout, go home!");
			scoutClicked(false);
		    }
		}}
							);

	repairRegisters = new RepairRegisters(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    sendRepairRegisters();
		}
	    });
	
	cards = new CardArray(new ActionListener(){
		public void actionPerformed(ActionEvent cardKlick) {
		    treatCardKlick((CardView) cardKlick.getSource());
		}
	    },
			      new ActionListener(){
				      public void actionPerformed(ActionEvent sendKlick) {
					  treatSendCards();
				      }
				  }
			      );
	
	registers = new RegisterArray(new ActionListener(){
		public void actionPerformed(ActionEvent registerKlick) {
		    treatRegisterKlick((RegisterView) registerKlick.getSource());
		}
	    }

				     );
	userInfo = new UserInfo();

	wiseAndScout.add(wisenheimerView);
	wiseAndScout.add(scoutView);


	regsAndCards.add(registers);
	regsAndCards.add(cards);
	switcherPanel.add(userInfo,"userInfo");
	switcherPanel.add(getDir,"getDirection");
	switcherPanel.add(againPowerDown,"againPowerDown");
	switcherPanel.add(repairRegisters,"repairRegisters");
	switcherPanel.add(regsAndCards,"regsAndCards");
	switcherPanel.add(reachedEndDead,"reachedEndDead");
	switcherPanel.add(reachedEndWinner,"reachedEndWinner");
	add(wiseAndScout);
	add(switcherPanel);
    }

    /**
     * display a message that is shown only to this player
     */
    public void showMessageToPlayer(String s) {
	userInfo.setInfo(s);
	panelSwitcher.show(switcherPanel,"userInfo");
    }

    /**
     * display the card
     */
    public void showCards(ArrayList humanCards) {
	cards.setCards(humanCards);
	panelSwitcher.show(switcherPanel,"regsAndCards");
    }


    /**
     * display info that the robot is power down int this turn
     */
    public void showPowerDown() {
	showMessageToPlayer(Message.say("SpielerMensch","istPowerDown"));
    }


    /**
     * display the get direction request
     */
    public void showGetDirection() {
	panelSwitcher.show(switcherPanel,"getDirection");
    }

    /**
     * display the power down again request
     */
    public void showRePowerDown() {
	panelSwitcher.show(switcherPanel,"againPowerDown");
    }

    /**
     * display the register repair request
     */
    public void showRegisterRepair(Karte[] robRegs, int repairNumber) {
	registers.updateRegisters(robRegs);
	repairRegisters.setChoises(registers.getRegisterViewArray(), repairNumber);
	panelSwitcher.show(switcherPanel,"repairRegisters");
    }

    /**
     * update the position of the scout
     */
    public void showUpdatedScout() {
	if (scoutView.selected()) {
	    Roboter[] doPhaseRob = new Roboter[1];
	    doPhaseRob[0] = Roboter.getCopy(human.getRob());
	    d("got a copy of my robot: " + doPhaseRob[0]);
	    ArrayList alreadyChosen = registers.getAlreadyChosen();
	    d("already chosen registers:"+alreadyChosen);
	    for (int m = 0; m < alreadyChosen.size(); m++) {
		doPhaseRob[0].setZug(m, ((HumanCard) alreadyChosen.get(m)));
	    }
	    doPhaseRob[0].zeige_Roboter();
	    human.passUpdatedScout(alreadyChosen.size(),doPhaseRob);
	}
    }

    /**
     * activate the scout
     */
    public void activateScout() {
    }


    /**
     * remove the scout
     */
    public void removeScout() {
	    Roboter[] doPhaseRob = new Roboter[1];
	    doPhaseRob[0] = Roboter.getCopy(human.getRob());
	    doPhaseRob[0].zeige_Roboter();
	    human.passUpdatedScout(0,doPhaseRob);
    }

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
    public void showGameOver(boolean dead, int winnerNumber) {
	if (dead) {
	    panelSwitcher.show(switcherPanel,"reacedEndDead");
	}
	else {
	    panelSwitcher.show(switcherPanel,"reacedEndWinner");
	}
    }


    /**
     *  exit the programm
     *  eihter by game over or by user request
     */
    public void shutup() {
    }


    private void sendDirection (int d) {
	human.sendDirection(d);
	showMessageToPlayer(Message.say("SpielerMensch","mkartenMisch"));
    }

    private void sendAgainPowerDown (boolean again) {
	if (!again) {
	showMessageToPlayer(Message.say("SpielerMensch","mkartenMisch"));
	}
	else {
	    showPowerDown();
	}
	human.sendAgainPowerDown(again);
    }

    private void sendRepairRegisters () {
	showMessageToPlayer(Message.say("SpielerMensch","mkartenMisch"));
	human.sendRepair(repairRegisters.getSelection());
    }


    private void treatCardKlick (CardView cv) {
	if ( (cv.getCard() != null) && ( ! registers.allOcupied() ) ) {
	    registers.addCard(cv.getCard());
	    cv.reset();
	    if (registers.allOcupied()) {
		cards.activateButton();
	    }
	}
	showUpdatedScout();
    }


    private void treatSendCards () {
	if ( registers.allOcupied()) {
	    showMessageToPlayer(Message.say("SpielerMensch","mkartenMisch"));
	    d("send to Server: "+registers.getCards() + " powerDown: "+cards.wishesPowerDown());
	    human.sendCards(registers.getCards(),cards.wishesPowerDown());
	    if (cards.wishesPowerDown()) {
		showPowerDown();
	    }
	    cards.resetAll();
	    registers.resetAll();
	}
    }

    private void treatRegisterKlick (RegisterView rv) {
	if ( (rv.getCard() != null) && (! rv.locked()) ) {
	    rv.getCard().setState(HumanCard.FREE);
	    cards.addCard(rv.getCard());
	    rv.reset();
	}
	if ( ! registers.allOcupied() ) {
	    cards.deActivateButton();
	}
	showUpdatedScout();

    }

    private void klugscheisserClicked (boolean awake) {
	
    }

    private void scoutClicked (boolean awake) {
	if (awake) {
	    showUpdatedScout();
	}
	else {
	    removeScout();
	}
    }

    private void d(String s) {
	Global.debug(this,s);
    }
}




