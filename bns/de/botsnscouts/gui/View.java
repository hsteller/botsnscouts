package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;

import org.apache.log4j.Category;

/**
 * Helferklasse, die die Komponenten auf dem Bildschirm plaziert
 * @author Lukasz Pekacki
 */

public class View extends JFrame {
    static Category CAT = Category.getInstance(View.class);

    private ChatPane chatpane;
    private JMenuBar menus;

    AusgabeView ausgabeView;
    HumanView humanView;

    protected final boolean NURAUSGABE = true;
    protected final boolean MENSCHAUSGABE = false;

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
        addMenuBar();

    }

    private void addMenuBar(){
      if (menus==null){
        CAT.debug("menus==null; getting menus..");
        if (ausgabeView!=null){
           menus = ausgabeView.getMenuBar();
           this.setJMenuBar(menus);
           menus.setVisible(true);
        }
        else {
          CAT.debug("unable to install MenuBar! No AusgabeView found!");
        }
      }
      else {
        CAT.debug("menus already loaded!\n Now setting them..");
        this.setJMenuBar(menus);
      }
      CAT.debug("Leaving addMenubar");
    }

    protected void makeVisible() {
        CAT.debug("makeVisible called");
        addMenuBar();
        if (CAT.isDebugEnabled())
          CAT.debug("menubar is "+menus);
	validate();
	setVisible(true);
    }


    public void addAusgabeView(AusgabeView av) {
        CAT.debug("addAusgabeView called");
	if (ausgabeView==null) {
          CAT.debug("ausgabeView is null");
          ausgabeView=av;
          this.addMenuBar();
        }
        getContentPane().add(av, BorderLayout.CENTER);

    }

    public void addChatPane(ChatPane cp){
        chatpane = cp;
	getContentPane().add(cp, BorderLayout.SOUTH);
    }

    public void removeChatPane() {
        getContentPane().remove(chatpane);
        chatpane = null;
    }

    protected void quitHumanPlayer() {
         CAT.debug("Telling the human player to quit..");
        if (humanView!=null) {
          humanView.quitHumanPlayer();
          humanView=null;
          CAT.debug(".. done");
        }
        else {
          CAT.debug("There seems to be no human player, I'm propably a standalone Ausgabe(view)..");
        }
        CAT.debug("Disposing the frame..");
        dispose();

    }




}



