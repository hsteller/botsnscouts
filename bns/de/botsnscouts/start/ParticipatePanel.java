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

import de.botsnscouts.util.*;
import de.botsnscouts.widgets.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The panel you get when you want to participate in a game hosted by someone else.
 * You choose the server parameters and you player here and then you
 * may press Go to register.
 */
public class ParticipatePanel extends ColoredComponent implements ActionListener {

    JTextField serv;
    JTextField nam;
    JComboBox farb;

    Start parent;

    Paint paint;

    public ParticipatePanel(Start par) {

        TransparentButton go;
        TransparentButton back;

        parent = par;
        parent.setTitle(Message.say("Start", "mTeilnehmen"));
        paint = parent.paint;

        GridLayout lay;
        lay = new GridLayout(4, 2);
        lay.setHgap(170);
        lay.setVgap(80);

        setLayout(lay);
        setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(150, 150, 150, 150),
                OptionPane.niceBorder
        ));

        serv = new TJTextField(Message.say("Start", "mServerInh"),
                               JTextField.CENTER, true);
        nam = new TJTextField(Conf.getDefaultRobName(), JTextField.CENTER, true);
        farb = new RoboBox(true);
        farb.setOpaque(false);
        farb.setFont(new Font("Sans", Font.BOLD, 24));
        go = new TransparentButton(Message.say("Start", "mGoButton"));
        back = new TransparentButton(Message.say("Start", "mZurueckButton"));

        go.addActionListener(this);
        back.addActionListener(this);

        go.setActionCommand("go");
        back.setActionCommand("back");

        add(new TJLabel(Message.say("Start", "mServer"), Color.lightGray, true));
        add(serv);
        add(new TJLabel(Message.say("Start", "mName"), Color.lightGray, true ));
        add(nam);

        add(new TJLabel(Message.say("Start", "mFarbe"), Color.lightGray, true ));
        add(farb);
        add(go);
        add(back);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) {

            Thread smth = parent.fassade.amSpielTeilnehmen(serv.getText(), nam.getText(), farb.getSelectedIndex());
            Global.debug(this, "SpielerMensch gestartet");
            parent.addKS(smth);
            parent.hide();
            parent.dispose();
            parent.beenden();
        } else if (e.getActionCommand().equals("back")) {
            parent.showMainMenu();
        }

    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

}//class StartTeilZusch end
