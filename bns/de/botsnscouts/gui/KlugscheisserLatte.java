package de.spline.rr;

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
    SpielerMensch mensch;
    int xsize=60, ysize=60;
    Image klugSchlaf[];
    Image klugWach[];

    public KlugscheisserLatte(SpielerMensch spielerMensch) {
	// Referenz holen
	mensch = spielerMensch;
	klugSchlaf=ImageMan.getImages(ImageMan.KSCHLAF);
	klugWach=ImageMan.getImages(ImageMan.KWACH);
	setContentAreaFilled(false);
	setBorder(null);
	setToolTipText(Message.say("KlugscheisserLatte","mtooltip"));
	setIcon(new ImageIcon(klugSchlaf[0]));
	setSelectedIcon(new ImageIcon(klugWach[0]));
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (isSelected()) {
			//			Global.debug(this,"Klugscheisser, wach auf!");
			mensch.klugscheisserClicked(true);
		    }
		    else {
			//			Global.debug(this,"Klugscheisser, geh schlafen!");
			mensch.klugscheisserClicked(false);
		    }
		}});
	
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    public void reset() {
	setSelected(false);
    }


    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
        JFrame f = new JFrame();
	f.setSize(100,100);
	f.getContentPane().add(new KlugscheisserLatte(null));
	f.setVisible(true);
    }

}
