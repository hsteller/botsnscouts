package de.botsnscouts.server;

/** Schickt einem Thread nach definiertem Timeout einen notify(). 
 *  @author: Dirk
 */

class Timer extends Thread
{
    private int timeout;
    private Object toNotify;
    private boolean toVorbei;
    private boolean beenden=false;
        
    void setTimer(int to,Object tn)
        {
            timeout=to;
            toNotify=tn;
        }

    synchronized boolean vorbei()
        {
            return toVorbei;
        }

    void kill()
        {
            beenden=true;
        }
        
    void run()
        {
            synchronized(this){
                setName("TimerThread");
              forever: while(!beenden)
                  try{
                      //Global.debug(this,"Laying down for sleep... Please notify() me.");
                  
                      wait(); // warte bis mich wer braucht...
                  
                      toVorbei=false;
                      //Global.debug(this,"Starting Timer; to="+timeout+"; will notify "+toNotify);
                      synchronized(this){
                          wait(timeout);
                      }

                      toVorbei=true;
                      Global.debug(this,"Time's up! Notifying...");
                      synchronized(toNotify){
                          toNotify.notify();
                      }
                  } catch (InterruptedException e){
                      //Global.debug(this,"Interrupted... Going back to sleep... Zzz...");
                  }
            }
        }
}
 

