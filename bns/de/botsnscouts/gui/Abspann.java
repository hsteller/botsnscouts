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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.JPanel;

import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;
/**
 * Diese Klasse spielt bei Spielende den Abspann ab
 * @deprecated use de.botsnscouts.gui.WinnerListPanel
 **/
public class Abspann extends JPanel {
    // Konstanten
    private static final Color foreColor = new Color(110,240,110);
    private final Font font1 = new Font("SansSerif",Font.BOLD,24);
    // Variablen
    public Abspann(String[] gewinnerListe) {
	setFont(font1);
	if (gewinnerListe != null) {
	setLayout(new GridLayout((gewinnerListe.length+2),3));
	for (int i=0; i<4; i++) add(new Label(""));
	add(new Label(Message.say("Abspann","mTitelAbspann")));
	add(new Label(""));

	    for (int i=0; i < gewinnerListe.length; i++) {
		if((gewinnerListe[i].equals("null"))) {
		    add(new Label(""));
		    add(new Label(""));
		    add(new Label(""));
		}
		else {
		    add(new Label(""));
		    add(new Label(Message.say("Abspann","mAbspannPlazierung",i+1,gewinnerListe[i])));
		    add(new Label(""));
		}
		
	    }
	}
	else {
	    Global.debug(this,"Gewinnerliste ist leer");
	    setLayout(new GridLayout(2,1));
	    add(new Label(Message.say("Abspann","mTitelAbspannTot")));
	    add(new Label(Message.say("Abspann","mTitelAbspannTotInfo")));
	}
	    
    }
								
}
