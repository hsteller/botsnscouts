package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Helferklasse, die die Komponenten auf dem Bildschirm plaziert
 * @author Lukasz Pekacki
 */

public class View extends JFrame {

    AusgabeView ausgabeView;

    public View() {
	setTitle(Message.say("AusgabeFrame","gameName"));
    }

    public View(AusgabeView av) {
	setTitle(Message.say("AusgabeFrame","gameName"));
	ausgabeView=av;
	// Fenstergröße auf Vollbild setzen
	Toolkit tk=Toolkit.getDefaultToolkit();
	setSize(tk.getScreenSize().width-8,tk.getScreenSize().height-8);
	setLocation(4,4);

	// Fentster-Schließen behandeln
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e){
		    shutup();
		}});


	// Layout erzeugen
	// setLayout(new FlowLayout());

	getContentPane().add(av);
	validate();
	setVisible(true);
    }


    public void shutup() {
	System.exit(0);
    }




    public static void main (String args[]) {
	try {
	    Message.setLanguage("deutsch");
	}
	catch (Exception e) {e.printStackTrace();}
	JFrame f = new View();
	AusgabeView a = new AusgabeView();
	f.getContentPane().add(a);
	f.setVisible(true);
   }


}



