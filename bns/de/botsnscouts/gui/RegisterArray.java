package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * where the registers are displayed in a column
 * @author Lukasz Pekacki
 */
public class RegisterArray extends JPanel {

    private ArrayList registerView = new ArrayList(5);
    private int xsize=70, ysize=550;

    public RegisterArray() {
	this(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Register klicked.");
		}
	    });
	     
    }

    public RegisterArray(ActionListener register) {
	JPanel chooser = new JPanel();

	setLayout(new GridLayout(5,1));

	for (int i=0; i<5; i++) {
	    RegisterView r = new RegisterView(register);
	    registerView.add((i),r);
	    add(r);
	}

    }

    private void resetAll() {
	for (int i=0; i < registerView.size(); i++) {
	    ((RegisterView) registerView.get(i)).reset();
	}
    }

    public void addCard(HumanCard hc) {	
	//
	}
    


    public boolean allOcupied() {
	int ocupied=0;
	for (int i=0; i < registerView.size(); i++) {
	    if (((CardView) registerView.get(i)).getCard() != null) {
		ocupied++;
	    }
	}
	return (ocupied == 5);
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

        RegisterArray re = new RegisterArray();

	f.getContentPane().add(re);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }


    
}






