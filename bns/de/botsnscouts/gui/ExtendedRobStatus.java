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


import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;


import de.botsnscouts.util.*;
/**
 * Fenster, das weitere Statusinfos anzeigt
 * @author Lukasz Pekacki
 */

public class ExtendedRobStatus extends JFrame {
    
    JLabel name, gesperrt, gelegt, archivpos, aktiviert, virtuell, pos;
    int xsize = 350;
    int ysize = 200;

    
    public ExtendedRobStatus () {
	this(Roboter.getNewInstance("DefaultRob"),100,100);
    }
    

    public ExtendedRobStatus (Roboter r, int locationX, int locationY) {
	setTitle(Message.say("Ausgabe","statusVon")+r.getName());
	setLocation(locationX,locationY);
	setSize(xsize,ysize);
	JPanel hauptPanel = new JPanel();

	// ---- gesperrte Regisster toString
	String gespReg = "[ ";
	if (r.gesperrteRegs() > 0) { 
	    for (int i = 0; i < r.gesperrteRegs(); i++) 
		if (r.getGesperrteRegister(i) != null) {
		    gespReg+= r.getGesperrteRegister()[i].getaktion() + " | ";
		}
	    gespReg += "]";
	}
	// ---- gelegte Karten toString
	String gelegtKarte = "[ ";
	if (r.getZug() != null) {
	    for (int i = 0; i < r.getZug().length; i++) 
		if (r.getZug()[i] != null) gelegtKarte+= r.getZug()[i].getaktion() + " | ";
	    gelegtKarte += "]";
	}
	// ---- Label erzeugen
	gesperrt = new JLabel(Message.say("Ausgabe","gespReg")+gespReg);
	hauptPanel.add(gesperrt);
	gelegt = new JLabel(Message.say("Ausgabe","gelKarte")+gelegtKarte);
	hauptPanel.add(gelegt);
	archivpos = new JLabel(Message.say("Ausgabe","archPos")+" x: "+r.getArchivX()+" y: "+r.getArchivY());
	hauptPanel.add(archivpos);
	aktiviert = new JLabel(Message.say("Ausgabe","aktiviert")+r.istAktiviert());
	hauptPanel.add(aktiviert);
	virtuell = new JLabel(Message.say("Ausgabe","virtuell")+r.istVirtuell());
	hauptPanel.add(virtuell);
	pos = new JLabel(Message.say("Ausgabe","pos")+" x: "+r.getX()+" y: "+r.getY());
	hauptPanel.add(pos);
	getContentPane().add(hauptPanel);
    }


    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JFrame f = new ExtendedRobStatus();
	f.setVisible(true);
    }
}
    





