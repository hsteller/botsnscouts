/*
 * Created on 19.10.2004
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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Enumeration;

import org.apache.log4j.Category;

import de.botsnscouts.board.LaserDef;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.board.Wall;
import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ElementCanvas extends Canvas implements DrawingConstants, Scalable{
   
    private static Category CAT = Category.getInstance(ElementCanvas.class);
   
    private SimBoard gameboard; 
    private Image [] miscImages;
    private  BufferedImage preBoard;
    private double dScale = 1.0;
    
    
    public ElementCanvas(SimBoard board, Image [] miscImages){
        this.gameboard = board;
        this.miscImages = miscImages;
    }
    
    protected void replaceGameboard (SimBoard newBoard) {
        this.gameboard = newBoard;
        
    }
    
    
    protected void paintUnbuffered(Graphics dbg) {
        
        paintLaserRays(dbg);
        paintWalls(dbg); 
        paintFlags(dbg);
    
    }
    
    
    public static Color getLaserColor(int laserStrength){

        switch (laserStrength) {
	        case 1:
	            return Color.RED.brighter();  
	        case 2:
	           return Color.orange;
	        case 3:
	            return Color.yellow;
	         default : {
	             CAT.error("Unknown value for laser strength: "+laserStrength);
	             return Color.RED.brighter();
	         }
        }
           
    }
    
    /** Paints the boardlaser-elements*/
    private void paintLaserRays(Graphics g) {
        Graphics2D dbg = (Graphics2D) g;
        AlphaComposite ac = null;
        //	if (activeBordLasers)
        // ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        //else
        ac = AC_SRC_OVER_05;
        dbg.setComposite(ac);

        LaserDef actuallaser;
        int ySize = gameboard.getSizeY();
        for (Enumeration e = gameboard.getLasers().elements(); e.hasMoreElements();) {
            actuallaser = ((LaserDef) e.nextElement());
            int lx = actuallaser.x - 1;
            int ly = ySize - actuallaser.y;
            int lf = actuallaser.facing;
            int ll = actuallaser.length;
            
            dbg.setColor(getLaserColor(actuallaser.strength));

            switch (lf) {
                case 0:
                    dbg.fillRect(lx * 64 + 30, (ly - ll + 1) * 64, 4, ll * 64);
                    break;
                case 1:
                    dbg.fillRect(lx * 64, ly * 64 + 30, ll * 64, 4);
                    break;
                case 2:
                    dbg.fillRect(lx * 64 + 30, ly * 64, 4, ll * 64);
                    break;
                case 3:
                    dbg.fillRect((lx - ll + 1) * 64, ly * 64 + 30, ll * 64, 4);
                    break;
            }
        }
        dbg.setComposite(AC_SRC);
    }
    
    private void paintWalls(Graphics g2) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite(AC_SRC_OVER);

        int sizeX = gameboard.getSizeX();
        int sizeY = gameboard.getSizeY();
        
        // Grenzen des zu zeichnenden Bereichs berechnen:
        Rectangle clip = g.getClipBounds();
        int x0 = clip.x / 64 + 1;
        int y0 = clip.y / 64 + 1;
        int x1 = (clip.x + clip.width - 1) / 64 + 1;
        int y1 = (clip.y + clip.height - 1) / 64 + 1;
        x1 = Math.min(x1, sizeX);
        y1 = Math.min(y1,sizeY);

        // Zeichnen
        for (int hori = x0; hori <= x1; hori++) {
            for (int vert = y0; vert <= y1; vert++) {
                int actx = hori * 64 - 64;
                int acty = vert * 64 - 64;
                int xpos = hori;
                int ypos = sizeY - vert + 1;
                paintSingleWall(g2, xpos, ypos, actx, acty);
            }
        }

    }
    
    
    
    
    /** Paints the wall(s) of a square field at position (xpos, ypos)
    on board and (pixel-)position (actx, acty)
    */
   private void paintSingleWall(Graphics g, int xpos, int ypos, int actx, int acty) {
       // paint wall in the north, if any
      // Graphics2D g2 = (Graphics2D)g;
       
   	if (gameboard.nw(xpos, ypos).isExisting()) {
           // is there a boardlaser to paint at this wall?
           if (gameboard.nw(xpos, ypos).getSouthDeviceType() == Wall.TYPE_LASER) {
               g.drawImage(miscImages[15], actx, acty + 5, 64, 64, this);
           }
           // is there a pisher?
           if (gameboard.nw(xpos, ypos).getSouthDeviceType() == Wall.TYPE_PUSHER) {
               g.drawImage(miscImages[7], actx - 1, acty + 5, 64, 64, this);
               // ------------draw text (phases when active) on pusher --------------------
               for (int phasecount = 1; phasecount <= 5; phasecount++) {
                   if (gameboard.nw(xpos, ypos).isSouthPusherActive(phasecount)) {
                       int strx = actx + 10 * phasecount;
                       g.setColor((phasecount % 2) == 0 ?
                               Color.black : Color.yellow);
                       g.drawString("" + phasecount, strx - 1, acty + 29);
                   }
               }

           }
           g.drawImage(miscImages[13], actx, acty - 6, 64, 64, this);
       }

       // paint wall in the south, if any
       if (gameboard.sw(xpos, ypos).isExisting()) {
           if (gameboard.sw(xpos, ypos).getNorthDeviceType() == Wall.TYPE_LASER) {
               g.drawImage(miscImages[17], actx, acty - 5, 64, 64, this);
           }
           if (gameboard.sw(xpos, ypos).getNorthDeviceType() == Wall.TYPE_PUSHER) {
               g.drawImage(miscImages[8], actx, acty - 5, 64, 64, this);
               // -----------text on pusher--------------------
               for (int phasecount = 1; phasecount <= 5; phasecount++) {
                   if (gameboard.sw(xpos, ypos).isNorthPusherActive(phasecount)) {
                       int strx = actx + 10 * phasecount;
                       g.setColor((phasecount % 2) == 0 ?
                               Color.black : Color.yellow);
                       g.drawString("" + phasecount, strx - 1, acty + 42);
                   }
               } //for
           }
           g.drawImage(miscImages[13], actx, acty + 58, 64, 64, this);
       }

       // paint wall in the south, if any

       if (gameboard.ew(xpos, ypos).isExisting()) {
           if (gameboard.ew(xpos, ypos).getWestDeviceType() == Wall.TYPE_LASER) {
               g.drawImage(miscImages[14], actx - 6, acty, 64, 64, this);
           }
           if (gameboard.ew(xpos, ypos).getWestDeviceType() == Wall.TYPE_PUSHER) {
               g.drawImage(miscImages[6], actx - 6, acty, 64, 64, this);
               // ------------text on pusher --------------------
               for (int phasecount = 1; phasecount <= 5; phasecount++) {
                   if (gameboard.ew(xpos, ypos).isWestPusherActive(phasecount)) {
                       int stry = acty + 10 * phasecount;
                       g.setColor((phasecount % 2) == 0 ?
                               Color.black : Color.yellow);
                       g.drawString("" + phasecount, actx + 37, stry + 4);
                   }
               } //for

           }
           g.drawImage(miscImages[12], actx + 57, acty, 64, 64, this);
       }

       // paint wall in the west, if any
       if (gameboard.ww(xpos, ypos).isExisting()) {
           if (gameboard.ww(xpos, ypos).getEastDeviceType() == Wall.TYPE_LASER) {
               g.drawImage(miscImages[16], actx + 5, acty, 64, 64, this);
           }
           if (gameboard.ww(xpos, ypos).getEastDeviceType() == Wall.TYPE_PUSHER) {
               g.drawImage(miscImages[9], actx + 4, acty, 64, 64, this);
               // ------------Beschriftung --------------------
               for (int phasecount = 1; phasecount <= 5; phasecount++) {
                   if (gameboard.ww(xpos, ypos).isEastPusherActive(phasecount)) {
                       int stry = acty + 10 * phasecount;
                       g.setColor((phasecount % 2) == 0 ?
                               Color.black : Color.yellow);
                       g.drawString("" + phasecount, actx + 24, stry + 4);
                   }
               } //for

           }
           g.drawImage(miscImages[12], actx - 7, acty, 64, 64, this);
       }
   }
   
   public void paintComponent(Graphics g) {
       
       // Blit the board (it's already scaled)
       if (preBoard == null) {
           preBoard = getBoardImage();
       }
       g.drawImage(preBoard, 0, 0, this);

       // draw the active elements (robos)
       Graphics2D dbg = (Graphics2D) g;
       // XXX HS paintHighlight(dbg); -> nach Topcanvas
       
       dbg.scale(dScale, dScale);
    // XXX HS  dbg.setComposite(AC_SRC); -> noch nötig?
    //   paintScout(dbg); -> BotCanvas/TopCanvas
   //    paintRobos(dbg); -> BotCanvas
       
   }
   
   
   
   private BufferedImage getBoardImage (){
       //preBoard = new BufferedImage(x,y, BufferedImage.TYPE_BYTE_INDEXED);
       Dimension dim = BoardLayers.calcBoardDimensionInPixel(dScale,gameboard);
       int width = (int) dim.getWidth();
       int height = (int) dim.getHeight();
       BufferedImage bi = new BufferedImage(width, height,  BufferedImage.TYPE_INT_RGB);
       Graphics2D g_off = (Graphics2D) bi.getGraphics();
       g_off.setClip(0, 0, width,height);
       g_off.scale(dScale, dScale);
       paintUnbuffered(g_off);
       g_off.dispose();
       return bi;
   }
   
   
   
   public void setScale(double scale){
       this.dScale = scale;
   }
   
   private void paintFlags(Graphics g2) {

       Graphics2D g = (Graphics2D) g2;
       g.setComposite(AC_SRC_OVER);

       if (gameboard.getFlags() != null) {
           Location[] flaggen = gameboard.getFlags();
           for (int flaggencount = 0; flaggencount < flaggen.length; flaggencount++) {
               int xflagge = flaggen[flaggencount].x - 1;
               int yflagge = gameboard.getSizeY() - flaggen[flaggencount].y;
               g.drawImage(miscImages[18 + flaggencount],
                       xflagge * 64, yflagge * 64, 64, 64, this);
           }
       }
   }
}
