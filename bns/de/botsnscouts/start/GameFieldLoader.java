package de.botsnscouts.start;

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.*;
import de.botsnscouts.BotsNScouts;

class GameFieldLoader{

    private static final Category CAT = Category.getInstance( GameFieldLoader.class );
    private static HashSet distSpf = new HashSet(); // The spfs from the distribution

    public String[] getSpielfelder(){
 	File kd=new File("kacheln");
	String[] part1 = kd.list(new SpfFilter());
	for (int i=0;i<part1.length;i++){
	    part1[i]=part1[i].substring(0,part1[i].length()-4);
	}

	// Load those from the distribution
	InputStream stream = BotsNScouts.class.getResourceAsStream("tiles/tile.index");
	if (stream==null){
	    CAT.warn("Couldn't find tiles/tile.index");
	    return part1;
	}
	Properties prop=new Properties();
	try{
	    prop.load(stream);
	}catch(IOException e){
	    CAT.warn("Couldn't load tile.index from distrib.");
	    return part1;
	}
	int numSpf=0;
	try{
	    numSpf=Integer.parseInt(prop.getProperty("numSpf"));
	}catch(NumberFormatException e){
	    CAT.warn("Error parsing numSpf in tile.index!");
	}
	if (numSpf==0) // none in distribution
	    return part1;

	String[] all=new String[part1.length+numSpf];

	for (int i=0;i<numSpf;i++){
	    String name=prop.getProperty("spf"+i);
	    all[i]=name;
	    distSpf.add(name);
	}
	for (int i=numSpf;i<all.length;i++)
	    all[i]=part1[i-numSpf];

	Arrays.sort(all);
	return all;
    }

    public Properties getProperties(String name){
	InputStream istream=null;

	// Is it from the distribution?
	if (distSpf.contains(name)){
	    istream=BotsNScouts.class.getResourceAsStream("tiles/"+name+".spf");
	    if (istream==null){
		CAT.warn("Error loading spf "+name+" from distribution.");
		return null;
	    }
	}else{
	    try{
		istream=new FileInputStream("kacheln"+System.getProperty("file.separator")+name+".spf");
	    }catch(IOException e){
		CAT.warn("Error loading spf "+name+" from user-def");
		return null;
	    }
	}

	Properties spfProp=new Properties();
	try{
	    spfProp.load(istream);
	}catch(IOException e){
	    CAT.warn("Error loading spf "+name);
	    return null;
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
