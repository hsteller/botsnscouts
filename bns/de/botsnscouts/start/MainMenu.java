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

package de.botsnscouts.start;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJButton;


public class MainMenu extends JPanel implements ActionListener {
    private static Category CAT = Category.getInstance(MainMenu.class.getName());

    private Paint paint;
    private JButton gameBut;
    private JButton partBut;
    private JButton watchBut;
    private JButton editBut;
    private JButton endBut;
    private JLabel logo;

    private Start parent;

    public MainMenu(Start par) {
        parent = par;
        parent.setTitle(Message.say("Start", "mStartTitel"));
        paint = parent.paint;

        GridLayout lay = new GridLayout(6, 1);
        lay.setHgap(170);
        lay.setVgap(20);
        JPanel buttons = new JPanel(lay);
        setOpaque(false);
        buttons.setOpaque(false);
        buttons.setBackground(new java.awt.Color(60, 80, 60, 200));
        setLayout(new GridBagLayout());

        logo = new Logo();

        gameBut = OptionPane.getTransparentButton(Message.say("Start", "mSpielStarten"));
        partBut = OptionPane.getTransparentButton(Message.say("Start", "mTeilnehmen"));
        watchBut = OptionPane.getTransparentButton(Message.say("Start", "mZuschauen"));
        editBut = OptionPane.getTransparentButton(Message.say("Start", "mEdit"));
        endBut = new TJButton(Message.say("Start", "mBeenden"));

        gameBut.addActionListener(this);
        partBut.addActionListener(this);
        watchBut.addActionListener(this);
        editBut.addActionListener(this);
        endBut.addActionListener(this);

        gameBut.setActionCommand("gameBut");
        partBut.setActionCommand("partBut");
        watchBut.setActionCommand("watchBut");
        editBut.setActionCommand("editBut");
        endBut.setActionCommand("endBut");

        buttons.add(gameBut);
        buttons.add(partBut);
        buttons.add(watchBut);
        buttons.add(editBut);
        buttons.add(endBut);

        GridBagConstraints con = new GridBagConstraints();
        buttons.setBorder(new EmptyBorder(50, 20, 50, 20));
        con.gridx = 0;
        con.gridheight = 2;
        con.weightx = 0.1;
        con.weighty = 0.1;
        add(logo, con);
        con.gridheight = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.VERTICAL;
        con.insets = new Insets(50, 20, 50, 20);
        con.anchor = GridBagConstraints.CENTER;
        con.weighty = 0.2;
        con.weightx = 0.0;
        add(buttons, con);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("endBut")) {
            parent.myclose();
        } else if (e.getActionCommand().equals("gameBut")) {
            parent.showGameFieldPanel();
        } else if (e.getActionCommand().equals("partBut")) {
            parent.showParticipatePanel();
        } else if (e.getActionCommand().equals("watchBut")) {
            parent.showWatchPanel();
        } else if (e.getActionCommand().equals("editBut")) {
            new de.botsnscouts.editor.BoardEditor();
        }
    }

    public void unrollOverButs() {
        gameBut.getModel().setRollover(false);
        partBut.getModel().setRollover(false);
        watchBut.getModel().setRollover(false);
        endBut.getModel().setRollover(false);
        editBut.getModel().setRollover(false);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension d = getSize();
        g2.setPaint(paint);
        g2.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

}