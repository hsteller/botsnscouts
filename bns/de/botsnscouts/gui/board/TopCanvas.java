/*
 * Created on 19.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.botsnscouts.gui.board;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;

import org.apache.log4j.Category;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TopCanvas extends Canvas implements Scalable, DrawingConstants {
    Category CAT = Category.getInstance(TopCanvas.class);
    
    private static final Color HIGH_COL_1 = new Color(255, 0, 0, 255);
    private static final Color HIGH_COL_2 = new Color(255, 255, 0, 128);
    
    private static final Stroke[] HI_STROKE = new Stroke[]{
                    new BasicStroke(6), new BasicStroke(4), new BasicStroke(2), new BasicStroke(1)
                };
    private static final  Color[] HI_COL_OUT = new Color[]{
                    Color.red.darker().darker(), Color.red.darker(), Color.red, Color.red.brighter()
                };
    
    private SimBoard gameboard;
    
    private double  dScale = 1.0;
    private double scaledFeldSize; // FELDSIZE * scale
    
    // for internal use. see repaintOrt()
    private Rectangle rc = new Rectangle();
    /** position to highlight*/
    private Location highlightPos = new Location(0, 0);
    /** last position of our famous scout ;-) */
    private Location lastScoutPos = new Location();
    

    /** This robot is used for calculations,
     *  like making a suggestion for the next move.
     */
    private Bot previewRob;
    
    private Image[] scoutImages;
    
    private BufferedImage blank = null;
    
    protected TopCanvas (SimBoard gameBoard, Image[] scoutImages){
        this.gameboard = gameBoard;
        this.scoutImages = scoutImages;
    }
    
               
    
    public void setScale(double scale){
        dScale = scale;
        scaledFeldSize = (dScale * FIELDSIZE_IN_PIXELS);
        blank = null;
    }
    
           

    /** Triggert ein Neuzeichnen des Feldes mit den \uFFFDbergebenen
     *  Koordinaten. N\uFFFDtzlich um einzelne Felder neuzeichnen zu lassen
     */

    private void repaintOrt(Location ort) {
        ort2Rect(ort, rc);
        repaint(1, rc.x, rc.y, rc.width, rc.height);
    }

    private void repaintOrt(int x, int y) {
        ort2Rect(x, y, rc);
        repaint(1, rc.x, rc.y, rc.width, rc.height);
    }

    private void unhighlight() {
        highlightPos.x = 0;
        highlightPos.y = 0;
        repaint();
    }

    private final javax.swing.Timer t = new javax.swing.Timer(5000, new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            unhighlight();
        }
    });


    protected void highlight(int x, int y) {
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

        int robX = previewRob.getX()-1;
        int robY = previewRob.getY();
        
        if (robX == 0 && robY == 0)
            return;       
        
        int sizeY = gameboard.getSizeY();
        int xpos = robX - 1;
        int ypos = sizeY- robY;
        
        int xpos64 = xpos * 64;
        int ypos64 = ypos * 64;
        // Scout
        AlphaComposite ac = AC_SRC_OVER_07;
        g2d.setComposite(ac);
        g.drawImage(scoutImages[previewRob.getFacing()], xpos64, ypos64, 64, 64, this);
        g2d.setComposite(AC_SRC);
    }


    /** Berechnet zu einem Location das Rechteck, das die Kachel umschliesst */
    private void ort2Rect(Location ort, Rectangle dest) {
        ort2Rect(ort.x, ort.y, dest);
    }

    private void ort2Rect(int x, int y, Rectangle dest) {
        dest.x = (int) ((x - 1) * scaledFeldSize);
        dest.y = (int) ((gameboard.getSizeY() - y) * scaledFeldSize);
        dest.width = (int) scaledFeldSize;
        dest.height = (int) scaledFeldSize;
    }

    
    


  /*  protected void preview(int phase, Bot simRob) {
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
            //gameboard.doPhase(phase, simRob);
            gameboard.doPhase(phase, robs);
        }
        //previewRob = vorschauRobArray[0];
        previewRob = simRob;
        showScout(simRob.getPos());

        //repaint();

    }

    */
    
    private void paintHighlight(Graphics2D g) {
        Rectangle rc = new Rectangle();
        ort2Rect(highlightPos, rc);
        rc.grow(-3, -3);
        for (int i = 0; i < HI_STROKE.length; i++) {
            g.setColor(HI_COL_OUT[i]);
            g.setStroke(HI_STROKE[i]);
            g.drawOval(rc.x, rc.y, rc.width, rc.height);
        }

        Paint p = new GradientPaint(rc.x, rc.y, HIGH_COL_1, rc.x + rc.width, rc.y + rc.height, HIGH_COL_2);
        g.setPaint(p);
        rc.grow(-1, -1);
        g.fillOval(rc.x, rc.y, rc.width, rc.height);
    }
    
    
    protected void preview(int phasen, Bot[] previewRoboters) {
        if (phasen == 0) {
            previewRob = null;
            deleteScout();
            //repaint();
            return;
        }

        for (int i = 1; i < phasen + 1; i++) {
            gameboard.doPhase(i, previewRoboters);
        }
        previewRob = previewRoboters[0];
        showScout(previewRob.getPos());
        //repaint();

    }
    
    
    
    public void paintComponent(Graphics g) {

        // Blit the board (it's already scaled)
        if (blank == null) {
            blank = BoardLayers.getBlankImage(dScale, gameboard); 
        }
        g.drawImage(blank, 0, 0, this);

        // draw the active elements (robos)
        Graphics2D dbg = (Graphics2D) g;
        if (highlightPos.x >0 && highlightPos.y > 0 )
            paintHighlight(dbg);

        dbg.scale(dScale, dScale);
        dbg.setComposite(AC_SRC);
        paintScout(dbg);
   
    }
    
    

    
    
    
}
