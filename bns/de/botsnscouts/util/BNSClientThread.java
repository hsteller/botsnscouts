/*
 * Created on 11.09.2005
 *
 */
package de.botsnscouts.util;


import de.botsnscouts.comm.KommException;
import de.botsnscouts.server.RegistrationException;
import de.botsnscouts.start.JoiningGameFailedException;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public abstract  class BNSClientThread extends BNSThread {
    
    private static final int REGISTRATION_RETRY_INTERVAL = 3000;
    private static final int MAX_RETRIES = 3;
  
    
    private String host;
    private int port;
    private int type;

    /**
     * 
     * @param clientName The name of the client
     * @param clientType one of the constants defined in @see de.botsnsouts.util.Registry
     * @param serverHost
     * @param serverPort
     */
    public BNSClientThread(String clientName, int clientType,  String serverHost, int serverPort){
        super(clientName);
        this.host = serverHost;
        this.port = serverPort;
        this.type = clientType;
    }
    
    public void start(){       
       throw new RuntimeException ("You are supposed to call bnsStart() instead of start()!");
    }
    
    // interestingly enough, "volatile" doesn't seem to work on my versions of
    // SUN's JDKs 1.4 and 1.5 on windows; on SuSE it didn't even need
    // volatile; anyway, letting the Threads sync on this dummy object
    // should work everywhere
    private Object myWindowsSucksVolatileHack = new Object();
    
    public void bnsStart() throws JoiningGameFailedException{
       
        RegistrationRunner  dontWantToLockAWT = new RegistrationRunner();        
        Thread t = new Thread(dontWantToLockAWT);          
        t.start();
        try {
            t.join(MAX_RETRIES*REGISTRATION_RETRY_INTERVAL+500);
            synchronized(myWindowsSucksVolatileHack){
            	// an interesting effect with different JDKs on Windows:
            	// isNullInCaseOfSuccess will be set waaaaay after this point
            	// (allthough the join() waits long enough and there is no 
            	//  InterruptedException)
            	// => isNullInCaseOfSuccess is _always_ null in the if-statement
            	// below;
            	// marking isNullInCaseOfSuccess "volatile" didn't help either..
            	myWindowsSucksVolatileHack.wait();
            	// also, we MUST USE WAIT AND JOIN
            	// removing the join() and using wait(MAX_RETRIES*..) also had
            	// the above effect
            }
        }
        catch (InterruptedException ie){
            CAT.warn(ie.getMessage(), ie);
        }
        if (dontWantToLockAWT.isNullInCaseOfSuccess == null) {          
            Registry.getSingletonInstance().addClient(host, port, this, type);
            super.start();
        }
        else {            
            String error ="Could not connect with the server at: "+host+":"+port;
            CAT.error(error);                        
            throw dontWantToLockAWT.isNullInCaseOfSuccess;
        }
     
    }
    
    public String getServer(){
        return host;
    }
    
    public int getPortOnServer(){
        return port;
    }
    
    
    
    
    public abstract boolean sendRegistrationRequestOnce( String hostname, int portNr) 
    throws KommException, RegistrationException;
        
   
    
   
    class RegistrationRunner implements Runnable{
       JoiningGameFailedException isNullInCaseOfSuccess = null;
       public void run(){
	       	try {
	           Exception tmp;
	           	try {
	           	    tmp = registerAtServer();
	           	}
	           	catch (Exception e){
	           	    tmp = e;
	           	}
	           	if (tmp != null) {
		           	if (tmp instanceof JoiningGameFailedException){
		           	    isNullInCaseOfSuccess = (JoiningGameFailedException) tmp;
		           	}
		           	else {
		           	    isNullInCaseOfSuccess = new JoiningGameFailedException(tmp);
		           	}
	           	}
	       	}
	       	finally {
	       		synchronized(myWindowsSucksVolatileHack){
	       			myWindowsSucksVolatileHack.notify();
	       		}
	       	}
        }
       	
       private Exception registerAtServer() {
           boolean registrationSuccess = false;
           int retries = 0;
           Exception lastEx = null;
           while ((!registrationSuccess) && (retries < MAX_RETRIES)) {
               try {
                   registrationSuccess = sendRegistrationRequestOnce(host, port);                                  
               }
               catch (RegistrationException re){
                  // no retries in that case, we have encountered some "logical error":
                  // the name is already in use, the game has already started or the 
                   // maximum number of players is already registered
                   return new JoiningGameFailedException(re);                   
               }
               catch (Exception kE) {
                   lastEx = kE;
                   CAT.debug(kE.getMessage());                                                   
               }            
               if (!registrationSuccess) {
                   waitToRetry();
                   retries++;
               }
           }
     
           if (registrationSuccess) {
               CAT.debug("registered for game with name: " + getName());
               return null;
           } 
           else {                  
               String error = "could not register at the server: " + host+":"+port; 
               CAT.warn(error);
               lastEx =  new JoiningGameFailedException(lastEx);
               return lastEx;
           }          
       }
       
       private void waitToRetry(){
           try {
               synchronized(this) {
                   this.wait(REGISTRATION_RETRY_INTERVAL);
               }
           } 
           catch (InterruptedException e) {
               CAT.warn(e.getMessage(),e );
           }
       }
       
    }

}