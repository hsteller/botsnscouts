package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import java.net.*;
import java.net.*;
import java.io.*;
import de.botsnscouts.util.*;

public class RobotCard extends JPanel{

    private JLabel name;
    private JPanel cards;

    public RobotCard(Roboter r){
	cards=new JPanel(new GridLayout(5,1));
	setLayout(new GridLayout(1,2));
	Image[] lll=ImageMan.getImages(r.getBotVis());
	name=new JLabel(r.getName(), new ImageIcon(lll[0]),SwingConstants.LEFT);
    }


    public void updateRobot(Roboter r){

    }

}//RobCard end
