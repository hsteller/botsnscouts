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


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJLabel;

/**
 * Fenster, das weitere Statusinfos anzeigt
 * @author Lukasz Pekacki
 */

public class ExtendedRobStatus extends JFrame {
    
    JLabel name, gesperrt, gelegt, archivpos, aktiviert, virtuell, pos;
    int xsize = 350;
    int ysize = 200;

    
    public ExtendedRobStatus () {
	this(Bot.getNewInstance("DefaultRob"),100,100);
    }
    

    public ExtendedRobStatus (Bot r, int locationX, int locationY) {
	setTitle(Message.say("Ausgabe","statusVon")+r.getName());
	setLocation(locationX,locationY);
	setSize(xsize,ysize);
	JPanel hauptPanel = new JPanel();

	// ---- gesperrte Regisster toString
	String gespReg = "[ ";
	if (r.countLockedRegisters() > 0) {
	    for (int i = 0; i < r.countLockedRegisters(); i++)
		if (r.getLockedRegister(i) != null) {
		    gespReg+= r.getLockedRegisters()[i].getAction() + " | ";
		}
	    gespReg += "]";
	}
	// ---- gelegte Karten toString
	String gelegtKarte = "[ ";
	if (r.getMove() != null) {
	    for (int i = 0; i < r.getMove().length; i++)
		if (r.getMove()[i] != null) gelegtKarte+= r.getMove()[i].getAction() + " | ";
	    gelegtKarte += "]";
	}
	// ---- Label erzeugen
	gesperrt = new TJLabel(Message.say("Ausgabe","gespReg")+gespReg);
	hauptPanel.add(gesperrt);
	gelegt = new TJLabel(Message.say("Ausgabe","gelKarte")+gelegtKarte);
	hauptPanel.add(gelegt);
	archivpos = new TJLabel(Message.say("Ausgabe","archPos")+" x: "+r.getArchiveX()+" y: "+r.getArchiveY());
	hauptPanel.add(archivpos);
	aktiviert = new TJLabel(Message.say("Ausgabe","aktiviert")+r.isActivated());
	hauptPanel.add(aktiviert);
	virtuell = new TJLabel(Message.say("Ausgabe","virtuell")+r.isVirtual());
	hauptPanel.add(virtuell);
	pos = new TJLabel(Message.say("Ausgabe","pos")+" x: "+r.getX()+" y: "+r.getY());
	hauptPanel.add(pos);
	getContentPane().add(hauptPanel);
    }


    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JFrame f = new ExtendedRobStatus();
	f.setVisible(true);
    }
}
    





