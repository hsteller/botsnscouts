/*
 * Created on 19.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.botsnscouts.gui.board;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import de.botsnscouts.board.Board;
import de.botsnscouts.board.Floor;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FloorCanvas extends Canvas implements DrawingConstants, Scalable{

    
    


    
    
    /** gameboard object;
     *  stores the information about the board we are playing on;
     *  (where are the pits, where are lasers, and so on..)
     */
   private  SimBoard gameboard;
   

   private  BufferedImage preBoard;
   
   
   /** maps Location(x,y) to the Image that should be painted as floor*/
   private HashMap floorElementHash = new HashMap();
    
    /** Zooming factor for scaling the internal Graphics2D object*/ 
   private double dScale;

    
    private Image[] beltImages;
    private Image [] expressBeltImages;
    private Image [] miscImages;
    
    // for painting crushers
    private static final int[] crushlb_x = {20, 30, 30, 30, 40};
    private static final int[] crushlb_y = {35, 25, 35, 45, 35};
    
    protected FloorCanvas(SimBoard sb, Image[] beltImages, Image [] expressBeltImages, Image [] miscImages){
       gameboard = sb;
       this.beltImages = beltImages;
       this.expressBeltImages = expressBeltImages;
       this.miscImages = miscImages;
       initFloorHashMap();
    }
    private BufferedImage getBoardImageFloor() {
        //preBoard = new BufferedImage(x,y, BufferedImage.TYPE_BYTE_INDEXED);
        Dimension dim = BoardLayers.calcBoardDimensionInPixel(dScale,gameboard);
        int width = (int) dim.getWidth();
        int height = (int) dim.getHeight();
        BufferedImage bi = new BufferedImage(width, height,  BufferedImage.TYPE_INT_RGB);
        Graphics2D g_off = (Graphics2D) bi.getGraphics();
        g_off.setClip(0, 0, width,height);
        g_off.scale(dScale, dScale);
        paintSpielfeldBoden(g_off);
        g_off.dispose();
        return bi;
    }
    
    public void paintComponent(Graphics g) {
        
        // Blit the board (it's already scaled)
        if (preBoard == null) {
            preBoard = getBoardImageFloor();
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

    
  

    /** paints the (back-)ground of the board*/
    protected void paintSpielfeldBoden( Graphics g2) {
        SimBoard board = gameboard;
        Graphics2D g = (Graphics2D) g2;
        int sizeX = board.getSizeX();
        int sizeY = board.getSizeY();
     // XXX HS noch nötig?   g.setComposite(AC_SRC_OVER);
        // Grenzen des zu zeichnenden Bereichs berechnen:
        Rectangle clip = g.getClipBounds();
        int x0 = clip.x / 64 + 1;
        int y0 = clip.y / 64 + 1;
        int x1 = (clip.x + clip.width - 1) / 64 + 1;
        int y1 = (clip.y + clip.height - 1) / 64 + 1;
        x1 = Math.min(x1, sizeX);
        y1 = Math.min(y1, sizeY);

        for (int hori = x0; hori <= x1; hori++) {
            for (int vert = y0; vert <= y1; vert++) {
                int actx = (hori - 1) * 64;
                int acty = (vert - 1) * 64;
                int xpos = hori;
                int ypos = sizeY + 1 - vert;
                Floor floor = board.floor(xpos, ypos);

                paintFeldBoden(g, xpos, ypos, actx, acty);
                if ((floor.isBelt()) && (floor.getInfo() > 0))
                    paintCrusher(g, floor, actx, acty);
            }
        }
    }
    
    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty) {
        paintFeldBoden(g, xpos, ypos, actx, acty, 64, 64);
    }
    
    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty,
                    int width, int height) {

        	Location l = new Location(xpos, ypos);
			Image img = (Image) floorElementHash.get(l);
			g.drawImage(img, actx, acty, width, height, this);
    }
    
//  TODO: Make method private again and find a proper way to update the hash map if nec.
    public void initFloorHashMap() {
         int sizeX = gameboard.getSizeX();
         int sizeY = gameboard.getSizeY();
         for (int x = 1; x <= sizeX; x++) {
             for (int y = 1; y <= sizeY; y++) {
                 Location l = new Location(x, y);
                 Image img = getFloorImage(x, y);
                 floorElementHash.put(l, img);
             }
         }
     }
    
    private Image getFloorImage(int xpos, int ypos) {
        Floor floor = gameboard.floor(xpos, ypos);
        switch (floor.getType()) {

            case (Board.FL_PIT):
                return miscImages[3];
            case (Board.FL_NORMAL):
                return miscImages[24 + ((xpos * ypos * 19) % 17) % 4];
            case (Board.FL_ROTGEAR):
                if (floor.getInfo() == 0)
                    return miscImages[2];
                else
                    return miscImages[1];
            case (Board.FL_REPAIR):
                if (floor.getInfo() == 1)
                    return miscImages[4];
                else
                    return miscImages[5];
                // ------------------- normal belts -------------------------

            case (Board.FN1):
                return beltImages[14];
            case (Board.FE1):
                return beltImages[19];
            case (Board.FW1):
                return beltImages[9];
            case (Board.FS1):
                return beltImages[4];

            case (Board.NFW1):
                if (turner(xpos, ypos - 1, Board.NORTH))
                    return beltImages[15];
                else
                    return beltImages[6];
            case (Board.NFE1):
                if (turner(xpos, ypos - 1, Board.NORTH))
                    return beltImages[18];
                else
                    return beltImages[7];
            case (Board.SFW1):
                if (turner(xpos, ypos + 1, Board.SOUTH))
                    return beltImages[13];
                else
                    return beltImages[3];
            case (Board.SFE1):
                if (turner(xpos, ypos + 1, Board.SOUTH))
                    return beltImages[10];
                else
                    return beltImages[0];
            case (Board.EFN1):
                if (turner(xpos - 1, ypos, Board.EAST))
                    return beltImages[16];
                else
                    return beltImages[5];
            case (Board.EFS1):
                if (turner(xpos - 1, ypos, Board.EAST))
                    return beltImages[12];
                else
                    return beltImages[2];
            case (Board.WFN1):
                if (turner(xpos + 1, ypos, Board.WEST))
                    return beltImages[17];
                else
                    return beltImages[8];
            case (Board.WFS1):
                if (turner(xpos + 1, ypos, Board.WEST))
                    return beltImages[11];
                else
                    return beltImages[1];

            case (Board.NFEW1):
                return beltImages[22];
            case (Board.SFWE1):
                return beltImages[20];
            case (Board.EFNS1):
                return beltImages[23];
            case (Board.WFNS1):
                return beltImages[21];

                // ------------------------  express belts ---------------------

            case (Board.FN2):
                return expressBeltImages[14];
            case (Board.FE2):
                return expressBeltImages[19];
            case (Board.FW2):
                return expressBeltImages[9];
            case (Board.FS2):
                return expressBeltImages[4];

            case (Board.NFW2):
                if (turner(xpos, ypos - 1, Board.NORTH))
                    return expressBeltImages[16];
                else
                    return expressBeltImages[6];
            case (Board.NFE2):
                if (turner(xpos, ypos - 1, Board.NORTH))
                    return expressBeltImages[17];
                else
                    return expressBeltImages[7];
            case (Board.SFW2):
                if (turner(xpos, ypos + 1, Board.SOUTH))
                    return expressBeltImages[13];
                else
                    return expressBeltImages[3];
            case (Board.SFE2):
                if (turner(xpos, ypos + 1, Board.SOUTH))
                    return expressBeltImages[10];
                else
                    return expressBeltImages[0];
            case (Board.EFN2):
                if (turner(xpos - 1, ypos, Board.EAST))
                    return expressBeltImages[15];
                else
                    return expressBeltImages[5];
            case (Board.EFS2):
                if (turner(xpos - 1, ypos, Board.EAST))
                    return expressBeltImages[12];
                else
                    return expressBeltImages[2];
            case (Board.WFN2):
                if (turner(xpos + 1, ypos, Board.WEST))
                    return expressBeltImages[18];
                else
                    return expressBeltImages[8];
            case (Board.WFS2):
                if (turner(xpos + 1, ypos, Board.WEST))
                    return expressBeltImages[11];
                else
                    return expressBeltImages[1];


            case (Board.NFWE2):
                return expressBeltImages[22];
            case (Board.SFWO2):
                return expressBeltImages[20];
            case (Board.EFNS2):
                return expressBeltImages[23];
            case (Board.WFNS2):
                return expressBeltImages[21];


            default:
                return null;
        }

    }
    
    private boolean turner(int x, int y, int r) {
        Floor floor = gameboard.floor(x, y);
        return floor.isBelt() && (floor.getBeltDirection() == r);
    }
    
    private void paintCrusher(Graphics2D g, Floor floor,
                    int actx, int acty) {

        	// XXX HS g.setComposite(AC_SRC_OVER);
        	g.drawImage(miscImages[10], actx, acty, 64, 64, this);
        	g.setColor(Color.white);
			for (int phasecount = 1; phasecount <= 5; phasecount++) {
			  if (floor.isCrusherActive(phasecount)) {
			      int strx = actx + crushlb_x[phasecount - 1];
			      int stry = acty + crushlb_y[phasecount - 1];
			      g.drawString("" + phasecount, strx, stry);
			  }
			} //for
	}
    
    protected  void replaceGameboard(SimBoard sfs) {
        gameboard= sfs;               
        initFloorHashMap();
    }
    
    public void setScale(double scale){
        this.dScale = scale;
    }
    
   
    
}
