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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.apache.log4j.Category;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.PaintPanel;
import de.botsnscouts.widgets.TJPanel;

/**
 * view for the human player
 * 
 * @author Lukasz Pekacki
 */

public class HumanView extends JPanel  {

    static Category CAT = Category.getInstance(HumanView.class);

    private static final String PANEL_REPAIR         = "repairRegs";
    private static final String PANEL_REGISTERS    = "regs";
    public  static final String PANEL_PHASE_EVAL  = "evalPhases";
    private static final String PANEL_DIRECTION    = "direction";
    private static final String PANEL_POWERDOWN = "powerdown";
    public  static final String PANEL_USERINFO      = "info";
    private static final String PANEL_GAMEOVER     = "gameover";
    
    private HumanPlayer human;

   
    private UserInfo userInfo = new UserInfo();
    private PhaseEvaluationPanel phaseInfo=new PhaseEvaluationPanel(); 
    private ZielfahneErreicht gameOverPanel = new ZielfahneErreicht();
    private RepairRegisters regRepairPanel;
    
    private CardArray cards;
    private RegisterArray registers;

    private JPanel wiseAndScout = new TJPanel(); 
    protected ScoutVertiefung scoutView;
    protected KlugscheisserLatte wisenheimerView;

    private JPanel panelSwitcher;
    private CardLayout panelSwitcherLayout = new CardLayout();
    private String panelToShow;
    
    private boolean dialogInSidebarActive = false;

    public HumanView(HumanPlayer hp) {
        human = hp;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));                         
        wisenheimerView = createWisenheimerHome();
        regRepairPanel = createRegisterRepairPanel();
        scoutView = createScoutHome();
        wiseAndScout.add(wisenheimerView);
        wiseAndScout.add(scoutView);
        initSwitchPanel();
        add(panelSwitcher);
    
    }
    
    
    private void initSwitchPanel(){
        panelSwitcher = new PaintPanel(OptionPane.getBackgroundPaint(this), true); 
        panelSwitcher.setLayout(panelSwitcherLayout);
                
        panelSwitcher.add(createDirectionPanel(), PANEL_DIRECTION);               
        panelSwitcher.add(createPowerDownPanel(), PANEL_POWERDOWN);
        panelSwitcher.add(createCardAndRegisterPanel(), PANEL_REGISTERS);
        panelSwitcher.add(regRepairPanel, PANEL_REPAIR);
        panelSwitcher.add(phaseInfo, PANEL_PHASE_EVAL);
        panelSwitcher.add(gameOverPanel, PANEL_GAMEOVER);
        panelSwitcher.add(userInfo, PANEL_USERINFO);
        panelToShow = PANEL_USERINFO;
        showPanel(PANEL_USERINFO);
        
    }

    private ScoutVertiefung createScoutHome(){
        return new ScoutVertiefung(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (((ScoutVertiefung) e.getSource()).isSelected()) {                 
                    scoutClicked(true);
                }
                else {                  
                    scoutClicked(false);
                }
            }
        });
    }
    
    private KlugscheisserLatte createWisenheimerHome() {
        return new KlugscheisserLatte(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (((KlugscheisserLatte) e.getSource()).selected()) {                 
                    if (human.mode == HumanPlayer.MODE_PROGRAM) {
                        klugscheisserClicked(true);
                        human.sendWisenheimerMsg();
                    }
                    else {
                        CAT.debug("asked wisenheimer but it did not make sense at this moment");
                    }
                }
                else {              
                    klugscheisserClicked(false);
                }
            }
        });
    }
    

    
    private AgainPowerDown createPowerDownPanel(){
        return  new AgainPowerDown(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {                
                sendAgainPowerDown(ae.getActionCommand().equals("powerdownagain"));
               //done in sendAgainPowerDown now:  setDialogInSidebarActive(false);
            }
        });
    }
    
    private GetDirection createDirectionPanel(){
        return new GetDirection(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int direct = Integer.parseInt(ae.getActionCommand());               
                sendDirection(direct);
               // done in sendDirection now: setDialogInSidebarActive(false);
            }
        });
    }
    
    private RepairRegisters createRegisterRepairPanel(){
       return  new RepairRegisters(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendRepairRegisters();
      // done in sendRepairRegsiters:          setDialogInSidebarActive(false);
            }
        });
    }
    
    private JPanel createCardAndRegisterPanel(){
        JPanel regsAndCards = new TJPanel();
        cards = new CardArray(new ActionListener() {
            public void actionPerformed(ActionEvent cardKlick) {
                treatCardKlick((CardView) cardKlick.getSource());
            }
        }, new ActionListener() {
            public void actionPerformed(ActionEvent sendKlick) {
                treatSendCards();
                setDialogInSidebarActive(false);
            }
        });

        registers = new RegisterArray(new ActionListener() {
            public void actionPerformed(ActionEvent registerKlick) {
                treatRegisterKlick((RegisterView) registerKlick.getSource());
            }
        }

        );
        regsAndCards.add(registers);
        regsAndCards.add(cards);
        return regsAndCards;
    }
    
    protected JPanel getWiseAndScoutPanel() {
        return wiseAndScout;

    }

    /**
     * display a message that is shown only to this player
     */
    public synchronized void showMessageToPlayer(String s) {
        
        if (isDialogInSidebarActive()) { /* don't switch :-) */
            return;
        }
        userInfo.setInfo(s);
        showPanel(panelToShow);     
        this.requestFocus();
    }
    
    protected  void setPanelToShow (String panelName) {
        panelToShow = panelName;
    }
    

    protected void fillPhaseInfoPanel(Bot [] robs, ScalableRegisterRow[]registerRows){
        phaseInfo.setContents(robs, registerRows);
        
    }
    
    /** Tell PhaseInfoPanel to cover all cards (show their backsides)     
     */
    protected void hidePhaseInfoCards() {
        phaseInfo.hideAll(true);
        
    }  
  
    /**
     * display info that the robot is power down int this turn
     */
    public synchronized void showPowerDown() {
         panelToShow = PANEL_PHASE_EVAL;
         showMessageToPlayer(Message.say("SpielerMensch", "istPowerDown"));
         this.requestFocus();
    }

    /**
     * display the card
     */
    public synchronized void showCards(ArrayList humanCards) {
        panelToShow = PANEL_REGISTERS;        
        cards.setCards(humanCards);
        showPanel(PANEL_REGISTERS);
        if (registers.allLocked()) {            
            cards.activateButton();
        }
        this.requestFocus();
        setDialogInSidebarActive(true);
    }

    
    protected synchronized void showPanel (String panelName) {
        panelSwitcherLayout.show(panelSwitcher, panelName);
    }

    /**
     * display the get direction request
     */
    public synchronized void showGetDirection() {
        showPanel(PANEL_DIRECTION);
        this.requestFocus();
        setDialogInSidebarActive(true);
    }

    /**
     * display the power down again request f
     */

    public synchronized void showRePowerDown() {
        showPanel(PANEL_POWERDOWN);
        this.requestFocus();
        setDialogInSidebarActive(true);
    }
    
    

    /**
     * display the register repair request
     */
    public synchronized void showRegisterRepair(Card[] robRegs, int repairNumber) {
        CAT.debug("Show Register Repair");
        registers.updateRegisters(robRegs);
        regRepairPanel.setChoices(registers.getRegisterViewArray(), repairNumber);
        showPanel(PANEL_REPAIR);
        this.requestFocus();
        setDialogInSidebarActive(true);
    }
    

    /**
     * show game over two types: a) winner + winner no. b) dead
     * @param lockPanel force the switcherPanel to not leave the dialog mode 
     *               (==showing the winnerpanel until the player quits, as he was removed and
     *                 won't get another dialog) 
     */
    public synchronized void showGameOver(boolean dead, int winnerNumber, String removalReason, boolean lockPanel) {        
        String bigVerticalMessage=dead?Message.say("SpielerMensch", "mkilled"):Message.say("SpielerMensch", "mflagreached");       
        gameOverPanel.setMessage(bigVerticalMessage,dead,removalReason);
        showPanel(PANEL_GAMEOVER);      
        // little not-so-nice hack:
        // we are not a dialog but pretend to be one, so nobody immediately replaces the reachedEnd-Panel        
        //  The Runnable/Thread only ensures that we are show at least 8 seconds 
        setDialogInSidebarActive(true);
        if (!lockPanel) { 
            panelToShow = PANEL_GAMEOVER;
	         Runnable foo = new Runnable(){
	             public void run(){                 
	                 synchronized (this){
	                     try {
	                         wait (8000);
	                     }
	                     catch (InterruptedException ie){
	                         CAT.error(ie.getMessage(), ie);
	                     }
	                     setDialogInSidebarActive(false);
	                 }
	             }
	         };
	         Thread t = new Thread(foo);
	         t.start();
        }
        //}
    }
    private void sendRepairRegisters(){
        sendRepairRegisters(regRepairPanel.getSelection());
    }
    protected synchronized void setDialogInSidebarActive(boolean dialogInSidebarActive) {
        this.dialogInSidebarActive = dialogInSidebarActive;
        if (!dialogInSidebarActive) {
            userInfo.setInfo(Message.say("SpielerMensch", "mrelax"));
            showPanel(panelToShow);
        }
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
            d("already chosen registers:" + alreadyChosen);
            for (int m = 0; m < alreadyChosen.size(); m++) {
                doPhaseRob[0].setMove(m, ((HumanCard) alreadyChosen.get(m)));
            }
            doPhaseRob[0].debug();
            human.passUpdatedScout(alreadyChosen.size(), doPhaseRob);
        }
    }

    /**
     * remove the scout
     */
    public void removeScout() {
        Bot[] doPhaseRob = new Bot[1];
        doPhaseRob[0] = Bot.getCopy(human.getRob());
        doPhaseRob[0].debug();
        human.passUpdatedScout(0, doPhaseRob);
    }

    /**
     * update the position of the wisenheimer
     */
    public void showUpdatedWisenheimer(boolean predicted) {
        int predCard = -1;
        if ((wisenheimerView.selected()) && (human.mode == HumanPlayer.MODE_PROGRAM)) {
            if (predicted) {
                predCard = human.getNextPrediction(registers.getWisenheimerCards(), cards.getCards());
            }
            else {
                predCard = human.getPrediction(registers.getWisenheimerCards(), cards.getCards());
            }
            if (predCard == -1) {
                wisenheimerView.setSelected(false);
            }

        }
        cards.setWisenheimer(predCard);
    }


    /**
     * Remove the Wisenheimer from the cards he might be sitting on
     */
    public void removeWisenheimer(){
        cards.removeWisenheimer();
    }

    
    protected void updateRegisters(Card[] robRegs) {
        registers.updateRegisters(robRegs);
        CAT.debug("Updating Registers...");
        registers.resetAll();
    }

    protected void sendDirection(int d) {
        human.sendDirection(d);
        setDialogInSidebarActive(false);
        showMessageToPlayer(Message.say("SpielerMensch", "mrelax"));
    }

    protected void sendAgainPowerDown(boolean again) {
        setDialogInSidebarActive(false);
        if (!again) {
            showMessageToPlayer(Message.say("SpielerMensch", "mkartenMisch"));
        }
        else {
            showPowerDown();
        }
        human.sendAgainPowerDown(again);
    }

 

    /**
     * 
     * @param regsRep A list of Integers, containing the number(s) of the register(s)
     *              to repair; register numbers start with 1 (not 0)
     */
    protected void sendRepairRegisters(ArrayList regsRep) {
        CAT.debug("sendRepairRegisters");
        setDialogInSidebarActive(false);
        showMessageToPlayer(Message.say("SpielerMensch", "mkartenMisch"));
        unlockRegisters(regsRep);
        human.sendRepair(regsRep);
    }

    private void unlockRegisters(ArrayList repairRegs) {
        if (CAT.isDebugEnabled()) {
            CAT.debug("registers before unlocking: " + registers.toString());
        }
        for (int i = 0; i < repairRegs.size(); i++) {
            int regNum = ((Integer) repairRegs.get(i)).intValue();
            CAT.debug("unlock register: " +regNum );
            registers.unlockRegister(regNum - 1);
        }
        if (CAT.isDebugEnabled()) {
        CAT.debug("register after unlocking: " + registers.toString());
        }
        registers.resetAll();

    }

    private void treatCardKlick(CardView cv) {
        boolean predicted = false;
        if ((cv.getCard() != null) && (!registers.allOcupied())) {
            registers.addCard(cv.getCard());
            predicted = cv.isWisenheimer();
            cv.reset();
            if (registers.allOcupied()) {
                cards.activateButton();
            }
        }
        showUpdatedScout();
        showUpdatedWisenheimer(predicted);
    }

    private void treatSendCards() {
        if (registers.allOcupied()) {
            showMessageToPlayer(Message.say("SpielerMensch", "mkartenMisch"));
            if (CAT.isDebugEnabled()) {
                CAT.debug("send to Server: " + registers.getCards()
                               + " powerDown: " + cards.wishesPowerDown());
            }
            panelToShow = PANEL_PHASE_EVAL;
            showPanel(PANEL_PHASE_EVAL);
            human.sendCards(registers.getCards(), cards.wishesPowerDown());
            if (cards.wishesPowerDown()) {
                showPowerDown();
            }   
            cards.resetAll();
        }
        wisenheimerView.setSelected(false);
    }
    

    private void treatRegisterKlick(RegisterView rv) {
        if ((rv.getCard() != null) && (!rv.locked())) {
            rv.getCard().setState(HumanCard.FREE);
            cards.addCard(rv.getCard());
            rv.reset();
        }
        if (!registers.allOcupied()) {
            cards.deActivateButton();
        }
        showUpdatedScout();
        showUpdatedWisenheimer(false);
    }

    private void klugscheisserClicked(boolean awake) {
        //if ((awake) && ( human.mode == HumanPlayer.MODE_PROGRAM)){
        if (human.mode == HumanPlayer.MODE_PROGRAM) {
            showUpdatedWisenheimer(false);
        }
    }

    private void scoutClicked(boolean awake) {
        if (awake) {
            showUpdatedScout();
        }
        else {
            removeScout();
        }
    }

    public void sendChatMessage(String msg) {
        if (msg != null && msg.trim().length() != 0) {
            human.sendChat(msg);
        }
    }

    protected void quitHumanPlayer(boolean joinHPThread) {
        CAT.debug("HumanView asks the HumanPLayer to quit..");        
        human.shutdown();
        if (joinHPThread){
            try {
                human.join(3000);
            }
            catch (InterruptedException dontCare){
                CAT.warn(dontCare);
            }
        }
    }

    private void d(String s) {
        Global.debug(this, s);
    }

    public HumanPlayer getHumanPlayer() {
        return human;
    }

    private synchronized boolean isDialogInSidebarActive() {
        return dialogInSidebarActive;
    }
    

  
   public Dimension getPreferredSize () {
        return new Dimension(150,550);
    }/*
   public Dimension getMinimumSize () {
       return new Dimension(150,550);
   }
   */
}

