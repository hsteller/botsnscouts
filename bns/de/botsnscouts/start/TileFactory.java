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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Category;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.gui.BoardView;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Message;

public class TileFactory {

    public static final Category CAT = Category.getInstance(TileFactory.class);

    private Hashtable<String, Tile[]> tileTab;

    private Hashtable<String, Image> imgTab;

    private int thumbnailsize;

    boolean fertig = false;

    Object lock = new Object();

    public TileFactory(int thumbnailsize) {
        tileTab = new Hashtable<String, Tile[]>();
        imgTab = new Hashtable<String, Image>();
        this.thumbnailsize = thumbnailsize;
    }

    boolean workerStarted = false;

    private Thread worker = new BNSThread("TileWorker") {
        public void doShutdown() {
            CAT.debug("TileFactory's workerThread: empty doShutdown() called");
        }

        public void run() {

            // Load those from bns.home/tiles
            File kd = null;
            kd = new File(Conf.getBnsHome() + System.getProperty("file.separator") + "tiles");
            File[] all = kd.listFiles(new RRAFilter());
            // File[] allj = kdj.listFiles(new RRAFilter());
            FileInputStream istream;
            if (all != null) {
                for (int i = 0; i < all.length; i++) {
                    try {
                        istream = new FileInputStream(all[i]);
                    }
                    catch (FileNotFoundException e) {
                        continue;
                    }
                    putOneTile(istream, all[i].getName());
                }
            }

            // Load those from the distribution
            InputStream stream = BotsNScouts.class.getResourceAsStream("tiles/tile.index");
            if (stream == null) {
                CAT.warn("Couldn't find tiles/tile.index");
                return;
            }
            Properties prop = new Properties();
            try {
                prop.load(stream);
            }
            catch (IOException e) {
                CAT.warn("Couldn't load tile.index from distrib.");
            }
            int numTiles = 0;
            try {
                numTiles = Integer.parseInt(prop.getProperty("numTiles"));
            }
            catch (NumberFormatException e) {
                CAT.warn("Error parsing numTiles in tile.index!");
            }
            for (int i = 0; i < numTiles; i++) {
                String name = prop.getProperty("tile" + i);
                stream = BotsNScouts.class.getResourceAsStream("tiles/" + name);
                if (stream == null) {
                    CAT.warn("Error loading tile" + i + " from distribution.");
                    continue;
                }
                putOneTile(stream, name);
            }
        }
    };

    /** Reads one tile and puts into the hashtable */
    private void putOneTile(InputStream istream, String name) {
        CAT.debug("putting tile " + name);
        if (tileTab.get(name) != null) {
            CAT.warn("Trying to redefine tile " + name);
            return;
        }
        StringBuffer str = new StringBuffer();
        try {
            BufferedReader kachReader = new BufferedReader(new InputStreamReader(istream));
            String tmp = null;
            // und lese Board aus
            while ((tmp = kachReader.readLine()) != null)
                str.append(tmp + "\n");

        }
        catch (Exception e) {
            CAT.error(e);
        }
        // create tile from tilestring
        Tile kach = null;
        try {
            kach = new Tile(name, str.toString(), thumbnailsize);
            // kach.getImage();
            Tile[] kachAr = new Tile[4];
            kachAr[0] = kach;// initial tile with rotation(orientation) 0
            tileTab.put(name, kachAr);
        }
        catch (FlagException e) {
            CAT.error(e.getMessage(), e);
        }
        catch (FormatException e) {
            CAT.error(e.getMessage(), e);
        }
    }

    // returns a tile with a rotation
    public Tile getTile(String name, int rotation) {
        checkLadeStatus();
        Tile[] kachAr = tileTab.get(name);
        // Global.debug(this,tileTab.toString());
        if (kachAr[rotation] != null) {
            return kachAr[rotation];
        }
        for (int i = 1; i <= rotation; i++) {
            if (kachAr[i] == null)
                kachAr[i] = kachAr[i - 1].getRotated();
        }
        return kachAr[rotation];
    }

    public Image getImage(String name) {
        if (imgTab.get(name) != null)
            return imgTab.get(name);
        Tile[] kachAr = tileTab.get(name);
        CAT.debug("creating image on-demand.");
        Image img = BoardView.createThumb(kachAr[0], thumbnailsize);
        imgTab.put(name, img);
        return img;
    }

    public TileInfo[] getTileInfos() {
        checkLadeStatus();
        int anz = tileTab.size();
        TileInfo[] infos = new TileInfo[anz];
        String[] all = new String[anz];
        int i = 0;
        for (String name : tileTab.keySet()) {
            all[i++] = name;
        }
        Arrays.sort(all);
        for (i = 0; i < anz; i++) {
            CAT.debug(all[i]);
            infos[i] = new TileInfo(all[i], getImage(all[i]));
            // ((Tile[])tileTab.get(all[i]))[0].getImage());
        }
        return infos;
    }

    private void checkLadeStatus() {

        if (!workerStarted)
            prepareTiles();

        try {
            synchronized (worker) { // FIXME vielleicht hilft das syncen hier gegen die NPE in prepareTiles?
                worker.join();
            }
        }
        catch (InterruptedException e) {
            CAT.error(e.getMessage(), e);
        }
    }

    public void prepareTiles() {
        synchronized (worker) {
            if (workerStarted)
                return;
            worker.start();
            // worker.setPriority(Thread.MIN_PRIORITY); // FIXME hier gibts NullPoin
            workerStarted = true;
        }
    }

    public void forgetTiles() {
        tileTab.clear();
    }

    public static void main(String args[]) {
        Message.setLanguage(new Locale("DE", "de"));
        org.apache.log4j.BasicConfigurator.configure();
        (new TileFactory(150)).checkLadeStatus();
    }
}
