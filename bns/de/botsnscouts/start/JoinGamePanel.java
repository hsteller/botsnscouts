package de.botsnscouts.start;

import de.botsnscouts.widgets.*;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Conf;
import de.botsnscouts.gui.OkComponent;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.*;
import java.util.Vector;

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
 * TODO: i18n
 *
 * @author Miriam
 */
public class JoinGamePanel extends ColoredComponent {

    private JTextField hostName = new TJTextField(Message.say("Start", "mServerInh"),
                                        JTextField.CENTER, true);
    private JTextField robName;
    private JComboBox colors;
    private int port = GameOptions.DPORT;

    private JList favServerList;
    private JList announcedGamesList = new JList();
    private JButton queryMetaButton = new TJButton(Conf.getDefaultMetaServer());

    private Start parent;

    private GamePreview gamePreview = new GamePreview( announcedGamesList, hostName );
    private Vector favServers;

    public JoinGamePanel(Start par) {

        parent = par;
        parent.setTitle(Message.say("Start", "mTeilnehmen"));

        setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(150, 150, 150, 150),
                OptionPane.niceBorder
        ));


        add(newParticipateAsPanel());
        add(Box.createVerticalStrut(20));
        add(newChooseGamePanel());
        add(Box.createVerticalStrut(20));

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

        JComponent left = createFindServersView();
        JComponent right = Box.createVerticalBox();

;

        right.add(new TJLabel(Message.say("Start", "mServer")+" ", Color.lightGray, true));
        right.add(hostName);
        right.add(gamePreview);

        panel.add(left);
        panel.add(right);

        return panel;

    }

    private JComponent createFindServersView() {
        // For now this contains just the list,
        // later buttons may follow...

        JComponent box = Box.createVerticalBox();
        box.add(new TJLabel("Favorite Servers"));

        favServers =  Conf.getMultiplePropertyVector("start.favServers");

        favServerList = new JList(favServers);
        favServerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favServerList.addListSelectionListener( new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                hostName.setText((String )favServerList.getSelectedValue());
            }

        });
        box.add(favServerList);

        hostName.addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent event) {
                //Do not care.
            }

            public void focusLost(FocusEvent event) {
                //Check if we already know the server.
                String entry = hostName.getText();
                if (!favServers.contains(entry)) {
                    favServers.add(entry);
                    Conf.setMultipleProperty("start.favServers", favServers);
                    Conf.saveProperties();
                }
            }

        }
        );

        queryMetaButton.addActionListener(gamePreview);

        box.add( new TJLabel("Query Meta Server") );
        box.add( queryMetaButton );

        box.add( new TJLabel("Public Games:"));
        box.add( announcedGamesList );

        return box;
    }


    private JComponent newParticipateAsPanel() {

        robName = new TJTextField(Conf.getDefaultRobName(), JTextField.CENTER, true);
        colors = new RoboBox(true);
        //colors.setOpaque(false);
        colors.setFont(new Font("Sans", Font.BOLD, 24));

        JComponent panel = new ColoredComponent();
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        GridLayout layout = new GridLayout(2,2);
        layout.setHgap(20);
        layout.setVgap(10);
        panel.setLayout( layout );

        panel.add( new TJLabel(Message.say("Start", "mParticipateAs"), Color.lightGray, true));
        panel.add( Box.createGlue() );

        JComponent panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.X_AXIS));

        panelLeft.add(new TJLabel(Message.say("Start", "mName")+" ", Color.lightGray, true));
        panelLeft.add(robName);

        panel.add( panelLeft );

        JComponent panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.X_AXIS));

        panelRight.add(new TJLabel(Message.say("Start", "mFarbe")+" ", Color.lightGray, true));
        panelRight.add(colors);

        panel.add(panelRight);

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
