package de.botsnscouts.gui;

import  de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

    /**
     * Status
     */
public class RobotStatus extends JComponent {
    int xsize=75, ysize=60;
    JLabel name;
    Image robotImage[];
    Roboter robot;

    public RobotStatus () {
	this(Roboter.getNewInstance("TestRob"));
    }

    public RobotStatus (Roboter r) {
	robot = r;
	name = new JLabel(r.getName());
	name.setSize(75,10);
	add(name);
	
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
        JFrame f = new JFrame();
	f.setSize(100,100);
	f.getContentPane().add(new RobotStatus());
	f.setVisible(true);
    }

}
