package de.botsnscouts.server;

import java.util.*;

import de.botsnscouts.board.*;
import de.botsnscouts.comm.*;
import de.botsnscouts.start.*;
import de.botsnscouts.util.*;

import org.apache.log4j.Category;

public class Server extends Thread implements ModusConstants, ServerOutputThreadMaintainer,
    InfoRequestAnswerer, OKListener, ServerRobotThreadMaintainer, ThreadMaintainer {
    static final Category CAT = Category.getInstance( Server.class );

    // Vektoren
    private Vector aThreads		= new Vector();	// ServerAusgabeThread
    private Vector ausgabenEintrittsListe	= new Vector();	// ServerAusgabeThread

    private Vector rThreads		= new Vector();	// ServerRoboterThread
    private Vector aktRoboter		= new Vector();	// ServerRoboterThread
    private Vector zerstoerteRoboter	= new Vector();	// ServerRoboterThread
    private Vector roboterEintrittsListe	= new Vector();	// ServerRoboterThread
    private Vector roboterFriedhof	= new Vector();	// ServerRoboterThread (endgültig tote)
    private Vector gewinner               = new Vector(); //ServerRoboterThread (Letzte Fahne erreicht)

    private RegistrationManager registrationManager;
    private MessageThread messageThread;

    protected SpielfeldSim feld;
    private Ort[] flaggen;
    protected int anzSpieler;
    protected int anmeldePort;

    protected String[] angemeldet = new String[8];
    protected StartServer startServer;
    private boolean gameover = false;
    private boolean gameStarted = false;
    private int aktPhase=0; // enthaelt die Nummer der gerade auswertenden Phase. 0 wenn nicht ausgewertet wird

    // Timeouts
    protected int zugto;
    private int kommto; //Für Dinge, die nur eine Reaktion erwarten, kein Nachdenken, z.B. Spielstart
    private final int rundenbeginnto = 60000; // Falls noch zerstoerte Roboter fehlen
    private final int ausgabennotifyto = 60000;
    protected final int anmeldeto = 60000; // So lange wartet der ServerAnmeldeThread auf eine Aktion

    //Modi, ihre Bearbeitung und Synchronistaion

    private int modus; //Aktueller Bearbeitungsmodus, die Clients haben eigene.
    private WaitingForSet waitablesImWaitingFor;

    // um die Statistikdaten zu halten
    protected StatsList stats;
    // Modus-Konstanten
    // 1. Server / ServerRoboterThread
    // 2. ServerAusgabeThread

    // Mandated by OKListener

    /** Removes caller from waitablesImWaitingFor
     */
    public void notifyDone(Waitable me){
	waitablesImWaitingFor.removeAndNotify(me);
    }

    // Methods needed for ServerRobotThreadMaintainer

    /** Entfernt den Thread, benachrichtigt den Spieler (dessen Kommverbindung moeglicherweise
     *  abgeschmiert ist!! -> nicht ewig warten), entfernt den Roboter aus aktiv, wiederein-
     *  trittsliste oder friedhof
     * @param t - der ServerRoboterThread des zu entfernenden Roboters
     * @param grund - "TO" wegen timeout oder "RV" wg. Regelverletzung oder....
     * @returns boolean, ob Hinrichtung erfolgreich war.
     */
    public void deleteRob(ServerRoboterThread t, String grund ) {
	d("deleteRob aufgerufen. robname="+t.rob.getName()+"; grund="+grund);
	waitablesImWaitingFor.removeAndNotify(t);
	try{
	    t.deleteMe(grund);
	}
	catch(KommFutschException ex) {
	    new Fehlermeldung(Message.say("Server","eKommFutschR", t.rob.getName()));
	}
	catch(KommException ex) {
	    d("Roboter "+t.rob.getName()+" konnte nicht mehr von seiner Entfernung wg. "+grund+" benachrichtigt werden: "+ex);
	}

	t.interrupt();     // Beende den Thread bei nächster Gelegenheit

	String[] tmpstr=new String[1];
	tmpstr[0]=t.rob.getName();
	if (grund.equals("LL"))
	    sendMsg("mHinrLL",tmpstr);
	else if (grund.equals("TO"))
	    sendMsg("mHinrTO",tmpstr);
	else if (grund.equals("RV"))
	    sendMsg("mHinrRV",tmpstr);

	gameover = istSpielende();
    }

    public int getTurnTimeout(){ return zugto; }

    public void sendMsg(String id, String arg){
	String[] tmp=new String[1];
	tmp[0]=arg;
	sendMsg(id,tmp);
    }

    /** Schickt eine Nachricht an alle Ausgaben >= Version 2.0 */
    public void sendMsg(String id,String[] s){
	messageThread.append(id,s);
    }

    public void reEntry(ServerRoboterThread s){
	zerstoerteRoboter.removeElement(s);
	roboterEintrittsListe.addElement(s);
    }

    // Methods mandated by interface ServerOutputThreadMaintainer

    public void deleteOutput(ServerAusgabeThread t, String grund)
    {
	d("deleteOutput aufgerufen. grund="+grund);
	try{
	    waitablesImWaitingFor.removeAndNotify(t);
	    t.deleteMe(grund);
	}
	catch(KommException ex) {
	    d("Ausgabe konnte nicht mehr von ihrer Entfernung wg. "+grund+" benachrichtigt werden: "+ex);
	}

	t.interrupt(); // Beende sie
    }

    public int getOutputTimeout(){ return ausgabennotifyto; }

    // Methods mandated by interface ThreadMaintainer
    public Vector getActiveOutputs(){ return aThreads; }
    public MOKListener getMOKListener(){ return messageThread; }
    public OKListener getOKListener(){ return this; }
    public ServerRobotThreadMaintainer getRobThreadMaintainer(){ return this; }
    public ServerOutputThreadMaintainer getOutputThreadMaintainer(){ return this; }
    public InfoRequestAnswerer getInfoRequestAnswerer(){ return this; }
    public StartServer getStartServer(){ return startServer; }
    public int getSignUpTimeout(){ return anmeldeto; }
    public int getMaxPlayers(){ return anzSpieler; }
    public void addOutput(ServerAusgabeThread s){
	d("Addiere Ausgabe zu Eintrittsliste hinzu");
	ausgabenEintrittsListe.addElement(s);
    }
    public void addRobotThread(ServerRoboterThread s){
	rThreads.addElement(s);
	aktRoboter.addElement(s);
    }
    public synchronized int allocateColor(int color, String name){
	if ((color>0)&&(angemeldet[color-1]==null))
	    color--;
	else{
	    color=(int)(Math.random()*7+1);
	    while (angemeldet[color]!=null)
		color=(color+1)%8;
	}
	angemeldet[color]=name;
	return color;
    }



    // Methods mandated by interface InfoRequestAnswerer

    public int getFieldSizeX(){
	return feld.getSizeX();
    }

    public int getFieldSizeY(){
	return feld.getSizeY();
    }

    public String getFieldString(){
	return feld.getSpielfeldString();
    }

    public Ort[] getFlags(){
	return feld.getFlaggen();
    }

    public String[] getNames(){
	String[] s;
	int len=rThreads.size();
	s=new String[len];
	int i=0;
	for (Iterator it=rThreads.iterator();it.hasNext();)
	    s[i++]=((ServerRoboterThread)it.next()).rob.getName();
	return s;
    }

    public Ort getRobPos(String name){
	for (Iterator it=rThreads.iterator();it.hasNext();){
	    ServerRoboterThread srt=(ServerRoboterThread)it.next();
	    if (srt.rob.getName().equals(name))
		return srt.rob.getPos();
	}
	return null;
    }

    public Roboter getRobStatus(String robotername){
	// durchsucht die aktRoboter-, zerstoerteRoboter-, roboterEintrittsListe-
	// und roboterFriedhof-Vector nach roboter mit dem name robotername und liefert
	// eine kopie des entsprechenden roboters zurück, falls kein roboter mit
	// dem name gefunden wird, wird null zurückgegeben
	for(Iterator e = rThreads.iterator(); e.hasNext();) {
	    Roboter r = ((ServerRoboterThread )e.next()).rob;
	    if (r.getName().equals(robotername))
		return r;
	}
	return null;
    }

    public boolean gameRunning(){
	return !gameover;
    }

    public String[] getStanding(){
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
    public Status[] getEvalStatus(){
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

    public String[] getNamesByColor(){ return angemeldet; }

    public StatsList getStats(){ return stats; }

    // Konstruktor

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

    // For the Board, to send the "something has changed"-message

    public void ausgabenBenachrichtigen(String[] s){

        // PRE: currentThread ist der ServerThread
        //  ... aber wir pruefen das lieber nochmal :-)

	if (Thread.currentThread()!=this){
	    d("sendMsgWennNoetig: DAS IST NICHT DER SERVERTHREAD HIER, sondern "+Thread.currentThread());
	    throw new RuntimeException("nur der Serverthread darf Server.sendMsgWennNoetig aufrufen.");
	}

	d("Größe der eintrittsliste: "+ausgabenEintrittsListe.size()+"; aThreads: "+aThreads.size());
	setzeAusgaben();         // neue Ausgaben begrüßen
	d("Größe der eintrittsliste: "+ausgabenEintrittsListe.size()+"; aThreads: "+aThreads.size());

	synchronized(aThreads){
	    waitablesImWaitingFor=new WaitingForSet(aThreads);

	    for (Iterator it=aThreads.iterator();it.hasNext();){
		ServerAusgabeThread tmp=(ServerAusgabeThread)it.next();
		if (!tmp.isAlive()){
		    it.remove(); //aus aThreads
		    waitablesImWaitingFor.remove(tmp);
		    continue;
		}

		tmp.setMode(FRAGENERLAUBT);

		try{
		    tmp.notifyChange(s);
		}
		catch (KommFutschException ex){
		    new Fehlermeldung(Message.say("Server","eKommFutschA"));
		}
		catch (KommException ex){
		    d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
		}
	    } //for
	    d("Alle AusgabeThreads benachrichtigt und in den richtigen Modus versetzt.");

	    if (aThreads.size()==0)
		return;              // die Muehe koennen wir uns dann auch gerade schenken...

	    d("Der Server wartet jetzt "+ausgabennotifyto+" Millisek. auf seine AusgabenThreads (aenderung()).");
	    Iterator it=waitablesImWaitingFor.waitFor(ausgabennotifyto);

	    while (it.hasNext())
		deleteOutput((ServerAusgabeThread)it.next(),"TO");
	}
    }

    // Private methods needed by run()

    /** Modus in ServerRoboterThreads setzen und warten, bis sie zu Potte kommen.
     *  Wird bei allen Modi außer ZERSTÖRT benutzt.
     *  PRE: Attribute rThreadsAufDieIchWarte und modus sind korrekt belegt,
     *       außerdem muessen die Roboter korrekt initialisiert sein,
     *       konkret: Bei Programmierungsmodus wird auf die Karten zugegriffen.
     *  POST: Der Modus wurde von unseren entsprechenden Threads bearbeitet
     */
    private void broadcastUndWarteAufRoboter() {

	for(Iterator iter = rThreads.iterator(); iter.hasNext();){
	    ServerRoboterThread tmp=(ServerRoboterThread)iter.next();
	    if (!tmp.isAlive()){
		iter.remove();  // aus waitablesImWaitingFor (also aktRoboter, etc)
		// WARNING: might be wrong :)
		waitablesImWaitingFor.remove(tmp);
		String[] s=new String[1];
		s[0]=tmp.rob.getName();
		sendMsg("mAbmeldung",s);
	    }
	}

	if (waitablesImWaitingFor.isEmpty())
	    return;

	//Broadcast an die betroffenen Threads, Inhalt je nach Modus
	synchronized (waitablesImWaitingFor){
	    for (Iterator e=waitablesImWaitingFor.iterator();e.hasNext();) {
		ServerRoboterThread srt = (ServerRoboterThread )e.next();

		try{
		    switch(modus) {
		    case SPIELSTART:
			srt.startGame();
			break;
		    case INITAUSR:
			srt.killed();
			break;
		    case PROGRAMMIERUNG:
			//PRE: Im RoboterServer stehen die richtigen Karten
			srt.makeYourMove();
			break;
		    case POWERUP:
			srt.reEntry();
			break;
		    case ENTSPERREN:
			srt.registerRepair(srt.rob.gesperrteRegs()-srt.rob.getSchaden()+4);
			break;
		    case SPIELENDE:
			srt.deleteMe("GO");
			break;
		    }

		}
		catch (KommFutschException ex){
		    new Fehlermeldung(Message.say("Server","eKommFutschR", srt.rob.getName()));
		}
		catch (KommException ex) {
		    deleteRob(srt, "RV");
		}
	    }//for Iterator
	}//synch

	//Schlafen bis TO oder alle fertig
	Iterator it = warteAufRoboter();

	//Falls welche nicht fertig geworden, entfernen
	while (it.hasNext()){
	    ServerRoboterThread tmp=(ServerRoboterThread)it.next();
	    d("!!!ServerRoboterThread "+tmp.rob.getName()+" rauswerfen wegen Timeout!!!");
	    it.remove();
	    deleteRob(tmp,"TO");
	}
    }

    /** Wartet -- je nach Modus -- eine bestimmte Zeit auf wiederkehrende
     *  ServerRoboterThreads oder eben bis alle ferig sind.
     * PRE: rThreadsAufDieIchWarte und modus ist korrekt gesetzt
     */
    private synchronized Iterator warteAufRoboter() {

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
	return waitablesImWaitingFor.waitFor(to);
    }

    /** Setzt Modus in ServerRoboterThreads um
     * @param Vector v Vektor der umzusetzenden ServerRoboterThreads
     * @param int newMode Modus in den die Threads gesetzt werden sollen
     */
    private void wechselModus(Iterator it, int newMode) {
	while (it.hasNext()) {
	    ServerRoboterThread t = (ServerRoboterThread )it.next();
	    t.setMode(newMode);
	}
    }

    /** Uebertraegt ausgabeneintrittsliste in offizielle Ausgabenliste */
    private void setzeAusgaben(){
	d("setzeAusgaben");
	synchronized (aThreads){
	    d("lock on aThreads");
	    synchronized (ausgabenEintrittsListe){
		d("lock on ausgabenEintrittsListe");
		if (ausgabenEintrittsListe.size()==0)
		    return;
		else
		    d("Es gibt neue Ausgaben. Begrüße sie.");

		waitablesImWaitingFor = new WaitingForSet(ausgabenEintrittsListe);

		for (Iterator e=ausgabenEintrittsListe.iterator();e.hasNext();){
		    ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
		    tmp.setMode(FRAGENERLAUBT);
		    try{
			tmp.startGame();
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

		Iterator it = waitablesImWaitingFor.waitFor(ausgabennotifyto);

		while (it.hasNext())
		    ausgabenEintrittsListe.remove(it.next());

		d("Kopiere ael: "+ausgabenEintrittsListe.size());
		for (Iterator iter=ausgabenEintrittsListe.iterator();iter.hasNext();){
		    ServerAusgabeThread tmp=(ServerAusgabeThread)iter.next();
		    tmp.setMode(KEINEFRAGEN);
		    iter.remove();     // aus ael
		    d("Addiere einen zu aThreads hinzu");
		    aThreads.addElement(tmp);
		}
	    } // synchronized ausgabenEL
	} // sync aThreads
    }

    // returns false if interrupted, true if all is ok
    private synchronized boolean anmeldung(){
	registrationManager = new RegistrationManager(this);
	registrationManager.beginRegistration();
	CAT.debug("registrationManager gestartet");
	if (isInterrupted())
	    return false;
	try{
	    wait();                       // supposed to be notified from startGame()
	}catch(InterruptedException e){
	    CAT.debug("In der anmeldung interruptiert worden!");

	    return false;
	}
	//Spiel geht los
	startServer.spielGehtLos(this);
	return true;
    }

    private void setzeStartPunkt(){
	// setzen der x-, y-, archivX- und archivY-Koordinaten in den Robots auf
	// die Koordinaten der ersten Flagge
	d("setze x und archivX in robots auf "+feld.getFlaggen()[0].getX());
	d("setze y und archivY in robots auf "+feld.getFlaggen()[0].getY());
	for(int i = 0; i < aktRoboter.size(); i++){
	    ((ServerRoboterThread)(aktRoboter.elementAt(i))).rob.setPos(feld.getFlaggen()[0]);
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

        return (t.rob.getSchaden()-4 < t.rob.gesperrteRegs());
    }

    /** Die eigentliche Spielmethode!
     */
    public void run(){
        setName("ServerThread");

	CAT.debug("MsgThreadStart");
	messageThread=new MessageThread(this, kommto);
	messageThread.start();

	CAT.debug("anmeldung()");
	boolean spielgestartet=anmeldung();
	if (!spielgestartet)
	    return;

	CAT.debug("setzeStartPunkt()");
	setzeStartPunkt();
	CAT.debug("roboterThreadStart()");
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
	//initialisiere Statistik
	stats = new StatsList(alleN);
	feld.initStats(stats);

        ausgabenBenachrichtigen(alleN);

	// 2. Roboter
	d("Spiel starten.");
	modus = SPIELSTART;
	waitablesImWaitingFor = new WaitingForSet(rThreads);
	wechselModus(waitablesImWaitingFor.iterator(), SPIELSTART);
	broadcastUndWarteAufRoboter();
	d("Spiel ist gestartet.");
	// schicken des ersten MNR an alle Roboter ohne die Anzahl der Leben zu reduzieren
	d("Initiale Ausrichtung holen.");
	modus = INITAUSR;
	waitablesImWaitingFor = new WaitingForSet(aktRoboter);
	wechselModus(waitablesImWaitingFor.iterator(), INITAUSR);
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
	    sendMsg("mNeueRunde",tmpstr);

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
		sendMsg("mZerstSync",tmpstr);

		waitablesImWaitingFor = new WaitingForSet(zerstoerteRoboter);
		wechselModus(waitablesImWaitingFor.iterator(), ZERSTOERT_SYNC);
		warteAufRoboter();
		wechselModus(zerstoerteRoboter.iterator(), ZERSTOERT_ASYNC);
		synchronized(roboterEintrittsListe){
		    alleN=new String[roboterEintrittsListe.size()];
		    int i=0;
		    for(Iterator e=roboterEintrittsListe.iterator();e.hasNext();)
			alleN[i++]=((ServerRoboterThread)e.next()).rob.getName();
		}
		if (alleN.length > 0)
		    ausgabenBenachrichtigen(alleN);
	    }
	    d("Es werden "+roboterEintrittsListe.size()+" nach ihrer Zerstörung wieder eingesetzt");

	    String[] namen;   // fuer notifyChange
	    Vector toBeAsked = new Vector();
	    synchronized(roboterEintrittsListe) {
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
		    aktRoboter.add(tmp);
		    toBeAsked.add(tmp);
		    namen[idx++]=tmp.rob.getName();
		}
		roboterEintrittsListe.removeAllElements();
	    }

	    ausgabenBenachrichtigen(namen);

	    //PowerUp -- nach Zerstoert eingesetzte Roboter werden auch gefragt!
	    // Sind bereits in vorheriger Liste in toBeAsked gesetzt worden

	    for(Iterator e=aktRoboter.iterator();e.hasNext();) {
		ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		if (!tmp.rob.istAktiviert())
		    toBeAsked.add(tmp);
		else if (tmp.rob.isNaechsteRundeDeaktiviert()) {
		    //Auschalten der, die letzte Mal PowerDown gesagt haben.
		    d(tmp.rob.getName()+" ist naechste Runde ausgeschaltet.");
		    tmp.rob.setAktiviert(false);
		    tmp.rob.setNaechsteRundeDeaktiviert(false);
                    tmp.rob.setSchaden(0);
		}

	    }
	    if (toBeAsked.size() > 0) {
		d("Ich frage "+toBeAsked.size()+" nach Powerup.");

		tmpstr=new String[1];
		Iterator iter=toBeAsked.iterator();
		tmpstr[0]=(((ServerRoboterThread)iter.next()).rob.getName());
		while (iter.hasNext())
		    tmpstr[0]=tmpstr[0]+", "+(((ServerRoboterThread)iter.next()).rob.getName());
		sendMsg("mPowUpFrage",tmpstr);

		modus = POWERUP;
		wechselModus(toBeAsked.iterator(), POWERUP);
		waitablesImWaitingFor=new WaitingForSet(toBeAsked);
		broadcastUndWarteAufRoboter();
		d("Powerup fertig.");

		namen=new String[toBeAsked.size()];
		int idx=0;
		for (Iterator e=toBeAsked.iterator();e.hasNext();)
		    namen[idx++]=((ServerRoboterThread)e.next()).rob.getName();
		ausgabenBenachrichtigen(namen);

		for (Iterator it=toBeAsked.iterator();it.hasNext();){
		    ServerRoboterThread srt=((ServerRoboterThread)it.next());
		    if (!srt.rob.istAktiviert())
			srt.rob.setSchaden(0);
		}
	    }

	    // Karten an die Roboter verteilen die nicht zerstoert oder deaktiviert sind
	    // warten auf die Karten der Roboter (Timeout)
	    Karte[] cards = new Karte[gesperrteKarten.size()];
	    for (int i=0; i<gesperrteKarten.size(); i++)
		cards[i] = (Karte )gesperrteKarten.get(i);

	    KartenStapel stapel = new KartenStapel(cards);
	    d("Neuer Stapel: "+stapel);
	    Vector activeThisRound = new Vector();
	    for(Iterator e=aktRoboter.iterator();e.hasNext();) {
		ServerRoboterThread tmp = ((ServerRoboterThread )e.next());
		if (tmp.rob.istAktiviert()) {
		    activeThisRound.addElement(tmp);
		    //Karten geben und dabei *beruecksichtigen*, wie viele der Roboter kriegt!
		    tmp.rob.setKarten(stapel.gibKarte(tmp.rob.anzKarten()));
		}
	    }
	    if (activeThisRound.size() > 0) {
		d("Ich verteile an "+activeThisRound.size()+" Karten.");
		modus = PROGRAMMIERUNG;
		waitablesImWaitingFor = new WaitingForSet(activeThisRound);
		wechselModus(activeThisRound.iterator(), PROGRAMMIERUNG);
		waitablesImWaitingFor.addRemovalListener(robProgListener);
		broadcastUndWarteAufRoboter();
		d("Programmierung zurueckerhalten");
	    }

	    // Auswertung beginnt
	    wechselModus(aktRoboter.iterator(),NIX);
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
			if (tmp.rob.getNaechsteFlagge() == feld.getFlaggen().length+1) {
			    //raus aus aktiven Robos, in Gewinnerliste,
                            // vom Plan nehmen, ausgabenbenachrichtigen
			    tmp.setMode(SPIELENDE);
			    gewinner.addElement(tmp);
			    e.remove(); //aus aktRoboter
			    // Messages....................
			    tmpstr=new String[2];
			    tmpstr[0]=tmp.rob.getName();
			    tmpstr[1]=""+gewinner.size();
			    sendMsg("mGewinn",tmpstr);
			    // ...........................
			    try{
				tmp.deleteMe("GO");
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
                                tmp.rob.setNaechsteRundeDeaktiviert(false);

				zerstoerteRoboter.addElement(tmp);
				tmp.setMode(ZERSTOERT_ASYNC);
				try {
				    tmp.killed();
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
				tmp.setMode(SPIELENDE);
				try {
				    tmp.deleteMe("LL");
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
	    Vector repairing = new Vector();
	    for (Iterator e=aktRoboter.iterator(); e.hasNext();) {
		ServerRoboterThread tmp = (ServerRoboterThread)e.next();
		if(repariereGgf(tmp)) {
		    d("Addiere einen zu repairing hinzu.");
		    repairing.addElement(tmp);
		}
	    }
	    if (repairing.size() > 0) {
		d("Reparaturanfrage stellen an "+repairing.size());
		modus = ENTSPERREN;
		wechselModus(repairing.iterator(),ENTSPERREN);
		waitablesImWaitingFor = new WaitingForSet(repairing);
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

        /* Falls wir jemals ein anderes Ende haben wollen, muß man sich hier nochmal Gedanken
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
	   deleteRoby(tmp, "GO");
	   }*/

	d("Es sind noch "+aThreads.size()+" Ausgaben da.");
        if (aThreads.size()>0){
            synchronized(aThreads){
                for (Iterator e=aThreads.iterator();e.hasNext();){
                    ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
		    tmp.setMode(FRAGENERLAUBT);
                    try{
                        tmp.deleteMe("GO");
                    }
                    catch (KommException ex){
                        d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
                    }
                } //for
                d("Alle AusgabeThreads vom Spielende benachrichtigt und in den richtigen Modus versetzt.");

		waitablesImWaitingFor = new WaitingForSet(aThreads);
		waitablesImWaitingFor.waitFor(ausgabennotifyto);

                for (Iterator e=aThreads.iterator();e.hasNext();){
                    ServerAusgabeThread tmp=(ServerAusgabeThread)e.next();
                    try{
			tmp.endGame();
                    }
                    catch (java.io.IOException ex){
                        d("IOException aufgetreten.");
                    }
                }
            } // synchronized aThreads
        } // if aThreads > 0

	// AnmeldeThread killen
        try{
            registrationManager.seso.close();
        }
        catch (java.io.IOException ex){
            d("IOException beim AnmeldeThreadKillen.");
        }

	// MessageThread killen
	messageThread.interrupt();

	//leo
	d("rufe spielZuEnde() beim StartServer auf");
	startServer.spielZuEnde(this);

	d("Ende meiner run()-Methode erreicht!!!");
    }// run() ende

    private void d(String s){
	Global.debug(this, s);
    }

    private RobProgListener robProgListener = new RobProgListener();
    private class RobProgListener implements RemovalListener{
	public void waitableRemoved(Waitable w){
	    sendMsg("mProgReceived", new String[] { ((ServerRoboterThread)w).rob.getName() } );
	    if (waitablesImWaitingFor.size()==1)
		sendMsg("mLastProg", new String[] { ((ServerRoboterThread)waitablesImWaitingFor.getElement()).rob.getName() } );
	}
    }

    synchronized boolean isGameStarted() {
        return gameStarted;
    }

    synchronized public void startGame() {
      gameStarted = true;
      notify();
    }
}// class Server ende
