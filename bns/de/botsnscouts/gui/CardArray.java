package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * where the cards are displayed
 * @author Lukasz Pekacki
 */
public class CardArray extends JPanel {

    private JButton sendButton = new JButton(Message.say("SpielerMensch","senden"));
    private ArrayList cardsView = new ArrayList(9);
    private JCheckBox powerDownBox = new JCheckBox(Message.say("SpielerMensch","powerdown"),false);
    private int xsize=150, ysize=550;

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
	JPanel chooser = new JPanel();

	setLayout(new GridLayout(5,2));

	sendButton.setEnabled(false);
	sendButton.addActionListener(send);
	powerDownBox.setEnabled(false);
	powerDownBox.setVerticalTextPosition(AbstractButton.BOTTOM);
	powerDownBox.setHorizontalTextPosition(AbstractButton.CENTER);
	powerDownBox.setFont(new Font("Dialog",Font.BOLD,8));
	for (int i=0; i<9; i++) {
	    CardView c = new CardView(cards);
	    cardsView.add((i),c);
	    add(c);
	}

	chooser.setBorder(new EmptyBorder(25,0,0,0));
	chooser.add(sendButton);
	chooser.add(powerDownBox);

	add(chooser);
	
    }

    private void resetAll() {
	powerDownBox.setSelected(false);
	powerDownBox.setEnabled(false);
	sendButton.setEnabled(false);
	for (int i=0; i < cardsView.size(); i++) {
	    ((CardView) cardsView.get(i)).reset();
	}
    }




    public void setCards(ArrayList cards) {	
	resetAll();
	for (int i=0; i < cards.size(); i++) {
	    ((CardView) cardsView.get(i)).setCard((HumanCard)cards.get(i));
	}
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
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        CardArray re = new CardArray();

	f.getContentPane().add(re);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }


    
}






