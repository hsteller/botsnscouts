package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;
import de.botsnscouts.util.*;

class GameFieldLoader{
    public String[] getSpielfelder(){
 	File kd=new File("kacheln");
	String[] all = kd.list(new SpfFilter());
	for (int i=0;i<all.length;i++){
	    all[i]=all[i].substring(0,all[i].length()-4);
	}
	Arrays.sort(all);
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
	    //	    System.err.println(e);
	    return null;
	}catch(IOException e){
	    System.err.println(e);
	}
	return spfProp;
    }

    public void saveSpielfeld(Properties spfProp, File file){
	try{
	 OutputStream ostream=new FileOutputStream(file);
	 spfProp.store(ostream,null);
	}catch(IOException e){
	    System.err.println(e);
	}

    }

}
