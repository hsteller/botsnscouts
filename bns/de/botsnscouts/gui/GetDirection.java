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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import de.botsnscouts.util.Directions;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;
/**
 * ask the user for the direction
 * @author Lukasz Pekacki
 */
public class GetDirection extends TJPanel {
    private static Color arrowColor = GreenTheme.getTextColor();// new Color(64,191,64); 
    public GetDirection() {
	this(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    System.err.println("Pressed: "+ae.getActionCommand());
		}
	    }
	     );
    }


    public GetDirection(ActionListener al) {
	setLayout(new BorderLayout());

	JLabel titel = new TJLabel(Message.say("SpielerMensch","richtungwahl"));
	add(titel,BorderLayout.NORTH);
	TJPanel p = new TJPanel();
	p.setBorder(new EtchedBorder(4));
	p.setLayout(new GridLayout(3,3));
	p.setSize(200,200);
	p.add(Box.createGlue());
	PfeilC no = new PfeilC("NORTH",Directions.NORTH);
	no.addActionListener(al);
	p.add(no);
	p.add(Box.createGlue());

	PfeilC we = new PfeilC("WEST",Directions.WEST);
	we.addActionListener(al);
	p.add(we);
	//p.add(new JButton());
        p.add(Box.createGlue());
	PfeilC os = new PfeilC("EAST",Directions.EAST);
	os.addActionListener(al);
	p.add(os);
	p.add(Box.createGlue());
	PfeilC su = new PfeilC("SOUTH",Directions.SOUTH);
	su.addActionListener(al);
	p.add(su);
	p.add(Box.createGlue());
	JPanel pp = new TJPanel();
	pp.add(p);
	add(pp, BorderLayout.CENTER);
    }


    private class PfeilC extends JButton {
	private int richt = Directions.NORTH;
	PfeilC(String s, int r){
	    this.setOpaque(false);
            setBorderPainted( false );
	    richt = r;
	    setActionCommand(""+r);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(60,60);
	}

	
	public void paintComponent(Graphics g){
	    g.setColor(arrowColor);
	    if (richt == Directions.NORTH){
		int[] x = {5,55,30};
		int[] y = {55,55,5};
		g.fillPolygon(x,y,3);
	    }
	    else if(richt == Directions.SOUTH){
		int[] x = {5,55,30};
		int[] y = {5,5,55};
		g.fillPolygon(x,y,3);
	    }
	    else if(richt == Directions.WEST){
		int[] x = {55,55,5};
		int[] y = {5,55,30};
		g.fillPolygon(x,y,3);
	    }
	    else {
		int[] x = {5,55,5};
		int[] y = {5,30,55};
		g.fillPolygon(x,y,3);
	    }

	}
    }

    public static void main (String args[]) {
	Message.setLanguage("deutsch");
        JWindow f = new JWindow();
        GetDirection g = new GetDirection();
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );
	f.getContentPane().add(g);
	f.pack();
	f.setLocation(100,100);
	f.setVisible(true);
    }

}






