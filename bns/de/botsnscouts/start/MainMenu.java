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
 
package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;

import org.apache.log4j.Category;

import de.botsnscouts.util.*;

public class MainMenu extends JPanel implements  ActionListener{

    static Category CAT = Category.getInstance(MainMenu.class);

    Paint paint;

    TransparentButton gameBut;
    TransparentButton partBut;
    TransparentButton watchBut;
    TransparentButton editBut;
    TransparentButton endBut;
    JLabel logo;

    Start parent;

    public MainMenu(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mStartTitel"));
	paint=parent.paint;

	GridLayout lay=new GridLayout(5,1);
	lay.setHgap(170);
	lay.setVgap(20);
	//lay.setHgap(100);
	//lay.setVgap(10);
	JPanel buttons = new JPanel( lay );
	buttons.setOpaque( false );
	setLayout( new GridBagLayout() );

	logo = new Logo();

	gameBut=new TransparentButton(Message.say("Start","mSpielStarten"));
	partBut=new TransparentButton(Message.say("Start","mTeilnehmen"));
	watchBut=new TransparentButton(Message.say("Start","mZuschauen"));
	editBut=new TransparentButton(Message.say("Start","mEdit"));
	endBut=new TransparentButton(Message.say("Start","mBeenden"));

	gameBut.addActionListener(this);
	partBut.addActionListener(this);
	watchBut.addActionListener(this);
	editBut.addActionListener(this);	
	endBut.addActionListener(this);

	gameBut.setActionCommand("gameBut");
	partBut.setActionCommand("partBut");
	watchBut.setActionCommand("watchBut");
	editBut.setActionCommand("editBut");
	endBut.setActionCommand("endBut");

	buttons.add(gameBut);
	buttons.add(partBut);
	buttons.add(watchBut);
	buttons.add(editBut);
	buttons.add(endBut);

	GridBagConstraints con = new GridBagConstraints();
	buttons.setBorder( new EmptyBorder(50, 20, 50, 20));
	con.gridx = 0;
	con.gridheight = 2;
	con.weightx = 0.1;
	con.weighty = 0.1;
	add( logo, con );
	con.gridheight = GridBagConstraints.REMAINDER;
	con.fill = GridBagConstraints.VERTICAL;
	con.insets = new Insets(50, 20, 50, 20);
	con.anchor = GridBagConstraints.CENTER;
	con.weighty = 0.2;
	con.weightx = 0.0;
	add( buttons, con );
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("endBut")){
	    parent.myclose();
	}else if(e.getActionCommand().equals("gameBut")){
	    new Thread(new Runnable(){
		    public void run(){
			parent.showBusy(Message.say("Start","mLoadGameFieldPanel"));
			if(parent.gameFieldPanel==null){
			    parent.gameFieldPanel=new GameFieldPanel(parent);
			}
			try{Thread.sleep(5000);}catch(InterruptedException ex){}
			CAT.debug("setting gameFieldPanel");
			parent.current=parent.gameFieldPanel;
			parent.setContentPane(parent.current);
			parent.show();
			parent.stopBusy();
		    }}).start();	    
	}else if(e.getActionCommand().equals("partBut")){
	    new Thread(new Runnable(){
		    public void run(){
			parent.showBusy(Message.say("Start","mLoadParticipatePanel"));
			if(parent.partPanel==null){
			    parent.partPanel=new ParticipatePanel(parent);
			}
			parent.current=parent.partPanel;
			parent.setContentPane(parent.current);
			parent.stopBusy();
			parent.show();
		    }}).start();
	}else if(e.getActionCommand().equals("watchBut")){
	    new Thread(new Runnable(){
		    public void run(){
			parent.showBusy(Message.say("Start","mLoadWatchPanel"));
			if(parent.watchPanel==null){
			    parent.watchPanel=new WatchPanel(parent);
			}
			parent.current=parent.watchPanel;
			parent.setContentPane(parent.current);
			parent.stopBusy();
			parent.show();
		    }}).start();
	} else if (e.getActionCommand().equals("editBut")){
	  new de.botsnscouts.editor.BoardEditor();
	}
    }

    public void unrollOverButs(){
	gameBut.getModel().setRollover(false);
	partBut.getModel().setRollover(false);
	watchBut.getModel().setRollover(false);
	endBut.getModel().setRollover(false);
	editBut.getModel().setRollover(false);
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }


}//class MainMenu end

