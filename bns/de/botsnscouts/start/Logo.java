package de.botsnscouts.start;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import de.botsnscouts.util.ImageMan;

public class Logo extends JLabel {
    public Logo() {
	super();
	Icon icon = ImageMan.getIcon("bnslogo.jpg");
	setIcon( icon );
	setBackground(Color.gray);
	setBorder( new EtchedBorder(8) );
    }
}
