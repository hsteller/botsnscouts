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

import java.awt.Button;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.apache.log4j.Category;

import de.botsnscouts.BotsNScouts;

public class ImageMan {
    static final Category CAT = Category.getInstance(ImageMan.class);

    public final static int CBELTS = 0, EBELTS = 1, DIVERSE = 2, ROBOS = 3, SCOUT = 4, STARTKNOEPFE = 5,
                    SCHLAFPLATZ = 6, SCHLAFSCOUT = 7, KSCHLAF = 8, KWACH = 9, WISENHEIMER_ACTIVE = 10,
                    PNG_ROBOCENTER = 11, PNG_REGLOCK = 12, PNG_BOTDAMAGE = 13, STATUS_STUFF = 14, STATUS_FRAME = 15,
                    MEDIUM_ROBOS = 16;

    // PNG-IDs
    // public final static int
    // ROBOCENTER = 0,
    // REGLOCK =1,
    // BOTDAMAGE =2;

    static Class<BotsNScouts> c = de.botsnscouts.BotsNScouts.class;

    private static HashMap<String,ImageIcon> actionToImageMapping;

    public static final String IMG_NAME_BACKSIDE = "images/karterueck.gif";

    public static final String IMG_NAME_EMPTY = "images/register-leer.gif";

    public static final String IMG_NAME_MOVE1 = "images/m1.gif";

    public static final String IMG_NAME_MOVE2 = "images/m2.gif";

    public static final String IMG_NAME_MOVE3 = "images/m3.gif";

    public static final String IMG_NAME_BACKUP = "images/bu.gif";

    public static final String IMG_NAME_ROTATE_R = "images/rr.gif";

    public static final String IMG_NAME_ROTATE_L = "images/rl.gif";

    public static final String IMG_NAME_UTURN = "images/ut.gif";

    public final static ImageIcon CardRUECK = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_BACKSIDE)));

    public final static ImageIcon CardM1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_MOVE1)));

    public final static ImageIcon CardM2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_MOVE2)));

    public final static ImageIcon CardM3 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_MOVE3)));

    public final static ImageIcon CardBU = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_BACKUP)));

    public final static ImageIcon CardRL = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_ROTATE_L)));

    public final static ImageIcon CardRR = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_ROTATE_R)));

    public final static ImageIcon CardUT = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_UTURN)));

    public final static ImageIcon CardRLEER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
                    c.getResource(IMG_NAME_EMPTY)));

    final static ImageSet[] imgSetDescr = { ImageSet.getInstance("images/cbelts.gif", 24, 5),
            ImageSet.getInstance("images/ebelts.gif", 24, 5), ImageSet.getInstance("images/diverse.gif", 30, 6),
            ImageSet.getInstance("images/robos.gif", 32, 4), ImageSet.getInstance("images/scoutklug.gif", 8, 4),
            ImageSet.getInstance("images/startknoepfe.gif", 6, 6),
            ImageSet.getInstance("images/schlafplatzmitscout.gif", 6, 6),
            ImageSet.getInstance("images/schlafplatz.gif", 1, 1), ImageSet.getInstance("images/klugschlaf.gif", 6, 6),
            ImageSet.getInstance("images/klugwach.gif", 1, 1), ImageSet.getInstance("images/kscheisser.gif", 1, 1),
            ImageSet.getInstance("images/robocenter.png", 1, 1), ImageSet.getInstance("images/locked2.png", 1, 1),
            ImageSet.getInstance("images/boom.png", 1, 1), ImageSet.getInstance("images/ministuff.png", 1, 1),
            ImageSet.getInstance("images/statusframe.png", 1, 1),

    };

    final static ImageSet[] imgSetDescrMedium = { ImageSet.getInstance("images/robos48x48.gif", 32, 4) };

    final static int IMGSETCOUNT = imgSetDescr.length;

    final static int IMGSETCOUNT_MEDIUM = imgSetDescrMedium.length;

    final static int IMGSETCOUNT_ALL = IMGSETCOUNT + IMGSETCOUNT_MEDIUM;

    static Image[][] imgSets = new Image[IMGSETCOUNT_ALL][];

    static boolean imagesLoading = false;

    static boolean loadingFinished = false;

    static Thread imageLoader;

    static MediaTracker tracker;

    public synchronized static void loadImages() {
        if (imagesLoading)
            return;
        imagesLoading = true;

        tracker = new MediaTracker(new Button());
        Toolkit tk = Toolkit.getDefaultToolkit();
        CropperField gridCropper = new CropperField(8, 8, 64, tk);
        CropperField mediumCropper = new CropperField(4, 8, 48, tk);

        for (int i = 0; i < IMGSETCOUNT; i++) {
            ImageSet descr = imgSetDescr[i];
            try {
                Image img = descr.getImage();
                imgSets[i] = new Image[descr.size];

                tracker.addImage(img, i);
                if (descr.size == 1) {
                    imgSets[i][0] = img;
                }
                else {
                    gridCropper.multiCrop(img, descr.rowlength, descr.size, imgSets[i], tracker, i);
                }
            }
            catch (IOException ioe) {
                CAT.error("Couldn't load: " + descr.name, ioe);
                imgSets[i] = new Image[0];
            }
        }
        for (int i = 0, globalI = IMGSETCOUNT; i < IMGSETCOUNT_MEDIUM; i++, globalI++) {
            ImageSet descr = imgSetDescrMedium[i];
            try {
                Image img = descr.getImage();
                imgSets[globalI] = new Image[descr.size];

                tracker.addImage(img, globalI);
                if (descr.size == 1) {
                    imgSets[globalI][0] = img;
                }
                else {
                    mediumCropper.multiCrop(img, descr.rowlength, descr.size, imgSets[globalI], tracker, globalI);
                }
            }
            catch (IOException ioe) {
                CAT.error("Couldn't load: " + descr.name, ioe);
                imgSets[globalI] = new Image[0];
            }
        }

        // start loading in a different thread
        imageLoader = new BNSThread("imageLoader") {
            public void doShutdown() {
                ImageMan.CAT.debug("ImageMan's empty doShutdown() called");
            }

            public void run() {
                try {
                    ImageMan.CAT.debug("started loading and cropping of images in background");
                    tracker.waitForAll();
                    ImageMan.CAT.debug("images ready");
                }
                catch (InterruptedException ie) {
                    ImageMan.CAT.error(ie);
                }
            }
        };
        imageLoader.start();
    }

    // ///////////////////////////////////////////////////////////////////
    // damit der KachelEditor unsere Bilder benutzen kann

    // Diese Methode ist nur fuer den Fall gedacht, dass man synchron
    // das Laden von Bildern erzwingen will
    // Achtung: ein Aufruf dieser Methode triggert auch dann das Laden
    // ALLER Bilder (im Hintergrund) falls man nur einen Satz davon
    // haben will - aber sie wartet eben nur auf diesen einen Satz
    public static Image[] waitForImages(int id) {
        if (id < 0 || id >= IMGSETCOUNT) {
            throw new RuntimeException("ImageMan: invalid id");
        }

        if (!imagesLoading)
            loadImages();

        try {
            tracker.waitForID(id);
        }
        catch (InterruptedException ie) {
            System.err.println(ie);
        }

        return imgSets[id];
    }

    // ////////////////////////////////////////////////////////////////////

    // Abfrage der Images ... ACHTUNG: die Referenzen in den
    // zur�ckgegebenen Arrays sind erst g�ltig, wenn finishLoading()
    // erfolgreich aufgerufen wurde
    public static Image[] getImages(int id) {
        if (!imagesLoading)
            loadImages();

        return imgSets[id];
    }

    public static Image getImage(int setId, int index) {
        if (!imagesLoading)
            loadImages();

        return imgSets[setId][index];
    }

    public static Image getImage(int setId) {
        if (!imagesLoading)
            loadImages();

        return imgSets[setId][0];
    }

    public static ImageIcon getImageIcon(int setId, int index) {
        return new ImageIcon(getImage(setId, index));
    }

    public static ImageIcon getImageIcon(int setId) {
        return new ImageIcon(getImage(setId));
    }

    // public static Image getPNGImage(int id) {
    // if( !imagesLoading )
    // loadImages();
    // return PNGImages[id];
    // }
    //
    // public static ImageIcon getPNGImageIcon(int id) {
    // if( !imagesLoading )
    // loadImages();
    // if(PNGImageIcons[id]==null){
    // PNGImageIcons[id]=new ImageIcon(PNGImages[id]);
    // }
    // return PNGImageIcons[id];
    // }

    public static void finishLoading() {
        if (!imagesLoading)
            loadImages();

        try {
            imageLoader.join();
        }
        catch (InterruptedException ie) {
            System.err.println(ie);
        }
    }

    public static ImageIcon getIcon(String s) {
        URL url = BotsNScouts.class.getResource("images/" + s);
        return new ImageIcon(url);
    }

    public static Image getCardImage(String action) {
        if (actionToImageMapping == null) {
            initActionToImageMapping();
        }

        ImageIcon icon =  actionToImageMapping.get(action);
        if (icon != null) {
            return icon.getImage();
        }
        else {
            return null;
        }
        /*
         * 
         * String resourceString = (String) actionToImageMapping.get(action); if (resourceString!=null){ return Toolkit.getDefaultToolkit().getImage(c.getResource(resourceString)); } else { return null; }
         */
    }

    private static void initActionToImageMapping() {
        ImageMan.finishLoading();
        actionToImageMapping = new HashMap<String,ImageIcon>();

        actionToImageMapping.put(Card.ACTION_MOVE1, CardM1);
        actionToImageMapping.put(Card.ACTION_MOVE2, CardM2);
        actionToImageMapping.put(Card.ACTION_MOVE3, CardM3);
        actionToImageMapping.put(Card.ACTION_BACK, CardBU);
        actionToImageMapping.put(Card.ACTION_ROTATE_L, CardRL);
        actionToImageMapping.put(Card.ACTION_ROTATE_R, CardRR);
        actionToImageMapping.put(Card.ACTION_UTURN, CardUT);

        /*
         * actionToImageMapping.put(Card.ACTION_MOVE1, IMG_NAME_MOVE1); actionToImageMapping.put(Card.ACTION_MOVE2, IMG_NAME_MOVE2 ); actionToImageMapping.put(Card.ACTION_MOVE3, IMG_NAME_MOVE3);
         * actionToImageMapping.put(Card.ACTION_BACK,IMG_NAME_BACKUP); actionToImageMapping.put(Card.ACTION_ROTATE_L, IMG_NAME_ROTATE_L); actionToImageMapping.put(Card.ACTION_ROTATE_R, IMG_NAME_ROTATE_R); actionToImageMapping.put(Card.ACTION_UTURN,
         * IMG_NAME_UTURN);
         */
    }
}
