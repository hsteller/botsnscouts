package de.spline.rr;

import java.awt.*;

//Die Klasse die zu Kachel Image und name speichert,
//ein gedrehtes Clone zurück gibt

public class Kachel extends SpielfeldSim{

    Image img;
    String kName;
    int drehung;
    //    int GR=150;

    //Der Konstruktor zum erzeugen einer neuen Kachel
    public Kachel(String name, String feld, int GR) throws FormatException, FlaggenException{
	super(12,12,feld,null);
	kName=name;
	drehung=0;
	img=SACanvas.createThumb(this,GR);
    }

    //Konstruktor zum erzeugen einer gedrehten Kachel
    public Kachel(String name, String feld, int dr, Image im) throws FormatException, FlaggenException{
	super(12,12,feld,null);
	kName=name;
	drehung=dr;
	img=im;
    }

    public Image getImage(){
	return img;
    }

    public String getName(){
	return kName;
    }

    public int getDrehung(){
	return drehung;
    }

    //gibt um 90° gedrehtes Clone
    public Kachel getGedreht(){
	String gedrKachel=get90GradGedreht();
	Kachel drKachel = null;
	try{
	    drKachel =new Kachel(kName,gedrKachel,(drehung+1)%4,img);
	}catch(FlaggenException e){
	    System.err.println(e);
	}catch(FormatException e){
	    System.err.println(e);
	}
	return drKachel;
    }

    //prüft ob Flagge(n) gültige Position hat(haben)
    public boolean testFlagge(Ort[] fl){
	String kach=getComputedString();
	SpielfeldSim test;
	try{
	    // enno: habe checkflaggen protected gemacht
	    checkFlaggen(fl);
	    //test= new SpielfeldSim(12,12,kach,fl);
	}catch(FlaggenException e){
	    return false;
	}
	/*
	catch(FormatException e){
	    System.err.println(e);
	}
	*/
	return true;
    }

}
