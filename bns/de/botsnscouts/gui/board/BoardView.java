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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JLayeredPane;

import org.apache.log4j.Category;

import de.botsnscouts.board.Board;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Location;

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

   

 








  


   
 
    


  
    
   

   

    

  
   


    


    

   

  

 

   

    

    


    
   
    protected Image getRobImage(Bot robot, int facing) {
        int botVis = robot.getBotVis();
        return robosCrop[facing + botVis * 4];
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









    protected void finalize() throws Throwable {
        super.finalize();
        g_off.dispose();
    }

   

    protected Location[] getFlags() {
        return sf.getFlags();
    }









  

}

