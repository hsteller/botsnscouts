package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import javax.swing.*;


public class ZielfahneErreicht extends JPanel{
  
    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor2 = new Color(140,255,140);

    public ZielfahneErreicht() {
	this("",false);
    }


    public ZielfahneErreicht(String inhalt, boolean tot) {
	setBackground(backColor);
	setLayout(new GridLayout((inhalt.length()+6),3)); 
	for (int i = 0; i < 9; i++) add(new Label(""));
	for (int i = 0; i < inhalt.length(); i++) {
	    Label l = new Label(inhalt.substring(i,i+1));
	    l.setFont(new Font("Sans", Font.BOLD, 24));
	    // ist der Robi tot, dann schreibe rot
	    if (tot) l.setForeground(Color.red);
	    else l.setForeground(foreColor2);
	    add(new Label(""));
	    add(l);
	    add(new Label(""));
	}
    }
    
    public Dimension getPreferredSize() {
	return new Dimension(180,550);
    }

    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
	Frame f = new Frame("Test");
	f.setSize(200,640);
	ZielfahneErreicht zf = new ZielfahneErreicht();
	f.add(zf);
	f.setVisible(true);
    }
}	
