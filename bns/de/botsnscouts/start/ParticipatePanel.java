package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;

public class ParticipatePanel extends JPanel implements  ActionListener{
    
    JLabel server;    
    JLabel name;    
    JLabel farbe;    

    JTextField serv;
    JTextField nam;
    JComboBox farb;

    TransparentButton go;
    TransparentButton zurueck;

    Start parent;

    Paint paint;

    Font font;

    public ParticipatePanel(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mTeilnehmen"));
	paint=parent.paint;
	
	String[] farben={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	font=new Font("Sans", Font.BOLD, 24);

	GridLayout lay;
	lay=new GridLayout(4,2);
	lay.setHgap(170);
	lay.setVgap(80);

	setLayout(lay);
	setBorder(new EmptyBorder(50,50,50,50));
	
	server=new JLabel(Message.say("Start","mServer"));
	name=new JLabel(Message.say("Start","mName"));
	farbe=new JLabel(Message.say("Start","mFarbe"));
        serv=new JTextField(Message.say("Start","mServerInh"),JTextField.CENTER);
        nam=new JTextField(KrimsKrams.randomName(),JTextField.CENTER);
	farb=new RoboBox( true );
        go=new TransparentButton(Message.say("Start","mGoButton"));
        zurueck=new TransparentButton(Message.say("Start","mZurueckButton"));

	server.setFont(font);
	name.setFont(font);
	farbe.setFont(font);
        serv.setFont(font);
	nam.setFont(font);
	farb.setFont(font);

	serv.setOpaque(false);
	nam.setOpaque(false);
	
	server.setForeground(Color.lightGray);
	name.setForeground(Color.lightGray);
	farbe.setForeground(Color.lightGray);

        go.addActionListener(this);
        zurueck.addActionListener(this);

        go.setActionCommand("go");
        zurueck.setActionCommand("zurueck");

	add(server);
        add(serv);
        add(name);
        add(nam);
        add(farbe);
        add(farb);
        add(go);
        add(zurueck);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("go")){

	    Thread smth=parent.fassade.amSpielTeilnehmen(serv.getText(),nam.getText(),farb.getSelectedIndex());
	    Global.debug(this,"SpielerMensch gestartet");
	    parent.addKS(smth);
	    parent.hide();
	    parent.dispose();
	    parent.beenden();
	}else if(e.getActionCommand().equals("zurueck")){
	    parent.toMainMenu();
	    parent.show();
	}

    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

}//class StartTeilZusch end
