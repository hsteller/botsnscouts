package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.applet.*;
import java.awt.geom.*;
import javax.swing.*;

import de.botsnscouts.board.*;
import de.botsnscouts.util.*;
import de.botsnscouts.*;
/**
 * Spielfeld-Ausgabe-Canvas ist das Objekt, das der Ausgabe und dem menschlichen Spieler das Spielfeld grafisch darstellt und verwaltet
 * @author ursprünglich Daniel Holtz
 * @version Verbesserung von 1.0

 * changes: enno v1.23
 * 1. beim painten wird nur noch der teil ins sichtbare kopiert, der auch
 *   in gepaintet werden muss (hat aber nicht viel gebracht)
 * 2. Felder, die nicht wirklich neu gezeichnet werden müssen, werden nicht
 *    mehr betrachtet (bringt was!)
 * 3. Code insgesamt lesbarer strukturiert (noch nicht ganz beendet)
 * 4. makePixelArray in eigene Methode ausgelagert
 * 5. lokale Variablen eingeführt ...
 
 * 6. Verwaltung und Speicherung der Bilder ausgelagert in
 *    Klasse ImageMan (kann so dann auch vom KachelEditor verwendet werden)
 *    bilder werden dann nur einmal pro JVM geladen, und zwar bei Programm-
 *    start (StartSpieler) im Hintergrund. Der erste Spielfeldaufbau ist
 *    damit viel schneller, ebenso die Spielfeld-Vorschau
 */

public class SACanvas extends Component {
    // inner classes
    public static interface ClickListener {
	void feldClicked( int x, int y, int modifiers );
    }

    // directions
    private static final int NORTH=0;
    private static final int EAST=1;
    private static final int SOUTH=2;
    private static final int WEST=3;


    private JScrollPane myScrollPane;
    private static final int FELDSIZE = 64; // Grösse der Felder in Pixeln

    /** Anzahl der Schritte in denen der Laser gezeichnet wird.
     */
    private static final int FULL_LENGTH_INT=30; 
    /** Anzahl der Schritte in denen der Laser gezeichnet wird. 
     */
    private static final double FULL_LENGTH_DOUBLE=30.0;


    // for painting active Lasers
    // private boolean drawRobLaser;
    //private boolean drawBordLaser;
    private Ort source;
    private Ort target;
    private int laserFacing;
    private boolean activeBordLasers; 
    
    // Farben fuer die Laser; Staerken von 1 bis 3
    static final Color[] laserColor = { Color.red.brighter(), 
					Color.orange,
					Color.yellow };

    // Zum Nachschauen, welche Farbe ein Roboter hat (anhand der Roboternamen)
    private java.util.Hashtable nameToColorHash;
    private boolean gotColors;

    // Ueberbleibsel zum bestimmen der Laserfarbe fuer statische Laser;
    // koennte wegoptimiert werden
    private LaserDef actuallaser;

    private AudioClip [] mLaserWav = new AudioClip[2];
    public boolean soundActive;
    private int laserWavCount;

    private Image dbi;
    private Image[] cbeltCrop,ebeltCrop,diverseCrop,robosCrop,scoutCrop,robosCrop2;

    private int x,y;
   
   
    private Roboter[] robos;
    private Roboter vorschauRob;
    private Color[] robocolor2; 

    public static final Color GREEN  = new Color(4,156,52);
    public static final Color YELLOW = new Color(251,253,4);
    public static final Color RED    = new Color(252,2,4);
    public static final Color BLUE   = new Color(4,2,250);
    public static final Color ROSA   = new Color(251,2,251);
    public static final Color ORANGE = new Color(233,94,4);
    public static final Color GRAY   = new Color(220,222,220);
    public static final Color VIOLET = new Color(155,2,203);



    //  public static final Color[] robocolor = { Color.green, Color.yellow, Color.red,Color.blue, Color.magenta, Color.orange, Color.gray, Color.magenta.darker()};
    /** Die Farben der Roboter*/
    public static final Color [] robocolor = {GREEN,YELLOW,RED,BLUE,ROSA,ORANGE,GRAY,VIOLET};

    
    SpielfeldSim sf;
    private double dScale = 1.0;
    boolean rescaled = true;

    ClickListener myClickListener;


    public double getScale() {
	return dScale;
    }

    public void setScale( double scale ) {
	// adapt this Component to the scaling factor
	dScale = scale;
	x = (int)(sf.getSizeX() * dScale * FELDSIZE);
	y = (int)(sf.getSizeY() * dScale * FELDSIZE);
	rescaled = true;
	System.err.println("dim : " + x + " " + y );
	setSize(x,y);
    }


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public SACanvas(SpielfeldSim sf_neu){
	init( sf_neu, robocolor );
    }

    public SACanvas(SpielfeldSim sf_neu, Color [] robColors){
	init( sf_neu, robColors );
	
	robosCrop2 = new Image[32];

	// umkopieren der Roboterbilder
	for (int i=0;i<robocolor2.length;i++) {
	    if (robocolor2[i]==null)
		break;
	    if (robocolor2[i].equals(GREEN)){
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j];
	    }
	    else if (robocolor2[i].equals(YELLOW)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+4];
	    }
	    else if (robocolor2[i].equals(RED)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+8];
	    } 
	    else if (robocolor2[i].equals(BLUE)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+12];
	    } 
	    else if (robocolor2[i].equals(ROSA)){
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+16];
	    } 
	    else if (robocolor2[i].equals(ORANGE)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+20];
	    }
	    else if (robocolor2[i].equals(GRAY)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+24];
	    } 
   	    else if (robocolor2[i].equals(VIOLET)) {
		for (int j=0;j<4;j++)
		    robosCrop2[4*i+j]=robosCrop[j+28];
	    } 
	}
	mouseInit();
    }


    
    private void init(SpielfeldSim sf_neu, Color [] robColors) {
	soundActive=true;
	robocolor2=robColors;
	//	drawRobLaser=false;
	//drawBordLaser=false;
	activeBordLasers=false;
	gotColors=false;
	sf=sf_neu;
	//x=(sf.boden.length-2)*64;
	//y=(sf.boden[0].length-2)*64;
	if (soundActive) {
	    laserWavCount=0;
	    String s ="sounds/laserhit.wav"; 
	    URL u = BotsNScouts.class.getResource(s);
	    mLaserWav [0] = Applet.newAudioClip(u);
	    s="sounds/laser2.wav";
	    u = BotsNScouts.class.getResource(s);
	    mLaserWav [1] = Applet.newAudioClip(u);
	    
	}
	setScale( dScale ); // does setSize()
	//setSize(x,y);

	
	ImageMan.finishLoading();

	ebeltCrop   = ImageMan.getImages( ImageMan.EBELTS );
	cbeltCrop   = ImageMan.getImages( ImageMan.CBELTS );
	diverseCrop = ImageMan.getImages( ImageMan.DIVERSE );
	robosCrop   = ImageMan.getImages( ImageMan.ROBOS );
	robosCrop2  = robosCrop;
	scoutCrop   = ImageMan.getImages( ImageMan.SCOUT );
    }


    void setScrollPane(JScrollPane j) {
	myScrollPane = j;
    }

    Point calcKachelPos(int mx, int my) {
	int sfh = sf.getSizeY();
	int sfw = sf.getSizeX();
	double scaledSize = FELDSIZE * getScale();
	
	Point p = new Point();
	p.x = 1 + (int) (mx / scaledSize );
	p.y = sfh - (int) (my / scaledSize );

	// sicherstellen, dass 1 <= p.x <= sfw
	// und  1 <= p.y <= sfy
	
	p.x = Math.min( Math.max(1, p.x), sfw );
	p.y = Math.min( Math.max(1, p.y), sfh );
	return p;
    }



    public void addClickListener( ClickListener listener ) {
	myClickListener = listener;
    }

    
    void mouseInit() {
	addMouseListener( new MouseAdapter() {
	    public void mouseClicked(MouseEvent me) {
		Point feld = calcKachelPos( me.getX(), me.getY() );
		if( myClickListener != null ) {
		    myClickListener.feldClicked( feld.x, feld.y, me.getModifiers() );
		}

		/*
		int mods = me.getModifiers();
	 	if( (mods & MouseEvent.BUTTON3_MASK) == 0 ) 
		     return;

		Dimension sz = myScrollPane.getViewport().getExtentSize();
		int w2 = sz.width/2;
		int h2 = sz.height/2;
		
		//make sure we dont want to scoll 'out' to
		// the left and top
		int x1 = Math.max( me.getX() - w2 , 0);
		int y1 = Math.max( me.getY() - h2 , 0);
		
		// ... and right and bottom
		x1 = Math.min( x1, x - sz.width );
		y1 = Math.min( y1, y - sz.height );
		
		myScrollPane.getViewport().setViewPosition(new Point(x1, y1));
		*/
    
	    }
	});
    }

    public Dimension getMinimumSize() {
	return new Dimension(x,y);
    }

    public Dimension getPreferredSize() {
	return new Dimension(x,y);
    }





    /** Create "name->color" - Hashtable*/
    private void setRobColors (Roboter[] robs) {
	gotColors=true; 
	nameToColorHash = new java.util.Hashtable();
	for (int i=0;i<robs.length;i++) 
	    if (robs[i]==null)
		break;
	    else 
		nameToColorHash.put (robs[i].getName(), robocolor2[i]);
    }
    /** Lookup the Robot's color (by name)
	@param name The Robot's name
	@return The Robot's color. If the name is unknown, Color.white will be returned,
    */
    private Color getRobColor (String name){
	Color foo=null;
	foo= (Color) nameToColorHash.get(name);
	if (foo==null) {
	    System.err.println ("SACanvas.getRobColor: Color for "+name+"'s Laser not found");
	    return Color.white;
	}
	else return foo;
    }


    protected void ersetzeRobos(Roboter[] robos_neu){
	
	if (!gotColors) // jetzt bekomme ich zum erstenmal die Roboter
	    setRobColors(robos_neu);
	    

	robos=robos_neu;
	repaint();
	
    }


    /////////////////////////////////////////////////////////////////////


    /**
       @param sourceRob Die Position des schiessenden Roboters
       @param targetRob Die Positon des getroffenen Roboters
    */

    public void doRobLaser (Roboter sourceRob, Roboter targetRob ) {
	source = sourceRob.getPos();
	target = targetRob.getPos();
	laserFacing=sourceRob.getAusrichtung();
	int laenge = calculateLaserLength(source, target, laserFacing); 
	laenge*=64;

	Color c = getRobColor(sourceRob.getName());
	if (soundActive) {
	    mLaserWav[laserWavCount%(mLaserWav.length)].play();
	    synchronized(this){
		try {
		    wait (50);
		}
		catch (InterruptedException ie){
		    System.err.println ("SACanvas.paint: wait interrupted");
		}
	    }
	}

	for(int i=1; i<=FULL_LENGTH_INT; i++) {
	    int tmp_laenge=(int) ((((double)i)/FULL_LENGTH_DOUBLE)*laenge);
	    Graphics2D g2 = (Graphics2D) getGraphics();
	    g2.scale( dScale, dScale );
	    paintActiveRobLaser(g2, tmp_laenge, c);	
		    
	     synchronized(this){
		 try {
		     wait (1);
		 }
		 catch (InterruptedException ie){
		     System.err.println ("SACanvas.paint: wait interrupted");
		 }
	     }
	}
	// drawRobLaser=false;
	if (soundActive) {
	    synchronized(this){
		try {
		    wait (200);
		}
		catch (InterruptedException ie){
		    System.err.println ("SACanvas.paint: wait interrupted");
		}
	    }
	    ++laserWavCount;
	}
	repaint();
	    
	
    }

    /** Berechnet die (Java-)Pixel-Koordinaten der linken oberen Ecke eines Bord-Feldes.
	Gibt die x- und y-Pixelwerte der linken oberen Ecke des Feldes
	mit der Position (x,y) auf dem Spielplan zurueck.
	@param x Die X-Koordinate des Feldes 
	@param y Die Y-Koordinate des Feldes 
	@return Die Position der linken oberen Ecke des Feldes als Java-Pixelwerte zum Zeichnen.
    */
	private Ort mapC2PixelNorthWest (int x, int y) {
	Ort pixel=new Ort();
	pixel.x=(x-1)*64;
	pixel.y=(sf.getSizeY()-y)*64;
	return pixel;
    }
    /** Berechnet die (Java-)Pixelwerte fuer den Mittelpunkt des Feldes.
	Genauer: Den Punkt (31,31) auf dem 64x64 grossen Feld mit Koordinaten
	zwischen 0 und 63.
    */ 
    private Ort mapC2PixelCenter (int x, int y) {
	Ort pixel=mapC2PixelNorthWest(x,y);
	pixel.x+=31;
	pixel.y+=31;
	return pixel;
    }

    private void paintActiveRobLaser(Graphics g, int actualLength, Color c) {
	// Laser sollen immer von Source nach Target gezeichnet werden
	
	int breite=4; // Die Breite des Lasers, sollte gerade sein
	int lSourceX=0;int lSourceY=0; // Anfangspunkt des Lasers in Pixeln,
	Ort tmp = mapC2PixelCenter(source.x, source.y); /* Mitte (Punkt (31,31) auf Feld
							   mit Punkten von 0 bis 63,
							   also einem 64x64 grossen Feld

							*/
	
	Graphics2D g2d = (Graphics2D) g;
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	g2d.setComposite( ac );
	g2d.setColor(c);
	switch (laserFacing) {
	case NORTH : {
	    lSourceX = tmp.x-(breite/2-1); 
	    lSourceY = tmp.y-actualLength;
	    g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
	    break;
	}
	case EAST : {
	    lSourceX = tmp.x;
	    lSourceY = tmp.y-(breite/2-1);;
	    g2d.fillRect(lSourceX,lSourceY,actualLength,breite);
	    break;
	}
	case SOUTH : {
	    lSourceX = tmp.x-(breite/2-1);
	    lSourceY = tmp.y;
	    g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
	    break;
	}
	case WEST : {
	    lSourceX = tmp.x-actualLength;
	    lSourceY = tmp.y-(breite/2-1);
	    g2d.fillRect(lSourceX,lSourceY,actualLength, breite);
	    break;
	}
	default : {
	    System.err.println("SACanvas.paintActiveRobLaser: ");
	    System.err.println("Ungueltige Laserrichtung: "+laserFacing);
	}	
	}// end switch facing
	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }
    


    /**
       Berechnet die Laenge eines Lasers (in Feldern) zwischen zwei Robotern.
       Bsp: Schiesst ein Roboter an Position (2,2) auf einen Roboter an 
       Position (5,2), so wird 3 zurueckgegeben
       (=> multipliziert man den Rueckgabewert mit 64, so erhaelt man die 
       zu zeichnende Laserlaenge in Pixeln).
       @param source Das Startfeld des Lasers
       @param target Das Feld des Ziels
       @param facing Die Richtung, in die der Laser schiesst (0=NORTH, 1=EAST, 2=SOUTH, 3=WEST)
                           
       @return Die Anzahl der Felder, ueber die der Laser geht (inklusive Startfeld).
       
    */
    private int calculateLaserLength(Ort source, Ort target, int facing){

	int laenge=0;
	laserFacing=facing;
	switch (laserFacing) {
	case NORTH : {
	    laenge = target.y - source.y;
	    break;
	}
	case EAST : {
	    laenge = target.x - source.x;
	    break;
	}
	case SOUTH : {
	    laenge = source.y - target.y;
	    break;
	}
	case WEST: {
	    laenge = source.x - target.x;
	    break;
	}
	default: {
	    System.err.println("SACanvas.calculateLaserLength(): ungueltige Laserrichtung: "+laserFacing);
	}
	}	
	//System.err.println("calculate Length: ("+source.x+","+source.y+")-"+facing+"->("+target.x+","+target.y+") ist "+laenge+" lang");
	return laenge;
    }
    private void paintActiveBordLaser (Graphics g, Color c,int actualLength) {
		
	Graphics2D g2d = (Graphics2D) g;
	//AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	//	g2d.setComposite( ac );
	g2d.setColor(c);
	
	int breite=4; // Die Breite des Lasers, sollte gerade sein
	int lSourceX=0;int lSourceY=0; // Anfangspunkt des Lasers in Pixeln,
	Ort tmp = mapC2PixelCenter(source.x, source.y); 
	
	switch (laserFacing) {
	case NORTH : {
	    lSourceX = tmp.x-(breite/2-1); 
	    lSourceY = tmp.y-actualLength+14;
	    g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
	    g2d.setColor(new Color(255,255,155));
	    g2d.drawRect(lSourceX,lSourceY,breite,actualLength);
	    break;
	}
	case EAST : {
	    lSourceX = tmp.x-17;
	    lSourceY = tmp.y-(breite/2-1);;
	    g2d.fillRect(lSourceX,lSourceY,actualLength,breite);
	    g2d.setColor(new Color(255,255,155));
	    g2d.drawRect(lSourceX,lSourceY,actualLength,breite);
	    break;
	}
	case SOUTH : {
	    lSourceX = tmp.x-(breite/2-1);
	    lSourceY = tmp.y-15;
	    g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
	    g2d.setColor(new Color(255,255,155));
	    g2d.drawRect(lSourceX,lSourceY,breite,actualLength);
	    break;
	}
	case WEST : {
	    lSourceX = tmp.x-actualLength+17;
	    lSourceY = tmp.y-(breite/2-1);
	    g2d.fillRect(lSourceX,lSourceY,actualLength-2, breite);
	    g2d.setColor(new Color(255,255,155));
	    g2d.drawRect(lSourceX,lSourceY,actualLength-2, breite);
	    break;
	}
	default : {
	    System.err.println("SACanvas.paintActiveRobLaser: ");
	    System.err.println("Ungueltige Laserrichtung: "+laserFacing);
	}	
	}// end switch facing
	
	//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }
    
    /**
       @param laserPos Die Koordinaten des schiessenden BordLasers
       @param laserDir Die Ausrichtung des Lasers
       @param targetRob Die Koordinaten des getroffenen Roboters
       @param surrounding Das ScrollPane in dem der Canvas dargestellt wird 
    */
    protected void doBordLaser(Ort laserPos, int laserDir,int strength, Ort targetRob,  JViewport surrounding) {
	// init laser values
	source=laserPos;
	target=targetRob;
	laserFacing=laserDir;
       	int laenge = calculateLaserLength(source, target, laserFacing); 
	laenge=laenge*64+17;
	Color c =laserColor[strength-1]; 
	
	// get viewable area
	//	Point upperLeftCorner = surrounding.getViewPosition();
	//  Dimension size = surrounding.getExtentSize();

	// Graphics g = getGraphics();
	//g.setClip(upperLeftCorner.x,upperLeftCorner.y,size.width,size.height);
	//activeBordLasers=true; // non-animated lasers will 
	//paint(g);              // be deleted now
	
	// paint lasers step by step
	for(int i=1; i<=FULL_LENGTH_INT; i++) {
	    int tmp_laenge=(int) ((((double)i)/FULL_LENGTH_DOUBLE)*laenge);    		    
	    Graphics2D g2 = (Graphics2D) getGraphics();
	    g2.scale( dScale, dScale );
	    paintActiveBordLaser(g2, c, tmp_laenge);
	    /*  synchronized(this){
		try {
		    wait (1);
		}
		catch (InterruptedException ie){
		    System.err.println ("SACanvas.doBordLaser: wait interrupted");
		}
		
		}*/
	}
	// activeBordLasers=false; // now paint the non-animated
	 repaint();              // lasers again
    }

   


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private boolean abbieger(int x, int y, int r){
	if (sf.bo(x,y).typ>=100 && (sf.bo(x,y).typ%10)==r)
	    return true;
	else
	    return false;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    protected void vorschau(int phasen, Roboter[] vorschauRobArray){
	if (phasen==0){
	    vorschauRob=null;
	    repaint();
	    return;
	}

	for (int i=1;i< phasen+1;i++)
	    sf.doPhase(i, vorschauRobArray);
	vorschauRob = vorschauRobArray[0];

	repaint();

    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty) {
	Boden boden = sf.bo(xpos, ypos);
	switch (boden.typ){
	    
	case (Spielfeld.BDGRUBE):
	    g.drawImage(diverseCrop[3],actx,acty,64,64,this);
	    break;
	case (Spielfeld.BDNORMAL):
		g.drawImage(diverseCrop[24+((xpos*ypos*19)%17)%4],actx,acty,64,64,this);
	    break;
	case (Spielfeld.BDDREHEL):
	    if (boden.spez==0)
		g.drawImage(diverseCrop[2],actx,acty,64,64,this);
	    else
		g.drawImage(diverseCrop[1],actx,acty,64,64,this);		
	    break;
	case (Spielfeld.BDREPA):
	    if (boden.spez==1)
		g.drawImage(diverseCrop[4],actx,acty,64,64,this);
	    else
		g.drawImage(diverseCrop[5],actx,acty,64,64,this);
	    break;
	    
	    // ------------------- normale Fliessbaender -------------------------
	    
	case (Spielfeld.FN1):g.drawImage(cbeltCrop[14],actx,acty,64,64,this);break;
	case (Spielfeld.FO1):g.drawImage(cbeltCrop[19],actx,acty,64,64,this);break;
	case (Spielfeld.FW1):g.drawImage(cbeltCrop[9],actx,acty,64,64,this);	break;
	case (Spielfeld.FS1):g.drawImage(cbeltCrop[4],actx,acty,64,64,this);	break;
	    
	case (Spielfeld.NVW1): if (abbieger(xpos,ypos-1,Spielfeld.NORD))
	    g.drawImage(cbeltCrop[15],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[6],actx,acty,64,64,this);break;
	case (Spielfeld.NVO1): if (abbieger(xpos,ypos-1,Spielfeld.NORD))
	    g.drawImage(cbeltCrop[18],actx,acty,64,64,this);						
	else g.drawImage(cbeltCrop[7],actx,acty,64,64,this);break;
	case (Spielfeld.SVW1): if (abbieger(xpos,ypos+1,Spielfeld.SUED))
	    g.drawImage(cbeltCrop[13],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[3],actx,acty,64,64,this);break;
	case (Spielfeld.SVO1):if (abbieger(xpos,ypos+1,Spielfeld.SUED))
			  g.drawImage(cbeltCrop[10],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[0],actx,acty,64,64,this);break;
	case (Spielfeld.OVN1):if (abbieger(xpos-1,ypos,Spielfeld.OST))
			  g.drawImage(cbeltCrop[16],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[5],actx,acty,64,64,this);break;
	case (Spielfeld.OVS1):if (abbieger(xpos-1,ypos,Spielfeld.OST))
			  g.drawImage(cbeltCrop[12],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[2],actx,acty,64,64,this);break;
	case (Spielfeld.WVN1):if (abbieger(xpos+1,ypos,Spielfeld.WEST))
			  g.drawImage(cbeltCrop[17],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[8],actx,acty,64,64,this);break;
	case (Spielfeld.WVS1):if (abbieger(xpos+1,ypos,Spielfeld.WEST))
			  g.drawImage(cbeltCrop[11],actx,acty,64,64,this);
	else g.drawImage(cbeltCrop[1],actx,acty,64,64,this);break;
	
	case (Spielfeld.NVWO1):g.drawImage(cbeltCrop[22],actx,acty,64,64,this);break;
	case (Spielfeld.SVWO1):g.drawImage(cbeltCrop[20],actx,acty,64,64,this);break;
	case (Spielfeld.OVNS1):g.drawImage(cbeltCrop[23],actx,acty,64,64,this);break;
	case (Spielfeld.WVNS1):g.drawImage(cbeltCrop[21],actx,acty,64,64,this);break;
	    
	    // ------------------------ Expressfliessbaender ---------------------
	    
	case (Spielfeld.FN2):g.drawImage(ebeltCrop[14],actx,acty,64,64,this);break;
	case (Spielfeld.FO2):g.drawImage(ebeltCrop[19],actx,acty,64,64,this);break;
	case (Spielfeld.FW2):g.drawImage(ebeltCrop[9],actx,acty,64,64,this);	break;
	case (Spielfeld.FS2):g.drawImage(ebeltCrop[4],actx,acty,64,64,this);	break;				
	    
	case (Spielfeld.NVW2): if (abbieger(xpos,ypos-1,Spielfeld.NORD))
	    g.drawImage(ebeltCrop[16],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[6],actx,acty,64,64,this);break;
	case (Spielfeld.NVO2): if (abbieger(xpos,ypos-1,Spielfeld.NORD))
	    g.drawImage(ebeltCrop[17],actx,acty,64,64,this);						
	else g.drawImage(ebeltCrop[7],actx,acty,64,64,this);break;
	case (Spielfeld.SVW2): if (abbieger(xpos,ypos+1,Spielfeld.SUED))
	    g.drawImage(ebeltCrop[13],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[3],actx,acty,64,64,this);break;
	case (Spielfeld.SVO2):if (abbieger(xpos,ypos+1,Spielfeld.SUED))
			  g.drawImage(ebeltCrop[10],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[0],actx,acty,64,64,this);break;
	case (Spielfeld.OVN2):if (abbieger(xpos-1,ypos,Spielfeld.OST))
			  g.drawImage(ebeltCrop[15],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[5],actx,acty,64,64,this);break;
	case (Spielfeld.OVS2):if (abbieger(xpos-1,ypos,Spielfeld.OST))
			  g.drawImage(ebeltCrop[12],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[2],actx,acty,64,64,this);break;
	case (Spielfeld.WVN2):if (abbieger(xpos+1,ypos,Spielfeld.WEST))
			  g.drawImage(ebeltCrop[18],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[8],actx,acty,64,64,this);break;
	case (Spielfeld.WVS2):if (abbieger(xpos+1,ypos,Spielfeld.WEST))
			  g.drawImage(ebeltCrop[11],actx,acty,64,64,this);
	else g.drawImage(ebeltCrop[1],actx,acty,64,64,this);break;
	
	
	case (Spielfeld.NVWO2):g.drawImage(ebeltCrop[22],actx,acty,64,64,this);break;
	case (Spielfeld.SVWO2):g.drawImage(ebeltCrop[20],actx,acty,64,64,this);break;
	case (Spielfeld.OVNS2):g.drawImage(ebeltCrop[23],actx,acty,64,64,this);break;
	case (Spielfeld.WVNS2):g.drawImage(ebeltCrop[21],actx,acty,64,64,this);break;
		
		
	default:
	}
    }

    private static final int[] crushlb_x = { 20, 30, 30, 30, 40 };
    private static final int[] crushlb_y = { 35, 25, 35, 45, 35 };
    private void paintCrusher(Graphics g, Boden boden, 
		      int actx, int acty) 
    {
	g.drawImage(diverseCrop[10],actx,acty,64,64,this);
	g.setColor(Color.white);
	for (int phasecount=1;phasecount<=5;phasecount++){
	    if (sf.isCrusherActive(boden.spez,phasecount)){
		int strx = actx + crushlb_x[phasecount-1];
		int stry = acty + crushlb_y[phasecount-1];
		g.drawString("" + phasecount,strx,stry);
	    }
	} //for
    }

    private void paintSpielfeldBoden( Graphics g ) {

	// Grenzen des zu zeichnenden Bereichs berechnen:
	Rectangle clip = g.getClipBounds();
	int x0 = clip.x / 64 + 1;
	int y0 = clip.y / 64 + 1;
	int x1 = (clip.x + clip.width - 1) / 64 + 1;
	int y1 = (clip.y + clip.height - 1) / 64 + 1;
	x1 = Math.min(x1, sf.getSizeX() );
	y1 = Math.min(y1, sf.getSizeY() );

	for(int hori=x0; hori <= x1; hori++) {
	    for(int vert = y0; vert <= y1; vert++) { 
		int actx = (hori-1) * 64;
		int acty = (vert-1) * 64;
		int xpos = hori;
		int ypos = sf.getSizeY() + 1 - vert;
		Boden boden = sf.bo(xpos, ypos);

		paintFeldBoden( g, xpos, ypos, actx, acty );
		if ((boden.typ>=100) && (boden.spez>0))
		    paintCrusher( g, boden, actx, acty);
	    }
	}
    }


   

    /** Hier werden die normalen Spielfeldlaser gezeichnet
     */
   private  void paintLaserStrahlen( Graphics g ) {
       Graphics2D dbg = (Graphics2D) g;
       AlphaComposite ac=null;
       //	if (activeBordLasers) 
       // ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
       //else 
       ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
       dbg.setComposite( ac );
       
	
       for (Enumeration e = sf.getLasers().elements(); e.hasMoreElements();){
	   actuallaser = ((LaserDef) e.nextElement());
	   int lx = actuallaser.x-1;
	   int ly = sf.getSizeY()-actuallaser.y;
	   int lf = actuallaser.facing;
	   int ll = actuallaser.length;
	   int ls = actuallaser.strength;
	   
	   switch ( ls ) {
	    case 1:
		dbg.setColor(Color.red.brighter());
		break;
	   case 2:
	       dbg.setColor(Color.orange);
	       break;
	   case 3:
	       dbg.setColor(Color.yellow);
	       break;
	   }
	   
	   switch ( lf ) {
	   case 0:
	       dbg.fillRect(lx*64+30,(ly-ll+1)*64,4,ll*64);
	       break;
	   case 1:
	       dbg.fillRect(lx*64,ly*64+30,ll*64,4);
	       break;
	   case 2:
	       dbg.fillRect(lx*64+30,ly*64,4,ll*64);
	       break;
	   case 3:
	       dbg.fillRect((lx-ll+1)*64,ly*64+30,ll*64,4);
	       break;
	   }
       }
       dbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
   }
    


    private void paintWaende( Graphics g ) {
	// Grenzen des zu zeichnenden Bereichs berechnen:
	Rectangle clip = g.getClipBounds();
	int x0 = clip.x / 64 + 1;
	int y0 = clip.y / 64 + 1;
	int x1 = (clip.x + clip.width - 1) / 64 + 1;
	int y1 = (clip.y + clip.height - 1) / 64 + 1;
	x1 = Math.min(x1, sf.getSizeX());
	y1 = Math.min(y1, sf.getSizeY());

	// Zeichnen
	for(int hori=x0; hori <= x1; hori++) {
	    for(int vert = y0; vert <= y1; vert++) { 
		int actx = hori*64-64;
		int acty = vert*64-64;
		int xpos = hori;
		int ypos = sf.getSizeY()-vert+1;
	       
		// Nordwand
		if (sf.nw(xpos,ypos).da){
		    if (sf.nw(xpos,ypos).wandEl[1]==1){
			g.drawImage(diverseCrop[15],actx,acty+5,64,64,this);
		    }
		    if (sf.nw(xpos,ypos).wandEl[1]==2){
			g.drawImage(diverseCrop[7],actx-1,acty+5,64,64,this);
			// ------------Beschriftung --------------------
			for (int phasecount=1;phasecount<=5;phasecount++){
			    if (sf.isPusherActive(sf.nw(xpos,ypos).spez[1],phasecount)){
				int strx = actx + 10*phasecount;
				g.setColor( (phasecount % 2) == 0 ?
					    Color.black : Color.yellow );
				g.drawString( ""+phasecount, strx-1, acty+29 );
			    }
			} //for

		    }
		    g.drawImage(diverseCrop[13],actx,acty-6,64,64,this);								
		}

		// Südwand
		if (sf.sw(xpos,ypos).da){
		    if (sf.sw(xpos,ypos).wandEl[0]==1){
			g.drawImage(diverseCrop[17],actx,acty-5,64,64,this);
		    }
		    if (sf.sw(xpos,ypos).wandEl[0]==2){
			g.drawImage(diverseCrop[8],actx,acty-5,64,64,this);
			// ------------Beschriftung --------------------
			for (int phasecount=1;phasecount<=5;phasecount++){
			    if (sf.isPusherActive(sf.sw(xpos,ypos).spez[0],phasecount)){
				int strx = actx + 10 * phasecount;
				g.setColor( (phasecount % 2) == 0 ?
					    Color.black : Color.yellow );
				g.drawString( ""+phasecount, strx-1, acty+42 );
			    }
			} //for
		    }
		    g.drawImage(diverseCrop[13],actx,acty+58,64,64,this);
		}

		// Ostwand
		if (sf.ow(xpos,ypos).da){
		    if (sf.ow(xpos,ypos).wandEl[0]==1){
			g.drawImage(diverseCrop[14],actx-6,acty,64,64,this);
		    }
		    if (sf.ow(xpos,ypos).wandEl[0]==2){
			g.drawImage(diverseCrop[6],actx-6,acty,64,64,this);
			// ------------Beschriftung --------------------
			for (int phasecount=1;phasecount<=5;phasecount++){
			    if (sf.isPusherActive(sf.ow(xpos,ypos).spez[0],phasecount)){
				int stry = acty + 10 * phasecount;
				g.setColor( (phasecount % 2) == 0 ?
					    Color.black : Color.yellow );
				g.drawString( ""+phasecount, actx+37, stry+4 );
			    }
			} //for
					
		    }
		    g.drawImage(diverseCrop[12],actx+57,acty,64,64,this);
		}
		
		// Westwand
		if (sf.ww(xpos,ypos).da){
		    if (sf.ww(xpos,ypos).wandEl[1]==1){
			g.drawImage(diverseCrop[16],actx+5,acty,64,64,this);
		    }
		    if (sf.ww(xpos,ypos).wandEl[1]==2){
			g.drawImage(diverseCrop[9],actx+4,acty,64,64,this);
			// ------------Beschriftung --------------------
			for (int phasecount=1;phasecount<=5;phasecount++){
			    if (sf.isPusherActive(sf.ww(xpos,ypos).spez[1],phasecount)){
				int stry = acty + 10 * phasecount;
				g.setColor( (phasecount % 2) == 0 ?
					    Color.black : Color.yellow );
				g.drawString( ""+phasecount, actx+24, stry+4 );
			    }
			} //for
					
		    }
		    g.drawImage(diverseCrop[12],actx-7,acty,64,64,this);
		}
	    }
	}			
    }

    private void paintFlaggen( Graphics g ) {
	if(sf.getFlaggen()!=null){
	    Ort[] flaggen = sf.getFlaggen();
	    for (int flaggencount = 0; flaggencount<flaggen.length;flaggencount++){
		int xflagge = flaggen[flaggencount].x-1;
		int yflagge = sf.getSizeY()-flaggen[flaggencount].y;
		g.drawImage(diverseCrop[18+flaggencount],
			    xflagge*64,yflagge*64,64,64,this);
	    }
	}
    }
    
    protected void paintScout( Graphics g ) {
	Graphics2D g2d = (Graphics2D) g;
	if( vorschauRob == null )
	    return;

    	    int xpos = vorschauRob.getX()-1;
	    int ypos = sf.getSizeY()-vorschauRob.getY();
	    int xpos64 = xpos*64;
	    int ypos64 = ypos*64;
	    // Scout
	    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	    g2d.setComposite( ac );
	    g.drawImage(scoutCrop[vorschauRob.getAusrichtung()],xpos64,ypos64,64,64,this);
	    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }

    private void paintRobos( Graphics g ) {
	Graphics2D g2d = (Graphics2D) g;
	if (robos!=null){	
	    for (int robocount=0;robocount<robos.length;robocount++){
		if((robos[robocount].getSchaden()<10)&&
		   (robos[robocount].getLeben() > 0)) {
		    int xpos = robos[robocount].getX()-1;
		    int ypos = sf.getSizeY()-robos[robocount].getY();
		    int xpos64 = xpos*64;
		    int ypos64 = ypos*64;
		    int actx = xpos64-64;
		    int acty = ypos64-64;
		    Image imgRob = robosCrop2[robos[robocount].getAusrichtung()+robocount*4];
		    boolean virtuell = robos[robocount].istVirtuell();

		    if( imgRob != null ) {
			if( virtuell ) {
			    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			    g2d.setComposite( ac );
			}
			g2d.drawImage(imgRob,xpos64,ypos64,64,64,this);
			if( virtuell ) {
			    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			}
			String beschriftung = "" + robos[robocount].getName();
			g2d.setColor( robocolor2[robocount] );
			g2d.drawString(beschriftung,xpos64,ypos64+8+robocount*8);
		    }
		}	
	    }
	}
    }

    private void createOffscreenImage() {
	// XXX vielleicht besser das skalieren erst beim reinkopieren 
	dbi = createImage(x,y);
	g_off = (Graphics2D)dbi.getGraphics();
	g_off.setFont(new Font(g_off.getFont().getName(),g_off.getFont().getStyle(),8));
	g_off.setClip(0,0,x,y);
	g_off.scale( dScale, dScale );
    }

    Graphics2D g_off;
    public void paint(Graphics g) {
	if(dbi == null || rescaled ) {
	    createOffscreenImage();
	    rescaled = false;
	}
	Graphics2D dbg = (Graphics2D)g_off;
	Rectangle clip = g.getClipBounds();
	//dbg.setClip( clip );

	paintSpielfeldBoden( dbg );
	paintLaserStrahlen( dbg );
	paintWaende( dbg );
	paintFlaggen( dbg );
	paintScout( dbg );
	paintRobos( dbg );

	Rectangle rc = g.getClipBounds();
	int x0 = rc.x, 
	    y0 = rc.y,
	    x1 = rc.x + rc.width,
	    y1 = rc.y + rc.height;
	g.drawImage( dbi, 0, 0, this );
	//g.drawImage(dbi, x0, y0, x1, y1, x0, y0, x1, y1, this);
    }

    private void paintUnbuffered(Graphics dbg) {
	paintSpielfeldBoden( dbg );
	paintLaserStrahlen( dbg );
	paintWaende( dbg );
	paintFlaggen( dbg );
	paintScout( dbg );
	paintRobos( dbg );
    }

    protected void finalize() {
	g_off.dispose();
    }

    public void update(Graphics g){
	paint(g);
    }

    public Image getThumb(int size) {
	/*
	BufferedImage bi = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
	
	Graphics2D g2 = bi.createGraphics();
	g2.setClip(0,0,x,y);
	//paint( g_off );
	

	paintUnbuffered( g2 );
	g2.dispose();

	AffineTransformOp atop = 
	    new AffineTransformOp(AffineTransform.getScaleInstance(((float)size)/x, ((float)size)/y), AffineTransformOp.TYPE_BILINEAR);
	BufferedImage thumb = atop.filter( bi, null );
							   
	//Image thumb=createImage(new FilteredImageSource(dbi.getSource(), new AreaAveragingScaleFilter(size, size)));
	g2.dispose();
	return thumb;
	*/

	BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
	
	Graphics2D g2 = bi.createGraphics();
	g2.setClip(0,0,size,size);
	g2.scale( ((double) size) / x, ((double) size) / y );
	paintUnbuffered( g2 );
	g2.dispose();

	return bi;
    }

    private void ersetzeSpielfeld(SpielfeldSim sfs){
	sf = sfs;
	x = (int) (sf.getSizeX() * FELDSIZE * dScale);
	y = (int) (sf.getSizeY() * FELDSIZE * dScale);
	setSize(x,y);
    }

    private static SACanvas sac = null;
    public static Image createThumb(SpielfeldSim sim, int size) {
	if( sac == null ) {
	    sac = new SACanvas(sim); 
	} else {
	    sac.ersetzeSpielfeld( sim );
	}
	return sac.getThumb(size);
    }

}

