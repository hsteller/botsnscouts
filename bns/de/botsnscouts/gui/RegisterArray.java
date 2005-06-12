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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JWindow;
import javax.swing.plaf.metal.MetalLookAndFeel;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.TJPanel;

/**
 * where the registers are displayed in a column
 * @author Lukasz Pekacki
 */
public class RegisterArray extends TJPanel {

    private ArrayList registerView = new ArrayList(Bot.NUM_REG);
    private int xsize = 70, ysize = 550;

    public RegisterArray() {
        this(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.err.println("Register klicked.");
            }
        });

    }

    public RegisterArray(ActionListener register) {
        setLayout(new GridLayout(Bot.NUM_REG, 1));
        int size=Bot.NUM_REG;
        for (int i = 0; i < size; i++) {
            RegisterView r = new RegisterView(register);
            registerView.add((i), r);
            add(r);
        }

    }

    protected void resetAll() {
        for (int i = 0; i < registerView.size(); i++) {
            ((RegisterView) registerView.get(i)).reset();
        }
    }

    /** 
     * 
     * @return a list containing all cards that already were chosen for the move (doesn't contain content of lockes registers).
     */
    protected ArrayList getCards() {
        ArrayList regs = new ArrayList(programmed());
        int size = registerView.size();
        for (int i = 0; i < size; i++) {
            RegisterView view = (RegisterView) registerView.get(i); 
            if (!view.locked()) {
                regs.add(view.getCard());
            }
        }
        return regs;
    }

    /**
     * 
     * @return the cards in the registers atm, including content of locked registers 
     */
    protected ArrayList getWisenheimerCards() {
        ArrayList regs = new ArrayList(programmed());
        int size = registerView.size();
        for (int i = 0; i < size; i++) {
            regs.add(((RegisterView) registerView.get(i)).getCard());
        }
        return regs;
    }

    protected ArrayList getAlreadyChosen() {
        int ap = alreadyProgrammed();
        d("already Programmed Registers: " + ap);
        ArrayList regs = new ArrayList(ap);
        for (int i = 0; i < ap; i++) {
            regs.add(((RegisterView) registerView.get(i)).getCard());
        }
        return regs;

    }

    void addCard(HumanCard hc) {
        int size = registerView.size();
        for (int i = 0; i < size; i++) {
            RegisterView view = (RegisterView) registerView.get(i); 
            if (view.getCard() == null) {
                view.setCard(hc);
                break;
            }
        }
    }


    boolean allOcupied() {
        int size = registerView.size();
        for (int i = 0; i < size; i++) {
            if (((RegisterView) registerView.get(i)).getCard() == null) {
                return false;
            }
        }
        return true;
    }

    public Dimension getMinimumSize() {
        return new Dimension(xsize, ysize);
    }

    public Dimension getPreferredSize() {
        return new Dimension(xsize, ysize);
    }

    /**
     * Main method for ad-Hoc-Testing only.
     */
    public static void main(String args[]) {
        Message.setLanguage("deutsch");
        JWindow f = new JWindow();
        MetalLookAndFeel.setCurrentTheme(new GreenTheme());

        RegisterArray re = new RegisterArray();

        f.getContentPane().add(re);
        f.pack();
        f.setLocation(100, 100);
        f.setVisible(true);
    }

    protected void updateRegisters(Card[] roboCards) {
        int size = Bot.NUM_REG;
        for (int i = 0; i < size; i++) {
            if (roboCards[i] != null) {
                ((RegisterView) registerView.get(i)).setLocked(true);
            } else {
                ((RegisterView) registerView.get(i)).setLocked(false);
            }

        }
    }

    protected void unlockRegister(int index) {
        ((RegisterView) registerView.get(index)).setLocked(false);
    }

    protected ArrayList getRegisterViewArray() {
        return registerView;
    }

    protected boolean allLocked() {
        int size = registerView.size();
        for (int i = 0; i < size;  i++) {
            RegisterView view =  (RegisterView) registerView.get(i);
            if ( view == null || !view.locked() ) {                
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @return The number of registers to be programmed for the current move (5 - number of locked registers)
     */
    private int programmed() {
        int oc = 0;
        for (int i = 0; i < registerView.size(); i++) {
            if (((RegisterView) registerView.get(i)).locked()) {
                oc++;
            }
        }
        return (Bot.NUM_REG - oc);
    }

    private int alreadyProgrammed() {
        int oc = 0;
        for (int i = 0; i < registerView.size(); i++) {
            if (((RegisterView) registerView.get(i)).free()) {
                break;
            } else {
                oc++;
            }
        }
        return oc;
    }

    private void d(String s) {
        Global.debug(this, s);
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < registerView.size(); i++) {
            s += "Reg: " + (i + 1) + " hat Card: " + ((RegisterView) registerView.get(i)).getCard() + "\n";
        }
        return s;
    }

}






