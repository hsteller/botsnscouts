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

    private int zuVerteilen=0;
    private int repairPoints=0;

    private ArrayList registers;
    private boolean[] locked;

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

	titel = new JLabel();
	fertig=new JButton(Message.say("SpielerMensch","ok"));
	fertig.addActionListener(al);
    }

    
    public void setChoises(ArrayList registers, int repairPoints) {	
	this.repairPoints=repairPoints;

	zuVerteilen = repairPoints;
	ArrayList tmp=new ArrayList(5);
//	resetAll();

	titel.setText(Message.say("SpielerMensch","mregwahl",repairPoints));
	locked=new boolean[registers.size()];
	RegisterView rv;
	boolean lo;
	String act;
	int prio;
	for(int i=0;i<registers.size();i++){
	    rv=(RegisterView)registers.get(i);
	    lo=rv.locked();
	    act=rv.getAction();
	    prio=rv.getPrio();
	    rv=new RegisterView(this);
	    rv.setCard(new HumanCard(prio,act));
	    rv.setLocked(lo);
	    tmp.add(rv);
	    add(rv);
	    rv.setActionCommand(""+i);
	    locked[i]=lo;
	    if (locked[i]) {Global.debug(this,"Das Register "+(i+1)+" ist gelockt");}
	}
	this.registers=tmp;
	add(titel);
	add(fertig);
    }

    public ArrayList getSelection() {	
	int cntr=0;
	boolean[] torepair=new boolean[5];
//	Global.debug(this,"registers: "+registers);
	for(int i=0;i<registers.size();i++){
	    torepair[i]=locked[i]&&(!(((RegisterView)registers.get(i)).locked()));
	    if(torepair[i]){
		cntr++;
	    }
	}
	ArrayList lst=new ArrayList(cntr);
	for(int i=0;i<registers.size();i++){
	    if(torepair[i]){
		lst.add(new Integer(i+1));
	    }
	}
	return lst;
    }

    public void actionPerformed (ActionEvent e) {
	int num=Integer.parseInt(e.getActionCommand());
	RegisterView rv=((RegisterView) e.getSource());
	if(rv.locked()&&(zuVerteilen>0)){
	    rv.setLocked(false);
	    zuVerteilen--;
	    titel.setText(Message.say("SpielerMensch","mregwahl",zuVerteilen));
	}else if((!rv.locked())&&locked[num]){
	    rv.setLocked(true);
	    zuVerteilen++;
	    titel.setText(Message.say("SpielerMensch","mregwahl",zuVerteilen));
	}
    }
    

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        RepairRegisters re = new RepairRegisters();

	ArrayList ra=new ArrayList(5);
	RegisterView rv;
	for(int i=0;i<5;i++){
	    rv=new RegisterView(re);
	    rv.setCard(new HumanCard(100+i,"M2"));
	    ra.add(rv);
	}
	
	((RegisterView)ra.get(2)).setLocked(true);
	((RegisterView)ra.get(0)).setLocked(true);
	((RegisterView)ra.get(4)).setLocked(true);

	re.setChoises(ra,2);
	f.getContentPane().add(re);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }


    
}






