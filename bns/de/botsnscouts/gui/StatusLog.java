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
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * shows the status messages and action messages on the scrren
 * @author Lukasz Pekacki
 */

public class StatusLog  extends JPanel {
    // Objects
    private JTextArea textArea = new JTextArea();
    private JLabel singleMessage = new JLabel();
    
    public StatusLog() {
	setLayout(new FlowLayout(FlowLayout.LEFT));
	add(singleMessage);
    }

    public void addMessage(String s) {
	singleMessage.setText(s);
    }
	

    private void d(String s){
	Global.debug(this, s);
    }

}



