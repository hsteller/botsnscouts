package de.botsnscouts.gui;

import  de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * KlugscheisserLatte, die im oben rechts in der Infoleiste dargestellt ist
     */
public class KlugscheisserLatte extends JToggleButton {

    int xsize=60, ysize=60;
    Image klugSchlaf[];
    Image klugWach[];

    public KlugscheisserLatte(ActionListener al) {
	// Referenz holen
	klugSchlaf=ImageMan.getImages(ImageMan.KSCHLAF);
	klugWach=ImageMan.getImages(ImageMan.KWACH);
	setContentAreaFilled(false);
	setBorder(null);
	setToolTipText(Message.say("KlugscheisserLatte","mtooltip"));
	setIcon(new ImageIcon(klugSchlaf[0]));
	setSelectedIcon(new ImageIcon(klugWach[0]));
	addActionListener(al);
	
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    protected void reset() {
	setSelected(false);
    }

    protected boolean selected() {
	return isSelected();
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JFrame f = new JFrame();
	f.setSize(100,100);
	f.getContentPane().add(new KlugscheisserLatte(null));
	f.setVisible(true);
    }

}
