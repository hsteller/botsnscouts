/*
 * Created on 23.05.2005
 *
 * 
 */
package de.botsnscouts.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Category;

import de.botsnscouts.autobot.AutoBot;
import de.botsnscouts.gui.Ausgabe;
import de.botsnscouts.gui.HumanPlayer;
import de.botsnscouts.server.Server;
import de.botsnscouts.start.Start;


/**
 * @author Hendrik Steller
 * @version $Id$
 * 
 * 
 * More or less a hack.
 * Is intended to keep track of the sevrver and clients/BNSThreads that were launched on  localhost;
 * to make sure that the clients get killed if the server isn't running anymore.
 * General purpose for that: being able to do a "clean" quit of local HumanPlayers/games 
 *  (==redisplay the main launcher app to maybe start a new game instead of the former "System.exit(0)"
 *  or the current "just hang up")
 * 
 */
public class Registry implements ShutdownListener, GameOverListener {
    
    	private static Category CAT = Category.getInstance(Registry.class);
   
    	private static Registry globalGameRegistry = new Registry();
    	
        private Collection games;
        private Game dummyCompareGame = new Game(null,"DUMMY",-1);
        private HashMap shutdownablesToGames = new HashMap();
        private HashMap serversToGames = new HashMap();
        private boolean isEnabled = false;
        
        
        private Registry(){
            games = new LinkedList();
            
        }
        
        public static Registry getSingletonInstance(){
            return globalGameRegistry;
        }
        
        // this is supposed to catch the case where someone (probably developer..)  starts a client via CLI
        // in this case, the client will not be bound to the Registry as it wasn't started via start.Launcher;
        // depending on the not-yet existing implementation of shutdwon() it is very likely that we will
        // (at least try to) display the main menu after the CLI-started player has shut down and its
        // inherited Shutdownable support calls Registry.shutdown() - and ending a CLI-based client
        // program by launching the main application sounds somewhat wrong..
        
        /** Must be called to activate the registry. Should be called when the program is started the 'normal'
         *   way (== program started with the main menu GUI, not a single player started via CLI in its own JVM;
         *   in the CLI-single-player-case we shouldn't enable the Registry to avoid an automatic launch of the
         *  main menu when the CLI-player shuts down).
         */      
        public void setEnabled(boolean enabled) {
            this.isEnabled = enabled;
        }
        
        public void addGame(Server server, String serverIp, int serverPort){
            if (!isEnabled) {
                return;
            }
            CAT.debug("xXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXx");
            CAT.debug(dump());
            CAT.debug("xXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXx");
            
            CAT.debug("addGame:server="+server+"; ip="+serverIp+"; port="+serverPort);
            Game game = new Game(server, serverIp, serverPort);          
            synchronized (games) {
                games.add(game);
            }
            
        }
        
        private Game findGame(String serverIp, int port){           
            dummyCompareGame.setServerIp(serverIp);
            dummyCompareGame.setServerPort(port);
            CAT.debug("findGame: ip="+serverIp+"; port="+port);
            synchronized (games) {
	            Iterator it = games.iterator();
	            while (it.hasNext()){
	                Object game = it.next();
	                if (game.equals(dummyCompareGame)){
	                	CAT.debug("\treturning game: "+game);
	                	return (Game) game;
	                }
	            }
            }
            CAT.debug("\treturning NULL");
            return null;
        }
        
        private Game findGame(Shutdownable clientOrServer) {
        	
        	Game g = (Game) shutdownablesToGames.get(clientOrServer);
        	CAT.debug("findGame for: "+clientOrServer);
        	CAT.debug("returning game: "+g);
        	return g;
        }
        
        
        public boolean isMyServerLocal(Shutdownable client){
           Game game = findGame(client);
           return game != null && game.getServer()!=null;              
        }
        
        public int getNumOfLocalViewsForMyGame(Shutdownable client){
            Game game = findGame(client);
            if (game != null){
                return game.getNumOfViews();
            }
            else {
                return 0;
            }
        }
        
        public  void addClient (HumanPlayer player, String serverIp, int port){
            addClient(serverIp, port, player, ClientInfo.CLIENT_TYPE_HUMANPLAYER);
        }
        
        public void addClient (AutoBot player, String serverIp, int port){
            addClient(serverIp, port, player, ClientInfo.CLIENT_TYPE_AUTOBOT);
        }
        
        public  void addClient (Ausgabe view, String serverIp, int port){
            addClient(serverIp, port, view, ClientInfo.CLIENT_TYPE_VIEW);
        }
        
        private  void addClient(String serverIp, int port, Shutdownable client, int clientInfoClientType){
            if (!isEnabled) {
                return;
            }
            CAT.debug("addClient: serverIp="+serverIp+";port="+port+";type="+clientInfoClientType
            		+"; client="+client);
            Game game = findGame(serverIp, port);
            if (game == null) {
                CAT.info("Registry: Tried to add a client to a non-existing game on "
                                +serverIp+":"+port+"; this is ok if you participate in a non-local game");             
                addGame(null, serverIp, port);  
            }            
            ClientInfo info = new ClientInfo( client, clientInfoClientType);
            game.addClient(info);
            shutdownablesToGames.put(client, game);     
            client.addShutdownListener(this);
        }
        
        public boolean isEmpty() {                
            synchronized (games){
                return games.isEmpty();
            }
        }
        
        public String dump() {
            StringBuffer sb = new StringBuffer();
            sb.append("isEnabled?= ").append(isEnabled).append('\n');          
            synchronized (games){
                if (games == null){
                    sb.append("games: NULL\n");
                }
                else {
                    sb.append("games: \n");
                    for (Iterator it = games.iterator();it.hasNext(); ){
                        sb.append(it.next()).append('\n');
                    }
                }
            }
            synchronized(shutdownablesToGames){
                sb.append("\n\nshutdownables->games:\n");
                for (Iterator keys=shutdownablesToGames.keySet().iterator(); keys.hasNext();){
                    Object key = keys.next();
                    sb.append(key).append("  -------->  ").append(shutdownablesToGames.get(key)).append('\n');
                }
            }
            synchronized (serversToGames){
                sb.append("\n\nservers->games:\n");
                for (Iterator keys=serversToGames.keySet().iterator(); keys.hasNext();){
                    Object key = keys.next();
                    sb.append(key).append("  -------->  ").append(serversToGames.get(key)).append('\n');
                }
            }
            return sb.toString();
        }
        
        /*
        public boolean removeClient(Shutdownable client){           
            Game game = findGame(client);
            boolean success = false;
            if (game != null){
                 success = game.removeClient(client);                
            }
            else {
                CAT.error("Registry: Tried to remove a client from a non-existing game!");            
            }
           clientsToGames.remove(client);
           return success;
        }
        */
        
        private void redisplayMenu(){
        
        	CAT.debug("redisplayMenu, dumping registry:");
        	CAT.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        	CAT.debug(dump());
        	CAT.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        	Start mainMenu = Start.getLauncherAppSingleton();
        	if (mainMenu != null ){
        		//mainMenu.setVisible(true);
        		//mainMenu.resetWaiter();        		
        		mainMenu.show();
        		mainMenu.showMainMenu();
        	}
        	else {
        		CAT.error("the main menu/launcher application was null");
        	}
        	
        }
        
       
        
        private  boolean areThereAnyLocalViews_killGamesWithoutThem(){
            // checking the other local game(s) and if someone local is still watching 
            boolean foundHumanClient = false;
            synchronized(games){
                Iterator it = games.iterator();
                while ( it.hasNext()){
                    Game g = (Game) it.next();
                    if (g.hasHumanView()){
                        foundHumanClient = true;
                        CAT.debug("areThereAny.. found a view");
                    }
                    else {                             
                        Server server = g.getServer();
                        CAT.debug("found other game; Serverthread: "+server);
                        // we found a local server with no local (human) players or views..
                        // TODO think about whether there might be cases where shutting 
                        // down is wrong..local server, no local GUI, but someone called shutdown..
                        //..hmm.. can this happen with AutoBots?!? Probably not as long as they
                        // don't call their "shutdown()" method themselves
                        if (server != null ){
                            shutdownServer(server, g, true);
                        }
                        else {
                            CAT.error("This is strange; there is a serverless game registered but noone "+
                                            "(local) seems to play..WTF? How get remote playing Autobots "+
                                            "added to this registry object?");
                            CAT.error("\t GAME DUMP for this strange game:\n"+g.dump());
                        }
                    }
                }
                return foundHumanClient;        
            }
        }
        
        private synchronized void shutdownServer (Server serv, Game game, boolean callShutdown){
            CAT.debug("shutting down server: "+serv); 
            
            if (callShutdown && !serv.isShutDown()){	               
                	serv.shutdown(false);	    
            }
            try {
                int seconds = 7;
                CAT.info("Registry waits at max. "+seconds+" seconds for the server to sort things out..");
                serv.join(seconds*1000);
            }
            catch (Exception e){
                	CAT.warn(e);
            }
            
           
            if (game != null) {           
                synchronized (games) {
                    games.remove(game);
                }
                game.setServerToNULL();
            }
            shutdownablesToGames.remove(serv);  
            
        }
        
        private void checkForGamesAndMaybeRedisplay() {
            if (isEmpty()) {
                CAT.debug ("\tsince there seems to be nothing else runnig, we will redisplay the main menu");
                	redisplayMenu();
             }
             else {
                 CAT.warn("\tThere seems to be another game running..");
                 boolean foundView = areThereAnyLocalViews_killGamesWithoutThem();
                 if (foundView){
                     CAT.debug("a local view is still active=>keep main menu hidden");
                 }
                 else{
                     redisplayMenu();
                 }                                                               
             }
        }
        
        /** Note: This method is supposed to be called by Shutdownables in their shutdown() method.
         *  It will NOT shut down <code>someThread</code> or we might get an infinite loop.
         * It is supposed to notify the registry that someThread has killed off its communications.
         * HOWEVER: This method WILL KILL other threads/players/views/servers and redisplay
         * the main menu if <code>someThread</code> was the last view of a (local) game.. 
         */
        public void shutdown(Shutdownable someThread){           
            if (!isEnabled || (someThread != null && someThread.isShutDown()) ){
                // this is supposed to catch the case where someone (probably developer..)  starts a client via CLI
                // in this case, the client will not be bound to the Registry as it wasn't started via start.Launcher;
                // depending on the not-yet existing implementation of shutdwon() it is very likely that we will
                // (at least try to) display the main menu after the CLI-started player has shut down and its
                // inherited Shutdownable support calls Registry.shutdown() - and ending a CLI-based client
                // program by launching the main application sounds somewhat wrong..
                return;
            }
            CAT.debug("shutdown called; thread: "+someThread);
            Game someThreadsGame = findGame(someThread);
            CAT.debug("shutdown: game="+someThreadsGame);
            if (someThreadsGame == null) {              
                CAT.debug("didn't find a game for Thread: "+someThread);
                CAT.debug("no game found for the client that is going down, hmm, let's see what there is to do..");   
                if (!someThread.isShutDown()) {
                    checkForGamesAndMaybeRedisplay();
                }
                return; // otherwise NullpointerEx. below
            }            
            else {
                // found the game; as we are about to clean up "someThread",
                // we will remove the "someThread->game" reference from the lookup table              
                CAT.debug("removing the game from thread->game lookup table ");   
                shutdownablesToGames.remove(someThread);                
            }
            
            
            if (someThread instanceof Server) {
                 CAT.debug("There is a server going down: "+someThread);               
                 Server serv = (Server) someThread;            
                 
                 shutdownServer(serv, someThreadsGame, false);
                 registryRemoveAndKillAutobots(someThreadsGame);
                                 
            }
            else {
                // some client         
                boolean removeSuccessful = someThreadsGame.removeClient(someThread);
                CAT.debug("remove of Thread "+someThread+(removeSuccessful?"":"NOT ")
                		+"successful");
                if (removeSuccessful) {                   
	                if (!someThreadsGame.hasHumanView()){
	                    CAT.debug("removed "+someThread+"; no local view connected anymore");	                    
	                     boolean probablyFinisheDshutdownNicely;
	                   
	                    final Game stg = someThreadsGame;
	                    final Object [] pseudoBoolean = new Object[1]; 
	                    Runnable farkingDeadLocks = new Runnable() {
	                        public void run() {
	                    
			                     // there is no local view left so we kill the server (if it exists) and all
			                    // AutoBots and then redisplay the main menu
			                    Server localServer = stg.getServer();	                   
			                    if (localServer == null) {
			                        // this probably means that we had a human playing/watching a game 
			                        // hosted on another computer;
			                        // to be on the safe side, we still try to kill all the (if I am right: not existing) 
			                        // AutoBots
			                       CAT.debug("no local server found for this game");
			                    }
			                    else {
			                       //CAT.debug("XXX shutting down the server: "+localServer);	  	                      
			                     shutdownServer(localServer,stg, true);
			                    }
			                    registryRemoveAndKillAutobots(stg);
			                    pseudoBoolean[0] = "probably clean shutdown";
	                        }
	                    };
	                    Thread temp = new Thread(farkingDeadLocks);
	                    temp.start();
	                    try {
	                        temp.join(5000);
	                    }
	                    catch (InterruptedException ie){
	                        CAT.warn(ie);
	                    }
	                    if (pseudoBoolean[0] == null){ // Thread "temp" seems not to have finished
	                                                               // we are only here because of the join-timeout
	                        CAT.info("Sorry I'm not sure that everything (the server)  shut down nicely,\n"
	                                        +" so I end the program as starting a new server probably won't work..");
	                        bruteRestart();
	                    }
	                    redisplayMenu();
	                    
	                    
	                }	           
                } // successful removal "someThread"       
            } // "someThread" was not a server
            
            
            if (!someThreadsGame.hasHumanView()){                  
                 redisplayMenu();
            }
           CAT.debug("end of shutdown: ");
           CAT.debug(dump());
              
            
        }
        
       private void bruteRestart(){
           System.exit(0);
       }
        
       private void registryRemoveAndKillAutobots (Game game) {
           Collection bots = game.removeAndKillAllAutoBots();
           if (bots != null ){
               synchronized (bots) {
	               Iterator it = bots.iterator();
	               while (it.hasNext()){
	                   ClientInfo ci = (ClientInfo) it.next();
	                   synchronized (shutdownablesToGames){
	                       shutdownablesToGames.remove(ci.getClient());
	                   }
	               }
               }
           }
       }
        
      public void gameIsOver (Game game){
            CAT.debug("gameIsOver(): "+game);
            if (games != null) {
	            synchronized (games) {
	               boolean  foo = games.remove(game);
	               CAT.debug("\tremoving of game was "+(foo?"":"NOT ")+"successful");
	               Server serv = game.getServer();
	               if (serv != null) {
	                  shutdownServer(serv, game,true);
	              }	               	              
	            }
            }
        }
        
    }


    class Game {
        private static Category CAT =  Category.getInstance(Game.class);
        
        private static final int CLIENT_TYPE_DUMMY = -1;
        public static final int CLIENT_TYPE_HUMANPLAYER = 1;
        public static final int CLIENT_TYPE_VIEW             = 2;
        public static final int CLIENT_TYPE_AUTOBOT       = 3; 
        
        private String servIp;
        private int serverPort;
        /** May be null, only exists for local games */
        private Server server;        
        private ArrayList clients;
        
        private LinkedList gameOverListeners = new LinkedList();
        
        public Game (Server serv, String ip, int serverPort){
            this.server = serv;
            this.servIp = ip;
            this.serverPort = serverPort;                                
        }
        
        public void addGameOverListener(GameOverListener gol){
            synchronized (gameOverListeners){
                gameOverListeners.add(gol);
            }
        }
        
        public void removeGameOverListeners(GameOverListener gol){
            synchronized (gameOverListeners){
                gameOverListeners.remove(gol);
            }
        }
        
        public void fireGameOver(){
            synchronized (gameOverListeners){
               Iterator it = gameOverListeners.iterator();
               while (it.hasNext()){
                   ((GameOverListener) it.next()).gameIsOver(this);
               }
            }        
        }
        
       
        /** Somewhat ugly that 'addClient()' has a 'ClientInfo' as parameter and 
         *   'removeClient()' a  Shutdownable.
         *   Reason: 'addClient()' is supposed to be called by the launcher application that knows
         *   what client it starts (and we want that to know that information here).
         *   But most (all?) calls of 'removeClient' will happen in BNSThread (closer: its 'ShutdownableSupport')
         *   and without using lots of 'instanceof' or a client-type field in BNSThread we don't know
         *   how to create a proper ClientInfo-object to pass to removeClient.
         *   And even IF we create it, we still need to modify ClientInfo.equals() so it is somewhat simpler
         *   to modify ClientInfo.equals() in a way that it works with CllientInfos AND Shutdownables.
         * 
         *  ..or this is simply crazy 3:00 AM thinking ;-)
         * 
         */
        public void addClient(ClientInfo client){
           if (clients == null) {
               clients = new ArrayList();
           }
           synchronized (clients){
               clients.add(client);
           }
           
        }
        
        /** Somewhat ugly that 'addClient()' has a 'ClientInfo' as parameter and 
         *   'removeClient()' a  Shutdownable.
         *   Reason: 'addClient()' is supposed to be called by the launcher application that knows
         *   what client it starts (and we want that to know that information here).
         *   But most (all?) calls of 'removeClient' will happen in BNSThread (closer: its 'ShutdownableSupport')
         *   and without using lots of 'instanceof' or a client-type field in BNSThread we don't know
         *   how to create a proper ClientInfo-object to pass to removeClient.
         *   And even IF we create it, we still need to modify ClientInfo.equals() so it is somewhat simpler
         *   to modify ClientInfo.equals() in a way that it works with ClientInfos AND Shutdownables.
         * 
         *  ..or this is simply crazy 3:00 AM thinking ;-)
         * 
         *  
         */
        public boolean removeClient (Shutdownable client){
        	
            synchronized (clients) {
                if (clients != null) {
                	// NOTE: see "NOTE" in ClientInfo.equals() why we need
                	// the "dummy" ClientInfo
                	ClientInfo dummy = new ClientInfo(client, CLIENT_TYPE_DUMMY);
                	boolean success = clients.remove(dummy);                                       
                    CAT.debug("removing of 'Shutdownable' from the 'ClientInfo' collection was "+(success?"":"NOT ")+"successful");
                    CAT.debug("\tShutdownable: "+client);
                    if (!hasClients()){
                        fireGameOver();
                    }
                    return success;                    
                }
                else {
                	CAT.debug("remove: clients collection is null");
                	if (!hasClients()){
                        fireGameOver();
                    }
                	return false;
                }
            }
           
        }
        
        protected void setServerToNULL(){
            server = null;
        }
      
        public boolean hasClients(){
            return clients != null &&  !clients.isEmpty();//  (server == null || server.isShutDown());
        }
        
        public int getNumOfViews() {
           
            int foundCounter = 0;
            synchronized(clients){
                if (clients != null) {
                    CAT.debug("hasHumanView: iterating clients");
                    Iterator it = clients.iterator();
                    while (it.hasNext()){
                        ClientInfo client = (ClientInfo) it.next();
                        CAT.debug("\tchecking client:"+client);
                        int type = client.getClientType();
                        CAT.debug("\t client type is: "+type);
                        if (/*type == CLIENT_TYPE_HUMANPLAYER || */type == CLIENT_TYPE_VIEW){
                          // a local human player without a local view doesn't make sense  
                           // as long as we don't  implement the option to start and register a telnet-client
                           // via the launcher app ;-)
                            
                            foundCounter++;
                        }
                    }
                }
            }
            return foundCounter;
        }
        
      
        
        public boolean hasHumanView() {
            return getNumOfViews()>0;
        }
        
        
        /** Removes all clients of type <code>type</code> that are associated
         * with this game and returns them
         * @param type a client type as specified by the constants in this class 
         * @return All ClientInfos of type <code>type</code> that were removed (currently in a LinkedList)
         */
        private Collection removeClients(int type) {
            LinkedList remClients = new LinkedList();
            synchronized (clients) {
                int size = clients!=null?clients.size():0;
                CAT.debug("removeClients("+type+"): # of clientts="+size);
                for (int i =size-1;i>=0;i--){ 
                    //iterating backwards because removing Objects from "clients"
                    // will change clients.size() and Ojects indices during iteration 
                    ClientInfo info = (ClientInfo) clients.get(i);
                    CAT.debug("checking client "+info);
                    if (info.getClientType() == type){
                        CAT.debug("calling remove for: "+info.getClient());                       
                        remClients.add(clients.remove(i));
                        
                    }
                }
            }
            if (!hasClients()) {
                fireGameOver();
            }
            return remClients;
        }
        
        /** @return the ClientInfos of the removed bots*/
        protected Collection removeAndKillAllAutoBots(){
            CAT.debug("removeAndKillAutoBots");
            Collection autoBots = this.removeClients(Game.CLIENT_TYPE_AUTOBOT);
            int numOfBots = autoBots!=null?autoBots.size():0;
            CAT.debug("num of bots: "+numOfBots);
            Iterator it = autoBots.iterator();
            while (it.hasNext()){
                ClientInfo bot = (ClientInfo) it.next();
                CAT.debug("calling shutdown for "+bot.getClient());
                Shutdownable client = bot.getClient();               
                if (client != null && !client.isShutDown()) {
                   client.shutdown(false);
                   try {
                       BNSThread a = (BNSThread) client;
                       a.join(2000);
                   }
                   catch (Exception e){
                       CAT.warn(e);
                   }
                }
                
            }
            return autoBots;           
        }
            
        public String toString(){
            return dump();
        }
        
         public String dump() {                 
                 StringBuffer sb = new StringBuffer();
                 sb.append("Server address="+servIp+':'+serverPort+"\tserver="+server+"\nClients:");
                 if (clients != null){
                     sb.append("\n");
	                 synchronized (clients) {
	                     Iterator it = clients.iterator();
		                 while (it.hasNext()){
		                     Object o = it.next();
		                     sb.append(o!=null?o.toString():o);
		                 }
	                 }
                 }
                 else {
                     sb.append("NULL\n");
                 }
                return sb.toString();                                 
        }
        
                       
        public boolean equals (Object o) {
            if (o == server) {
                return true;
            }
            Game g = (Game )o;
            if (g == null) {
                return false;
            }
            else {    
                return serverPort == g.serverPort && this.servIp.equals(g.servIp);
            }
        }
        
        protected void setServerIp(String ip){
            this.servIp = ip;
        }
        
        protected void setServerPort(int port){
            this.serverPort = port;
        }
        /** 
         * 
         * @return the server if this Game object has one or <code>null</code> otherwise 
         */
        protected Server getServer(){
            return server;
        }
                                    
}
        
/** Somehow not in the mood for "instanceof" right now, so here comes another inner class.. ;-)
 *   And who knows, there might be other info besides the client type that we want to keep later. 
 * */
class ClientInfo {
   
	Category CAT = Category.getInstance(ClientInfo.class);
	
    public static final int CLIENT_TYPE_UNKNOWN      = -1;
    public static final int CLIENT_TYPE_HUMANPLAYER = 1;
    public static final int CLIENT_TYPE_VIEW              = 2;
    public static final int CLIENT_TYPE_AUTOBOT        = 3;
    
    
    private int clientType = CLIENT_TYPE_UNKNOWN;
    private Shutdownable client = null;
    
    /**
     * 
     * @param client Uuuh..the client we want to store some additional info for.
     * @param clientType whether this client is an AutoBot, HumanPlayer, View,..
     */
    public ClientInfo(Shutdownable client, int clientType){
        this.client = client;
        this.clientType = clientType;
    }

    public void setClient(Shutdownable client) {
        this.client = client;
    }

    public Shutdownable getClient() {
        return client;
    }
    

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public int getClientType() {
        return clientType;
    }
    
    /** Can be used to compare this ClientInfo object to another ClientInfo _OR_ to 
     *  compare it with an instanceof 'Shutdownable'; in the second case this ClientInfo's
     *  internal Shutdownable is compared (using '==') with the Shutdownable that was passed
     * as parameter
     * 
     * @param o another Object to compare this Clientinfo to; may also return true in case of o being a Shutdownable, not a ClientInfo
     * @return (this == o OR this.getClient() == o)  
     */
        public boolean equals(Object o ){
        	// NOTE: This is not enough because of the way ArrayList.remove()
        	// is implemented: it calls o.equals(this) instead of
        	// this.equals(o)
        	// => need to override the "equals" of a ShutDownable or stuff it
        	// into a ClientInfos
        	CAT.debug("equals(Object) called");
        	boolean equal = this == o || this.client == o;
        	if (o != null) {
        		if (o instanceof ClientInfo){
        			Shutdownable oc = ((ClientInfo)o).getClient();
        			equal = this.client == oc;
        			if (!equal && this.client != null){
        				equal = this.client.equals(oc);
        			}
        		}
        	}
        	return equal;
        }
        /*
        public int hashCode(){
        	CAT.debug ("hashCode called");
        	Shutdownable mine = this.getClient();
        	if (mine != null){
        		return mine.hashCode();
        	}
        	else {
        		return super.hashCode();
        	}
        	
        }
   */
        public String toString(){
            String name = null;
            if (client instanceof BNSThread) {
                BNSThread  foo = (BNSThread) client;
                name = foo.getName();
            }
        
            return "client class="+client.getClass()
        		+"; name="+(name==null?"N/A":name)
        		+"; clientType="+clientType;
    }
            
}

interface GameOverListener{

     void gameIsOver(Game game);
    
}