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
* KommStSrv zusammen mit KommSpPr sorgt für die Netzwerkkommunikation
* zwischen StartSpieler und StartServer.
* @author  Ludmila und Leo Scharf
* @see     KommSpPr
* @see     StartSpieler
* @see     StartServer
*/
public class KommStSrv{
    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(KommStSrv.class);


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

    private final int PORTNR=23722;
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
	} catch (Exception e){CAT.error("StSrv: Kann nicht ACCEPT!");}

	try{
	    pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
	} catch (Exception e){
	    CAT.error("StSrv: Kann nicht getOutputStream!");}

	try{
	    br= new BufferedReader(new InputStreamReader(clt.getInputStream()));
	} catch (Exception e)
	    {CAT.error("StSrv: Kann nicht getInputStream!");}

	try{
	    pw.println("StartServer ist bereit.");
	    //if(br.ready())
	    fromclt=br.readLine();
	} catch (Exception e){CAT.error("StSrv: Kann nicht println/readLine!");}
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
	    CAT.error("Klientenfehler!"+e);
	}
	CAT.debug(fromclt);
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
	    CAT.error("Kann die Sockets nicht schliessen!");
	}
    }

    protected void closeSock(){
	try{
	    //   srv.close();
	    clt.close();
	} catch (Exception e){
	    CAT.error("Kann die Sockets nicht schliessen!");
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
	    CAT.error("Kann die Sockets nicht schliessen!");
	}
    }


    public void servPorts(int servnr,int [] servprt){

	for(int i=0;i<servnr;i++)
	    pw.println(""+servprt[i]);
	pw.println(".");

    }


}
