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
        private Game dummyCompareGame = new Game(null,null,-1);
        private HashMap clientsToGames = new HashMap();
        
        private Registry(){
            games = new LinkedList();
            
        }
        
        public static Registry getSingletonInstance(){
            return globalGameRegistry;
        }
        
        
        public void addGame(Server server, String serverIp, int serverPort){
            Game game = new Game(server, serverIp, serverPort);
        }
        
        private Game findGame(String serverIp, int port){
            dummyCompareGame.serverIp = serverIp;
            dummyCompareGame.serverPort = port;
            Iterator it = games.iterator();
            while (it.hasNext()){
                Object game = it.next();
                if (game.equals(dummyCompareGame)){
                    return (Game) game;
                }
            }
            return null;
        }
        
        private Game findGame(Shutdownable client) {
            return (Game) clientsToGames.get(client);
        }
        
        public void addClient(String serverIp, int port, Shutdownable client){
            Game game = findGame(serverIp, port);
            if (game != null){
                game.addClient(client);
                clientsToGames.put(client, game);
            }
            else {
                CAT.error("Registry: Tried to add a client to a non-existing game on "
                                +serverIp+":"+port);
            }
        }
        
        public void removeClient(Shutdownable client){           
            Game game = findGame(client);
            if (game != null){
                game.removeClient(client);                
            }
            else {
                CAT.error("Registry: Tried to remove a client from a non-existing game!");
            }
            clientsToGames.remove(client);            
        }
        
        public void shutdown(Shutdownable someThread){
            if (someThread instanceof Server) {
                
            }
            else {
                removeClient(someThread);
            }
            
        }
        
    }


    class Game {
        String serverIp;
        int serverPort;
        Server server;        
        Collection clients;
        
        public Game (Server serv, String serverIp, int serverPort){
            this.server = serv;
            this.serverIp = serverIp;
            this.serverPort = serverPort;                                
        }
        
        
        public void addClient(Shutdownable client){
           if (clients == null) {
               clients = new LinkedList();
           }
            clients.add(client);
        }
        
        public void removeClient (Shutdownable client){
            if (clients != null) {
                clients.remove(client);
            }
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
                return serverIp.equals(g.serverIp) && serverPort == g.serverPort;
            }
        }
        
        
    
}
