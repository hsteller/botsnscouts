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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.plaf.metal.MetalLookAndFeel;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJCheckBox;
import de.botsnscouts.widgets.TJPanel;
/**
 * where the cards are displayed
 * @author Lukasz Pekacki
 */
public class CardArray extends TJPanel {

    private JButton sendButton = OptionPane.getTransparentButton(Message.say("SpielerMensch","senden"), 14);
    private ArrayList cardsView = new ArrayList(9);
    private JCheckBox powerDownBox = new TJCheckBox(Message.say("SpielerMensch","powerdown"),false);
    
    private Dimension myPrefSize = new Dimension (150,550);
    
    public CardArray() {
	this(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Card klicked.");
		}
	    }
	     ,new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Send.");
		}
	    });

    }

    public CardArray(ActionListener cards, ActionListener send) {
	JPanel chooser = new TJPanel( new GridLayout(2,1, 3, 3) );


	setLayout(new GridLayout(5,2));

	sendButton.setEnabled(false);
	powerDownBox.setEnabled(true);
        powerDownBox.setOpaque(false);
	sendButton.addActionListener(send);
	powerDownBox.setVerticalTextPosition(AbstractButton.BOTTOM);
	powerDownBox.setHorizontalTextPosition(AbstractButton.CENTER);
	powerDownBox.setFont(new Font("Dialog",Font.BOLD,8));
	for (int i=0; i<9; i++) {
	    CardView c = new CardView(cards);
	    cardsView.add((i),c);
	    add(c);
	}

	//chooser.setBorder(new EmptyBorder(25,0,0,0));
	chooser.add(sendButton);
	chooser.add(powerDownBox);
	
	add(chooser);

    }

    protected void resetAll() {
	powerDownBox.setSelected(false);
	sendButton.setEnabled(false);
	for (int i=0; i < cardsView.size(); i++) {
	    ((CardView) cardsView.get(i)).reset();
            ((CardView) cardsView.get(i)).setEnabled( false ); // XXX
	}
    }

    protected boolean wishesPowerDown() {
	return powerDownBox.isSelected();
    }

    public void addCard(HumanCard hc) {
	for (int i=0; i < cardsView.size(); i++) {
	    if (((CardView) cardsView.get(i)).getCard() == null) {
		((CardView) cardsView.get(i)).setCard(hc);
		break;
	    }
	}
    }

    protected ArrayList getCards() {
	ArrayList regs = new ArrayList(9);
	for (int i=0; i < cardsView.size(); i++) {
	    regs.add(((CardView) cardsView.get(i)).getCard());
	}
	return regs;
    }


    public void setCards(ArrayList cards) {
	resetAll();
	for (int i=0; i < cards.size(); i++) {
	    ((CardView) cardsView.get(i)).setCard((HumanCard)cards.get(i));
	    ((CardView) cardsView.get(i)).setEnabled( true );
	}
    }

    protected void activateButton() {
	sendButton.setEnabled(true);
    }

    protected void deActivateButton() {
	sendButton.setEnabled(false);
    }

    protected void setWisenheimer(int predCard) {
	for (int i=0;i<9;i++){
	    ((CardView)cardsView.get(i)).delWisenheimer();
	}
	if (predCard!=-1){
	    ((CardView)cardsView.get(predCard)).setWisenheimer();
	}
    }

    public Dimension getMinimumSize() {
	return myPrefSize;
    }

    public Dimension getPreferredSize() {
	return myPrefSize;
    }

   

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        CardArray re = new CardArray();

	f.getContentPane().add(re);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }



}






