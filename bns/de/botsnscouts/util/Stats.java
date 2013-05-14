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

package de.botsnscouts.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Category;

public class Stats implements Comparable <Stats>{
    private static final Category CAT = Category.getInstance(Stats.class);

    private String name;

    private int hits;

    private int kills;

    private int damageByBoard;

    private int damageByRobots;

    private int askedWisenheimer;

    private int sentCardsLast;

    private Vector<ActionListener> listeners = new Vector<ActionListener>();

    public Stats(String n) {
        this.name = n;
        this.hits = 0;
        this.kills = 0;
        this.damageByBoard = 0;
        this.damageByRobots = 0;
        this.askedWisenheimer = 0;
        this.sentCardsLast = 0;
    }

    public String getName() {
        return name;
    }

    public int getHits() {
        return hits;
    }

    public int getDamageByBoard() {
        return damageByBoard;
    }

    public int getDamageByRobots() {
        return damageByRobots;
    }

    public int getKills() {
        return kills;
    }

    public int getWasSlowest() {
        return sentCardsLast;
    }

    public int getAskedWisenheimer() {
        return this.askedWisenheimer;
    }

    public void setName(String n) {
        this.name = n;
        fireActionEvent();
    }

    public void setHits(int i) {
        this.hits = i;
        fireActionEvent();
    }

    public void setKills(int i) {
        this.kills = i;
        fireActionEvent();
    }

    public void setDamageByBoard(int i) {
        this.damageByBoard = i;
        fireActionEvent();
    }

    public void setDamageByRobots(int i) {
        this.damageByRobots = i;
        fireActionEvent();
    }

    public void setAskedWisenheimer(int i) {
        this.askedWisenheimer = i;
        fireActionEvent();
    }

    public void setWasSlowest(int i) {
        this.sentCardsLast = i;
        fireActionEvent();
    }

    public void incAskWisenheimer() {
        ++askedWisenheimer;
        fireActionEvent();
    }

    /** Increases this Stats number of hits by(?) one */
    public void incHits() {
        ++hits;
        fireActionEvent();
    }

    /** Increases this Stats number of kills by(?) one */
    public void incKills() {
        ++kills;
        fireActionEvent();
    }

    /** Increases this Stats number of damage got by Boardlasers by(?) one */
    public void incDamageByBoard() {
        ++damageByBoard;
        fireActionEvent();
    }

    /** Increases this Stats number of damage got by(?) robotlasers by(?) one */
    public void incDamageByRobots() {
        ++damageByRobots;
        fireActionEvent();
    }

    public void incWasSlowest() {
        ++sentCardsLast;
        fireActionEvent();
    }

    public String toSendString() {
        return (Encoder.commEncode(this.name) + "," + this.hits + "," + this.kills + "," + this.damageByBoard + ","
                        + this.damageByRobots + "," + this.askedWisenheimer + "," + sentCardsLast);
    }

    public String toString() {
        return ("Bot: " + name + "\tHits: " + hits + "\tKills: " + kills + "\tdamByBoard:" + damageByBoard
                        + "\tdamByRobot:" + damageByRobots + "\taskedWise" + askedWisenheimer + "\tsentLast:" + sentCardsLast);
    }

    /** Helper method for implementing the <code>Comparable</code> interface. */
    private boolean less(Stats s) {
        if (this.hits < s.getHits())
            return true;
        else
            if (this.hits == s.getHits()) {
                if (this.kills < s.getKills())
                    return true;
                else
                    if (this.kills == s.kills) {
                        if (this.name.toUpperCase().compareTo(s.name.toUpperCase()) < 0)
                            return true;
                        else
                            return false;
                    }
                    else
                        return false;
            }
            else
                return false;
    }

    /**
     * Implementation of the Comparable-interface; two stats-Objects can not be equal.
     * 
     * @return -1 if this is less than o 1 otherwise
     */
    @Override
    public int compareTo(Stats s) {
        if (this.less(s)) {
            return 1;
        }
        else {
            return -1;
        }

    }

    public void addActionListener(ActionListener al) {
        if (al != null && !listeners.contains(al)) {
            listeners.add(al);
            CAT.debug("registered a listener that wants to be informed about my robot's stats");
        }
    }

    public void removeActionListener(ActionListener al) {
        CAT.debug("removeActionListener was called");
        if (listeners.removeElement(al)) {
            CAT.debug("removed Listener");
        }
        else {
            CAT.debug("this listener was not registered");
        }
    }

    private void fireActionEvent() {
        CAT.debug("my robot has changed; firing actionEvent..");
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
        Iterator<ActionListener> it = listeners.iterator();
        while (it.hasNext()) {
            ActionListener l =  it.next();
            l.actionPerformed(e);
        }
    }

}
