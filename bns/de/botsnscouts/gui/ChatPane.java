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

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.*;

import org.apache.log4j.Category;

import de.botsnscouts.util.*;

/** Ein ChatPanel, das unterhalb des Spielfeldes angezeigt werden soll.
 *  @author Dirk
 */

public class ChatPane extends JPanel {
  static Category CAT = Category.getInstance(ChatPane.class);



    private class ChatActionListener implements ActionListener{
	public void actionPerformed(ActionEvent e){
	    if (nachricht.getText().equals(""))
		return;
	    parent.sendChat(filter(nachricht.getText()));
	    nachricht.setText("");
	}
	/** filters out disallowed chars */
	private String filter(String in){
	    StringBuffer out=new StringBuffer();
	    StringTokenizer st=new StringTokenizer(in, "$,");
	    while (st.hasMoreTokens())
		out.append(st.nextToken());
	    return out.toString();
	}
    }

    private JTextField nachricht;
    HumanPlayer parent;


    public ChatPane(HumanPlayer parent){
	super(new FlowLayout());

	add(new JLabel(Message.say("ChatPane","prompt")));
	nachricht=new JTextField();
	nachricht.setPreferredSize(new Dimension(500,20));
	add(nachricht);
	ChatActionListener cal=new ChatActionListener();
	nachricht.addActionListener(cal);
	JButton senden=new JButton(Message.say("ChatPane","button"));
	senden.addActionListener(cal);
	add(senden);
	this.parent=parent;
	setPreferredSize(new Dimension(600,32));
    }

    public static void main(String[] args){
	JFrame f=new JFrame();
	f.getContentPane().add(new ChatPane(null));
	f.setVisible(true);
    }




}
