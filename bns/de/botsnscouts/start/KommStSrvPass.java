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
 
package de.botsnscouts.start;

import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.io.*;


public class KommStSrvPass{

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(KommStSrvPass.class);

 private Socket tosrv=null;

 private InputStream fr=null;
 private OutputStream to=null;
 private BufferedReader frSrv=null;
 private PrintWriter toSrv=null;
    // private int PORT=8889;

 public KommStSrvPass(){
 }


 public boolean sendString(String s, String ip,int PORT){
     //     System.err.println("Sende Bot und Farbe: "+s+" an "+ip+": "+PORT);//!!!!!!
  try {

   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
   CAT.error("KommStSrvPass: Kann die Verbindung zum Server nicht herstellen!");
   return false;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
   CAT.error("Kann keine Streams öffnen!");
   return false;
 }

 try{
  if(frSrv.readLine().equals("StartSpielerListener ist bereit."))
   toSrv.println(s);
 } catch(Exception e){
  CAT.error("Kann nicht zum Server schreiben!");
  return false;
 }

 try{
     if(!frSrv.readLine().equals("OK."))
	 throw new Exception();
 } catch(Exception e){
     CAT.error("Keine (richtige) Antwort vom Server!");
     try{
	 frSrv.close();
	 toSrv.close();
	 fr.close();
	 to.close();
     } catch (Exception ex){
	 CAT.error("Kann Sockets nicht schließen");
	 return false;
     }
     return false;
 }

 try{
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
 } catch (Exception e){
  CAT.error("Kann Sockets nicht schließen");
  return false;
 }
    return true;
 } 

    protected void closeSock(){
	try{
	    //   srv.close();
	    tosrv.close();
	} catch (Exception e){
	    CAT.error("Kann die Sockets nicht schliessen!");
	}
    }


}

