package de.botsnscouts.server;

import de.botsnscouts.util.*;
import de.botsnscouts.comm.*;

public class ServerRoboterThread extends Thread implements Waitable
{
    //Integer modus;
    int mode;
    //boolean fertig;
    
    private OKListener okListener;
    private InfoRequestAnswerer info;
    private ServerRobotThreadMaintainer robMaint;
    private ServerAntwort ans;
    private KommServerRoboter komm;
    protected Roboter rob;

    private boolean ende;
    
    public ServerRoboterThread(Roboter r, OKListener ok, InfoRequestAnswerer inf, ServerRobotThreadMaintainer maint,KommServerRoboter k)
        {
	    super(r.getName());
            rob=r;
            okListener=ok;
	    info=inf;
	    robMaint=maint;
	    mode = Server.NIX;
	    komm=k;
        }

    synchronized void setMode(int i){
	mode=i;
    }
    
    private void notifyServer(){
	okListener.notifyDone(this);
    }

    public void run()
        {
            ende=false;
	    d("starting up.");
            while((!ende)&&(!isInterrupted())){
                try{
                    ans=komm.warte();
                    synchronized(this){
			d("Got a "+ans.getTyp());
                        int m=mode;
                        switch(ans.typ){
                            case ServerAntwort.PROGRAMMIERUNG:
                                if (m!=Server.PROGRAMMIERUNG){
                                    d("RV: habe Programmierung im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }

                                rob.setNaechsteRundeDeaktiviert(ans.ok); // PowerDown?
                                
                                d("Setze Programmierung um.");
				rob.program(ans.register);
                                d("Programmierung umgesetzt. Roboter:");
                                rob.zeige_Roboter();
                                
				notifyServer();

                                break;
                            
                            case ServerAntwort.AUSRICHTUNG:
                                if ((m!=Server.INITAUSR)&&(m!=Server.ZERSTOERT_SYNC)&&(m!=Server.ZERSTOERT_ASYNC)){
                                    d("RV: habe Ausrichtung im Modus"+m+"erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
				    break;
                                }
                                rob.setAusrichtung(ans.wohin);
                                if (m==Server.ZERSTOERT_SYNC || m==Server.ZERSTOERT_ASYNC){
				    // Implizit synchronized
				    robMaint.reEntry(this);

				    if (m==Server.ZERSTOERT_SYNC)
					notifyServer();
					
                                } else { // INITAUSRICHTUNG
				    notifyServer();
                                }
                                break;
                                
                            case ServerAntwort.REAKTIVIERUNG:
                                if (m!=Server.POWERUP){
                                    d("RV: habe Reaktivierung im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    break;
                                }
                                
                                rob.setAktiviert(ans.ok);
                                
				notifyServer();
                                break;
                                
                            case ServerAntwort.REPARATUR: 
                                if (m!=Server.ENTSPERREN){
                                    d("RV: habe Reperatur im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
				    break;
                                }
                                
                                for (int i=0;i<ans.register.length;i++)
				    rob.entsperreReg(ans.register[i]-1);
                                
				notifyServer();

                                break;
                                
                            case ServerAntwort.ABMELDUNG:
				d("Abmeldung. Modus: "+m);
                                ende=true;
				robMaint.deleteRob(this,"LL");
                                break;
                            
                            case ServerAntwort.AENDERUNGFERTIG:
                                if ((m!=Server.SPIELSTART)&&(m!=Server.SPIELENDE)){
                                    d("RV: habe OK im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }

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
                                if ((m==Server.SPIELSTART)||(m==Server.SPIELENDE)||(m==Server.NIX)){
                                    d("RV: habe GibRoboterPos im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }
                                if (((m==Server.ZERSTOERT_SYNC)||(m==Server.ZERSTOERT_ASYNC))&&(!ans.name.equals(rob.getName()))){
                                    d("RV: habe GibRoboterPos fuer nicht-mich im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }

                                d("Gibroboterpos fuer "+ans.name+" erhalten.");

				Ort o=info.getRobPos(ans.name);
				if (o!=null)
				    komm.sendRobpos(o);

                                else{
				    d("RV: gibRoboterPos irgendwie unter falschen Voraussetzungen erhalten");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                }
                    
                                break;
                            
                            case ServerAntwort.GIBROBSTATUS:
                            if ((m==Server.SPIELSTART)||(m==Server.SPIELENDE)||(m==Server.NIX)){
                                    d("RV: habe GibRobStatus im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }
                                if (((m==Server.ZERSTOERT_SYNC)||(m==Server.ZERSTOERT_ASYNC))&&(!ans.name.equals(rob.getName()))){
                                    d("RV: habe GibRobStatus fuer nicht-mich im Modus "+m+" erhalten; und tschuess");
                                    robMaint.deleteRob(this,"RV");
                                    ende=true;
                                    return;
                                }
                            
                                Roboter r;
                                r = info.getRobStatus(ans.name);
                                if(r.getName().compareTo("")!=0){
                                    komm.sendRobStatus(r);
                                }else{ 
                                    komm.sendRobStatus(); 
				}
                                break;

                            case ServerAntwort.GIBSPIELSTAND:
				if(info.gameRunning()){
                                    komm.spielstand(new Boolean(true), info.getStanding());
                                }else
                                    komm.spielstand(new Boolean(false), info.getStanding());
                                break;
                            
                            case ServerAntwort.GIBAUSWERTUNGSSTATUS:
				komm.spielStatus(info.getEvalStatus());
                                break;
                            
                            case ServerAntwort.GIBTIMEOUT:
				komm.sendTimeOut(robMaint.getTurnTimeout()/1000); 
                                break;
			    case ServerAntwort.GIBFARBEN:	
                                komm.sendFarben(info.getNamesByColor());
				break;
			     case ServerAntwort.MESSAGE:
				 String[] tmp=new String[ans.msg.length-1];
				 for (int k=1;k<ans.msg.length;k++)
				     tmp[k-1]=ans.msg[k];
				 robMaint.sendMsg(ans.msg[0],tmp);
				 break;
                        } //switch
                    } //synchronized this
                } //try
		catch (KommFutschException e){
		    d("KommFutschException aufgetreten. Beende mich.");
		    ende=true;
		}
                catch (KommException e){
                    d("RV: KommException ist aufgetreten:("+e+") Beende mich.");
                    robMaint.deleteRob(this,"RV");
                    ende=true;
                }
            } //Endlosschleife
	    d("Ende meiner run()-methode erreicht.");
        } // run() 

    private void d(String s)
        {
            Global.debug(this," for "+rob.getName()+": "+s);
        }

    // Delegation methods to comm object

    synchronized void deleteMe(String reason) throws KommException{
	komm.entfernen(reason);
    }
    synchronized void startGame() throws KommException{
	komm.spielstart();
    }
    synchronized void killed() throws KommException{
	komm.zerstoert();
    }
    synchronized void makeYourMove() throws KommException{
	komm.zugabgabe(rob.getKarten());
    }
    synchronized void reEntry() throws KommException{
	komm.reaktivierung();
    }
    synchronized void registerRepair(int nr) throws KommException{
	komm.regReparatur(nr);
    }
} // class

