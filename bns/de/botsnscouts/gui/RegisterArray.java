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

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * where the registers are displayed in a column
 * @author Lukasz Pekacki
 */
public class RegisterArray extends TJPanel {

    private ArrayList registerView = new ArrayList(5);
    private int xsize = 70, ysize = 550;

    public RegisterArray() {
        this(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.err.println("Register klicked.");
            }
        });

    }

    public RegisterArray(ActionListener register) {
        setLayout(new GridLayout(5, 1));

        for (int i = 0; i < 5; i++) {
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

    protected ArrayList getCards() {
        ArrayList regs = new ArrayList(programmed());
        for (int i = 0; i < registerView.size(); i++) {
            if (!((RegisterView) registerView.get(i)).locked()) {
                regs.add(((RegisterView) registerView.get(i)).getCard());
            }
        }
        return regs;
    }

    protected ArrayList getWisenheimerCards() {
        ArrayList regs = new ArrayList(programmed());
        for (int i = 0; i < registerView.size(); i++) {
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
        for (int i = 0; i < registerView.size(); i++) {
            if (((RegisterView) registerView.get(i)).getCard() == null) {
                ((RegisterView) registerView.get(i)).setCard(hc);
                break;
            }
        }
    }


    boolean allOcupied() {
        for (int i = 0; i < registerView.size(); i++) {
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
        for (int i = 0; i < 5; i++) {
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
        for (int i = 0; i < registerView.size(); i++) {
            if ((RegisterView) registerView.get(i) != null) {
                if (!((RegisterView) registerView.get(i)).locked()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private int programmed() {
        int oc = 0;
        for (int i = 0; i < registerView.size(); i++) {
            if (((RegisterView) registerView.get(i)).locked()) {
                oc++;
            }
        }
        return (5 - oc);
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






