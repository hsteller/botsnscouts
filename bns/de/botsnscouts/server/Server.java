package de.botsnscouts.server;

import java.util.*;
import de.botsnscouts.start.*;
import de.botsnscouts.util.*;

public class Server extends Thread{

	// Vektoren
        private Vector aThreads		= new Vector();	// ServerAusgabeThread
	private Vector ausgabenEintrittsListe	= new Vector();	// ServerAusgabeThread
	private Vector rThreads		= new Vector();	// ServerRoboterThread
	private Vector aktRoboter		= new Vector();	// ServerRoboterThread
	private Vector zerstoerteRoboter	= new Vector();	// ServerRoboterThread
	private Vector roboterEintrittsListe	= new Vector();	// ServerRoboterThread
	private Vector roboterFriedhof	= new Vector();	// ServerRoboterThread (endg¸ltig tote)
        private Vector gewinner               = new Vector(); //ServerRoboterThread (Letzte Fahne erreicht)

	private ServerAnmeldeOberThread sAnmeldeThread;
	protected SpielfeldSim feld;	
	private Ort[] flaggen;	
	protected int anzSpieler;
	protected int anmeldePort;

	protected String[] angemeldet = new String[8];
	protected StartServer startServer;
	private boolean gameover = false;
        private int aktPhase=0; // enthaelt die Nummer der gerade auswertenden Phase. 0 wenn nicht ausgewertet wird

        private Vector msgQ; // Um zu sendende Messages zwischenzuspeichern
        private Timer roboterWecker;
        private Timer ausgabenWecker;
    
	// Timeouts
	protected int zugto;
        private int kommto; //F¸r Dinge, die nur eine Reaktion erwarten, kein Nachdenken, z.B. Spielstart
        private final int rundenbeginnto = 60000; // Falls noch zerstoerte Roboter fehlen
        private final int ausgabennotifyto = 60000;
        protected final int anmeldeto = 60000; // So lange wartet der ServerAnmeldeThread auf eine Aktion
    

        //Modi, ihre Bearbeitung und Synchronistaion

        private int modus; //Aktueller Bearbeitungsmodus, die Clients haben eigene.
        private Vector rThreadsAufDieIchWarte = new Vector();
        private Vector aThreadsAufDieIchWarte = new Vector();
    
          // RoboterThreads auf die aktuell gewartet wird, 
          // Grund des Wartens ergibt sich aus Modus.

	// Modus-Konstanten
        // 1. Server / ServerRoboterThread
	protected static final int SPIELSTART=0;
	protected static final int INITAUSR=1;
	protected static final int PROGRAMMIERUNG=2;
	protected static final int POWERUP=3;
	protected static final int ENTSPERREN=4;
	protected static final int SPIELENDE=5;
	protected static final int NIX=6;
	protected static final int ALLGEMEIN=NIX;
	protected static final int ZERSTOERT_ASYNC=7;
        protected static final int ZERSTOERT_SYNC=8;
        // 2. ServerAusgabeThread
        protected static final int FRAGENERLAUBT=10;
        protected static final int KEINEFRAGEN=11;

	/**
	* Der Konstruktor wird pro Spiel nur einmal vom StartServer aufgerufen
	*
	* @param anzahlmitspieler 	Anzahl der Mitspieler inklusive k~Anstliche Spieler
	* @param anmeldeport    	Portnummer auf dem sich die Spieler und Ausgabekan~Dle anmelden, defauls = 8000
	* @param zugabgabetimeout  	Dauer (Timeout) der R~Ackgabe der ausgew~Dhlten Karten der Spieler
	* @param Spielfeld      	Komplettes Spielfeld in Stringform
	* @param Flaggen     		Flaggen in Stringform laut "Protokolle und Datenformate"
	* @param x        		erste Koordinate der Dimension des Spielfeldes
	* @param y        		zweite Koordinate der Dimension des Spielfeldes
	*/
public Server(	int 		anzahlmitspieler,
		StartServer	sserver,
      		int 		anmeldeport,
      		int 		zugabgabetimeout,
      		String 		Spielfeld,
      		Ort[] 		Flaggen,
      		int 		x,
      		int 		y){
    //Global.setVerbose(true);
      	anzSpieler = anzahlmitspieler;
      	anmeldePort = anmeldeport;
      	zugto = zugabgabetimeout*1000;
	startServer = sserver;	
	flaggen = Flaggen;
        msgQ=new Vector();
        roboterWecker=new Timer();
        ausgabenWecker=new Timer();

	try{
      		feld=new SpielfeldSim(x,y,Spielfeld,flaggen,this);
      	}catch (FormatException e){
		System.err.println("Fehler im Spielfeldstring.");
		System.exit(5);
      	}catch (FlaggenException e){
		System.err.println("Fehler in den Flaggen.");
		System.exit(5);
      	}

      	d("Anzahl der Mitspieler : "+anzSpieler);
      	d("Anmeldeport           : "+anmeldePort);
      	d("Zugabgabetimeout      : "+zugto);
      	d("spielfeld             : \n"+feld);
}

    public void addAusgabeThread(ServerAusgabeThread s){
	d("Addiere Ausgabe zu Eintrittsliste hinzu");
	ausgabenEintrittsListe.addElement(s);
    }

    public void addRoboterThread(ServerRoboterThread s){
	rThreads.addElement(s);
	aktRoboter.addElement(s);
    }

    public void wiederEintritt(ServerRoboterThread s){
	zerstoerteRoboter.removeElement(s);
	roboterEintrittsListe.addElement(s);
    }

    public String[] gibNamen(){
	String[] s;
	int len=rThreads.size();
	s=new String[len];
	int i=0;
	for (Iterator it=rThreads.iterator();it.hasNext();)
	    s[i++]=((ServerRoboterThread)it.next()).rob.getName();
	return s;
    }

    public Ort gibRobPos(String name){
	for (Iterator it=rThreads.iterator();it.hasNext();){
	    ServerRoboterThread srt=(ServerRoboterThread)it.next();
	    if (srt.rob.name.equals(name))
		return srt.rob.getPos();
	} 
	return null;
    }

public synchronized void ausgabenBenachrichtigen(String[] s){

        // PRE: currentThread ist der ServerThread
        //  ... aber wir pruefen das lieber nochmal :-)
            
    if (Thread.currentThread()!=this){
        d("ausgabenMsgWennNoetig: DAS IST NICHT DER SERVERTHREAD HIER, sondern "+Thread.currentThread());
        throw new RuntimeException("nur der Serverthread darf Server.ausgabenMsgWennNoetig aufrufen.");
    }

    ausgabenMsgWennNoetig();      // Dies ist ein guter Zeitpunkt alles los zu werden :-)

    d("Grˆﬂe der eintrittsliste: "+ausgabenEintrittsListe.size()+"; aThreads: "+aThreads.size());
    setzeAusgaben();         // neue Ausgaben begr¸ﬂen
    d("Grˆﬂe der eintrittsliste: "+ausgabenEintrittsListe.size()+"; aThreads: "+aThreads.size());

    Vector killList=new Vector();  // nach TO zu entstoepselnde Ausgaben

    synchronized(aThreads){
        for (Iterator it=aThreads.iterator();it.hasNext();){
            ServerAusgabeThread tmp=(ServerAusgabeThread)it.next();
	    if (!tmp.isAlive()){
		it.remove(); //aus aThreads
		continue;
	    }
	    
            tmp.fertig=false;
            synchronized (tmp.modus){
                tmp.modus=new Integer(FRAGENERLAUBT);
            }
            try{
                tmp.komm.aenderung(s);
            }
	    catch (KommFutschException ex){
		new Fehlermeldung(Message.say("Server","eKommFutschA"));
	    }
            catch (KommException ex){
                d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
            }
        } //for
        d("Alle AusgabeThreads benachrichtigt und in den richtigen Modus versetzt.");
    }

    if (aThreads.size()==0)
        return;              // die Muehe koennen wir uns dann auch gerade schenken...

    aThreadsAufDieIchWarte=aThreads;
    
    d("Der Server wartet jetzt "+ausgabennotifyto+" Millisek. auf seine AusgabenThreads (aenderung()).");
    ausgabenWecker.setTimer(ausgabennotifyto,this);
    synchronized(ausgabenWecker){
        ausgabenWecker.notify();
    }
    boolean beenden=false;
    while (!beenden){
        try {
            wait();
        } catch(InterruptedException e) {
            d("InterruptedException! warteAufAusgaben: "+e);
        }
        d("Jemand hat mich notifyed.");
        if (ausgabenWecker.vorbei()){
            d("War wohl ein timeout");
            beenden=true; // timeout...
        } else if (alleAusgabenFertig()) {
            d("War wohl weil alle fertig sind");
            beenden=true; // alle durch...
            ausgabenWecker.interrupt(); // setze den Wecker zurueck
        }
        else{ // Na, dann haben wir vielleicht messages zu verschicken
            d("Hm, kein Grund zu erkennen - gucke mal nach zu verschickenden Nachrichten");
            ausgabenMsgWennNoetig();
            if (alleAusgabenFertig()||ausgabenWecker.vorbei()){
                beenden=true;
                ausgabenWecker.interrupt();
            }
        }
    }
    
    synchronized(aThreads){
        for (Iterator e=aThreads.iterator();e.hasNext();){
            ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
            tmp.modus=new Integer(KEINEFRAGEN);
            if (!tmp.fertig){
                d("Ausgabe nicht fertig. ‹berf¸hre in Killliste.");
                killList.addElement(tmp);
            }
        }
    } // synchronized aThreads
    
    for (Iterator e=killList.iterator();e.hasNext();)
        ausgabeEntstoepseln((ServerAusgabeThread)e.next(),"TO");
}


    /** Schickt Messages aus der msgQ an die Ausgaben */
    private void ausgabenMsgWennNoetig()
        {
                // PRE: currentThread ist der ServerThread
               //  ... aber wir pruefen das lieber nochmal :-)
            
            if (Thread.currentThread()!=this){
                d("ausgabenMsgWennNoetig: DAS IST NICHT DER SERVERTHREAD HIER, sondern "+Thread.currentThread());
                throw new RuntimeException("nur der Serverthread darf Server.ausgabenMsgWennNoetig aufrufen.");
            }
            
            d("ausgabenMsgWennNoetig aufgerufen...");
            synchronized(msgQ){
                while (msgQ.size()>0){
                    d("Die Q hat "+msgQ.size()+"Elemente... Sende "+(String)msgQ.elementAt(0)+".");
                    ausgabenMsg((String)msgQ.elementAt(0),(String [])msgQ.elementAt(1));
                    msgQ.removeElementAt(0);
                    msgQ.removeElementAt(0);  // The first one already is gone...
                }
            }
        }
    

    protected void ausgabenMsg(String id, String arg){
	String[] tmp=new String[1];
	tmp[0]=arg;
	ausgabenMsg(id,tmp);
    }

        /** Schickt eine Nachricht an alle Ausgaben >= Version 2.0 */
    protected synchronized void ausgabenMsg(String id,String s[]){

            /* Vorsicht: Nur der ServerThread darf diese Methode wirklich ausf¸hren!
               Ansonsten legt sich ein anderer Thread sleepen, und der Server wird
               notifyed - wenn er womˆglich ganz woanders sleept... */
        
        d("ausgabenMsg aufgerufen. id="+id);

        if (Thread.currentThread()!=this){
            d("Uh-oh: ausgabenMsg von "+Thread.currentThread()+"aufgerufen... Packe msg in Q...");

            synchronized(msgQ){
                msgQ.addElement(id);
                msgQ.addElement(s);
            }

            synchronized(this){
                d("Notifie ServerThread, damit die Nachrichten geschickt werden...");
                this.notify();
            }
                    
            return;
        }        

        synchronized(aThreads){
            for (Iterator it=aThreads.iterator();it.hasNext();){
                ServerAusgabeThread tmp=(ServerAusgabeThread)it.next();

		if (!tmp.isAlive()){
		    it.remove();      //aus aThreads
		    continue;
		}
		
                if (tmp.version >= 2)
                    tmp.fertig=false;
                else{
                    tmp.fertig=true;
                    continue;
                }
                
                synchronized (tmp.modus){
                    tmp.modus=new Integer(FRAGENERLAUBT);
            }
                tmp.komm.message(id, s);
            } //for
            d("Msg: Alle relevanten AusgabeThreads benachrichtigt und in den richtigen Modus versetzt.");
        }

        if (aThreads.size()==0)
            return;              // die Muehe koennen wir uns dann auch gerade schenken...
    
        Vector killList=new Vector();  // nach TO zu entstoepselnde Ausgaben
    
        aThreadsAufDieIchWarte=aThreads;

        ausgabenWecker.setTimer(ausgabennotifyto,this);
        synchronized(ausgabenWecker){
            ausgabenWecker.notify();
        }
        try{
            while (!(ausgabenWecker.vorbei()||alleAusgabenFertig()))
                wait();
        } catch (InterruptedException ex){
            d("Interruptiert worden.");
        }
        ausgabenWecker.interrupt();
        
        synchronized(aThreads){
            for (Iterator e=aThreads.iterator();e.hasNext();){
                ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
                tmp.modus=new Integer(KEINEFRAGEN);
                if (!tmp.fertig){
                    d("Ausgabe nicht fertig. ‹berf¸hre in Killliste.");
                    killList.addElement(tmp);
                }
            }
        } // synchronized aThreads
        
        for (Iterator e=killList.iterator();e.hasNext();)
            ausgabeEntstoepseln((ServerAusgabeThread)e.next(),"TO");
    }

/** Entfernt den Thread, benachrichtigt den Spieler (dessen Kommverbindung moeglicherweise
 *  abgeschmiert ist!! -> nicht ewig warten), entfernt den Roboter aus aktiv, wiederein-
 *  trittsliste oder friedhof 
 * @param t - der ServerRoboterThread des zu entfernenden Roboters
 * @param grund - "TO" wegen timeout oder "RV" wg. Regelverletzung oder....
 * @returns boolean, ob Hinrichtung erfolgreich war.
*/
    protected void roboterHinrichten(ServerRoboterThread t, String grund ) {
	d("roboterHinrichten aufgerufen. robname="+t.rob.getName()+"; grund="+grund);
	try{
	    t.rob.Komm.entfernen(grund);
	}
	catch(KommFutschException ex) {
	    new Fehlermeldung(Message.say("Server","eKommFutschR", t.rob.getName()));
	}
	catch(KommException ex) {
	    d("Roboter "+t.rob.getName()+" konnte nicht mehr von seiner Entfernung wg. "+grund+" benachrichtigt werden: "+ex);
	}

	/** wird removed, wenn kein anderer Thread den Vector modifizieren darf
	    if (!aktRoboter.removeElement(t))
	    if(!zerstoerteRoboter.removeElement(t))
	    if(!roboterEintrittsListe.removeElement(t))
	    if(!roboterFriedhof.removeElement(t))
	    if(gewinner.contains(t)){
	    d("Hinzurichtender Roboter "+t.rob.name+" ist Gewinner und wird nicht hingerichtet.");
	    return;
	    }else{
	    d("Hinzurichtender Roboter "+t.rob.name+" ist bereits verschwunden.");
	    return;
	    }
	    d("vor rThreads.removeElement()");
	rThreads.removeElement(t);
	*/
	//	d("vor rTadiw.re()");
	//	rThreadsAufDieIchWarte.removeElement(t);
	/*try{
	  t.rob.Komm.in.close();
	  t.rob.Komm.out.close();
	  }
	  catch (java.io.IOException ex){
	  d("IOException beim Killen des KommObjektes.");
	  }*/

	t.interrupt();     // Beende den Thread bei n‰chster GElegenheit

	//Messages.............
	String[] tmpstr=new String[1];
	tmpstr[0]=t.rob.getName();
	if (grund.equals("LL"))
	    ausgabenMsg("mHinrLL",tmpstr);	
	else if (grund.equals("TO"))
	    ausgabenMsg("mHinrTO",tmpstr);	
	else if (grund.equals("RV"))
	ausgabenMsg("mHinrRV",tmpstr);	
	//....................

	gameover = istSpielende();
    }
    
    protected boolean alleAusgabenFertig()
        {
            synchronized(aThreadsAufDieIchWarte) {
	    d("alleAusgabenFertig() wird geprueft.");
		for (Iterator e=aThreadsAufDieIchWarte.iterator();e.hasNext();){
		    ServerAusgabeThread tmp = (ServerAusgabeThread  )e.next();
		    d("SAT: Fertig?        "+tmp.fertig);
		    if (!tmp.fertig)
			return false;
		}
		d("Alle sind fertig!");
		return true;		
	    }
        }

    protected void ausgabeEntstoepseln(ServerAusgabeThread t, String grund)
        {
	    d("ausgabeEntstoepseln aufgerufen. grund="+grund);
            try{
                t.komm.entfernen(grund);
            }
            catch(KommException ex) {
                d("Ausgabe konnte nicht mehr von ihrer Entfernung wg. "+grund+" benachrichtigt werden: "+ex);
            }
	    /** Machen wir bei n‰chster Gelegenheit
            if (!aThreads.removeElement(t))
                d("Entzustoepselnde Ausgabe ist bereits verschwunden.");
	    */
	    t.interrupt(); // Beende sie
        }

/** Wenn auf ServerRoboterThreads gewartet wird, setzen diese bei Empfang der gewuenschten
 *  Antwort fertig auf true und rufen dies auf. Soll true zurueckgeben, wenn der Server
 *  notify()-ed werden soll, false sonst 
 *  @param sendMsg True falls eine Nachricht beim vorletzten Abgeber geschickt werden
 *                 soll, false sonst
 */
    protected boolean alleRoboterFertig(boolean sendMsg)
        {
	    synchronized(rThreadsAufDieIchWarte) {
		d("alleRoboterfertig() wird geprueft.");
		int unfertig = 0;
		ServerRoboterThread unfertigerRob = null;
		for (Iterator e=rThreadsAufDieIchWarte.iterator();e.hasNext();){
		    ServerRoboterThread tmp = (ServerRoboterThread  )e.next();
		    d(tmp.rob.name+"fertig?        "+tmp.fertig);
		    if (!tmp.fertig) {
			unfertig++;
			unfertigerRob = tmp;
		    }
		}
                if (sendMsg && modus == PROGRAMMIERUNG && unfertig == 1) {
		    // Messages....................
		    String []tmpstr=new String[1];
		    tmpstr[0]=unfertigerRob.rob.getName();
		    ausgabenMsg("mUnfertigerRob",tmpstr);
		    // ...........................
		}
		if (unfertig>0)
		    return false;
		d("Alle sind fertig!");
		return true;		
	    }
        }

/** Modus in ServerRoboterThreads setzen und warten, bis sie zu Potte kommen.
 *  Wird bei allen Modi auﬂer ZERST÷RT benutzt.
 *  PRE: Attribute rThreadsAufDieIchWarte und modus sind korrekt belegt,
 *       auﬂerdem muessen die Roboter korrekt initialisiert sein,
 *       konkret: Bei Programmierungsmodus wird auf die Karten zugegriffen.
 *  POST: Der Modus wurde von unseren entsprechenden Threads bearbeitet
 */
    private  void  broadcastUndWarteAufRoboter() {

	//Die Threads auf die ich warte auf unfertig setzen
	synchronized(rThreadsAufDieIchWarte) {
	    for(Iterator iter = rThreadsAufDieIchWarte.iterator(); iter.hasNext();){
		ServerRoboterThread tmp=(ServerRoboterThread)iter.next();
		if (!tmp.isAlive()){
		    iter.remove();  // aus rThreadsAufDieIchWarte (also aktRoboter, etc)
		    rThreads.remove(tmp);
		    String[] s=new String[1];
		    s[0]=tmp.rob.getName();
		    ausgabenMsg("mAbmeldung",s);
		}
		tmp.fertig = false;
	    }

	    if (rThreadsAufDieIchWarte.size()==0)
		return;

	    //Broadcast an die betroffenen Threads, Inhalt je nach Modus
	    for (Iterator e=rThreadsAufDieIchWarte.iterator();e.hasNext();) {
		ServerRoboterThread srt = (ServerRoboterThread )e.next();
		KommServerRoboter komm = srt.rob.Komm;

		try{
		    switch(modus) {
		    case SPIELSTART:
			komm.spielstart();
			break;
		    case INITAUSR:
			komm.zerstoert();
			break;
		    case PROGRAMMIERUNG:
		    //PRE: Im RoboterServer stehen die richtigen Karten
			komm.zugabgabe(srt.rob.zugeteilteKarten);
			break;
		    case POWERUP:
			komm.reaktivierung();
			break;
		    case ENTSPERREN:
			komm.regReparatur(srt.rob.gesperrteRegs()-srt.rob.getSchaden()+4);
			break;
		    case SPIELENDE:
			komm.entfernen("GO");
		    break;
		    }
		    
		} 
		catch (KommFutschException ex){
		    new Fehlermeldung(Message.say("Server","eKommFutschR", srt.rob.getName()));
		}
		catch (KommException ex) {
		    roboterHinrichten(srt, "RV");
		}
	    }
	}

	//Schlafen bis TO oder alle fertig
	warteAufRoboter();

	//Falls welche nicht fertig geworden, entfernen
	// Achtung! Loesche aus betrachtetem Vektor....
	//TODO: nochmal nachdenken, ob hier alles glatt geht
	/*for (int i=0;i<rThreadsAufDieIchWarte.size();i++) {
	    ServerRoboterThread tmp = (ServerRoboterThread )rThreadsAufDieIchWarte.elementAt(i);
	    if (!tmp.fertig){
		d("ServerRoboterThread "+tmp.rob.getName()+" rauswerfen.");
		if (roboterHinrichten(tmp,"TO"))
		    i--;
	    }                     
	    }*/
	synchronized (rThreadsAufDieIchWarte){
	    for (Iterator it = rThreadsAufDieIchWarte.listIterator();it.hasNext();){
		ServerRoboterThread tmp=(ServerRoboterThread)it.next();
		if (!tmp.fertig){
		    d("!!!ServerRoboterThread "+tmp.rob.getName()+" rauswerfen wegen Timeout!!!");
		    it.remove();                    // aus rThreadsAufDieIchWarte
		    roboterHinrichten(tmp,"TO");
		}
	    }
	}
    }   

    /** Wartet -- je nach Modus -- eine bestimmte Zeit auf wiederkehrende
     *  ServerRoboterThreads oder eben bis alle ferig sind.
     * PRE: rThreadsAufDieIchWarte und modus ist korrekt gesetzt
     */
    private synchronized void warteAufRoboter() {

	int to;

	switch (modus) {
	case SPIELSTART:
	    to = kommto; 
            break;
	case INITAUSR:
	case PROGRAMMIERUNG:
	case POWERUP:
	case ENTSPERREN:
	case SPIELENDE:
	    to = zugto;
	    break;
	case ZERSTOERT_SYNC:
	case ZERSTOERT_ASYNC:
	    to = rundenbeginnto;
            break;
	default:
	    to = 0;
	}

	d("Der Server wartet jetzt "+to+" Millisek. auf seine RoboterThreads ("+modus+").");
        roboterWecker.setTimer(to,this);
        synchronized(roboterWecker){
            roboterWecker.notify();
        }
        boolean beenden=false;
        while (!beenden){
            try {
                wait();
            } catch(InterruptedException e) {
                d("InterruptedException! warteAufRoboter: "+e);
            }
            d("Jemand hat mich notifyed.");
            if (roboterWecker.vorbei()){
                d("War wohl ein timeout");
                beenden=true; // timeout...
            } else if (alleRoboterFertig(false)) {
                d("War wohl weil alle fertig sind");
                beenden=true; // alle durch...
                roboterWecker.interrupt(); // setze den RoboterWecker zurueck
            }
            else{ // Na, dann haben wir vielleicht messages zu verschicken
                d("Hm, kein Grund zu erkennen - gucke mal nach zu verschickenden Nachrichten");
                ausgabenMsgWennNoetig();
                if (alleRoboterFertig(false)||roboterWecker.vorbei()){
                    beenden=true;
                    roboterWecker.interrupt();
                }
            }
        }
    }
    

    /** Setzt Modus in ServerRoboterThreads um
     * @param Vector v Vektor der umzusetzenden ServerRoboterThreads
     * @param int neuerModus Modus in den die Threads gesetzt werden sollen
     */
    private void wechselModus(Vector v, int neuerModus) {
	for (Iterator e=v.iterator();e.hasNext();) {
	    ServerRoboterThread t = (ServerRoboterThread )e.next();
	    synchronized(t.modus) {
		t.modus = new Integer(neuerModus);
	    }
	}
    }

	public Roboter roboterStatus(String robotername){
	    // durchsucht die aktRoboter-, zerstoerteRoboter-, roboterEintrittsListe- 
	    // und roboterFriedhof-Vector nach roboter mit dem name robotername und liefert
	    // eine kopie des entsprechenden roboters zur¸ck, falls kein roboter mit
	    // dem name gefunden wird, wird null zur¸ckgegeben
	    for(Iterator e = rThreads.iterator(); e.hasNext();) {
		Roboter r = ((ServerRoboterThread )e.next()).rob;
		if (r.getName().equals(robotername))
		    return r;
	    }
	    return null;
	}

        public boolean spiellaeuft(){
	    return !gameover;	
	}
	
	public String[] auswertung(){
		String[] s = new String[8];
		int i=0;
		for (Iterator e = gewinner.iterator(); e.hasNext();) {
		    s[i++]=((ServerRoboterThread )e.next()).rob.getName();
		}
		return s;
	}

	/**
	 * liefert eine Liste von Statusobjekten.Ein solcher Statusobjekt enthaelt de NAMEN DES Spielers
	 * seine ausgespielten Karten und die Nummer der jetzige Phase.
	 * @return Status[ ]
	 */  
	public Status[] gibAuswertungsStatus(){ 
            synchronized(rThreads){
		Status[] s = new Status[rThreads.size()];
                for (int i=0;i<s.length;i++){
                    s[i]=new Status();
                    ServerRoboterThread tmp=(ServerRoboterThread)rThreads.elementAt(i);
                    s[i].aktPhase=aktPhase;
                    s[i].robName=tmp.rob.getName();
                    s[i].register=new Karte[aktPhase];
                    for (int j=0;j<aktPhase;j++)
                        s[i].register[j]=tmp.rob.getZug(j);
                }
		return s;
            }
	}

private void d(String s){
	Global.debug(this, s);
}

/** Uebertraegt ausgabeneintrittsliste in offizielle Ausgabenliste */
private synchronized void setzeAusgaben(){

    synchronized (ausgabenEintrittsListe){
        if (ausgabenEintrittsListe.size()==0)
            return;
        else
            d("Es gibt neue Ausgaben. Begr¸ﬂe sie.");

        for (Iterator e=ausgabenEintrittsListe.iterator();e.hasNext();){
            ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
            tmp.fertig=false;
            synchronized (tmp.modus){
                tmp.modus=new Integer(FRAGENERLAUBT);
            }
            try{
                tmp.komm.spielstart();
            }
	    catch (KommFutschException ex){
		new Fehlermeldung(Message.say("Server", "eKummFutschA"));
	    }
            catch (KommException ex){
                d("setzeAusgaben: Es ist eine KommException aufgetreten.");
            }
            tmp.start();
        } //for
        d("Alle ael-s benachrichtigt und in den richtigen Modus versetzt.");
    }
    
    aThreadsAufDieIchWarte = ausgabenEintrittsListe;

    ausgabenWecker.setTimer(ausgabennotifyto,this);
    synchronized(ausgabenWecker){
        ausgabenWecker.notify();
    }
    try{
        while (!(ausgabenWecker.vorbei()||alleAusgabenFertig()))
            wait();
    } catch (InterruptedException ex){
	d("Interruptiert worden.");
    }
    ausgabenWecker.interrupt();

    synchronized (ausgabenEintrittsListe){
        d("Kopiere ael: "+ausgabenEintrittsListe.size());
	for (Iterator iter=ausgabenEintrittsListe.iterator();iter.hasNext();){
	    ServerAusgabeThread tmp=(ServerAusgabeThread)iter.next();
            tmp.modus=new Integer(KEINEFRAGEN);
            iter.remove();     // aus ael
            if (tmp.fertig){
		d("Addiere einen zu aThreads hinzu");
                aThreads.addElement(tmp); // implizit synchronized
	    }
        }
    } // synchronized ausgabenEL
}
    
	
    // returns false if interrupted, true if all is ok
private synchronized boolean anmeldung(){
	sAnmeldeThread = new ServerAnmeldeOberThread(this);
	sAnmeldeThread.start();
	d("sAnmeldeThread gestartet");
	if (isInterrupted())
	    return false;
	try{
	    wait();                            // wird vom StartSpieler notified!
	}catch(InterruptedException e){
	    d("In der anmeldung interruptiert worden!");
	    sAnmeldeThread.interrupt();
	    return false;
	}
	//Spiel geht los
	startServer.spielGehtLos(this);
	synchronized(sAnmeldeThread.roboterAnmeldung){
	    sAnmeldeThread.roboterAnmeldung = Boolean.FALSE;
	}
	return true;
}

private void setzeStartPunkt(){
	// setzen der x-, y-, archivX- und archivY-Koordinaten in den Robots auf 
	// die Koordinaten der ersten Flagge
	d("setze x und archivX in robots auf "+feld.flaggen[0].getX());
	d("setze y und archivY in robots auf "+feld.flaggen[0].getY());
	for(int i = 0; i < aktRoboter.size(); i++){
	    ((ServerRoboterThread)(aktRoboter.elementAt(i))).rob.setPos(feld.flaggen[0]);
	    ((ServerRoboterThread)(aktRoboter.elementAt(i))).rob.touchArchiv();
	}
}

private void roboterThreadStart(){
	for(int i = 0; i < rThreads.size(); i++){
		((ServerRoboterThread)(rThreads.elementAt(i))).start();
	}
	d("ServerRoboterThreads wurden gestartet");	
}

    /** Sind noch Roboter dabei?
     */
    private boolean istSpielende() {
	return (rThreads.size()==0) || ((aktRoboter.size() == 0)
					&& (zerstoerteRoboter.size()==0)
					&& (roboterEintrittsListe.size()==0));
    }

    /** Repariere Schaden, falls auf Reparaturfeld oder Flagge
     * Liefert true, falls erst erfragt werden muss, welche  Register entsperren.werden sollen.
     */
    private boolean repariereGgf(ServerRoboterThread t) {
	if (t.rob.gesperrteRegs()==0)
	    return false;
        if ((t.rob.gesperrteRegs()>0)&&(t.rob.getSchaden()<5)){ 
	    t.rob.entsperreAlleRegs();
	    return false;
	}
	
	d("**repGGf: schaden-4="+(t.rob.getSchaden()-4)+"; gespRegs()="+t.rob.gesperrteRegs());
        return (t.rob.getSchaden()-4 < t.rob.gesperrteRegs());
    }

    /** Die eigentliche Spielmethode!
     */
    public void run(){
        setName("ServerThread");
        roboterWecker.start();              // den brauchen wir gleich...
        ausgabenWecker.start();
        
	d("anmeldung()");
	boolean spielgestartet=anmeldung();
	if (!spielgestartet)
	    return;

	d("setzeStartPunkt()");
	setzeStartPunkt();  
	d("roboterThreadStart()");
	roboterThreadStart();
	// warten auf NTS von Ausgabekanaelen und Robotern
            // 1. Ausgaben NTSen und NTCen, damit Ort schonmal stimmt
            // kreiere String[] mit allen Namen
        String[] alleN;
        synchronized(rThreads){
            alleN=new String[rThreads.size()];
            int i=0;
            for(Iterator e=rThreads.iterator();e.hasNext();)
                alleN[i++]=((ServerRoboterThread)e.next()).rob.getName();
        }

        ausgabenBenachrichtigen(alleN);
        
            // 2. Roboter
	d("Spiel starten.");
	modus = SPIELSTART;
	rThreadsAufDieIchWarte = rThreads;
	wechselModus(rThreadsAufDieIchWarte, SPIELSTART);
	broadcastUndWarteAufRoboter();
	d("Spiel ist gestartet.");
	// schicken des ersten MNR an alle Roboter ohne die Anzahl der Leben zu reduzieren
	d("Initiale Ausrichtung holen.");
	modus = INITAUSR;
	rThreadsAufDieIchWarte = aktRoboter;
	wechselModus(rThreadsAufDieIchWarte, INITAUSR);
	broadcastUndWarteAufRoboter();
	d("InitialeAusrichtung ist geholt.");
            // Ausgabekanaele von Initialausrichtung benachrichtigen
            // kreiere String[] mit allen Namen
        synchronized(rThreads){
            alleN=new String[rThreads.size()];
            int i=0;
            for(Iterator e=rThreads.iterator();e.hasNext();)
                alleN[i++]=((ServerRoboterThread)e.next()).rob.getName();
        }
        ausgabenBenachrichtigen(alleN);
        
	
	// Rundenschleife
	gameover = false;
	Vector gesperrteKarten = new Vector(); //Vektor von Karten
	for (int iRunde = 1; !gameover; iRunde++) {

	    String[] tmpstr=new String[1];
	    tmpstr[0]=""+iRunde;
	    ausgabenMsg("mNeueRunde",tmpstr);

	    // Evtl. auf wiedereintretende Roboter warten
	    if (zerstoerteRoboter.size()>0) {
		for (Iterator iter=zerstoerteRoboter.iterator();iter.hasNext();)
		    if (!(((ServerRoboterThread)iter.next()).isAlive()))
			iter.remove();

		d("Warte kurz auf zerstoerte Roboter.");
		modus=ZERSTOERT_SYNC;

		tmpstr=new String[1];
		Iterator iter=zerstoerteRoboter.iterator();
		tmpstr[0]=(((ServerRoboterThread)iter.next()).rob.getName());
		while (iter.hasNext())
		    tmpstr[0]=tmpstr[0]+", "+(((ServerRoboterThread)iter.next()).rob.getName());
		ausgabenMsg("mZerstSync",tmpstr);

		rThreadsAufDieIchWarte = zerstoerteRoboter;
		wechselModus(rThreadsAufDieIchWarte, ZERSTOERT_SYNC);
		warteAufRoboter();
		wechselModus(rThreadsAufDieIchWarte, ZERSTOERT_ASYNC);
		synchronized(roboterEintrittsListe){
		    alleN=new String[roboterEintrittsListe.size()];
		    int i=0;
		    for(Iterator e=roboterEintrittsListe.iterator();e.hasNext();)
			alleN[i++]=((ServerRoboterThread)e.next()).rob.getName();
		}
		if (alleN.length > 0)
		    ausgabenBenachrichtigen(alleN);
	    }
	    d("Es werden "+roboterEintrittsListe.size()+" nach ihrer Zerstˆrung wieder eingesetzt"); 

	    String[] namen;   // fuer notifyChange
	    synchronized(roboterEintrittsListe) {
		rThreadsAufDieIchWarte = new Vector();
		d("Lasse Roboter wieder eintreten.");
		for(Iterator e=roboterEintrittsListe.iterator();e.hasNext();) {
		    // Alle wiedereinzusetzenden Roboter auf ihre Archivpos setzen.
		    // In eigener while-Schleife, damit danach wiedereinsetzen korrekt geprueft
		    // wird!
		    ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		    tmp.rob.zumArchiv();
		}
		for(Iterator e=roboterEintrittsListe.iterator();e.hasNext();) {
		    ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		    // Ggf. devirtualisieren.
		    // Achtung, auch aktRoboter beruecksichtigen, die aber reell bleiben.
		    tmp.rob.setVirtuell(false);  //Default: Real einsetzen, ABER....
		    // Virtuell, wenn ein aktiver auch da steht
		    for (Iterator f=aktRoboter.iterator();f.hasNext();) {
			Roboter anderer = ((ServerRoboterThread )f.next()).rob;
			if (tmp.rob.getX() == anderer.getX() && tmp.rob.getY() == anderer.getY()){
			    tmp.rob.setVirtuell();
			    break;
			}
		    }
		    // oder noch ein anderer der wiedereinzusetzenden auch da steht 
		    if (!tmp.rob.istVirtuell()) {
			for (Iterator f=roboterEintrittsListe.iterator();f.hasNext();) {
			    Roboter anderer = ((ServerRoboterThread )f.next()).rob;
			    if (anderer!=tmp.rob && anderer.getX() == tmp.rob.getX() && anderer.getY() == tmp.rob.getY()) {
				tmp.rob.setVirtuell();
			    }
			}
		    }
		}
		
		namen=new String[roboterEintrittsListe.size()];
		int idx=0;
		// Jetzt wirklich wieder einteten lassen
		for(Iterator e=roboterEintrittsListe.iterator();e.hasNext();) {
		    ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		    aktRoboter.addElement(tmp);
		    rThreadsAufDieIchWarte.addElement(tmp);
		    namen[idx++]=tmp.rob.getName();
		}
		roboterEintrittsListe.removeAllElements();
	    }

	    ausgabenBenachrichtigen(namen);    

	    //PowerUp -- nach Zerstoert eingesetzte Roboter werden auch gefragt!
	    // Sind bereits in vorheriger Liste in rThreadsAufDieIchWarte gesetzt worden

	    for(Iterator e=aktRoboter.iterator();e.hasNext();) {
		ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		if (!tmp.rob.istAktiviert()) 
		    rThreadsAufDieIchWarte.addElement(tmp);
		else if (tmp.rob.naechsteRundeDeaktiviert) {
		    //Auschalten der, die letzte Mal PowerDown gesagt haben.
		    d(tmp.rob.getName()+" ist naechste Runde ausgeschaltet.");
		    tmp.rob.setAktiviert(false);
		    tmp.rob.naechsteRundeDeaktiviert = false;
                    tmp.rob.setSchaden(0);
		}
		
	    }
	    if (rThreadsAufDieIchWarte.size() > 0) {
		d("Ich frage "+rThreadsAufDieIchWarte.size()+" nach Powerup.");

		tmpstr=new String[1];
		Iterator iter=rThreadsAufDieIchWarte.iterator();
		tmpstr[0]=(((ServerRoboterThread)iter.next()).rob.getName());
		while (iter.hasNext())
		    tmpstr[0]=tmpstr[0]+", "+(((ServerRoboterThread)iter.next()).rob.getName());
		ausgabenMsg("mPowUpFrage",tmpstr);

		modus = POWERUP;
		wechselModus(rThreadsAufDieIchWarte, POWERUP);
		broadcastUndWarteAufRoboter();
		d("Powerup fertig.");

		namen=new String[rThreadsAufDieIchWarte.size()];
		int idx=0;
		for (Iterator e=rThreadsAufDieIchWarte.iterator();e.hasNext();)
		    namen[idx++]=((ServerRoboterThread)e.next()).rob.getName();
		ausgabenBenachrichtigen(namen);

		for (Iterator it=rThreadsAufDieIchWarte.iterator();it.hasNext();){
		    ServerRoboterThread srt=((ServerRoboterThread)it.next());
		    if (!srt.rob.istAktiviert())
			srt.rob.setSchaden(0);
		}		    
	    }

	    // Karten an die Roboter verteilen die nicht zerstoert oder deaktiviert sind
	    // warten auf die Karten der Roboter (Timeout)

	    KartenStapel stapel = new KartenStapel(gesperrteKarten);
	    
	    rThreadsAufDieIchWarte = new Vector();
	    for(Iterator e=aktRoboter.iterator();e.hasNext();) {
		ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		if (tmp.rob.istAktiviert()) {
		    rThreadsAufDieIchWarte.addElement(tmp);
		    //Karten geben und dabei *beruecksichtigen*, wie viele der Roboter kriegt!
		    for (int i=0; i<(9-tmp.rob.getSchaden()); i++)
			tmp.rob.zugeteilteKarten[i]=stapel.gibKarte();
		    for (int i=(9-tmp.rob.getSchaden());i<9;i++)
			tmp.rob.zugeteilteKarten[i]=null;
		}
	    }
	    if (rThreadsAufDieIchWarte.size() > 0) {
		d("Ich verteile an "+rThreadsAufDieIchWarte.size()+" Karten.");
		modus = PROGRAMMIERUNG;
		wechselModus(rThreadsAufDieIchWarte, PROGRAMMIERUNG);
		broadcastUndWarteAufRoboter();
		d("Programmierung zurueckErhalten");
	    }	    
		
	    // Auswertung beginnt
	    wechselModus(aktRoboter,NIX);
	    modus = NIX;

	    // Start der Phasenschleife
	    for (aktPhase=1; aktPhase!=0; aktPhase = (aktPhase+1)%6) {
		d("Auswertung Phase "+aktPhase);
		// Am Spiel beteiligte Roboter in Array kopieren (aus technischen Gruenden)
		Roboter[] robs;
		synchronized(aktRoboter) {
		    robs = new Roboter[aktRoboter.size()];
		    int i=0;
		    for(Iterator e=aktRoboter.iterator(); e.hasNext(); i++) {
			robs[i] = ((ServerRoboterThread )e.next()).rob;
		    }
		}
		// (doPhase())
		//DEBUG
		d("feld.doPhase("+aktPhase+") mit ");
		for(int i=0;i<aktRoboter.size();i++) {
		    d(((ServerRoboterThread )aktRoboter.elementAt(i)).rob.getName()+": "+((ServerRoboterThread )aktRoboter.elementAt(i)).rob);
		}
		feld.doPhase(aktPhase,robs);

		// Phase auswerten 
		synchronized(aktRoboter) {
		    for (Iterator e=aktRoboter.listIterator(); e.hasNext();) {
			ServerRoboterThread tmp = (ServerRoboterThread )e.next();
			// Gewinner?
			if (tmp.rob.getNaechsteFlagge() == feld.flaggen.length+1) {
			    //raus aus aktiven Robos, in Gewinnerliste,
                            // vom Plan nehmen, ausgabenbenachrichtigen
			    synchronized(tmp.modus) {
				tmp.modus = new Integer(SPIELENDE);
			    }
			    gewinner.addElement(tmp);
			    e.remove(); //aus aktRoboter
			    // Messages....................
			    tmpstr=new String[2];
			    tmpstr[0]=tmp.rob.getName();
			    tmpstr[1]=""+gewinner.size();
			    ausgabenMsg("mGewinn",tmpstr);	
			    // ...........................
			    try{
				tmp.rob.Komm.entfernen("GO");
			    }
			    catch(KommException ex) {
				d("Roboter "+tmp.rob.getName()+" konnte nicht mehr von seiner Entfernung wg. GO(Gewinn) benachrichtigt werden: "+ex);
			    }

                            tmp.rob.setInvalidPos();
                            tmp.rob.setVirtuell();

			    namen=new String[1];
			    namen[0]=tmp.rob.getName();
			    ausgabenBenachrichtigen(namen);
                            
			    gameover = istSpielende();

			}
			// Schaden > 9
			if (tmp.rob.getSchaden() > 9) {
			    e.remove();  //aus aktRoboter
			    tmp.rob.decrLeben();

			    // Voellig tot?
			    if (tmp.rob.getLeben() > 0) {
				tmp.rob.setSchaden(2);
				//Register entsperren
				tmp.rob.entsperreAlleRegs();
                                tmp.rob.naechsteRundeDeaktiviert=false;
                                
				zerstoerteRoboter.addElement(tmp);
				synchronized(tmp.modus) {
				    tmp.fertig=false;
				    tmp.modus = new Integer(ZERSTOERT_ASYNC);
				}
				try {
				    tmp.rob.Komm.zerstoert();
				}
                                catch(KommFutschException ex){
                                    new Fehlermeldung(Message.say("Server", "eKommFutschR",tmp.rob.getName()));
				}
				catch (KommException ex) {
				    System.err.println("Mit Spieler "+tmp.rob.getName()+"ist ein Kommunikationsfehler aufgetreten.");
				}
			    }
			    else {
				roboterFriedhof.addElement(tmp);
				synchronized(tmp.modus) {
				    tmp.modus = new Integer(SPIELENDE);
				}
				try {
				    tmp.rob.Komm.entfernen("LL");
				}
				catch (KommException ex){
				    d("Kommunikationsfehler beim Entfernen eines Roboters");
				}
				gameover = istSpielende();
			    }
			    
			    namen=new String[1];
			    namen[0]=tmp.rob.getName();
			    ausgabenBenachrichtigen(namen);
			}
		    }
		}
		
	    } // Ende Phasenschleife
	    
	    // Rundenauswertung
	    // Reparaturfelder/Fahnen auswerten und Powerdown setzen
	    rThreadsAufDieIchWarte = new Vector();
	    for (Iterator e=aktRoboter.iterator(); e.hasNext();) {
		ServerRoboterThread tmp = (ServerRoboterThread)e.next();
		if(repariereGgf(tmp)) {
		    d("Addiere einen zu rTadiw hinzu.");
		    rThreadsAufDieIchWarte.addElement(tmp);
		}
	    }
	    if (rThreadsAufDieIchWarte.size() > 0) {
		d("Reparaturanfrage stellen an "+rThreadsAufDieIchWarte.size());
		modus = ENTSPERREN;
		wechselModus(rThreadsAufDieIchWarte,ENTSPERREN);
		broadcastUndWarteAufRoboter();
		d("Reparaturanfragen sind beantwortet.");
	    }

                // gesperrte Register einsammeln
            d("Gesperrte Register einsammeln.");
            gesperrteKarten.removeAllElements();
            synchronized(rThreads){
                for (Iterator e=rThreads.iterator();e.hasNext();){
                    ServerRoboterThread tmp=((ServerRoboterThread)e.next());
                    for (int i=0;i<5;i++)
                        if (tmp.rob.getGesperrteRegister(i)!=null)
                            gesperrteKarten.addElement(tmp.rob.getGesperrteRegister(i));
                }
            }
            d("Habe "+gesperrteKarten.size()+" gesperrte Karten gefunden und gespeichert.");

	} // Rundenschleife ende

	d("Das Spiel ist jetzt zu Ende. (Rundenschleife verlassen.)");

        /* Falls wir jemals ein anderes Ende haben wollen, muﬂ man sich hier nochmal Gedanken
	   machen (eines, bei dem nicht alle Roboter auf der letzten Flagge angekommen sind.

	  if (rThreads.size()>0){
            modus = SPIELENDE;
            rThreadsAufDieIchWarte = rThreads;
            wechselModus(rThreadsAufDieIchWarte, SPIELENDE);
            broadcastUndWarteAufRoboter();           
        }
	d("Alle Roboter sind vom Spielende benachrichtet worden.");

	d("Jetzt noch "+rThreads.size()+ "Roboter hinrichten!");
	while (rThreads.size()>0){
            ServerRoboterThread tmp=(ServerRoboterThread)rThreads.elementAt(0);
            roboterHinrichten(tmp, "GO");
	    }*/

	d("Es sind noch "+aThreads.size()+" Ausgaben da.");
        if (aThreads.size()>0){
            synchronized(aThreads){
                for (Iterator e=aThreads.iterator();e.hasNext();){
                    ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
                    tmp.fertig=false;
                    synchronized (tmp.modus){
                        tmp.modus=new Integer(FRAGENERLAUBT);
                    }
                    try{
                        tmp.komm.entfernen("GO");
                    }
                    catch (KommException ex){
                        d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
                    }
                } //for
                d("Alle AusgabeThreads vom Spielende benachrichtigt und in den richtigen Modus versetzt.");
            }
            
            ausgabenWecker.setTimer(ausgabennotifyto,this);
            synchronized(ausgabenWecker){
                ausgabenWecker.notify();
            }
            try{
                while (!(ausgabenWecker.vorbei()||alleAusgabenFertig()))
		    synchronized(this){
			wait();
		    }
            } catch (InterruptedException ex){
                d("Interruptiert worden.");
            }
            ausgabenWecker.interrupt();
            
            synchronized(aThreads){
                for (Iterator e=aThreads.iterator();e.hasNext();){
                    ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
                    try{
                        tmp.komm.in.close();
                        tmp.komm.out.close();
                    }
                    catch (java.io.IOException ex){
                        d("IOException aufgetreten.");
                    }
                }
            } // synchronized aThreads
        } // if aThreads > 0

            // AnmeldeThread killen
        try{
            sAnmeldeThread.seso.close();
        }
        catch (java.io.IOException ex){
            d("IOException beim AnmeldeThreadKillen.");
        }
        
	d("Ende meiner run()-Methode erreicht!!!");
//leo
	d("rufe spielZuEnde() beim StartServer auf");
startServer.spielZuEnde(this);
    }// run() ende
}// class Server ende


class RRHinrichtungNichtMoeglichException extends RuntimeException {
    RRHinrichtungNichtMoeglichException() { 
	super();
    }
    RRHinrichtungNichtMoeglichException(String s) { 
	super(s);
    }
    
}
