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

import de.botsnscouts.util.*;
import de.botsnscouts.widgets.TJPanel;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.TJLabel;

import javax.swing.plaf.metal.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.log4j.*;
/**
 * ask the user for the register reparation
 * @author Lukasz Pekacki
 */
public class RepairRegisters extends TJPanel implements ActionListener{

    private JButton done;
    private JLabel title;

    private int zuVerteilen=0;
    private int repairPoints=0;

    private ArrayList registers;
    private boolean[] locked;

    private Box left = new Box(BoxLayout.Y_AXIS);

     private RepairRegisters() {
	this(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		  System.err.println("Send.");
		}
	    });

    }

    public RepairRegisters(ActionListener al) {
	setBorder(new EmptyBorder(10,10,10,10));
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	title = new TJLabel();
	done=OptionPane.getTransparentButton(Message.say("SpielerMensch","ok"), 14);
	done.addActionListener(al);

	Box right = new Box(BoxLayout.Y_AXIS);
	Box buttonBox = new Box(BoxLayout.X_AXIS);
	Box main = new Box(BoxLayout.X_AXIS);
	main.add(left);
	main.add(right);
	buttonBox.add(Box.createHorizontalGlue());
	buttonBox.add(done);
	right.add(buttonBox);
	right.add(Box.createVerticalGlue());
	add(title);
	add(main);
    }


    public void setChoises(ArrayList registers, int repairPoints) {
	this.repairPoints=repairPoints;

	zuVerteilen = repairPoints;
	ArrayList tmp=new ArrayList(5);
//	resetAll();

	title.setText(Message.say("SpielerMensch","mregwahl",repairPoints));
	locked=new boolean[registers.size()];
	RegisterView rv;
	boolean lo;
	String act;
	int prio;
	for(int i=0;i<registers.size();i++){
	    rv=(RegisterView)registers.get(i);
	    lo=rv.locked();
	    act=rv.getCardAction();
	    prio=rv.getPrio();
	    rv=new RegisterView(this);
	    rv.setCard(new HumanCard(prio,act));
	    rv.setLocked(lo);
	    tmp.add(rv);
	    left.add(rv);
	    rv.setActionCommand(""+i);
	    locked[i]=lo;
	    if (locked[i]) {Global.debug(this,"Das Register "+(i+1)+" ist gelockt");}
	}
	this.registers=tmp;
    }

    public ArrayList getSelection() {
	int cntr=0;
	boolean[] torepair=new boolean[5];
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
	    title.setText(Message.say("SpielerMensch","mregwahl",zuVerteilen));
	}else if((!rv.locked())&&locked[num]){
	    rv.setLocked(true);
	    zuVerteilen++;
	    title.setText(Message.say("SpielerMensch","mregwahl",zuVerteilen));
	}
      }


    public static void main (String args[]) {
	BasicConfigurator.configure();
	Message.setLanguage("deutsch");
        JFrame f = new JFrame("testing RepairRegisters");
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






