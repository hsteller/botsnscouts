package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.util.*;
import de.botsnscouts.util.*;


public class Start extends JFrame implements WindowListener{

    StSpFassade fassade;

    StartAnfang startAnfang;                //startbildschirm
    StartSpielfeld startSpielfeld;          //spielfeld zusammenstellen
    StartTeilnehmen startTeilnehmen;        //am spiel teilnehmen
    StartZuschauen startZuschauen;          //einem spiel zuschauen
    StartStart startStart;                  //bildschirm mit anmeldeinfos und LOS button
    StartSpielfeldEditor startSpielfeldEditor;

    JPanel current;         //was in moment angezeigt wird
    WaiterThread wth;
    
    public Start(){  
	super(Message.say("Start","mStartTitel"));
	//System.out.println(Message.say("Start","mBla"));
	wth=new WaiterThread(this);

	Toolkit tk=Toolkit.getDefaultToolkit();

	if(tk.getScreenSize().height<600){
	    fassade=new StSpFassade(150);
	}else{
	    fassade=new StSpFassade();
	}

	Global.debug(this,tk.getScreenSize().width+"x"+tk.getScreenSize().height);
	setSize(tk.getScreenSize());
	setLocation(0,0);
//	requestFocus();

	startAnfang=new StartAnfang(this);
	current=startAnfang;
//	startStart=new StartStart(this);
//	current=startStart;
	setContentPane(current);

//	Global.verbose=true;

	addWindowListener(this);

	show();
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
//	dispose();
    }

    public void nullen(){
	if(startAnfang!=null){
	    startAnfang.parent=null;
	    startAnfang=null;
	}
	if(startSpielfeld!=null){
	    startSpielfeld.parent=null;
	    startSpielfeld.buttons.parent=null;
	    startSpielfeld.buttons=null;
	    startSpielfeld.unten.parent=null;
	    startSpielfeld.unten=null;
	    startSpielfeld.spf.parent=null;
	    startSpielfeld.spf.kachP=null;
	    startSpielfeld.spf.fassade=null;
	    startSpielfeld.spf=null;
	    startSpielfeld=null;
	}
	if(startTeilnehmen!=null){
	    startTeilnehmen.parent=null;
	    startTeilnehmen=null;
	}
	if(startZuschauen!=null){
	    startZuschauen.parent=null;
	    startZuschauen=null;
	}
	if(startStart!=null){
	    startStart.parent=null;
	    startStart.anmeldung.parent=null;
	    startStart.anmeldung=null;
	    startStart.unten.parent=null;
	    startStart.unten=null;
	    startStart.ks.parent=null;	    
	    startStart.ks=null;	    
	    startStart.listen=null;	    
	    startStart=null;
	}
	if(startSpielfeldEditor!=null){
	    startSpielfeldEditor.parent=null;
	    startSpielfeldEditor.spf=null;
	    startSpielfeldEditor.images=null;
	    startSpielfeldEditor.kachelInfos=null;
	    startSpielfeldEditor.cursors=null;
	}
	
    }


    public static void main(String[] argv){
	Global.verbose=true;
	String lang="english";
	if (argv.length>=1){
	    if (argv[0].equals("english")||argv[0].equals("deutsch")){
		lang=argv[0];
	    }
	}
	try{
	    Message.setLanguage(lang);
	}catch(LanguageLoadException exc){
	    System.err.println(exc+"kann nicht!");
	}
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );

	if(argv.length>=4){
	    try{
		String spielfeld=argv[1];
		if (spielfeld.endsWith(".spf")){
		    spielfeld=spielfeld.substring(0,spielfeld.length()-4);
		}
		Global.debug("Spielfeld "+spielfeld);
		StartSpielfeldLoadHelfer helfer=new StartSpielfeldLoadHelfer();
		StSpFassade fassade=new StSpFassade();
		Properties prop = helfer.getProperties(spielfeld);
		Global.debug("Properties "+prop);
		fassade.loadSpfProp(prop);
		Global.debug("Spielfed loaded");
		fassade.startSpiel();
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
		    Global.debug("Künstlichen Spieler gestartet");
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
