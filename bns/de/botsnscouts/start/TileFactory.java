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

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.gui.BoardView;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Message;
import org.apache.log4j.Category;

import java.awt.*;
import java.io.*;
import java.util.*;

public class TileFactory {

    public static final Category CAT = Category.getInstance(TileFactory.class);

    private Hashtable tileTab;
    private Hashtable imgTab;
    private int thumbnailsize;
    boolean fertig = false;
    Object lock = new Object();


    public TileFactory(int thumbnailsize) {
        tileTab = new Hashtable();
        imgTab = new Hashtable();
        this.thumbnailsize = thumbnailsize;
    }

    boolean workerStarted = false;
    Thread worker = new BNSThread("TileWorker") {
        public void run() {

            // Load those from bns.home/tiles
            File kd = null;
            kd = new File(Conf.getBnsHome() + System.getProperty("file.separator") + "tiles");
            File[] all = kd.listFiles(new RRAFilter());
            //File[] allj = kdj.listFiles(new RRAFilter());
            FileInputStream istream;
            if (all != null) {
                for (int i = 0; i < all.length; i++) {
                    try {
                        istream = new FileInputStream(all[i]);
                    } catch (FileNotFoundException e) {
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
            } catch (IOException e) {
                CAT.warn("Couldn't load tile.index from distrib.");
            }
            int numTiles = 0;
            try {
                numTiles = Integer.parseInt(prop.getProperty("numTiles"));
            } catch (NumberFormatException e) {
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
//und lese Board aus
            while ((tmp = kachReader.readLine()) != null)
                str.append(tmp + "\n");

        } catch (Exception e) {
            System.err.println(e);
        }
        //erzeige Tile mit der Tilestring
        Tile kach = null;
        try {
            kach = new Tile(name, str.toString(), thumbnailsize);
            //kach.getImage();
            Tile[] kachAr = new Tile[4];
            kachAr[0] = kach;//Die Tile mit Drehung 0 wird initialisiert
            tileTab.put(name, kachAr);
        } catch (FlagException e) {
            System.err.println(e);
        } catch (FormatException e) {
            System.err.println(e);
        }
    }

    //gibt eine Tile mit Drehung zurück
    public Tile getTile(String name, int drehung) {
        checkLadeStatus();
        Tile[] kachAr = (Tile[]) tileTab.get(name);
        //Global.debug(this,tileTab.toString());
        if (kachAr[drehung] != null) {
            return kachAr[drehung];
        }
        for (int i = 1; i <= drehung; i++) {
            if (kachAr[i] == null)
                kachAr[i] = kachAr[i - 1].getRotated();
        }
        return kachAr[drehung];
    }

    public Image getImage(String name) {
        if (imgTab.get(name) != null)
            return (Image) imgTab.get(name);
        Tile[] kachAr = (Tile[]) tileTab.get(name);
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
        for (Enumeration namen = tileTab.keys(); namen.hasMoreElements(); i++) {
            all[i] = (String) namen.nextElement();
        }
        Arrays.sort(all);
        for (i = 0; i < anz; i++) {
            CAT.debug(all[i]);
            infos[i] = new TileInfo(all[i], getImage(all[i]));
            //((Tile[])tileTab.get(all[i]))[0].getImage());
        }
        return infos;
    }

    private void checkLadeStatus() {
    
            if (!workerStarted)
                prepareTiles();
      
        try {
            synchronized (worker){ // FIXME vielleicht hilft das syncen hier gegen die NPE in prepareTiles?
                worker.join();
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    public void prepareTiles() {
        synchronized (worker) {
            if (workerStarted) return;
            worker.start();
            worker.setPriority(Thread.MIN_PRIORITY); // FIXME hier gibts NullPoin
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


