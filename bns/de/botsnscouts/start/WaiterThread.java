package de.botsnscouts.start;
import de.botsnscouts.util.*;


public class WaiterThread extends Thread{
    Thread[] ks=new Thread[100];
    int ksanz=0;
    Start parent;
    boolean meldung=true;
    boolean beendemich=false;

    public WaiterThread(Start par){
	parent=par;
    }

    public void run(){
	if(ks==null||ksanz==0){
	    Global.debug(this,"habe keine threads! beende mich");
	    return;
	}

        Global.debug(this,"starte mich und warte auf "+ksanz+" threads");
	for(int i=0;i<ksanz;i++){
	    try{
		while(ks[i]!=null&&ks[i].isAlive()){
		    for(int j=0;j<ksanz;j++){
			if(ks[j]!=null){
			    //Global.debug(this,i+" habe "+ksanz+" threads; "+j+":"+ks[j]+" isAlive: "+ks[j].isAlive());
			}
			else{
			    //Global.debug(this,i+" habe "+ksanz+" threads; "+j+":"+ks[j]+" ist null");
			}
		    }
		    Thread.sleep(5000);
		}
	    }catch(InterruptedException ex){
		System.err.println("Interrupted while waiting for Threads!");
	    }
	}

	for(int j=0;j<ksanz;j++){
	    if(ks[j]!=null){
		//Global.debug(this,"habe "+ksanz+" threads; "+j+":"+ks[j]+" isAlive: "+ks[j].isAlive());
	    }
	    else{
		//Global.debug(this,"habe "+ksanz+" threads; "+j+":"+ks[j]+" ist null");
	    }
	}
	Global.debug(this,"gonna wait for Server");
	while(!beendemich){
	    try{
		sleep(3000);
	    }catch(InterruptedException ex){
		System.err.println("Interrupted while waiting for Server!");
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

	parent.fassade.killStartServer();
	parent.fassade=null;
	parent.dispose();
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

    public void beende(){
	beendemich=true;
    }

}


