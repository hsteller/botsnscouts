package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * ask the user for the direction
 * @author Lukasz Pekacki
 */
public class GetDirection extends JPanel {
    public GetDirection(ActionListener al) {
	setLayout(new BorderLayout());
	    
	JLabel titel = new JLabel(Message.say("SpielerMensch","richtungwahl"));
	add(titel,BorderLayout.NORTH);
	TJPanel p = new TJPanel();
	p.setBorder(new EtchedBorder(4));
	p.setLayout(new GridLayout(3,3));
	p.setSize(200,200);
	p.add(Box.createGlue());
	PfeilC no = new PfeilC("NORD",Directions.NORD);
	no.addActionListener(al);
	p.add(no);
	p.add(Box.createGlue());
	PfeilC we = new PfeilC("WEST",Directions.WEST);
	we.addActionListener(al);
	p.add(we);
	p.add(new JButton());
	PfeilC os = new PfeilC("OST",Directions.EAST);
	os.addActionListener(al);
	p.add(os);
	p.add(Box.createGlue());
	PfeilC su = new PfeilC("SUED",Directions.SOUTH);
	su.addActionListener(al);
	p.add(su);
	p.add(Box.createGlue());
	JPanel pp = new JPanel();
	pp.add(p);
	add(pp, BorderLayout.CENTER);
    }


    private class PfeilC extends JButton {
	private int richt = Directions.NORD;
	PfeilC(String s, int r){
	    this.setOpaque(false);
	    richt = r;
	}

	public Dimension getPreferredSize() {
	    return new Dimension(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(60,60);
	}

	public void paintComponent(Graphics g){
	    g.setColor(Color.lightGray);
	    g.setColor(this.getForeground());
	    if (richt == Directions.NORD){
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
}






