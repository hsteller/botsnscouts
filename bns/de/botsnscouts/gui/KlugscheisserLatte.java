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

import  de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * KlugscheisserLatte, die im oben rechts in der Infoleiste dargestellt ist
     */
public class KlugscheisserLatte extends JToggleButton {

    int xsize=60, ysize=60;
    Image klugSchlaf[];
    Image klugWach[];

    public KlugscheisserLatte(ActionListener al) {
	// Referenz holen
	klugSchlaf=ImageMan.getImages(ImageMan.KSCHLAF);
	klugWach=ImageMan.getImages(ImageMan.KWACH);
	setContentAreaFilled(false);
	setBorder(null);
	setToolTipText(Message.say("KlugscheisserLatte","mtooltip"));
	setIcon(new ImageIcon(klugSchlaf[0]));
	setSelectedIcon(new ImageIcon(klugWach[0]));
	addActionListener(al);
	
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    protected void reset() {
	setSelected(false);
    }

    protected boolean selected() {
	return isSelected();
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JFrame f = new JFrame();
	f.setSize(100,100);
	f.getContentPane().add(new KlugscheisserLatte(null));
	f.setVisible(true);
    }

}
