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

package de.botsnscouts.server;

import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;

import org.apache.log4j.Category;

class ServerAusgabeThread extends BNSThread implements Waitable
{
    static final Category CAT = Category.getInstance( ServerAusgabeThread.class );

    private InfoRequestAnswerer info;
    private OKListener ok;
    private MOKListener mok;
    private ServerOutputThreadMaintainer outMaint;
    private KommServerAusgabe komm;
    private boolean ende;
    private int mode;
    //boolean fertig;
    private float version;

    synchronized void setMode(int i){
	mode=i;
    }

    synchronized void setVersion(float v){
	version=v;
    }

    public ServerAusgabeThread(KommServerAusgabe ksa, OKListener okL, MOKListener mokl, InfoRequestAnswerer info, ServerOutputThreadMaintainer m)
    {
	ok=okL;
	mok=mokl;
	this.info=info;
	outMaint=m;
	komm=ksa;
	mode=Server.KEINEFRAGEN; // sicher ist sicher
    }

    private void notifyServer(){
	ok.notifyDone(this);
    }

    public void run()
    {
	try{
            ende=false;
            ServerAntwort ans;
	    outer: while((!ende)&&(!isInterrupted())){
		try{
		    ans=komm.warte();
                    CAT.debug("77 lock auf mir");
		    synchronized(this){
			int m=mode;
			if (m==Server.KEINEFRAGEN){ // dumm gelaufen...
			    if (ans.typ!=ans.ABMELDUNG){
				d("Keine Fragen erlaubt. Ausgabe entstoepseln."+this);
				outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
				break outer;
			    } else
				return;
			}

			switch (ans.typ){
			case ServerAntwort.AENDERUNGFERTIG:
			    d("Aenderungfertig erhalten.");
			    notifyServer();
			    break;

			case ServerAntwort.GIBSPIELFELDDIM:
			    komm.sendSpielfeldDim(info.getFieldSizeX(),info.getFieldSizeY());
			    break;

			case ServerAntwort.GIBSPIELFELD:
			    komm.sendSpielfeld(info.getFieldString());
			    break;

			case ServerAntwort.GIBFAHNENPOS:
			    komm.sendFahnenpos(info.getFlags());
			    break;

			case ServerAntwort.GIBNAMEN:
			    komm.sendNamen(info.getNames());
			    break;

			case ServerAntwort.GIBROBOTERPOS:
			    Location o=info.getRobPos(ans.name);
			    if (o!=null)
				komm.sendRobpos(o);
			    else{
				outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
				ende=true;
			    }
			    break;

			case ServerAntwort.GIBROBSTATUS:
			    Bot r = info.getRobStatus(ans.name);
			    if(r != null){
				komm.sendRobStatus(r);
			    }else{
				komm.sendRobStatus();
			    }
			    break;

			case ServerAntwort.GIBSPIELSTAND:
			    if(info.gameRunning()){
				komm.spielstand(new Boolean(true), info.getStanding());
			    }else{
				komm.spielstand(new Boolean(false), info.getStanding());
				notifyServer();
			    }
			    break;

			case ServerAntwort.GIBAUSWERTUNGSSTATUS:
			    d("in GibAuswertungsStatus");
			    komm.spielStatus(info.getEvalStatus());
			    break;

			case ServerAntwort.GIBTIMEOUT:
			    komm.sendTimeOut(outMaint.getOutputTimeout()/1000);
			    break;
			case ServerAntwort.GIBFARBEN:
			    komm.sendFarben(info.getNamesByColor());
			    break;
			case ServerAntwort.STATS:
			    komm.sendStats(info.getStats());
			    break;

			case ServerAntwort.ABMELDUNG:
                            CAT.debug("received abmeldung!");
			    ende=true;
			    notifyServer();
                            mok.notifyDone(this);// otherwise the sever will deadlock
                                                 // if ABMELDUNG occurs while doing the phases
                            outMaint.deleteOutput(this, MessageID.SOMEONE_QUIT);
			    return;

			case ServerAntwort.MSG_ACK:
			    d("Msg_ack received.");
			    mok.notifyDone(this);
			    break;

			default:
			    d("Erhielt einen Typ "+ans.typ+"; ich weiss nicht was ich damit soll und entstoepsle mich.");
			    outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
			    notifyServer();
			    break;
			} // switch
		    } // synchronized modus
		} //try
		catch (KommException e){
                    CAT.error(e.getMessage(), e);
		    d("KommException ist aufgetreten. Beende mich.");
		    d("Message: "+e.getMessage());
		    outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
		    ende=true;
		}
                finally {
                  CAT.debug("?? auf mir verlassen");
                }
	    } //"Endlos"schleife
	} catch( Throwable t ) {
	    CAT.fatal("Exception:", t);
	}
	d("Ende meiner run-Methode erreicht.");
    } // run()

    private void d(String s)
    {
	Global.debug(this,s);
    }

    // comm delegation
    synchronized void notifyChange(int msgId, String[] s) throws KommException{
	komm.aenderung(msgId, s);
    }
    synchronized void deleteMe(String reason) throws KommException{
	komm.entfernen(reason);
    }
    synchronized void startGame() throws KommException{
	komm.spielstart();
    }
    synchronized void endGame() throws java.io.IOException{
	komm.in.close();
	komm.out.close();
    }
    synchronized void sendMsg(String id, String[] args){
	komm.message(id,args);
    }

}
