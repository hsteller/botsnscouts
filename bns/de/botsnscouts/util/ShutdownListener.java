/*
 * Created on 23.05.2005
 */
package de.botsnscouts.util;

/**
 *  @author Hendrik Steller
 *  @version $Id$
 * 
 */
public interface ShutdownListener {

    public void shutdown(Shutdownable isNowDown);
    
}
