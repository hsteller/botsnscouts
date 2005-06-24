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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.ColoredComponent;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJButton;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJTextField;

/**
 * The panel you get when you want to participate in a game hosted by someone else.
 * You choose the server parameters and you player here and then you
 * may press Go to register.
 */
public class ParticipatePanel extends ColoredComponent implements ActionListener {

    private JTextField hostName;
    private JTextField robName;
    private JComboBox colors;
    private int port = GameOptions.DPORT;

    private Start parent;

    private Paint paint;

    public ParticipatePanel(Start par) {
    	
        TJButton go;
        TJButton back;

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



        hostName = new TJTextField(Message.say("Start", "mServerInh"),
                               JTextField.CENTER, true);
        robName = new TJTextField(Conf.getDefaultRobName(), JTextField.CENTER, true);
        colors = new RoboBox(true);
        colors.setOpaque(false);
        Font bigFont = new Font("Sans", Font.BOLD, 20);
        colors.setFont(bigFont);
        go = new TJButton(Message.say("Start", "mGoButton"));
        back = new TJButton(Message.say("Start", "mZurueckButton"));

        go.addActionListener(this);
        back.addActionListener(this);

        go.setActionCommand("go");
        back.setActionCommand("back");

        add(new TJLabel(Message.say("Start", "mServer"), Color.lightGray, bigFont));
        add(hostName);
        add(new TJLabel(Message.say("Start", "mName"), Color.lightGray, bigFont ));
        add(robName);

        add(new TJLabel(Message.say("Start", "mFarbe"), Color.lightGray, bigFont ));
        add(colors);
        add(back);
        add(go);
        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) {
            BNSThread smth = Facade.participateInAGame(hostName.getText(), port, robName.getText(),
                    colors.getSelectedIndex());
            Global.debug(this, "SpielerMensch gestartet");
            parent.addKS(smth);
            parent.hide();
           // XXX HS 28.05.2005 parent.dispose();
           // XXX HS 28.05.2005 parent.beenden();
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
