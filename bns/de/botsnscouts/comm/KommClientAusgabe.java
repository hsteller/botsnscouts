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

package de.botsnscouts.comm;


import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import de.botsnscouts.server.RegistrationException;
import de.botsnscouts.util.Encoder;
import de.botsnscouts.util.Stats;
import de.botsnscouts.util.StatsList;

/**
 * This class contains the communication stuff that is only relevant for a view("Ausgabe")
 * 
 * @author Hendrik Steller
 * @version $Id$
 */
public class  KommClientAusgabe extends KommClient {
  public KommClientAusgabe() {
  super();
  }
  /** 
   * Registers this view (Ausgabekanal) at the server
   *  
   * @param ipnr ip or hostname of the server
   * @param port the port the server uses to listen for clients to register
   * @param name the name of this view 
   * @return true if the registration was successful
   * 
   * @exception KommException thrown if parsing of the server answer failed or in case of an I/O error   
   * @exception RegistrationException thrown if the server has a logical reason to deny registration
   * (too many players, name already in use..)
   **/
  
  public boolean anmelden  (String ipnr, int portnr, String name) throws KommException, RegistrationException{
      return super.anmelden (ipnr, portnr, Encoder.commEncode(name), "RGA");
  }

  // here: name encoding not really needed, because we use our generated random
  // names that do not contain 'evil' characters..
 // ..but who knows who will use these methods 100years later
 public boolean anmelden2  (String ipnr, int portnr, String name) throws KommException, RegistrationException{
     return super.anmelden (ipnr, portnr, Encoder.commEncode(name), "RA2");
  }


/**
 * The "answer" for a "notify change"-message from the server.
 * Sends a confirmation  to signal he server that we have done (displayed) all the stuff we wanted
 * and the server may continue (with the phase evaluation).
 */
 public void aenderungFertig() {
      try {
          decNTCCounter();
		  this.senden("ok");
		  
      }
      catch (KommFutschException k) {
		  System.err.println ("Exception bei KCA.aenderungfertig(von"+cn+"\nMessage: "+k.getMessage());
		  k.printStackTrace();
      }
  }

    /** Acknowledges a Message has been received */
    public void acknowledgeMsg() throws KommFutschException{
        decMSGCounter();
        this.senden("MOK");
    }

    /** This methods asks for the actual game stats (laserhits).
	@throws KommException if an error occurs
	@return A sorted list of Stats-objects.
    */

    public StatsList getStats() throws KommException {
        // Should be placed in StatsList, like toSendString()

        // format: GST(name,int,int,int,int,name,..,name,int,int,int,int)
	senden("GST");
	String rein = einlesen();
	if (rein.startsWith("GST")) {
	    Vector stats = new Vector();
	    StringTokenizer st = new StringTokenizer (rein.substring(4,rein.length()-1),",");
	    while (st.hasMoreElements()) {
		Stats s = new Stats (Encoder.commDecode(st.nextToken()));
	    try {
		s.setHits(Integer.parseInt(st.nextToken()));
		s.setKills(Integer.parseInt(st.nextToken()));
		s.setDamageByBoard(Integer.parseInt(st.nextToken()));
		s.setDamageByRobots(Integer.parseInt(st.nextToken()));
                s.setAskedWisenheimer(Integer.parseInt(st.nextToken()));
                s.setWasSlowest(Integer.parseInt(st.nextToken()));
		stats.addElement(s);
	    }
	    catch (NumberFormatException nfe) {
		nfe.printStackTrace();
	    }
	    }
	    return new StatsList(stats);
	}
	else
	    throw new KommException ("getStats: Wrong answer: "+rein);
    }
    public synchronized void abmelden (String name) {        
            while (getNTCCounter()>0){
                aenderungFertig();           
            }
            while (getMSGCounter()>0){
                try {
                    acknowledgeMsg();
                }
                catch (KommException willOnlyBeThrownAfterDecreasingTheCounter){
                    CAT.warn(willOnlyBeThrownAfterDecreasingTheCounter.getMessage(),
                                    willOnlyBeThrownAfterDecreasingTheCounter);
                }
            }        
        
	        String back="RLE("+encodedName+")";        
	        if (out!=null && !deregistered)  {      
	            try {
	                senden(back);
	                CAT.debug("waiting, so the server can receive the message before someone closes the socket");
	                this.wait(300); // wait for the server to receive the message 
	                CAT.debug("done waiting");
	            }
	            catch (Exception ke){
	                CAT.warn(ke.getMessage(), ke);
	            }
	        }
        
    }
    private boolean deregistered = false;
 
}



