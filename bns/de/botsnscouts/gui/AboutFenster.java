package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;


public class AboutFenster extends JFrame{
  
    public AboutFenster() {
	Toolkit tk=Toolkit.getDefaultToolkit();
	Dimension d = tk.getScreenSize();
	setLocation((d.width/2)-200,((d.height/2)-200));
	setSize(400,500);
	setTitle(Message.say("AboutFenster","mtitel"));

	String s = Message.say("AboutFenster","mtext");
	JPanel inhalt = new JPanel();
	inhalt.setBorder(new EmptyBorder(10,10,10,10));

	JTextArea text = new JTextArea(s){
		public Dimension getPreferredSize() {
		    return new Dimension(370,400);
		}
	    };
	text.setLineWrap(true);
	text.setWrapStyleWord(true);
	text.setEditable(false);
	inhalt.add(text);

	JButton ok = new JButton(Message.say("AboutFenster","mok"));
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    dispose();
		}
	    }
			     );


	inhalt.add(ok);

	getContentPane().add(inhalt);

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e){
		    dispose();
		}});


	

	setVisible(true);
	}
    
    public static void main (String args[]) {
	try {
	   	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
	AboutFenster f = new AboutFenster();
	// f.setSize(200,640);
	f.setVisible(true);
   }
}	
