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

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import org.apache.log4j.Category;

public class CursorMan {
    public final static int
	CURSOR = 0,
	BOTCENTER=1,
	STATUSROBOTS = 2,
	DAMAGE = 3,
	WINNER = 4;

    static final Category CAT = Category.getInstance( ImageMan.class );



    final static ImageSet[] imgSetDescr = {
	ImageSet.getInstance("images/cursor.gif",   5, 5 ),
	ImageSet.getInstance("images/botcenter.gif",   5, 5 ),
	ImageSet.getInstance("images/minibots.gif", 8, 1),
	ImageSet.getInstance("images/damage.gif", 1, 1),
	ImageSet.getInstance("images/winner.gif", 8, 8)
    };

    final static int IMGSETCOUNT = imgSetDescr.length;


    static Image[][] imgSets = new Image[IMGSETCOUNT][];

    static boolean imagesLoading = false;
    static boolean loadingFinished = false;

    static Thread imageLoader;
    static MediaTracker tracker;

    synchronized static void loadImages() {
	if( imagesLoading )
	    return;
	imagesLoading = true;

	//Miriam: Bilder werden relativ zum Klassenpfad gesucht
	Class c = de.botsnscouts.BotsNScouts.class;

	tracker = new MediaTracker(new Button());
	Toolkit tk = Toolkit.getDefaultToolkit();
	CropperField gridCropper = new CropperField(8, 8, 24 , tk);

	for(int i=0; i<IMGSETCOUNT; i++) {
	    ImageSet descr = imgSetDescr[i];

	    Image img = tk.getImage( c.getResource( descr.name ) );
	    tracker.addImage( img, i );
	    imgSets[i] = new Image[ descr.size ];
	    gridCropper.multiCrop( img, descr.rowlength, descr.size, imgSets[i], tracker, i);
	}

	// start loading in a different thread
	imageLoader = new BNSThread() {
	    public void run() {
		try {
		    CursorMan.CAT.debug("started loading and cropping of images in background");
		    tracker.waitForAll();
		    CursorMan.CAT.debug("images ready");
		}catch(InterruptedException ie) { System.err.println( ie ); }
	    }
	};
	imageLoader.start();
    }


    /////////////////////////////////////////////////////////////////////
    // damit der KachelEditor unsere Bilder benutzen kann

    // Diese Methode ist nur fuer den Fall gedacht, dass man synchron
    // das Laden von Bildern erzwingen will
    // Achtung: ein Aufruf dieser Methode triggert auch dann das Laden
    // ALLER Bilder (im Hintergrund) falls man nur einen Satz davon
    // haben will - aber sie wartet eben nur auf diesen einen Satz
    static Image[] waitForImages(int id) {
	if( id < 0 || id >= IMGSETCOUNT ) {
	    throw new RuntimeException("ImageMan: invalid id");
	}

	if(! imagesLoading )
	    loadImages();

	try{
	    tracker.waitForID( id );
	}catch(InterruptedException ie) { System.err.println( ie ); }

	return imgSets[id];
    }
    //////////////////////////////////////////////////////////////////////

    // Abfrage der Images ... ACHTUNG: die Referenzen in den
    // zurückgegebenen Arrays sind erst gültig, wenn finishLoading()
    // erfolgreich aufgerufen wurde
    public static Image[] getImages(int id) {
	if( !imagesLoading )
	    loadImages();

	return imgSets[id];
    }

    static void finishLoading() {
	if(! imagesLoading )
	    loadImages();

	try{
	    imageLoader.join();
        }catch(InterruptedException ie) { System.err.println( ie ); }
    }
}
