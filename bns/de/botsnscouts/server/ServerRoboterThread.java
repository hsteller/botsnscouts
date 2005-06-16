/*******************************************************************************
 * ****************************************************************** Bots 'n'
 * Scouts - Multi-Player networked Java game * * Copyright (C) 2001 scouties. *
 * Contact botsnscouts-devel@sf.net *
 * ******************************************************************
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, in version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program, in a file called COPYING in the top directory of the Bots 'n'
 * Scouts distribution; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 ******************************************************************************/

package de.botsnscouts.server;

import org.apache.log4j.Category;

import de.botsnscouts.comm.KommException;
import de.botsnscouts.comm.KommFutschException;
import de.botsnscouts.comm.KommServerRoboter;
import de.botsnscouts.comm.MessageID;
import de.botsnscouts.comm.OtherConstants;
import de.botsnscouts.comm.ServerAntwort;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Location;

public class ServerRoboterThread extends BNSThread implements Waitable {
    static final Category CAT = Category.getInstance(ServerRoboterThread.class);

    int mode;

    private OKListener okListener;

    private InfoRequestAnswerer info;

    private ServerRobotThreadMaintainer robMaint;

    private ServerAntwort ans;

    private KommServerRoboter komm;

    protected Bot rob;

    private boolean ende;

    public ServerRoboterThread(Bot r, OKListener ok, InfoRequestAnswerer inf, ServerRobotThreadMaintainer maint,
                    KommServerRoboter k) {
        super(r.getName());
        rob = r;
        okListener = ok;
        info = inf;
        robMaint = maint;
        mode = Server.NIX;
        komm = k;
    }

    synchronized void setMode(int i) {
        mode = i;
    }

    private void notifyServer() {
        okListener.notifyDone(this);
    }

    private boolean isMoveValid(int[] regs) {
        if (regs == null) {
            CAT.error("move for robot "+rob.getName()+" was null");
            return false;
        }       
        else {
            int size = regs.length;
            boolean [] cardsReceivedSoFar = new boolean [Bot.NUM_CARDS];
            for (int i=0;i<size;i++){
                int cardNum = regs[i];
                if(cardNum<1 || cardNum>rob.cardsToGive()){
                    CAT.error(rob.getName()+" returned invalid card index:"+cardNum);
                    CAT.error("\tshould have gotten  "+rob.cardsToGive()+" cards");
                    return false;
                }
                else {
                    // case above should have taken care of size>=cardsReceivedSoFar.length
                    if (cardsReceivedSoFar[cardNum-1]) {
                        CAT.error(rob.getName()+" used a card more than once");
                        return false;
                    }
                    else {
                        cardsReceivedSoFar[cardNum-1] = true;
                    }
                }               
            }
            return true;
        }
    }
    
    public void run() {
        try {
            ende = false;
            CAT.debug ("SERVERROBOTER THREAD STARTED");
            while ((!ende) && (!isInterrupted())) {
                try {
                    ans = komm.warte();
                    synchronized (this) {
                        d("Got a " + ans.getTyp());
                        int m = mode;
                        switch (ans.typ) {
                            case ServerAntwort.PROGRAMMIERUNG:
                                if (m != Server.PROGRAMMIERUNG) {
                                    d("RULE VIOLATION by "+rob.getName());
                                    d("\treceived programming in mode " + m + "; byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }
                                else if (!isMoveValid(ans.register)){
                                    d("RULE VIOLATION by "+rob.getName());
                                    d("\treceived an illegal programming; byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }

                                rob.setNextTurnPoweredDown(ans.ok); // PowerDown?

                                d("Apply register programming...");
                                rob.program(ans.register);
                                d("Programming applied; Bot values:");
                                rob.debug();

                                notifyServer();

                                break;

                            case ServerAntwort.AUSRICHTUNG:
                                if ((m != Server.INITAUSR) && (m != Server.ZERSTOERT_SYNC)
                                                && (m != Server.ZERSTOERT_ASYNC)) {
                                    d("RV: received direction choice in mode " + m + "; byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    break;
                                }
                                rob.setFacing(ans.wohin);
                                if (m == Server.ZERSTOERT_SYNC || m == Server.ZERSTOERT_ASYNC) {
                                    // Implizit synchronized
                                    robMaint.reEntry(this);

                                    if (m == Server.ZERSTOERT_SYNC)
                                        notifyServer();

                                }
                                else { // INITAUSRICHTUNG
                                    notifyServer();
                                }
                                break;

                            case ServerAntwort.REAKTIVIERUNG:
                                if (m != Server.POWERUP) {
                                    d("RV: received reactivation in mode " + m + "; byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    break;
                                }

                                rob.setActivated(ans.ok);

                                notifyServer();
                                break;

                            case ServerAntwort.REPARATUR:
                                if (m != Server.ENTSPERREN) {
                                    d("RV: received register repait in mode " + m + "; byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    break;
                                }

                                for (int i = 0; i < ans.register.length; i++) {
                                    int regIndex = ans.register[i] - 1;
                                    rob.unlockRegister(regIndex);
                                    robMaint.sendMsg(MessageID.REGISTER_UNLOCKED, new String[]
                                                                                             {rob.getName(),""+regIndex});
                                }
                                notifyServer();

                                break;

                            case ServerAntwort.ABMELDUNG:
                                d("Got a deregistration. Mode: " + m);
                                abgemeldet = true;
                                ende = true;
                                robMaint.deleteRob(this, MessageID.SOMEONE_QUIT);
                                notifyServer();
                                break;

                            case ServerAntwort.AENDERUNGFERTIG:
                                if ((m != Server.SPIELSTART) && (m != Server.SPIELENDE)) {
                                    d("RV:received  OK in mode " + m + " ;byebye!");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }

                                notifyServer();
                                break;

                            case ServerAntwort.GIBSPIELFELDDIM:
                                komm.sendSpielfeldDim(info.getFieldSizeX(), info.getFieldSizeY());
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
                                if ((m == Server.SPIELSTART) || (m == Server.SPIELENDE) || (m == Server.NIX)) {
                                    d("RV: habe GibRoboterPos im Modus " + m + " erhalten; und tschuess");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }
                                if (((m == Server.ZERSTOERT_SYNC) || (m == Server.ZERSTOERT_ASYNC))
                                                && (!ans.name.equals(rob.getName()))) {
                                    d("RV: habe GibRoboterPos fuer nicht-mich im Modus " + m
                                                    + " erhalten; und tschuess");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }

                                d("Gibroboterpos fuer " + ans.name + " erhalten.");

                                Location o = info.getRobPos(ans.name);
                                if (o != null)
                                    komm.sendRobpos(o);

                                else {
                                    d("RV: gibRoboterPos irgendwie unter falschen Voraussetzungen erhalten");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                }

                                break;

                            case ServerAntwort.GIBROBSTATUS:
                                if ((m == Server.SPIELSTART) || (m == Server.SPIELENDE) || (m == Server.NIX)) {
                                    d("RV: habe GibRobStatus im Modus " + m + " erhalten; und tschuess");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }
                                if (((m == Server.ZERSTOERT_SYNC) || (m == Server.ZERSTOERT_ASYNC))
                                                && (!ans.name.equals(rob.getName()))) {
                                    d("RV: habe GibRobStatus fuer nicht-mich im Modus " + m + " erhalten; und tschuess");
                                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                                    ende = true;
                                    return;
                                }

                                Bot r;
                                r = info.getRobStatus(ans.name);
                                if (r.getName().compareTo("") != 0) {
                                    komm.sendRobStatus(r);
                                }
                                else {
                                    komm.sendRobStatus();
                                }
                                break;

                            case ServerAntwort.GIBSPIELSTAND:
                                if (info.gameRunning()) {
                                    komm.spielstand(true, info.getStanding());
                                }
                                else
                                    komm.spielstand(false, info.getStanding());
                                break;

                            case ServerAntwort.GIBAUSWERTUNGSSTATUS:
                                komm.spielStatus(info.getEvalStatus());
                                break;

                            case ServerAntwort.GIBTIMEOUT:
                                komm.sendTimeOut(robMaint.getTurnTimeout() / 1000);
                                break;
                            case ServerAntwort.GIBFARBEN:
                                komm.sendFarben(info.getNamesByColor());
                                break;
                            case ServerAntwort.IS_SCOUT_ALLOWED: {
                                komm.sendBoolean(info.isScoutAllowed());
                                break;
                            }
                            case ServerAntwort.IS_WISENHEIMER_ALLOWED: {
                                komm.sendBoolean(info.isWisenheimerAllowed());
                                break;
                            }
                            case ServerAntwort.CAN_PUSHERS_PUSH_MORE_THAN_ONE_BOT: {
                                komm.sendBoolean(info.arePushersPushingMultipleBots());
                                break;
                            }
                            case ServerAntwort.MESSAGE:
                                String[] tmp = new String[ans.msg.length - 1];
                                for (int k = 1; k < ans.msg.length; k++)
                                    tmp[k - 1] = ans.msg[k];
                                robMaint.sendMsg(ans.msg[0], tmp);
                                break;
                        } //switch
                    } //synchronized this
                } //try
                catch (KommFutschException e) {
                    d("KommFutschException aufgetreten. Beende mich.");
                    ende = true;
                }
                catch (KommException e) {
                    d("RV: KommException ist aufgetreten:(" + e + ") Beende mich.");
                    robMaint.deleteRob(this, OtherConstants.REASON_RULE_VIOLATION);
                    ende = true;
                }
            } //Endlosschleife
        }
        catch (Throwable t) {
            CAT.fatal("Exception:", t);
        }        
        CAT.debug ("SERVERROBOTERTHREAD REACHED END OF RUN");
    } // run()

    private void d(String s) {
        Global.debug(this, " for " + rob.getName() + ": " + s);
    }

    // Delegation methods to comm object

    boolean abgemeldet = false;

    public boolean isThere() {
        return !abgemeldet;
    }

    synchronized void deleteMe(String reason) throws KommException {
        komm.entfernen(reason);
    }

    synchronized void startGame() throws KommException {
        komm.spielstart();
    }

    synchronized void killed() throws KommException {
        komm.zerstoert();
    }

    synchronized void makeYourMove() throws KommException {
        komm.zugabgabe(rob.getCards());
    }

    synchronized void reEntry() throws KommException {
        komm.reaktivierung();
    }

    synchronized void registerRepair(int nr) throws KommException {
        komm.regReparatur(nr);
    }
    
    
    public void doShutdown() {
        try {
            komm.entfernen(OtherConstants.REASON_SERVER_SHUTDOWN);
            komm.out.close();
            komm.in.close();
        }
        catch (Exception e){
            CAT.warn("while sending shutdown notice to Roboter", e);
        }
        ende = true;
        this.interrupt();        
    }
} // class

