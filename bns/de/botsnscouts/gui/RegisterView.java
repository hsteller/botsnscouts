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

import  de.botsnscouts.util.*;
import de.botsnscouts.widgets.GreenTheme;

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
    int myX=0,myY=0;
    private Font prioFont=new Font("SansSerif",Font.PLAIN,8);
    private Color prioColor=Color.darkGray;

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
        setBorderPainted(false);
        setFocusPainted( false );
	setIcon(registerFree);
	setEnabled(true);
	addActionListener(al);
    }

    public void reset() {
	if (!locked()) {
	h = null;
	setIcon(registerFree);
	}
    }

    public boolean locked() {
	if (h != null) {
	return h.locked();
	}
	else return false;
    }

    public String getCardAction(){
	return h.getaktion();
    }

    public int getPrio(){
	return h.getprio();
    }

    public boolean free() {
	if (h != null) {
	return h.free();
	}
	else return true;
    }

    public void setLocked(boolean b){
	if(b) h.setState(HumanCard.LOCKED);
	else  {
	    if (h!=null) {
		h.setState(HumanCard.FREE);
	    }
	}
	//	repaint();
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
	h.setState(HumanCard.FILLED);
	setEnabled(true);

    }

    public HumanCard getCard() {
	return h;
    }

    static final Image lockedImage = ImageMan.getImage(ImageMan.PNG_REGLOCK);
    static Color back = new Color(65,65,65,192);
    public void paintComponent(Graphics g){
        g.setColor( back );
        g.fillRect(0,0,getWidth(),getHeight());
	super.paintComponent(g);
	if(myX==0||myY==0){
	    Dimension d=getSize();
	    myX=d.width;
	    myY=d.height;
	    Global.debug(this,"(myX,myY)=("+myX+","+myY+")");
	}
	if(locked()){
	    //g.drawImage( lockedImage,(myX-50)/2,(myY-50)-1,this);
	    g.drawImage( lockedImage,4,3,this);
	}
	g.setFont(prioFont);
	g.setColor(prioColor);
	//Miriam: priority is multiplies by ten because the cards look better then.
	if (h!=null && (h.getprio()>0)) g.drawString(""+10*h.getprio(),26,22);
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
        HumanCard card = new HumanCard( 1, "M1" );
        card.setState( HumanCard.LOCKED );
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        RegisterView r = new RegisterView(  );
        r.setCard( card );
        r.setLocked( true );

	f.getContentPane().add(r);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }

}
