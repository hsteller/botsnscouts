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
import de.botsnscouts.util.*;

/**
* KommSpPr zusammen mit KommStSrv sorgt für die Netzwerkkommunikation
* zwischen StartSpieler und StartServer.
* @author  Ludmila und Leo Scharf
* @see     KommStSrv
* @see     StartSpieler
* @see     StartServer
*/
public class KommSpPr{

  /* CONSTRUCTORS */

 private Socket tosrv=null;

 private InputStream fr=null;
 private OutputStream to=null;
 private BufferedReader frSrv=null;
 private PrintWriter toSrv=null;
 private final int PORT=23722;
/**
*   Bindet Instance an Socket.
**/
 public KommSpPr(){
 }

/**
* Sendet an StartServer Request ein neues Spiel mit übergebenen Optionen zu starten, gibt
*     <code>true</code> oder <code>false</code> zurück.
* @param     ip IP Adresse des Servers
* @param     port Portnummer des Servers
* @param     anz  Anzahl der Mitspieler
* @param     anmTimeout  Anmeldetimeout in Sekunden
* @param     zugTimeout  Zugabgabetimeout in Sekunden
* @param     feld  Spielfeld laut Protokol
* @param     flagsX  Flagepositionen
* @param     flagY  als Koordinaten
* @return    <code>true</code>, falls das Spiel gestartet werden konnte, <code>false</code>, sonst
*/

 public void sendString(String s, String ip){
  try {

   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
//   System.err.println("Kann die Verbindung zum Server nicht herstellen!");
   return;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
//   System.err.println("Kann keine Streams öffnen!");
   return;
 }

 try{
  if(frSrv.readLine().equals("StartServer ist bereit."))
   toSrv.println(s);
 } catch(Exception e){
//  System.err.println("Kann nicht zum Server schreiben!");
  return;
 }

 try{
     if(!frSrv.readLine().equals("OK.")){
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
	 tosrv.close();
	 throw new Exception();
     }
 } catch(Exception e){
//  System.err.println("Keine (richtige) Antwort vom Server!");
  return;
 }

 try{
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
 } catch (Exception e){
//  System.out.println("Kann Sockets nicht schließen");
 }
    return;
 }

 public int newGame(String ip, int port,int anz, int anmTimeout, int zugTimeout, String feld, int[] flagsX, int[] flagsY, int spfX, int spfY,int sport){
  boolean ipgeaendert=false;
  Global.debug(this,"Starte neues Spiel!");

  try {
      tosrv=new Socket(ip,PORT);
      Global.debug(this,"OK, habe den alten StartServer!");
  }
  catch (Exception e){
      try{
	  Global.debug(this,"Starte einen neuen StartServer");

	  (new StartServer()).start();
	  ip="127.0.0.1";
	  Thread.sleep(2000);
	  tosrv=new Socket(ip,PORT);
	  ipgeaendert=true;
      }catch(Exception l){
	  return 1;
      }
      //   System.err.println("Kann die Verbindung zum Server nicht herstellen!");
//   return false;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
     //   System.err.println("Kann keine Streams öffnen!");
   return 1;
 }

 String tosr="NSS("+anz+"\n"+port+"\n"+anmTimeout+"\n"+zugTimeout+"\n"+spfX+"\n"+spfY+"\n"+feld+"\n&\n"+flagsY.length+"\n";

 for(int i=0;i<flagsX.length;i++)
  tosr=tosr+flagsX[i]+"\n"+flagsY[i]+"\n";

 tosr=tosr+")"+"\n"+sport;

 try{
  if(frSrv.readLine().equals("StartServer ist bereit."))
   toSrv.println(tosr);
 } catch(Exception e){
     //  System.err.println("Kann nicht zum Server schreiben!");
  return 1;
 }

 try{
     if(!frSrv.readLine().equals("OK.")){
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
  tosrv.close();
   throw new Exception();
     }
 } catch(Exception e){
     //  System.err.println("Keine (richtige) Antwort vom Server!");
  return 1;
 }

 try{
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
 } catch (Exception e){
     //  System.out.println("Kann Sockets nicht schließen");
 }

  if (ipgeaendert) return 2; else return 0;
}

 public boolean cancelGame(String ip, int port){//spiel abbrechen

  try {
   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
      System.err.println(Message.say("StartServer","eNoConnection"));
      return false;
  }

  try{
      fr = tosrv.getInputStream();
      to = tosrv.getOutputStream();

      frSrv = new BufferedReader(new InputStreamReader(fr));
      toSrv = new PrintWriter(new OutputStreamWriter(to), true);
  } catch(Exception e){
      System.err.println(Message.say("StartServer","eNoConnection"));
      //   System.err.println("Kann keine Streams öffnen!");
      return false;
  }

  String tosr="SAB("+port+")\n";

  try{
      if(frSrv.readLine().equals("StartServer ist bereit."))
	  toSrv.println(tosr);
      //  System.out.println("Habe "+tosr+" an StartServer gesendet");
  } catch(Exception e){
      System.err.println(Message.say("StartServer","eNoConnection"));
      //  System.err.println("Kann nicht zum Server schreiben!");
      return false;
  }

  try{
      if(!frSrv.readLine().equals("OK.")){
	  frSrv.close();
	  toSrv.close();
	  fr.close();
	  to.close();
	  tosrv.close();
	  throw new Exception();
      }
  } catch(Exception e){
      System.err.println(Message.say("StartServer","eNoConnection"));
      //  System.err.println("Keine (richtige) Antwort vom Server!");
      return false;
  }

  try{
      frSrv.close();
      toSrv.close();
      fr.close();
      to.close();
      tosrv.close();
  } catch (Exception e){
      System.err.println(Message.say("StartServer","eNoConnection"));
      //  System.out.println("Kann Sockets nicht schließen");

  }

  return true;
 }


 public boolean game(String ip, int port){//spiel geht los
  try {
   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
      //   System.err.println("Kann die Verbindung zum Server nicht herstellen!");
   return false;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
     //   System.err.println("Kann keine Streams öffnen!");
   return false;
 }

 String tosr="LOS("+port+"\n";

 try{
  if(frSrv.readLine().equals("StartServer ist bereit."))
   toSrv.println(tosr);
  //  System.out.println("Habe "+tosr+" an StartServer gesendet");
 } catch(Exception e){
     //  System.err.println("Kann nicht zum Server schreiben!");
  return false;
 }

 try{
     if(!frSrv.readLine().equals("OK.")){
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
  tosrv.close();
   throw new Exception();
     }
 } catch(Exception e){
     //  System.err.println("Keine (richtige) Antwort vom Server!");
  return false;
 }

 try{
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
  tosrv.close();
 } catch (Exception e){
     //  System.out.println("Kann Sockets nicht schließen");
 }

  return true;
}



/**
* Sendet an StartServer Request künstliche Spieler zu starten, gibt
*     <code>true</code> oder <code>false</code> zurück.
* @param     ip  Adresse des Servers
* @param     port  Anmeldeportnummer des Servers
* @param     anz  Anzahl der Künstlichen Spielern
* @return    <code>true</code>, falls Spieler gestartet werden konnten, <code>false</code>, sonst
*/
 public boolean newKS(String ip, int port, int anz,int iq){
  try {

   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
      //   System.err.println("Kann die Verbindung zum Server nicht herstellen!");
   return false;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
     //   System.err.println("Kann keine Streams öffnen!");
   return false;
 }

 String tosr="KSS("+anz+"\n"+port+"\n"+iq+"\n)";

 try{
  if(frSrv.readLine().equals("StartServer ist bereit."))
   toSrv.println(tosr);
 } catch(Exception e){
     //  System.err.println("Kann nicht zum Server schreiben!");
  return false;
 }

 try{
  if(!frSrv.readLine().equals("OK."))
   throw new Exception();
 } catch(Exception e){
     //  System.err.println("Keine (richtige) Antwort vom Server!");
  return false;
 }

 try{
  frSrv.close();
  toSrv.close();
  fr.close();
  to.close();
 } catch (Exception e){
     //  System.out.println("Kann Sockets nicht schließen");
 }


  return true;
 }

}
