/*
 * Created on 23.05.2005
 */
package de.botsnscouts.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Hendrik Steller
 * @version $Id$
 * 
 */
public class ShutdownableSupport  {

    private Collection listeners;
    private Shutdownable ownerInNeedOfSupport;
    
    public ShutdownableSupport(Shutdownable supportee){
        listeners = new LinkedList();
        ownerInNeedOfSupport = supportee;
    }
    
    public void addShutdownListener(ShutdownListener listener){
        listeners.add(listener);
    }  
    
    public boolean removeShutdownListener(ShutdownListener listener){
        return listeners.remove(listener);
    }
    
    /** 
     * The Shutdownable using this helper object should call this method at the
     * end of its shutdown()-method.
     * It will notify all listeners.
     */
    public void shutdown(){
        Iterator it = listeners.iterator();
        while (it.hasNext()){
            ((ShutdownListener) it.next()).shutdown(ownerInNeedOfSupport);
        }
        
    }
    
    
    
}
