package de.botsnscouts.util;

import java.io.*;
import java.awt.*;
import java.awt.image.*;




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
	KWACH = 9;
    
	

	
    
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
	Class c;
	try {
	    c = Class.forName("de.spline.rr.ImageMan");
	} catch(ClassNotFoundException e) {
	    System.out.println( e );
	    throw new RuntimeException("ImageMan nicht gefunden");
	}

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
