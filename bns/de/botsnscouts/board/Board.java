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

/**
 * The map of the game
 * @author Dirk Materlik
 * Id: $Id$
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.ParseUtils;

public class Board implements de.botsnscouts.util.Directions, FloorConstants {

    /* not static on purpose, need to inherit it */
    org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Board.class);
    static org.apache.log4j.Category sCAT = org.apache.log4j.Category.getInstance(Board.class);

    /** Preview-Image is possibly saved along with the tile */
    protected java.awt.Image img;

    /** Die Spielfeldgroesse */
    protected int sizeX,sizeY;

    /** Die Floortypen
     *  2-dimensional   1. x-Koordinate
     *                  2. y-Koordinate
     */
    private Floor[][] floor;

    private Wall[][] vWall;    // vertikal walls
    protected Wall[][] hWall;    // horizontal walls

    protected Location[] flags;
    protected String flagErrors;

    /** Sicherungskopie des Spielfeldstrings */
    protected String boardAsString;

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    /***** Construktors *****/

    /** Initialisiert ein Board aus zweien. Zerst�rt unter Umst�nden Teile der hereingegebenen
     Spielfelder!
     */
//    // UNGETESTET!!!
//    public Board(Board s1, Board s2, boolean nebeneinander) throws FormatException{
//      CAT.debug("new Board called");
//	if (nebeneinander){
//	    sizeX=s1.sizeX+s2.sizeX;
//	    sizeY=s1.sizeY;
//	    if (sizeY!=s2.sizeY)
//		throw new FormatException("Die beiden Spielfelder sind nicht gleich hoch!");
//
//	    initArys();
//
//	    // Erstes reinkopieren
//	    for (int x=1;x<=s1.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    floor[x][y]=s1.floor[x][y];
//	    for (int x=0;x<=s1.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][y]=s1.vWall[x][y];
//	    for (int x=0;x<s1.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][y]=s1.hWall[x][y];
//
//	    // Zweites reinkopieren
//	    for (int x=1;x<=s2.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    floor[x+s1.sizeX][y]=s2.floor[x][y];
//	    for (int x=0;x<=s2.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x+s1.sizeX][y]=s2.vWall[x][y];
//	    for (int x=0;x<s2.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x+s1.sizeX][y]=s2.hWall[x][y];
//
//	    // Konflikte resolven
//	    for (int y=0;y<sizeY;y++){
//                Wall w = vWall[s1.sizeX][y];
// //		w.setExisting( s1.vWall[s1.sizeX][y].isExisting()||s2.vWall[0][y].isExisting() );
// //              w.copyElementNW( s1.vWall[s1.sizeX][y] );
//                // wall on border between boards:
//                if( s1.getVWall(s1.sizeX,y).isExisting() ||s2.getVWall(0,y).isExisting() ) {
//                    vWall[s1.sizeX][y] = w.getWithElementNW( s1.getVWall(s1.sizeX, y) );
// //                  w.copyElementNW( s1.vWall[s1.sizeX][y] );
//                }
//	    }
//
//	}else{ //untereinander
//	    sizeX=s1.sizeX;
//	    sizeY=s1.sizeY+s2.sizeY;
//	    if (sizeX!=s2.sizeX)
//		throw new FormatException("Die beiden Spielfelder sind nicht gleich breit!");
//
//	    initArys();
//
//	    // Erstes reinkopieren
//	    for (int x=1;x<=s1.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    floor[x][s2.sizeY+y]=s1.floor[x][y];
//	    for (int x=0;x<=s1.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][s2.sizeY+y]=s1.vWall[x][y];
//	    for (int x=0;x<s1.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][s2.sizeY+y]=s1.hWall[x][y];
//
//	    // Zweites reinkopieren
//	    for (int x=1;x<=s2.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    floor[x][y]=s2.floor[x][y];
//	    for (int x=0;x<=s2.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][y]=s2.vWall[x][y];
//	    for (int x=0;x<s2.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][y]=s2.hWall[x][y];
//
//	    // Konflikte resolven
//	    for (int x=0;x<sizeX;x++){
//                if ( s1.vWall[x][0].isExisting()||s2.vWall[x][s2.sizeY].isExisting() ) {
// //                 vWall[x][s2.sizeY] =
//                }
// //		hWall[x][s2.sizeY].setExisting( s1.vWall[x][0].isExisting()||s2.vWall[x][s2.sizeY].isExisting() );
// //		hWall[x][s2.sizeY].copyElementNW( s1.vWall[x][0] );
//	    }
//	}
//
//	SpielfeldString = getComputedString();
//    }

    public Board(int x, int y, String map, Location[] flags) throws FormatException, FlagException {
        CAT.debug("new Board called");
        sizeX = x;
        sizeY = y;
        boardAsString = map;

        initArys();

        int strpos = 0;            // Current pos in the String

        for (int zeile = sizeY; zeile > 0; zeile--) {
            //parse ZwischenReihe (Nordwaende)
            for (int spalte = 0; spalte < sizeX; spalte++) {
                strpos = parseAndCreateWall(strpos, map, hWall, spalte, zeile);
            }

            strpos = ParseUtils.assertws(map, strpos);
            // one wall, then repeatedly floor & wall
            strpos = parseAndCreateWall(strpos, map, vWall, 0, zeile - 1);
            for (int spalte = 1; spalte <= sizeX; spalte++) {
                strpos = parseAndCreateFloor(strpos, map, floor, spalte, zeile);
                strpos = parseAndCreateWall(strpos, map, vWall, spalte, zeile - 1);
            }
            strpos = ParseUtils.assertws(map, strpos);
        }    // for zeile
        // parse last row of walls
        for (int spalte = 0; spalte < sizeX; spalte++) {
            strpos = parseAndCreateWall(strpos, map, hWall, spalte, 0);
        }

        checkFlaggen(flags);
        this.flags = flags;
    } //Konstruktor

    private void initArys() {
        // initialize arrays
        floor = new Floor[sizeX + 2][sizeY + 2];
        vWall = new Wall[sizeX + 1][sizeY];
        hWall = new Wall[sizeX][sizeY + 1];
        for (int i = 0; i <= sizeX + 1; i++) {
            floor[i][0] = Floor.getPit();
            floor[i][sizeY + 1] = Floor.getPit();
        }
        for (int j = 1; j <= sizeY; j++) {
            floor[0][j] = Floor.getPit();
            floor[sizeX + 1][j] = Floor.getPit();
        }
    }

    protected void checkFlaggen(Location[] f) throws FlagException {
        // prueft ob Flaggen regelkonform plaziert sind (sonst Exception)
        // und ob sie "gut" sind - sonst kann man die Probleme mit
        // getFlaggenProbleme() abfragen

        if (f == null) {
            flagErrors = Message.say("Board", "mFlagProbNoFlagSet");
            return;
        }

        for (int i = 0; i < f.length; i++) {
            if ((f[i].x > sizeX) || (f[i].y > sizeY))
                throw new FlagException(Message.say("Board", "eFlagNotInField", (i + 1)));
            if (floor(f[i].x, f[i].y).isPit())
                throw new FlagException(Message.say("Board", "eFlagOnHole", (i + 1)));
        }

        flagErrors = "";
        for (int i = 0; i < f.length; i++) {
            int anzwand = 0;
            if (nw(f[i].x, f[i].y).isExisting())
                anzwand++;
            if (ew(f[i].x, f[i].y).isExisting())
                anzwand++;
            if (sw(f[i].x, f[i].y).isExisting())
                anzwand++;
            if (ww(f[i].x, f[i].y).isExisting())
                anzwand++;

            if (anzwand > 2) {
                //Ludmila:String so ge�ndert, da� keine Nummer angezeigt wird
                // flaggenProbleme+=Message.say("Board","mFlagProbManyWalls",(i+1),anzwand); //original
                flagErrors += Message.say("Board", "mFlagProbManyWalls", anzwand);//ge�ndert
            }

            if (floor(f[i].x, f[i].y).isBelt()) { //Fliessband
                //flaggenProbleme+=Message.say("Board","mFlagProbConvBelt",(i+1));//original
                flagErrors += Message.say("Board", "mFlagProbConvBelt");//gfe�ndert
            }
        }
    }

    public String getFlaggenProbleme() {
        return flagErrors;
    }

    /** Deep Magic, rotates LEFT */
    public String getTurnedLeft90Degrees() {
        CAT.debug("get90GradGedreht() called");
        StringBuffer s = new StringBuffer();
        for (int x = sizeX; x > 0; x--) {
            for (int y = sizeY; y > 0; y--) {
                // "obere" ZwischenReihe
                ew(x, y).writeReversed(s);
            }
            s.append('\n');
            nw(x, sizeX).write(s);
            for (int y = sizeY; y > 0; y--) {
                //Floor
                floor(x, y).write(s, true);
                sw(x, y).write(s);
            }
            s.append("\n");
        } //for x
        // unterste ZwischenReihe
        for (int y = sizeY; y > 0; y--)
            ww(1, y).writeReversed(s);
        s.append("\n.\n");
        return new String(s);
    }

/* Well, and I hoped I'd never have to do this one -right- :-) */
    public String getComputedString() {
        CAT.debug("getComputedString called");
        StringBuffer s = new StringBuffer();
        for (int y = sizeY; y > 0; y--) {
            for (int x = 1; x <= sizeX; x++) {
                // obere ZwischenReihe
                nw(x, y).write(s);
            }
            s.append("\n");
            ww(1, y).write(s);
            for (int x = 1; x <= sizeX; x++) {
                //Floor
                floor(x, y).write(s, false);
                ew(x, y).write(s);
            }
            s.append("\n");
        } //for y
        // unterste ZwischenReihe
        for (int x = 1; x <= sizeX; x++) {
            sw(x, 1).write(s);
        }
        s.append("\n.\n");
        return new String(s);
    }

    private int parseAndCreateWall(int strpos, String kacheln, Wall[][] walls, int a, int b)
            throws FormatException {
        String wallString = Wall.extractWallDef(strpos, kacheln);
        walls[a][b] = Wall.getWall(wallString);
        return strpos + wallString.length();
    }

    private int parseAndCreateFloor(int strpos, String kacheln, Floor[][] somefloor, int a, int b)
            throws FormatException {
        String floorString = Floor.extractFloorDef(strpos, kacheln);
        somefloor[a][b] = Floor.getFloor(floorString);
        return strpos + floorString.length();
    }

    public Location[] getFlags() {
        return flags;
    }

    public String getBoardAsString() {
        return boardAsString;
    }

    /***** protected Methods *****/

    protected final static void p(String s) {
        sCAT.debug(s);
    }

    protected final static void pn(String s) {
        sCAT.debug(s);
    }

    public boolean hasNorthWall(int x, int y) {
        return nw(x, y).isExisting();
    }

    public boolean hasSouthWall(int x, int y) {
        return sw(x, y).isExisting();
    }

    public boolean hasWestWall(int x, int y) {
        return ww(x, y).isExisting();
    }

    public boolean hasEastWall(int x, int y) {
        return ew(x, y).isExisting();
    }

    /* Floortype */
    public Floor floor(int x, int y) {
        return (floor[x][y]);
    }

    /* North wall */
    public Wall nw(int x, int y) {
        return (hWall[x - 1][y]);
    }

    /* East wall */
    public Wall ew(int x, int y) {
        return (vWall[x][y - 1]);
    }


    /* South wall */
    public Wall sw(int x, int y) {
        return (hWall[x - 1][y - 1]);
    }

    /* West wall */
    public Wall ww(int x, int y) {
        return (vWall[x - 1][y - 1]);
    }


    public Wall getVWall(int a, int b) {
        return vWall[a][b];
    }

    public Wall getHWall(int a, int b) {
        return hWall[a][b];
    }

    public void setVWall(int a, int b, Wall wand) {
        vWall[a][b] = wand;
    }

    public void setHWall(int a, int b, Wall wand) {
        hWall[a][b] = wand;
    }

    public Floor getFloor(int a, int b) {
        return floor[a][b];
    }

    public void setFloor(int a, int b, Floor aFloor) {
        CAT.debug("Setting new floor elemnt to ("+a+","+b+")");
        floor[a][b] = aFloor;
    }

    /** for debugging purposes */
    public void print() {
        p("Floor:");
        p("X=" + floor.length + "; Y=" + floor[0].length);
        p("");
        pn("\t");
        for (int x = 0; x <= sizeX + 1; x++)
            pn(x + "\t");
        p("");
        for (int y = sizeY + 1; y >= 0; y--) {
            pn(y + "\t");
            for (int x = 0; x <= sizeX + 1; x++) {
                if (floor[x][y] == null)
                    pn("null\t");
                else
                    pn(floor[x][y].getType() + "|" + floor[x][y].getInfo() + "\t");
            }
            p("");
        }

        p("");
        p("vertikale W�nde:");
        p("X=" + vWall.length + "; Y=" + vWall[0].length);
        p("");
        pn("\t");
        for (int x = 0; x <= sizeX; x++)
            pn(x + "\t");
        p("");
        for (int y = sizeY - 1; y >= 0; y--) {
            pn(y + "\t");
            for (int x = 0; x <= sizeX; x++) {
                Wall wall = vWall[x][y];
                if (wall == null)
                    pn("null\t");
                else if (wall.isExisting()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(wall.getNWDeviceType()).append('|').append(wall.getNWDeviceInfo());
                    sb.append('#').append(wall.getSEDeviceType()).append('|');
                    sb.append(wall.getSEDeviceInfo()).append('\t');
                    pn(sb.toString());
                } else
                    pn(".\t");
            }
            p("");
        }

        p("");
        p("horizontale W�nde:");
        p("X=" + hWall.length + "; Y=" + hWall[0].length);
        p("");
        pn("\t");
        for (int x = 0; x < sizeX; x++)
            pn(x + "\t");
        p("");
        for (int y = sizeY; y >= 0; y--) {
            pn(y + "\t");
            for (int x = 0; x < sizeX; x++) {
                Wall wall = hWall[x][y];
                if (wall == null)
                    pn("null\t");
                else if (wall.isExisting()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(wall.getNWDeviceType()).append('|').append(wall.getNWDeviceInfo());
                    sb.append('#').append(wall.getSEDeviceType()).append('|');
                    sb.append(wall.getSEDeviceInfo()).append('\t');
                    pn(sb.toString());
                } else
                    pn(".\t");
            }
            p("");
        }
    }

    public static String readMagicString(File file) throws IOException {
        BufferedReader kachReader = new BufferedReader(new InputStreamReader(
                                                       new FileInputStream(file)));
        StringBuffer str = new StringBuffer();
        String tmp = null;
        //read board:
        while ((tmp = kachReader.readLine()) != null)
            str.append(tmp + "\n");
        return new String(str);
    }

}









