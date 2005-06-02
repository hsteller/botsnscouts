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
   
   /** Should be set to true once shutdown() got called */
   public boolean isShutDown();

   public void addShutdownListener(ShutdownListener listener);
   
   public boolean removeShutdownListener(ShutdownListener listener);
   
   
   
}
