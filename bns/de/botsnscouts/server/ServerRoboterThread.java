package de.botsnscouts.server;

public class ServerRoboterThread extends Thread
{
    Integer modus;
    int m;             // Modus während der Auswertung einer Antwort
    boolean fertig;
    
    private Server server;
    private ServerAntwort ans;
    protected RoboterServer rob;

    private boolean ende;
    
    public ServerRoboterThread(RoboterServer r,Server s)
        {
            rob=r;
            server=s;
	    modus = new Integer(s.NIX);
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
                    ans=rob.Komm.warte();
		    d("Got a "+ans.getTyp());
                    synchronized(modus){
                        int m=modus.intValue();
                        switch(ans.typ){
                            case ans.PROGRAMMIERUNG:
                                if (m!=server.PROGRAMMIERUNG){
                                    d("RV: habe Programmierung im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }

                                rob.naechsteRundeDeaktiviert=ans.ok; // PowerDown?
                                
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
                                        rob.setZug(i,rob.zugeteilteKarten[ans.register[ansidx++]-1]);
                                
                                d("Programmierung umgesetzt. Roboter:");
                                rob.zeige_Roboter();
                                
				notifyServer();

                                break;
                            
                            case ans.AUSRICHTUNG:
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
                                
                            case ans.REAKTIVIERUNG:
                                if (m!=server.POWERUP){
                                    d("RV: habe Reaktivierung im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }
                                
                                rob.setAktiviert(ans.ok);
                                
				notifyServer();
                                break;
                                
                            case ans.REPARATUR: 
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
                                
                            case ans.ABMELDUNG:
				d("Abmeldung. Modus: "+m);
                                ende=true;
				if ((m==server.PROGRAMMIERUNG)||(m==server.INITAUSR)||(m==server.ZERSTOERT_SYNC)||(m==server.POWERUP)||(m==server.ENTSPERREN)||(m==server.SPIELSTART)||(m==server.SPIELENDE))
				    notifyServer();
				
                                return;
                            
                            case ans.AENDERUNGFERTIG:
                                if ((m!=server.SPIELSTART)&&(m!=server.SPIELENDE)){
                                    d("RV: habe OK im Modus "+m+" erhalten; und tschuess");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                    return;
                                }

				notifyServer();
                                break;

                            case ans.GIBSPIELFELDDIM: 
                                rob.Komm.sendSpielfeldDim(server.feld.sizeX,server.feld.sizeY);
                                break;
                            
                            case ans.GIBSPIELFELD:
                                rob.Komm.sendSpielfeld(server.feld.getSpielfeldString());
                                break;
                            
                            case ans.GIBFAHNENPOS:
                                rob.Komm.sendFahnenpos(server.feld.flaggen);
                                break;
                            
                            case ans.GIBNAMEN:
				rob.Komm.sendNamen(server.gibNamen());
                                break;
                            
                            case ans.GIBROBOTERPOS:
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
				    rob.Komm.sendRobpos(o);

                                else{
				    d("RV: gibRoboterPos irgendwie unter falschen Voraussetzungen erhalten");
                                    server.roboterHinrichten(this,"RV");
                                    ende=true;
                                }
                    
                                break;
                            
                            case ans.GIBFELDINHALT:
				rob.Komm.sendFeldinhalt(server.feld.getFeld(ans.ort.x, ans.ort.y));      
                                break;
                            
                            case ans.GIBROBSTATUS:
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
                                    rob.Komm.sendRobStatus(r);
                                }else{ 
                                    rob.Komm.sendRobStatus(); 
				}
                                break;

                            case ans.GIBSPIELSTAND:
				if(server.spiellaeuft()){
                                    rob.Komm.spielstand(new Boolean(true),  server.auswertung());
                                }else
                                    rob.Komm.spielstand(new Boolean(false), server.auswertung());
                                break;
                            
                            case ans.GIBAUSWERTUNGSSTATUS:
				rob.Komm.spielStatus(server.gibAuswertungsStatus());
                                break;
                            
                            case ans.GIBTIMEOUT:
				rob.Komm.sendTimeOut(server.zugto/1000); 
                                break;
			    case ans.GIBFARBEN:	
                                rob.Komm.sendFarben(server.angemeldet);
				break;
			     case ans.MESSAGE:
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
            Global.debug(this," for "+rob.name+": "+s);
        }
} // class
