package de.botsnscouts.server;

import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;

class ServerAusgabeThread extends Thread
{
    Server server;
    KommServerAusgabe komm;
    private boolean ende;
    Integer modus;
    boolean fertig;
    float version;

    public ServerAusgabeThread(KommServerAusgabe ksa, Server s)
        {
            server=s;
            komm=ksa;
            modus=new Integer(s.KEINEFRAGEN); // sicher ist sicher
        }

    private void notifyServer(){
	fertig = true;
	if (server.alleAusgabenFertig()){
	    synchronized(server){
		d("ServerAusgabenThread macht notify() bei Aenderungenfertig");
		server.notify();
	    }
	}
    }

    public void run()
        {
            ende=false;
            ServerAntwort ans;
          outer: while((!ende)&&(!isInterrupted())){
              try{
                  ans=komm.warte();
                  synchronized(modus){
                      int m=modus.intValue();
                      if (m==server.KEINEFRAGEN){ // dumm gelaufen...
			  if (ans.typ!=ans.ABMELDUNG){
			      d("Keine Fragen erlaubt. Ausgabe entstoepseln."+this);
			      server.ausgabeEntstoepseln(this,"RV");
			      break outer;
			  } else
			      return;
                      }

                      switch (ans.typ){
                          case ServerAntwort.AENDERUNGFERTIG:
                              d("Aenderungfertig erhalten.");
			      notifyServer();
                              break;

                          case ServerAntwort.GIBSPIELFELDDIM:
                              komm.sendSpielfeldDim(server.feld.getSizeX(),server.feld.getSizeY());
                              break;

                          case ServerAntwort.GIBSPIELFELD:
                              komm.sendSpielfeld(server.feld.getSpielfeldString());
                              break;

                          case ServerAntwort.GIBFAHNENPOS:
                              komm.sendFahnenpos(server.feld.getFlaggen());
                              break;

                          case ServerAntwort.GIBNAMEN:
			      komm.sendNamen(server.gibNamen());
                              break;

                          case ServerAntwort.GIBROBOTERPOS:
			      Ort o=server.gibRobPos(ans.name);
			      if (o!=null)
				  komm.sendRobpos(o);
                              else{
                                  server.ausgabeEntstoepseln(this,"RV");
                                  ende=true;
                              }
                              break;

                          case ServerAntwort.GIBROBSTATUS:
                              Roboter r = server.roboterStatus(ans.name);
                              if(r != null){
                                  komm.sendRobStatus(r);
                              }else{
                                  komm.sendRobStatus();
                              }
                              break;

                          case ServerAntwort.GIBSPIELSTAND:
                              if(server.spiellaeuft()){
                                  komm.spielstand(new Boolean(true),  server.auswertung());
                              }else{
                                  komm.spielstand(new Boolean(false), server.auswertung());
				  notifyServer();
                              }
                              break;

                          case ServerAntwort.GIBAUSWERTUNGSSTATUS:
                              d("in GibAuswertungsStatus");
                              komm.spielStatus(server.gibAuswertungsStatus());
                              break;

		          case ServerAntwort.GIBTIMEOUT:
                              komm.sendTimeOut(server.zugto/1000);
                              break;
		          case ServerAntwort.GIBFARBEN:
			      komm.sendFarben(server.angemeldet);
		              break;
		          case ServerAntwort.STATS:
			      komm.sendStats(server.stats);
			      break;

		          case ServerAntwort.ABMELDUNG:
			      ende=true;
			      notifyServer();
			      return;

		      default:
                              d("Erhielt einen Typ "+ans.typ+"; ich weiss nicht was ich damit soll und entstoepsle mich.");
                              server.ausgabeEntstoepseln(this,"RV");
                              notifyServer();
                              break;
                      } // switch
                  } // synchronized modus
              } //try
              catch (KommException e){
                  d("KommException ist aufgetreten. Beende mich.");
                  d("Message: "+e.getMessage());
                  server.ausgabeEntstoepseln(this,"RV");
                  ende=true;
              }
          } //"Endlos"schleife
        } // run()

    private void d(String s)
        {
            Global.debug(this,s);
        }
}
