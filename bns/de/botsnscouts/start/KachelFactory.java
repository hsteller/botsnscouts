package de.botsnscouts.start;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import de.botsnscouts.board.*;

public class KachelFactory extends Thread{

    private Hashtable kachelTab;
    private int thGR;
    boolean fertig = false;
    Object lock=new Object();

    
    public KachelFactory(int gr){
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
		putOneKachel(all[i]);
	    }
	}
	//	setFertig();
	/*	synchronized(lock){
	    fertig = true;
	    lock.notify();
	    }*/
    }

    //liest eine Kachel aus der Datei ein und speichert die in hashtable
    private void putOneKachel(String name){
	//öfene die Kacheldatei
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
	//erzeige Kachel mit der Kachelstring
	Kachel kach=null;
	try{
	    kach = new Kachel(name,str.toString(), thGR);
	    Kachel[] kachAr=new Kachel[4];
	    kachAr[0]=kach;//Die Kachel mit Drehung 0 wird initialisiert
	    kachelTab.put(name, kachAr);
	}catch(FlaggenException e){
	    System.err.println(e);
	}catch(FormatException e){
	    System.err.println(e);
	}
    }

    //gibt eine Kachel mit Drehung zurück
    public Kachel getKachel(String name, int drehung){
	checkLadeStatus();	
	Kachel[] kachAr=(Kachel[])kachelTab.get(name);
	//Global.debug(this,kachelTab.toString());
	if (kachAr[drehung]!=null){
	    return kachAr[drehung];
	}
	//nehme erst an, es wird jedes mal nur um 90 Grad gedreht
	//also die Drehung davor existiert
	kachAr[drehung]=kachAr[drehung-1].getGedreht();
	return kachAr[drehung];
    }

    public KachelInfo[] getKachelInfos(){
	checkLadeStatus();
	int anz=kachelTab.size();
	KachelInfo[] infos=new KachelInfo[anz];
	String[] all= new String[anz];
	int i=0;
	for (Enumeration namen=kachelTab.keys(); namen.hasMoreElements();i++){
	    all[i]=(String)namen.nextElement();
          }
	Arrays.sort(all);
	for (i=0;i<anz;i++){
	    infos[i]=new KachelInfo(all[i],((Kachel[])kachelTab.get(all[i]))[0].getImage());
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
