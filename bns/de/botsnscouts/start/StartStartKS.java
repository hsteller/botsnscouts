package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;

public class StartStartKS extends JPanel implements  ActionListener, MouseListener{

    Start parent;

    JLabel ks;
    JSlider intel;
    //    JButton starten;

    public StartStartKS(Start par){
	parent=par;
	
	setOpaque(false);
	Font font=new Font("Sans", Font.BOLD, 24);


	JPanel p = this; //new JPanel();
	p.setOpaque( false );
	p.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	Insets noInsets = new Insets(0,0,0,0);
	Insets insets = new Insets(0,0,20,0);
	

	JLabel label = new TJLabel(Message.say("Start", "mStartKS"));
	gc.gridwidth = GridBagConstraints.REMAINDER;
	gc.gridy = 1;
	gc.insets = insets;
	gc.ipadx = 5;
	gc.ipady = 5;
	label.setFont(font);
	p.add(label, gc);

	JLabel ks=new TJLabel(Message.say("Start","mIntel"));
	gc.gridy++;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.insets = noInsets;
	p.add( ks, gc);


	JLabel lb = new TJLabel(Message.say("Start", "mDumm"), JLabel.LEFT);
	gc.insets = insets;
	gc.gridx = GridBagConstraints.RELATIVE;     
	gc.gridy++;
	gc.gridwidth = 1; gc.gridheight = 1;
	gc.fill =  GridBagConstraints.NONE;
	gc.anchor = GridBagConstraints.WEST;
	p.add( lb, gc );

	// Slider
        intel=new JSlider();
	intel.setOpaque(false);
	gc.gridwidth = 2;
	gc.gridheight = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.fill =  GridBagConstraints.BOTH;
	p.add(intel,gc);

	// Label schlau
	gc.anchor = GridBagConstraints.EAST;
	lb = new TJLabel(Message.say("Start", "mSchlau"), JLabel.RIGHT);
	gc.fill =  GridBagConstraints.HORIZONTAL;
	p.add( lb, gc );

        JButton starten=new TransparentButton(Message.say("Start","mKSStarten"));

	starten.setActionCommand("ksstart");
	starten.addActionListener(this);

	gc.gridy++;
	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.fill =  GridBagConstraints.CENTER;
	gc.insets = new Insets(0,0,0,0);
	p.add(starten, gc);
	p.setBorder( new CompoundBorder( new EtchedBorder(8),
				       new EmptyBorder(10, 10, 10, 10)) );
	
	//add(p);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("ksstart")){
	    Global.debug(this,"Starte einen neuen KS ("+intel.getValue()+")");
	    parent.addKS(parent.fassade.kuenstlicheSpielerStarten(intel.getValue()));
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
