package de.botsnscouts.start;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class Logo extends JLabel {
    public Logo() {
	super();
	Icon icon = new ImageIcon(de.botsnscouts.BotsNScouts.class.getResource("images/bnslogo.jpg"));
	setIcon( icon );
	setBackground(Color.gray);
	setBorder( new EtchedBorder(8) );
    }
}
