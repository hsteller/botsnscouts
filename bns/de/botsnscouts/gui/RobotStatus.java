package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import javax.swing.plaf.metal.*;
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
    int xsize=75, ysize=70;
    JLabel name;
    JLabel lifes;
    JLabel flag;
    JLabel damage;
    JLabel track;
    JPanel roboInfo;
    Image robotImage[];
    Image flagge = CursorMan.getImages(CursorMan.CURSOR)[0];
    ImageIcon botcenterImage = ImageMan.getPNGImageIcon(ImageMan.ROBOCENTER);
    Image damageImage = CursorMan.getImages(CursorMan.DAMAGE)[0];
    Image[] robotImages = CursorMan.getImages(CursorMan.STATUSROBOTS);
    Roboter robot;

    public RobotStatus () {
	this(Roboter.getNewInstance("TestRob"),null);
    }

    public RobotStatus (Roboter r, MouseListener botCenter) {
	robot = r;
	name = new JLabel(r.getName());
	flag = new JLabel(""+r.getNaechsteFlagge(),new ImageIcon(flagge),JLabel.LEFT);
	lifes = new JLabel(""+r.getLeben(),new ImageIcon(robotImages[r.getBotVis()]),JLabel.RIGHT);
	d("Visualisierung :"+r.getBotVis()+ " an "+r.getName()+" zugeteilt.");
	damage = new JLabel(""+r.getSchaden(),new ImageIcon(damageImage),JLabel.LEFT);
	track = new JLabel("F",botcenterImage,JLabel.RIGHT);
	Font labelFont = new Font("Dialog",Font.PLAIN,8);
	Font nameFont = new Font("Dialog",Font.BOLD,10);
	roboInfo = new JPanel();

	setLayout(new BorderLayout());
	setBorder( new EtchedBorder(4));

	roboInfo.setLayout(new GridLayout(2,2,1,1));

	name.setFont(nameFont);
	flag.setFont(labelFont);
	lifes.setFont(labelFont);
	damage.setFont(labelFont);
	track.setFont(labelFont);

	flag.setVerticalTextPosition(SwingConstants.BOTTOM);
	lifes.setVerticalTextPosition(SwingConstants.BOTTOM);
	damage.setVerticalTextPosition(SwingConstants.BOTTOM);
	track.setVerticalTextPosition(SwingConstants.BOTTOM);

	track.setName(r.getName());

	track.addMouseListener(botCenter);

	roboInfo.add(flag);
	roboInfo.add(lifes);
	roboInfo.add(damage);
	roboInfo.add(track);

	add(name,BorderLayout.NORTH);
	add(roboInfo,BorderLayout.CENTER);
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    public void updateRobot(Roboter r) {
    if (r.getLeben() != Integer.parseInt(lifes.getText())) {
	lifes.setText(""+r.getLeben());
    }
    if (r.getNaechsteFlagge() != Integer.parseInt(flag.getText())) {
	flag.setText(""+r.getNaechsteFlagge());
    }
    if (r.getSchaden() != Integer.parseInt(damage.getText())) {
	damage.setText(""+r.getSchaden());
    }

    }

    public void setWinnerNumber (int ranking) {
    Image winner = CursorMan.getImages(CursorMan.WINNER)[(ranking-1)];
    remove(roboInfo);
    add (new JLabel(new ImageIcon(winner)),BorderLayout.CENTER);
    validate();
    }


    private void d(String s){
	Global.debug(this, s);
}
    

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	JPanel pa = new JPanel(new GridLayout(1,4));
	for (int i=0; i < 4; i++) pa.add(new RobotStatus());
					 
	f.getContentPane().add(pa);
	f.pack();
	f.setVisible(true);
    }


}
