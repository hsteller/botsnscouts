/*
 * Created on 21.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.botsnscouts.gui.board;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;

import org.apache.log4j.Category;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.gui.AusgabeView;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.SoundMan;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AnimationCanvas extends Canvas implements Scalable, DrawingConstants{
    Category CAT = Category.getInstance(AnimationCanvas.class);
    
    
    
    
    /**Number of single steps a laser animation is drawn.*/
    private static final int FULL_LENGTH_INT = 30;

    /**Number of single steps a laser animation is drawn.*/
    private static final double FULL_LENGTH_DOUBLE = 30.0;
   
    /** The color used for the background of active lasers. */
    private final static Color BG_LASER_COLOR = new Color(255, 255, 155);
    
   private double dScale = 1.0;
    
   private SimBoard gameboard;
   private BufferedImage blank = null;
   
   /**Needed to obtain the robot colors (for drawing lasers)*/
   private BotCanvas botCanvas;
   
   private int delay=AusgabeView.MEDIUM;
   
   /** @param botCanvas to obtain the robot colors (for drawing lasers)*/
   public AnimationCanvas (SimBoard gameBoard, BotCanvas botCanvas){
       this.gameboard  = gameBoard;       
       this.botCanvas = botCanvas;
   }
   
   
   
   
   
   
   
   /**
    @param laserPos Die Koordinaten des schiessenden BordLasers
    @param laserDir Die Ausrichtung des Lasers
    @param targetRob Die Koordinaten des getroffenen Bots
    @param surrounding Das ScrollPane in dem der Canvas dargestellt wird
    */
   protected void doBordLaser(Location laserPos, int laserDir, int strength, Location targetRob, JViewport surrounding) {
       // init laser values
       Location source = laserPos;
       Location target = targetRob;
       int laserFacing = laserDir;
       int laenge = calculateLaserLength(source, target, laserFacing);
       laenge = laenge * 64 + 17;
       Color c = ElementCanvas.getLaserColor(strength - 1);

       // paint lasers step by step
       for (int i = 1; i <= FULL_LENGTH_INT; i++) {
           int tmp_laenge = (int) ((((double) i) / FULL_LENGTH_DOUBLE) * laenge);
           Graphics2D g2 = (Graphics2D) getGraphics();
           g2.scale(dScale, dScale);
           paintActiveBordLaser(g2, c, source, tmp_laenge, laserFacing);

       }
       // now paint the non-animated lasers again
       repaint();              
       try {
           synchronized (this){
               wait(delay);  
           }
        } catch (InterruptedException ie) {
           CAT.error("BoardView.doBordLaser: wait interrupted");
       }
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
  private int calculateLaserLength(Location source, Location target, int laserFacing) {

      int length = 0;
    
      switch (laserFacing) {
          case NORTH: {
                  length = target.y - source.y;
                  break;
              }
          case EAST: {
                  length = target.x - source.x;
                  break;
              }
          case SOUTH: {
                  length = source.y - target.y;
                  break;
              }
          case WEST: {
                  length = source.x - target.x;
                  break;
              }
          default: {
                  CAT.error("BoardView.calculateLaserLength(): ungueltige Laserrichtung: " + laserFacing);
            }
      }
      //CAT.debug("calculate Length: ("+source.x+","+source.y+")-"+facing+"->("+target.x+","+target.y+") = "+length);
      return length;
  }
    
  private void paintActiveRobLaser(Graphics g, Color c,  Location source, int actualLength, int laserFacing) {
      // Laser sollen immer von Source nach Target gezeichnet werden

      int breite = 4; // Die Breite des Lasers, sollte gerade sein
      int lSourceX = 0;
      int lSourceY = 0; // Anfangspunkt des Lasers in Pixeln,
      Location tmp = mapC2PixelCenter(source.x, source.y); /* Mitte (Punkt (31,31) auf Feld
							   mit Punkten von 0 bis 63,
							   also einem 64x64 grossen Feld

							*/

      Graphics2D g2d = (Graphics2D) g;
      AlphaComposite ac = AC_SRC_OVER_05;
      g2d.setComposite(ac);
      g2d.setColor(c);
      switch (laserFacing) {
          case NORTH:
              {
                  lSourceX = tmp.x - (breite / 2 - 1);
                  lSourceY = tmp.y - actualLength;
                  g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                  break;
              }
          case EAST:
              {
                  lSourceX = tmp.x;
                  lSourceY = tmp.y - (breite / 2 - 1);
                  ;
                  g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                  break;
              }
          case SOUTH:
              {
                  lSourceX = tmp.x - (breite / 2 - 1);
                  lSourceY = tmp.y;
                  g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                  break;
              }
          case WEST:
              {
                  lSourceX = tmp.x - actualLength;
                  lSourceY = tmp.y - (breite / 2 - 1);
                  g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                  break;
              }
          default :
              {
                  CAT.error("BoardView.paintActiveRobLaser: ");
                  CAT.error("Ungueltige Laserrichtung: " + laserFacing);
              }
      }// end switch facing
      g2d.setComposite(AC_SRC);
  }

  /** Berechnet die (Java-)Pixel-Koordinaten der linken oberen Ecke eines Bord-Feldes.
  Gibt die x- und y-Pixelwerte der linken oberen Ecke des Feldes
  mit der Position (x,y) auf dem Spielplan zurueck.
  @param x Die X-Koordinate des Feldes
  @param y Die Y-Koordinate des Feldes
  @return Die Position der linken oberen Ecke des Feldes als Java-Pixelwerte zum Zeichnen.
  */
 private Location mapC2PixelNorthWest(int x, int y) {
     Location pixel = new Location();
     pixel.x = (x - 1) * 64;
     pixel.y = (gameboard.getSizeY() - y) * 64;
     return pixel;
 }

  /** Berechnet die (Java-)Pixelwerte fuer den Mittelpunkt des Feldes.
  Genauer: Den Punkt (31,31) auf dem 64x64 grossen Feld mit Koordinaten
  zwischen 0 und 63.
  */
 private Location mapC2PixelCenter(int x, int y) {
     Location pixel = mapC2PixelNorthWest(x, y);
     pixel.x += 31;
     pixel.y += 31;
     return pixel;
 }
  
  public void setScale (double dscale){
      this.dScale = dscale;
  }
 
  
  
  private void paintActiveBordLaser(Graphics g, Color c, Location source, int actualLength, int laserFacing) {
      boolean boardLaser;
      Graphics2D g2d = (Graphics2D) g;
      AlphaComposite ac = AC_SRC_OVER;//, 0.5f
      g2d.setComposite(ac);
      g2d.setColor(c);

      int breite = 4; // Die Breite des Lasers, sollte gerade sein
      int lSourceX = 0;
      int lSourceY = 0; // Anfangspunkt des Lasers in Pixeln,
      Location tmp = mapC2PixelCenter(source.x, source.y);
  
      int paintx=0;
      int painty=0;
      // synchronized (lock) {
      switch (laserFacing) {
          case NORTH:
              {
           
                  lSourceX = tmp.x - (breite / 2 - 1);
                  lSourceY = tmp.y - actualLength + 14;
                  g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                  
                  g2d.setColor(BG_LASER_COLOR);
                  g2d.drawRect(lSourceX, lSourceY, breite, actualLength);
                  break;
              }
          case EAST:
              {
                  lSourceX = tmp.x - 17;
                  lSourceY = tmp.y - (breite / 2 - 1);
                  
                  g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                  g2d.setColor(BG_LASER_COLOR);
                  g2d.drawRect(lSourceX, lSourceY, actualLength, breite);
                  break;
              }
          case SOUTH:
              {
                  lSourceX = tmp.x - (breite / 2 - 1);
                  lSourceY = tmp.y - 15;
                  g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                  g2d.setColor(BG_LASER_COLOR);
                  g2d.drawRect(lSourceX, lSourceY, breite, actualLength);
                  break;
              }
          case WEST:
              {
                  lSourceX = tmp.x - actualLength + 17;
                  lSourceY = tmp.y - (breite / 2 - 1);
                  g2d.fillRect(lSourceX, lSourceY, actualLength - 2, breite);
                  g2d.setColor(BG_LASER_COLOR);
                  g2d.drawRect(lSourceX, lSourceY, actualLength - 2, breite);
                  break;
              }
          default :
              {
                  CAT.error("BoardView.paintActiveRobLaser: ");
                  CAT.error("Ungueltige Laserrichtung: " + laserFacing);
              }
      }// end switch facing
      //   allDone = true;
      //   lock.notifyAll();
      // }
      //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
  }

  
  public void setDelay(int millisecs){
  	delay = millisecs;
  }
  
  public int getDelay() {
  	return delay;
  }
  /**
   * Draws animated robot lasers.
   @param sourceRob position of firing robot
   @param targetRob position of the robot hit
   */

  public void doRobLaser(Bot sourceRob, Bot targetRob) {
      //  allDone = false;
      if (CAT.isDebugEnabled())
          CAT.debug("doRobLaser: " + sourceRob.getName() + " -> " + targetRob.getName());
      Location source = sourceRob.getPos();
      Location target = targetRob.getPos();
      int laserFacing = sourceRob.getFacing();
      int laenge = calculateLaserLength(source, target, laserFacing);
      laenge *= 64;

      String name = sourceRob.getName();

      Color c = botCanvas.getRobColor(name);
      
      synchronized (this) {
          try {
              wait(delay/2);  
          } catch (InterruptedException ie) {
              CAT.error("BoardView.paint: wait interrupted");
          }
          		
          SoundMan.playSound(BotVis.getBotLaserSoundByName(name));
          for (int i = 1; i <= FULL_LENGTH_INT; i++) {
              int tmp_laenge = (int) ((((double) i) / FULL_LENGTH_DOUBLE) * laenge);
              Graphics2D g2 = (Graphics2D) getGraphics();
              g2.scale(dScale, dScale);
              paintActiveRobLaser(g2,  c, source, tmp_laenge, laserFacing);

              //     synchronized(this){
              try {
                  wait(1);
              } catch (InterruptedException ie) {
                  CAT.error("BoardView.paint: wait interrupted");
              }
              
              //   }
          }
          try {
              wait(delay/2);  
          } catch (InterruptedException ie) {
              CAT.error("BoardView.paint: wait interrupted");
          }
      }

      // drawRobLaser=false;
      if (SoundMan.isSoundActive()) {
          // SoundMan.playSound(SoundMan.BUMM);
          synchronized (this) {
              try {
                  wait(200);
              } catch (InterruptedException ie) {
                  CAT.error("BoardView.paint: wait interrupted");
              }
          }
      }
      repaint();


  }

  public void paintComponent(Graphics g) {
      
      if (blank== null) {
          blank = getBlankImage();
         }
         g.drawImage(blank, 0, 0, this);

     
      Graphics2D dbg = (Graphics2D) g;
      dbg.scale(dScale, dScale);
     
  }
 
  private BufferedImage getBlankImage(){
      Dimension dim = BoardLayers.calcBoardDimensionInPixel(dScale,gameboard);
      int width = (int) dim.getWidth();
      int height = (int) dim.getHeight();
      BufferedImage bi = new BufferedImage(width, height,  BufferedImage.TYPE_INT_RGB);
      Graphics2D g_off = (Graphics2D) bi.getGraphics();
      g_off.setClip(0, 0, width,height);
      g_off.scale(dScale, dScale);
      // XXX g_off.dispose() ???
      return bi;
  }
  
  //
 // protected void replaceGameboard(SimBoard board){
 //    this.gameboard = board;
 // }
//

}
