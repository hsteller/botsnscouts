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
    HumanView humanView;

    public View() {
	setTitle(Message.say("AusgabeFrame","gameName"));
    }

    public View(AusgabeView av) {
	setTitle(Message.say("AusgabeFrame","gameName"));
	ausgabeView=av;
	initView();
	getContentPane().add(av, BorderLayout.CENTER);
	makeVisible();
    }

    public View(HumanView hv) {
	setTitle(Message.say("AusgabeFrame","gameName"));
	humanView = hv;
	initView();
	getContentPane().add(hv, BorderLayout.EAST);
    }


    public void shutup() {
	System.exit(0);
    }




    public static void main (String args[]) {
	Message.setLanguage("deutsch");
	JFrame f = new View();
	AusgabeView a = new AusgabeView();
	f.getContentPane().add(a);
	f.setVisible(true);
   }

    synchronized private void initView() {
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
	getContentPane().setLayout(new BorderLayout());
	
    }

    protected void makeVisible() {
	validate();
	setVisible(true);
    }


    public void addAusgabeView(AusgabeView av) {
	getContentPane().add(av, BorderLayout.CENTER);
    }
}



