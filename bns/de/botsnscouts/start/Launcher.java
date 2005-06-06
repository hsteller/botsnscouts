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

package de.botsnscouts.start;

import org.apache.log4j.Category;

import de.botsnscouts.autobot.AutoBot;
import de.botsnscouts.gui.Ausgabe;
import de.botsnscouts.gui.HumanPlayer;
import de.botsnscouts.server.Server;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Registry;

// launches human player, output ...

public class Launcher implements RegistrationStartListener{

    static final Category CAT = Category.getInstance(Launcher.class);

    private static Registry gameRegistry = Registry.getSingletonInstance();
    
    private Server server;

    /** Only created so this class and the static methods can implement and 
     *  use the RegistrationListener interface.
     *  ParticipateInAGame will use this method to synchronize on so that the  player
     *  won't be launched until the server has signaled that the registration has started. 
     */
    private static Launcher meTheLauncher = new Launcher();
    
    // launches output
    public static BNSThread watchAGame(String ip, int port, boolean noSplash) {
       
        try {
            Ausgabe view = new Ausgabe(ip, port, noSplash){
                public void doShutdown() {
                    // TODO maybe kill the Ausgabe?
                }     
            };
            gameRegistry.addClient(view, ip, port);
            view .start();
            return view;
           
        } catch (Exception exp) {
            return null;
        }
        
    }

    // launches human player
    public static BNSThread participateInAGame(String ip, int port, String name, int farbe, boolean noSplash) {
        meTheLauncher.ensureRegistrationHasStarted();
        HumanPlayer ret;
        try {
            if (CAT.isDebugEnabled()) CAT.debug("Trying to start human player...");
            ret = new HumanPlayer(ip, port, name, farbe, noSplash);
            ret.start();
            gameRegistry.addClient(ret, ip, port);
        } catch (Exception u) {
            CAT.error("Error while starting the game for player " + name + ": " + u.getMessage());
            return null;
        }
        return ret;
    }

    // launch autobots
    public static BNSThread startAutoBot(String ip, int port, int iq) {
        return startAutoBot(ip, port, iq, false);
    }

    public static BNSThread startAutoBot(String ip, int port, int iq, boolean beltAware) {
        return startAutoBot(ip, port, iq, beltAware, KrimsKrams.randomName());
    }

    public static BNSThread startAutoBot(String ip, int port, int iq, boolean beltAware,
                                      String botName) {      
        CAT.debug("creating AutoBot-BNSThread for "+botName);
        if (ip == null) { 
            ip=GameOptions.DHOST; 
        }
        AutoBot ks = new AutoBot(ip, port, iq, beltAware, botName);       
        ks.setPriority(java.lang.Thread.MIN_PRIORITY);
        ks.start();
        gameRegistry.addClient(ks, ip, port);
        CAT.debug(botName+" started");
        return ks;
    }

    public Server startGame(GameOptions options, ServerObserver listener) throws OneFlagException, NonContiguousMapException {
       server = new Server(options, listener);
       server.addRegistrationStartListener(meTheLauncher);
       server.start();
       String ip = options.getHost();
       if (ip == null){
           ip = GameOptions.DHOST;
       }
       gameRegistry.addGame(server, ip, options.getRegistrationPort());
       return server;
   }


    public void gameStarts(String ip, int port) {
        server.startGame();
    }

    public void stopServer() {
        // Not nice, but this is the way it was done before...
      
        server.shutdown();              
        server = null;
        System.gc();
    }
    
    private volatile boolean regHasStarted = false;
   
    public  void registrationStarted(){
        synchronized (meTheLauncher) {
            regHasStarted = true;
            meTheLauncher.notifyAll();
            
        }
    }
    
    private void ensureRegistrationHasStarted(){
        synchronized (meTheLauncher) {
	        while (!regHasStarted) {
	            try {
	                meTheLauncher.wait();
	            }
	            catch (InterruptedException ie){
	                CAT.error(ie);
	                meTheLauncher.notifyAll();
	            }	            	            
	        }
	       meTheLauncher.notifyAll();
        }
        
    }
    

    
    
  

}
