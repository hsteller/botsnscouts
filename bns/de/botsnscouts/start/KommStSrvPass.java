package de.botsnscouts.start;

import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.io.*;


public class KommStSrvPass{



 private Socket tosrv=null;

 private InputStream fr=null;
 private OutputStream to=null;
 private BufferedReader frSrv=null;
 private PrintWriter toSrv=null;
    // private int PORT=8889;

 public KommStSrvPass(){
 }


 public boolean sendString(String s, String ip,int PORT){
     //     System.err.println("Sende Roboter und Farbe: "+s+" an "+ip+": "+PORT);//!!!!!!
  try {

   tosrv=new Socket(ip,PORT);
  }
  catch (Exception e){
   System.err.println("KommStSrvPass: Kann die Verbindung zum Server nicht herstellen!");
   return false;
  }

 try{
  fr = tosrv.getInputStream();
  to = tosrv.getOutputStream();

  frSrv = new BufferedReader(new InputStreamReader(fr));
  toSrv = new PrintWriter(new OutputStreamWriter(to), true);
 } catch(Exception e){
   System.err.println("Kann keine Streams öffnen!");
   return false;
 }

 try{
  if(frSrv.readLine().equals("StartSpielerListener ist bereit."))
   toSrv.println(s);
 } catch(Exception e){
  System.err.println("Kann nicht zum Server schreiben!");
  return false;
 }

 try{
     if(!frSrv.readLine().equals("OK."))
	 throw new Exception();
 } catch(Exception e){
     System.err.println("Keine (richtige) Antwort vom Server!");
     try{
	 frSrv.close();
	 toSrv.close();
	 fr.close();
	 to.close();
     } catch (Exception ex){
	 System.out.println("Kann Sockets nicht schließen");
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
  System.out.println("Kann Sockets nicht schließen");
  return false;
 }
    return true;
 } 

    protected void closeSock(){
	try{
	    //   srv.close();
	    tosrv.close();
	} catch (Exception e){
	    System.err.println("Kann die Sockets nicht schliessen!");
	}
    }


}

