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
import javax.swing.border.*;
import java.net.*;

/**
 * @author Daniel Holtz
 */

public class Splash{
    JWindow splash;
    JPanel panel;
    JLabel bildLabel,textLabel;
    String labelText;

    public void setText(String s){ 
	textLabel.setText("  "+s);
    }

    public void showSplash(String s){
	splash = new JWindow();
        panel = (JPanel)splash.getContentPane();
	int width = 744;
	int height = 184;
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	int x = (screen.width-width)/2;
	int y = (screen.height-height)/2;
	splash.setBounds(x,y,width,height);
	bildLabel = new JLabel(ImageMan.getIcon("bnslogo.jpg"));
	bildLabel.setBorder( new EtchedBorder(8));
	textLabel = new JLabel("  "+s);
	textLabel.setBorder( new EtchedBorder(8));
	//textLabel.setBackground(Color.black);
	textLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
	panel.add(bildLabel, BorderLayout.CENTER);
	panel.add(textLabel, BorderLayout.SOUTH);	
	splash.pack();
	splash.setVisible(true);
    }    

    public void noSplash(){
	splash.setVisible(false);
    }
}
