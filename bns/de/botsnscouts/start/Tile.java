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

package de.botsnscouts.start;

import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Location;

/** Saves image and name of tiles and
 *  gives rotated image.
 */
public class Tile extends SimBoard {

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Tile.class);

    private int thumbnailsize;

    String kName;
    int rotat;

    //create new tile
    public Tile(String name, String field, int thumbnailsize) throws FormatException, FlagException {
        super(12, 12, field, null);
        this.thumbnailsize = thumbnailsize;
        kName = name;
        rotat = 0;
    }

    //create rotated tile
    public Tile(String name, String field, int rot, int thumbnailsize) throws FormatException, FlagException {
        this(name, field, thumbnailsize);
        rotat = rot;
    }

/*public Image getImage(){
	if (img==null){
	    CAT.debug("creating image on-demand.");
	    img=BoardView.createThumb(this,thumbnailsize);
	}
	return img;
	}*/

    public String getName() {
        return kName;
    }

    public int getRotation() {
        return rotat;
    }

    //gibt um 90° gedrehtes Clone
    public Tile getRotated() {
        String gedrTile = getTurnedLeft90Degrees();
        Tile drTile = null;
        try {
            drTile = new Tile(kName, gedrTile, (rotat + 1) % 4, thumbnailsize);
        } catch (FlagException e) {
            System.err.println(e);
        } catch (FormatException e) {
            System.err.println(e);
        }
        return drTile;
    }

    /**
     *  Check whether flag locations are valid.
     * @param fl flag locations to be checked
     * @return
     */
    public boolean areFlagPositionsValid(Location[] fl) {
        try {
            // enno: habe checkflaggen protected gemacht
            checkFlaggen(fl);
        } catch (FlagException e) {
            return false;
        }
        return true;
    }

}
