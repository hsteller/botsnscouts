package de.spline.rr;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * Scoutvertiefung, die im oben rechts in der Infoleiste dargestellt ist
     */
public class ScoutVertiefung extends JToggleButton {
    SpielerMensch mensch;
    int xsize=60, ysize=60;
    Image imageActive[];
    Image imageDream[];
    //    Image scoutImages[];

    public ScoutVertiefung(SpielerMensch spielerMensch) {
	// Referenz holen
	mensch = spielerMensch;
	imageActive=ImageMan.getImages(ImageMan.SCHLAFPLATZ);
	imageDream=ImageMan.getImages(ImageMan.SCHLAFSCOUT);
	//	scoutImages=ImageMan.getImages(ImageMan.SCOUT);
	setContentAreaFilled(false);
	setBorder(null);
	setToolTipText(Message.say("ScoutVertiefung","mtooltip"));
	setIcon(new ImageIcon(imageActive[0]));
	//	setPressedIcon(new ImageIcon(scoutImages[2]));
	setSelectedIcon(new ImageIcon(imageDream[0]));
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (isSelected()) {
			//			Global.debug(this,"Scout, wach auf!");
			mensch.scoutClicked(true);
		    }
		    else {
			//			Global.debug(this,"Scout, geh schlafen!");
			mensch.scoutClicked(false);
		    }
		}});
	
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }


    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
        JFrame f = new JFrame();
	f.setSize(100,100);
	f.getContentPane().add(new ScoutVertiefung(null));
	f.setVisible(true);
    }

}
