package de.botsnscouts.server;

import de.botsnscouts.util.*;
import de.botsnscouts.comm.*;

public class ServerRoboterThread extends Thread
{
    Integer modus;
    int m;             // Modus w�hrend der Auswertung einer Antwort
    boolean fertig;
    
    private Server server;
    private ServerAntwort ans;
    protected KommServerRoboter komm;
    protected Roboter rob;

    private boolean ende;
    
    public ServerRoboterThread(Roboter r,Server s,KommServerRoboter k)
        {
            rob=r;
            server=s;
	    modus = new Integer(s.NIX);
	    komm=k;
        }

    private void notifyServer(){
	d("notifyServer()");
	fertig=true;
	if (server.alleRoboterFertig(true))
	    synchronized(server){
		d(this+" server.notify() in modus "+modus);
		server.notify();
	    }
    }	

    public void run()
        {
            ende=false;
	    d("starting up.");
            while((!ende)&&(!isInterrupted())){
                try{
                    ans=komm.warte();
		    d("Got a "+ans.getTyp());
                    synchronized(modus){
                        int m=modus.intValue();
                        switch(ans.typ){
                            case ServerAntwort.PROGRAMMIERUNG:
                                if (m!=server.PROGRAMMIERUNG){
                                    d("RV: habe Programmierung im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }

                                rob.setNaechsteRundeDeaktiviert(ans.ok); // PowerDown?
                                
                                d("Setze Programmierung um.");
                                // Zug loeschen
                                for (int i=0;i<5;i++)
                                    rob.setZug(i,null);
                                
                                // gesperrte Register kopieren
                                for (int i=0;i<5;i++)
                                    if (rob.getGesperrteRegister(i)!=null)
                                        rob.setZug(i,rob.getGesperrteRegister(i));

                                // Programmierung umsetzen
                                int ansidx=0;
                                for (int i=0;i<5;i++)
                                    if (rob.getZug(i)==null)
                                        rob.setZug(i,rob.getKarten()[ans.register[ansidx++]-1]);
                                
                                d("Programmierung umgesetzt. Roboter:");
                                rob.zeige_Roboter();
                                
				notifyServer();

                                break;
                            
                            case ServerAntwort.AUSRICHTUNG:
                                if ((m!=server.INITAUSR)&&(m!=server.ZERSTOERT_SYNC)&&(m!=server.ZERSTOERT_ASYNC)){
                                    d("RV: habe Ausrichtung im Modus"+m+"erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    //return;
                                }
                                rob.setAusrichtung(ans.wohin);
                                if (m==server.ZERSTOERT_SYNC || m==server.ZERSTOERT_ASYNC){
				    // Implizit synchronized
				    server.wiederEintritt(this);

				    if (m==server.ZERSTOERT_SYNC)
					notifyServer();
					
                                } else { // INITAUSRICHTUNG
				    notifyServer();
                                }
                                break;
                                
                            case ServerAntwort.REAKTIVIERUNG:
                                if (m!=server.POWERUP){
                                    d("RV: habe Reaktivierung im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                                
                                rob.setAktiviert(ans.ok);
                                
				notifyServer();
                                break;
                                
                            case ServerAntwort.REPARATUR: 
                                if (m!=server.ENTSPERREN){
                                    d("RV: habe Reperatur im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                                
                                for (int i=0;i<ans.register.length;i++)
                                    rob.sperreRegister(ans.register[i]-1, null);
                                
				notifyServer();

                                break;
                                
                            case ServerAntwort.ABMELDUNG:
				d("Abmeldung. Modus: "+m);
                                ende=true;
				if ((m==server.PROGRAMMIERUNG)||(m==server.INITAUSR)||(m==server.ZERSTOERT_SYNC)||(m==server.POWERUP)||(m==server.ENTSPERREN)||(m==server.SPIELSTART)||(m==server.SPIELENDE))
				    notifyServer();
				
                                return;
                            
                            case ServerAntwort.AENDERUNGFERTIG:
                                if ((m!=server.SPIELSTART)&&(m!=server.SPIELENDE)){
                                    d("RV: habe OK im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }

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
                                if ((m==server.SPIELSTART)||(m==server.SPIELENDE)||(m==server.NIX)){
                                    d("RV: habe GibRoboterPos im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                                if (((m==server.ZERSTOERT_SYNC)||(m==server.ZERSTOERT_ASYNC))&&(!ans.name.equals(rob.getName()))){
                                    d("RV: habe GibRoboterPos fuer nicht-mich im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }

                                d("Gibroboterpos fuer "+ans.name+" erhalten.");

				Ort o=server.gibRobPos(ans.name);
				if (o!=null)
				    komm.sendRobpos(o);

                                else{
				    d("RV: gibRoboterPos irgendwie unter falschen Voraussetzungen erhalten");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                }
                    
                                break;
                            
                            case ServerAntwort.GIBROBSTATUS:
                            if ((m==server.SPIELSTART)||(m==server.SPIELENDE)||(m==server.NIX)){
                                    d("RV: habe GibRobStatus im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                                if (((m==server.ZERSTOERT_SYNC)||(m==server.ZERSTOERT_ASYNC))&&(!ans.name.equals(rob.getName()))){
                                    d("RV: habe GibRobStatus fuer nicht-mich im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                            
                                Roboter r;
                                r = server.roboterStatus(ans.name);
                                if(r.getName().compareTo("")!=0){
                                    komm.sendRobStatus(r);
                                }else{ 
                                    komm.sendRobStatus(); 
				}
                                break;

                            case ServerAntwort.GIBSPIELSTAND:
				if(server.spiellaeuft()){
                                    komm.spielstand(new Boolean(true),  server.auswertung());
                                }else
                                    komm.spielstand(new Boolean(false), server.auswertung());
                                break;
                            
                            case ServerAntwort.GIBAUSWERTUNGSSTATUS:
				komm.spielStatus(server.gibAuswertungsStatus());
                                break;
                            
                            case ServerAntwort.GIBTIMEOUT:
				komm.sendTimeOut(server.zugto/1000); 
                                break;
			    case ServerAntwort.GIBFARBEN:	
                                komm.sendFarben(server.angemeldet);
				break;
			     case ServerAntwort.MESSAGE:
				 String[] tmp=new String[ans.msg.length-1];
				 for (int k=1;k<ans.msg.length;k++)
				     tmp[k-1]=ans.msg[k];
				 server.ausgabenMsg(ans.msg[0],tmp);
				 break;
                        } //switch
                    } //synchronized modus
                } //try
		catch (KommFutschException e){
		    d("KommFutschException aufgetreten. Beende mich.");
		    ende=true;
		}
                catch (KommException e){
                    d("RV: KommException ist aufgetreten:("+e+") Beende mich.");
                    server.roboterHinrichten(this,"RV");
                    ende=true;
                }
            } //Endlosschleife
	    d("Ende meiner run()-methode erreicht.");
        } // run() 

    private void d(String s)
        {
            Global.debug(this," for "+rob.getName()+": "+s);
        }
} // class
