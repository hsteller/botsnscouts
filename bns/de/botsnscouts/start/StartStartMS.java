package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;

public class StartStartMS extends JPanel implements  ActionListener{

    JLabel name;    
    JLabel farbe;    

    JTextField nam;
    JComboBox farb;

    TransparentButton ok;

    Start parent;
    Font font;

    public StartStartMS(Start par){
	parent=par;
	setOpaque(false);
	String[] farben={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};

	font=new Font("Sans", Font.BOLD, 24);
	setBorder(new EmptyBorder(50,50,50,50));

	JPanel p = this; //new TJPanel();

	p.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();

	JLabel label = new TJLabel(Message.say("Start", "mLokaleMS"));
	label.setFont(font);
	gc.gridwidth = 3;
	gc.insets = new Insets(0,0,20,0);
	gc.ipadx = 5;
	gc.ipady = 5;
	p.add( label, gc );

	// Name-Label
	label = new TJLabel(Message.say("Start","mName"));
	gc.gridx = 0;
	gc.gridy = 1;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.EAST;
	p.add( label, gc);

	// Name-Textfield
	nam = new JTextField(KrimsKrams.randomName(),JTextField.CENTER);
	nam.setOpaque( false );
	gc.gridwidth = 2;
	gc.gridx = 1;
	gc.anchor = GridBagConstraints.WEST;
	gc.fill   = GridBagConstraints.BOTH;
	p.add( nam, gc );

	// Farbe-Label
	label = new TJLabel(Message.say("Start","mFarbe"));
	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.gridy = 2;
	gc.anchor = GridBagConstraints.EAST;
	p.add( label, gc);

	// Farbe-Combobox
	/*
	farb=new JComboBox(farben);
	farb.setLightWeightPopupEnabled(false);
	farb.setRenderer( new MyCellRenderer() );
	farb.setOpaque(false);
	*/
	gc.gridx = 1;
	gc.gridwidth = 2;
	gc.anchor = GridBagConstraints.WEST;
	farb = new RoboBox( true ); // mit Egal-Eintrag

	gc.fill = GridBagConstraints.NONE;
	gc.anchor = GridBagConstraints.CENTER;
	p.add( farb, gc );

	// Go Button

        JButton ok=new TransparentButton(Message.say("Start","mGoButton"));
        ok.setActionCommand("ok");
        ok.addActionListener(this);

	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.gridy++;
	gc.anchor = GridBagConstraints.WEST;
	gc.fill =  GridBagConstraints.CENTER;
	gc.insets = new Insets(0,0,0,0);
	p.add( ok, gc );
	p.setBorder( new CompoundBorder( new EtchedBorder(8),
				       new EmptyBorder(10, 10, 10, 10)) );


	//add(p);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("ok")){
	    Thread spMensch=parent.fassade.amSpielTeilnehmenNoSplash(nam.getText(),farb.getSelectedIndex());
	    parent.addKS(spMensch);
	    nam.setText(KrimsKrams.randomName());
	    //	    parent.beenden();
	}
    }


}//class StartTeilZusch end
