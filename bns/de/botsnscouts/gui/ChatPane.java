package de.botsnscouts.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import de.botsnscouts.util.*;

/** Ein ChatPanel, das unterhalb des Spielfeldes angezeigt werden soll.
 *  @author Dirk
 */

public class ChatPane extends JPanel{

    private class ChatActionListener implements ActionListener{
	public void actionPerformed(ActionEvent e){
	    if (nachricht.getText().equals(""))
		return;
	    parent.sendMessage(nachricht.getText());
	    nachricht.setText("");
	}
    }

    private JTextField nachricht;
    HumanPlayer parent;

    public ChatPane(HumanPlayer parent){
	super(new FlowLayout());
	add(new JLabel(Message.say("ChatPane","prompt")));
	nachricht=new JTextField();
	nachricht.setPreferredSize(new Dimension(500,20));
	add(nachricht);
	ChatActionListener cal=new ChatActionListener();
	nachricht.addActionListener(cal);
	JButton senden=new JButton(Message.say("ChatPane","button"));
	senden.addActionListener(cal);
	add(senden);
	this.parent=parent;
	setPreferredSize(new Dimension(600,32));
    }

    public static void main(String[] args){
	JFrame f=new JFrame();
	f.getContentPane().add(new ChatPane(null));
	f.setVisible(true);
    }
}
