package de.botsnscouts.gui;

import de.botsnscouts.board.*;
import java.awt.*;
import java.net.*;
import javax.swing.*;
/**
 * Diese Klasse spielt bei Spielende den Abspann ab
 **/
public class Abspann extends JPanel {
    // Konstanten
    private static final int x = 700;
    private static final int y = 700;
    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor = new Color(110,240,110);
    private final Color[] robocolor = SACanvas.robocolor;
    private final Font font1 = new Font("SansSerif",Font.BOLD,24);
    private final static  Image RR = Toolkit.getDefaultToolkit().getImage("./images/RallyMed.jpg");
    // Variablen
    public Abspann(String[] gewinnerListe) {
	setSize(700,700);
	setFont(font1);
	setForeground(foreColor);
	setBackground(backColor);
	if (gewinnerListe != null) {
	setLayout(new GridLayout((gewinnerListe.length+2),3));
	for (int i=0; i<4; i++) add(new Label(""));
	add(new Label(Message.say("Abspann","mTitelAbspann")));
	add(new Label(""));

	    for (int i=0; i < gewinnerListe.length; i++) {
		if((gewinnerListe[i].equals("null"))) {
		    add(new Label(""));
		    add(new Label(""));
		    add(new Label(""));
		}
		else {
		    add(new Label(""));
		    add(new Label(Message.say("Abspann","mAbspannPlazierung",i+1,gewinnerListe[i])));
		    add(new Label(""));
		}
		
	    }
	}
	else {
	    Global.debug(this,"Gewinnerliste ist leer");
	    setLayout(new GridLayout(2,1));
	    add(new Label(Message.say("Abspann","mTitelAbspannTot")));
	    add(new Label(Message.say("Abspann","mTitelAbspannTotInfo")));
	}
	    
	setVisible(true);
    }
								
}
