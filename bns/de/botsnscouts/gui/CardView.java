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

import org.apache.log4j.Category;
    /**
     * gui-container that has a card
     */
public class CardView extends JButton {
    static final Category CAT = Category.getInstance( CardView.class );

    private HumanCard h;
    private boolean wisenheimerPresent = false;
    private Font prioFont=new Font("SansSerif",Font.PLAIN,8);
    private Color prioColor=Color.darkGray;

    private Image wisenheimerPic = ImageMan.getImages(ImageMan.WISENHEIMER_ACTIVE)[0];

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
	setIcon(ImageMan.CardRUECK);
	setEnabled(false);
	addActionListener(al);
    }

    public void reset() {
	setSelected(false);
	wisenheimerPresent=false;
	setIcon(ImageMan.CardRUECK);
	h = null;
    }

    protected void delWisenheimer(){
	wisenheimerPresent=false;
	repaint();
    }

    public boolean locked() {
	return h.locked();
    }


    public void setCard(HumanCard h) {
	this.h = h;
	String ktyp = h.getaktion();
	if (ktyp.equals("M1")) setIcon(ImageMan.CardM1);
	else if (ktyp.equals("M2")) setIcon(ImageMan.CardM2);
	else if (ktyp.equals("M3")) setIcon(ImageMan.CardM3);
	else if (ktyp.equals("BU")) setIcon(ImageMan.CardBU);
	else if (ktyp.equals("RL")) setIcon(ImageMan.CardRL);
	else if (ktyp.equals("RR")) setIcon(ImageMan.CardRR);
	else if (ktyp.equals("UT")) setIcon(ImageMan.CardUT);
	else System.err.println("CardView: the card is bad.");
	setSelectedIcon(ImageMan.CardRUECK);
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
	g.setFont(prioFont);
	g.setColor(prioColor);
	//Miriam: priority is multiplies by ten because the cards look better then.
	if (h!=null&&(h.getState() == HumanCard.FREE) && (h.getprio()>0)) g.drawString(""+10*h.getprio(),28,22);

	if (wisenheimerPresent) g.drawImage(wisenheimerPic,25,42,this);
    }


    protected void setWisenheimer() {
	wisenheimerPresent = true;
	CAT.debug("lasse mich von einem Klugscheisser besetzten, ich habe die Karte: "+h);
	repaint();
    }

    protected boolean isWisenheimer(){
	return wisenheimerPresent;
    }

}
