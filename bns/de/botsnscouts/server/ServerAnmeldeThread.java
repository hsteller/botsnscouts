package de.botsnscouts.server;

import java.io.*;
import java.net.*;
import de.botsnscouts.util.*;
import de.botsnscouts.comm.*;

/**
 * Handhabt eine Connection nebenlaeufig.
 * modified for 2.0 by Dirk
 */

public class ServerAnmeldeThread extends java.lang.Thread{

    private Server server;
    private Socket socket;
    private ServerAnmeldeOberThread oberThread;

    private static final int ILLEGAL=-1;
    private static final int SPIELER=0;
    private static final int AUSGABE=1;
    private static final int SPIELERV2=2;
    private static final int AUSGABEV2=3;

    public ServerAnmeldeThread(Server se,Socket so,ServerAnmeldeOberThread saot){
	server=se;
	socket=so;
	oberThread=saot;
    }

    /** Wartet server.anmeldeto auf eine Aktion, kreiert ggf. neue ServerRoboterThread-
     *  bzw ServerAusgabeThread-Objekte und haengt diese in die richtigen Vektoren ein.
     */
    public void run(){
	PrintWriter out=null;
	BufferedReader in=null;
	int clienttype=ILLEGAL;
	String clientname="";
	int farbe=-1;
	boolean exception=false;

	try{      // Globales Try fuer's String Einlesen und Parsen.
	    d("out = new PrintWriter    ...");
	    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);

	    d("in  = new BufferedReader ...");
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	    d("... verbunden!");
	    d("schaue nach ob Client etwas geschickt hat.");

	    String erhalten;
	    socket.setSoTimeout(server.anmeldeto);
	    erhalten = in.readLine();
	    d("erhalten = "+erhalten);
	    socket.setSoTimeout(0);

	    int len=erhalten.length();

	    if(len < 6)
		{
		    d("Zeichenkette kleiner 6, z.B. 'RGS(X)' min");
		    throw new FormatException();
		}

	    if(erhalten.charAt(0) == 'R' && erhalten.charAt(1)=='G' && erhalten.charAt(2) == 'S')
		{
		    d("Spieler");
		    clienttype = SPIELER;
		}
	    else if(erhalten.charAt(0) == 'R' && erhalten.charAt(1)=='G' && erhalten.charAt(2)=='A')
		{
		    d("Ausgabekanal");
		    clienttype = AUSGABE;
		}
	    else if(erhalten.charAt(0) == 'R' && erhalten.charAt(1)=='S' && erhalten.charAt(2)=='2')
		{
		    d("Spieler V2");
		    clienttype=SPIELERV2;
		}
	    else if(erhalten.charAt(0) == 'R' && erhalten.charAt(1)=='A' && erhalten.charAt(2)=='2')
                {
		    d("Ausgabe V2");
		    clienttype=AUSGABEV2;
                }
	    else
		{
		    d("Falscher String");
		    throw new FormatException();
		}

	    if(erhalten.charAt(3)=='(' && erhalten.charAt(len-1)==')')
		{
		    for(int n = 4; n<(len-1); n++)
			clientname = clientname+erhalten.charAt(n);
		}
	    else
		{
		    d("Klammerung falsch.");
		    throw new FormatException();
		}

	    // Farbe parsen
	    int kommapos=clientname.indexOf(',');
	    if ((clienttype==SPIELERV2)&&(kommapos!=-1)){
		farbe=java.lang.Character.digit(clientname.charAt(clientname.length()-1),10);
		clientname=clientname.substring(0,clientname.length()-2);
		d("farbe="+farbe+"; clientname="+clientname);
	    }

	    if(!nurLatein(clientname))
		{
		    d("Name darf nur aus <a-z,A-Z>+ bestehen");
		    throw new FormatException();
		}

	    d("Parsen erfolgreich. Clientname = "+clientname);
	} catch (SocketException e){
	    exception=true;
	    d("SocketException ist aufgetreten.");
	} catch (IOException e){
	    exception=true;
	    d("IOException ist aufgetreten.");
	} catch (FormatException e){
	    exception=true;
	    d("FormatException ist aufgetreten.");
	}

	if (exception){
	    if (in != null)
		try{
		    in.close();
		} catch (IOException e){}
	    out.close();
	    return;
	}

	// OK, nun geeignete Objekte kreieren und einsortieren

	if((clienttype==AUSGABE)||(clienttype==AUSGABEV2)){
	    KommServerAusgabe ksa = new KommServerAusgabe(in, out);
	    // sende 'ok' zur anmeldebestaetigung
	    try{
		ksa.anmeldeBestaetigung(true);
	    }catch(KommException ke){
		d("ok konnte nicht an Ausgabekanal gesendet werden");
		return;
	    }
	    ServerAusgabeThread neu = new ServerAusgabeThread(ksa, server);

	    if (clienttype==AUSGABEV2)
		neu.version=2;
	    else
		neu.version=1;

	    synchronized (oberThread.namen){
		if (oberThread.isLegalName(clientname)){
		    oberThread.addName(" ( "+clientname+" ) ");
		    server.addAusgabeThread(neu);
		    d("neuen Ausgabethread erzeugt");
		}
		else {
		    d("Name schon vorhanden. Kille die Verbindung..");
		    out.println("REN(SO(SchonAngemeldeterName))");
		    try{
			in.close();
		    }catch(IOException e){}
		    out.close();
		    return;
		}
	    }
	    return;
	}

	if((clienttype==SPIELER)||(clienttype==SPIELERV2)){
	    // darf ein Spieler sich anmelden?
	    d("Ein Spieler versucht sich an der Anmeldung.");
	    synchronized(oberThread.roboterAnmeldung){
		if (oberThread.roboterAnmeldung==Boolean.FALSE){
		    d("Keine Roboteranmeldungen jetzt. Kille Verbindung");
		    out.println("REN(SO(SpielLaeuftSchon))");
		    try{
			in.close();
		    }catch(IOException e){}
		    out.close();
		    return;
		} //oberThread.roboterAnmeldung false
		d("Er darf jedenfalls, vom Server aus.");

		synchronized(oberThread.namen){
		    if (!oberThread.isLegalName(clientname)){
			d("Name schon vergeben. Kille Verbindung");
			out.println("REN(SO(SchonVergebenerName))");
			try{
			    in.close();
			}catch(IOException e){}
			out.close();
			return;
		    } //Name illegal
		    d("Der Name ist jedenfalls noch nicht vergeben.");

		    synchronized(oberThread.anzSpieler){
			if (oberThread.anzSpieler.intValue()==server.anzSpieler){
			    d("Zuviele Spieler. Kille Verbindung");
			    out.println("REN(ZS)");
			    try{
				in.close();
			    }catch(IOException e){}
			    out.close();
			    return;
			} // Zuviele Spieler
			d("Noch nicht zuviele Spieler.");

			// Farbe festlegen und eintragen
			synchronized(server.angemeldet){
			    if ((farbe>0)&&(server.angemeldet[farbe-1]==null))
				farbe--;
			    else{
				farbe=(int)(Math.random()*7+1);
				while (server.angemeldet[farbe]!=null)
				    farbe=(farbe+1)%8;
			    }
			    server.angemeldet[farbe]=clientname;
			} //synchronized server.angemeldet
			d("Farbe Nr. "+farbe+" zugeteilt.");

			Roboter h=Roboter.getNewInstance(clientname);
			h.setBotVis(farbe);
			KommServerRoboter komm = new KommServerRoboter(in,out);
			try{
			    komm.anmeldeBestaetigung(true);
			}catch(KommException ke){
			    d("ok konnte nicht an roboter gesendet werden");
			    return;
			}
			d("ok an Spieler geschickt.");

			oberThread.anzSpieler=new Integer(oberThread.anzSpieler.intValue()+1);
			d(""+oberThread.anzSpieler+". Roboter mit Name "+clientname+" erzeugt.");

			ServerRoboterThread neu=new ServerRoboterThread(h,server,komm);
			server.addRoboterThread(neu);
			d("ServerRoboterThread erzeugt und einsortiert.");
			oberThread.addName(" "+clientname+" ");
			server.startServer.neuerSpieler(clientname,farbe,server);

			if (oberThread.anzSpieler.intValue()>=server.anzSpieler){ // alle da
			    try {
				sleep(5000);
			    }
			    catch(InterruptedException ex) {
				d("InterruptedException "+ex);
			    }
			    synchronized (server){
				d("server.notify()");
				server.notify();
			    }
			} // if maxspieler angemeldet
		    } // synchronized oberThread.anzSpieler
		} // synchronized oberThread.namen
	    } // synchronized oberThread.roboterAnmeldung
	} //clienttype==spieler
    } //run

    private boolean nurLatein(String s)
    {
	if (s==null)
	    return false;
	else
            {
                int l=s.length();
                String su=new String(s.toUpperCase());
                for (int i=0;i<l;i++)
		    if ( (su.charAt(i)>'Z' )||( su.charAt(i)<'A') )
			return false;
                return true;
            }
    }


    private void d(String a){
        Global.debug(this,a);
    }
}
