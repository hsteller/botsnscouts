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

package de.botsnscouts.board;

import de.botsnscouts.comm.MessageID;
import de.botsnscouts.comm.OtherConstants;
import de.botsnscouts.server.Server;
import de.botsnscouts.util.*;
import org.apache.log4j.Priority;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This board is also able to do phases.
 *
 * @author Dirk Materlik
 *         Id: $Id$
 */
public class SimBoard extends Board implements Directions {

    private Priority origLevel = null;

    public void setDebug(boolean b) {
        if (origLevel == null) {
            origLevel = CAT.getPriority();
        }

        if (b) {
            CAT.setPriority(origLevel);
        } else {
            CAT.setPriority(Priority.FATAL);
        }
    }

    protected Vector /* of LaserDef */ lasers;

    /**
     * TODO: the server to be notified of every change... This design sucks royally
     */
    protected Server server;

    /* The bots that moved so far. Used to notify server */
    protected boolean[] moved;

    /**
     * Contains the laser stats
     */
    private StatsList stats = null;
    private Stats curStats = null;

    private Vector msgIdsQ = new Vector();
    private Vector msgArgsQ = new Vector();


    private int seqNumber = 0;
    private static final String MSG_NUM_STRING = OtherConstants.MESSAGE_NUMBER + "=";

    private String getSeqNumberMessageString() {
        return MSG_NUM_STRING + (++seqNumber);
    }

    public Vector getLasers() {
        return lasers;
    }

    private void moved2false() {
        for (int i = 0; i < moved.length; i++) {
            moved[i] = false;
        }
    }

    private void fireBotsChanged(BoardBot[] robbis) {
        if (server == null) {
            return;
        }
        sendCollectedMsgs();
        
        int botNum = moved.length;
        int num = 0;
        for (int i = 0; i < botNum; i++) {
            if (moved[i]) {
                num++;
            }
        }
      
        if (num != 0) {  
	        String[] names = new String[num];       
	        for (int i = 0, j=0; i<botNum; i++) {
	            if (moved[i]) {
	               names[j++] = robbis[i].getName();
	            }
	        }
	        server.notifyViews(++seqNumber, names);
        }
       
        moved2false();
    }


    /**
     * Collects the messages which are sent later after NTC.
     * Only has effect if this is the server's board.
     */
    private void ausgabenMsgString(String id) {
        if (server != null) {
            String[] tmp = new String[1];
            tmp[0] = getSeqNumberMessageString();
            ausgabenMsg(id, tmp, true);
        }
    }

    public void ausgabenMsg(String id, String[] args) {
        ausgabenMsg(id, args, false);
    }

    public void ausgabenMsg(String id, String[] args, boolean argsHasSequenceNumber) {
        if (server != null) {
            msgIdsQ.add(id);
            if (argsHasSequenceNumber) {
                msgArgsQ.add(args);
            } else {
                int l = args.length;
                String[] newArgs = new String[l + 1];
                for (int i = 0; i < l; i++) {
                    newArgs[i] = args[i];
                }
                newArgs[l] = getSeqNumberMessageString();
                msgArgsQ.add(newArgs);
            }
        }
    }

    private void ausgabenMsgString(String id, String arg) {
        if (server != null) {
            String[] tmp = new String[2];
            tmp[0] = arg;
            tmp[1] = getSeqNumberMessageString();
            ausgabenMsg(id, tmp, true);
        }
    }

    private void ausgabenMsgString2(String id, String arg1, String arg2) {
        if (server != null) {
            String[] tmp = new String[3];
            tmp[0] = arg1;
            tmp[1] = arg2;
            tmp[2] = getSeqNumberMessageString();
            ausgabenMsg(id, tmp, true);
        }
    }

    private void sendCollectedMsgs() {
        if (server != null) {
            while (msgIdsQ.size() > 0) {
                server.sendMsg((String) msgIdsQ.remove(0), (String[]) msgArgsQ.remove(0));
            }
        }
    }

    /**
     * Creates the internal StatsList.
     *
     * @param sl The robots that should be managed in the StatsList
     */
    public void initStats(StatsList sl) {
        stats = sl;
    }

    /**
     * Initializes including notification support
     *
     * @param map   The game map in the network-specified string
     * @param flags The flags
     */
    public SimBoard(int sx, int sy, String map, Location[] flags, Server s) throws FormatException, FlagException {
        super(sx, sy, map, flags);
        server = s;

        curStats = new Stats("foo");

        initLaserList();
    }

    private void initLaserList() {
        lasers = new Vector();
        for (int x = 1; x <= sizeX; x++) {
            for (int y = 1; y <= sizeY; y++) {
                Wall w = nw(x, y);
                //d("x="+x+"; y="+y+";w: "+w.wandEl[0]+"|"+w.getElemSpecialNW()+(w.isExisting()?"#":"_")+w.wandEl[1]+"|"+w.getElemSpecialSE());
                if (w.isExisting() && (w.getSouthDeviceType() == Wall.TYPE_LASER)) {  // Laser nach S
                    //d("LaserSued");
                    int length = findLLength(x, y, SOUTH);
                    LaserDef neu = new LaserDef(x, y, SOUTH, w.getSouthDeviceInfo(), length);
                    lasers.addElement(neu);
                }
                w = ew(x, y);
                if (w.isExisting() && (w.getWestDeviceType() == Wall.TYPE_LASER)) {  // Laser nach W
                    //d("LaserWest");
                    int length = findLLength(x, y, WEST);
                    LaserDef neu = new LaserDef(x, y, WEST, w.getWestDeviceInfo(), length);
                    lasers.addElement(neu);
                }
                w = sw(x, y);
                if (w.isExisting() && (w.getNorthDeviceType() == Wall.TYPE_LASER)) { // Laser nach N
                    //d("LaserNord");
                    int length = findLLength(x, y, NORTH);
                    LaserDef neu = new LaserDef(x, y, NORTH, w.getNorthDeviceInfo(), length);
                    lasers.addElement(neu);
                }
                w = ww(x, y);
                if (w.isExisting() && (w.getEastDeviceType() == Wall.TYPE_LASER)) { // Laser nach O
                    //d("LaserOst");
                    int length = findLLength(x, y, EAST);
                    LaserDef neu = new LaserDef(x, y, EAST, w.getEastDeviceInfo(), length);
                    lasers.addElement(neu);
                }
            }
        }
    }

    /**
     * Finds the maximum length of a laser at (x,y) facing to facing)
     */
    protected int findLLength(int x, int y, int facing) {
        int l = 0;
        if (facing == EAST) {
            while ((x + l <= sizeX) && (!ew(x + l, y).isExisting())) {
                l++;
            }
        }
        if (facing == WEST) {
            while ((x - l > 0) && (!ww(x - l, y).isExisting())) {
                l++;
            }
        }
        if (facing == NORTH) {
            while ((y + l <= sizeY) && (!nw(x, y + l).isExisting())) {
                l++;
            }
        }
        if (facing == SOUTH) {
            while ((y - l > 0) && (!sw(x, y - l).isExisting())) {
                l++;
            }
        }
        return l + 1;
    }

    /**
     * Initialize without notification support
     */
    public SimBoard(int x, int y, String map, Location[] flags) throws FormatException, FlagException {
        this(x, y, map, flags, null);
    }

    /**
     * Board without notification support and without inital flags.
     *
     * @throws FlagException will not be thrown here.
     */
    public SimBoard(int x, int y, String map) throws FormatException, FlagException {
        this(x, y, map, new Location[0]);
    }

    /**
     * Debugging only
     */
    public void print() {
        super.print();
        p("LaserDefs:");
        p("");
        for (Enumeration e = lasers.elements(); e.hasMoreElements();) {
            LaserDef l = (LaserDef) e.nextElement();
            p("str=" + l.strength + "; facing=" + l.facing + "; x=" + l.x + "; y=" + l.y + "; length=" + l.length);
        }
    }

    /**
     * Simulates a phase with a single Bot
     */
    public void doPhase(int phase, BoardBot r) {
        doPhaseReal(phase, new BoardBot[]{r});
    }

    /**
     * Simulates a phase. If initialized with notifiaciton support,
     * notifies server of every change.
     *
     * @param phase The number of the phase to be simulated (1..5)
     * @param bots  The bots to be considered
     */
    public void doPhase(int phase, Bot[] bots) {
        BoardBot[] b = new BoardBot[bots.length];
        for (int i = 0; i < bots.length; i++) {
            b[i] = (BoardBot) bots[i];
        }
        doPhaseReal(phase, b);
    }

    private void doPhaseReal(int phase, BoardBot[] bots) {
        for (int i = 0; i < bots.length; i++) {  // potential direction to -1
            bots[i].aa = -1;
        }

        moved = new boolean[bots.length];
        moved2false();

        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswRobBew");
        doRobMoveCards(phase, bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswExprFl");
        doExpressBelts(bots);
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswFl");
        doBelts(bots);  // also 2nd move of express belts
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswPusher");
        doPushers(phase, bots);
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswRot", null);
        doRotatingGears(bots);
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        ausgabenMsgString("mAuswCrushers");
        doCrushers(phase, bots);
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_START);
        doLasers(bots);        // Board and bot lasers
        fireBotsChanged(bots);
        ausgabenMsgString(MessageID.SIGNAL_ACTION_STOP);

        doArchiveUpdate(bots);
        fireBotsChanged(bots);
        doFlaggenUpdate(bots);
        fireBotsChanged(bots);
        if (phase == 5) {
            doRepairs(bots);
            fireBotsChanged(bots);
            devirtualize(bots);
            fireBotsChanged(bots);
        }
    }

    private void doRobMoveCards(int phase, BoardBot[] robbis) {
        boolean[] mymoved = new boolean[robbis.length];
        int todo = robbis.length;
        for (int i = 0; i < robbis.length; i++) {
            if (!robbis[i].isActivated() || (robbis[i].getDamage() >= 10)) {
                todo--;
                mymoved[i] = true;
            }
        }
        while (todo > 0) {
            int highest = 0;
            int highrob = -1;
            for (int i = 0; i < robbis.length; i++) {
                if ((!mymoved[i]) && (robbis[i].getMove()[phase - 1].getprio() > highest)) {
                    highest = robbis[i].getMove()[phase - 1].getprio();
                    highrob = i;
                }
            }
            moveRob(robbis, highrob, robbis[highrob].getMove()[phase - 1].getAction());
            mymoved[highrob] = true;
            fireBotsChanged(robbis);

            todo--;
        }
    }

    private void moveRob(BoardBot[] robbis, int rob, String aktion) {
        if (aktion.equals("M1")) {
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
        } else if (aktion.equals("M2")) {
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
        } else if (aktion.equals("M3")) {
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
            moveRobOne(robbis, rob, robbis[rob].getFacing(), true);
            checkForPitVictims(robbis, false);
        } else if (aktion.equals("BU")) {  // Back Up
            moveRobOne(robbis, rob, (robbis[rob].getFacing() + 2) % 4, true);
            checkForPitVictims(robbis, false);
        } else if (aktion.equals("RL")) {  // Rotate Left
            turnBot(robbis[rob], GEAR_COUNTERCLOCKWISE);
            moved[rob] = true;
        } else if (aktion.equals("RR")) {  // Rotate Right
            turnBot(robbis[rob], GEAR_CLOCKWISE);
            moved[rob] = true;
        } else if (aktion.equals("UT")) {  // U-Turn
            robbis[rob].setFacing((robbis[rob].getFacing() + 2) % 4);
            ausgabenMsgString(MessageID.BOT_UTURN, robbis[rob].getName());
            moved[rob] = true;
        } else {
            throw new DoPhaseException("Unknown card '" + aktion + "' for Bot " + robbis[rob].getName());
        }
    }

    private boolean moveRobOne(BoardBot[] robbis, int rob, int direction, boolean pushOthers) {
        if (robbis[rob].getDamage() >= 10) {
            return false;
        }

        // first, check for Wall
        switch (direction) {
            case NORTH:
                if (nw(robbis[rob].getX(), robbis[rob].getY()).isExisting()) {
                    return (false);
                }
                break;
            case EAST:
                if (ew(robbis[rob].getX(), robbis[rob].getY()).isExisting()) {
                    return (false);
                }
                break;
            case SOUTH:
                if (sw(robbis[rob].getX(), robbis[rob].getY()).isExisting()) {
                    return (false);
                }
                break;
            case WEST:
                if (ww(robbis[rob].getX(), robbis[rob].getY()).isExisting()) {
                    return (false);
                }
                break;
        }

        if (pushOthers) {
            // second, do a "virtual" move to be able to collision-check
            int xx = robbis[rob].getX();
            int yy = robbis[rob].getY();
            switch (direction) {
                case NORTH:
                    yy++;
                    break;
                case EAST:
                    xx++;
                    break;
                case SOUTH:
                    yy--;
                    break;
                case WEST:
                    xx--;
                    break;
            }

            //third, check for collision with other robbis
            for (int i = 0; i < robbis.length; i++) {
                if ((i != rob) && (robbis[i].getX() == xx) && (robbis[i].getY() == yy) && (!robbis[i].isVirtual()) && (!robbis[rob].isVirtual())) {
                    if (!moveRobOne(robbis, i, direction, true)) {
                        return (false);
                    }
                }
            }

            //fourth, commit the change if we reach this point
            robbis[rob].setPos(xx, yy);
            //in that case, the robot has actually moved a square
            moved[rob] = true;
        } //if pushOthers
        else {
            switch (direction) {
                case NORTH:
                    robbis[rob].yy = robbis[rob].getY() + 1;
                    robbis[rob].xx = robbis[rob].getX();
                    break;
                case EAST:
                    robbis[rob].yy = robbis[rob].getY();
                    robbis[rob].xx = robbis[rob].getX() + 1;
                    break;
                case SOUTH:
                    robbis[rob].yy = robbis[rob].getY() - 1;
                    robbis[rob].xx = robbis[rob].getX();
                    break;
                case WEST:
                    robbis[rob].yy = robbis[rob].getY();
                    robbis[rob].xx = robbis[rob].getX() - 1;
                    break;
            }
        }
        ausgabenMsgString2(MessageID.BOT_MOVE, "" + robbis[rob].getName(), direction + "");
        return (true);
    }

    private void checkForPitVictims(BoardBot[] robbis, boolean xxyy) {
        for (int rob = 0; rob < robbis.length; rob++) {
            if (!xxyy) {
                if (floor(robbis[rob].getX(), robbis[rob].getY()).isPit()) {
                    if (!robbis[rob].isInPit()) {
                        ausgabenMsgString(de.botsnscouts.comm.MessageID.BOT_IN_PIT, robbis[rob].getName());
                    }
                    destroyBot(robbis[rob]);
                }
            } else if (floor(robbis[rob].xx, robbis[rob].yy).isPit()) {
                if (!robbis[rob].isInPit()) {
                    ausgabenMsgString(de.botsnscouts.comm.MessageID.BOT_IN_PIT, robbis[rob].getName());
                }
                destroyBot(robbis[rob]);
            }
        }
    }

    /**
     * Destroy a bot (invalidate position, set damage to 10)
     */
    private void destroyBot(BoardBot thorsten) {
        thorsten.setDamage(10);
        thorsten.setVirtual();
        thorsten.setInvalidPos();
        thorsten.xx = 0;
        thorsten.yy = 0;
    }

    private void turnBot(BoardBot robbi, int rotationDirection) {
        int direction = OtherConstants.BOT_TURN_CLOCKWISE;
        switch (rotationDirection) {
            case GEAR_CLOCKWISE:
                robbi.turnClockwise();
                break;
            case GEAR_COUNTERCLOCKWISE:
                robbi.turnCounterClockwise();
                direction = OtherConstants.BOT_TURN_COUNTER_CLOCKWISE;
                break;
        } // switch
        ausgabenMsgString2(MessageID.BOT_TURN, robbi.getName(), direction + "");
    }

    private void turnBotIntended(BoardBot robbi, int drehR) {
        int direction = OtherConstants.BOT_TURN_CLOCKWISE;
        switch (drehR) {
            case GEAR_CLOCKWISE:
                robbi.aa = (robbi.getFacing() + 1) % 4;
                break;
            case GEAR_COUNTERCLOCKWISE:
                direction = OtherConstants.BOT_TURN_COUNTER_CLOCKWISE;
                robbi.aa = (robbi.getFacing() - 1);
                if (robbi.aa == -1) {
                    robbi.aa = 3;
                }
                break;
        } // switch
        ausgabenMsgString2(MessageID.BOT_TURN, robbi.getName(), direction + "");
    }

    private void doExpressBelts(BoardBot[] bots) {
        initIntendedValues(bots);
        for (int i = 0; i < bots.length; i++) {
            Floor floor = floor(bots[i].getX(), bots[i].getY());
            if (floor.isExpressBelt()) {
                doBelts(bots, i, floor.getBeltInfo());
            }
        }
        checkForPitVictims(bots, true);
        doIntended(bots);
    }

    private void doBelts(BoardBot[] bots) {
        initIntendedValues(bots);
        for (int i = 0; i < bots.length; i++) {
            Floor floor = floor(bots[i].getX(), bots[i].getY());
            if (floor.isBelt()) {
                doBelts(bots, i, floor.getBeltInfo());
            }
        }
        checkForPitVictims(bots, true);
        doIntended(bots);
    }

    private void doBelts(BoardBot[] robbis, int rob, int typ) {
        switch (typ % 10) {
            case BELT_NORTH:
                if (!moveRobOne(robbis, rob, NORTH, false)) {
                    return;
                }
                break;
            case BELT_EAST:
                if (!moveRobOne(robbis, rob, EAST, false)) {
                    return;
                }
                break;
            case BELT_SOUTH:
                if (!moveRobOne(robbis, rob, SOUTH, false)) {
                    return;
                }
                break;
            case BELT_WEST:
                if (!moveRobOne(robbis, rob, WEST, false)) {
                    return;
                }
                break;
        } // switch

        Floor floor = floor(robbis[rob].xx, robbis[rob].yy);
        if (floor.isBelt()) {
            int bInfo = floor.getBeltInfo();
            if (((bInfo / 10) == 2) || ((bInfo / 10) == 5)) { // counterclockwise
                if (((bInfo % 10 + 1) % 4) == typ % 10) {
                    turnBotIntended(robbis[rob], GEAR_COUNTERCLOCKWISE);
                }
            }
            if (((bInfo / 10) == 3) || ((bInfo / 10) == 5)){ // clockwise
                if (((typ % 10 + 1) % 4) == bInfo % 10) {
                    turnBotIntended(robbis[rob], GEAR_CLOCKWISE);
                }
            }
        }
    }

    private void initIntendedValues(BoardBot[] robbis) {
        for (int i = 0; i < robbis.length; i++) {
            robbis[i].xx = robbis[i].getX();
            robbis[i].yy = robbis[i].getY();
            robbis[i].aa = robbis[i].getFacing();
        }
    }

    private void doPushers(int phase, BoardBot[] robbis) {
        initIntendedValues(robbis);
        for (int i = 0; i < robbis.length; i++) {
            if (robbis[i].getDamage() >= 10) {
                continue;
            }
            Bot r = robbis[i];
            int x = r.getX();
            int y = r.getY();
            if (nw(x, y).isSouthPusherActive(phase)) {
                moveRobOne(robbis, i, SOUTH, false);
            }
            if (sw(x, y).isNorthPusherActive(phase)) {
                moveRobOne(robbis, i, NORTH, false);
            }
            if (ew(x, y).isWestPusherActive(phase)) {
                moveRobOne(robbis, i, WEST, false);
            }
            if (ww(x, y).isEastPusherActive(phase)) {
                moveRobOne(robbis, i, EAST, false);
            }
        } //for
        checkForPitVictims(robbis, true);
        doIntended(robbis);
    }

    private void doIntended(BoardBot[] robbis) {
        boolean[] robmove = new boolean[robbis.length];
        for (int i = 0; i < robmove.length; i++) {
            robmove[i] = true;
        }
        for (int rob1 = 0; rob1 < robbis.length; rob1++) {
            for (int rob2 = rob1 + 1; rob2 < robbis.length; rob2++) {
                if ((!robbis[rob1].isVirtual()) && (!robbis[rob2].isVirtual())) {
                    if ((robbis[rob1].xx == robbis[rob2].xx) && (robbis[rob1].yy == robbis[rob2].yy)) {
                        robmove[rob1] = false;
                        robmove[rob2] = false;
                    }
                    if ((robbis[rob1].getX() == robbis[rob2].xx) && (robbis[rob1].getY() == robbis[rob2].yy) && (robbis[rob2].getX() == robbis[rob1].xx) && (robbis[rob2].getY() == robbis[rob1].yy)) {
                        robmove[rob1] = false;
                        robmove[rob2] = false;
                    }
                }
            }
        }
        for (int i = 0; i < robbis.length; i++) {
            if (robmove[i]) {
                robbis[i].setPos(robbis[i].xx, robbis[i].yy);
                if (robbis[i].aa > -1) {
                    robbis[i].setFacing(robbis[i].aa);
                    robbis[i].aa = -1;
                }
                moved[i] = true;
            }
        }
    }

    private void doRotatingGears(BoardBot[] robbis) {
        for (int i = 0; i < robbis.length; i++) {
            Floor floor = floor(robbis[i].getX(), robbis[i].getY());
            if (floor.isRotator()) {
                turnBot(robbis[i], floor.getInfo());
                moved[i] = true;
            }
        }
    }

    private void doCrushers(int phase, BoardBot[] robbis) {
        BoardBot b;
        int botcount =  robbis.length;
        for (int i = 0; i < botcount; i++) {
            b= robbis[i];
            Floor floor = floor(b.getX(), b.getY());
            if (floor.isCrusherActive(phase)) {
                ausgabenMsgString(de.botsnscouts.comm.MessageID.BOT_CRUSHED, b.getName());
                destroyBot(b);                
            }
        }
    }

    private void doLasers(BoardBot[] robbis) {
        // Board lasers
        for (Enumeration e = lasers.elements(); e.hasMoreElements();) {
            LaserDef l = (LaserDef) e.nextElement();
            int x = l.x;
            int y = l.y;
            boolean hit = false;

            aussen: for (int i = l.length; i > 0; i--) {
                for (int j = 0; j < robbis.length; j++) {
                    if ((robbis[j].getX() == x) && (robbis[j].getY() == y)) { // Hit
                        if (server != null) {
                            String[] tmp = new String[5];
                            tmp[0] = robbis[j].getName();
                            tmp[1] = "" + l.strength;
                            tmp[2] = "" + l.x;
                            tmp[3] = "" + l.y;
                            tmp[4] = "" + l.facing;
                            curStats = stats.getStats(robbis[j].getName());
                            curStats.incDamageByBoard();

                            ausgabenMsg(de.botsnscouts.comm.MessageID.BORD_LASER_SHOT, tmp);
                        }

                        if (robbis[j].isVirtual()) {
                            // Virtualle Robots are dealt damage, but don't block the laser
                            for (int s = l.strength; s > 0; s--) {
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                            }
                            moved[j] = true;
                        } else if (!hit) {
                            for (int s = l.strength; s > 0; s--) {
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                            }
                            moved[j] = true;
                            hit = true;
                        }
                    } // Hit
                }

                if (hit) {
                    break aussen;
                }

                switch (l.facing) {
                    case NORTH:
                        y++;
                        break;
                    case EAST:
                        x++;
                        break;
                    case SOUTH:
                        y--;
                        break;
                    case WEST:
                        x--;
                        break;
                } //switch
            } // for length
        } //for Enumeration

        // Bot-Laser
        aussen2: for (int rob = 0; rob < robbis.length; rob++) {
            if ((robbis[rob].isVirtual()) || (!robbis[rob].isActivated())) {
                continue aussen2;
            }
            int x = robbis[rob].getX();
            int y = robbis[rob].getY();
            switch (robbis[rob].getFacing()) {
                case EAST:
                    if (ew(x, y).isExisting()) {
                        continue aussen2;
                    }
                    x++;  // Start auf Folgefeld
                    while ((x <= sizeX) && (!ww(x, y).isExisting())) {
                        for (int j = 0; j < robbis.length; j++) {
                            if ((robbis[j].getX() == x) && (robbis[j].getY() == y) && (!robbis[j].isVirtual())) { // Treffer
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                                moved[j] = true; // Änderung erfolgt

                                ausgabenMsgString2(de.botsnscouts.comm.MessageID.BOT_LASER, robbis[rob].getName(), robbis[j].getName());
                                curStats = stats.getStats(robbis[rob].getName());
                                curStats.incHits();
                                if (robbis[j].getDamage() >= 10) {
                                    curStats.incKills();
                                }
                                curStats = stats.getStats(robbis[j].getName());
                                curStats.incDamageByRobots();


                                continue aussen2;
                            }
                        }
                        x++;
                    }
                    break;
                case WEST:
                    if (ww(x, y).isExisting()) {
                        continue aussen2;
                    }
                    x--;
                    while ((x > 0) && (!ew(x, y).isExisting())) {
                        for (int j = 0; j < robbis.length; j++) {
                            if ((robbis[j].getX() == x) && (robbis[j].getY() == y) && (!robbis[j].isVirtual())) { //Treffer
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                                moved[j] = true;

                                ausgabenMsgString2(de.botsnscouts.comm.MessageID.BOT_LASER, robbis[rob].getName(), robbis[j].getName());

                                continue aussen2;
                            }
                        }
                        x--;
                    }
                    break;
                case NORTH:
                    if (nw(x, y).isExisting()) {
                        continue aussen2;
                    }
                    y++;
                    while ((y <= sizeY) && (!sw(x, y).isExisting())) {
                        for (int j = 0; j < robbis.length; j++) {
                            if ((robbis[j].getX() == x) && (robbis[j].getY() == y) && (!robbis[j].isVirtual())) { //Treffer
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                                moved[j] = true;

                                ausgabenMsgString2(de.botsnscouts.comm.MessageID.BOT_LASER, robbis[rob].getName(), robbis[j].getName());

                                continue aussen2;
                            }
                        }
                        y++;
                    }
                    break;
                case SOUTH:
                    if (sw(x, y).isExisting()) {
                        continue aussen2;
                    }
                    y--;
                    while ((y > 0) && (!nw(x, y).isExisting())) {
                        for (int j = 0; j < robbis.length; j++) {
                            if ((robbis[j].getX() == x) && (robbis[j].getY() == y) && (!robbis[j].isVirtual())) { //Treffer
                                robbis[j].incDamage();
                                lockRegisters(robbis[j]);
                                moved[j] = true;

                                ausgabenMsgString2(de.botsnscouts.comm.MessageID.BOT_LASER, robbis[rob].getName(), robbis[j].getName());

                                continue aussen2;
                            }
                        }
                        y--;
                    }
                    break;
            } //switch
        } //for rob
    } // doLasers

    private void doArchiveUpdate(BoardBot[] robbis) {
        //d("doArchivUpdate called.");

        for (int i = 0; i < robbis.length; i++) {
            Floor floor = floor(robbis[i].getX(), robbis[i].getY());

            if (floor.isRepairing()) {
                robbis[i].setArchive(robbis[i].getPos());
                moved[i] = true;
                //d(robbis[i].getName()+" ist auf einem Reperaturfeld. Archivpos updated");
            }
            for (int j = 0; j < flags.length; j++) {
                if ((robbis[i].getX() == flags[j].getX()) && (robbis[i].getY() == flags[j].getY())) {
                    robbis[i].touchArchive();
                    moved[i] = true;
                    //d(robbis[i].getName()+" ist auf einer Flagge (R1). Archivpos updated");
                }
            }
        }
    } // doArchivUpdate

    private void doFlaggenUpdate(BoardBot[] robbis) {
        //d("doFlaggenUpdate called.");

        for (int i = 0; i < robbis.length; i++) {
            if (robbis[i].getNextFlag() == flags.length + 1) {
                continue;
            }
            if ((robbis[i].getX() == flags[robbis[i].getNextFlag() - 1].getX()) && (robbis[i].getY() == flags[robbis[i].getNextFlag() - 1].getY())) {
                robbis[i].incNextFlag();
                moved[i] = true;
                //d(robbis[i].getName()+" hat naechste Flagge erreicht.");
                ausgabenMsgString2(de.botsnscouts.comm.MessageID.FLAG_REACHED, robbis[i].getName(), "" + (robbis[i].getNextFlag() - 1));
            }

        }
    } // doCheckUpdate

    /**
     * Repair bots at end of 5th phase
     */
    private void doRepairs(BoardBot[] robbis) {
        //d("doRepairs called.");

        for (int i = 0; i < robbis.length; i++) {
            Floor floor = floor(robbis[i].getX(), robbis[i].getY());
            if (floor.isRepairing()) {
                boolean msg = robbis[i].getDamage() > 0;
                robbis[i].decrDamage(floor.getInfo());
                moved[i] = true;
                //d(robbis[i].getName()+" repariert wegen Repa-Feld.");
                if (msg) {
                    ausgabenMsgString2("mRepFeld", robbis[i].getName(), "" + floor.getInfo());
                }
            }

            for (int j = 0; j < flags.length; j++) {
                if ((robbis[i].getX() == flags[j].getX()) && (robbis[i].getY() == flags[j].getY())) {
                    boolean msg = robbis[i].getDamage() > 0;
                    robbis[i].decrDamage(1);
                    moved[i] = true;
                    //d(robbis[i].getName()+" repariert wegen Flagge.");
                    if (msg) {
                        ausgabenMsgString("mRepFlag", robbis[i].getName());
                    }
                }
            }

            if (robbis[i].getDamage() < 0) {
                robbis[i].setDamage(0);
            }
        }
    } // doRepairs


    /**
     * Devirtualize a bot if he is not destroyed and alone on a field
     */
    private void devirtualize(BoardBot[] bots) {
        boolean cont;
        for (int a = 0; a < bots.length; a++) {      // Schleife 1
            cont = false;
            if (bots[a].isVirtual()) {
                for (int b = 0; b < bots.length; b++)   // Schleife 2
                {
                    if (bots[a] != bots[b]) {
                        if ((bots[a].getX() == bots[b].getX()) && (bots[a].getY() == bots[b].getY()))  // wenn zwei verschiedene Bot auf gleicher Position
                        {
                            cont = true;   // continue aktivieren
                            break;       // dann entvirtualisieren fuer robbis[a] abbrechen
                        }
                    }
                }
                if (cont) {
                    continue;  // naechsten Bot bearbeiten
                }
                if (bots[a].getDamage() < 10) {
                    bots[a].setVirtual(false);     // wenn er nicht zerstoert ist: entvirtualisieren durchführen
                    //d("Entvirtualisiere Bot "+robbis[a].getName());
                    moved[a] = true;
                }
            } // if
        } // for Schleife 1
        if (bots.length == 1)          // Sonderfall einzelner Bot
        {
            if (bots[0].getDamage() < 10) {
                bots[0].setVirtual(false);
                //d("Entvirtualisiere einzelnen Bot "+robbis[0].getName());
                moved[0] = true;
            }
        }
    } // entvirtualisiere ende


    /**
     * Lock registers if damage sufficiently high
     */
    private void lockRegisters(BoardBot robbi) {
        //d("registerSperren called mit "+robbi.getName());

        if (robbi.getDamage() >= 10) {
            destroyBot(robbi);
            return;
        }
        if (robbi.getDamage() >= 5) {
            for (int i = 4; i >= 0; i--) {
                if (robbi.getLockedRegisters()[i] == null) {
                    if (robbi.getMove()[i] != null) {
                        robbi.lockRegister(i);
                        //d("Sperre Register "+i);
                    }
                    return;
                }
            } // for
        } // if
    } // registerSperren ende

    private static Map instances = new HashMap();

    public static SimBoard getInstance(int x, int y, String field, Location[] flags) throws FormatException, FlagException {
        if (instances.get(field) == null) {
            instances.put(field, new SimBoard(x, y, field, flags));
        }
        return (SimBoard) instances.get(field);
    }

} // spielfeldsim ende