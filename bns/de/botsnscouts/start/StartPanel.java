package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.util.*;
import de.botsnscouts.util.*;
import de.botsnscouts.gui.*;

public class StartPanel extends JPanel{
    Paint paint;
    Start parent;
    Thread thread;

    JLabel angem;
    PlayersPanel playersPanel;
    JPanel okPanel;
    JPanel autobotPanel;
    JPanel localPanel;
    StSpListener listen;

    JSlider intel;
    JTextField name;
    JComboBox color;

    public StartPanel(Start par){
	parent=par;
	paint=parent.paint;
	Font font=new Font("Sans", Font.BOLD, 24);

	BorderLayout lay=new BorderLayout();

	setLayout(lay);
	setBorder(new EmptyBorder(50,50,50,50));
	setOpaque( false );

	angem=new JLabel(Message.say("Start","mAngem"));
	playersPanel=new PlayersPanel(parent);
	okPanel=getOkPanel();
        autobotPanel=getABPanel();
        localPanel=getLocalPanel();
	listen=new StSpListener(playersPanel);
	listen.start();

	angem.setFont(font);
	JPanel p = new JPanel(new BorderLayout());
	p.setOpaque( false );
	p.add( angem, BorderLayout.NORTH );
	p.add( playersPanel, BorderLayout.CENTER );
	add(BorderLayout.WEST,p);
	add(BorderLayout.SOUTH,okPanel);
	JPanel panel=new TJPanel();

	panel.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	gc.anchor = GridBagConstraints.NORTH;
	gc.fill = GridBagConstraints.BOTH;
	gc.gridx = 0;
	gc.gridy = GridBagConstraints.RELATIVE;
	gc.insets = new Insets(30, 30, 30, 30);
	
	panel.add(autobotPanel, gc);
	panel.add(localPanel, gc);
	add(BorderLayout.EAST,panel);
    }

    JPanel getLocalPanel(){
	JPanel panel = new JPanel();

	panel.setOpaque(false);
	String[] farben={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};

	Font font=new Font("Sans", Font.BOLD, 24);
	panel.setBorder(new EmptyBorder(50,50,50,50));

	panel.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	JLabel label = new TJLabel(Message.say("Start", "mLokaleMS"));
	label.setFont(font);
	gc.gridwidth = 3;
	gc.insets = new Insets(0,0,20,0);
	gc.ipadx = 5;
	gc.ipady = 5;
	panel.add( label, gc );

	// Name-Label
	label = new TJLabel(Message.say("Start","mName"));
	gc.gridx = 0;
	gc.gridy = 1;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.EAST;
	panel.add( label, gc);

	// Name-Textfield
	name = new JTextField(KrimsKrams.randomName(),JTextField.CENTER);
	name.setOpaque( false );
	gc.gridwidth = 2;
	gc.gridx = 1;
	gc.anchor = GridBagConstraints.WEST;
	gc.fill   = GridBagConstraints.BOTH;
	panel.add( name, gc );

	// Farbe-Label
	label = new TJLabel(Message.say("Start","mFarbe"));
	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.gridy = 2;
	gc.anchor = GridBagConstraints.EAST;
	panel.add( label, gc);

	gc.gridx = 1;
	gc.gridwidth = 2;
	gc.anchor = GridBagConstraints.WEST;
	color = new RoboBox( true ); // mit Egal-Eintrag

	gc.fill = GridBagConstraints.NONE;
	gc.anchor = GridBagConstraints.CENTER;
	panel.add( color, gc );

	// Go Button

        JButton ok=new TransparentButton(Message.say("Start","mGoButton"));
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Thread player=parent.fassade.amSpielTeilnehmenNoSplash(name.getText(),color.getSelectedIndex());
		    parent.addKS(player);
		    name.setText(KrimsKrams.randomName());
		}});
	
	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.gridy++;
	gc.anchor = GridBagConstraints.WEST;
	gc.fill =  GridBagConstraints.CENTER;
	gc.insets = new Insets(0,0,0,0);
	panel.add( ok, gc );
	panel.setBorder( new CompoundBorder( new EtchedBorder(8),
				       new EmptyBorder(10, 10, 10, 10)) );

	return panel;
    }

    JPanel getABPanel(){
	JPanel panel = new JPanel();
	panel.setOpaque(false);
	Font font=new Font("Sans", Font.BOLD, 24);

	panel.setLayout( new GridBagLayout() );
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
	panel.add(label, gc);

	JLabel ks=new TJLabel(Message.say("Start","mIntel"));
	gc.gridy++;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.insets = noInsets;
	panel.add( ks, gc);

	JLabel lb = new TJLabel(Message.say("Start", "mDumm"), JLabel.LEFT);
	gc.insets = insets;
	gc.gridx = GridBagConstraints.RELATIVE;     
	gc.gridy++;
	gc.gridwidth = 1; gc.gridheight = 1;
	gc.fill =  GridBagConstraints.NONE;
	gc.anchor = GridBagConstraints.WEST;
	panel.add( lb, gc );

	// Slider
        intel=new JSlider();
	intel.setOpaque(false);
	gc.gridwidth = 2;
	gc.gridheight = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.fill =  GridBagConstraints.BOTH;
	panel.add(intel,gc);

	// Label schlau
	gc.anchor = GridBagConstraints.EAST;
	lb = new TJLabel(Message.say("Start", "mSchlau"), JLabel.RIGHT);
	gc.fill =  GridBagConstraints.HORIZONTAL;
	panel.add( lb, gc );

        JButton startAB=new TransparentButton(Message.say("Start","mKSStarten"));
	startAB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    parent.addKS(parent.fassade.kuenstlicheSpielerStarten(intel.getValue()));
		}});

	gc.gridy++;
	gc.gridx = 0;
	gc.gridwidth = 1;
	gc.anchor = GridBagConstraints.CENTER;
	gc.fill =  GridBagConstraints.CENTER;
	gc.insets = new Insets(0,0,0,0);
	panel.add(startAB, gc);
	panel.setBorder( new CompoundBorder( new EtchedBorder(8),
					     new EmptyBorder(10, 10, 10, 10)) );
	return panel;
    }
    JPanel getOkPanel(){
	JPanel panel=new JPanel();
	GridLayout lay=new GridLayout(1,2);
	lay.setHgap(50);
	lay.setVgap(50);
	panel.setBorder(new EmptyBorder(20,20,20,20));

	panel.setLayout(lay);
	panel.setOpaque(false);

	JButton okBut=new TransparentButton(Message.say("Start","mLos"));
	JButton backBut=new TransparentButton(Message.say("Start","mAbbrechen"));

	okBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(playersPanel.names.size()!=8){
			parent.fassade.spielGehtLos();
			parent.hide();
			parent.beenden();
		    }
		}});
	backBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    parent.fassade.killServer();
		    parent.resetWaiter();  
		    listen.closeSock();
		    parent.current=parent.gameFieldPanel;
		    parent.startPanel=null;
		    parent.setContentPane(parent.current);
		    parent.show();
		}});

	panel.add(okBut);
	panel.add(backBut);
	return panel;
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    public void setThreadToWait(Thread th){
	thread=th;
    }

}//class StartStart end
