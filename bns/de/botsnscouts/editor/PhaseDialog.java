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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import de.botsnscouts.util.Message;

class PhaseDialog extends JDialog implements ActionListener{

    private Button ok=null;
    private Checkbox[] chkb=new Checkbox[5];

    private BoardEditor par=null;
    private int x=200,y=300;

    public PhaseDialog(BoardEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(6,1,2,2));

	for(int i=0;i<5;i++){
	    chkb[i]=new Checkbox(Message.say("BoardEditor","mPhase")+" "+(i+1));
	    getContentPane().add(chkb[i]);
	}
	ok=new Button(Message.say("BoardEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);
	setVisible(true);
	//show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 int mult=1;
	 par.phasen=0;
	 for(int i=0;i<5;i++){
	     if(chkb[i].getState())
		 par.phasen+=mult;
	     mult*=2;
	 }
	 if(par.phasen>0)
	     this.dispose();
     }
 }

}