package de.botsnscouts.gui;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJButton;

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
 * Provides ok and cancel button in a colored component.
 * Call addOKListener and addBackListener to privide functionality.
 *
 * @author miriam
 */
public class OkComponent  extends JPanel {

    private JButton okBut;
    private JButton backBut;

    /** ok/cancel-buttons with specified texts */
    public OkComponent(String ok, String back) {

        GridLayout lay = new GridLayout(1, 2);
        lay.setHgap(50);
        lay.setVgap(50);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        setLayout(lay);
        setOpaque(false);

        backBut = new TJButton(back);
        okBut = new TJButton(ok);

        add(backBut);
        add(okBut);

    }

    /** Ok/cancel buttons with specified ok-text*/
    public OkComponent(String ok) {
        this(ok, Message.say("Start", "mZurueckButton"));
    }

    public OkComponent() {
        this(Message.say("Start", "mOK")) ;
    }

    public void addOkListener(ActionListener l) {
        okBut.addActionListener(l);
    }

    public void addBackListener(ActionListener l) {
        backBut.addActionListener(l);
    }


}
