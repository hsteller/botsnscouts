/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.apache.log4j.Category;

import de.botsnscouts.widgets.ColoredComponent;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJTextField;

@SuppressWarnings("serial")
public class ChatLine extends ColoredComponent implements ActionListener, ComponentListener, KeyListener {

    static final Category CAT = Category.getInstance(ChatLine.class);

    // AusgabeView ausgabeview;
    View view;

    public JTextField text;

    Timer timer;

    HumanPlayer humanPlayer;

    ChatLine(/* AusgabeView ausgabeview, */View view, HumanPlayer humanPlayer) {
        // this.ausgabeview = ausgabeview;
        this.view = view;
        this.humanPlayer = humanPlayer;
        setLayout(new BorderLayout());
        add(new TJLabel("Chat: "), BorderLayout.WEST);
        text = new TJTextField(45) {
            boolean firstCall = true;

            public void paint(Graphics g) {
                if (firstCall) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                    super.paint(g2);
                }
                else
                    super.paint(g);
            }
        };
        text.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        text.setOpaque(false);
        add(text, BorderLayout.CENTER);
        text.addActionListener(this);
        text.addKeyListener(this);

        JButton tb = OptionPane.getTransparentButton("Send", 12);
        add(tb, BorderLayout.EAST);
        tb.addActionListener(this);
        setBorder(OptionPane.etchedBorder);
        timer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        addComponentListener(this);

    }

    private boolean autoHide;

    public boolean isAutoHide() {
        CAT.debug("autohode");
        return autoHide;
    }

    public void setAutoHide(boolean newAutoHide) {
        autoHide = newAutoHide;
    }

    public void actionPerformed(ActionEvent parm1) {
        CAT.debug("actionPerformed");
        String s = text.getText();
        if (s != null && s.trim().length() > 0)
            humanPlayer.sendChat(s);
        text.setText("");
        setVisible(false);
    }

    public void componentShown(ComponentEvent parm1) {
        CAT.debug("comp shown");
        text.requestFocus();
        timer.start();
    }

    public void componentHidden(ComponentEvent parm1) {
        CAT.debug("comp hidden");
        // XXX HS ausgabeview.requestFocus();
        text.setText("");
        KeyboardFocusManager kman = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Object o = kman.getFocusOwner();
        if (CAT.isDebugEnabled()) {
            CAT.debug("focus owner: " + o);
        }
        view.requestFocus();
        o = kman.getFocusOwner();
        if (CAT.isDebugEnabled()) {
            CAT.debug("focus owner: " + o);
        }
        timer.stop();

    }

    public void componentResized(ComponentEvent parm1) {
    }

    public void componentMoved(ComponentEvent parm1) {
    }

    public void keyTyped(KeyEvent parm1) {
        text.requestFocus();
        CAT.debug("keyTyped!");
    }

    public void keyPressed(KeyEvent parm1) {
        text.requestFocus();
        CAT.debug("keyPressed");

        if (parm1.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setVisible(false);
            view.requestFocus();
        }
        else
            timer.restart();
    }

    public void keyReleased(KeyEvent parm1) {
    }

}
