package de.botsnscouts.server;

import java.util.Vector;
import java.util.Iterator;

class MessageThread extends Thread
    implements MOKListener{

    private WaitingForSet wait;
    private ThreadMaintainer server;
    private MessageQ msgQ;
    private int timeout;

    public MessageThread(ThreadMaintainer m, int timeout){
	super("MessageThread");
	server=m;
	msgQ=new MessageQ();
	this.timeout=timeout;
    }

    /** Mandated by MOKListener, removes from WaitingForSet */
    public void notifyDone(Waitable me){
	wait.removeAndNotify(me);
    }

    /** anyone may append messages here */
    public void append(String id, String[] args){
	msgQ.addMsg(id,args);
    }

    private void sendMsg(Msg msg){
	Vector v=server.getActiveOutputs();
	// Synchronization between MessageThread and ServerThread: whenever one of
	// them wishes to communicate with the Outputs, it synchronizes on the 
	// Vector that contains them all.
	d("Sending msg: "+msg.id);
	synchronized(v){
	    d("Got lock");
	    wait = new WaitingForSet(v);
	    for (Iterator it=v.iterator();it.hasNext();){
		ServerAusgabeThread tmp=(ServerAusgabeThread)it.next();
		if (!tmp.isAlive())
		    it.remove();
		else
		    tmp.sendMsg(msg.id,msg.args);
	    }
	    d("now starting wait");
	    Iterator it=wait.waitFor(timeout);
	    d("end of wait");
	    while(it.hasNext())
		server.deleteOutput((ServerAusgabeThread)it.next(),"TO");
	}
	d("released lock");
    }

    public void run(){
	try{
	    while (!isInterrupted()){
		sendMsg(msgQ.getMsg());
	    }
	}catch(InterruptedException e){
	    de.botsnscouts.util.Global.debug("Interrupted. End of run().");
	}
    }

    /** Just a struct, really. */
    private class Msg{
	String id;
	String[] args;
    }

    /** Monitor that stores and retrieves Messages */
    private class MessageQ{
	private Vector ids;
	private Vector argss;
	private Msg msg;
	public MessageQ(){
	    ids=new Vector();
	    argss=new Vector();
	    msg=new Msg();
	}
	public synchronized void addMsg(String id, String[] args){
	    ids.add(id);
	    argss.add(args);
	    notifyAll();
	}
	public synchronized Msg getMsg() throws InterruptedException{
	    while (ids.size()==0)
		wait();
	    msg.id=(String)ids.remove(0);
	    msg.args=(String[])argss.remove(0);
	    return msg;
	}
    }
    
    private void d(String s){
	de.botsnscouts.util.Global.debug(this,s);
    }
}
