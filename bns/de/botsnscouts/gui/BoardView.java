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

import org.apache.log4j.Category;

import de.botsnscouts.board.*;
import de.botsnscouts.util.*;

import de.botsnscouts.*;
/**
 * Board-Ausgabe-Canvas ist das Objekt, das der Ausgabe und dem menschlichen Spieler das Board grafisch darstellt und verwaltet
 * @author urspr\uFFFDnglich Daniel Holtz
 * @version Verbesserung von 1.0

 * changes: enno v1.23
 * 1. beim painten wird nur noch der teil ins sichtbare kopiert, der auch
 *   in gepaintet werden muss (hat aber nicht viel gebracht)
 * 2. Felder, die nicht wirklich neu gezeichnet werden m\uFFFDssen, werden nicht
 *    mehr betrachtet (bringt was!)
 * 3. Code insgesamt lesbarer strukturiert (noch nicht ganz beendet)
 * 4. makePixelArray in eigene Methode ausgelagert
 * 5. lokale Variablen eingef\uFFFDhrt ...
 *
 * 6. Verwaltung und Speicherung der Bilder ausgelagert in
 *    Klasse ImageMan (kann so dann auch vom KachelEditor verwendet werden)
 *    bilder werden dann nur einmal pro JVM geladen, und zwar bei Programm-
 *    start (StartSpieler) im Hintergrund. Der erste Spielfeldaufbau ist
 *    damit viel schneller, ebenso die Board-Vorschau
 */

public class BoardView extends JComponent {
    static Category CAT = Category.getInstance(BoardView.class);

    // inner classes
    public static interface ClickListener {
	void feldClicked( int x, int y, int modifiers );
    }

    /** Constant for direction/facing north*/
    protected static final int NORTH=0;
    /** Constant for direction/facing east*/
    protected static final int EAST=1;
    /** Constant for direction/facing south*/
    protected static final int SOUTH=2;
    /** Constant for direction/facing west*/
    protected static final int WEST=3;


    private JScrollPane myScrollPane;

   /** size (length and width) of one little field in pixels*/
    protected static final int FELDSIZE = 64;

    /**Number of single steps a laser animation is drawn.*/
    private static final int FULL_LENGTH_INT=30;

    /**Number of single steps a laser animation is drawn.*/
    private static final double FULL_LENGTH_DOUBLE=30.0;


    // for painting active Lasers
    /** position of firing robot*/
    private Location source;
    /**position of robot hit*/
    private Location target;
    /** facing (direction) of the laser, according to the directions above*/
    private int laserFacing;
    private boolean activeBordLasers;

    /** contains colors of the boardlasers, strength 1 to 3*/
    static final Color[] laserColor = { Color.red.brighter(),//strength 1
					Color.orange,//strength 2
					Color.yellow };//strength 3

    /** The color used for the background of active lasers. */
    private final static Color sndLaserColor = new Color(255,255,155);

    /** To lookup the color of a robot; contains name->color mapping.*/
    private java.util.Hashtable nameToColorHash;
    private boolean gotColors;

    /** where are the sounds, if we are not using getResource() to find out */
    //  private static final String SOUND_DIR="de/botsnscouts/sounds";
    /** used to select the file(types) we want*/
    // private static SoundFileFilter soundFilter = new SoundFileFilter();


    private Image dbi;
    /** some board elements..*/
    private Image[] cbeltCrop,ebeltCrop,diverseCrop,robosCrop,scoutCrop;//,robosCrop2;

    private int x,y;

    /** Stores data of the robots.*/
    private Bot[] robos;
    /** This robot is used for calculations,
     *  like making a suggestion for the next move.
     */
    private Bot vorschauRob;

    /** last position of our famous scout ;-) */
    private Location lastScoutPos = new Location();
    // Let's define some colors, so that everybody uses the same..
    public static final Color YELLOW = BotVis.YELLOW;
    public static final Color RED    = BotVis.RED;
    public static final Color BLUE   = BotVis.BLUE;
    public static final Color ROSA   = BotVis.ROSA;
    public static final Color ORANGE = BotVis.ORANGE;
    public static final Color GRAY   = BotVis.GRAY;
    public static final Color VIOLET = BotVis.VIOLET;
    public static final Color GREEN  = BotVis.GREEN;


    //  public static final Color[] robocolor = { Color.green, Color.yellow, Color.red,Color.blue, Color.magenta, Color.orange, Color.gray, Color.magenta.darker()};
    /** Die Farben der Bot*/
    public static final Color [] robocolor = {GREEN,YELLOW,RED,BLUE,ROSA,ORANGE,GRAY,VIOLET};

    /** gameboard object;
     *  stores the information about the board we are playing on;
     *  (where are the pits, where are lasers, and so on..)
     */
    SimBoard sf;

    /** scale factor for zooming*/
    private double dScale = 1.0;
    boolean rescaled = true;

    protected double scaledFeldSize; // FELDSIZE * scale

    /** position to highlight*/
    Location highlightPos = new Location(0,0);

    ClickListener myClickListener;


     /** Number of pixels a robot will be moved in a single animation step.
     *  Has to be between 1 and FELDSIZE.
     *  => Number of steps a one-field-move is drawn = FELDSIZE/MOVE_ROB_ANIMATION_OFFSET
     *
     *   @todo I guess that this must not be final and needs to be scaled so that it works
     *    with different zoomlevels
     */
    private static final int MOVE_ROB_ANIMATION_OFFSET=8;
    /** Amount of time (in ms) we wait after a single animation step
     *  of (pixel-)length MOVE_ROB_ANIMATION_OFFSET.
     */
    private static final int MOVE_ROB_ANIMATION_DELAY=5;



    public double getScale() {
	return dScale;
    }

    public void setScale( double scale ) {
	// adapt this Component to the scaling factor
	dScale = scale;
	scaledFeldSize = (dScale * FELDSIZE);
	x = (int)(sf.getSizeX() * scaledFeldSize );
	y = (int)(sf.getSizeY() * scaledFeldSize );
	rescaled = true;
	CAT.debug("dim : " + x + " " + y );
	setSize(x,y);

	// the preComputed-BoardImage is no longer valid
	preBoard = null;


	//invalidate();
    }


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public BoardView(SimBoard sf_neu){
	init( sf_neu, robocolor );
    }

    public BoardView(SimBoard sf_neu, Color [] robColors){
	init( sf_neu, robColors );
	mouseInit();
    }








    private void init(SimBoard sf_neu, Color [] robColors) {
	//robocolor2=robColors;
	//	drawRobLaser=false;
	//drawBordLaser=false;
	activeBordLasers=false;
	gotColors=false;
	sf=sf_neu;
	//x=(sf.floor.length-2)*64;
	//y=(sf.floor[0].length-2)*64;
	setDoubleBuffered( true );
	setScale( dScale ); // does setSize()
	//setSize(x,y);


	ImageMan.finishLoading();

	ebeltCrop   = ImageMan.getImages( ImageMan.EBELTS );
	cbeltCrop   = ImageMan.getImages( ImageMan.CBELTS );
	diverseCrop = ImageMan.getImages( ImageMan.DIVERSE );
	robosCrop   = ImageMan.getImages( ImageMan.ROBOS );
	//robosCrop2  = robosCrop;
	scoutCrop   = ImageMan.getImages( ImageMan.SCOUT );
    }


    void setScrollPane(JScrollPane j) {
	myScrollPane = j;
    }

    Point calcKachelPos(int mx, int my) {
	int sfh = sf.getSizeY();
	int sfw = sf.getSizeX();

	Point p = new Point();
	p.x = 1 + (int) (mx / scaledFeldSize );
	p.y = sfh - (int) (my / scaledFeldSize );

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
    private void setRobColors (Bot[] robs) {
	gotColors=true;
	nameToColorHash = new java.util.Hashtable();
	for (int i=0;i<robs.length;i++)
	    if (robs[i]==null)
		break;
	    else
		nameToColorHash.put (robs[i].getName(), robocolor[robs[i].getBotVis()]);
    }


    /** Lookup the Bot's color (by name)
	@param name The Bot's name
	@return The Bot's color. If the name is unknown, Color.white will be returned,
    */
    private Color getRobColor (String name){
	Color foo=null;
	foo= (Color) nameToColorHash.get(name);
	if (foo==null) {
	    CAT.error ("getRobColor: Color for "+name+"'s Laser not found");
	    return Color.white;
	}
	else return foo;
    }


    private HashMap internalPositionHash = new java.util.HashMap();

    private static final Location pit = new Location(0,0);
    protected void ersetzeRobos(Bot[] robos_neu){

	if (!gotColors) { // jetzt bekomme ich zum erstenmal die Bot
	    setRobColors(robos_neu);
	    robos=robos_neu;
	}
	// we dont want to overwrite the robots positions, because they
	// have been updated in animateRobMove() before;
	// animateRonMove() gets informed earlier, so overwriting the positions
	// would reset the robot back to a position he has already left
	else {
          if (Ausgabe.enableRobMoveAnimation) {
	    for (int i=0;i<robos.length;i++) // saving my internal robot positions
		internalPositionHash.put(robos[i].getName(), robos[i].getPos());
	    robos=robos_neu; // updating all robots

            // replacing robot positions - if it was not destroyed -
	    // with the positions we saved above


	    for (int i=0;i<robos.length;i++) {
		Bot r = robos[i];
		Location tmp = (Location) internalPositionHash.get(r.getName());

		if (!(r.getPos().equals(pit) || r.getDamage()>=10 || tmp.equals(pit))){
		                                                     // ^^^^^^^^^^^^^
                    		                                     // otherwise we would not show
 		                                                     // the destroyed robot ever again
		                                                     // as we would ignore him if he
		                                                     // is placed on the board again
		    if (CAT.isDebugEnabled()){
			CAT.debug("ignoring server position of robot "+r.getName()
				  +" as my calculated position will be more accurate");
		    }
		    // use the internal kept position of our robot unless it is not
		    // destroyed
		    r.setPos(tmp);
		}
		else {
		    if (CAT.isDebugEnabled())
			CAT.debug ("using server position of robot "+r.getName());
		}

	    }
          }
          else { // no animation
            robos=robos_neu;
          }
	}

	repaint();


    }


    /////////////////////////////////////////////////////////////////////

    private void moveRobNorth(Bot internal, int robocount){
      if (CAT.isDebugEnabled())
           CAT.debug("Move direction: NORTH");
      if (MOVE_ROB_ANIMATION_DELAY>0){
         synchronized (this){
           for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
              // paintRobos(this.getGraphics(), internal);
              paintRobot((Graphics2D)this.getGraphics(), internal, robocount, 0,offset, true);
                try {wait (MOVE_ROB_ANIMATION_DELAY);}
               catch (InterruptedException ie){CAT.warn ("SACanvas.paint: wait int moveRobNorth interrupted");}
           }
         }
       }
       else {// paint without delay
          for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
             //paintRobos(this.getGraphics(), internal);
            paintRobot((Graphics2D)this.getGraphics(), internal, robocount,0, offset,true);
          }
       }
    }
    private void moveRobSouth(Bot internal, int robocount){
      if (CAT.isDebugEnabled())
         CAT.debug("Move direction: SOUTH");
      if (MOVE_ROB_ANIMATION_DELAY>0){
         synchronized (this){
            for (int offset=0;offset<=64;offset+=MOVE_ROB_ANIMATION_OFFSET){
                // paintRobos(this.getGraphics(), internal);
                paintRobot((Graphics2D)this.getGraphics(), internal, robocount, 0,offset, true);
                  try {wait (MOVE_ROB_ANIMATION_DELAY);}
                  catch (InterruptedException ie){CAT.warn ("SACanvas.paint: wait in moveRobSouthinterrupted");}
            }
         }
      }
      else {// paint without delay
        for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
         //  paintRobos(this.getGraphics(), internal);
          paintRobot((Graphics2D)this.getGraphics(), internal, robocount,0,offset, true);
        }
      }
    }
     private void moveRobEast(Bot internal, int robocount){
      if (CAT.isDebugEnabled())
                CAT.debug("Move direction: EAST");
      if (MOVE_ROB_ANIMATION_DELAY>0){
        synchronized (this){
           for (int offset=0;offset<=64;offset+=MOVE_ROB_ANIMATION_OFFSET){
         //     paintRobos(this.getGraphics(), internal);
              paintRobot((Graphics2D)this.getGraphics(), internal, robocount,offset,0, true);
                try {wait (MOVE_ROB_ANIMATION_DELAY);}
                catch (InterruptedException ie){CAT.warn ("SACanvas.paint: wait in moveRobSouthinterrupted");}
           }
        }
      }
      else {// paint without delay
        for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
        //  paintRobos(this.getGraphics(), internal);
          paintRobot((Graphics2D)this.getGraphics(), internal, robocount,offset,0, true);
        }
      }
    }
    private void moveRobWest(Bot internal, int robocount){
      if (CAT.isDebugEnabled())
         CAT.debug("Move direction: WEST");
      if (MOVE_ROB_ANIMATION_DELAY>0){
         synchronized (this){
           for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
         //      paintRobos(this.getGraphics(), internal);
              paintRobot((Graphics2D)this.getGraphics(), internal, robocount,offset,0, true);
                try {wait (MOVE_ROB_ANIMATION_DELAY);}
                catch (InterruptedException ie){CAT.warn ("SACanvas.paint: wait int moveRobNorth interrupted");}
           }
         }
      }
      else { // paint without delay
        for (int offset=0;offset>=-64;offset-=MOVE_ROB_ANIMATION_OFFSET){
    //     paintRobos(this.getGraphics(), internal);
          paintRobot((Graphics2D)this.getGraphics(), internal, robocount,offset,0, true);
        }
      }
    }


      protected void animateRobMove(Bot rob, int direction){
        // important: according to the code on SpielfeldSim we do not get
        //            the updated robot position;
        //            the updated position will be the endposition of the total move,
        //            as ersetzeRobos() will be called when the robot has reached its
        //            final position
        //            THIS METHOD will be called for each single step of a move
        //             (i.e. three times for a "Move 3 forward")
        //            So we have to update our internal position of the robot in
        //            between to show an animation that makes sense

        String name = rob.getName();
        Bot internal = null;
        int robocount=-1;
        for (int i=0;i<robos.length;i++){
          if (robos[i].getName().equals(name)){
            internal = robos[i];
            robocount = i;
            break;
          }
        }
        int oldX = internal.getX();
        int oldY = internal.getY();

       // paint the move animation  and update the position in my internal robot array
        switch (direction){
          case NORTH: {
              if (oldY<sf.getSizeY()){
                moveRobNorth(internal, robocount);
                internal.setPos(oldX, oldY+1);
              }
              return;
          }
          case EAST: {
            if (oldX<sf.getSizeX()){
              moveRobEast(internal, robocount);
              internal.setPos(oldX+1, oldY);
            }
            return;
          }
          case WEST:{
            if (oldX>1) {
              moveRobWest(internal, robocount);
              internal.setPos(oldX-1, oldY);
            }
            return;
          }
          case SOUTH:{
            if (oldY>1){
              moveRobSouth(internal, robocount);
              internal.setPos(oldX, oldY-1);
            }
            return;
          }
          default:{
            // this must not happen,
            // otherwise the whole gui might be useless as it keeps probably
            // a wrong position for one robot
            CAT.fatal("Got illgeal direction for animating robot");
          }
        }
    }


    /**
     * Draws animated robot lasers.
       @param sourceRob position of firing robot
       @param targetRob position of the robot hit
    */

    public void doRobLaser (Bot sourceRob, Bot targetRob ) {
       //  allDone = false;
	if (CAT.isDebugEnabled())
	    CAT.debug("doRobLaser: "+sourceRob.getName()+" -> "+targetRob.getName());
	source = sourceRob.getPos();
	target = targetRob.getPos();
	laserFacing=sourceRob.getFacing();
	int laenge = calculateLaserLength(source, target, laserFacing);
	laenge*=64;

        String name = sourceRob.getName();

        Color c = getRobColor(name);
        SoundMan.playSound(BotVis.getBotLaserSoundByName(name));
        synchronized(this){
	  try {
	    wait (50);
          }
	  catch (InterruptedException ie){
	    CAT.error("BoardView.paint: wait interrupted");
	  }


          for(int i=1; i<=FULL_LENGTH_INT; i++) {
              int tmp_laenge=(int) ((((double)i)/FULL_LENGTH_DOUBLE)*laenge);
              Graphics2D g2 = (Graphics2D) getGraphics();
              g2.scale( dScale, dScale );
              paintActiveRobLaser(g2, tmp_laenge, c);

          //     synchronized(this){
                   try {
                       wait (1);
                   }
                   catch (InterruptedException ie){
                       CAT.error ("BoardView.paint: wait interrupted");
                   }
            //   }
          }
        }

          // drawRobLaser=false;
          if (SoundMan.isSoundActive()) {
             // SoundMan.playSound(SoundMan.BUMM);
              synchronized(this){
                  try {
                      wait (200);
                  }
                  catch (InterruptedException ie){
                      CAT.error ("BoardView.paint: wait interrupted");
                  }
              }
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
	private Location mapC2PixelNorthWest (int x, int y) {
	Location pixel=new Location();
	pixel.x=(x-1)*64;
	pixel.y=(sf.getSizeY()-y)*64;
	return pixel;
    }
    /** Berechnet die (Java-)Pixelwerte fuer den Mittelpunkt des Feldes.
	Genauer: Den Punkt (31,31) auf dem 64x64 grossen Feld mit Koordinaten
	zwischen 0 und 63.
    */
    private Location mapC2PixelCenter (int x, int y) {
	Location pixel=mapC2PixelNorthWest(x,y);
	pixel.x+=31;
	pixel.y+=31;
	return pixel;
    }

    private void paintActiveRobLaser(Graphics g, int actualLength, Color c) {
	// Laser sollen immer von Source nach Target gezeichnet werden

	int breite=4; // Die Breite des Lasers, sollte gerade sein
	int lSourceX=0;int lSourceY=0; // Anfangspunkt des Lasers in Pixeln,
	Location tmp = mapC2PixelCenter(source.x, source.y); /* Mitte (Punkt (31,31) auf Feld
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
	    CAT.error("BoardView.paintActiveRobLaser: ");
	    CAT.error("Ungueltige Laserrichtung: "+laserFacing);
	}
	}// end switch facing
	g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }



    /**
       Berechnet die Laenge eines Lasers (in Feldern) zwischen zwei Botn.
       Bsp: Schiesst ein Bot an Position (2,2) auf einen Bot an
       Position (5,2), so wird 3 zurueckgegeben
       (=> multipliziert man den Rueckgabewert mit 64, so erhaelt man die
       zu zeichnende Laserlaenge in Pixeln).
       @param source Das Startfeld des Lasers
       @param target Das Feld des Ziels
       @param facing Die Richtung, in die der Laser schiesst (0=NORTH, 1=EAST, 2=SOUTH, 3=WEST)

       @return Die Anzahl der Felder, ueber die der Laser geht (inklusive Startfeld).

    */
    private int calculateLaserLength(Location source, Location target, int facing){

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
	    CAT.error("BoardView.calculateLaserLength(): ungueltige Laserrichtung: "+laserFacing);
	}
	}
	//System.err.println("calculate Length: ("+source.x+","+source.y+")-"+facing+"->("+target.x+","+target.y+") ist "+laenge+" lang");
	return laenge;
    }
    private void paintActiveBordLaser (Graphics g, Color c,int actualLength) {

	Graphics2D g2d = (Graphics2D) g;
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER/*, 0.5f*/);
	g2d.setComposite( ac );
	g2d.setColor(c);

	int breite=4; // Die Breite des Lasers, sollte gerade sein
	int lSourceX=0;int lSourceY=0; // Anfangspunkt des Lasers in Pixeln,
	Location tmp = mapC2PixelCenter(source.x, source.y);
       // synchronized (lock) {
          switch (laserFacing) {
          case NORTH : {
              lSourceX = tmp.x-(breite/2-1);
              lSourceY = tmp.y-actualLength+14;
              g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
              g2d.setColor(sndLaserColor);
              g2d.drawRect(lSourceX,lSourceY,breite,actualLength);
              break;
          }
          case EAST : {
              lSourceX = tmp.x-17;
              lSourceY = tmp.y-(breite/2-1);;
              g2d.fillRect(lSourceX,lSourceY,actualLength,breite);
              g2d.setColor(sndLaserColor);
              g2d.drawRect(lSourceX,lSourceY,actualLength,breite);
              break;
          }
          case SOUTH : {
              lSourceX = tmp.x-(breite/2-1);
              lSourceY = tmp.y-15;
              g2d.fillRect(lSourceX,lSourceY,breite,actualLength);
              g2d.setColor(sndLaserColor);
              g2d.drawRect(lSourceX,lSourceY,breite,actualLength);
              break;
          }
          case WEST : {
              lSourceX = tmp.x-actualLength+17;
              lSourceY = tmp.y-(breite/2-1);
              g2d.fillRect(lSourceX,lSourceY,actualLength-2, breite);
              g2d.setColor(sndLaserColor);
              g2d.drawRect(lSourceX,lSourceY,actualLength-2, breite);
              break;
          }
          default : {
              CAT.error("BoardView.paintActiveRobLaser: ");
              CAT.error("Ungueltige Laserrichtung: "+laserFacing);
          }
          }// end switch facing
       //   allDone = true;
       //   lock.notifyAll();
       // }
	//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }

    /**
       @param laserPos Die Koordinaten des schiessenden BordLasers
       @param laserDir Die Ausrichtung des Lasers
       @param targetRob Die Koordinaten des getroffenen Bots
       @param surrounding Das ScrollPane in dem der Canvas dargestellt wird
    */
    protected void doBordLaser(Location laserPos, int laserDir,int strength, Location targetRob,  JViewport surrounding) {
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
		    System.err.println ("BoardView.doBordLaser: wait interrupted");
		}

		}*/
	}
	// activeBordLasers=false; // now paint the non-animated
	 repaint();              // lasers again
    }




    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private boolean abbieger(int x, int y, int r){
        Floor floor = sf.floor(x,y);
	return floor.isBelt() && (floor.getBeltDirection() == r);
    }

    protected void vorschau(int phasen, Bot simRob){
	if (phasen==0){
	    //scoutOn = true; // flag for repaint: yes, paint scout!
	    vorschauRob=null;
	    deleteScout();
	    //repaint();
	    return;
	}

	Bot[] robs = new Bot[1];
	robs[0] = simRob;
	for (int i=1;i< phasen+1;i++) {
	    //sf.doPhase(phasen, simRob);
	    sf.doPhase(phasen, robs);
	}
	//vorschauRob = vorschauRobArray[0];
	vorschauRob = simRob;
	showScout( simRob.getPos() );

	//repaint();

    }

    protected void vorschau(int phasen, Bot[] vorschauRobArray){
	if (phasen==0){
	    vorschauRob=null;
	    deleteScout();
	    //repaint();
	    return;
	}

	for (int i=1;i< phasen+1;i++) {
	    sf.doPhase(i, vorschauRobArray);
	}
	vorschauRob = vorschauRobArray[0];
	showScout( vorschauRob.getPos() );
	//repaint();

    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty) {
       paintFeldBoden(g, xpos, ypos,  actx, acty, 64, 64);
    }

    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty,
				  int width, int height) {
        Floor floor = sf.floor(xpos, ypos);
	switch ( floor.getType() ){

	case (Board.FL_PIT):
	    g.drawImage(diverseCrop[3],actx,acty,width,height,this);
	    break;
	case (Board.FL_NORMAL):
		g.drawImage(diverseCrop[24+((xpos*ypos*19)%17)%4],actx,acty,width,height,this);
	    break;
	case (Board.FL_ROTGEAR):
	    if (floor.getInfo()==0)
		g.drawImage(diverseCrop[2],actx,acty,width,height, this);
	    else
		g.drawImage(diverseCrop[1],actx,acty,width,height,this);
	    break;
	case (Board.FL_REPAIR):
	    if (floor.getInfo()==1)
		g.drawImage(diverseCrop[4],actx,acty,width,height,this);
	    else
		g.drawImage(diverseCrop[5],actx,acty,width,height,this);
	    break;

	    // ------------------- normale Fliessbaender -------------------------

	case (Board.FN1):g.drawImage(cbeltCrop[14],actx,acty,width,height,this);break;
	case (Board.FE1):g.drawImage(cbeltCrop[19],actx,acty,width,height,this);break;
	case (Board.FW1):g.drawImage(cbeltCrop[9],actx,acty,width,height,this);	break;
	case (Board.FS1):g.drawImage(cbeltCrop[4],actx,acty,width,height,this);	break;

	case (Board.NFW1): if (abbieger(xpos,ypos-1,Board.NORD))
	    g.drawImage(cbeltCrop[15],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[6],actx,acty,width,height,this);break;
	case (Board.NFE1): if (abbieger(xpos,ypos-1,Board.NORD))
	    g.drawImage(cbeltCrop[18],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[7],actx,acty,width,height,this);break;
	case (Board.SFW1): if (abbieger(xpos,ypos+1,Board.SUED))
	    g.drawImage(cbeltCrop[13],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[3],actx,acty,width,height,this);break;
	case (Board.SFE1):if (abbieger(xpos,ypos+1,Board.SUED))
			  g.drawImage(cbeltCrop[10],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[0],actx,acty,width,height,this);break;
	case (Board.EFN1):if (abbieger(xpos-1,ypos,Board.OST))
			  g.drawImage(cbeltCrop[16],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[5],actx,acty,width,height,this);break;
	case (Board.EFS1):if (abbieger(xpos-1,ypos,Board.OST))
			  g.drawImage(cbeltCrop[12],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[2],actx,acty,width,height,this);break;
	case (Board.WFN1):if (abbieger(xpos+1,ypos,Board.WEST))
			  g.drawImage(cbeltCrop[17],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[8],actx,acty,width,height,this);break;
	case (Board.WFS1):if (abbieger(xpos+1,ypos,Board.WEST))
			  g.drawImage(cbeltCrop[11],actx,acty,width,height,this);
	else g.drawImage(cbeltCrop[1],actx,acty,width,height,this);break;

	case (Board.NFEW1):g.drawImage(cbeltCrop[22],actx,acty,width,height,this);break;
	case (Board.SFWE1):g.drawImage(cbeltCrop[20],actx,acty,width,height,this);break;
	case (Board.EFNS1):g.drawImage(cbeltCrop[23],actx,acty,width,height,this);break;
	case (Board.WFNS1):g.drawImage(cbeltCrop[21],actx,acty,width,height,this);break;

	    // ------------------------ Expressfliessbaender ---------------------

	case (Board.FN2):g.drawImage(ebeltCrop[14],actx,acty,width,height,this);break;
	case (Board.FE2):g.drawImage(ebeltCrop[19],actx,acty,width,height,this);break;
	case (Board.FW2):g.drawImage(ebeltCrop[9],actx,acty,width,height,this);break;
	case (Board.FS2):g.drawImage(ebeltCrop[4],actx,acty,width,height,this);break;

	case (Board.NFW2): if (abbieger(xpos,ypos-1,Board.NORD))
	    g.drawImage(ebeltCrop[16],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[6],actx,acty,width,height,this);break;
	case (Board.NFE2): if (abbieger(xpos,ypos-1,Board.NORD))
	    g.drawImage(ebeltCrop[17],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[7],actx,acty,width,height,this);break;
	case (Board.SFW2): if (abbieger(xpos,ypos+1,Board.SUED))
	    g.drawImage(ebeltCrop[13],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[3],actx,acty,width,height,this);break;
	case (Board.SFE2):if (abbieger(xpos,ypos+1,Board.SUED))
			  g.drawImage(ebeltCrop[10],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[0],actx,acty,width,height,this);break;
	case (Board.EFN2):if (abbieger(xpos-1,ypos,Board.OST))
			  g.drawImage(ebeltCrop[15],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[5],actx,acty,width,height,this);break;
	case (Board.EFS2):if (abbieger(xpos-1,ypos,Board.OST))
			  g.drawImage(ebeltCrop[12],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[2],actx,acty,width,height,this);break;
	case (Board.WFN2):if (abbieger(xpos+1,ypos,Board.WEST))
			  g.drawImage(ebeltCrop[18],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[8],actx,acty,width,height,this);break;
	case (Board.WFS2):if (abbieger(xpos+1,ypos,Board.WEST))
			  g.drawImage(ebeltCrop[11],actx,acty,width,height,this);
	else g.drawImage(ebeltCrop[1],actx,acty,width,height,this);break;


	case (Board.NFWE2):g.drawImage(ebeltCrop[22],actx,acty,width,height,this);break;
	case (Board.SFWO2):g.drawImage(ebeltCrop[20],actx,acty,width,height,this);break;
	case (Board.EFNS2):g.drawImage(ebeltCrop[23],actx,acty,width,height,this);break;
	case (Board.WFNS2):g.drawImage(ebeltCrop[21],actx,acty,width,height,this);break;


	default:
	}
    }

    // for painting crushers
    private static final int[] crushlb_x = { 20, 30, 30, 30, 40 };
    private static final int[] crushlb_y = { 35, 25, 35, 45, 35 };
    private void paintCrusher(Graphics g2, Floor floor,
		      int actx, int acty)
    {
        Graphics2D g = (Graphics2D) g2;
        g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	g.drawImage(diverseCrop[10],actx,acty,64,64,this);
	g.setColor(Color.white);
	for (int phasecount=1;phasecount<=5;phasecount++){
	    if (floor.isCrusherActive(phasecount)){
		int strx = actx + crushlb_x[phasecount-1];
		int stry = acty + crushlb_y[phasecount-1];
		g.drawString("" + phasecount,strx,stry);
	    }
	} //for
    }

    /** paints the (back-)ground of the board*/
    private void paintSpielfeldBoden( Graphics g2 ) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
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
		Floor floor = sf.floor(xpos, ypos);

		paintFeldBoden( g, xpos, ypos, actx, acty );
		if ((floor.isBelt() ) && (floor.getInfo()>0))
		    paintCrusher( g, floor, actx, acty);
	    }
	}
    }




    /** Paints the boardlaser-elements*/
   private  void paintLaserStrahlen( Graphics g ) {
       Graphics2D dbg = (Graphics2D) g;
       AlphaComposite ac=null;
       //	if (activeBordLasers)
       // ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
       //else
       ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
       dbg.setComposite( ac );

       LaserDef actuallaser;
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

    /** Paints the wall(s) of a square field at position (xpos, ypos)
	on board and (pixel-)position (actx, acty)
    */
    private void paintWall(Graphics g, int xpos, int ypos, int actx, int acty){
	// paint wall in the north, if any
	if (sf.nw(xpos,ypos).isExisting()){
	    // is there a boardlaser to paint at this wall?
	    if (sf.nw(xpos,ypos).getSouthDeviceType()==Wall.TYPE_LASER){
		g.drawImage(diverseCrop[15],actx,acty+5,64,64,this);
	    }
	    // is there a pisher?
	    if (sf.nw(xpos,ypos).getSouthDeviceType()==Wall.TYPE_PUSHER){
		g.drawImage(diverseCrop[7],actx-1,acty+5,64,64,this);
		// ------------draw text (phases when active) on pusher --------------------
		for (int phasecount=1;phasecount<=5;phasecount++){
		    if (sf.nw(xpos,ypos).isSouthPusherActive(phasecount)){
			int strx = actx + 10*phasecount;
			g.setColor( (phasecount % 2) == 0 ?
				    Color.black : Color.yellow );
			g.drawString( ""+phasecount, strx-1, acty+29 );
		    }
		}

	    }
	    g.drawImage(diverseCrop[13],actx,acty-6,64,64,this);
	}

	// paint wall in the south, if any
	if (sf.sw(xpos,ypos).isExisting()){
	    if (sf.sw(xpos,ypos).getNorthDeviceType()==Wall.TYPE_LASER){
		g.drawImage(diverseCrop[17],actx,acty-5,64,64,this);
	    }
	    if (sf.sw(xpos,ypos).getNorthDeviceType()==Wall.TYPE_PUSHER){
		g.drawImage(diverseCrop[8],actx,acty-5,64,64,this);
		// -----------text on pusher--------------------
		for (int phasecount=1;phasecount<=5;phasecount++){
		    if (sf.sw(xpos,ypos).isNorthPusherActive(phasecount)){
			int strx = actx + 10 * phasecount;
			g.setColor( (phasecount % 2) == 0 ?
				    Color.black : Color.yellow );
			g.drawString( ""+phasecount, strx-1, acty+42 );
		    }
		} //for
	    }
	    g.drawImage(diverseCrop[13],actx,acty+58,64,64,this);
	}

	// paint wall in the south, if any

	if (sf.ew(xpos,ypos).isExisting()){
	    if (sf.ew(xpos,ypos).getWestDeviceType()==Wall.TYPE_LASER){
		g.drawImage(diverseCrop[14],actx-6,acty,64,64,this);
	    }
	    if (sf.ew(xpos,ypos).getWestDeviceType()==Wall.TYPE_PUSHER){
		g.drawImage(diverseCrop[6],actx-6,acty,64,64,this);
		// ------------text on pusher --------------------
		for (int phasecount=1;phasecount<=5;phasecount++){
		    if (sf.ew(xpos,ypos).isWestPusherActive(phasecount)){
			int stry = acty + 10 * phasecount;
			g.setColor( (phasecount % 2) == 0 ?
				    Color.black : Color.yellow );
			g.drawString( ""+phasecount, actx+37, stry+4 );
		    }
		} //for

	    }
	    g.drawImage(diverseCrop[12],actx+57,acty,64,64,this);
	}

	// paint wall in the west, if any
	if (sf.ww(xpos,ypos).isExisting()){
	    if (sf.ww(xpos,ypos).getEastDeviceType()==Wall.TYPE_LASER){
		g.drawImage(diverseCrop[16],actx+5,acty,64,64,this);
	    }
	    if (sf.ww(xpos,ypos).getEastDeviceType()==Wall.TYPE_PUSHER){
		g.drawImage(diverseCrop[9],actx+4,acty,64,64,this);
		// ------------Beschriftung --------------------
		for (int phasecount=1;phasecount<=5;phasecount++){
		    if (sf.ww(xpos,ypos).isEastPusherActive(phasecount)){
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

    private void paintWaende( Graphics g2) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER));


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
		paintWall(g2, xpos, ypos, actx, acty);
	    }
	}

    }

    private void paintFlaggen( Graphics g2 ) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        if(sf.getFlags()!=null){
	    Location[] flaggen = sf.getFlags();
	    for (int flaggencount = 0; flaggencount<flaggen.length;flaggencount++){
		int xflagge = flaggen[flaggencount].x-1;
		int yflagge = sf.getSizeY()-flaggen[flaggencount].y;
		g.drawImage(diverseCrop[18+flaggencount],
			    xflagge*64,yflagge*64,64,64,this);
	    }
	}
    }

    /** Berechnet zu einem Location das Rechteck, das die Kachel umschliesst */
    void ort2Rect(Location ort, Rectangle dest) {
	ort2Rect(ort.x, ort.y, dest);
    }

    void ort2Rect(int x, int y, Rectangle dest) {
	dest.x = (int) ((x - 1) * scaledFeldSize);
	dest.y = (int) ((sf.getSizeY() - y) * scaledFeldSize);
	dest.width  = (int)scaledFeldSize;
	dest.height = (int)scaledFeldSize;
    }

    public Point ort2Point( Location ort, Point p ) {
        return ort2Point( ort.x, ort.y, p );
    }

    public Location point2Ort( Point p, Location ort ) {
        ort.x = (int)(p.x / scaledFeldSize) + 1;
        ort.y = (int)((getHeight() - p.y) / scaledFeldSize) + 1;
        return ort;
    }
    /** returns left upper point of square*/
    public Point ort2Point( int ortx, int orty, Point p ) {
	p.x = (int) ((ortx - 1) * scaledFeldSize);
	p.y = (int) ((sf.getSizeY() - orty) * scaledFeldSize);
        return p;
    }


    Rectangle rc = new Rectangle();
    // for internal use. see repaintOrt()

    /** Triggert ein Neuzeichnen des Feldes mit den \uFFFDbergebenen
     *  Koordinaten. N\uFFFDtzlich um einzelne Felder neuzeichnen zu lassen
     */

    void repaintOrt(Location ort) {
	ort2Rect(ort, rc);
	repaint( 1, rc.x, rc.y, rc.width, rc.height );
    }

    void repaintOrt(int x, int y) {
	ort2Rect(x, y, rc);
	repaint( 1, rc.x, rc.y, rc.width, rc.height );
    }

    void unhighlight() {
	highlightPos.x = 0;
	highlightPos.y = 0;
	repaint();
    }

    private final javax.swing.Timer t = new javax.swing.Timer(5000, new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            unhighlight();
        }
    });


    void highlight(int x, int y) {
        // remove old highlight:
        repaintOrt( highlightPos );

        if( CAT.isDebugEnabled() )
    	    CAT.debug("highlighting 1 " + x + " " + y);
	highlightPos.x = x;
	highlightPos.y = y;

        //this.paintHighlight((Graphics2D)this.getGraphics());
        if( !t.isRunning() )
            t.start();
        else
            t.restart();

	repaintOrt(x,y);
    }


    private void showScout(Location ort) {
	deleteScout();
	repaintOrt( ort );
	lastScoutPos.set( ort );
    }

    private void deleteScout() {
	repaintOrt( lastScoutPos );
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
	    g.drawImage(scoutCrop[vorschauRob.getFacing()],xpos64,ypos64,64,64,this);
	    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }


     private void paintFeldWithElements(Graphics2D g2d, int xpos, int ypos, int actx, int acty){
      Floor floor = sf.floor(xpos, ypos);
      paintFeldBoden(g2d,xpos,ypos, actx, acty);
      if ((floor.isBelt() ) && (floor.getInfo()>0)) // restore possible Crusher
		    paintCrusher( g2d, floor, actx, acty);
      // @todo: only repaint the stuff on the field we want to paint
      paintWall(g2d, xpos, ypos, actx, acty);
      paintFlaggen(g2d);
    }

      private void paintRobot(Graphics2D g2d, Bot robot, int robocount,
                            int xoffset, int yoffset, boolean eraseFloor){
       g2d.scale(getScale(), getScale());

       int xpos = robot.getX()-1;
       int ypos = sf.getSizeY()-robot.getY();
       int xpos64 = xpos*64;
       int ypos64 = ypos*64;
       int actx = xpos64;//-64;
       int acty = ypos64;//-64;
       //if (CAT.isDebugEnabled()){
       //  CAT.debug("drawing robot ("+robot.getX()+", "+robot.getY()+"), xpos64="+xpos64+", ypos64="+ypos64);
       //}
       if (eraseFloor) {// for animating robot movement
          int x = xpos+1;
          int y = robot.getY();


          paintFeldWithElements(g2d, x, y, actx, acty);   // erase old robot image

          // we will also have to erase the field the robot enters
            if (yoffset<0 && y<sf.getSizeY()){ // moving north => erasing field
                                              // above (if there is)
              paintFeldWithElements(g2d, x,y+1, actx, acty-64);

            }
            else if (yoffset>0 && y>1){ // moving south, erasing field below
              paintFeldWithElements(g2d, x,  y-1, actx, acty+64);
            }
            if (xoffset>0 && x<sf.getSizeX()) {// moving east..
              paintFeldWithElements(g2d,x+1, y, actx+64, acty);
            }
            else if (xoffset<0 && x>1){// moving west
              paintFeldWithElements(g2d,x-1, y, actx-64, acty);
            }
       }
       xpos64 +=xoffset;
       ypos64 +=yoffset;

       int botVis = robot.getBotVis();
       Image imgRob = robosCrop[robot.getFacing()+botVis*4];
       boolean virtuell = robot.isVirtual();

       if( imgRob != null ) {
          if( virtuell ) {
	     AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	     g2d.setComposite( ac );
	  }
	  g2d.drawImage(imgRob,xpos64,ypos64,64,64,this);
	  if( virtuell ) {
	    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	  }
	  String beschriftung = "" + robot.getName();
	  g2d.setColor( robocolor[botVis] );
	  g2d.drawString(beschriftung,xpos64,ypos64+8+robocount*8);
      }
    }

     private void paintRobos (Graphics g){
     paintRobos(g, null);
    }

    private void paintRobos( Graphics g, Bot dontPaintMe) {
	Graphics2D g2d = (Graphics2D) g;
	if (dontPaintMe==null) {
	    if (robos!=null){
		for (int robocount=0;robocount<robos.length;robocount++){
		    Bot robot = robos[robocount];
		    if((robot.getDamage()<10)&&
		       (robot.getLivesLeft() > 0)) {
			paintRobot(g2d, robot, robocount, 0, 0, false);
		    }
		}
	    }
	}
	else {
	    if (robos!=null){
		for (int robocount=0;robocount<robos.length;robocount++){
		    Bot robot = robos[robocount];
		    if((robot.getDamage()<10)&&
		       (robot.getLivesLeft() > 0)&&
		       !dontPaintMe.getName().equals(robot.getName())) {
			paintRobot(g2d, robot, robocount, 0, 0, false);
		    }
		}
	    }
	}

    }

    protected Image getRobImage(Bot robot, int facing){
      int botVis = robot.getBotVis();
      return robosCrop[facing+botVis*4];
    }


    private final static Stroke[] hi = new Stroke[] {
        new BasicStroke(6), new BasicStroke(4), new BasicStroke(2), new BasicStroke(1)
    };
    private final static Color[] hiColOut = new Color[] {
        Color.red.darker().darker(), Color.red.darker(), Color.red, Color.red.brighter()
    };
    public final Color highCol1 = new Color(255, 0, 0, 255 );
    public final Color highCol2 = new Color(255, 255, 0, 128 );
    private void paintHighlight(Graphics2D g) {
        Rectangle rc = new Rectangle();
        ort2Rect(highlightPos, rc);
        rc.grow(-3,-3);
        for(int i = 0; i < hi.length; i++ ) {
            g.setColor( hiColOut[i] );
            g.setStroke( hi[i] );
            g.drawOval( rc.x, rc.y, rc.width, rc.height );
        }

        Paint p = new GradientPaint( rc.x, rc.y, highCol1, rc.x + rc.width, rc.y + rc.height, highCol2 );
        g.setPaint( p );
        rc.grow(-1, -1);
        g.fillOval( rc.x, rc.y, rc.width, rc.height );
    }

//     private void createOffscreenImage() {
// 	// XXX vielleicht besser das skalieren erst beim reinkopieren
// 	dbi = createImage(x,y);
// 	g_off = (Graphics2D)dbi.getGraphics();
// 	g_off.setFont(new Font(g_off.getFont().getName(),g_off.getFont().getStyle(),8));
// 	g_off.setClip(0,0,x,y);
// 	g_off.scale( dScale, dScale );
//     }
    Graphics2D g_off;


    BufferedImage preBoard = null;

    /** ein Image des Spielfeldes anlegen, ohne aktive Elemente */
    private void prepareBoardImage() {
	preBoard = getBoardImage();
    }

    public BufferedImage getBoardImage() {
	//preBoard = new BufferedImage(x,y, BufferedImage.TYPE_BYTE_INDEXED);
        BufferedImage bi = new BufferedImage(x,y, BufferedImage.TYPE_INT_RGB);
	g_off = (Graphics2D)bi.getGraphics();
	g_off.setClip(0,0,x,y);
	g_off.scale( dScale, dScale );
	paintUnbuffered( g_off );
	g_off.dispose();
        return bi;
    }


    public void paintComponent(Graphics g) {

	// Blit the board (it's already scaled)
	if( preBoard == null ) {
	    prepareBoardImage();
	}
	g.drawImage(preBoard, 0, 0, this);

	// draw the active elements (robos)
	Graphics2D dbg = (Graphics2D) g;
	paintHighlight( dbg );

	dbg.scale( dScale, dScale );
        dbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	paintScout( dbg );
	paintRobos( dbg );
    }

    protected void paintUnbuffered(Graphics dbg) {
	paintSpielfeldBoden( dbg );
	paintLaserStrahlen( dbg );
	paintWaende( dbg );
	paintFlaggen( dbg );
	paintScout( dbg );
    }

    protected void finalize() throws Throwable {
      	super.finalize();
	g_off.dispose();
    }

    public void update(Graphics g){
	paint(g);
    }

    protected Location [] getFlags() {
      return sf.getFlags();
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

    private void ersetzeSpielfeld(SimBoard sfs){
	sf = sfs;
	x = (int) (sf.getSizeX() * scaledFeldSize);
	y = (int) (sf.getSizeY() * scaledFeldSize);
	setSize(x,y);
    }



  // Little helper for getting thumbnails of the board
    private static BoardView sac = null;
    public static Image createThumb(SimBoard sim, int size) {
	if( sac == null ) {
	    sac = new BoardView(sim);
	} else {
	    sac.ersetzeSpielfeld( sim );
	}
	return sac.getThumb(size);
    }

}

