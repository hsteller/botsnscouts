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
     *  Temporary, "intended" positions for the bot. Only used internally by
     *  SimBoard.
     */
    protected int xx;
    protected int yy;
    /** Temporary facing */
    private  int aa;
    private int lastRotateDirection;
    /**
     *  
     * @param newFacing SimBoard uses constants as specified in Directions class (example: Directions.NORTH) 
     * @param rotateDirection SimBoard uses constants as defined in Directions (example: OtherConstants.BOT_TURN_CLOCKWISE) 
     */
    protected void setTempFacing(int newFacing, int rotateDirection){
        lastRotateDirection = rotateDirection;
        aa = newFacing;
    }
    /**
     * 
     * @return SimBoard uses constants as specified in Directions class (example: Directions.NORTH) 
     */
    protected int getTempFacing(){
        return aa;
    }
    
    
    /**
     * 
     * @return SimBoard uses constants as defined in OtherConstants (example: OtherConstants.BOT_TURN_CLOCKWISE)
     */
    protected int getLastTempRotateDirection(){
        return lastRotateDirection;
    }
    
    

    public void dumpZug() {
        for (int i = 0; i < move.length; i++) {
            CAT.debug("dumpZug: " + i + " " + move[i]);
        }
    }

}
