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

/**
 * A position on the board.
 * 
 * @author Hendrik<BR>
 */
public class Location {
    /** @todo SHOULD BE PRIVATE -> REASON: HASHFUNCTION.. */
    public int x;

    public int y;

    public Location() {
    }

    public Location(int a, int b) {
        x = a;
        y = b;
        calcHash();
    }

    public Location(Location o) {
        new Location(o.x, o.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
        calcHash();
    }

    public void set(Location o) {
        this.x = o.x;
        this.y = o.y;
        calcHash();
    }

    public boolean equals(Object o) {
        return equals((Location) o);
    }

    public boolean equals(Location o) {
        return (this.x == o.x) && (this.y == o.y);
    }

    int hash = 0;

    private void calcHash() {
        hash = 100 * x + y;
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        String back = "(" + x + ", " + y + ")";
        return back;
    }
}
