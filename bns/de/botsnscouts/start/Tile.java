package de.botsnscouts.start;

import java.awt.*;
import de.botsnscouts.board.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;

//saves image and name of tiles
//gives rotated image

public class Tile extends SpielfeldSim{

  public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Tile.class);

    private int thumbnailsize;
    private Image img;

    String kName;
    int rotat;

    //create new tile
    public Tile(String name, String field, int thumbnailsize) throws FormatException, FlaggenException{
	super(12,12,field,null);
	this.thumbnailsize=thumbnailsize;
	kName=name;
	rotat=0;
    }

    //create rotated tile
    public Tile(String name, String field, int rot, Image im, int thumbnailsize) throws FormatException, FlaggenException{
	this(name, field, thumbnailsize);
	rotat=rot;
	img=im;
    }

    public Image getImage(){
	if (img==null){
	    CAT.debug("creating image on-demand.");
	    img=SACanvas.createThumb(this,thumbnailsize);
	}
	return img;
    }

    public String getName(){
	return kName;
    }

    public int getRotation(){
	return rotat;
    }

    //gibt um 90° gedrehtes Clone
    public Tile getGedreht(){
	String gedrTile=get90GradGedreht();
	Tile drTile = null;
	try{
	    drTile =new Tile(kName,gedrTile,(rotat+1)%4,img,thumbnailsize);
	}catch(FlaggenException e){
	    System.err.println(e);
	}catch(FormatException e){
	    System.err.println(e);
	}
	return drTile;
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
