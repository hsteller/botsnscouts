package de.botsnscouts.start;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;

public class StartSpielfeldLoadHelfer{

    public String[] getSpielfelder(){
	//	URL kurl=getClass().getResource("kacheln");
	//File kd=new File(kurl.getFile());
 	File kd=new File("kacheln");
	String[] all = kd.list(new SpfFilter());
	for (int i=0;i<all.length;i++){
	    all[i]=all[i].substring(0,all[i].length()-4);
	}
	return all;
    }

    public Properties getProperties(String name){
	//InputStream istream=null;
	//istream=getClass().getResourceAsStream("kacheln/"+name+".spf");
	Properties spfProp=new Properties();
	try{
	    FileInputStream istream=new FileInputStream("kacheln/"+name+".spf");
	    spfProp.load(istream);
	}catch(FileNotFoundException e){
	    System.err.println(e);
	}catch(IOException e){
	    System.err.println(e);
	}
	return spfProp;
    }

    public void saveSpielfeld(Properties spfProp, File file){
	try{
	    //	 URL url=getClass().getResource("kacheln");
	    //File to=new File(name);
	 OutputStream ostream=new FileOutputStream(file);
	 spfProp.store(ostream,null);
	}catch(IOException e){
	    System.err.println(e);
	}

    }

}

class SpfFilter implements FilenameFilter{
    public SpfFilter(){}
    public boolean accept(File dir, String name){
	try{
	    // endsWith(".spf") ???
	    return name.endsWith(".spf");
	} catch(Throwable t){return false;}
    }
}
