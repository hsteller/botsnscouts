/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package de.botsnscouts.server;

import java.util.*;

import de.botsnscouts.board.*;
import de.botsnscouts.comm.*;
import de.botsnscouts.start.*;
import de.botsnscouts.util.*;

import org.apache.log4j.Category;

public class Server extends BNSThread implements ModusConstants, ServerOutputThreadMaintainer,
        InfoRequestAnswerer, OKListener, ServerRobotThreadMaintainer, ThreadMaintainer {
    static final Category CAT = Category.getInstance(Server.class);

    // Vektoren
    private Vector aThreads = new Vector();	// ServerAusgabeThread
    private Vector ausgabenEintrittsListe = new Vector();	// ServerAusgabeThread

    private Vector rThreads = new Vector();	// ServerRoboterThread
    private Vector aktRoboter = new Vector();	// ServerRoboterThread
    private Vector zerstoerteRoboter = new Vector();	// ServerRoboterThread
    private Vector roboterEintrittsListe = new Vector();	// ServerRoboterThread
    private Vector roboterFriedhof = new Vector();	// ServerRoboterThread (endgültig tote)
    private Vector gewinner = new Vector(); //ServerRoboterThread (Letzte Fahne erreicht)

    private RegistrationManager registrationManager;
    private MessageThread messageThread;

    private StSpListener gameStartListener;

    protected SimBoard feld;

    protected String[] angemeldet = new String[8];
    private boolean gameover = false;
    private boolean gameStarted = false;
    private int aktPhase = 0; // enthaelt die Nummer der gerade auswertenden Phase. 0 wenn nicht ausgewertet wird

    // Timeouts
    private int kommto; //Für Dinge, die nur eine Reaktion erwarten, kein Nachdenken, z.B. Spielstart
    private final int rundenbeginnto = 60000; // Falls noch zerstoerte Bot fehlen
    private final int ausgabennotifyto = 60000;
    protected final int anmeldeto = 60000; // So lange wartet der ServerAnmeldeThread auf eine Aktion

    //Modi, ihre Bearbeitung und Synchronistaion

    private int modus; //Aktueller Bearbeitungsmodus, die Clients haben eigene.
    private WaitingForSet waitablesImWaitingFor;

    // um die Statistikdaten zu halten
    protected StatsList stats;

    /* Options for this game. */
    private GameOptions options;

    // Modus-Konstanten
    // 1. Server / ServerRoboterThread
    // 2. ServerAusgabeThread

    // Mandated by OKListener

    /**
     * This constructor is called once per Game by StartServer.
     *
     */
    public Server(GameOptions options,
                  StSpListener listener)
    {
        gameStartListener = listener;
        this.options = options;
        try {
            feld = new SimBoard(options.getX(),
                    options.getY(),
                    options.getBoard(),
                    options.getFlags(),
                    this);
        } catch (FormatException e) {
            System.err.println("Fehler im Spielfeldstring.");
            System.exit(5);
        } catch (FlagException e) {
            System.err.println("Fehler in den Flaggen.");
            System.exit(5);
        }

        CAT.debug("New server with these options: "+options);
    }


    /** Removes caller from waitablesImWaitingFor
     */
    public void notifyDone(Waitable me) {
        CAT.debug("Server: notify done");
        waitablesImWaitingFor.removeAndNotify(me);
    }

    // Methods needed for ServerRobotThreadMaintainer

    /** Entfernt den Thread, benachrichtigt den Spieler (dessen Kommverbindung moeglicherweise
     *  abgeschmiert ist!! -> nicht ewig warten), entfernt den Bot aus aktiv, wiederein-
     *  trittsliste oder friedhof
     * @param t - der ServerRoboterThread des zu entfernenden Roboters
     * @param grund - "TO" wegen timeout oder "RV" wg. Regelverletzung oder....
     */
    public void deleteRob(ServerRoboterThread t, String grund) {
        d("deleteRob aufgerufen. robname=" + t.rob.getName() + "; grund=" + grund);
        waitablesImWaitingFor.removeAndNotify(t);
        try {
            t.rob.setLives(0); // Hendrik: dont know whether this is the right place to do..
            t.deleteMe(grund);
        } catch (KommFutschException ex) {
            new Fehlermeldung(Message.say("Server", "eKommFutschR", t.rob.getName()));
        } catch (KommException ex) {
            d("Bot " + t.rob.getName() + " konnte nicht mehr von seiner Entfernung wg. " + grund + " benachrichtigt werden: " + ex);
        }

        t.interrupt();     // Beende den Thread bei nächster Gelegenheit

        rThreads.remove(t);
        aktRoboter.remove(t);
        roboterFriedhof.add(t);

        String[] tmpstr = new String[1];
        tmpstr[0] = t.rob.getName();
        if (grund.startsWith(MessageID.BOT_REMOVED))
            sendMsg(MessageID.BOT_REMOVED + grund, tmpstr);
        else if (grund.equals(MessageID.SOMEONE_QUIT))
            sendMsg(MessageID.SOMEONE_QUIT, tmpstr);

        gameover = istSpielende();
    }

    public int getTurnTimeout() {
        return options.getHandInTimeout();
    }

    public void sendMsg(String id, String arg) {
        String[] tmp = new String[1];
        tmp[0] = arg;
        sendMsg(id, tmp);
    }

    /** Schickt eine Nachricht an alle Ausgaben >= Version 2.0 */
    public void sendMsg(String id, String[] s) {
        if (id.equals(de.botsnscouts.comm.MessageID.WISE_USED)) {
// Dirk wants that to be counted :-)
            Stats st = stats.getStats(s[0]);
            if (st != null)
                st.incAskWisenheimer();
        }
        messageThread.append(id, s);
        Thread.yield();                   // allow the message to be sent
    }

    public void reEntry(ServerRoboterThread s) {
        zerstoerteRoboter.removeElement(s);
        roboterEintrittsListe.addElement(s);
    }

    // Methods mandated by interface ServerOutputThreadMaintainer

    public void deleteOutput(ServerAusgabeThread t, String grund) {
        synchronized (aThreads) {
            d("deleteOutput aufgerufen. grund=" + grund);
            try {
                waitablesImWaitingFor.removeAndNotify(t);
                t.deleteMe(grund);
            } catch (KommException ex) {
                d("Ausgabe konnte nicht mehr von ihrer Entfernung wg. " + grund + " benachrichtigt werden: " + ex);
            }

            t.interrupt(); // Beende sie
            aThreads.remove(t);
        }
    }

    public int getOutputTimeout() {
        return ausgabennotifyto;
    }

    // Methods mandated by interface ThreadMaintainer
    public Vector getActiveOutputs() {
        return aThreads;
    }

    public MOKListener getMOKListener() {
        return messageThread;
    }

    public OKListener getOKListener() {
        return this;
    }

    public ServerRobotThreadMaintainer getRobThreadMaintainer() {
        return this;
    }

    public ServerOutputThreadMaintainer getOutputThreadMaintainer() {
        return this;
    }

    public InfoRequestAnswerer getInfoRequestAnswerer() {
        return this;
    }

    public void updateNewBot(String name, int color) {
        if (gameStartListener != null) {
            gameStartListener.updateNewBot(name, color);
        }
    }

    public int getSignUpTimeout() {
        return anmeldeto;
    }

    public int getMaxPlayers() {
        return options.getMaxPlayers();
    }

    public boolean isScoutAllowed (){
        return options.isAllowScout();
    }
    
    public boolean isWisenheimerAllowed(){
        return options.isAllowWisenheimer();
    }
    
    int getRegistrationPort() {
        return options.getRegistrationPort();
    }

    public void addOutput(ServerAusgabeThread s) {
        d("Addiere Ausgabe zu Eintrittsliste hinzu");
        ausgabenEintrittsListe.addElement(s);
    }

    public void addRobotThread(ServerRoboterThread s) {
        rThreads.addElement(s);
        aktRoboter.addElement(s);
    }

    public synchronized int allocateColor(int color, String name) {
        if ((color > 0) && (angemeldet[color - 1] == null))
            color--;
        else {
            color = (int) (Math.random() * 7 + 1);
            while (angemeldet[color] != null)
                color = (color + 1) % 8;
        }
        angemeldet[color] = name;
        return color;
    }



    // Methods mandated by interface InfoRequestAnswerer

    public int getFieldSizeX() {
        return feld.getSizeX();
    }

    public int getFieldSizeY() {
        return feld.getSizeY();
    }

    public String getFieldString() {
        return feld.getBoardAsString();
    }

    public Location[] getFlags() {
        return feld.getFlags();
    }

    public String[] getNames() {
        String[] s;
        int len = rThreads.size();
        s = new String[len];
        int i = 0;
        for (Iterator it = rThreads.iterator(); it.hasNext();)
            s[i++] = ((ServerRoboterThread) it.next()).rob.getName();
        return s;
    }

    public Location getRobPos(String name) {
        for (Iterator it = rThreads.iterator(); it.hasNext();) {
            ServerRoboterThread srt = (ServerRoboterThread) it.next();
            if (srt.rob.getName().equals(name))
                return srt.rob.getPos();
        }
        return null;
    }

    public Bot getRobStatus(String robotername) {
        // durchsucht die aktRoboter-, zerstoerteRoboter-, roboterEintrittsListe-
        // und roboterFriedhof-Vector nach roboter mit dem name robotername und liefert
        // eine kopie des entsprechenden roboters zurück, falls kein roboter mit
        // dem name gefunden wird, wird null zurückgegeben
        for (Iterator e = rThreads.iterator(); e.hasNext();) {
            Bot r = ((ServerRoboterThread) e.next()).rob;
            if (r.getName().equals(robotername))
                return r;
        }
        return null;
    }

    public boolean gameRunning() {
        return !gameover;
    }

    public String[] getStanding() {
        String[] s = new String[8];
        int i = 0;
        for (Iterator e = gewinner.iterator(); e.hasNext();) {
            s[i++] = ((ServerRoboterThread) e.next()).rob.getName();
        }
        return s;
    }

    /**
     * liefert eine Liste von Statusobjekten.Ein solcher Statusobjekt enthaelt de NAMEN DES Spielers
     * seine ausgespielten Karten und die Nummer der jetzige Phase.
     * @return Status[ ]
     */
    public Status[] getEvalStatus() {
        CAT.debug("276 for rThreads");
        try {
            synchronized (rThreads) {
                CAT.debug("279: lock rThreads");
                Status[] s = new Status[rThreads.size()];
                for (int i = 0; i < s.length; i++) {
                    s[i] = new Status();
                    ServerRoboterThread tmp = (ServerRoboterThread) rThreads.elementAt(i);
                    s[i].aktPhase = aktPhase;
                    s[i].robName = tmp.rob.getName();
                    s[i].register = new Card[aktPhase];
                    for (int j = 0; j < aktPhase; j++)
                        s[i].register[j] = tmp.rob.getMove(j);
                }
                return s;

            }

        } finally {
            CAT.debug("279 relesase rThreads");
        }

    }

    public String[] getNamesByColor() {
        return angemeldet;
    }

    public StatsList getStats() {
        return stats;
    }

    // Konstruktor


    /*
    * Tell all boards that "something has changed"
    * @param robotNames List of all robots whose state changed.
    */

    public void notifyViews(String[] robotNames) {
        notifyViews(-1, robotNames);
    }

    public void notifyViews(int msgNum, String[] robotNames) {

        // PRE: currentThread ist der ServerThread
        //  ... aber wir pruefen das lieber nochmal :-)

        if (Thread.currentThread() != this) {
            d("sendMsgWennNoetig: DAS IST NICHT DER SERVERTHREAD HIER, sondern " + Thread.currentThread());
            throw new RuntimeException("nur der Serverthread darf Server.sendMsgWennNoetig aufrufen.");
        }

        if (robotNames.length == 0) {
            CAT.debug("Warning: ausgabenBenachrichtigen called with 0-size array. Returning.");
            return;
        }

        d("Größe der eintrittsliste: " + ausgabenEintrittsListe.size() + "; aThreads: " + aThreads.size());
        setzeAusgaben();         // neue Ausgaben begrüßen
        d("Größe der eintrittsliste: " + ausgabenEintrittsListe.size() + "; aThreads: " + aThreads.size());

        CAT.debug("372 wait For athreads");
        synchronized (aThreads) {
            CAT.debug("374 lock aThreads");
            waitablesImWaitingFor = new WaitingForSet(aThreads);

            for (Iterator it = aThreads.iterator(); it.hasNext();) {
                ServerAusgabeThread tmp = (ServerAusgabeThread) it.next();
                if (!tmp.isAlive()) {
                    it.remove(); //aus aThreads
                    waitablesImWaitingFor.remove(tmp);
                    continue;
                }

                tmp.setMode(FRAGENERLAUBT);

                try {
                    tmp.notifyChange(msgNum, robotNames);
                } catch (KommFutschException ex) {
                    new Fehlermeldung(Message.say("Server", "eKommFutschA"));
                } catch (KommException ex) {
                    d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
                }
            } //for
            d("Alle AusgabeThreads benachrichtigt und in den richtigen Modus versetzt.");

            if (aThreads.size() == 0)
                return;              // die Muehe koennen wir uns dann auch gerade schenken...

            d("Der Server wartet jetzt " + ausgabennotifyto + " Millisek. auf seine AusgabenThreads (aenderung()).");
            Iterator it = waitablesImWaitingFor.waitFor(ausgabennotifyto);

            while (it.hasNext())
                deleteOutput((ServerAusgabeThread) it.next(), "TO");
        }
        CAT.debug("374 release aThread");
        // allow possibly generated messages to be sent
        // synchronizes laser-fire-animations.
        messageThread.blockUntilQEmpty();
        Thread.yield();
    }

    // Private methods needed by run()

    /** Modus in ServerRoboterThreads setzen und warten, bis sie zu Potte kommen.
     *  Wird bei allen Modi außer ZERSTÖRT benutzt.
     *  PRE: Attribute rThreadsAufDieIchWarte und modus sind korrekt belegt,
     *       außerdem muessen die Bot korrekt initialisiert sein,
     *       konkret: Bei Programmierungsmodus wird auf die Karten zugegriffen.
     *  POST: Der Modus wurde von unseren entsprechenden Threads bearbeitet
     */
    private void broadcastUndWarteAufRoboter() {

        for (Iterator iter = rThreads.iterator(); iter.hasNext();) {
            ServerRoboterThread tmp = (ServerRoboterThread) iter.next();
            if (!tmp.isAlive()) {
                iter.remove();  // aus waitablesImWaitingFor (also aktRoboter, etc)
                // WARNING: might be wrong :)
                waitablesImWaitingFor.remove(tmp);
                String[] s = new String[1];
                s[0] = tmp.rob.getName();
                sendMsg("mAbmeldung", s);
            }
        }

        if (waitablesImWaitingFor.isEmpty())
            return;

        //Broadcast an die betroffenen Threads, Inhalt je nach Modus
        CAT.debug("442 wait for waitablesImWaitingFor");
        synchronized (waitablesImWaitingFor) {
            CAT.debug("444 lock waitablesImWaitingFor");
            for (Iterator e = waitablesImWaitingFor.iterator(); e.hasNext();) {
                ServerRoboterThread srt = (ServerRoboterThread) e.next();

                try {
                    switch (modus) {
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
                            srt.registerRepair(srt.rob.countLockedRegisters() - srt.rob.getDamage() + 4);
                            break;
                        case SPIELENDE:
                            srt.deleteMe("GO");
                            break;
                    }

                } catch (KommFutschException ex) {
                    new Fehlermeldung(Message.say("Server", "eKommFutschR", srt.rob.getName()));
                } catch (KommException ex) {
                    deleteRob(srt, OtherConstants.REASON_RULE_VIOLATION);
                }
            }//for Iterator
        }//synch
        CAT.debug("444 release waitablesImWaitingFor");
        //Schlafen bis TO oder alle fertig
        Iterator it = warteAufRoboter();

        //Falls welche nicht fertig geworden, entfernen
        while (it.hasNext()) {
            ServerRoboterThread tmp = (ServerRoboterThread) it.next();
            d("!!!ServerRoboterThread " + tmp.rob.getName() + " rauswerfen wegen Timeout!!!");
            it.remove();
            deleteRob(tmp, OtherConstants.REASON_TIMEOUT);
        }
    }

    /** Wartet -- je nach Modus -- eine bestimmte Zeit auf wiederkehrende
     *  ServerRoboterThreads oder eben bis alle ferig sind.
     * PRE: rThreadsAufDieIchWarte und modus ist korrekt gesetzt
     */
    private synchronized Iterator warteAufRoboter() {
        CAT.debug("498 lock Server");
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
                to = options.getHandInTimeout();
                break;
            case ZERSTOERT_SYNC:
            case ZERSTOERT_ASYNC:
                to = rundenbeginnto;
                break;
            default:
                to = 0;

        }
        CAT.debug("498 release Server");
        d("Der Server wartet jetzt " + to + " Millisek. auf seine RoboterThreads (" + modus + ").");
        return waitablesImWaitingFor.waitFor(to);
    }

    /** Setzt Modus in ServerRoboterThreads um
     * @param it  umzusetzenden ServerRoboterThreads
     * @param newMode Modus in den die Threads gesetzt werden sollen
     */
    private void wechselModus(Iterator it, int newMode) {
        while (it.hasNext()) {
            ServerRoboterThread t = (ServerRoboterThread) it.next();
            t.setMode(newMode);
        }
    }

    /** Uebertraegt ausgabeneintrittsliste in offizielle Ausgabenliste */
    private void setzeAusgaben() {
        d("setzeAusgaben");
        d("539 wait for aThreads");
        synchronized (aThreads) {
            d("541 lock on aThreads\nwait for ausgabenEL");
            synchronized (ausgabenEintrittsListe) {
                d("543 lock on ausgabenEintrittsListe");
                if (ausgabenEintrittsListe.size() == 0)
                    return;
                else
                    d("Es gibt neue Ausgaben. Begrüße sie.");

                waitablesImWaitingFor = new WaitingForSet(ausgabenEintrittsListe);

                for (Iterator e = ausgabenEintrittsListe.iterator(); e.hasNext();) {
                    ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                    tmp.setMode(FRAGENERLAUBT);
                    try {
                        tmp.startGame();
                    } catch (KommFutschException ex) {
                        new Fehlermeldung(Message.say("Server", "eKummFutschA"));
                    } catch (KommException ex) {
                        d("setzeAusgaben: Es ist eine KommException aufgetreten.");
                    }
                    tmp.start();
                } //for
                d("Alle ael-s benachrichtigt und in den richtigen Modus versetzt.");

                Iterator it = waitablesImWaitingFor.waitFor(ausgabennotifyto);

                while (it.hasNext())
                    ausgabenEintrittsListe.remove(it.next());

                d("Kopiere ael: " + ausgabenEintrittsListe.size());
                for (Iterator iter = ausgabenEintrittsListe.iterator(); iter.hasNext();) {
                    ServerAusgabeThread tmp = (ServerAusgabeThread) iter.next();
                    tmp.setMode(KEINEFRAGEN);
                    iter.remove();     // aus ael
                    d("Addiere einen zu aThreads hinzu");
                    aThreads.addElement(tmp);
                }
            } // synchronized ausgabenEL
            CAT.debug("543 release ausgabenEl");
        } // sync aThreads
        CAT.debug("541 release aThreads");
    }

    // returns false if interrupted, true if all is ok
    private synchronized boolean anmeldung() {
        CAT.debug("588 lock on Server");
        try {
            registrationManager = new RegistrationManager(this);
            registrationManager.beginRegistration();
            CAT.debug("registrationManager gestartet");
            if (isInterrupted())
                return false;
            try {
                wait();                       // supposed to be notified from startGame()
            } catch (InterruptedException e) {
                CAT.debug("In der anmeldung interruptiert worden!");

                return false;
            }
            //Spiel starts!
            if (gameStartListener != null){
                gameStartListener.updateStartGame();
            }
            return true;
        } finally {
            CAT.debug("588 release Server");
        }
    }

    private void setzeStartPunkt() {
        // setzen der x-, y-, archivX- und archivY-Koordinaten in den Robots auf
        // die Koordinaten der ersten Flagge
        if (feld == null)
            CAT.error("feld ist null");
        d("setze x und archivX in robots auf " + feld.getFlags()[0].getX());
        d("setze y und archivY in robots auf " + feld.getFlags()[0].getY());
        for (int i = 0; i < aktRoboter.size(); i++) {
            ((ServerRoboterThread) (aktRoboter.elementAt(i))).rob.setPos(feld.getFlags()[0]);
            ((ServerRoboterThread) (aktRoboter.elementAt(i))).rob.touchArchive();
        }
    }

    private void roboterThreadStart() {
        for (int i = 0; i < rThreads.size(); i++) {
            ((ServerRoboterThread) (rThreads.elementAt(i))).start();
        }
        d("ServerRoboterThreads wurden gestartet");
    }

    /** Sind noch Bot dabei?
     */
    private boolean istSpielende() {
        return (rThreads.size() == 0) || ((aktRoboter.size() == 0)
                && (zerstoerteRoboter.size() == 0)
                && (roboterEintrittsListe.size() == 0));
    }

    /** Repariere Schaden, falls auf Reparaturfeld oder Flagge
     * Liefert true, falls erst erfragt werden muss, welche  Register entsperren.werden sollen.
     */
    private boolean repariereGgf(ServerRoboterThread t) {
        if (t.rob.countLockedRegisters() == 0)
            return false;
        if ((t.rob.countLockedRegisters() > 0) && (t.rob.getDamage() < 5)) {
            t.rob.unlockAllRegisters();
            return false;
        }

        return (t.rob.getDamage() - 4 < t.rob.countLockedRegisters());
    }

    /**
     * The main method of the server!
     */
    public void run() {
        try {
            setName("ServerThread");

            CAT.debug("MsgThreadStart");
             messageThread = new MessageThread(this, kommto);
             messageThread.start();

             CAT.debug("anmeldung()");
             boolean spielgestartet = anmeldung();
             if (!spielgestartet) {
                 messageThread.interrupt();
                 return;
             }

            CAT.debug("setzeStartPunkt()");
            setzeStartPunkt();
            CAT.debug("roboterThreadStart()");
            roboterThreadStart();
            // warten auf NTS von Ausgabekanaelen und Robotern
// 1. Ausgaben NTSen und NTCen, damit Location schonmal stimmt
            // kreiere String[] mit allen Namen
            String[] alleN;
            CAT.debug("wait for rThreads");
            synchronized (rThreads) {
                CAT.debug("have rThreads");
                alleN = new String[rThreads.size()];
                int i = 0;
                for (Iterator e = rThreads.iterator(); e.hasNext();)
                    alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
            }
            CAT.debug("released rThreads");
            //Init Statistics.
            stats = new StatsList(alleN);
            feld.initStats(stats);

            notifyViews(alleN); 

// 2. Bot
            sendMsg("mWelcome", "");
           
            d("Spiel starten.");
            modus = SPIELSTART;
            waitablesImWaitingFor = new WaitingForSet(rThreads);
            wechselModus(waitablesImWaitingFor.iterator(), SPIELSTART);
            broadcastUndWarteAufRoboter();
            d("Spiel ist gestartet.");
// schicken des ersten MNR an alle Bot ohne die Anzahl der Leben zu reduzieren
            d("Initiale Ausrichtung holen.");
            modus = INITAUSR;
            waitablesImWaitingFor = new WaitingForSet(aktRoboter);
            wechselModus(waitablesImWaitingFor.iterator(), INITAUSR);
            broadcastUndWarteAufRoboter();
            d("InitialeAusrichtung ist geholt.");
            // Ausgabekanaele von Initialausrichtung benachrichtigen
            // kreiere String[] mit allen Namen
            CAT.debug("708 wait for rthreads");
            synchronized (rThreads) {
                CAT.debug("710 lock rthreads");
                alleN = new String[rThreads.size()];
                int i = 0;
                for (Iterator e = rThreads.iterator(); e.hasNext();)
                    alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
            }
            CAT.debug("716 release rthreads");
            notifyViews(alleN); // HS: HIERHIERHIER initiale Ausrichtungen
            sendMsg(MessageID.INITIAL_FACINGS, alleN);
            
            // Loop for all turns.
            gameover = false;
            Vector gesperrteKarten = new Vector(); //Vector of cards!
            for (int iRunde = 1; !gameover; iRunde++) {

                String[] tmpstr = new String[1];
                tmpstr[0] = "" + iRunde;
                sendMsg("mNeueRunde", tmpstr);

// Evtl. auf wiedereintretende Bot warten
                for (Iterator iter = zerstoerteRoboter.iterator(); iter.hasNext();)
                    if (!(((ServerRoboterThread) iter.next()).isAlive()))
                        iter.remove();

                if (zerstoerteRoboter.size() > 0) {

                    d("Warte kurz auf zerstoerte Bot.");
                    modus = ZERSTOERT_SYNC;

                    tmpstr = new String[1];
                    Iterator iter = zerstoerteRoboter.iterator();
                    tmpstr[0] = (((ServerRoboterThread) iter.next()).rob.getName());
                    while (iter.hasNext())
                        tmpstr[0] = tmpstr[0] + ", " + (((ServerRoboterThread) iter.next()).rob.getName());
                    sendMsg("mZerstSync", tmpstr);

                    waitablesImWaitingFor = new WaitingForSet(zerstoerteRoboter);
                    wechselModus(waitablesImWaitingFor.iterator(), ZERSTOERT_SYNC);
                    warteAufRoboter();
                    wechselModus(zerstoerteRoboter.iterator(), ZERSTOERT_ASYNC);
                    CAT.debug("wait for roboterEL");
                    synchronized (roboterEintrittsListe) {
                        CAT.debug("have roboterEL");
                        alleN = new String[roboterEintrittsListe.size()];
                        int i = 0;
                        for (Iterator e = roboterEintrittsListe.iterator(); e.hasNext();)
                            alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
                    }
                    CAT.debug("release roboterEL");
                    if (alleN.length > 0)
                        notifyViews(alleN); 
                }
                d("Es werden " + roboterEintrittsListe.size() + " nach ihrer Zerstörung wieder eingesetzt");

                String[] namen;   // fuer notifyChange
                Vector toBeAsked = new Vector();
                CAT.debug("wait for roboterEL");
                synchronized (roboterEintrittsListe) {
                    CAT.debug("have roboterEL");
                    d("Lasse Bot wieder eintreten.");
                    for (Iterator e = roboterEintrittsListe.iterator(); e.hasNext();) {
// Alle wiedereinzusetzenden Bot auf ihre Archivpos setzen.
                        // In eigener while-Schleife, damit danach wiedereinsetzen korrekt geprueft
                        // wird!
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        tmp.rob.toArchive();
                    }
                    for (Iterator e = roboterEintrittsListe.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        // Ggf. devirtualisieren.
                        // Achtung, auch aktRoboter beruecksichtigen, die aber reell bleiben.
                        tmp.rob.setVirtual(false);  //Default: Real einsetzen, ABER....
                        // Virtuell, wenn ein aktiver auch da steht
                        for (Iterator f = aktRoboter.iterator(); f.hasNext();) {
                            Bot anderer = ((ServerRoboterThread) f.next()).rob;
                            if (tmp.rob.getX() == anderer.getX() && tmp.rob.getY() == anderer.getY()) {
                                tmp.rob.setVirtual();
                                break;
                            }
                        }
                        // oder noch ein anderer der wiedereinzusetzenden auch da steht
                        if (!tmp.rob.isVirtual()) {
                            for (Iterator f = roboterEintrittsListe.iterator(); f.hasNext();) {
                                Bot anderer = ((ServerRoboterThread) f.next()).rob;
                                if (anderer != tmp.rob && anderer.getX() == tmp.rob.getX() && anderer.getY() == tmp.rob.getY()) {
                                    tmp.rob.setVirtual();
                                }
                            }
                        }
                    }

                    namen = new String[roboterEintrittsListe.size()];
                    int idx = 0;
                    // Jetzt wirklich wieder einteten lassen
                    for (Iterator e = roboterEintrittsListe.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        aktRoboter.add(tmp);
                        toBeAsked.add(tmp);
                        CAT.debug("Adding " + tmp.rob + " to toBeAsked 1");
                        namen[idx++] = tmp.rob.getName();
                    }
                    roboterEintrittsListe.removeAllElements();
                }
                CAT.debug("have roboterEL");

                notifyViews(namen);

/* Power Up!
                *  Ask just entering bots whether they want to be powered up again.
                */
                for (Iterator e = aktRoboter.iterator(); e.hasNext();) {
                    ServerRoboterThread robThread = ((ServerRoboterThread) e.next());
                    if (!robThread.rob.isActivated()) {
                        if (robThread.rob.isPoweredDownNextTurn()) {
//This bot just chose PowerDown.
                            robThread.rob.setNextTurnPoweredDown(false);
                            robThread.rob.setDamage(0);
                        } else {
                            if (!toBeAsked.contains(robThread)) {
                                toBeAsked.add(robThread);
                                CAT.debug("Adding " + robThread.rob + " to toBeAsked 2");
                            }
                        }
                    }
                }
                if (toBeAsked.size() > 0) {
                    d("Ich frage " + toBeAsked.size() + " nach Powerup.");

                    tmpstr = new String[1];
                    Iterator iter = toBeAsked.iterator();
                    tmpstr[0] = (((ServerRoboterThread) iter.next()).rob.getName());
                    while (iter.hasNext())
                        tmpstr[0] = tmpstr[0] + ", " + (((ServerRoboterThread) iter.next()).rob.getName());
                    sendMsg("mPowUpFrage", tmpstr);

                    modus = POWERUP;
                    wechselModus(toBeAsked.iterator(), POWERUP);
                    waitablesImWaitingFor = new WaitingForSet(toBeAsked);
                    broadcastUndWarteAufRoboter();
                    d("Powerup fertig.");

                    namen = new String[toBeAsked.size()];
                    int idx = 0;
                    for (Iterator e = toBeAsked.iterator(); e.hasNext();)
                        namen[idx++] = ((ServerRoboterThread) e.next()).rob.getName();
                    notifyViews(namen);

                    for (Iterator it = toBeAsked.iterator(); it.hasNext();) {
                        ServerRoboterThread srt = ((ServerRoboterThread) it.next());
                        if (!srt.rob.isActivated())
                            srt.rob.setDamage(0);
                    }
                }

// Karten an die Bot verteilen die nicht zerstoert oder deaktiviert sind
// warten auf die Karten der Bot (Timeout)
                Card[] cards = new Card[gesperrteKarten.size()];
                for (int i = 0; i < gesperrteKarten.size(); i++)
                    cards[i] = (Card) gesperrteKarten.get(i);

                Deck stapel = new Deck(cards);
                d("Neuer Stapel: " + stapel);
                Vector activeThisRound = new Vector();
                for (Iterator e = aktRoboter.iterator(); e.hasNext();) {
                    ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                    if (tmp.rob.isActivated()) {
                        activeThisRound.addElement(tmp);
//Karten geben und dabei *beruecksichtigen*, wie viele der Bot kriegt!
                        tmp.rob.setCards(stapel.handOutCards(tmp.rob.cardsToGive()));
                    }
                }
                if (activeThisRound.size() > 0) {
                    d("Ich verteile an " + activeThisRound.size() + " Karten.");
                    modus = PROGRAMMIERUNG;
                    waitablesImWaitingFor = new WaitingForSet(activeThisRound);
                    wechselModus(activeThisRound.iterator(), PROGRAMMIERUNG);
                    waitablesImWaitingFor.addRemovalListener(robProgListener);
                    broadcastUndWarteAufRoboter();
                    d("Programmierung zurueckerhalten");
                }

                CAT.debug("890 locke aktRoboter..");
                // Auswertung beginnt
                synchronized (aktRoboter) {
                    CAT.debug("893 habe aktRoboter");
                    wechselModus(aktRoboter.iterator(), NIX);
                    modus = NIX;
                }
                CAT.debug("890 frei: aktRoboter");
                // Loop for the five phases.
                for (aktPhase = 1; aktPhase != 0; aktPhase = (aktPhase + 1) % 6) {
                    CAT.info("Evaluation phase " + aktPhase + ", turn " + iRunde + " starts.");
// Am Spiel beteiligte Bot in Array kopieren (aus technischen Gruenden)
                    Bot[] robs;
                    CAT.debug("903 wait for aktRoboter");
                    synchronized (aktRoboter) {
                        CAT.debug("903 have aktRoboter");
                        CAT.debug("");
//<hendrik was here>
                        int removedBots = 0;
                        for (Iterator e = aktRoboter.iterator(); e.hasNext();) {
                            ServerRoboterThread t = ((ServerRoboterThread) e.next());
                            if (t.isAlive()) {
                                CAT.debug(t.getName() + " IS ALIVE");
                            } else {
                                CAT.error(t.getName() + " IS !!NOT!! ALIVE");
//aktRoboter.remove(t);
                                removedBots++;
                            }
                        }

                        robs = new Bot[aktRoboter.size() - removedBots];
                        int i = 0;
                        for (Iterator e = aktRoboter.iterator(); e.hasNext(); i++) {
                            ServerRoboterThread t = ((ServerRoboterThread) e.next());
                            if (t.isAlive() && t.isThere()) {
                                CAT.debug(t.getName() + " IS ALIVE");
                                robs[i] = t.rob;
                            } else {
                                CAT.error(t.getName() + " IS !!NOT!! ALIVE");
                            }

                        }
                    }
                    CAT.debug("903 release  aktRoboter");
                    // (doPhase())
                    //DEBUG
                    d("feld.doPhase(" + aktPhase + ") mit ");
                    for (int i = 0; i < aktRoboter.size(); i++) {
                        d(((ServerRoboterThread) aktRoboter.elementAt(i)).rob.getName() + ": " + ((ServerRoboterThread) aktRoboter.elementAt(i)).rob);
                    }
                    feld.doPhase(aktPhase, robs);

                    // Evaluate what has happends.
                    CAT.debug("945 wait aktRoboter");
                    synchronized (aktRoboter) {
                        CAT.debug("945 have aktRoboter");
                        for (Iterator e = aktRoboter.listIterator(); e.hasNext();) {
                            ServerRoboterThread tmp = (ServerRoboterThread) e.next();
                            // Gewinner?
                            if (tmp.rob.getNextFlag() == feld.getFlags().length + 1) {
                                //raus aus aktiven Robos, in Gewinnerliste,
                                // vom Plan nehmen, ausgabenbenachrichtigen
                                tmp.setMode(SPIELENDE);
                                gewinner.addElement(tmp);
                                e.remove(); //aus aktRoboter
                                // Messages....................
                                tmpstr = new String[2];
                                tmpstr[0] = tmp.rob.getName();
                                tmpstr[1] = "" + gewinner.size();
                                sendMsg("mGewinn", tmpstr);
                                // ...........................
                                try {
                                    tmp.deleteMe("GO");
                                } catch (KommException ex) {
                                    d("Bot " + tmp.rob.getName() + " konnte nicht mehr von seiner Entfernung wg. GO(Gewinn) benachrichtigt werden: " + ex);
                                }

                                tmp.rob.setInvalidPos();
                                tmp.rob.setVirtual();

                                namen = new String[1];
                                namen[0] = tmp.rob.getName();
                                notifyViews(namen);

                                gameover = istSpielende();

                            }
                            // Schaden > 9
                            if (tmp.rob.getDamage() > 9) {
                                e.remove();  //aus aktRoboter
                                tmp.rob.decrLife();

                                // Voellig tot?
                                if (tmp.rob.getLivesLeft() > 0) {
                                    tmp.rob.setDamage(2);
                                    //Register entsperren
                                    tmp.rob.unlockAllRegisters();
                                    tmp.rob.setNextTurnPoweredDown(false);

                                    zerstoerteRoboter.addElement(tmp);
                                    tmp.setMode(ZERSTOERT_ASYNC);
                                    try {
                                        tmp.killed();
                                    } catch (KommFutschException ex) {
                                        new Fehlermeldung(Message.say("Server", "eKommFutschR", tmp.rob.getName()));
                                    } catch (KommException ex) {
                                        System.err.println("Mit Spieler " + tmp.rob.getName() + "ist ein Kommunikationsfehler aufgetreten.");
                                    }
                                } else {
                                    roboterFriedhof.addElement(tmp);
                                    tmp.setMode(SPIELENDE);
                                    try {
                                        tmp.deleteMe("LL");
                                    } catch (KommException ex) {
                                        d("Kommunikationsfehler beim Entfernen eines Roboters");
                                    }
                                    gameover = istSpielende();
                                }

                                namen = new String[1];
                                namen[0] = tmp.rob.getName();
                                notifyViews(namen);
                            }
                        }
                    }
                    CAT.debug("945 release aktRoboter");

                } // End phase evaluation

                /*  End-of-turn-evaluation:
    1.) Repairing issues.
    Relevant for all bots on flags and repair fields.
    */
                Vector repairing = new Vector();
                for (Iterator e = aktRoboter.iterator(); e.hasNext();) {
                    ServerRoboterThread tmp = (ServerRoboterThread) e.next();
                    if (repariereGgf(tmp)) {
                        d("Addiere einen zu repairing hinzu.");
                        repairing.addElement(tmp);
                    }
                }
                if (repairing.size() > 0) {
                    d("Reparaturanfrage stellen an " + repairing.size());
                    modus = ENTSPERREN;
                    wechselModus(repairing.iterator(), ENTSPERREN);
                    waitablesImWaitingFor = new WaitingForSet(repairing);
                    broadcastUndWarteAufRoboter();
                    d("Reparaturanfragen sind beantwortet.");
                }

                // gesperrte Register einsammeln
                d("Gesperrte Register einsammeln.");
                gesperrteKarten.removeAllElements();
                CAT.debug("1050 wait rThreads");
                synchronized (rThreads) {
                    CAT.debug("1050 have rThreads");
                    for (Iterator e = rThreads.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        for (int i = 0; i < 5; i++)
                            if (tmp.rob.getLockedRegister(i) != null)
                                gesperrteKarten.addElement(tmp.rob.getLockedRegister(i));
                    }
                }
                CAT.debug("1050 release rThreads");
                d("Habe " + gesperrteKarten.size() + " gesperrte Karten gefunden und gespeichert.");

/* End-of-turn-evaluation:
                   2.) Set bots to deactivated if they chose power down
                 */
                Vector changedBots = new Vector();
                CAT.debug("1066 wait rThreads");
                synchronized (rThreads) {
                    CAT.debug("1066 have rThreads");
                    for (Iterator it = rThreads.iterator(); it.hasNext();) {
                        Bot bot = ((ServerRoboterThread) it.next()).rob;
                        if (bot.isPoweredDownNextTurn()) {
                            bot.setActivated(false);
                            changedBots.add(bot.getName());
/* NextTurnPowerDown is not set to false here,
                            *  but at the beginning of the next turn to be
                            *  able to distinguish between bots that were
                            *  already powered down and newly deactivated ones.
                            */
                        }
                    }
                }
                CAT.debug("1066 release rThreads");
/* Tell the clients if someone powered down. */
                if (changedBots.size() > 0) {
                    String[] changedBotsArray = new String[changedBots.size()];
                    changedBots.toArray(changedBotsArray);
                    notifyViews(changedBotsArray);
                }

            } // Rundenschleife ende

            d("Das Spiel ist jetzt zu Ende. (Rundenschleife verlassen.)");

/* Falls wir jemals ein anderes Ende haben wollen, muß man sich hier nochmal Gedanken
	       machen (eines, bei dem nicht alle Bot auf der letzten Flagge angekommen sind.

	       if (rThreads.size()>0){
	       modus = SPIELENDE;
	       rThreadsAufDieIchWarte = rThreads;
	       wechselModus(rThreadsAufDieIchWarte, SPIELENDE);
	       broadcastUndWarteAufRoboter();
	       }
	       d("Alle Bot sind vom Spielende benachrichtet worden.");

	       d("Jetzt noch "+rThreads.size()+ "Bot hinrichten!");
	       while (rThreads.size()>0){
	       ServerRoboterThread tmp=(ServerRoboterThread)rThreads.elementAt(0);
	       deleteRoby(tmp, "GO");
	       }*/

            d("Es sind noch " + aThreads.size() + " Ausgaben da.");
            if (aThreads.size() > 0) {
                CAT.debug("1114 wait aThreads");
                synchronized (aThreads) {
                    CAT.debug("1114 have raThreads");
                    for (Iterator e = aThreads.iterator(); e.hasNext();) {
                        ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                        tmp.setMode(FRAGENERLAUBT);
                        try {
                            tmp.deleteMe("GO");
                        } catch (KommException ex) {
                            d("ausgabenBenachrichtigen: Es ist eine KommException aufgetreten.");
                        }
                    } //for
                    d("Alle AusgabeThreads vom Spielende benachrichtigt und in den richtigen Modus versetzt.");

                    waitablesImWaitingFor = new WaitingForSet(aThreads);
                    waitablesImWaitingFor.waitFor(ausgabennotifyto);

                    for (Iterator e = aThreads.iterator(); e.hasNext();) {
                        ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                        try {
                            tmp.endGame();
                        } catch (java.io.IOException ex) {
                            d("IOException aufgetreten.");
                        }
                    }
                } // synchronized aThreads
                CAT.debug("1114 release raThreads");
            } // if aThreads > 0

            // AnmeldeThread killen
            try {
                registrationManager.seso.close();
            } catch (java.io.IOException ex) {
                d("IOException beim AnmeldeThreadKillen.");
            }

            // MessageThread killen
            messageThread.interrupt();

            if (gameStartListener != null) {
                gameStartListener.updateGameFinished();
            }
        } catch (Throwable t) {
            CAT.fatal("Exception:", t);
        }
        d("Ende meiner run()-Methode erreicht!!!");
    }// run() ende

    private void d(String s) {
        CAT.debug(s);
    }

    private RobProgListener robProgListener = new RobProgListener();

    private class RobProgListener implements RemovalListener {
        public void waitableRemoved(Waitable w) {
            CAT.debug("RobProgListener: waitable removed");
            sendMsg("mProgReceived", new String[]{((ServerRoboterThread) w).rob.getName()});
            if (waitablesImWaitingFor.size() == 1) {
                String robName = ((ServerRoboterThread) waitablesImWaitingFor.getElement()).rob.getName();
                stats.getStats(robName).incWasSlowest();
                sendMsg(MessageID.LAST_PROG, new String[]{robName});
            }
        }
    }

    synchronized boolean isGameStarted() {
        try {
            CAT.debug("1183 wait for server");
            return gameStarted;

        } finally {
            CAT.debug("1183 release server");
        }
    }

    synchronized public void startGame() {
        try {
            CAT.debug("1193 wait for server");
            gameStarted = true;
            notify();
        } catch (Exception e) {
            CAT.debug("1197 release server");
        }
    }
}// class Server ende
