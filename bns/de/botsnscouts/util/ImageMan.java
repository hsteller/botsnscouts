package de.botsnscouts.util;

import java.io.*;
import java.awt.*;
import java.awt.image.*;

import de.botsnscouts.BotsNScouts;
import javax.swing.ImageIcon;
import java.net.URL;

import com.sixlegs.image.png.PngImage;

public class ImageMan {
    public final static int
	CBELTS = 0,
	EBELTS = 1,
	DIVERSE = 2,
	ROBOS = 3,
	SCOUT = 4,
	STARTKNOEPFE = 5,
	SCHLAFPLATZ = 6,
	SCHLAFSCOUT = 7,
	KSCHLAF = 8,
        KWACH = 9,
        WISENHEIMER_ACTIVE = 10;

    // PNG-IDs
    public final static int
	ROBOCENTER = 0,
	REGLOCK =1,
	BOTDAMAGE =2;


    static Class c = de.botsnscouts.BotsNScouts.class;

    public final static  ImageIcon CardRUECK = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/karterueck.gif")));
    public final static  ImageIcon CardM1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/m1.gif")));
    public final static  ImageIcon CardM2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/m2.gif")));
    public final static  ImageIcon CardM3 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/m3.gif")));
    public final static  ImageIcon CardBU = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/bu.gif")));
    public final static  ImageIcon CardRL = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/rl.gif")));
    public final static  ImageIcon CardRR = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/rr.gif")));
    public final static  ImageIcon CardUT = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/ut.gif")));
    public final static  ImageIcon CardRLEER = new ImageIcon(Toolkit.getDefaultToolkit().getImage(c.getResource("images/register-leer.gif")));

    public final static String[] PngImages = {
	"images/robocenter.png",
	"images/lock.png",
	"images/boom.png"
    };

    public static Image[] PNGImages = new Image[PngImages.length];
    public static ImageIcon[] PNGImageIcons = new ImageIcon[PngImages.length];


    final static ImageSet[] imgSetDescr = {
	new ImageSet("images/cbelts.gif",   24, 5 ),
	new ImageSet("images/ebelts.gif",   24, 5 ),
	new ImageSet("images/diverse.gif",  30, 6 ),
	new ImageSet("images/robos.gif",    32, 4 ),
	new ImageSet("images/scoutklug.gif", 8, 4 ),
	new ImageSet("images/startknoepfe.gif", 6, 6),
	new ImageSet("images/schlafplatzmitscout.gif", 6, 6),
	new ImageSet("images/schlafplatz.gif", 1, 1),
	new ImageSet("images/klugschlaf.gif", 6, 6),
	new ImageSet("images/klugwach.gif", 1, 1),
	new ImageSet("images/kscheisser.gif", 1, 1),
    };

    final static int IMGSETCOUNT = imgSetDescr.length;


    static Image[][] imgSets = new Image[IMGSETCOUNT][];

    static boolean imagesLoading = false;
    static boolean loadingFinished = false;

    static Thread imageLoader;
    static MediaTracker tracker;

    public synchronized static void loadImages() {
	if( imagesLoading )
	    return;
	imagesLoading = true;

	tracker = new MediaTracker(new Button());
	Toolkit tk = Toolkit.getDefaultToolkit();
	CropperField gridCropper = new CropperField(8, 8, 64, tk);

	for(int i=0; i<IMGSETCOUNT; i++) {
	    ImageSet descr = imgSetDescr[i];

	    Image img = tk.getImage( c.getResource( descr.name ) );
	    tracker.addImage( img, i );
	    imgSets[i] = new Image[ descr.size ];
	    gridCropper.multiCrop( img, descr.rowlength, descr.size, imgSets[i], tracker, i);
	}

	for (int i=0; i < PngImages.length; i++) {
	        try {
		    PngImage png = new PngImage(c.getResource(PngImages[i]));
		    PNGImages[i] = tk.createImage(png);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	// start loading in a different thread
	imageLoader = new Thread() {
	    public void run() {
		try {
		    System.err.println("started loading and cropping of images in background");
		    tracker.waitForAll();
		    System.err.println("images ready");
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
    public static Image[] waitForImages(int id) {
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

    public static Image getPNGImage(int id) {
	if( !imagesLoading )
	    loadImages();
	return PNGImages[id];
    }

    public static ImageIcon getPNGImageIcon(int id) {
	if( !imagesLoading )
	    loadImages();
	if(PNGImageIcons[id]==null){
	    PNGImageIcons[id]=new ImageIcon(PNGImages[id]);
	}
	return PNGImageIcons[id];
    }


    public static void finishLoading() {
	if(! imagesLoading )
	    loadImages();

	try{
	    imageLoader.join();
        }catch(InterruptedException ie) { System.err.println( ie ); }
    }

    public static ImageIcon getIcon(String s) {
	URL url = BotsNScouts.class.getResource( "images/" + s );
	return new ImageIcon( url );
    }
}
