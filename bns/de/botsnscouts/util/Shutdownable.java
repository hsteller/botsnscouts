/*
 * Created on 08.12.2004
*/
package de.botsnscouts.util;

/**
 *  @author Hendrik Steller
 *  @version $Id$
 * 
 */
public interface Shutdownable {
    
   public void shutdown(); 

   public void addShutdownListener(ShutdownListener listener);
   
   public boolean removeShutdownListener(ShutdownListener listener);
   
   
   
}
