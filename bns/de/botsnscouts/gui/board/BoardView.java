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

package de.botsnscouts.gui.board;

import de.botsnscouts.board.*;import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import org.apache.log4j.Category;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import com.keypoint.PngEncoder;

/**
 * Board-Ausgabe-Canvas ist das Objekt, das der Ausgabe und dem menschlichen Spieler das Board grafisch darstellt und verwaltet
 * @author some time ago Daniel Holtz
 * @version improvement of 1.0

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

public class BoardView extends JLayeredPane implements DrawingConstants{
    static Category CAT = Category.getInstance(BoardView.class);




    /** size (length and width) of one little field in pixels*/
    protected static final int FELDSIZE = 64;




    
    
    
    
    

    
    // for painting active Lasers
    /** position of firing robot*/
    private Location source;
    /**position of robot hit*/
    private Location target;
    /** facing (direction) of the laser, according to the directions above*/
    private int laserFacing;
    private boolean activeBordLasers;

    /** contains colors of the boardlasers, strength 1 to 3*/
    static final Color[] laserColor = {Color.red.brighter(), //strength 1
                                       Color.orange, //strength 2
                                       Color.yellow};//strength 3

   

  

    /** some board elements..*/
    private Image[] cbeltCrop,ebeltCrop,diverseCrop,robosCrop,scoutCrop;

    /** maps Location(x,y) to the Image that should be painted as floor*/
   private HashMap floorElementHash = new HashMap();

    private int x,y;


    /** This robot is used for calculations,
     *  like making a suggestion for the next move.
     */
    private Bot previewRob;

    /** last position of our famous scout ;-) */
    private Location lastScoutPos = new Location();
    // Let's define some colors, so that everybody uses the same..
    public static final Color YELLOW = BotVis.YELLOW;
    public static final Color RED = BotVis.RED;
    public static final Color BLUE = BotVis.BLUE;
    public static final Color ROSA = BotVis.ROSA;
    public static final Color ORANGE = BotVis.ORANGE;
    public static final Color GRAY = BotVis.GRAY;
    public static final Color VIOLET = BotVis.VIOLET;
    public static final Color GREEN = BotVis.GREEN;

    /** Colors of the robots. */
    public static final Color[] ROBOCOLOR = {GREEN, YELLOW, RED, BLUE, ROSA, ORANGE, GRAY, VIOLET};

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
    Location highlightPos = new Location(0, 0);




    
    
    
    /** Number of pixels a robot will be moved in a single animation step.
     *  Has to be between 1 and FELDSIZE.
     *  => Number of steps a one-field-move is drawn = FELDSIZE/MOVE_ROB_ANIMATION_OFFSET
     *
     *   TODO: I guess that this must not be final and needs to be scaled so that it works
     *    with different zoomlevels
     */
    private static final int MOVE_ROB_ANIMATION_OFFSET = 4;
    /** Amount of time (in ms) we wait after a single animation step
     *  of (pixel-)length MOVE_ROB_ANIMATION_OFFSET.
     */
    private static final int MOVE_ROB_ANIMATION_DELAY = 1;

  

    public BoardView(SimBoard sf_neu) {
        init(sf_neu, ROBOCOLOR);
        
    }

    public BoardView(SimBoard sf_neu, Color[] robColors) {
        init(sf_neu, robColors);
     
    }

    /** Get a simple board view loaded from one tile file.
     *  This is not the way to do this in general since boards may
     *  consist of serveral tiles and each tile may be rotated.
     * @param boardFile The board file to load.
     */
    public BoardView(File boardFile) throws IOException, FormatException, FlagException {
        this(new SimBoard(12, 12, Board.readMagicString(boardFile)));
    }


    public double getScale() {
        return dScale;
    }



    private void init(SimBoard sf_neu, Color[] robColors) {
        activeBordLasers = false;
        //HS gotColors = false;
        sf = sf_neu;

        setDoubleBuffered(true);
        //HSsetScale(dScale); // does setSize()

        ImageMan.finishLoading();

        ebeltCrop = ImageMan.getImages(ImageMan.EBELTS);
        cbeltCrop = ImageMan.getImages(ImageMan.CBELTS);
        diverseCrop = ImageMan.getImages(ImageMan.DIVERSE);
        robosCrop = ImageMan.getImages(ImageMan.ROBOS);
        scoutCrop = ImageMan.getImages(ImageMan.SCOUT);

        // HS initFloorHashMap();
        
      
    }


    //void setScrollPane(JScrollPane j) {
    //    myScrollPane = j;
    //}

   

 








  


    /////////////////////////////////////////////////////////////////////

    private void moveRobNorth(Bot internal, int robocount) {    
        CAT.debug("moving bot one square to the north");
        synchronized (this) {
           Graphics2D g2 = (Graphics2D) this.getGraphics();
         
            g2.scale(dScale, dScale);
            AlphaComposite ac = AC_SRC;
            AlphaComposite ac2 = null;
            if (internal.isVirtual())
                ac2 = AC_SRC_OVER_05;
            Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
            int x1 = internal.getX();
            int y1 = internal.getY();
            int xpos = x1 - 1;
            int ypos = sf.getSizeY() - y1;
            int xpos64 = xpos * 64;
            int ypos64 = ypos * 64;
            int actx1 = xpos64;
            int acty1 = ypos64;
            int x2 = x1;
            int y2 = y1 + 1;
            int actx2 = actx1;
            int acty2 = acty1 - 64;
            if (MOVE_ROB_ANIMATION_DELAY > 0) {
                for (int yoffset = 0; yoffset >= -64; yoffset -= MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64, ypos64 + yoffset,
                            ac, ac2);

                    try {
                        Thread.currentThread().sleep(MOVE_ROB_ANIMATION_DELAY);
                    } catch (InterruptedException ie) {
                        CAT.warn("BoardView.paint: wait int moveRobNorth interrupted");
                    }
                }
            } else { // loop without sleeping
                for (int yoffset = 0; yoffset >= -64; yoffset -= MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64, ypos64 + yoffset,
                            ac, ac2);
                }
            }
        }
    }


    private void moveRobSouth(Bot internal, int robocount) {
        CAT.debug("moving bot one square to the south");
        synchronized (this) {
     Graphics2D g2 = (Graphics2D) this.getGraphics();
            
            g2.scale(dScale, dScale);
            AlphaComposite ac = AC_SRC;
            AlphaComposite ac2 = null;
            if (internal.isVirtual())
                ac2 = AC_SRC_OVER_05;
            Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
            int x1 = internal.getX();
            int y1 = internal.getY();
            int xpos = x1 - 1;
            int ypos = sf.getSizeY() - y1;
            int xpos64 = xpos * 64;
            int ypos64 = ypos * 64;
            int actx1 = xpos64;
            int acty1 = ypos64;
            // direction dependend part:
            int x2 = x1;
            int y2 = y1 - 1;
            int actx2 = actx1;
            int acty2 = acty1 + 64;
            if (MOVE_ROB_ANIMATION_DELAY > 0) {
                for (int yoffset = 0; yoffset <= 64; yoffset += MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64, ypos64 + yoffset,
                            ac, ac2);
                    if (MOVE_ROB_ANIMATION_DELAY > 0)
                        try {
                            Thread.currentThread().sleep(MOVE_ROB_ANIMATION_DELAY);
                        } catch (InterruptedException ie) {
                            CAT.warn("BoardView.paint: wait int moveRobSouth interrupted");
                        }
                }
            } else {
                for (int yoffset = 0; yoffset <= 64; yoffset += MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64, ypos64 + yoffset,
                            ac, ac2);
                }
            }
        }
    }


    private void moveRobEast(Bot internal, int robocount) {
        CAT.debug("moving bot one square to the east");
        synchronized (this) {
         Graphics2D g2 = (Graphics2D) this.getGraphics();
           
            g2.scale(dScale, dScale);
            AlphaComposite ac = AC_SRC;
            AlphaComposite ac2 = null;
            if (internal.isVirtual())
                ac2 = AC_SRC_OVER_05;
            Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
            int x1 = internal.getX();
            int y1 = internal.getY();
            int xpos = x1 - 1;
            int ypos = sf.getSizeY() - y1;
            int xpos64 = xpos * 64;
            int ypos64 = ypos * 64;
            int actx1 = xpos64;
            int acty1 = ypos64;
            // direction dependend part:
            int x2 = x1 + 1;
            int y2 = y1;
            int actx2 = actx1 + 64;
            int acty2 = acty1;
            if (MOVE_ROB_ANIMATION_DELAY > 0) {
                for (int xoffset = 0; xoffset <= 64; xoffset += MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64 + xoffset, ypos64,
                            ac, ac2);
                    try {
                        Thread.currentThread().sleep(MOVE_ROB_ANIMATION_DELAY);
                    } catch (InterruptedException ie) {
                        CAT.warn("BoardView.paint: wait int moveRobEast interrupted");
                    }
                }
            } else { // loop without sleeping
                for (int xoffset = 0; xoffset <= 64; xoffset += MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64 + xoffset, ypos64,
                            ac, ac2);
                }
            }
        }

    }

    private void moveRobWest(Bot internal, int robocount) {
        CAT.debug("moving bot one square to the west");
        synchronized (this) {
      Graphics2D g2 = (Graphics2D) this.getGraphics();
           
            g2.scale(dScale, dScale);
            AlphaComposite ac = AC_SRC;
            AlphaComposite ac2 = null;
            if (internal.isVirtual())
                ac2 = AC_SRC_OVER_05;
            Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
            int x1 = internal.getX();
            int y1 = internal.getY();
            int xpos = x1 - 1;
            int ypos = sf.getSizeY() - y1;
            int xpos64 = xpos * 64;
            int ypos64 = ypos * 64;
            int actx1 = xpos64;
            int acty1 = ypos64;
            // direction dependend part:
            int x2 = x1 - 1;
            int y2 = y1;
            int actx2 = actx1 - 64;
            int acty2 = acty1;
            if (MOVE_ROB_ANIMATION_DELAY > 0) {
                for (int xoffset = 0; xoffset >= -64; xoffset -= MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64 + xoffset, ypos64,
                            ac, ac2);
                    try {
                        Thread.currentThread().sleep(MOVE_ROB_ANIMATION_DELAY);
                    } catch (InterruptedException ie) {
                        CAT.warn("BoardView.paint: wait int moveRobWest interrupted");
                    }
                }
            } else {
                for (int xoffset = 0; xoffset >= -64; xoffset -= MOVE_ROB_ANIMATION_OFFSET) {
                    paintRobotForMoveAnimation(g2, imgRob,
                            x1, y1, actx1, acty1,
                            x2, y2, actx2, acty2,
                            xpos64 + xoffset, ypos64,
                            ac, ac2);
                }
            }
        }
    }

    // XXX HS toPleaseCompiler
    Bot [] robos;
    protected void animateRobMove(Bot rob, int direction) {
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
        int robocount = -1;
        for (int i = 0; i < robos.length; i++) {
            if (robos[i].getName().equals(name)) {
                internal = robos[i];
                robocount = i;
                break;
            }
        }
        int oldX = internal.getX();
        int oldY = internal.getY();

        // paint the move animation  and update the position in my internal robot array
        switch (direction) {
            case NORTH:
                {
                    if (oldY < sf.getSizeY()) {
                        moveRobNorth(internal, robocount);
                        internal.setPos(oldX, oldY + 1);
                    }
                    return;
                }
            case EAST:
                {
                    if (oldX < sf.getSizeX()) {
                        moveRobEast(internal, robocount);
                        internal.setPos(oldX + 1, oldY);
                    }
                    return;
                }
            case WEST:
                {
                    if (oldX > 1) {
                        moveRobWest(internal, robocount);
                        internal.setPos(oldX - 1, oldY);
                    }
                    return;
                }
            case SOUTH:
                {
                    if (oldY > 1) {
                        moveRobSouth(internal, robocount);
                        internal.setPos(oldX, oldY - 1);
                    }
                    return;
                }
            default:
                {
                    // this must not happen,
                    // otherwise the whole gui might be useless as it keeps probably
                    // a wrong position for one robot
                    CAT.fatal("Got illgeal direction for animating robot");
                }
        }
    }


  
    
   

   

    

  
   


    protected void preview(int phase, Bot simRob) {
        if (phase == 0) {
            //scoutOn = true; // flag for repaint: yes, paint scout!
            previewRob = null;
            deleteScout();
            //repaint();
            return;
        }

        Bot[] robs = new Bot[1];
        robs[0] = simRob;
        for (int i = 1; i < phase + 1; i++) {
            //sf.doPhase(phase, simRob);
            sf.doPhase(phase, robs);
        }
        //previewRob = vorschauRobArray[0];
        previewRob = simRob;
        showScout(simRob.getPos());

        //repaint();

    }

    protected void preview(int phasen, Bot[] previewRoboters) {
        if (phasen == 0) {
            previewRob = null;
            deleteScout();
            //repaint();
            return;
        }

        for (int i = 1; i < phasen + 1; i++) {
            sf.doPhase(i, previewRoboters);
        }
        previewRob = previewRoboters[0];
        showScout(previewRob.getPos());
        //repaint();

    }

    


    

   

  

 

   

    

    /** Berechnet zu einem Location das Rechteck, das die Kachel umschliesst */
    void ort2Rect(Location ort, Rectangle dest) {
        ort2Rect(ort.x, ort.y, dest);
    }

    void ort2Rect(int x, int y, Rectangle dest) {
        dest.x = (int) ((x - 1) * scaledFeldSize);
        dest.y = (int) ((sf.getSizeY() - y) * scaledFeldSize);
        dest.width = (int) scaledFeldSize;
        dest.height = (int) scaledFeldSize;
    }

    public Point ort2Point(Location ort, Point p) {
        return ort2Point(ort.x, ort.y, p);
    }

    public Location point2Ort(Point p, Location ort) {
        ort.x = (int) (p.x / scaledFeldSize) + 1;
        ort.y = (int) ((getHeight() - p.y) / scaledFeldSize) + 1;
        return ort;
    }

    /** returns left upper point of square*/
    public Point ort2Point(int ortx, int orty, Point p) {
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
        repaint(1, rc.x, rc.y, rc.width, rc.height);
    }

    void repaintOrt(int x, int y) {
        ort2Rect(x, y, rc);
        repaint(1, rc.x, rc.y, rc.width, rc.height);
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
        repaintOrt(highlightPos);

        if (CAT.isDebugEnabled())
            CAT.debug("highlighting 1 " + x + " " + y);
        highlightPos.x = x;
        highlightPos.y = y;

        //this.paintHighlight((Graphics2D)this.getGraphics());
        if (!t.isRunning())
            t.start();
        else
            t.restart();

        repaintOrt(x, y);
    }


    private void showScout(Location ort) {
        deleteScout();
        repaintOrt(ort);
        lastScoutPos.set(ort);
    }

    private void deleteScout() {
        repaintOrt(lastScoutPos);
    }

    protected void paintScout(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (previewRob == null)
            return;

        int xpos = previewRob.getX() - 1;
        int ypos = sf.getSizeY() - previewRob.getY();
        int xpos64 = xpos * 64;
        int ypos64 = ypos * 64;
        // Scout
        AlphaComposite ac = AC_SRC_OVER_07;
        g2d.setComposite(ac);
        g.drawImage(scoutCrop[previewRob.getFacing()], xpos64, ypos64, 64, 64, this);
        g2d.setComposite(AC_SRC);
    }


    private void paintFeldWithElements(Graphics2D g2d, int xpos, int ypos, int actx, int acty) {
        Floor floor = sf.floor(xpos, ypos);
       //HS paintFeldBoden(g2d, xpos, ypos, actx, acty);
        if ((floor.isBelt()) && (floor.getInfo() > 0)) // restore possible Crusher
            //HSpaintCrusher(g2d, floor, actx, acty);
        // TODO: only repaint the stuff on the field we want to paint
        g2d.setComposite(AC_SRC_OVER);
        //HSpaintWall(g2d, xpos, ypos, actx, acty);
        //HSpaintFlaggen(g2d);
    }


    /** Some performance optimizations..*/
    private void paintRobotForMoveAnimation(Graphics2D g2d, Image botImage,
                                            int xpos, int ypos, int actx, int acty,
                                            int x2, int y2, int actx2, int acty2,
                                            int xpos64, int ypos64,
                                            AlphaComposite ac, AlphaComposite ac2) {
        // erase the old robot image from the square of its original position by
        // painting it again
        paintFeldWithElements(g2d, xpos, ypos, actx, acty);
        // erase the old robot image from the square the robot is moving to
        paintFeldWithElements(g2d, x2, y2, actx2, acty2);


        if (ac2 != null) {// robot is virtual
            g2d.setComposite(ac2); // set the robot image to be half transparent
            g2d.drawImage(botImage, xpos64, ypos64, 64, 64, this); // paint the image
            g2d.setComposite(ac); // reset the transparency level as the next call
            // of this method will start with painting the
            // background again
        } else // robot is not virtual
            g2d.drawImage(botImage, xpos64, ypos64, 64, 64, this);

        // for animating we will skip to paint the name of the bot

    }

   
    protected Image getRobImage(Bot robot, int facing) {
        int botVis = robot.getBotVis();
        return robosCrop[facing + botVis * 4];
    }


    private final static Stroke[] hi = new Stroke[]{
        new BasicStroke(6), new BasicStroke(4), new BasicStroke(2), new BasicStroke(1)
    };
    private final static Color[] hiColOut = new Color[]{
        Color.red.darker().darker(), Color.red.darker(), Color.red, Color.red.brighter()
    };
    public final Color highCol1 = new Color(255, 0, 0, 255);
    public final Color highCol2 = new Color(255, 255, 0, 128);

    private void paintHighlight(Graphics2D g) {
        Rectangle rc = new Rectangle();
        ort2Rect(highlightPos, rc);
        rc.grow(-3, -3);
        for (int i = 0; i < hi.length; i++) {
            g.setColor(hiColOut[i]);
            g.setStroke(hi[i]);
            g.drawOval(rc.x, rc.y, rc.width, rc.height);
        }

        Paint p = new GradientPaint(rc.x, rc.y, highCol1, rc.x + rc.width, rc.y + rc.height, highCol2);
        g.setPaint(p);
        rc.grow(-1, -1);
        g.fillOval(rc.x, rc.y, rc.width, rc.height);
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






    public void paintComponent(Graphics g) {

        // Blit the board (it's already scaled)
        if (preBoard == null) {
         //HS  preBoard = getBoardImage();
        }
        g.drawImage(preBoard, 0, 0, this);

        // draw the active elements (robos)
        Graphics2D dbg = (Graphics2D) g;
        paintHighlight(dbg);

        dbg.scale(dScale, dScale);
        dbg.setComposite(AC_SRC);
        paintScout(dbg);
    // HS in BotCanvas    paintRobos(dbg);
    }


    protected void finalize() throws Throwable {
        super.finalize();
        g_off.dispose();
    }

   

    protected Location[] getFlags() {
        return sf.getFlags();
    }









  

}

