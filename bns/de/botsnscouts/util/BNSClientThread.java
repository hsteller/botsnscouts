/*
 * Created on 11.09.2005
 *
 */
package de.botsnscouts.util;


import de.botsnscouts.comm.KommException;
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
    
    public void bnsStart() throws JoiningGameFailedException{
       
        RegistrationRunner  dontWantToLockAWT = new RegistrationRunner();        
        Thread t = new Thread(dontWantToLockAWT);          
        t.start();
        try {
            t.join(MAX_RETRIES*REGISTRATION_RETRY_INTERVAL+500);
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
    throws KommException;
        
   
    
   
    class RegistrationRunner implements Runnable{
       JoiningGameFailedException isNullInCaseOfSuccess = null;
       public void run(){
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
       private Exception registerAtServer() {
           boolean registrationSuccess = false;
           int retries = 0;
           Exception lastEx = null;
           while ((!registrationSuccess) && (retries < MAX_RETRIES)) {
               try {
                   registrationSuccess = sendRegistrationRequestOnce(host, port);                                  
               }
               catch (Exception kE) {
                   lastEx = kE;
                   CAT.debug(kE.getMessage());              
                   retries++;                   
               }            
               if (!registrationSuccess) {
                   waitToRetry();
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