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
public class RegisterView extends JButton {
    ImageIcon registerFree = ImageMan.CardRLEER;
    ImageIcon cardImage;
    HumanCard h;

    public RegisterView() {
	this(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Pressed:"+ae.getActionCommand());
		}
	    }
	     );
	setEnabled(true);
    }

    public RegisterView (ActionListener al) {
	setContentAreaFilled(false);
	setBorder(null);
	setIcon(registerFree);
	setEnabled(false);
	addActionListener(al);
    }

    public void reset() {
	setSelected(false);
	setEnabled(false);
	setIcon(registerFree);
	h = null;
    }

    public boolean locked() {
	return h.locked();
    }

    public void setLocked(boolean b){
	if(b) h.setState(HumanCard.LOCKED);
	else  h.setState(HumanCard.FREE);
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
	setEnabled(true);
    }

    public HumanCard getCard() {
	return h;
    }

    public void paintComponent(Graphics g){
	super.paintComponent(g);
	if(locked()) g.drawString("Locked",55,55);
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        RegisterView r = new RegisterView();

	f.getContentPane().add(r);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }

}
