package de.botsnscouts.start;
import de.botsnscouts.util.*;


public class WaiterThread extends BNSThread{
    Thread[] ks=new Thread[100];
    int ksanz=0;
//    Start parent;
    boolean meldung=true;
    boolean beendemich=false;

    boolean waitServer=false;

//    public WaiterThread(Start par){
//	parent=par;
//    }

    public WaiterThread(Object o){
        super("WaiterThread");
    }

    public void run(){
	if(ks==null||ksanz==0){
	    Global.debug(this,"habe keine threads! beende mich");
	    return;
	}

        Global.debug(this,"starte mich und warte auf "+ksanz+" threads");
	for(int i=0;i<ksanz;i++){
	    try{
		ks[i].join();
	    }catch(InterruptedException ex){
		System.err.println("Interrupted while waiting for Threads!");
	    }
	}
	if (waitServer){
	    Global.debug(this,"gonna wait for Server");
	    while(!beendemich){
		try{
		    sleep(3000);
		}catch(InterruptedException ex){
		    System.err.println("Interrupted while waiting for Server!");
		}
	    }
	}
	Global.debug(this,"in 5 sec. beende alles!");
	try{
	    sleep(5000);
	}catch(InterruptedException ex){
	    System.err.println("Interrupted while waiting for Threads!");
	}
 	if(meldung){
 	    javax.swing.JOptionPane.showMessageDialog(null,Message.say("Start","mBeendeAlles"),Message.say("Start","mMeldung"),javax.swing.JOptionPane.INFORMATION_MESSAGE);
 	}

        // enno: wozu das alles, wenn wir sowieso system.exit(0) machen?
//	parent.fassade.killStartServer();
//	parent.fassade=null;
//	parent.dispose();
	System.exit(0);
    }

    public void addThread(Thread th){
	if(ksanz>=ks.length){
	    ks=new Thread[ks.length*2];
	}
	ks[ksanz++]=th;
    }

    public void setMeldung(boolean m){
	meldung=m;
    }

    public void reset(){
	ksanz=0;
	ks=new Thread[100];
	waitServer=false;
    }

    public void setServer(){
	waitServer=true;
    }

    public void beende(){
	beendemich=true;
    }

}


