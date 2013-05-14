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

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Directions;

public class BoardBot extends Bot {

    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(BoardBot.class);

    public BoardBot() {
        super("");
    }

    public BoardBot(String name) {
        super(name);
    }

    public BoardBot(Bot r) {
        super(r);
        BoardBot rob = (BoardBot) r;
        xx = rob.xx;
        yy = rob.yy;
        aa = rob.aa;
    }

    public void fillCopy(BoardBot r) {
        super.fillCopy(r);
        r.xx = xx;
        r.yy = yy;
        r.aa = aa;
    }

    public void initFrom(BoardBot r) {
        r.fillCopy(this);
    }

    /**
     * X-coordinate of temporary, "intended" position for the bot. Only used internally by
     * SimBoard.
     */
    protected int xx;

    /**
     * Y-coordinate of temporary, "intended" position for the bot. Only used internally by
     * SimBoard.
     */
    protected int yy;

    /** Temporary facing */
    private int aa;

    private int lastRotateDirection;

    /**
     * 
     * @param newFacing SimBoard uses constants as specified in Directions class (example: Directions.NORTH)
     * @param rotateDirection SimBoard uses constants as defined in Directions (example: OtherConstants.BOT_TURN_CLOCKWISE)
     */
    protected void setTempFacing(int newFacing, int rotateDirection) {
        lastRotateDirection = rotateDirection;
        aa = newFacing;
    }

    /**
     * 
     * @return SimBoard uses constants as specified in Directions class (example: Directions.NORTH)
     */
    protected int getTempFacing() {
        return aa;
    }

    /**
     * 
     * @return SimBoard uses constants as defined in OtherConstants (example: OtherConstants.BOT_TURN_CLOCKWISE)
     */
    protected int getLastTempRotateDirection() {
        return lastRotateDirection;
    }

    /**
     * Uses the current position of the bot and its intended position (this.xx, this.yy)
     * to determinen in which direction the robot will move.
     * If there is no movement (if xx=getX() && yy ==getY())
     * de.botsncouts.util.Directions.DUMMY_DIRECTION will be returned
     * 
     * @return one of the direction constants from de.botsncouts.util.Directions;
     *         might be Directions.DUMMY_DIRECTION
     */
    protected int getIntendedMoveDirection() {
        int direction = Directions.DUMMY_DIRECTION;
        if (this.yy > this.getY()) {
            direction = Directions.NORTH;
        }
        else
            if (this.yy < this.getY()) {
                direction = Directions.SOUTH;
            }
            else
                if (this.xx < this.getX()) {
                    direction = Directions.WEST;
                }
                else
                    if (this.xx > this.getX()) {
                        direction = Directions.EAST;
                    }
        return direction;
    }

    /**
     * Increment damage by one
     * WARNING: won't lock any registers
     * */
    public void incDamage() {
        this.damage++;
    }

    /**
     * Reduces the damage (==repairs the bot).
     * WARNING: won't unlock any registers
     * NOTE: the damage will not become less than zero
     * * @param i the amount of damage points to decrease by
     */
    public void decrDamage(int i) {
        damage -= i;
        if (damage < 0) {
            damage = 0;
        }
    }

    /**
     * Sets damage to 10, the position to somewhere outside of the board
     * and the bot to virtual.
     */
    public void destroyBot() {
        this.damage = 10;
        this.setVirtual();
        this.setInvalidPos();
        this.xx = 0;
        this.yy = 0;
    }

    public void dumpZug() {
        for (int i = 0; i < move.length; i++) {
            CAT.debug("dumpZug: " + i + " " + move[i]);
        }
    }

}
