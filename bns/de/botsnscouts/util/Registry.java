/*
 * Created on 23.05.2005
 *
 * 
 */
package de.botsnscouts.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Category;
import de.botsnscouts.autobot.AutoBot;
import de.botsnscouts.gui.Ausgabe;
import de.botsnscouts.gui.HumanPlayer;
import de.botsnscouts.server.Server;


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
public class Registry implements ShutdownListener {
    
    	private static Category CAT = Category.getInstance(Registry.class);
   
    	private static Registry globalGameRegistry = new Registry();
    	
        private Collection games;
        private Game dummyCompareGame = new Game(null,"DUMMY",-1);
        private HashMap clientsToGames = new HashMap();
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
            Game game = new Game(server, serverIp, serverPort);
            synchronized (games) {
                games.add(game);
            }
        }
        
        private Game findGame(String serverIp, int port){           
            dummyCompareGame.setServerIp(serverIp);
            dummyCompareGame.setServerPort(port);
            synchronized (games) {
	            Iterator it = games.iterator();
	            while (it.hasNext()){
	                Object game = it.next();
	                if (game.equals(dummyCompareGame)){
	                    return (Game) game;
	                }
	            }
            }
            return null;
        }
        
        private Game findGame(Shutdownable client) {
            return (Game) clientsToGames.get(client);
        }
        
        
        public void addClient (HumanPlayer player, String serverIp, int port){
            addClient(serverIp, port, player, ClientInfo.CLIENT_TYPE_HUMANPLAYER);
        }
        
        public void addClient (AutoBot player, String serverIp, int port){
            addClient(serverIp, port, player, ClientInfo.CLIENT_TYPE_AUTOBOT);
        }
        
        public void addClient (Ausgabe view, String serverIp, int port){
            addClient(serverIp, port, view, ClientInfo.CLIENT_TYPE_VIEW);
        }
        
        private  void addClient(String serverIp, int port, Shutdownable client, int clientInfoClientType){
            if (!isEnabled) {
                return;
            }
            Game game = findGame(serverIp, port);
            if (game == null) {
                CAT.info("Registry: Tried to add a client to a non-existing game on "
                                +serverIp+":"+port+"; this is ok if you participate in a non-local game");             
                addGame(null, serverIp, port);  
            }            
            ClientInfo info = new ClientInfo( client, clientInfoClientType);
            game.addClient(info);
            clientsToGames.put(client, game);            
        }
        
        public boolean isEmpty() {                
            synchronized (games){
                return games.isEmpty();
            }
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
        
        public void shutdown(Shutdownable someThread){           
            if (!isEnabled){
                // this is supposed to catch the case where someone (probably developer..)  starts a client via CLI
                // in this case, the client will not be bound to the Registry as it wasn't started via start.Launcher;
                // depending on the not-yet existing implementation of shutdwon() it is very likely that we will
                // (at least try to) display the main menu after the CLI-started player has shut down and its
                // inherited Shutdownable support calls Registry.shutdown() - and ending a CLI-based client
                // program by launching the main application sounds somewhat wrong..
                return;
            }
            if (someThread instanceof Server) {
                // TODO wait some time and then shut down all clients associated with this server;
                // then redisplay the launcher app
            }
            else {
                // some client
                // TODO 
                // 1. remove the client from the game
                // 2. check the game; 
                //     if other clients are associated with the game {
                //        if there is no other View/HumanPlayer {
                //            ??? hmm, kill the server and all other clients, then redisplay launcher?
                //        }
                //     }
                //    else {
                //			redisplay launcher??
                //    }
                Game clientsGame = findGame(someThread);
                if (clientsGame == null){
                    CAT.error("no game found for the client that is going down, hmm, let's see what there is to do..");
                    if (isEmpty()) {
                       CAT.error ("\tsince there seems to be nothing else runnig, we will redisplay the main menu");
                        // TODO if Registry contains no other game, redisplay launcher; HMM, there might
                    }
                    else {
                        CAT.error("\tThere seems to be another game running..");
                        // TODO if there is a game with a server or another GUI (HumanPlayer/View client), do nothing
                        //          otherwise: redisplay (??hmm, then it must be AutoBots..simply killing them should be ok
                        //          as the only way to start them is AFAIK AutoBot.main so 
                    }
                }
                //removeClient(someThread);
            }
            
        }
        
    }


    class Game {
        private static Category CAT = Category.getInstance(Game.class);
        
        public static final int CLIENT_TYPE_HUMANPLAYER = 1;
        public static final int CLIENT_TYPE_VIEW             = 2;
        public static final int CLIENT_TYPE_AUTOBOT       = 3; 
        
        private String servIp;
        private int serverPort;
        /** May be null, only exists for local games */
        private Server server;        
        private Collection clients;
        
        public Game (Server serv, String ip, int serverPort){
            this.server = serv;
            this.servIp = ip;
            this.serverPort = serverPort;                                
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
               clients = new LinkedList();
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
         *   to modify ClientInfo.equals() in a way that it works with CllientInfos AND Shutdownables.
         * 
         *  ..or this is simply crazy 3:00 AM thinking ;-)
         * 
         */
        public boolean removeClient (Shutdownable client){
            synchronized (clients) {
                if (clients != null) {
                    boolean success = clients.remove(client);
                    CAT.debug("removing of 'Shutdownable' from the 'ClientInfo' collection was "+(success?"":"NOT ")+"successful");
                    return success;
                }
            }
            return false;
        }
            
         public String dump() {                 
                 StringBuffer sb = new StringBuffer();
                 sb.append("Server address="+servIp+':'+serverPort+"\tserver="+server+"\nClients:\n");
                 synchronized (clients) {
                     Iterator it = clients.iterator();
	                 while (it.hasNext()){
	                     Object o = it.next();
	                     sb.append(o!=null?o.toString():o);
	                 }
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
                                    
}
        
/** Somehow not in the mood for "instanceof" right now, so here comes another inner class.. ;-)
 *   And who knows, there might be other info besides the client type that we want to keep later. 
 * */
class ClientInfo {
   
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
            	return this == o || this.getClient() == o;
            
        }
   
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

