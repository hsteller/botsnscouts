package de.botsnscouts.start;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nanoxml.XMLElement;

import org.apache.log4j.Category;

import de.botsnscouts.util.Conf;
import de.botsnscouts.widgets.TJLabel;

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
 * @author miriam
 */
@SuppressWarnings("serial")
public class GamePreview extends JPanel implements ActionListener {

    /** Maps names of hosted games to GameOptions */
    private HashMap<String, GameOptions> gameMap;

    private JList<String> gameList;

    private JTextField hostField;

    private TJLabel info = new TJLabel();

    private static String QUERY = "<query />";

    private static Category CAT = Category.getInstance(GamePreview.class);

    public GamePreview(final JList<String> gameList, JTextField hostField) {
        this.gameList = gameList;
        this.hostField = hostField;
        gameList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                setContent(gameMap.get(gameList.getSelectedValue()));
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(info);
    }

    /** call this to stop the current preview */
    void clear() {
        info.setVisible(false);

    }

    void setContent(GameOptions game) {
        hostField.setText(game.getHost());
        String inv = game.getInvitor();
        info.setText("Game hosted by " + (inv == null ? "?" : inv + " at " + game.getHost()) + "\n" + game.getComment()
                        + "\n" + "Rules: " + (game.isAllowScout() ? "Scout allowed" : "NoScout") + ", "
                        + (game.isAllowWisenheimer() ? "Wisenheimer allowed" : "No Wisenheimer"));
        // TODO: Display icons for wisenheimer&scout instead of text
        // TODO: display board
        info.setVisible(true);
    }

    /** Query Metaserver */
    public void actionPerformed(ActionEvent event) {

        /** Query server */
        XMLElement xml = new XMLElement();
        try {
            Socket s = new Socket(Conf.getDefaultMetaServer(), Conf.getDefaultMetaServerPort());
            Reader reader = new InputStreamReader(s.getInputStream());
            Writer writer = new OutputStreamWriter(s.getOutputStream());
            writer.write(QUERY);
            writer.flush();
            xml.parseFromReader(reader);

        }
        catch (IOException ex) {
            CAT.error(ex.getMessage(), ex);
        }

        /** Parse xml */
        gameMap = GameOptions.parseXMLGames(xml);

        CAT.info("Got " + gameMap.size() + " games from meta server.");

        gameList.setListData(new Vector<String>(gameMap.keySet()));

        /** Update preview */

    }

}
