package de.botsnscouts.util;

public class BNSThread extends Thread {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( BNSThread.class );

    private static int count =  0;

    private static ThreadGroup threadGroup = new ThreadGroup("BNS Threads") {
        public void uncaughtException( Thread t, Throwable e ) {
            if( e instanceof ThreadDeath )
                return; // do nothing
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

    public BNSThread() {
        this( "BNSThread-"+count );
    }

    public BNSThread(Runnable r) {
        super( threadGroup, r );
    }

    public BNSThread(Runnable r, String name) {
        super( threadGroup, r, name );
    }
}