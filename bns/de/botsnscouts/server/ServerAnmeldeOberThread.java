package de.botsnscouts.server;

import java.net.*;
import java.io.*;
import de.botsnscouts.util.Global;

import org.apache.log4j.Category;

/** Erlaubt die nebenlaeufige Anmeldung von Robotern und Ausgaben 
 *  Startet fuer jeden Anmeldeversuch einen ServerAnmeldeThread.
*/

class ServerAnmeldeOberThread extends Thread
{
    static final Category CAT = Category.getInstance( ServerAnmeldeOberThread.class );

    Server server;
    ServerSocket seso;
    Boolean roboterAnmeldung;
    String namen="";
    Integer anzSpieler;

    public ServerAnmeldeOberThread(Server s)
    {
        super("ServerAnmObThread");
	server=s;
	roboterAnmeldung=Boolean.TRUE;
	anzSpieler=new Integer(0);
    }    

    private void d(String s)
    {
	Global.debug(this,s);
    }

    public void run()
    {
	try{
	    try{
		seso=new ServerSocket(server.anmeldePort);
	    } catch (IOException e){
		System.err.println("ServerAnmeldeOberThread: Konnte Socket nicht binden. Beende mich.");
		return;
	    }
	
	    try{
		seso.setSoTimeout(0); // wir warten bis zum notify()
	    } catch (SocketException e){
		System.err.println("ServerAnmeldeOberThread: SocketExc beim Setzen des TO. Beende mich.");
		try {
		    seso.close();
		} catch (IOException f){}
		return;
	    }
	
	    Socket client;

	    while(!isInterrupted()){
		try{
		    client = seso.accept();
		}catch(IOException e){
		    try{
			seso.close();
		    }catch(IOException f){}
		    System.err.println("ServerAnmeldeOberThread: Fehler bei ServerSocket.accept(). Beende mich.");
		    return;
		}
		ServerAnmeldeThread handhaber=new ServerAnmeldeThread(server,client,this);
		d("Neuer Client! Starte ServerAnmeldeThread...");
		handhaber.start();
	    }
	} catch( Throwable t ) {
	    CAT.fatal("Exception:", t);
	}
    }
    
    /** Registriert Namen als benutzt - soll mit isLegalName() benutzt werden */
    protected void addName(String s){
	d("Addiere "+s+" zu namen hinzu.");
	d("Namen: "+namen);
	namen+=s;
    }

    /** True falls der Name noch unbenutzt ist */
    protected boolean isLegalName(String s){
	d("Teste "+s+" in Namen.");
	d("Namen: "+namen);
	return (namen.indexOf(" "+s+" ")==-1);
    }
}

