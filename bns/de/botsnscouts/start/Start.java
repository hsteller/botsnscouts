package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import java.net.*;
import java.io.*;


import org.apache.log4j.Category;

import de.botsnscouts.util.*;


public class Start extends JFrame implements WindowListener{

    static Category CAT = Category.getInstance(Start.class);

    Facade fassade;

    Paint paint;
    MainMenu mainMenu;                //startbildschirm
    GameFieldPanel gameFieldPanel;          //spielfeld zusammenstellen
    ParticipatePanel partPanel;        //am spiel teilnehmen
    WatchPanel watchPanel;          //einem spiel zuschauen
    StartPanel startPanel;                  //bildschirm mit anmeldeinfos und LOS button
    FieldEditor fieldEditor;

    JPanel current;         //was in moment angezeigt wird
    WaiterThread wth;

    public Start(){
	super(Message.say("Start","mStartTitel"));
	wth=new WaiterThread(this);
	Toolkit tk=Toolkit.getDefaultToolkit();
	if(tk.getScreenSize().height<600){
	    fassade=new Facade(150);
	}else{
	    fassade=new Facade();
	}
	setSize(tk.getScreenSize());
	setLocation(0,0);

	ImageIcon icon = ImageMan.getIcon( "garage2.jpg");
	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
	Graphics g = bgimg.getGraphics();
	icon.paintIcon(this, g, 0,0);
	g.dispose();
	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
	paint = new TexturePaint( bgimg, anchor );

	mainMenu=new MainMenu(this);
	current=mainMenu;
	setContentPane(current);

	addWindowListener(this);
	show();
    }

    public void toMainMenu(){
	current=mainMenu;
	setContentPane(current);
	mainMenu.unrollOverButs();
	setTitle(Message.say("Start","mStartTitel"));
    }

    public void windowDeactivated(WindowEvent e) {}
    public void windowOpened(WindowEvent e)      {}
    public void windowClosing(WindowEvent e) {
	myclose();
    }
    public void windowClosed(WindowEvent e)      {}
    public void windowIconified(WindowEvent e)   {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e)   {}

    public void myclose(){
	dispose();
	System.exit(0);
    }

    public void addKS(Thread k){
	wth.addThread(k);
    }

    public void addServer(){
	wth.setServer();
    }

    public void resetWaiter(){
	wth.beende();
	wth.reset();
	wth=new WaiterThread(this);
    }

    public void beenden(){
	Global.debug(this,"beenden() wurde aufgerufen");
	try{
	    if(!wth.isAlive()){
		wth.start();
	    }
	}catch(IllegalThreadStateException e){
	    System.err.println(Message.say("Start","eSpielEnde"));
	}
    }


    public static void main(String[] argv){
	Global.verbose=true;
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );


        //load Sounds
        CAT.debug("starting soundman..");
        SoundMan.loadSounds();
        CAT.debug("..done");

	//language conf
	Locale myLocale=null;
	String loc=Conf.getProperty("language.isSet");
	if (loc != null){
	    myLocale=new Locale(Conf.getProperty("language.lang"),Conf.getProperty("language.country"));
	}else{
	    Locale[] list=Message.getLocales();
	    String[] locals=new String[list.length];
	    for (int i=0;i<locals.length;i++){
		locals[i]=list[i].getDisplayLanguage();
	    }
	    int sel=JOptionPane.showOptionDialog(null,"Please select your Language","Language selection",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,locals,locals[0]);
	    if (sel==JOptionPane.CLOSED_OPTION){
		myLocale=new Locale("en","US");
	    }else{
		myLocale=list[sel];
		Conf.setProperty("language.isSet","yes");
		Conf.setProperty("language.lang",myLocale.getLanguage());
		Conf.setProperty("language.country",myLocale.getCountry());
		Conf.saveProperties();
	    }
	}

	Message.setLanguage(myLocale);
	//ende language conf


	if(argv.length>=4){
	    try{
		String spielfeld=argv[1];
		if (spielfeld.endsWith(".spf")){
		    spielfeld=spielfeld.substring(0,spielfeld.length()-4);
		}
		Global.debug("Spielfeld "+spielfeld);
		GameFieldLoader loader=new GameFieldLoader();
		Facade fassade=new Facade();
		Properties prop = loader.getProperties(spielfeld);
		Global.debug("Properties "+prop);
		fassade.loadSpfProp(prop);
		Global.debug("Spielfed loaded");
		fassade.startGame();
		Global.debug("Server gestartet");
		if (argv[2].equals("yes")){
		    fassade.amSpielTeilnehmen(KrimsKrams.randomName(),0);
		    Global.debug("Menschlichen Spieler gestartet");
		}else{
		    fassade.einemSpielZuschauen();
		    Global.debug("Ausgabe gestartet");
		}
		int anzKS=0;
		try{
		    anzKS=Integer.parseInt(argv[3]);
		    for (int i=0;i<anzKS;i++){
			fassade.kuenstlicheSpielerStarten(100);
		    Global.debug("Knstlichen Spieler gestartet");
		    }
		}catch(NumberFormatException e){
		}
		try{
		    Thread.sleep((anzKS+1)*3000);
		}catch(InterruptedException e){
		    System.err.println(e);
		}
		fassade.spielGehtLos();
		Global.debug("Spiel geht los");
		return;
	    }catch(Exception e){
		System.err.println(e);
	    }
	}else{
	new Start();
	}
    }

}//class Start end
