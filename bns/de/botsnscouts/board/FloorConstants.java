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


public interface FloorConstants {
    /** Floor: Pit */
    public static final int FL_PIT = -1;
    /** Floor: Normal */
    public static final int FL_NORMAL = 0;
    /** Floor: Repairfield */
    public static final int FL_REPAIR = 1;
    /** Floor: Rotating gear */
    public static final int FL_ROTGEAR = 10;

    /** Gears: clockwise turning */
    public static final int GEAR_CLOCKWISE = 0;
    /** Gears: counterclockwise turning */
    public static final int GEAR_COUNTERCLOCKWISE = 1;

    /* Belts: Last number stands for: */
    public static final int BELT_NORTH = 0;
    public static final int BELT_EAST = 1;
    public static final int BELT_SOUTH = 2;
    public static final int BELT_WEST = 3;

    // Belts - straight ahead
    // ========================
    /** North, Speed 1 */
    public static final int FN1 = 100;
    /** East, Speed 1 */
    public static final int FE1 = 101;
    /** South, Speed 1 */
    public static final int FS1 = 102;
    /** West, Speed 1 */
    public static final int FW1 = 103;

    /** North, speed 2 */
    public static final int FN2 = 200; //
    /** East, speed 2 */
    public static final int FE2 = 201; //
    /** North, speed 2 */
    public static final int FS2 = 202; //
    /** West, speed 2 */
    public static final int FW2 = 203; //

    // Regular turning belts
    /** North from West */
    public static final int NFW1 = 120;
    /** North from East */
    public static final int NFE1 = 130;
    /** East from North */
    public static final int EFN1 = 121;
    /** East from South */
    public static final int EFS1 = 131;
    /** South from West */
    public static final int SFW1 = 132;
    /** South from East */
    public static final int SFE1 = 122;
    /** West from North */
    public static final int WFN1 = 133;
    /** West from South */
    public static final int WFS1 = 123;
    /** North from either West or East */
    public static final int NFEW1 = 150;
    /** East from either North or South */
    public static final int EFNS1 = 151;
    /** South from West or East */
    public static final int SFWE1 = 152;
    /** West from North or South */
    public static final int WFNS1 = 153;

    // Express turning belts
    //
    public static final int NFW2 = 220;
    public static final int NFE2 = 230;
    public static final int EFN2 = 221;
    public static final int EFS2 = 231;
    public static final int SFW2 = 232;
    public static final int SFE2 = 222;
    public static final int WFN2 = 233;
    public static final int WFS2 = 223;
    public static final int NFWE2 = 250;
    public static final int EFNS2 = 251;
    public static final int SFWO2 = 252;
    public static final int WFNS2 = 253;
}