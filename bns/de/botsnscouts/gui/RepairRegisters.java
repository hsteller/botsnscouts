package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * ask the user for the register reparation
 * @author Lukasz Pekacki
 */
public class RepairRegisters extends JPanel implements ActionListener{

    private JButton fertig;
    private JLabel titel;

    private JCheckBox cb1 = new JCheckBox(Message.say("SpielerMensch","mcregister","1"),false);
    private JCheckBox cb2 = new JCheckBox(Message.say("SpielerMensch","mcregister","2"),false);
    private JCheckBox cb3 = new JCheckBox(Message.say("SpielerMensch","mcregister","3"),false);
    private JCheckBox cb4 = new JCheckBox(Message.say("SpielerMensch","mcregister","4"),false);
    private JCheckBox cb5 = new JCheckBox(Message.say("SpielerMensch","mcregister","5"),false);

    private int zuVerteilen=0;
    private int repairPoints=0;

    private ArrayList registers;

    public RepairRegisters() {
	this(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Send.");
		}
	    });
	     
    }

    public RepairRegisters(ActionListener al) {
	setBorder(new EmptyBorder(10,10,10,10));
	setLayout(new GridLayout(9,1));
	cb1.addActionListener(this);
	cb2.addActionListener(this);
	cb3.addActionListener(this);
	cb4.addActionListener(this);
	cb5.addActionListener(this);
	cb1.setActionCommand("0");
	cb1.setActionCommand("1");
	cb1.setActionCommand("2");
	cb1.setActionCommand("3");
	cb1.setActionCommand("4");
	add(cb1);
	add(cb2);
	add(cb3);
	add(cb4);
	add(cb5);

	add(new JLabel(""));
	titel = new JLabel(Message.say("SpielerMensch","mregwahl",repairPoints));
	add(titel);

	fertig=new JButton(Message.say("SpielerMensch","ok"));

	fertig.addActionListener(al);
	add(fertig);
    }

    private void resetAll() {
	cb1.setSelected(false);
	cb2.setSelected(false);
	cb3.setSelected(false);
	cb4.setSelected(false);
	cb5.setSelected(false);

	cb1.setEnabled(false);
	cb2.setEnabled(false);
	cb3.setEnabled(false);
	cb4.setEnabled(false);
	cb5.setEnabled(false);
    }

    
    private void setChoises(ArrayList registers, int repairPoints) {	
	this.repairPoints=repairPoints;
	this.registers=registers;
	zuVerteilen = repairPoints;

	resetAll();

	titel.setText(Message.say("SpielerMensch","mregwahl",repairPoints));

	if(((HumanCard) registers.get(0)).getState() == HumanCard.LOCKED) {
	    cb1.setEnabled(true);
	}
	if(((HumanCard) registers.get(1)).getState() == HumanCard.LOCKED){
	    cb2.setEnabled(true);
	}
	if(((HumanCard) registers.get(2)).getState() == HumanCard.LOCKED){
	    cb3.setEnabled(true);
	}
	if(((HumanCard) registers.get(3)).getState() == HumanCard.LOCKED){
	    cb4.setEnabled(true);
	}
	if(((HumanCard) registers.get(4)).getState() == HumanCard.LOCKED){
	    cb5.setEnabled(true);
	}
    }

    public ArrayList getSelection() {
	while (zuVerteilen > 0) {
	    for (int i=0; ((i < registers.size())&&(zuVerteilen >0)); i++) {
		if(((HumanCard) registers.get(i)).getState() == HumanCard.LOCKED){
		    ((HumanCard) registers.get(i)).setState(HumanCard.FREE);
		    zuVerteilen--;
		}
	    }
	}
	return registers;
    }


    public void actionPerformed (ActionEvent e) {
	if ( ((JCheckBox) e.getSource()).getModel().isSelected()) { 
	    if (zuVerteilen > 0) {
		((HumanCard) registers.get(
					   Integer.parseInt(e.getActionCommand()))
		 ).setState(HumanCard.FREE);
		zuVerteilen--; 
	    }
	    else { 
		((JCheckBox) e.getSource()).getModel().setSelected(false);
		((HumanCard) registers.get(
					   Integer.parseInt(e.getActionCommand()))
		 ).setState(HumanCard.LOCKED);
	    }
	}
	else if (zuVerteilen < repairPoints) {
	    zuVerteilen++;
	}
    }
    

    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        RepairRegisters re = new RepairRegisters();

	f.getContentPane().add(re);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }


    
}






