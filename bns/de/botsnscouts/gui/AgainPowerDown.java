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

import javax.swing.plaf.metal.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * ask the user for the direction
 * @author Lukasz Pekacki
 */
public class AgainPowerDown extends TJPanel {
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
	JButton wieder=OptionPane.getButton(Message.say("SpielerMensch","wiederPowerFrage"));
	JButton weiter=OptionPane.getButton(Message.say("SpielerMensch","weiterspielen"));

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
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

        AgainPowerDown ag = new AgainPowerDown();

	f.getContentPane().add(ag);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }

}






