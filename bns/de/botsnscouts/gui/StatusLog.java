package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * shows the status messages and action messages on the scrren
 * @author Lukasz Pekacki
 */

public class StatusLog  extends JPanel {
    // Objects
    private JTextArea textArea = new JTextArea();
    private JLabel singleMessage = new JLabel();
    
    public StatusLog() {
	setLayout(new FlowLayout(FlowLayout.LEFT));
	add(singleMessage);
    }

    public void addMessage(String s) {
	singleMessage.setText(s);
    }
	

    private void d(String s){
	Global.debug(this, s);
    }

}



