package de.botsnscouts.start;

import de.botsnscouts.board.*;
import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import de.botsnscouts.BotsNScouts;
import org.apache.log4j.*;

public class TileFactory {

    public static final Category CAT = Category.getInstance( TileFactory.class );

    private Hashtable tileTab;
    boolean fertig = false;
    Object lock=new Object();


    public TileFactory(){
	tileTab=new Hashtable();
    }

    boolean workerStarted = false;
    Thread worker = new Thread("TileWorker") {
        public void run(){

            // Load those from bns.home/tiles
            File kd=null;
            kd=new File(Conf.getBnsHome()+System.getProperty("file.separator")+"kacheln");
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

            // Load those from the distribution
            InputStream stream = BotsNScouts.class.getResourceAsStream("tiles/tile.index");
            if (stream==null){
                CAT.warn("Couldn't find tiles/tile.index");
                return;
            }
            Properties prop=new Properties();
            try{
                prop.load(stream);
            }catch(IOException e){
                CAT.warn("Couldn't load tile.index from distrib.");
            }
            int numTiles=0;
            try{
                numTiles=Integer.parseInt(prop.getProperty("numTiles"));
            }catch(NumberFormatException e){
                CAT.warn("Error parsing numTiles in tile.index!");
            }
            for (int i=0;i<numTiles;i++){
                String name=prop.getProperty("tile"+i);
                stream=BotsNScouts.class.getResourceAsStream("tiles/"+name);
                if (stream==null){
                    CAT.warn("Error loading tile"+i+" from distribution.");
                    continue;
                }
                putOneTile(stream,name);
            }
        }
    };


    /** Reads one tile and puts into the hashtable */
    private void putOneTile(InputStream istream, String name){
        CAT.debug("putting tile " + name);
	if (tileTab.get(name)!=null){
	    CAT.warn("Trying to redefine tile "+name);
	    return;
	}
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
	    kach = new Tile(name,str.toString());
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
        if( !workerStarted ) prepareTiles();
	try{
	    worker.join();
	}catch(InterruptedException e){
	    System.err.println(e);
	}
    }

    public void prepareTiles() {
        synchronized( worker ) {
            if( workerStarted ) return;
            worker.start();
	    worker.setPriority(Thread.MIN_PRIORITY);
            workerStarted = true;
        }
    }

    public void forgetTiles() {
        tileTab.clear();
    }
}


