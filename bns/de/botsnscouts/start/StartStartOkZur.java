package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;
//import de.botsnscouts.board.*;

public class StartStartOkZur extends JPanel implements  ActionListener, MouseListener{

    JButton ok;
    JButton zurueck;

    Start parent;

    public StartStartOkZur(Start par){
	parent=par;

	GridLayout lay=new GridLayout(1,2);
	lay.setHgap(50);
	lay.setVgap(50);
	setBorder(new EmptyBorder(20,20,20,20));

	setLayout(lay);
	setOpaque(false);

	ok=new TransparentButton(Message.say("Start","mLos"));	
	zurueck=new TransparentButton(Message.say("Start","mAbbrechen"));

	ok.addActionListener(this);
	zurueck.addActionListener(this);

	ok.setActionCommand("ok");
	zurueck.setActionCommand("zurueck");

	add(ok);
	add(zurueck);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("zurueck")){
	    //beende den StartServer und somit den Server
	    Global.debug(this,"kille den server!");
	    parent.fassade.killServer(8077);
	    Global.debug(this,"server gekilled!");
	    //resete anzeige der angemeldeten roboter
	    parent.resetWaiter();  
	    parent.startPanel.anmeldung.reset();
	    parent.startPanel.listen.closeSock();
	    //schalte auf Spielfeld um
	    parent.current=parent.startSpielfeld;
	    parent.startPanel=null;
	    parent.setContentPane(parent.current);
	    parent.show();
	    Global.debug(this,"zum Spielfeld zurückgekehrt!");
	}else if(e.getActionCommand().equals("ok")&&(parent.startPanel.anmeldung.names.size()!=8)){
	    parent.fassade.spielGehtLos();
	    Global.debug(this,"ok, Spiel ging los");
	    parent.hide();
	    parent.beenden();
//	    parent.nullen();
	    /*if(parent.startPanel.thread!=null){
		try{
		    parent.startPanel.thread.join();
		}catch(InterruptedException ex){
		    System.err.println(ex);
		}
	    }
	    parent.show();*/
	}
    }

    public void mouseEntered(MouseEvent e){
	
    }

    public void mouseExited(MouseEvent e){

    }


    public void mouseClicked(MouseEvent e){

    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

}
