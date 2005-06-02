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
 
package de.botsnscouts.util;

public abstract class BNSThread extends Thread implements Shutdownable {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( BNSThread.class );

    /** boolean is set to true once this BNSThread's shutdown() method has been called*/ 
    private boolean hasBeenShutDown = false;
    
    private static int count =  0;    
    
    private static ThreadGroup threadGroup = new ThreadGroup("BNS Threads") {
        public void uncaughtException( Thread t, Throwable e ) {
            if( e instanceof ThreadDeath )
                CAT.debug("Thread "+t.getName()+" exited gracefully.");
            else
                CAT.error("Exception in thread: " + t.getName(), e);
        }
    };

    public static ThreadGroup getBNSThreadGroup() {
        // if s.o. ever needs to build ones own threadgroup, this one should be its parent
        // so that logging exceptions will automatically work
        return threadGroup;
    }

    public BNSThread(String name) {
        super( threadGroup, name );
        count++;
    }

    private BNSThread() {
        this( "BNSThread-"+count );
    }

    public BNSThread(Runnable r) {
        super( threadGroup, r );
    }

    public BNSThread(Runnable r, String name) {
        super( threadGroup, r, name );
    }
    
    public String toString(){
        String s = super.toString();     
        return s;
    }
    
    private ShutdownableSupport shutdownSupport = new ShutdownableSupport(this);
    
    /** Override this method instead of "shutdown()" */
    public abstract void doShutdown();
 
    public final void shutdown(){
        try {
            doShutdown();
        }
        finally {
            shutdownSupport.shutdown();
            hasBeenShutDown = true;
        }
    }
    
    public boolean isShutDown(){
        return hasBeenShutDown;
    }
    
    public void addShutdownListener(ShutdownListener l){
        shutdownSupport.addShutdownListener(l);
    }
    
    public boolean removeShutdownListener(ShutdownListener l){
        return shutdownSupport.removeShutdownListener(l);
    }
}