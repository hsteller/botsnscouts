package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;

public class StartSpielfeldOkZur extends JPanel implements  ActionListener{

    JButton ok;
    JButton zurueck;

    Start parent;

    public StartSpielfeldOkZur(Start par){
	parent=par;

	GridLayout lay=new GridLayout(1,2);
	lay.setHgap(50);
	lay.setVgap(50);
	setBorder(new EmptyBorder(20,20,20,20));

	setLayout(lay);
	setOpaque(false);

	ok=new TransparentButton(Message.say("Start","mSpielStarten"));	
	zurueck=new TransparentButton(Message.say("Start","mZurueckButton"));

	ok.addActionListener(this);
	zurueck.addActionListener(this);

	ok.setActionCommand("ok");
	zurueck.setActionCommand("zurueck");

	add(ok);
	add(zurueck);
    }

    public void actionPerformed(ActionEvent e){
	boolean verbose=Global.verbose;
	Global.verbose=true;
	Global.debug(this,"Aktion!");
//System.err.println("Aktion");//////////////////////////////////////
	if(e.getActionCommand().equals("zurueck")){
	    parent.current=parent.startAnfang;
	    parent.setContentPane(parent.current);
	    parent.startAnfang.starten.getModel().setRollover(false);
	    parent.startAnfang.teilnehmen.getModel().setRollover(false);
	    parent.startAnfang.zuschauen.getModel().setRollover(false);
	    parent.startAnfang.beenden.getModel().setRollover(false);
	    parent.show();
	}else if(e.getActionCommand().equals("ok")){
	    okClicked();
	}//actionPerformed
	Global.verbose=verbose;	
    }
    
    private void okClicked(){
	Global.debug(this,"OK clicked!");
	try{
	    if(parent.startStart==null){
		parent.startStart=new StartStart(parent);
		Global.debug(this,"neuen StartStart geStartStartet!!");
	    }
	    Global.debug(this,"starte spiel!");
	    parent.fassade.startSpiel();//starte Spiel
	    Global.debug(this,"spielGestartet!");
	    parent.addServer();
	    if(parent.startSpielfeld.buttons.mitspielen.getSelectedObjects()!=null){//starte einen SpielerMensch
		Thread smth=parent.fassade.amSpielTeilnehmenNoSplash(parent.startSpielfeld.buttons.nam.getText(),parent.startSpielfeld.buttons.farben.getSelectedIndex()); 
		parent.addKS(smth);
		Global.debug(this,"menschlichen spieler gestartet");
	    }else{//starte einen AusgabeFrame
		parent.addKS( parent.fassade.einemSpielZuschauenNoSplash());
	    }
	    parent.current=parent.startStart;
	    parent.setContentPane(parent.current);
	    parent.show();
	}catch(OneFlagException ex){
	    JOptionPane.showMessageDialog(this,Message.say("Start","mZweiFlaggen"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);
	    System.err.println(ex+": Must be at least 2 flags!");
	}catch(NichtZusSpfException exc){
	    JOptionPane.showMessageDialog(this,Message.say("Start","mNichtZus"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);
	    System.err.println(exc);
	}
	
    }//okclicked

}
