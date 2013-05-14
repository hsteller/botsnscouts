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

import org.apache.log4j.Category;

import de.botsnscouts.comm.KommException;
import de.botsnscouts.comm.KommServerAusgabe;
import de.botsnscouts.comm.MessageID;
import de.botsnscouts.comm.OtherConstants;
import de.botsnscouts.comm.ServerAntwort;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Location;

class ServerAusgabeThread extends BNSThread implements Waitable {

    static final Category CAT = Category.getInstance(ServerAusgabeThread.class);

    private InfoRequestAnswerer info;

    private OKListener ok;

    private MOKListener mok;

    private ServerOutputThreadMaintainer outMaint;

    private KommServerAusgabe komm;

    private boolean ende;

    private int mode;

    private boolean clientHasDeregistered = false;

    @SuppressWarnings("unused")
    private float version;

    synchronized void setMode(int i) {
        mode = i;
    }

    synchronized void setVersion(float v) {
        version = v;
    }

    public ServerAusgabeThread(KommServerAusgabe ksa, OKListener okL, MOKListener mokl, InfoRequestAnswerer info,
                    ServerOutputThreadMaintainer m) {
        super(ksa.getName());
        ok = okL;
        mok = mokl;
        this.info = info;
        outMaint = m;
        komm = ksa;
        mode = ModusConstants.KEINEFRAGEN; // sicher ist sicher
    }

    private void notifyServer() {
        ok.notifyDone(this);
        CAT.debug("leaving notify server");
    }

    private void processReceivedMessage(ServerAntwort ans) throws DeleteOutputTriggerToAvoidDeadlock, KommException {
        switch (ans.typ) {
            case ServerAntwort.AENDERUNGFERTIG: {
                d("Aenderungfertig erhalten.");
                notifyServer();
                break;
            }
            case ServerAntwort.GIBSPIELFELDDIM: {
                komm.sendSpielfeldDim(info.getFieldSizeX(), info.getFieldSizeY());
                break;
            }
            case ServerAntwort.GIBSPIELFELD: {
                komm.sendSpielfeld(info.getFieldString());
                break;
            }
            case ServerAntwort.GIBFAHNENPOS: {
                komm.sendFahnenpos(info.getFlags());
                break;
            }
            case ServerAntwort.GIBNAMEN: {
                komm.sendNamen(info.getNames());
                break;
            }
            case ServerAntwort.GIBROBOTERPOS: {
                Location o = info.getRobPos(ans.name);
                if (o != null)
                    komm.sendRobpos(o);
                else {
                    // outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
                    throw new DeleteOutputTriggerToAvoidDeadlock(OtherConstants.REASON_RULE_VIOLATION);
                }
                break;
            }
            case ServerAntwort.GIBROBSTATUS: {
                Bot r = info.getRobStatus(ans.name);
                if (r != null) {
                    komm.sendRobStatus(r);
                }
                else {
                    komm.sendRobStatus();
                }
                break;
            }
            case ServerAntwort.GIBSPIELSTAND: {
                if (info.gameRunning()) {
                    komm.spielstand(true, info.getStanding());
                }
                else {
                    komm.spielstand(false, info.getStanding());
                    notifyServer();
                }
                break;
            }
            case ServerAntwort.GIBAUSWERTUNGSSTATUS: {
                komm.spielStatus(info.getEvalStatus());
                break;
            }
            case ServerAntwort.GIBTIMEOUT: {
                komm.sendTimeOut(outMaint.getOutputTimeout() / 1000);
                break;
            }
            case ServerAntwort.GIBFARBEN: {
                komm.sendFarben(info.getNamesByColor());
                break;
            }
            case ServerAntwort.STATS: {
                komm.sendStats(info.getStats());
                break;
            }
            case ServerAntwort.CAN_PUSHERS_PUSH_MORE_THAN_ONE_BOT: {
                komm.sendBoolean(info.arePushersPushingMultipleBots());
                break;
            }
            case ServerAntwort.IS_SCOUT_ALLOWED: {
                komm.sendBoolean(info.isScoutAllowed());
                break;
            }
            case ServerAntwort.IS_WISENHEIMER_ALLOWED: {
                komm.sendBoolean(info.isWisenheimerAllowed());
                break;
            }

            case ServerAntwort.ABMELDUNG: {
                CAT.debug("received abmeldung!");
                ende = true;
                clientHasDeregistered = true;
                // outMaint.deleteOutput(this, MessageID.SOMEONE_QUIT);
                throw new DeleteOutputTriggerToAvoidDeadlock(MessageID.SOMEONE_QUIT);
                // CAT.debug("called deleteOutput");

            }
            case ServerAntwort.MSG_ACK: {
                d("Msg_ack received.");
                mok.notifyDone(this);
                break;
            }
            default: {
                d("Erhielt einen Typ " + ans.typ + "; ich weiss nicht was ich damit soll und entstoepsle mich.");
                outMaint.deleteOutput(this, OtherConstants.REASON_RULE_VIOLATION);
                notifyServer();
                break;
            }
        } // switch
    }

    public void run() {
        CAT.debug("SERVERAUSGABETHREAD STARTED");
        try {
            ende = false;
            ServerAntwort ans;
            while (!(ende || isInterrupted())) {
                try {
                    ans = komm.warte();
                    CAT.debug("LOCK on me: " + this);
                    synchronized (this) {
                        if (mode == ModusConstants.KEINEFRAGEN) { // dumm gelaufen...
                            if (ans.typ != ServerAntwort.ABMELDUNG) {
                                CAT.debug("no requests allowed at this time; killing output" + this);
                                // outMaint.deleteOutput(this,OtherConstants.REASON_RULE_VIOLATION);
                                // break outer;
                                throw new DeleteOutputTriggerToAvoidDeadlock(OtherConstants.REASON_RULE_VIOLATION);
                            }
                            else {
                                return;
                            }
                        }
                        else {
                            processReceivedMessage(ans);
                        }
                    } // synchronized modus
                } // try
                catch (KommException e) {
                    CAT.error(e.getMessage(), e);
                    ende = true;
                    notifyServer();
                    CAT.debug("notify done");
                    mok.notifyDone(this);
                    CAT.debug("MSG notify done");
                    outMaint.deleteOutput(this, OtherConstants.REASON_RULE_VIOLATION);
                }
                catch (DeleteOutputTriggerToAvoidDeadlock x) {
                    String reason = x.getMessage();
                    ende = true;
                    clientHasDeregistered = true;
                    notifyServer();
                    CAT.debug("notify done");
                    mok.notifyDone(this);
                    CAT.debug("MSG notify done");
                    outMaint.deleteOutput(this, reason);
                }
                finally {
                    CAT.debug("RELEASE LOCK on me:" + this);
                }
            } // "infinite" loop
        }
        catch (Throwable t) {
            CAT.fatal("Exception:", t);
        }
        CAT.debug("SERVER AUSGABETHREAD REACHED END OF RUN");
    } // run()

    private void d(String s) {
        Global.debug(this, s);
    }

    // comm delegation
    synchronized void notifyChange(int msgId, String[] s) throws KommException {
        if (!clientHasDeregistered) {
            CAT.debug("sending NTC");
            komm.aenderung(msgId, s);
            CAT.debug("NTC sent");
        }
        else {
            notifyServer();
        }
    }

    synchronized void deleteMe(String reason) throws KommException {
        CAT.debug("deleteMe:" + reason);
        komm.entfernen(reason);
        CAT.debug("leaving deleteMe");
    }

    synchronized void startGame() throws KommException {
        CAT.debug("sending gameStart");
        komm.spielstart();
        CAT.debug("gameStart sent");
    }

    synchronized void endGame() throws java.io.IOException {
        CAT.debug("closing 'in'");
        komm.in.close();
        CAT.debug("closing 'out'");
        komm.out.close();
        CAT.debug("comm channels closed");
    }

    synchronized void sendMsg(String id, String[] args) {
        CAT.debug("send MSG");
        if (!clientHasDeregistered) {
            komm.message(id, args);
        }
        else {
            mok.notifyDone(this);
        }
        CAT.debug("sent MSG");
    }

    public void doShutdown() {
        try {
            komm.entfernen(OtherConstants.REASON_SERVER_SHUTDOWN);
            komm.out.close();
            komm.in.close();
        }
        catch (Exception e) {
            CAT.warn("while sending shutdown notice to Output channel", e);
        }
        ende = true;
        this.interrupt();
    }

    /**
     * The thing is, this Thread MUST NOT call deleteOutput while it holds its own lock - which it does most of the time => if we receive a
     * deregistration message, we will throw and catch this exception to leave the "synchronized(this)"
     * 
     * Reason: deleteOutput() requires the lock of the Server's "ausgabeThread"-Vector which is very likely owned by "MessageThread" during phase
     * evaluation; and MessageThread will want to call our synchronized "sendMsg()"-method one or more times
     * before releasing the lock.. => DEADLOCK
     * 
     */
    @SuppressWarnings("serial")
    class DeleteOutputTriggerToAvoidDeadlock extends Exception {

        public DeleteOutputTriggerToAvoidDeadlock(String reason) {
            super(reason);
        }
    }
}
