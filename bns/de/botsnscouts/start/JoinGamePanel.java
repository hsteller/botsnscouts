package de.botsnscouts.start;

import de.botsnscouts.widgets.*;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Conf;
import de.botsnscouts.gui.OkComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/*
  *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001-2004 scouties.                               *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 */

/**
 * Panel for choosing options while joining a game, incl. querying the metaserver
 * about currently available games.
 *
 * This will replace ParticipatePanel !!!
 *
 * @author Miriam
 */
public class JoinGamePanel extends ColoredComponent {

    private JTextField hostName;
    private JTextField robName;
    private JComboBox colors;
    private int port = GameOptions.DPORT;

    private Start parent;

    public JoinGamePanel(Start par) {

        parent = par;
        parent.setTitle(Message.say("Start", "mTeilnehmen"));

        GridLayout lay;
        lay = new GridLayout(3, 1);
        lay.setHgap(170);
        lay.setVgap(80);

        setLayout(lay);
        setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(150, 150, 150, 150),
                OptionPane.niceBorder
        ));


        add(newParticipateAsPanel());
        add(newChooseGamePanel());

        OkComponent confirmPanel = new OkComponent(Message.say("Start", "mGoButton"),
                Message.say("Start", "mZurueckButton"));

        confirmPanel.addOkListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread smth = parent.facade.participateInAGame(hostName.getText(), port, robName.getText(),
                        colors.getSelectedIndex());
                Global.debug(this, "SpielerMensch gestartet");
                parent.addKS(smth);
                parent.hide();
                parent.dispose();
                parent.beenden();
            }
        });
        confirmPanel.addBackListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showMainMenu();
            }
        });

        add(confirmPanel);

    }

    private JComponent newChooseGamePanel() {

        JComponent panel = new ColoredComponent();

        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        hostName = new TJTextField(Message.say("Start", "mServerInh"),
                JTextField.CENTER, true);

        panel.add(new TJLabel(Message.say("Start", "mServer"), Color.lightGray, true));
        panel.add(hostName);

        return panel;

    }

    private JComponent newParticipateAsPanel() {

        robName = new TJTextField(Conf.getDefaultRobName(), JTextField.CENTER, true);
        colors = new RoboBox(true);
        //colors.setOpaque(false);
        colors.setFont(new Font("Sans", Font.BOLD, 24));

        JComponent panel = new ColoredComponent();
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add( new TJLabel(Message.say("Start", "mParticipateAs"), Color.lightGray, true));

        JComponent panelSouth = new JPanel();
        panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));

        panelSouth.add(new TJLabel(Message.say("Start", "mName"), Color.lightGray, true));
        panelSouth.add(robName);

        panelSouth.add(Box.createHorizontalGlue());

        panelSouth.add(new TJLabel(Message.say("Start", "mFarbe"), Color.lightGray, true));
        panelSouth.add(colors);

        panel.add(panelSouth);

        return panel;
    }

    /**
     * Copied from the old ParticiptePanel
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint( parent.paint );
        g2d.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

}
