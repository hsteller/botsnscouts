package de.botsnscouts.server;

import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;

class ServerAusgabeThread extends Thread implements Waitable
{
    private InfoRequestAnswerer info;
    private OKListener ok;
    private ServerOutputThreadMaintainer outMaint;
    private KommServerAusgabe komm;
    private boolean ende;
    private int mode;
    //boolean fertig;
    private float version;

    synchronized void setMode(int i){
	mode=i;
    }

    synchronized void setVersion(float v){
	version=v;
    }

    public ServerAusgabeThread(KommServerAusgabe ksa, OKListener okL, InfoRequestAnswerer info, ServerOutputThreadMaintainer m)
        {
            ok=okL;
	    this.info=info;
	    outMaint=m;
            komm=ksa;
            mode=Server.KEINEFRAGEN; // sicher ist sicher
        }

    private void notifyServer(){
	ok.notifyDone(this);
    }

    public void run()
        {
            ende=false;
            ServerAntwort ans;
          outer: while((!ende)&&(!isInterrupted())){
              try{
                  ans=komm.warte();
                  synchronized(this){
                      int m=mode;
                      if (m==Server.KEINEFRAGEN){ // dumm gelaufen...
			  if (ans.typ!=ans.ABMELDUNG){
			      d("Keine Fragen erlaubt. Ausgabe entstoepseln."+this);
			      outMaint.deleteOutput(this,"RV");
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
                              komm.sendSpielfeldDim(info.getFieldSizeX(),info.getFieldSizeY());
                              break;

                          case ServerAntwort.GIBSPIELFELD:
                              komm.sendSpielfeld(info.getFieldString());
                              break;

                          case ServerAntwort.GIBFAHNENPOS:
                              komm.sendFahnenpos(info.getFlags());
                              break;

                          case ServerAntwort.GIBNAMEN:
			      komm.sendNamen(info.getNames());
                              break;

                          case ServerAntwort.GIBROBOTERPOS:
			      Ort o=info.getRobPos(ans.name);
			      if (o!=null)
				  komm.sendRobpos(o);
                              else{
                                  outMaint.deleteOutput(this,"RV");
                                  ende=true;
                              }
                              break;

                          case ServerAntwort.GIBROBSTATUS:
                              Roboter r = info.getRobStatus(ans.name);
                              if(r != null){
                                  komm.sendRobStatus(r);
                              }else{
                                  komm.sendRobStatus();
                              }
                              break;

                          case ServerAntwort.GIBSPIELSTAND:
                              if(info.gameRunning()){
                                  komm.spielstand(new Boolean(true), info.getStanding());
                              }else{
                                  komm.spielstand(new Boolean(false), info.getStanding());
				  notifyServer();
                              }
                              break;

                          case ServerAntwort.GIBAUSWERTUNGSSTATUS:
                              d("in GibAuswertungsStatus");
                              komm.spielStatus(info.getEvalStatus());
                              break;

		          case ServerAntwort.GIBTIMEOUT:
                              komm.sendTimeOut(outMaint.getOutputTimeout()/1000);
                              break;
		          case ServerAntwort.GIBFARBEN:
			      komm.sendFarben(info.getNamesByColor());
		              break;
		          case ServerAntwort.STATS:
			      komm.sendStats(info.getStats());
			      break;

		          case ServerAntwort.ABMELDUNG:
			      ende=true;
			      notifyServer();
			      return;

		      default:
                              d("Erhielt einen Typ "+ans.typ+"; ich weiss nicht was ich damit soll und entstoepsle mich.");
                              outMaint.deleteOutput(this,"RV");
                              notifyServer();
                              break;
                      } // switch
                  } // synchronized modus
              } //try
              catch (KommException e){
                  d("KommException ist aufgetreten. Beende mich.");
                  d("Message: "+e.getMessage());
                  outMaint.deleteOutput(this,"RV");
                  ende=true;
              }
          } //"Endlos"schleife
	    d("Ende meiner run-Methode erreicht.");
        } // run()

    private void d(String s)
        {
            Global.debug(this,s);
        }

    // comm delegation
    synchronized void notifyChange(String[] s) throws KommException{
	komm.aenderung(s);
    }
    synchronized void deleteMe(String reason) throws KommException{
	komm.entfernen(reason);
    }
    synchronized void startGame() throws KommException{
	komm.spielstart();
    }
    synchronized void endGame() throws java.io.IOException{
	komm.in.close();
	komm.out.close();
    }

}
