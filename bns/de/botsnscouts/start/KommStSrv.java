package de.botsnscouts.start;

import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.io.*;
import de.botsnscouts.util.*;

/**
* KommStSrv zusammen mit KommSpPr sorgt für die Netzwerkkommunikation
* zwischen StartSpieler und StartServer.
* @author  Ludmila und Leo Scharf
* @see     KommSpPr
* @see     StartSpieler
* @see     StartServer
*/
public class KommStSrv{

    /**
     * Anmeldetimeout in Sekunden
     **/
    public int anmTimeout;
    /**
     * Zugabgabetimeout in Sekunden
     **/
    public int zugTimeout;
    public int port;
    public String cltIP=null;
    public int cltPort=0;
    public int feldX,feldY;
    public int iq;
    /**
     * Spielfeld laut Protokol
     **/
    public String feld;
    /**
     * Flage laut Protokol
     **/
    public Ort[] flags;



    /**
     * Erzeugt ein Kommunikationsobjekt und bindet ihn an ein Port an.
     **/
    private ServerSocket srv=null;

    private  Socket clt =null;
    private  InputStream inp =null;
    private  BufferedReader br=null;
    private  OutputStream outp=null;
    private  PrintWriter pw=null;

    private final int PORTNR=8888;
    private String fromclt=null;

    public KommStSrv() throws Exception{
	//  try{
	srv = new ServerSocket(PORTNR);
	//  } catch (Exception e){System.err.println("Kann nicht ServerSocket!");}

    }

    /**
     * Wartet auf Verbindungen, parst gesendete String, und wenn sie dem Format entsprechen,
     * gibt ein Integer zurück, die restlichen Informationen, wenn nötig können aus Klassenvariablen
     * entnommen werden
     * @return Positiver Integer <code>n</code> bedeutet neues Spiel
     mit <code>n</code> Mitspielern starten, * negativer Integer
     <code>-n</code> bedeutet <code>n</code> künstliche Spieler
     Starten. */  
    public int listen(){


	try{
	    clt = srv.accept();
	    clt.setSoTimeout(30000);
	    cltIP=clt.getInetAddress().getHostAddress();
	    //   cltPort=clt.getPort();
	    //System.out.println("Ein Klient!");
	} catch (Exception e){System.err.println("StSrv: Kann nicht ACCEPT!");}

	try{
	    pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
	} catch (Exception e){
	    System.err.println("StSrv: Kann nicht getOutputStream!");}

	try{
	    br= new BufferedReader(new InputStreamReader(clt.getInputStream()));
	} catch (Exception e)
	    {System.err.println("StSrv: Kann nicht getInputStream!");}

	try{
	    pw.println("StartServer ist bereit.");
	    //if(br.ready())
	    fromclt=br.readLine();
	} catch (Exception e){System.err.println("StSrv: Kann nicht println/readLine!");}
	//System.out.println(fromclt.substring(0,4));
	try{
	    if(fromclt.substring(0,4).equals("NSS(")){//neues spiel starten
		int ret=Integer.parseInt(fromclt.substring(4,5)); //anzahl der mitspieler
		fromclt=br.readLine();
		port=Integer.parseInt(fromclt);
		fromclt=br.readLine();
		anmTimeout=Integer.parseInt(fromclt);
		fromclt=br.readLine();
		zugTimeout=Integer.parseInt(fromclt);
		fromclt=br.readLine();
		feldX=Integer.parseInt(fromclt);
		fromclt=br.readLine();
		feldY=Integer.parseInt(fromclt);

		feld=new String();
		fromclt=br.readLine();
		while(!fromclt.equals("&")){
		    feld+=fromclt+"\n";
		    fromclt=br.readLine();
		}
		flags=new Ort[Integer.parseInt(br.readLine())];
		for(int i=0;i<flags.length;i++){
		    flags[i]=new Ort(Integer.parseInt(br.readLine()),Integer.parseInt(br.readLine()));
		    /*     flags[i].x=Integer.parseInt(br.readLine());
			   flags[i].y=Integer.parseInt(br.readLine());*/
		}
		fromclt=br.readLine();
		fromclt=br.readLine();
		cltPort=Integer.parseInt(fromclt);
		Global.debug(this,"StSpListener Port: "+cltPort);
		Global.debug(this,"gebe "+ret+" an StartServer zurück");
		return ret;//Integer.parseInt(fromclt.substring(4,5));
	    }
	    if(fromclt.substring(0,4).equals("KSS(")){
		int ret=(Integer.parseInt(fromclt.substring(4,5)))*(-1);
		fromclt=br.readLine();
		port=Integer.parseInt(fromclt);
		fromclt=br.readLine();
		iq=Integer.parseInt(fromclt);
		return ret;//(Integer.parseInt(fromclt.substring(4,5)))*(-1);
	    }

	    if(fromclt.substring(0,2).equals("GS")){
		return 10;//(Integer.parseInt(fromclt.substring(4,5)))*(-1);
	    }

	    if(fromclt.substring(0,4).equals("LOS(")){
		port=Integer.parseInt(fromclt.substring(4,8));
		//     System.out.println("Habe "+fromclt+" bekommen");
		return 9;//(Integer.parseInt(fromclt.substring(4,5)))*(-1);
	    }

	    if(fromclt.substring(0,4).equals("SAB(")){//spiel abbrechen
		port=Integer.parseInt(fromclt.substring(4,8));
		//     System.out.println("Habe "+fromclt+" bekommen");
		return 10000+port;//(Integer.parseInt(fromclt.substring(4,5)))*(-1);
	    }

	    if(fromclt.substring(0,4).equals("back"))
		return 9999;
	} catch (Exception e){
	    System.err.println("Klientenfehler!"+e);
	}
	System.out.println(fromclt);
	return 0;
    }

    private void parse(){
    }

    /**
     * Sendet an StartSpieler String "OK"
     */
    public void ok(){
	pw.println("OK.");
	try{
	    //   srv.close();
	    clt.close();
	} catch (Exception e){
	    System.err.println("Kann die Sockets nicht schliessen!");
	}
    }

    protected void closeSock(){
	try{
	    //   srv.close();
	    clt.close();
	} catch (Exception e){
	    System.err.println("Kann die Sockets nicht schliessen!");
	}
    }

    /**
     * Sendet an StartSpieler String "error"
     */
    public void error(){
	pw.println("error.");
	try{
	    // srv.close();
	    clt.close();
	} catch (Exception e){
	    System.err.println("Kann die Sockets nicht schliessen!");
	}
    }


    public void servPorts(int servnr,int [] servprt){

	for(int i=0;i<servnr;i++)
	    pw.println(""+servprt[i]);
	pw.println(".");

    }


}
