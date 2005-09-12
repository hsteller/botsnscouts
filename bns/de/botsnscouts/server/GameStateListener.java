/*
 * Created on 12.09.2005
 *
 */
package de.botsnscouts.server;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public interface GameStateListener {

    public void gameStarted(Server theServer);
    
    public void gameFinished(Server theServer);
    
}
