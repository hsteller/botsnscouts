package de.botsnscouts.gui;

import de.botsnscouts.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;

/**
 * @author Daniel Holtz
 */

public class Splash{
    JWindow splash;
    JPanel panel;
    JLabel bildLabel,textLabel;
    String labelText;

    public void setText(String s){ 
	textLabel.setText("  "+s);
    }

    public void showSplash(String s){
	splash = new JWindow();
        panel = (JPanel)splash.getContentPane();
	int width = 744;
	int height = 184;
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	int x = (screen.width-width)/2;
	int y = (screen.height-height)/2;
	splash.setBounds(x,y,width,height);
	try{
	    // XXX: move to ImageMan
	    URL url = Class.forName("de.botsnscouts.util.BotsnScouts").getResource("images/bnslogo.jpg");
	    bildLabel = new JLabel(new ImageIcon(url));
	    bildLabel.setBorder( new EtchedBorder(8));
	}
	catch(ClassNotFoundException cnfe) {System.err.println("splash: kaputt "+cnfe.getMessage()); System.exit(0);}
	textLabel = new JLabel("  "+s);
	textLabel.setBorder( new EtchedBorder(8));
	//textLabel.setBackground(Color.black);
	textLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
	panel.add(bildLabel, BorderLayout.CENTER);
	panel.add(textLabel, BorderLayout.SOUTH);	
	splash.pack();
	splash.setVisible(true);
    }    

    public void noSplash(){
	splash.setVisible(false);
    }
}
