package de.botsnscouts.start;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import de.botsnscouts.autobot.*;
import org.apache.log4j.Category;

// launches human player, output ...

public class Launcher{

    static final Category CAT = Category.getInstance( Launcher.class );
    // launches output
    public Thread einemSpielZuschauen(String ip, int port, boolean noSplash){
	Thread ret;
	try{
	    ret=new Thread(new Ausgabe(ip, port,null,noSplash));
	    ret.start();
	}catch(Exception exp){
	    return null;
	}
	return ret;
    }

    // launches human player
    public Thread amSpielTeilnehmen(String ip, int port, String name, int farbe, boolean noSplash){
	Thread ret;
	try {
	    if( CAT.isDebugEnabled() ) CAT.debug("Trying to start human player...");
	    ret=(Thread) new HumanPlayer(ip,port,name,farbe,noSplash);
	    ret.start();
	} catch (Exception u){
	    return null;
	}
	return ret;
    }

    // launch autobots
    public Thread  kuenstlicheSpielerStarten(String ip, int port, boolean local, int iq, KommSpPr com){
	if (local){
	    Thread ks;
	    ks = new SpielerKuenstlich(ip,port,iq);
	    ks.start();
	    ks.setPriority(java.lang.Thread.MIN_PRIORITY);
	    return ks;
	}else{
	    if(com.newKS(ip,port,1,iq))
		return null;
	    else
		return null;
	}
    }


}
