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

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Category;

import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.comm.KommException;
import de.botsnscouts.comm.KommFutschException;
import de.botsnscouts.comm.MessageID;
import de.botsnscouts.comm.OtherConstants;
import de.botsnscouts.start.GameOptions;
import de.botsnscouts.start.RegistrationStartListener;
import de.botsnscouts.start.ServerObserver;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Fehlermeldung;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Shutdownable;
import de.botsnscouts.util.Stats;
import de.botsnscouts.util.StatsList;
import de.botsnscouts.util.Status;

public class Server extends BNSThread implements ModusConstants,  ServerOutputThreadMaintainer,
InfoRequestAnswerer, OKListener, ServerRobotThreadMaintainer,
ThreadMaintainer, Shutdownable {
    private static final Category CAT = Category.getInstance(Server.class);
    
    // Vektoren
    private Vector ausgabeThreads = new Vector() /* of ServerAusgabeThread*/;
    private Vector ausgabenEnterRequestedThreads = new Vector() /* of ServerAusgabeThread*/;
    
    private Vector botThreads = new Vector();	// of ServerRoboterThread
    private Vector curBotsThreads = new Vector();	// of ServerRoboterThread
    private Vector destroyedBotsThreads = new Vector();	// of ServerRoboterThread
    private Vector botEnterRequestedThreads = new Vector();	// of ServerRoboterThread
    private Vector graveyardThreads = new Vector();	// of ServerRoboterThread
    private Vector wonThreads = new Vector(); //of ServerRoboterThread
    
    private Vector [] shutdownableCollections = new Vector []{
                    ausgabeThreads, ausgabenEnterRequestedThreads, botThreads, curBotsThreads,
                    destroyedBotsThreads, botEnterRequestedThreads, graveyardThreads, wonThreads
    };
    
    private RegistrationManager registrationManager;
    private MessageThread messageThread;
    
    private ServerObserver serverObserver;
    
    private SimBoard board;
    
    protected String[] players = new String[8];
    private boolean gameover = false;
    private boolean gameStarted = false;
    /**
     * The phase currently under execution, or 0 when not executing.
     */
    private int curPhase = 0;
    
    // Timeouts
    private final int startRoundTO = 60000; // Waiting for destroyed bots
    private final int notifyTO = 60000; // the time views get for updating
    private int commTO = 20000; // MessageThreads wait this long for ACK; Server waits this long for robots at game start("SPIELSTART")
    private final int registerPlayersTO = 60000; // we wait this time for players to join the game
    
    private int mode;
    private WaitingForSet waitablesImWaitingFor;
    
    protected StatsList stats;
    
    /* Options for this game. */
    private GameOptions options;
    
    
    /**
     * This constructor is called once per Game by StartServer.
     */
    public Server(GameOptions options, ServerObserver listener) {
        super("THE_SERVER");
        serverObserver = listener;
        this.options = options;
        try {
            board = new SimBoard(options.getX(), options.getY(), options.getBoard(), options.getFlags(), this);
            board.setPusherCanPushMoreThanOneBot(options.arePushersAbleToPushMultipleBots());
        } catch (FormatException e) {
            CAT.error("Fehler im Spielfeldstring.", e);
            System.exit(5);
        } catch (FlagException e) {
            CAT.error("Fehler in den Flaggen.", e);
            System.exit(5);
        }
        registrationManager = new RegistrationManager(this);
        CAT.debug("New server with these options: " + options);
    }
    
    public ServerObserver getServerObserver(){
        return serverObserver;
    }
    
    /**
     * Removes caller from waitablesImWaitingFor
     */
    public void notifyDone(Waitable me) {
        CAT.debug("Server: notify done");
        waitablesImWaitingFor.removeAndNotify(me);
    }
    
    // Methods needed for ServerRobotThreadMaintainer
    /**
     * Remove the Thread, notify player (if comm is still possible), remove Thread from active / entering /
     * graveyard.
     *
     * @param t     the Thread of the bot to remove
     * @param reason Reason-String
     */
    public void deleteRob(ServerRoboterThread t, String reason) {
        CAT.debug("deleteRob aufgerufen. robname=" + t.rob.getName() + "; grund=" + reason);
        waitablesImWaitingFor.removeAndNotify(t);
        try {
            t.rob.setLives(0); // Hendrik: dont know whether this is the right place to do..
            t.deleteMe(reason);
        } catch (KommFutschException ex) {
            new Fehlermeldung(Message.say("Server", "eKommFutschR", t.rob.getName()));
        } catch (KommException ex) {
            CAT.debug("Bot " + t.rob.getName() + " konnte nicht mehr von seiner Entfernung wg. " + reason + " benachrichtigt werden: " + ex);
        }
        
        t.interrupt();     // Beende den Thread bei n�chster Gelegenheit
        
        botThreads.remove(t);
        curBotsThreads.remove(t);
        graveyardThreads.add(t);
        
        String[] tmpstr = new String[1];
        tmpstr[0] = t.rob.getName();
        if (reason.startsWith(MessageID.BOT_REMOVED)) {
            sendMsg(MessageID.BOT_REMOVED + reason, tmpstr);
        } else if (reason.equals(MessageID.SOMEONE_QUIT)) {
            sendMsg(MessageID.SOMEONE_QUIT, tmpstr);
        }
        
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
    
    /**
     * Send a message to all Ausgeaben >= VV2.0
     */
    public void sendMsg(String id, String[] s) {
        if (id.equals(de.botsnscouts.comm.MessageID.WISE_USED)) {
            // Dirk wants that to be counted :-)
            Stats st = stats.getStats(s[0]);
            if (st != null) {
                st.incAskWisenheimer();
            }
        }
        messageThread.append(id, s);
        Thread.yield();                   // allow the message to be sent
    }
    
    public void reEntry(ServerRoboterThread s) {
        destroyedBotsThreads.removeElement(s);
        botEnterRequestedThreads.addElement(s);
    }
    
    // Methods mandated by interface ServerOutputThreadMaintainer
    
    public void deleteOutput(ServerAusgabeThread t, String reason) {
        synchronized (ausgabeThreads) {
            CAT.debug("deleteOutput called. Reason:" + reason);
            try {
                waitablesImWaitingFor.removeAndNotify(t);
                t.deleteMe(reason);
            } catch (KommException ex) {
                CAT.debug("Could not notify Ausgabe",ex);
            }
            
            t.interrupt();
            ausgabeThreads.remove(t);
        }
    }
    
    public int getOutputTimeout() {
        return notifyTO;
    }
    
    // Methods mandated by interface ThreadMaintainer
    public Vector getActiveOutputs() {
        return ausgabeThreads;
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
        if (serverObserver != null) {
            serverObserver.fireNewBot(name, color);
        }
    }
    
    public int getSignUpTimeout() {
        return registerPlayersTO;
    }
    
    public int getMaxPlayers() {
        return options.getMaxPlayers();
    }
    
    public boolean isScoutAllowed() {
        return options.isAllowScout();
    }
    
    public boolean isWisenheimerAllowed() {
        return options.isAllowWisenheimer();
    }
    
    public boolean arePushersPushingMultipleBots(){
        return options.arePushersAbleToPushMultipleBots();
    }
    
    int getRegistrationPort() {
        return options.getRegistrationPort();
    }
    
    public void addOutput(ServerAusgabeThread s) {
        CAT.debug("Adding output to enterList");
        ausgabenEnterRequestedThreads.addElement(s);
    }
    
    public void addRobotThread(ServerRoboterThread s) {
        botThreads.addElement(s);
        curBotsThreads.addElement(s);
    }
    
    public synchronized int allocateColor(int color, String name) {
        if ((color > 0) && (players[color - 1] == null)) {
            color--;
        } else {
            color = (int) (Math.random() * 7 + 1);
            while (players[color] != null) {
                color = (color + 1) % 8;
            }
        }
        players[color] = name;
        return color;
    }
    
    // Methods mandated by interface InfoRequestAnswerer
    
    public int getFieldSizeX() {
        return board.getSizeX();
    }
    
    public int getFieldSizeY() {
        return board.getSizeY();
    }
    
    public String getFieldString() {
        return board.getBoardAsString();
    }
    
    public Location[] getFlags() {
        return board.getFlags();
    }
    
    public String[] getNames() {
        String[] s;
        int len = botThreads.size();
        s = new String[len];
        int i = 0;
        for (Iterator it = botThreads.iterator(); it.hasNext();) {
            s[i++] = ((ServerRoboterThread) it.next()).rob.getName();
        }
        return s;
    }
    
    public Location getRobPos(String botName) {
        for (Iterator it = botThreads.iterator(); it.hasNext();) {
            ServerRoboterThread srt = (ServerRoboterThread) it.next();
            if (srt.rob.getName().equals(botName)) {
                return srt.rob.getPos();
            }
        }
        return null;
    }
    
    public Bot getRobStatus(String botName) {
        for (Iterator e = botThreads.iterator(); e.hasNext();) {
            Bot r = ((ServerRoboterThread) e.next()).rob;
            if (r.getName().equals(botName)) {
                return r;
            }
        }
        return null;
    }
    
    public boolean gameRunning() {
        return !gameover;
    }
    
    public String[] getStanding() {
        String[] s = new String[wonThreads.size()];
        int i = 0;
        for (Iterator e = wonThreads.iterator(); e.hasNext();) {
            s[i++] = ((ServerRoboterThread) e.next()).rob.getName();
        }
        return s;
    }
    
    public Status[] getEvalStatus() {
        
        
        synchronized (botThreads) {
            
            Status[] s = new Status[botThreads.size()];
            for (int i = 0; i < s.length; i++) {
                s[i] = new Status();
                ServerRoboterThread tmp = (ServerRoboterThread) botThreads.elementAt(i);
                s[i].aktPhase = curPhase;
                s[i].robName = tmp.rob.getName();
                s[i].register = new Card[curPhase];
                for (int j = 0; j < curPhase; j++) {
                    s[i].register[j] = tmp.rob.getMove(j);
                }
            }
            return s;
        }
    }
    
    public String[] getNamesByColor() {
        return players;
    }
    
    public StatsList getStats() {
        return stats;
    }
    
    /*
     * Tell all boards that "something has changed"
     * @param robotNames List of all robots whose state changed.
     */
    
    public void notifyViews(String[] robotNames) {
        notifyViews(-1, robotNames);
    }
    
    public void notifyViews(int msgNum, String[] robotNames) {
        
        // PRE: currentThread is the ServerThread
        // but let's make sure.
        
        if (Thread.currentThread() != this) {
            CAT.debug("This method must be called by the ServerThread, not" + Thread.currentThread());
            throw new RuntimeException();
        }
        
        if (robotNames.length == 0) {
            CAT.debug("0-size array");
            return;
        }
        
        CAT.debug("Size of enterList:" + ausgabenEnterRequestedThreads.size() + "; aThreads: " + ausgabeThreads.size());
        setzeAusgaben();
        CAT.debug("Size of enterList:" + ausgabenEnterRequestedThreads.size() + "; aThreads: " + ausgabeThreads.size());
        
        //  CAT.debug("372 wait For athreads");
        synchronized (ausgabeThreads) {
            //     CAT.debug("374 lock aThreads");
            waitablesImWaitingFor = new WaitingForSet(ausgabeThreads);
            
            for (Iterator it = ausgabeThreads.iterator(); it.hasNext();) {
                ServerAusgabeThread tmp = (ServerAusgabeThread) it.next();
                if (!tmp.isAlive()) {
                    it.remove();
                    waitablesImWaitingFor.remove(tmp);
                    continue;
                }
                
                tmp.setMode(FRAGENERLAUBT);
                
                try {
                    tmp.notifyChange(msgNum, robotNames);
                } catch (KommFutschException ex) {
                    new Fehlermeldung(Message.say("Server", "eKommFutschA"));
                } catch (KommException ex) {
                    CAT.error("while notifying", ex);
                }
            }
            CAT.debug("updated all ausgabeThreads");
            
            if (ausgabeThreads.size() == 0) {
                return;
            }
            
            CAT.debug("Now waiting " + notifyTO + " msec for AusgabeThreads");
            
            // FIXME TODO XXX 
            // there can be ConcurrentModificationExceptions (deleting something from a Vector
            // that we are iterating)
            Iterator it = waitablesImWaitingFor.waitFor(notifyTO);
            while (it.hasNext()) {
                deleteOutput((ServerAusgabeThread) it.next(), "TO");
            }
        }
        //  CAT.debug("374 release aThread");
        // allow possibly generated messages to be sent
        // synchronizes laser-fire-animations.
        messageThread.blockUntilQEmpty();
        Thread.yield();
    }
    
    // Private methods needed by run()
    
    /**
     * Modus in ServerRoboterThreads setzen und warten, bis sie zu Potte kommen.
     * Wird bei allen Modi au�er ZERST�RT benutzt.
     * PRE: Attribute rThreadsAufDieIchWarte und modus sind korrekt belegt,
     * au�erdem muessen die Bot korrekt initialisiert sein,
     * konkret: Bei Programmierungsmodus wird auf die Karten zugegriffen.
     * POST: Der Modus wurde von unseren entsprechenden Threads bearbeitet
     */
    private void broadcastUndWarteAufRoboter() {
        
        for (Iterator iter = botThreads.iterator(); iter.hasNext();) {
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
        
        if (waitablesImWaitingFor.isEmpty()) {
            return;
        }
        
        //Broadcast an die betroffenen Threads, Inhalt je nach Modus
        //   CAT.debug("442 wait for waitablesImWaitingFor");
        synchronized (waitablesImWaitingFor) {
            //      CAT.debug("444 lock waitablesImWaitingFor");
            for (Iterator e = waitablesImWaitingFor.iterator(); e.hasNext();) {
                ServerRoboterThread srt = (ServerRoboterThread) e.next();
                
                try {
                    switch (mode) {
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
        //  CAT.debug("444 release waitablesImWaitingFor");
        //Schlafen bis TO oder alle fertig
        Iterator it = warteAufRoboter();
        
        //Falls welche nicht fertig geworden, entfernen
        while (it.hasNext()) {
            ServerRoboterThread tmp = (ServerRoboterThread) it.next();
            CAT.debug("!!!ServerRoboterThread " + tmp.rob.getName() + " rauswerfen wegen Timeout!!!");
            it.remove();
            deleteRob(tmp, OtherConstants.REASON_TIMEOUT);
        }
    }
    
    /**
     * Wartet -- je nach Modus -- eine bestimmte Zeit auf wiederkehrende
     * ServerRoboterThreads oder eben bis alle ferig sind.
     * PRE: rThreadsAufDieIchWarte und modus ist korrekt gesetzt
     */
    private synchronized Iterator warteAufRoboter() {
        //  CAT.debug("498 lock Server");
        int to;
        
        switch (mode) {
            case SPIELSTART:
                to = commTO;
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
                to = startRoundTO;
                break;
            default:
                to = 0;
            
        }
        //   CAT.debug("498 release Server");
        CAT.debug("Der Server wartet jetzt " + to + " Millisek. auf seine RoboterThreads (" + mode + ").");
        return waitablesImWaitingFor.waitFor(to);
    }
    
    /**
     * Setzt Modus in ServerRoboterThreads um
     *
     * @param it      umzusetzenden ServerRoboterThreads
     * @param newMode Modus in den die Threads gesetzt werden sollen
     */
    private void wechselModus(Iterator it, int newMode) {
        while (it.hasNext()) {
            ServerRoboterThread t = (ServerRoboterThread) it.next();
            t.setMode(newMode);
        }
    }
    
    /**
     * Uebertraegt ausgabeneintrittsliste in offizielle Ausgabenliste
     */
    private void setzeAusgaben() {
        CAT.debug("setzeAusgaben");
        //  CAT.debug("539 wait for aThreads");
        synchronized (ausgabeThreads) {
            //       CAT.debug("541 lock on aThreads\nwait for ausgabenEL");
            synchronized (ausgabenEnterRequestedThreads) {
                //         CAT.debug("543 lock on ausgabenEintrittsListe");
                if (ausgabenEnterRequestedThreads.size() == 0) {
                    return;
                } else {
                    CAT.debug("There are new views. Welcoming them");
                }
                
                waitablesImWaitingFor = new WaitingForSet(ausgabenEnterRequestedThreads);
                
                for (Iterator e = ausgabenEnterRequestedThreads.iterator(); e.hasNext();) {
                    ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                    tmp.setMode(FRAGENERLAUBT);
                    try {
                        tmp.startGame();
                    } catch (KommFutschException ex) {
                        new Fehlermeldung(Message.say("Server", "eKummFutschA"));
                    } catch (KommException ex) {
                        CAT.debug("setzeAusgaben: Es ist eine KommException aufgetreten.");
                    }
                    tmp.start();
                } //for
                CAT.debug("Alle ael-s benachrichtigt und in den richtigen Modus versetzt.");
                
                Iterator it = waitablesImWaitingFor.waitFor(notifyTO);
                
                while (it.hasNext()) {
                    ausgabenEnterRequestedThreads.remove(it.next());
                }
                
                CAT.debug("Kopiere ael: " + ausgabenEnterRequestedThreads.size());
                for (Iterator iter = ausgabenEnterRequestedThreads.iterator(); iter.hasNext();) {
                    ServerAusgabeThread tmp = (ServerAusgabeThread) iter.next();
                    tmp.setMode(KEINEFRAGEN);
                    iter.remove();     // aus ael
                    CAT.debug("Addiere einen zu aThreads hinzu");
                    ausgabeThreads.addElement(tmp);
                }
            } // synchronized ausgabenEL
            //   CAT.debug("543 release ausgabenEl");
        } // sync aThreads
        //     CAT.debug("541 release aThreads");
    }
    
    // returns false if interrupted, true if all is ok
    private synchronized boolean anmeldung() {
        //       CAT.debug("588 lock on Server");
        try {
            // instantiation moved  to constructor: registrationManager = new RegistrationManager(this);
            registrationManager.beginRegistration();
            CAT.debug("registrationManager gestartet");
            if (isInterrupted()) {
                return false;
            }
            try {
                wait();                       // supposed to be notified from startGame()
            } catch (InterruptedException e) {
                CAT.debug("In der anmeldung interruptiert worden!"); 
                return false;
            }
            //Spiel starts!
            if (serverObserver != null) {
                serverObserver.fireGameStarted();
            }
            return true;
        } finally {
            // CAT.debug("588 release Server");
        }
    }
    
    private void setzeStartPunkt() {
        // setzen der x-, y-, archivX- und archivY-Koordinaten in den Robots auf
        // die Koordinaten der ersten Flagge
        if (board == null) {
            CAT.error("feld ist null");
        }
        if (CAT.isDebugEnabled()) {
            CAT.debug("setze x und archivX in robots auf " + board.getFlags()[0].getX());
            CAT.debug("setze y und archivY in robots auf " + board.getFlags()[0].getY());
        }
        for (int i = 0; i < curBotsThreads.size(); i++) {
            ((ServerRoboterThread) (curBotsThreads.elementAt(i))).rob.setPos(board.getFlags()[0]);
            ((ServerRoboterThread) (curBotsThreads.elementAt(i))).rob.touchArchive();
        }
    }
    
    private void roboterThreadStart() {
        for (int i = 0; i < botThreads.size(); i++) {
            ((ServerRoboterThread) (botThreads.elementAt(i))).start();
        }
        CAT.debug("ServerRoboterThreads wurden gestartet");
    }
    
    /**
     * Sind noch Bot dabei?
     */
    private boolean istSpielende() {
        return (botThreads.size() == 0) || ((curBotsThreads.size() == 0) && (destroyedBotsThreads.size() == 0) && (botEnterRequestedThreads.size() == 0));
    }
    
    /**
     * Repair damage.
     * Returns true, if we have to ask the player which registers should be repaired
     * (if more than one register is locked)
     */
    private boolean repariereGgf(ServerRoboterThread t) {
        // TODO this  method sucks (figuring out whether to ask for repair
        //                                       and ignoring the bot being on a repair field/flag or not
        
        int lockedCount = t.rob.countLockedRegisters(); 
        int damage = t.rob.getDamage();
        if ( lockedCount == 0) {
            // no registers locked, nothing to unlock
            return false;
        }
        else if ((lockedCount > 0) && damage< 5) {
            // (damage == 5 => first register locked)
            // only one register to unlock, no need to ask the player
            String [] msg = t.rob.unlockAllRegistersAndGetMessage();
            if (msg != null){
                sendMsg(MessageID.REGISTER_UNLOCKED, msg);
            }
            return false;
        }
        else {
            int numberOfRegsThatShouldBeLocked = damage-4;          
            boolean canRepair = numberOfRegsThatShouldBeLocked < lockedCount;
            return canRepair;
        }
    }
    
    /**
     * The main method of the server!
     */
    public void run() {
        try {
            setName("ServerThread");
            
            CAT.debug("MsgThreadStart");
            messageThread = new MessageThread(this, commTO);
            messageThread.start();
            
            CAT.debug("anmeldung()");
            boolean spielgestartet = anmeldung();
            if (!spielgestartet) {
                messageThread.interrupt();                
                registrationManager.endRegistration();
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
            synchronized (botThreads) {
                CAT.debug("have rThreads");
                alleN = new String[botThreads.size()];
                int i = 0;
                for (Iterator e = botThreads.iterator(); e.hasNext();) {
                    alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
                }
            }
            CAT.debug("released rThreads");
            //Init Statistics.
            stats = new StatsList(alleN);
            board.initStats(stats);
            
            notifyViews(alleN);
            
            // 2. Bot
            sendMsg("mWelcome", "");
            
            CAT.debug("Starting game.");
            mode = SPIELSTART;
            waitablesImWaitingFor = new WaitingForSet(botThreads);
            wechselModus(waitablesImWaitingFor.iterator(), SPIELSTART);
            broadcastUndWarteAufRoboter();
            CAT.debug("Game has started.");
            // ssending a first  MNR (request for new facing) to all bots without reducing the number of lives
            CAT.debug("Getting initial facing.");
            mode = INITAUSR;
            waitablesImWaitingFor = new WaitingForSet(curBotsThreads);
            wechselModus(waitablesImWaitingFor.iterator(), INITAUSR);
            broadcastUndWarteAufRoboter();
            CAT.debug("Got initial facings.");
            //  notifying views about initial facings
            //  creating String[] containing all names
            //     CAT.debug("708 wait for rthreads");
            synchronized (botThreads) {
                //         CAT.debug("710 lock rthreads");
                alleN = new String[botThreads.size()];
                int i = 0;
                for (Iterator e = botThreads.iterator(); e.hasNext();) {
                    alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
                }
            }
            //   CAT.debug("716 release rthreads");
            notifyViews(alleN); 
            sendMsg(MessageID.INITIAL_FACINGS, alleN);
            
            // Loop for all turns.
            gameover = false;
            Vector gesperrteKarten = new Vector(); //Vector of cards!
            for (int iRunde = 1; !gameover; iRunde++) {
                
                String[] tmpstr = new String[1];
                tmpstr[0] = "" + iRunde;
                sendMsg("mNeueRunde", tmpstr);
                
                // Wait for reentering bots if neccessary
                for (Iterator iter = destroyedBotsThreads.iterator(); iter.hasNext();) {
                    if (!(((ServerRoboterThread) iter.next()).isAlive())) {
                        iter.remove();
                    }
                }
                
                if (destroyedBotsThreads.size() > 0) {
                    
                    CAT.debug("Waiting a short time for destroyed bots.");
                    mode = ZERSTOERT_SYNC;
                    
                    tmpstr = new String[1];
                    Iterator iter = destroyedBotsThreads.iterator();
                    tmpstr[0] = (((ServerRoboterThread) iter.next()).rob.getName());
                    while (iter.hasNext()) {
                        tmpstr[0] = tmpstr[0] + ", " + (((ServerRoboterThread) iter.next()).rob.getName());
                    }
                    sendMsg("mZerstSync", tmpstr);
                    
                    waitablesImWaitingFor = new WaitingForSet(destroyedBotsThreads);
                    wechselModus(waitablesImWaitingFor.iterator(), ZERSTOERT_SYNC);
                    warteAufRoboter();
                    wechselModus(destroyedBotsThreads.iterator(), ZERSTOERT_ASYNC);
                    CAT.debug("wait for roboterEL");
                    synchronized (botEnterRequestedThreads) {
                        CAT.debug("have roboterEL");
                        alleN = new String[botEnterRequestedThreads.size()];
                        int i = 0;
                        for (Iterator e = botEnterRequestedThreads.iterator(); e.hasNext();) {
                            alleN[i++] = ((ServerRoboterThread) e.next()).rob.getName();
                        }
                    }
                    CAT.debug("release roboterEL");
                    if (alleN.length > 0) {
                        notifyViews(alleN);
                    }
                }
                CAT.debug(botEnterRequestedThreads.size() + " destroyed robot threads to reenter the game");
                
                String[] namen;   // for notifyChange
                Vector toBeAsked = new Vector();
                CAT.debug("wait for roboterEL");
                synchronized (botEnterRequestedThreads) {
                    CAT.debug("have roboterEL");
                    CAT.debug("Letting bots reenter.");
                    for (Iterator e = botEnterRequestedThreads.iterator(); e.hasNext();) {
                        // Place all reentering bots on their archive position.
                        // In own while-loop, so the reentering can be checked correctly later.                                               
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        tmp.rob.toArchive();
                    }
                    for (Iterator e = botEnterRequestedThreads.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        // maybe needs a de-virtualization
                        // Caution, also consider active roboter that will stay non-virtual.
                        tmp.rob.setVirtual(false);  //Default: place them non-virtual, BUT....
                        // ..virtual, if there already is a non-virtual bot present..
                        for (Iterator f = curBotsThreads.iterator(); f.hasNext();) {
                            Bot anderer = ((ServerRoboterThread) f.next()).rob;
                            if (tmp.rob.getX() == anderer.getX() && tmp.rob.getY() == anderer.getY()) {
                                tmp.rob.setVirtual();
                                break;
                            }
                        }
                        // .. or if another bot will reenter on the same position
                        if (!tmp.rob.isVirtual()) {
                            for (Iterator f = botEnterRequestedThreads.iterator(); f.hasNext();) {
                                Bot anderer = ((ServerRoboterThread) f.next()).rob;
                                if (anderer != tmp.rob && anderer.getX() == tmp.rob.getX() && anderer.getY() == tmp.rob.getY()) {
                                    tmp.rob.setVirtual();
                                }
                            }
                        }
                    }
                    
                    namen = new String[botEnterRequestedThreads.size()];
                    int idx = 0;
                    // Now doing the _real_ reentering
                    for (Iterator e = botEnterRequestedThreads.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        curBotsThreads.add(tmp);
                        toBeAsked.add(tmp);
                        CAT.debug("Adding " + tmp.rob + " to toBeAsked 1");
                        namen[idx++] = tmp.rob.getName();
                    }
                    botEnterRequestedThreads.removeAllElements();
                }
                CAT.debug("have roboterEL");
                
                notifyViews(namen);
                
                /* Power Up!
                 *  Ask just entering bots whether they want to be powered up again.
                 */
                for (Iterator e = curBotsThreads.iterator(); e.hasNext();) {
                    ServerRoboterThread robThread = ((ServerRoboterThread) e.next());
                    if (!robThread.rob.isActivated()) {
                        if (robThread.rob.isPoweredDownNextTurn()) {
                            //This bot just chose PowerDown.
                            robThread.rob.setNextTurnPoweredDown(false);
                            robThread.rob.fullRepair();
                        } else {
                            if (!toBeAsked.contains(robThread)) {
                                toBeAsked.add(robThread);
                                CAT.debug("Adding " + robThread.rob + " to toBeAsked 2");
                            }
                        }
                    }
                }
                if (toBeAsked.size() > 0) {
                    CAT.debug("I ask " + toBeAsked.size() + " bots about Powerup.");
                    
                    tmpstr = new String[1];
                    Iterator iter = toBeAsked.iterator();
                    tmpstr[0] = (((ServerRoboterThread) iter.next()).rob.getName());
                    while (iter.hasNext()) {
                        tmpstr[0] = tmpstr[0] + ", " + (((ServerRoboterThread) iter.next()).rob.getName());
                    }
                    sendMsg("mPowUpFrage", tmpstr);
                    
                    mode = POWERUP;
                    wechselModus(toBeAsked.iterator(), POWERUP);
                    waitablesImWaitingFor = new WaitingForSet(toBeAsked);
                    broadcastUndWarteAufRoboter();
                    CAT.debug("Powerup done.");
                    
                    namen = new String[toBeAsked.size()];
                    int idx = 0;
                    for (Iterator e = toBeAsked.iterator(); e.hasNext();) {
                        namen[idx++] = ((ServerRoboterThread) e.next()).rob.getName();
                    }
                    notifyViews(namen);
                    
                    for (Iterator it = toBeAsked.iterator(); it.hasNext();) {
                        ServerRoboterThread srt = ((ServerRoboterThread) it.next());
                        if (!srt.rob.isActivated()) {                         
                            srt.rob.fullRepair();
                        }
                    }
                }
                
                // Deal cards to all bots that are not destroyed or deactivated;
                // wait for the moves of the bots  (Timeout)
                Card[] cards = new Card[gesperrteKarten.size()];
                for (int i = 0; i < gesperrteKarten.size(); i++) {
                    cards[i] = (Card) gesperrteKarten.get(i);
                }
                
                Deck stapel = new Deck(cards);
                CAT.debug("New card deck: " + stapel);
                Vector activeThisRound = new Vector();
                for (Iterator e = curBotsThreads.iterator(); e.hasNext();) {
                    ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                    if (tmp.rob.isActivated()) {
                        activeThisRound.addElement(tmp);
                        // Handing out cards, regarding which bot gets how many cards!
                        tmp.rob.setCards(stapel.handOutCards(tmp.rob.cardsToGive()));
                    }
                }
                if (activeThisRound.size() > 0) {
                    CAT.debug("I will deal out " + activeThisRound.size() + " cards.");
                    mode = PROGRAMMIERUNG;
                    waitablesImWaitingFor = new WaitingForSet(activeThisRound);
                    wechselModus(activeThisRound.iterator(), PROGRAMMIERUNG);
                    waitablesImWaitingFor.addRemovalListener(robProgListener);
                    broadcastUndWarteAufRoboter();
                    CAT.debug("received programming");
                }
                
                //              CAT.debug("890 locke aktRoboter..");
                // Evaluation starts
                synchronized (curBotsThreads) {
                    //              CAT.debug("893 habe aktRoboter");
                    wechselModus(curBotsThreads.iterator(), NIX);
                    mode = NIX;
                }
                //                CAT.debug("890 frei: aktRoboter");
                // Loop for the five phases.
                for (curPhase = 1; curPhase != 0; curPhase = (curPhase + 1) % 6) {
                    CAT.info("Evaluation phase " + curPhase + ", turn " + iRunde + " starts.");
                    // Copy participating bots into array (for technical reasons)
                    Bot[] robs;
                    //             CAT.debug("903 wait for aktRoboter");
                    synchronized (curBotsThreads) {
                        //                    CAT.debug("903 have aktRoboter");
                        //                    CAT.debug("");
                        //<hendrik was here>
                        int removedBots = 0;
                        for (Iterator e = curBotsThreads.iterator(); e.hasNext();) {
                            ServerRoboterThread t = ((ServerRoboterThread) e.next());
                            if (t.isAlive()) {
                                CAT.debug(t.getName() + " IS ALIVE");
                            } else {
                                CAT.error(t.getName() + " IS !!NOT!! ALIVE");
                                //aktRoboter.remove(t);
                                removedBots++;
                            }
                        }
                        
                        robs = new Bot[curBotsThreads.size() - removedBots];
                        int i = 0;
                        for (Iterator e = curBotsThreads.iterator(); e.hasNext(); i++) {
                            ServerRoboterThread t = ((ServerRoboterThread) e.next());
                            if (t.isAlive() && t.isThere()) {
                                CAT.debug(t.getName() + " IS ALIVE");
                                robs[i] = t.rob;
                            } else {
                                CAT.error(t.getName() + " IS !!NOT!! ALIVE");
                            }
                            
                        }
                    }
                    //                CAT.debug("903 release  aktRoboter");
                    // (doPhase())
                    //DEBUG
                    CAT.debug("feld.doPhase(" + curPhase + ") mit ");
                    for (int i = 0; i < curBotsThreads.size(); i++) {
                        CAT.debug(((ServerRoboterThread) curBotsThreads.elementAt(i)).rob.getName() + ": " + ((ServerRoboterThread) curBotsThreads.elementAt(i)).rob);
                    }
                    board.doPhase(curPhase, robs);
                    
                    // Evaluate what has happened.
                    //             CAT.debug("945 wait aktRoboter");
                    synchronized (curBotsThreads) {
                        //                 CAT.debug("945 have aktRoboter");
                        for (Iterator e = curBotsThreads.listIterator(); e.hasNext();) {
                            ServerRoboterThread tmp = (ServerRoboterThread) e.next();
                            // Winner?
                            if (tmp.rob.getNextFlag() == board.getFlags().length + 1) {
                                //remove from active robots, adding to winnerlist,
                                // remove from plan(?), notify views
                                tmp.setMode(SPIELENDE);
                                wonThreads.addElement(tmp);
                                e.remove(); //from active robots
                                // Messages....................
                                tmpstr = new String[2];
                                tmpstr[0] = tmp.rob.getName();
                                tmpstr[1] = "" + wonThreads.size();
                                sendMsg("mGewinn", tmpstr);
                                // ...........................
                                try {
                                    tmp.deleteMe("GO");
                                } catch (KommException ex) {
                                    CAT.debug("Bot " + tmp.rob.getName() + " could not be notified of its removal because of  GO(Winner): " + ex);
                                }
                                
                                tmp.rob.setInvalidPos();
                                tmp.rob.setVirtual();
                                
                                namen = new String[1];
                                namen[0] = tmp.rob.getName();
                                notifyViews(namen);
                                
                                gameover = istSpielende();
                                
                            }
                            // damage > 9
                            if (tmp.rob.getDamage() > 9) {
                                e.remove();  // from active robots
                                tmp.rob.decrLife();
                                
                                // really dead, no lives left?
                                if (tmp.rob.getLivesLeft() > 0) {
                                    tmp.rob.setReenterDamage();
                                    // unlock registers
                                    // tmp.rob.unlockAllRegistersAndGetMessage();
                                    // setReenterDamage() takes care of unlocking registers 
                                   
                                    // TODO notifying the views of unlock? 
                                    // Probably not necessary because of destruction messages                                 
                                    tmp.rob.setNextTurnPoweredDown(false);
                                    
                                    destroyedBotsThreads.addElement(tmp);
                                    tmp.setMode(ZERSTOERT_ASYNC);
                                    try {
                                        tmp.killed();
                                    } catch (KommFutschException ex) {
                                        new Fehlermeldung(Message.say("Server", "eKommFutschR", tmp.rob.getName()));
                                    } catch (KommException ex) {
                                        CAT.error("Communication error with player "+ tmp.rob.getName());
                                    }
                                } else {
                                    graveyardThreads.addElement(tmp);
                                    tmp.setMode(SPIELENDE);
                                    try {
                                        tmp.deleteMe("LL");
                                    } catch (KommException ex) {
                                        CAT.debug("Communication error deleting a robot");
                                    }
                                    gameover = istSpielende();
                                }
                                
                                namen = new String[1];
                                namen[0] = tmp.rob.getName();
                                notifyViews(namen);
                            }
                        }
                    }
                    //                CAT.debug("945 release aktRoboter");
                    
                } // End phase evaluation
                
                /*  End-of-turn-evaluation:
                 1.) Repairing issues.
                 Relevant for all bots on flags and repair fields.
                 */
                Vector repairing = new Vector();
                for (Iterator e = curBotsThreads.iterator(); e.hasNext();) {
                    ServerRoboterThread tmp = (ServerRoboterThread) e.next();
                    if (repariereGgf(tmp)) {
                        CAT.debug("Adding one to 'repairing' list:");
                        CAT.debug(tmp.rob);
                        repairing.addElement(tmp);
                    }
                }
                if (repairing.size() > 0) {
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("Asking " + repairing.size()+ " players for register repair choice");
                    }
                    mode = ENTSPERREN;
                    wechselModus(repairing.iterator(), ENTSPERREN);
                    waitablesImWaitingFor = new WaitingForSet(repairing);
                    broadcastUndWarteAufRoboter();
                    CAT.debug("repair questions answered");
                }
                
                
                CAT.debug("Collecting locked registers");
                gesperrteKarten.removeAllElements();
                //               CAT.debug("1050 wait rThreads");
                synchronized (botThreads) {
                    //              CAT.debug("1050 have rThreads");
                    for (Iterator e = botThreads.iterator(); e.hasNext();) {
                        ServerRoboterThread tmp = ((ServerRoboterThread) e.next());
                        for (int i = 0; i < 5; i++) {
                            if (tmp.rob.getLockedRegister(i) != null) {
                                gesperrteKarten.addElement(tmp.rob.getLockedRegister(i));
                            }
                        }
                    }
                }
                //          CAT.debug("1050 release rThreads");
                if (CAT.isDebugEnabled()) {
                    CAT.debug("Did find and save " + gesperrteKarten.size() + " locked cards");
                }
                /* End-of-turn-evaluation:
                 2.) Set bots to deactivated if they chose power down                                  
                 */
                Vector changedBots = new Vector();
                //         CAT.debug("1066 wait rThreads");
                synchronized (botThreads) {
                    //                   CAT.debug("1066 have rThreads");
                    for (Iterator it = botThreads.iterator(); it.hasNext();) {
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
                //             CAT.debug("1066 release rThreads");
                /* Tell the clients if someone powered down. */
                if (changedBots.size() > 0) {
                    String[] changedBotsArray = new String[changedBots.size()];
                    changedBots.toArray(changedBotsArray);
                    notifyViews(changedBotsArray);
                }
                
            } // Rundenschleife ende
            
            CAT.debug("The game is now over (leaving round evaluation loop)");
            
            /* Falls wir jemals ein anderes Ende haben wollen, mu� man sich hier nochmal Gedanken
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
            
            if (CAT.isDebugEnabled()) {
                CAT.debug("There are " + ausgabeThreads.size() + " views left.");
            }
            if (ausgabeThreads.size() > 0) {
                //   CAT.debug("1114 wait aThreads");
                synchronized (ausgabeThreads) {
                    //       CAT.debug("1114 have raThreads");
                    for (Iterator e = ausgabeThreads.iterator(); e.hasNext();) {
                        ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                        tmp.setMode(FRAGENERLAUBT);
                        try {
                            tmp.deleteMe("GO");
                        } catch (KommException ex) {
                            CAT.error("notifiying Views: KommException");
                            CAT.error(ex.getMessage(), ex);
                        }
                    } //for
                    CAT.debug("Notified all AusgabeThreads (Views) about the end of the game;set them into the right mode.");
                    
                    waitablesImWaitingFor = new WaitingForSet(ausgabeThreads);
                    waitablesImWaitingFor.waitFor(notifyTO);
                    
                    for (Iterator e = ausgabeThreads.iterator(); e.hasNext();) {
                        ServerAusgabeThread tmp = (ServerAusgabeThread) e.next();
                        try {
                            tmp.endGame();
                        } catch (java.io.IOException ex) {
                            CAT.error("IOException at view.endGame().");
                            CAT.error(ex.getMessage(), ex);
                        }
                    }
                } // synchronized aThreads
                //     CAT.debug("1114 release raThreads");
            } // if aThreads > 0
            
            // AnmeldeThread killen
            try {
                registrationManager.seso.close();
            } catch (java.io.IOException ex) {
                CAT.error("IOException while killing registration Thread.");
                CAT.error(ex.getMessage(), ex);
            }
            
            // MessageThread killen
            messageThread.interrupt();
            
            if (serverObserver != null) {
                serverObserver.fireGameFinished();
            }
            
        } catch (Throwable t) {
            CAT.fatal("Exception:"+t.getMessage(), t);
        }
        finally {            
            CAT.info("SERVER REACHED END OF RUN METHOD");
            shutdown();
        }
    }// run() ende
    
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
            //     CAT.debug("1183 wait for server");
            return gameStarted;
            
        } finally {
            //     CAT.debug("1183 release server");
        }
    }
    
    synchronized public void startGame() {
        try {
            //      CAT.debug("1193 wait for server");
            gameStarted = true;
            notify();
        } catch (Exception e) {
            //     CAT.debug("1197 release server");
        }
    }
    
    
    public void doShutdown() {
        CAT.debug("doShutdown..");
        if (board != null) {
            board.setServer(null);
        }
        
        if (messageThread != null){
            CAT.debug("messageThread");
            messageThread.shutdown();      
            try {
                messageThread.join(2000);
            }
            catch (Exception e){
                CAT.warn(e);
            }
        }
        
        int count = shutdownableCollections.length;
        CAT.debug("count="+count);
        for (int i=0;i<count;i++){
            CAT.debug("blubb");
            Vector v = shutdownableCollections[i];
            CAT.debug("Vector #"+i+": "+v);
            if (v != null) {
                CAT.debug("\tbefore synchronized");
                synchronized(v){
                    Iterator it = v.iterator();
                    CAT.debug("have iterator");
                    while (it.hasNext()){
                        try {        	                    
                            Shutdownable thread = (Shutdownable) it.next();        	       
                            CAT.debug("shutting down: "+thread);
                            thread.shutdown(true);
                        }
                        catch (Exception e){
                            CAT.error("error killing thread: "+e.getMessage(), e);
                        }
                    }
                }
                CAT.debug("after synchronized");
            }
        }
        
        if (registrationManager != null) {
            CAT.debug("regMan");
            registrationManager.shutdown(true);
        }
        if (serverObserver != null){
            CAT.debug("serverObserver");
            serverObserver.shutdown();        	    
        }
        
        
        this.interrupt();
    }
    
    public void addRegistrationStartListener(RegistrationStartListener l){
        registrationManager.addRegStartListener(l);
    }
    
    public void interrupt() {
        CAT.debug("SERVER GOT INTERRUPTED!");
        super.interrupt();     
    }
    
}// class Server ende
