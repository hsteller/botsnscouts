/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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
    
  
    public void bnsStart() throws JoiningGameFailedException{
       
    	Exception tmp; 
    	JoiningGameFailedException isNullInCaseOfSuccess=null;
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
        if (isNullInCaseOfSuccess == null) {          
            Registry.getSingletonInstance().addClient(host, port, this, type);
            super.start();
        }
        else {            
            String error ="Could not connect with the server at: "+host+":"+port;
            CAT.error(error);                        
            throw isNullInCaseOfSuccess;
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

