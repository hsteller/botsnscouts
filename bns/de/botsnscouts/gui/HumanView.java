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


import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.log4j.Category;

/**
 * view for the human player
 * @author Lukasz Pekacki
 */

public class HumanView extends JPanel implements HumanViewInterface {

    static Category CAT = Category.getInstance(HumanView.class);

    private HumanPlayer human;
    private CardLayout panelSwitcher = new CardLayout();
    private RepairRegisters repairRegisters;
    private UserInfo userInfo;
    private CardArray cards;
    private RegisterArray registers;
    ScoutVertiefung scoutView;
    KlugscheisserLatte wisenheimerView;
    private JPanel switcherPanel;
    private JPanel wiseAndScout;

    public HumanView(HumanPlayer hp) {
	human = hp;
	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	switcherPanel = new PaintPanel(OptionPane.getBackgroundPaint(this), true);
	JPanel regsAndCards = new TJPanel();
        wiseAndScout = new TJPanel();

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
		    CAT.debug("The user clicked: "+ae.getActionCommand());
		    sendAgainPowerDown(ae.getActionCommand().equals("powerdownagain"));
		}
	    }
							   );
	ZielfahneErreicht reachedEndDead = new ZielfahneErreicht(Message.say("SpielerMensch","mkilled"),true);
	ZielfahneErreicht reachedEndWinner = new ZielfahneErreicht(Message.say("SpielerMensch","mflagreached"),false);

	wisenheimerView = new KlugscheisserLatte(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (( (KlugscheisserLatte) e.getSource()).selected()) {
			//			d(this,"wisenheimer, wake up!");
			if (human.mode == HumanPlayer.MODE_PROGRAM) {
                           klugscheisserClicked(true);
      			   human.sendWisenheimerMsg();
                        }
                        else
                          CAT.debug("asked wisenheimer but it did not make sense at this moment");
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
	//add(wiseAndScout);
	add(switcherPanel);
    }


    protected JPanel getWiseAndScoutPanel(){
      return wiseAndScout;

    }

    /**
     * display a message that is shown only to this player
     */
    public void showMessageToPlayer(String s) {
	userInfo.setInfo(s);
	panelSwitcher.show(switcherPanel,"userInfo");
	this.requestFocus();
    }

    /**
     * display the card
     */
    public void showCards(ArrayList humanCards) {
        CAT.debug("Show cards");
	cards.setCards(humanCards);
	panelSwitcher.show(switcherPanel,"regsAndCards");
	if (registers.allLocked()) {
	    CAT.debug("All Registes locked!");
	    cards.activateButton();
	}
	this.requestFocus();
    }


    /**
     * display info that the robot is power down int this turn
     */
    public void showPowerDown() {
	showMessageToPlayer(Message.say("SpielerMensch","istPowerDown"));
	this.requestFocus();
    }


    /**
     * display the get direction request
     */
    public void showGetDirection() {
	panelSwitcher.show(switcherPanel,"getDirection");
	this.requestFocus();
    }

    /**
     * display the power down again request
f     */
    public void showRePowerDown() {
	panelSwitcher.show(switcherPanel,"againPowerDown");
	this.requestFocus();
    }

    /**
     * display the register repair request
     */
    public void showRegisterRepair(Card[] robRegs, int repairNumber) {
      CAT.debug("Show Register Repair");
	registers.updateRegisters(robRegs);
	repairRegisters.setChoises(registers.getRegisterViewArray(), repairNumber);
	panelSwitcher.show(switcherPanel,"repairRegisters");
	this.requestFocus();
    }


    /**
     * update the position of the scout
     */
    public void showUpdatedScout() {
	if (scoutView.selected()) {
	    Bot[] doPhaseRob = new Bot[1];
	    doPhaseRob[0] = Bot.getCopy(human.getRob());
	    d("got a copy of my robot: " + doPhaseRob[0]);
	    ArrayList alreadyChosen = registers.getAlreadyChosen();
	    d("already chosen registers:"+alreadyChosen);
	    for (int m = 0; m < alreadyChosen.size(); m++) {
		doPhaseRob[0].setMove(m, ((HumanCard) alreadyChosen.get(m)));
	    }
	    doPhaseRob[0].debug();
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
	    Bot[] doPhaseRob = new Bot[1];
	    doPhaseRob[0] = Bot.getCopy(human.getRob());
	    doPhaseRob[0].debug();
	    human.passUpdatedScout(0,doPhaseRob);
    }

    /**
     * update the position of the wisenheimer
     */
    public void showUpdatedWisenheimer(boolean predicted) {
	int predCard = -1;
	if ((wisenheimerView.selected()) && ( human.mode == HumanPlayer.MODE_PROGRAM)){
	    if (predicted){
		predCard=human.getNextPrediction(registers.getWisenheimerCards(),cards.getCards());
	    }else{
		predCard=human.getPrediction(registers.getWisenheimerCards(),cards.getCards());
	    }
	    if (predCard==-1){
		wisenheimerView.setSelected(false);
	    }

	}
	cards.setWisenheimer(predCard);
    }

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
	    panelSwitcher.show(switcherPanel,"reachedEndDead");
	}
	else {
	    panelSwitcher.show(switcherPanel,"reachedEndWinner");
	}
    }


    /**
     *  exit the programm
     *  eihter by game over or by user request
     */
   // public void shutup() {}


    protected void updateRegisters(Card[] robRegs){
	registers.updateRegisters(robRegs);
	CAT.debug("Updating Registers...");
	registers.resetAll();
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
      CAT.debug("sendRepairRegisters");
	showMessageToPlayer(Message.say("SpielerMensch","mkartenMisch"));
	ArrayList regsRep = repairRegisters.getSelection();
	unlockRegisters(regsRep);
	human.sendRepair(regsRep);
    }

    private void unlockRegisters(ArrayList repairRegs){
	CAT.debug("Die Register vor dem unlock: "+registers.toString());
	for (int i =0; i< repairRegs.size(); i++) {
	    CAT.debug("Entsperre Register: "+((Integer)repairRegs.get(i)).intValue());
	    registers.unlockRegister(((Integer)repairRegs.get(i)).intValue()-1);
	}

	CAT.debug("Die Register nach dem unlock: "+registers.toString());
	registers.resetAll();

    }

    private void treatCardKlick (CardView cv) {
	boolean predicted=false;
	if ( (cv.getCard() != null) && ( ! registers.allOcupied() ) ) {
	    registers.addCard(cv.getCard());
	    predicted=cv.isWisenheimer();
	    cv.reset();
	    if (registers.allOcupied()) {
		cards.activateButton();
	    }
	}
	showUpdatedScout();
	showUpdatedWisenheimer(predicted);
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
	}
	wisenheimerView.setSelected(false);
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
	showUpdatedWisenheimer(false);
    }

    private void klugscheisserClicked (boolean awake) {
	//if ((awake) && ( human.mode == HumanPlayer.MODE_PROGRAM)){
	if ( human.mode == HumanPlayer.MODE_PROGRAM ){
	    showUpdatedWisenheimer(false);
	}
    }

    private void scoutClicked (boolean awake) {
	if (awake) {
	    showUpdatedScout();
	}
	else {
	    removeScout();
	}
    }

    public void sendChatMessage(String msg) {
        if( msg != null && msg.trim().length() != 0) {
            human.sendChat(msg);
        }
    }


    protected void quitHumanPlayer() {
      CAT.debug("HumanView asks the HumanPLayer to quit..");
      human.quit();
    }

    private void d(String s) {
	Global.debug(this,s);
    }

    public HumanPlayer getHumanPlayer() {
        return human;
    }
}




