package de.botsnscouts.start;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import de.botsnscouts.board.*;
import de.botsnscouts.util.*;

public class TileFactory extends Thread{

    private Hashtable kachelTab;
    private int thGR;
    boolean fertig = false;
    Object lock=new Object();

    
    public TileFactory(int gr){
	thGR=gr;
	kachelTab=new Hashtable();
    }
    public void run(){
// 	URL kurl=getClass().getResource("kacheln");
// 	File kd=new File(kurl.getFile());

 	File kd=new File("kacheln");
	//Global.debug(this,kd.toString());
	String[] all = kd.list(new RRAFilter());
	if(all!=null){
	    for (int i=0;i<all.length;i++){
		//Global.debug(this,"kacheln"+File.separator+all[i]);
		putOneTile(all[i]);
	    }
	}
	//	setFertig();
	/*	synchronized(lock){
	    fertig = true;
	    lock.notify();
	    }*/
    }

    //liest eine Tile aus der Datei ein und speichert die in hashtable
    private void putOneTile(String name){
	//öfene die Tiledatei
	StringBuffer str=new StringBuffer();
	try{
//	    InputStream istream=getClass().getResourceAsStream("kacheln/"+name);
	    File file = new File("kacheln", name);
	    //FileInputStream istream=new FileInputStream("kacheln" + File.separator + name);
	    FileInputStream istream = new FileInputStream( file );
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
	    kachelTab.put(name, kachAr);
	}catch(FlaggenException e){
	    System.err.println(e);
	}catch(FormatException e){
	    System.err.println(e);
	}
    }

    //gibt eine Tile mit Drehung zurück
    public Tile getTile(String name, int drehung){
	checkLadeStatus();	
	Tile[] kachAr=(Tile[])kachelTab.get(name);
	//Global.debug(this,kachelTab.toString());
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
	int anz=kachelTab.size();
	TileInfo[] infos=new TileInfo[anz];
	String[] all= new String[anz];
	int i=0;
	for (Enumeration namen=kachelTab.keys(); namen.hasMoreElements();i++){
	    all[i]=(String)namen.nextElement();
          }
	Arrays.sort(all);
	for (i=0;i<anz;i++){
	    infos[i]=new TileInfo(all[i],((Tile[])kachelTab.get(all[i]))[0].getImage());
	}
	return infos;
    }

    private void checkLadeStatus(){
	try{
	    this.join();
	}catch(InterruptedException e){
	    System.err.println(e);
	}
	/*	synchronized(lock){
	    if( !fertig ) {
	    //	    if (!getFertig()){
		try{
		    lock.wait();
		}catch(InterruptedException e){
		    System.err.println(e);
		}
	    }
	    }*/
    }

}


class RRAFilter implements FilenameFilter{

    public RRAFilter(){}

    public boolean accept(File dir, String name){
	try{
	    // endsWith(".rra") ???
	    return name.endsWith(".rra");
	    /*
	    if (name.substring(name.length()-4,name.length()).equals(".rra")) return true;
	    */
	} catch(Throwable t){return false;}
	//return false;
    }

}
