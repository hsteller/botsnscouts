package de.botsnscouts.gui;

import  de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * gui-container that has a card
     */
public class CardView extends JButton {
    ImageIcon cardFree = ImageMan.CardRUECK;
    ImageIcon cardImage;
    HumanCard h;
    private boolean wisenheimerPresent = false;

    public CardView() {
	this(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Pressed:"+ae.getActionCommand());
		}
	    }
	     );
	setEnabled(true);
    }

    public CardView (ActionListener al) {
	setContentAreaFilled(false);
	setBorder(null);
	setIcon(cardFree);
	setEnabled(false);
	addActionListener(al);
    }

    public void reset() {
	setSelected(false);
	wisenheimerPresent=false;
	setIcon(cardFree);
	h = null;
    }

    public boolean locked() {
	return h.locked();
    }


    public void setCard(HumanCard h) {
	this.h = h;
	String ktyp = h.getaktion();
	if (ktyp.equals("M1")) cardImage = ImageMan.CardM1;
	else if (ktyp.equals("M2")) cardImage = ImageMan.CardM2;
	else if (ktyp.equals("M3")) cardImage = ImageMan.CardM3;
	else if (ktyp.equals("BU")) cardImage = ImageMan.CardBU;
	else if (ktyp.equals("RL")) cardImage = ImageMan.CardRL;
	else if (ktyp.equals("RR")) cardImage = ImageMan.CardRR;
	else if (ktyp.equals("UT")) cardImage = ImageMan.CardUT;
	else System.err.println("CardView: the card is bad.");
	setIcon(cardImage);
	setSelectedIcon(cardFree);
	setEnabled(true);
    }

    public HumanCard getCard() {
	return h;
    }

    

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        CardView c = new CardView();

	f.getContentPane().add(c);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }
    
    public void paintComponent(Graphics g){
	super.paintComponent(g);
	if (wisenheimerPresent) g.drawImage(Images.KSCHEISSER,25,42,this);
    }
    

    protected void setWisenheimer() {
	wisenheimerPresent = true;
	Global.debug(this,"lasse mich von einem Klugscheisser besetzten, ich habe die Karte: "+h);
	repaint();
    }

}
