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
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
/**
 * ask the user for the direction
 * @author Lukasz Pekacki
 */
public class UserInfo extends JPanel {
    
    private ArrayList labels = new ArrayList(10);
    private static final int textWidth=25;
    

    public UserInfo() {
	BoxLayout b = new BoxLayout(this,BoxLayout.Y_AXIS);
	JButton weiter=new JButton(Message.say("SpielerMensch","weiterspielen"));
	for (int i =0; i < 10; i++) {
	    JLabel l = new JLabel("");
	    labels.add(i,l);
	    add(l);
	    add(Box.createVerticalStrut(5));
	}
	setLayout(b);
	setBorder(new EmptyBorder(10,10,0,0));

    }

    public void setInfo(String s) {
	StringTokenizer st = new StringTokenizer(s);
	StringBuffer subString = new StringBuffer(textWidth);
	String token ="";
	int length = 0;
	int index = 0;
	while (st.hasMoreTokens()) {
	    token = st.nextToken();
	    if  ( (subString.toString().length() + token.length()) > textWidth  ) {
		( (JLabel) labels.get(index) ).setText(subString.toString());
		subString.delete(0,subString.length());
		index++;
	    }
	    subString.append(token + " ");
	    
	}
	( (JLabel) labels.get(index) ).setText(subString.toString());
	index++;

	for (int i = index; i < labels.size(); i++) {
	( (JLabel) labels.get(index) ).setText("");
	}

    }
    
    

	public static void main (String args[]) {
	    Message.setLanguage("deutsch");
	    JWindow f = new JWindow();
	    MetalLookAndFeel.setCurrentTheme( new GreenTheme() );
	    
	    UserInfo ui = new UserInfo();
	    
	    f.getContentPane().add(ui);
	    f.setLocation(100,100);
	    f.setSize(200,400);
	    f.setVisible(true);
	    ui.setInfo("Das ist ein Test.");
	    ui.setInfo("Und jetzt der Proof, ob es auch ersetzen geht.");
	}
    
    private void d(String s) {
	System.err.println(s);
    }

}






