package de.botsnscouts.start;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import de.botsnscouts.board.*;
import de.botsnscouts.util.*;

public class TileFactory extends Thread{

    private Hashtable tileTab;
    private int thGR;
    boolean fertig = false;
    Object lock=new Object();

    
    public TileFactory(int gr){
	thGR=gr;
	tileTab=new Hashtable();
    }
    public void run(){
// 	URL kurl=getClass().getResource("kacheln");
// 	File kd=new File(kurl.getFile());

	File kd=null;
	try{
	    kd=new File(Conf.getBnsHome()+System.getProperty("file.separator")+"kacheln");
	}catch(Exception e){
	    try{
		kd=new File(Conf.getBnsHome()+System.getProperty("file.separator")+"tiles");
	    }catch (Exception ee){
		return;
	    }
	}
	// 	File kdj=new File(de.botsnscouts.BotsNScouts.class.getResource("kacheln").getFile());
	File[] all = kd.listFiles(new RRAFilter());
	//File[] allj = kdj.listFiles(new RRAFilter());
	FileInputStream istream;
	if(all!=null){
	    for (int i=0;i<all.length;i++){
		try{
		    istream = new FileInputStream( all[i] );
		}catch(FileNotFoundException e){
		    continue;
		}
		putOneTile(istream,all[i].getName());
	    }
	}
	/*if(allj!=null){
	    for (int i=0;i<allj.length;i++){
		try{
		    istream = new FileInputStream( allj[i] );
		}catch(FileNotFoundException e){
		    continue;
		}
		putOneTile(istream,allj[i].getName());
	    }
	    }*/
    }

    //liest eine Tile aus der Datei ein und speichert die in hashtable
    private void putOneTile(InputStream istream, String name){
	//öfene die Tiledatei
	StringBuffer str=new StringBuffer();
	try{
	    BufferedReader kachReader =new BufferedReader(new InputStreamReader(istream));
	    String tmp=null;
	    //und lese Spielfeld aus
	    while((tmp=kachReader.readLine())!=null)
		str.append(tmp+"\n");
	    
	}catch(Exception e){
	    System.err.println(e);
	}
	//erzeige Tile mit der Tilestring
	Tile kach=null;
	try{
	    kach = new Tile(name,str.toString(), thGR);
	    Tile[] kachAr=new Tile[4];
	    kachAr[0]=kach;//Die Tile mit Drehung 0 wird initialisiert
	    tileTab.put(name, kachAr);
	}catch(FlaggenException e){
	    System.err.println(e);
	}catch(FormatException e){
	    System.err.println(e);
	}
    }

    //gibt eine Tile mit Drehung zurück
    public Tile getTile(String name, int drehung){
	checkLadeStatus();	
	Tile[] kachAr=(Tile[])tileTab.get(name);
	//Global.debug(this,tileTab.toString());
	if (kachAr[drehung]!=null){
	    return kachAr[drehung];
	}
	//nehme erst an, es wird jedes mal nur um 90 Grad gedreht
	//also die Drehung davor existiert
	kachAr[drehung]=kachAr[drehung-1].getGedreht();
	return kachAr[drehung];
    }

    public TileInfo[] getTileInfos(){
	checkLadeStatus();
	int anz=tileTab.size();
	TileInfo[] infos=new TileInfo[anz];
	String[] all= new String[anz];
	int i=0;
	for (Enumeration namen=tileTab.keys(); namen.hasMoreElements();i++){
	    all[i]=(String)namen.nextElement();
          }
	Arrays.sort(all);
	for (i=0;i<anz;i++){
	    infos[i]=new TileInfo(all[i],((Tile[])tileTab.get(all[i]))[0].getImage());
	}
	return infos;
    }

    private void checkLadeStatus(){
	try{
	    this.join();
	}catch(InterruptedException e){
	    System.err.println(e);
	}
    }

}


