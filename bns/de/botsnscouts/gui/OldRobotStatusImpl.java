/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

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
public class OldRobotStatusImpl extends JPanel implements RobotStatus {
    int xsize=75, ysize=70;
    JLabel name;
    JLabel lifes;
    JLabel flag;
    JLabel damage;
    JLabel track;
    JPanel roboInfo;

    static final Image flagge = CursorMan.getImages(CursorMan.CURSOR)[0];
    static final ImageIcon botcenterImage = ImageMan.getImageIcon(ImageMan.PNG_ROBOCENTER);
    static final ImageIcon damageImage = ImageMan.getImageIcon(ImageMan.PNG_BOTDAMAGE);
    static final Image[] robotImages = CursorMan.getImages(CursorMan.STATUSROBOTS);
    Bot robot;

    public OldRobotStatusImpl () {
	this(Bot.getNewInstance("TestRob"),null,null);
    }

    public OldRobotStatusImpl (Bot r, MouseListener botCenter, MouseListener flagCenter) {
	robot = r;
	name = new JLabel(r.getName());
	flag = new JLabel(""+r.getNextFlag(),new ImageIcon(flagge),JLabel.LEFT);
	lifes = new JLabel(""+r.getLivesLeft(),new ImageIcon(robotImages[r.getBotVis()]),JLabel.RIGHT);
	d("Visualisierung :"+r.getBotVis()+ " an "+r.getName()+" zugeteilt.");
	damage = new JLabel(""+r.getDamage(),damageImage,JLabel.LEFT);
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

	track.setToolTipText(Message.say("OldRobotStatusImpl","centerRobToolTip",r.getName()));
	flag.setToolTipText(Message.say("OldRobotStatusImpl","centerFlag",flag.getText()));

	track.addMouseListener(botCenter);
	flag.addMouseListener(flagCenter);

	roboInfo.add(flag);
	roboInfo.add(lifes);
	roboInfo.add(damage);
	roboInfo.add(track);

	add(name,BorderLayout.NORTH);
	add(roboInfo,BorderLayout.CENTER);
    }


    public static Image[] getRobotImages() {
      return robotImages;
    }

    public Dimension getMinimumSize() {
	return new Dimension(xsize,ysize);
    }

    public Dimension getPreferredSize() {
	return new Dimension(xsize,ysize);
    }

    public void updateRobot(Bot r) {
    if (r.getLivesLeft() != Integer.parseInt(lifes.getText())) {
	lifes.setText(""+r.getLivesLeft());
    }
    if (r.getNextFlag() != Integer.parseInt(flag.getText())) {
	flag.setText(""+r.getNextFlag());
    }
    if (r.getDamage() != Integer.parseInt(damage.getText())) {
	damage.setText(""+r.getDamage());
    }

    robot = r;

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
        org.apache.log4j.BasicConfigurator.configure();
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
	JPanel pa = new JPanel(new GridLayout(1,4));
	for (int i=0; i < 4; i++) pa.add(new OldRobotStatusImpl());

	f.getContentPane().add(pa);
	f.pack();
	f.setVisible(true);
    }

    public Bot getRobot() {
        return robot;
    }
}
