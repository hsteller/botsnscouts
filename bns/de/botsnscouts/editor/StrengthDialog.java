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
 
package de.botsnscouts.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.*;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.board.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import org.apache.log4j.*;


class StrengthDialog extends JDialog implements ActionListener{

    private Button ok=null;
    private Checkbox[] chkb=new Checkbox[3];
    private CheckboxGroup cgrp=null;

    private BoardEditor par=null;
    private int x=200,y=200;

    public StrengthDialog(BoardEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(4,1,2,2));

	cgrp=new CheckboxGroup();

	for(int i=0;i<3;i++){
	    chkb[i]=new Checkbox(Message.say("BoardEditor","mStaerke")+" "+(i+1),cgrp,false);
	    getContentPane().add(chkb[i]);
	}
	cgrp.setSelectedCheckbox(chkb[0]);
	ok=new Button(Message.say("BoardEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);
	show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 int sel=1;
	 if(chkb[1]==cgrp.getSelectedCheckbox()) sel=2;
	 if(chkb[2]==cgrp.getSelectedCheckbox()) sel=3;
	 par.laserSt=sel;
	 this.dispose();
     }
 }

}

