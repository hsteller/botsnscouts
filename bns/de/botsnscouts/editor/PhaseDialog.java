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
	show();
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