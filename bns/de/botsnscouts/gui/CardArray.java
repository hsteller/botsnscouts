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
	powerDownBox.setEnabled(true);
	sendButton.addActionListener(send);
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

    protected void resetAll() {
	powerDownBox.setSelected(false);
	sendButton.setEnabled(false);
	for (int i=0; i < cardsView.size(); i++) {
	    ((CardView) cardsView.get(i)).reset();
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
	}
    }

    protected void activateButton() {
	sendButton.setEnabled(true);
    }

    protected void deActivateButton() {
	sendButton.setEnabled(false);
    }

    protected void setWisenheimer(int predCard) {
	((CardView)cardsView.get(predCard)).setWisenheimer();
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
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






