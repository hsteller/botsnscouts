package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * Status of a robot
     */
public class RobotStatus extends JPanel {
    int xsize=75, ysize=60;
    JLabel name;
    Image robotImage[];
    Image flagge = CursorMan.getImages(CursorMan.CURSOR)[0];
    Image botcenterImage = CursorMan.getImages(CursorMan.BOTCENTER)[0];
    Image[] robotImages = CursorMan.getImages(CursorMan.STATUSROBOTS);
    Roboter robot;

    public RobotStatus (int i) {
	this(Roboter.getNewInstance("TestRob"),i);
    }

    public RobotStatus (Roboter r, int i) {
	robot = r;
	setBackground(new Color(64,128,64));
	setLayout(new BorderLayout());
	setBorder( new EtchedBorder(4));
	name = new JLabel(r.getName());
	System.out.println(name.getFont());
	Font nameFont = new Font("Dialog",Font.BOLD,10);
	name.setFont(nameFont);
	name.setSize(75,10);
	add(name,BorderLayout.NORTH);
	JPanel p = new JPanel();
	p.setLayout(new GridLayout(2,2,2,2));
	Font labelFont = new Font("Dialog",Font.PLAIN,9);
	JLabel flagLabel = new JLabel("1",new ImageIcon(flagge),JLabel.LEFT);
	flagLabel.setFont(labelFont);
	p.add(flagLabel);
	JLabel lifeLabel = new JLabel("3",new ImageIcon(robotImages[i]),JLabel.LEFT);
	lifeLabel.setFont(labelFont);
	p.add(lifeLabel);

	flagLabel = new JLabel("1",new ImageIcon(flagge),JLabel.LEFT);
	flagLabel.setFont(labelFont);
	p.add(flagLabel);
	lifeLabel = new JLabel(new ImageIcon(botcenterImage),JLabel.LEFT);
	p.add(lifeLabel);



	add(p,BorderLayout.CENTER);
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
        JWindow f = new JWindow();
	JPanel pa = new JPanel(new GridLayout(1,4));
	for (int i=0; i < 4; i++) pa.add(new RobotStatus(i));
					 
	f.getContentPane().add(pa);
	f.pack();
	f.setVisible(true);
    }


}
