package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * ask the user for the direction
 * @author Lukasz Pekacki
 */
public class AgainPowerDown extends JPanel {
    public AgainPowerDown() {
	this(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("ActionCommand: "+ae.getActionCommand());
		}
	    }
	     );
    }
    public AgainPowerDown(ActionListener al) {
	BoxLayout b = new BoxLayout(this,BoxLayout.Y_AXIS);
	JLabel titel = new JLabel(Message.say("SpielerMensch","roboreaktwtitle"));
	JLabel unter = new JLabel(Message.say("SpielerMensch","powerdownwieder"));
	JButton wieder=new JButton(Message.say("SpielerMensch","wiederPowerFrage"));
	JButton weiter=new JButton(Message.say("SpielerMensch","weiterspielen"));

	setLayout(b);
	setBorder(new EmptyBorder(0,10,0,0));

	wieder.addActionListener(al);
	wieder.setActionCommand("powerdownagain");
	weiter.addActionListener(al);
	weiter.setActionCommand("continueplaying");

	add(Box.createVerticalStrut(30));
	add(titel);
	add(unter); 
	add(Box.createVerticalStrut(10));
	add(wieder);
	add(Box.createVerticalStrut(10));
	add(weiter);
    }


    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        AgainPowerDown ag = new AgainPowerDown();

	f.getContentPane().add(ag);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }

}






