package de.botsnscouts.start;

// startet SpielerMensch, AusgabeFrame u.s.w.

public class Launcher{

    // startet ein AusgabeKanal
    public Thread einemSpielZuschauen(String ip, int port, boolean noSplash){
	Thread ret;
	try{
	    ret=new Thread(new AusgabeFrame(ip, port,null,noSplash));
	    ret.start();
	}catch(Exception exp){
	    return null;
	}
	return ret;
    }

    // startet einen SpielerMensch
    public Thread amSpielTeilnehmen(String ip, int port, String name, int farbe, boolean noSplash){
	Thread ret;
	try {
	    ret=(Thread) new SpielerMensch(ip,port,name,farbe,noSplash);
	    ret.start();
	} catch (Exception u){
	    return null;
	}
	return ret;
    }

    // startet Künstliche Spieler
    public Thread  kuenstlicheSpielerStarten(String ip, int port, boolean local, int iq, KommSpPr com){
	if (local){
	    Thread ks;
	    //for (int i=0;i<anzahl;i++){
	    ks = new SpielerKuenstlich(ip,port,iq);
	    ks.start();
	    ks.setPriority(java.lang.Thread.MIN_PRIORITY);
		//}
	    return ks;
	}else{
	    if(com.newKS(ip,port,1,iq))
		return null;
	    else
		return null;
	}
	//	return true;
    }


}
