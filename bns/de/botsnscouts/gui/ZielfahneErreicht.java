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
import java.awt.*;
import javax.swing.*;


public class ZielfahneErreicht extends JPanel{
  
    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor2 = new Color(140,255,140);

    public ZielfahneErreicht() {
	this("",false);
    }


    public ZielfahneErreicht(String inhalt, boolean tot) {
	setBackground(backColor);
	setLayout(new GridLayout((inhalt.length()+6),3)); 
	for (int i = 0; i < 9; i++) add(new Label(""));
	for (int i = 0; i < inhalt.length(); i++) {
	    Label l = new Label(inhalt.substring(i,i+1));
	    l.setFont(new Font("Sans", Font.BOLD, 24));
	    // ist der Robi tot, dann schreibe rot
	    if (tot) l.setForeground(Color.red);
	    else l.setForeground(foreColor2);
	    add(new Label(""));
	    add(l);
	    add(new Label(""));
	}
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(180,550);
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
	Frame f = new Frame("Test");
	f.setSize(200,640);
	ZielfahneErreicht zf = new ZielfahneErreicht();
	f.add(zf);
	f.setVisible(true);
    }
}	
