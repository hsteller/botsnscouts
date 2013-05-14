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

public interface Directions {

    // Don't change this values! Sadly, lots of code depends on them being 0,1,2,3
    /** Multi-purpose direction with the value 0 */
    public static final int NORTH = 0;

    /** Multi-purpose direction with the value 1 */
    public static final int EAST = 1;

    /** Multi-purpose direction with the value 2 */
    public static final int SOUTH = 2;

    /** Multi-purpose direction with the value 3 */
    public static final int WEST = 3;

    public static final int BOT_TURN_CLOCKWISE = 1;

    public static final int BOT_TURN_COUNTER_CLOCKWISE = 2;

    public static final int DUMMY_DIRECTION = -1;

}
