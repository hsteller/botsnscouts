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
import de.botsnscouts.server.Server;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Location;

import java.awt.*;
import java.util.Properties;

// Diese Klasse dient der Entkopplung der GUI-Schicht (StartSpieler)
// von der Fachkonzeptschicht(...) und der Datenhaltungsschicht (...)

public class Facade {

    private int thumbnailsize;
    private TileRaster tileRaster, tileRasterSave;
    private Launcher launcher;
    private TileFactory tileFactory;

    private GameOptions gameOptions;

    public Facade() {
        this(180);
    }

    public Facade(int thumbnailsize) {
        this.thumbnailsize = thumbnailsize;
        tileFactory = new TileFactory(thumbnailsize);
        tileRaster = new TileRaster(tileFactory);
        launcher = new Launcher();
    }

    public int getThumbnailSize() {
        return thumbnailsize;
    }

    public void setBoardDim(int x, int y) {
        tileRaster.setBoardDim(x, y);
    }

    public Location getBoardDim() {
        return tileRaster.getBoardDim();
    }

    public void setTile(int x, int y, int rot, String tile) throws FlagPresentException {
        tileRaster.setTile(x, y, rot, tile);
    }

    /**
     * Rotates the tile 90° left
     */
    public void rotTile(int x, int y) {
        tileRaster.rotTile(x, y);
    }

    public void delTile(int x, int y) {
        tileRaster.delTile(x, y);
    }

    public boolean flagsOnTile(int x, int y) {
        return tileRaster.flagsOnTile(x, y);
    }

    public boolean legalFlagPosition(int x, int y) {
        return tileRaster.legalFlagPosition(x, y);
    }

    public boolean legalFlagPosAfterMove(int x, int y) {
        return tileRaster.legalFlagPosAfterMove(x, y);
    }

    /**
     * @return the reason the flag may not be placed, or null if it may be.
     */
    public String reasonFlagIllegal(int x, int y) {
        return tileRaster.reasonFlagIllegal(x, y);
    }

    public void addFlag(int x, int y) throws FlagException {
        tileRaster.addFlag(x, y);
    }

    public void delFlag(int nr) {
        tileRaster.delFlag(nr);
    }

    public void delFlag(int ax, int ay) {
        tileRaster.delFlag(ax, ay);
    }

    public boolean flagExists(int ax, int ay) {
        return tileRaster.flagExists(ax, ay);
    }

    public void moveFlag(int nr, int x, int y) throws FlagException {
        tileRaster.moveFlag(nr, x, y);
    }

    public void moveFlag(int ax, int ay, int x, int y) throws FlagException {
        tileRaster.moveFlag(ax, ay, x, y);
    }

    public Location[] getFlagPositions() {
        return tileRaster.getFlagPositions();
    }

    public Tile[][] getTiles() {
        return tileRaster.getTiles();
    }

    public Tile getTileAt(int x, int y) {
        return tileRaster.getTileAt(x, y);
    }

    public void saveTileRaster() {
        tileRasterSave = tileRaster.getClone();
    }

    public void restoreTileRaster() {
        tileRaster = tileRasterSave;
    }

    public boolean isBoardValid() throws OneFlagException, NonContiguousMapException {
        return tileRaster.isBoardValid();
    }

    /**
     * @return the current board as a Properties-object.
     */
    public Properties getBoardAsProperties() {
        Properties ret = new Properties();
        Tile[][] tiles = getTiles();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j] != null) {
                    ret.setProperty("kach" + i + "," + j, tiles[i][j].getName());
                    ret.setProperty("dreh" + i + "," + j, "" + tiles[i][j].getRotation());
                }
            }
        }
        Location[] flag = getFlagPositions();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] != null) {
                ret.setProperty("flag" + i + "x", "" + flag[i].x);
                ret.setProperty("flag" + i + "y", "" + flag[i].y);
            }
        }
        return ret;
    }


    /**
     * Load a board.
     * @param spfProp a Properties-object generated by getBoardAsProperties, above.
     */
    public void loadBoardFromProperties(Properties spfProp) {
        Location dim = getBoardDim();
        for (int i = 0; i < dim.x; i++) {
            for (int j = 0; j < dim.y; j++) {
                //read name of tile
                String name = spfProp.getProperty("kach" + i + "," + j);
                delTile(i, j);
                if (name != null) {
                    int rot = Integer.parseInt(spfProp.getProperty("dreh" + i + "," + j));
                    try {
                        setTile(i, j, rot, name);
                    } catch (FlagPresentException e) {
                    }
                }
            }
        }
        int flAnz = tileRaster.getMaxFlag();
        int flx,fly;
        for (int i = 0; i < flAnz; i++) {
            delFlag(i);
        }
        for (int i = 0; i < flAnz; i++) {
            try {
                if (spfProp.getProperty("flag" + i + "x") != null) {
                    flx = Integer.parseInt(spfProp.getProperty("flag" + i + "x"));
                    fly = Integer.parseInt(spfProp.getProperty("flag" + i + "y"));
                    addFlag(flx, fly);
                }
            } catch (Exception e) {
            }
        }
    }

    public TileInfo[] getTileInfos() {
        return tileFactory.getTileInfos();
    }

    public Image getImage(String name) {
        return tileFactory.getImage(name);
    }

    /**
     * Start a stand-alone Ausgabe.
     */
    public static BNSThread watchAGame(String ip, int port) {
        return Launcher.watchAGame(ip, port, false);
    }

    public static BNSThread watchAGame() {
        return Launcher.watchAGame(GameOptions.DHOST, GameOptions.DPORT, false);
    }

    public static BNSThread watchAGameNoSplash() {
        return Launcher.watchAGame(GameOptions.DHOST, GameOptions.DPORT, true);
    }

    /**
     * Start a human player
     */
    public static BNSThread participateInAGame(String ip, int port, String name, int color) {
        return Launcher.participateInAGame(ip, port, name, color, false);
    }

    public static BNSThread participateInAGame(String ip, String name, int color) {
        return Launcher.participateInAGame(ip, GameOptions.DPORT, name, color, false);
    }

    public static BNSThread participateInAGame(String name, int color) {
        return Launcher.participateInAGame(GameOptions.DHOST, GameOptions.DPORT, name, color, false);
    }

    public static BNSThread participateInAGameNoSplash(String name, int color) {
        return Launcher.participateInAGame(GameOptions.DHOST, GameOptions.DPORT, name, color, true);
    }

    public static BNSThread startAutoBot(String ip, int port, int iq) {
        return Launcher.startAutoBot(ip, port, iq);
    }

    public static BNSThread startAutoBot(int iq, boolean beltAware) {
        return Launcher.startAutoBot(GameOptions.DHOST, GameOptions.DPORT, iq, beltAware);
    }

    public static BNSThread startAutoBot(int iq, boolean beltAware, String botName) {
        return Launcher.startAutoBot(GameOptions.DHOST, GameOptions.DPORT, iq, beltAware, botName);
    }

    public Server startGame() throws OneFlagException, NonContiguousMapException {
       return  startGame(null);
    }

    public Server startGame(ServerObserver listener) throws OneFlagException, NonContiguousMapException {
        return launcher.startGame(gameOptions, listener );
    }

    public void gameStarts() {
        tileFactory.forgetTiles();
        launcher.gameStarts(GameOptions.DHOST, GameOptions.DPORT);
    }

    public void killServer() {
        launcher.stopServer();
    }

    public void prepareTiles() {
        tileFactory.prepareTiles();
    }

    GameOptions getGameOptions() {
        if (gameOptions == null) {
            gameOptions = new GameOptions();
        }
        return gameOptions;
    }

    /**
     * After everything is chosen, update the game options,
     * i.e. set field and flags.
     */
    public void updateGameOptions() throws OneFlagException, NonContiguousMapException  {
        Location dim = tileRaster.getSpielfeldSize();
        gameOptions.setX(dim.x);
        gameOptions.setY(dim.y);
        gameOptions.setBoard(tileRaster.getBoard());
        gameOptions.setFlags(tileRaster.getRFlaggen());
    }
}
