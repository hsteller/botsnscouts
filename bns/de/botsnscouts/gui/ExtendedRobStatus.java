package de.spline.rr;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Fenster, das weitere Statusinfos anzeigt
 * @author Lukasz Pekacki
 */

public class ExtendedRobStatus extends JFrame {
    
    JLabel name, gesperrt, gelegt, archivpos, aktiviert, virtuell, pos;
    int xsize = 350;
    int ysize = 200;

    public ExtendedRobStatus () {
	this(new Roboter("DefaultRob"),100,100);
    }

    public ExtendedRobStatus (Roboter r, int locationX, int locationY) {
	setTitle(Message.say("Ausgabe","statusVon")+r.name);
	setLocation(locationX,locationY);
	setSize(xsize,ysize);
	JPanel hauptPanel = new JPanel();

	// ---- gesperrte Regisster toString
	String gespReg = "[ ";
	if (r.gesperrteRegs() > 0) { 
	    for (int i = 0; i < r.gesperrteRegs(); i++) 
		if (r.getGesperrteRegister(i) != null) {
		    gespReg+= r.gesperrteRegister[i].getaktion() + " | ";
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
	archivpos = new JLabel(Message.say("Ausgabe","archPos")+" x: "+r.archivX+" y: "+r.archivY);
	hauptPanel.add(archivpos);
	aktiviert = new JLabel(Message.say("Ausgabe","aktiviert")+r.aktiviert);
	hauptPanel.add(aktiviert);
	virtuell = new JLabel(Message.say("Ausgabe","virtuell")+r.virtuell);
	hauptPanel.add(virtuell);
	pos = new JLabel(Message.say("Ausgabe","pos")+" x: "+r.getX()+" y: "+r.getY());
	hauptPanel.add(pos);
	getContentPane().add(hauptPanel);
    }


    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
        JFrame f = new ExtendedRobStatus();
	f.setVisible(true);
    }
}
    





