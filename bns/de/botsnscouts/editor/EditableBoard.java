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

package de.botsnscouts.editor;

import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.Floor;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.board.Wall;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Location;

class EditableBoard extends SimBoard {
    EditableBoard(int x, int y, String kacheln, Location[] flaggen) throws FormatException, FlagException {
        super(x, y, kacheln, flaggen);
    }

    void setNorthPusher(int x, int y, int phases) {
        setHWall(x, y, getHWall(x, y).getWithNWPusher(phases));
    }

    void setWestPusher(int x, int y, int phases) {
        setVWall(x, y, getVWall(x, y).getWithNWPusher(phases));
    }

    void setSouthPusher(int x, int y, int phases) {
        setHWall(x, y, getHWall(x, y).getWithSEPusher(phases));
    }

    void setEastPusher(int x, int y, int phases) {
        setVWall(x, y, getVWall(x, y).getWithSEPusher(phases));
    }

    void setNorthLaser(int x, int y, int strength) {
        setHWall(x, y, getHWall(x, y).getWithNWLaser(strength));
    }

    void setWestLaser(int x, int y, int strength) {
        setVWall(x, y, getVWall(x, y).getWithNWLaser(strength));
    }

    void setSouthLaser(int x, int y, int strength) {
        setHWall(x, y, getHWall(x, y).getWithSELaser(strength));
    }

    void setEastLaser(int x, int y, int strength) {
        setVWall(x, y, getVWall(x, y).getWithSELaser(strength));
    }

    void removeVWall(int x, int y) {
        setVWall(x, y, Wall.getNonExistingWall());
    }

    void removeHWall(int x, int y) {
        setHWall(x, y, Wall.getNonExistingWall());
    }

    void addVWall(int x, int y) {
        setVWall(x, y, Wall.getEmptyWall());
    }

    void addHWall(int x, int y) {
        setHWall(x, y, Wall.getEmptyWall());
    }

    // Floor-Stuff
    void clearFloor(int x, int y) {
        setFloor(x, y, Floor.getEmptyFloor());
    }

    void setFloor(int x, int y, int type, int info) {
        setFloor(x, y, Floor.getFloor(type, info));
    }

    void setCrusher(int x, int y, int phases) {
        setFloor(x, y, getFloor(x, y).getWithCrusher(phases));
    }

    public void setFlags(Location[] flags) {
        this.flags = flags;
    }

}
