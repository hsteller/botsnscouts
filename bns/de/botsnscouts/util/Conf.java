package de.botsnscouts.util;

import org.apache.log4j.*;
import java.util.*;
import java.io.*;

/** Used to access configuration that users may change */
public class Conf{
    public static final Category CAT = Category.getInstance( de.botsnscouts.BotsNScouts.class );

    private static final String CONFNAME="bns.config";
    private static final String bnsHome;
    private static Properties properties;
    
    /**
     * Used to access the bns installation directory.
     * @return a <code>String</code> value representing the absolute path there.
     */
    public static String getBnsHome(){
	return bnsHome;
    }

    public static String getProperty(String key){
	String data=System.getProperty(key);
	if (data==null)
	    data=properties.getProperty(key);
	return data;
    }

    public static int getIntProperty(String key){
	String data=getProperty(key);
	int ret=-1;
	try{
	    ret=Integer.parseInt(data);
	}catch(Exception e){}
	return ret;
    }

    public static void setProperty(String key, String data){
	properties.setProperty(key, data);
    }

    public static void saveProperties(){
	try{
	    File file=new File(bnsHome+System.getProperty("file.separator")+CONFNAME);
	    OutputStream ostream=new FileOutputStream(file);
	    properties.store(ostream,null);
	}catch(IOException e){
	    CAT.info("Save of user-defined bns.config failed.");
	}
    }

    static {
	// Set bnsHome: if it's explicitly set, we take that
	String s=System.getProperty("bns.home");
	if (s==null) // probably windows, use current working dir
	       s=System.getProperty("user.dir");
	bnsHome=s;
	CAT.debug("bnsHome: "+bnsHome);

	// Set the properties: a) the default one
	properties=new Properties();
	try{
	    InputStream in = de.botsnscouts.BotsNScouts.class.getResourceAsStream("conf/"+CONFNAME);
	    if (in == null){
		CAT.fatal("default bns.config not found.");
		throw new RuntimeException("default bns.config not found");
	    }
	    properties.load(in);
	    CAT.debug("defautl bns.config loaded.");
	}catch (IOException e){
	    CAT.fatal("default bns.config not found.");
	    throw new RuntimeException("default bns.config not found");
	}
	// b) a user-defined one overrides that
	try{
	    Properties p=new Properties(properties);
	    p.load(new FileInputStream(bnsHome+System.getProperty("file.separator")+CONFNAME));
	    properties=p;
	    CAT.debug("user-defined bns.config loaded.");
	}catch(IOException e){
	    CAT.debug("no user-defined bns.config found.");
	}
	// c) System-wide properties override that -- see getProperty
    }
}
