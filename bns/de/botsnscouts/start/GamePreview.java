package de.botsnscouts.start;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import nanoxml.XMLElement;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.Socket;
import java.io.*;

import de.botsnscouts.util.Conf;
import org.apache.log4j.Category;

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
public class GamePreview extends JPanel implements ActionListener {

    /** Maps names of hosted games to GameOptions */
    private HashMap gameMap;
    private JList gameList;
    private JTextField hostField;
    private static String QUERY = "<query />";

    private static Category CAT = Category.getInstance(GamePreview.class);

    public GamePreview( final JList gameList, JTextField hostField )  {
        this.gameList = gameList;
        this.hostField = hostField;
        gameList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                setContent( (GameOptions )gameMap.get( gameList.getSelectedValue()) );
            }

        });
    }

    /** call this to stop the current preview */
    void clear() {

    }

    void setContent( GameOptions game ) {
        hostField.setText( game.getHost());
    }

    /** Query Metaserver */
    public void actionPerformed(ActionEvent event) {

        /** Query server */
        XMLElement xml = new XMLElement();
        try {
            Socket s = new Socket( Conf.getDefaultMetaServer(), Conf.getDefaultMetaServerPort());
            Reader reader = new InputStreamReader(s.getInputStream());
            Writer writer = new OutputStreamWriter(s.getOutputStream());
            writer.write( QUERY );
            writer.flush();
            xml.parseFromReader(reader);

        } catch (IOException ex) {
            CAT.warn(ex);
            //TODO
        }


        /** Parse xml */
        gameMap = GameOptions.parseXMLGames(xml);

        CAT.info("Got "+gameMap.size()+" games from meta server.");

        gameList.setListData( new Vector(gameMap.keySet()));

        /** Update preview */


    }

}
