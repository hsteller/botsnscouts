package de.botsnscouts.start;

import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.io.*;
import de.botsnscouts.util.*;

class StSpListener extends Thread{
    
    public String name=null;
    public int farbe=0;
    public StartStartAnmeldung par=null;
    
    private ServerSocket srv=null;
    private  Socket clt =null;
    private  InputStream inp =null;
    private  BufferedReader br=null;
    private  OutputStream outp=null;
    private  PrintWriter pw=null;
    
    int PORTNR=8889;
    private boolean torun=true;
    private String fromclt=null;
    
    public StSpListener(StartStartAnmeldung r) {
	boolean gotit=false;
	for (int i=PORTNR;(i<PORTNR+10)&&(!gotit);i++)
	    try{
		srv = new ServerSocket(i);
		par=r;
		gotit=true;
		PORTNR=i;
	    }catch(Exception e){
		System.err.println("StSpListener: Kann ServerSocket nicht öffnen:"+e+"\nprobiert: "+i);
	    }
	
    }
    
    public void run(){
	while(torun){
	    if (listen()){
		//	par.neurob(name,farbe);
		ok();
	    } 
	    else
		error();
	}
	Global.debug(this,"Habe ende meiner run() methode erreicht");
    }
    
    
    public boolean listen(){
	try{
	    clt = srv.accept();
	    clt.setSoTimeout(30000);
	    //System.out.println("Ein Klient!");
	} catch (Exception e){System.err.println("StSpListener: Kann nicht ACCEPT!");}
	
	try{
	    pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
	} catch (Exception e){
	    System.err.println("StSpListener: Kann nicht getOutputStream!");}
	
	try{
	    br= new BufferedReader(new InputStreamReader(clt.getInputStream()));
	} catch (Exception e)
	    {System.err.println("StSpListener: Kann nicht getInputStream!");}
	
	try{
	    pw.println("StartSpielerListener ist bereit.");
	    //if(br.ready())
	    fromclt=br.readLine();
	} catch (Exception e){System.err.println("StSpListener: Kann nicht println/readLine!");}
	//System.out.println(fromclt.substring(0,4));
	Global.debug(this,"empfange "+fromclt);	
	try{
	    if(fromclt.substring(0,3).equals("NSA")){//neuerSpielerAngemeldet
		name=br.readLine(); //SpielerName
		Global.debug(this,"Neuer Spieler! "+name);//!!!!!
		fromclt=br.readLine();
		farbe=Integer.parseInt(fromclt); //Farbe als Zahl zw. 1 u. 7
		if((farbe>=0)&&(farbe<=7)){
		    par.neurob(name,farbe);
		    return true; //alles war OK
		}
		else return false;
	    }  else if(fromclt.substring(0,3).equals("SGL")){ //SpielGehtLos
		par.spGL();
		return true;
	    }  else if(fromclt.substring(0,3).equals("SZE")){ //SpielZuEnde
		par.spZE();
		torun=false;
		return true;
	    } 

	} catch (Exception e){
	    System.err.println("StSpListener: Klientenfehler!"+e);
	}
	return false; //fehler ist aufgetreten
    }
    
    
    public void ok(){
	pw.println("OK.");
	try{
	    //   srv.close();
	    clt.close();
	} catch (Exception e){
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!");
	}
    }
    
    protected void closeSock(){
	try{
	    srv.close();
	    torun=false;
	    clt.close();
	} catch (Exception e){
	    try{
		clt.close();
	    }catch (Exception ex){
		System.err.println("StSpListener: Kann die Sockets nicht schliessen!"+ex);
	    }
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!"+e);
	}
    }

    
    public void error(){
	if (pw!=null){
	    pw.println("error.");
	}
	try{
	    //   srv.close(); 
	    clt.close();
	} catch (Exception e){
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!");
	}
    }
}
