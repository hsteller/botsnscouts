/*
 * Created on 16.10.2004
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
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JViewport;

import org.apache.log4j.Category;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.gui.Ausgabe;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BotCanvas extends Canvas implements DrawingConstants, Scalable{

    Category CAT = Category.getInstance(BotCanvas.class);
    
    private static final Location LOCATION_PIT = new Location(0, 0);
    
    private double  dScale = 1.0;
    private SimBoard gameboard;
    private BufferedImage blank;
    /** To lookup the color of a robot; contains name->color mapping.*/
    private java.util.Hashtable nameToColorHash;
    private boolean gotColors;
    
    private HashMap internalPositionHash = new java.util.HashMap();
    
    private Image [] botImages;
    
    /** Stores data of the robots.*/
    private Bot[] robos;

    /** During movement animation this robot must not be painted because
     * it will be painted in the AnimationCanvas
     * 
     */ 
    private Bot animatedBot = null;
    
    public BotCanvas(SimBoard board, Image [] botImages){
        this.gameboard = board;
        this.botImages = botImages;
        gotColors = false;
        
    }
    
    protected void ersetzeRobos(Bot[] robos_neu) {

        if (!gotColors) { // this is the first time I get the robots
            setRobColors(robos_neu);
            robos = robos_neu;
        }
        // we dont want to overwrite the robots positions, because they
        // have been updated in animateRobMove() before;
        // animateRobMove() gets informed earlier, so overwriting the positions
        // would reset the robot back to a position he has already left
        else {
            if (Ausgabe.IS_ROB_MOVE_ANIMATION_ENABLED) {
                for (int i = 0; i < robos.length; i++) // saving my internal robot positions
                    internalPositionHash.put(robos[i].getName(), robos[i].getPos());
                robos = robos_neu; // updating all robots

// replacing robot positions - if it was not destroyed -
                // with the positions we saved above
                for (int i = 0; i < robos.length; i++) {
                    Bot r = robos[i];
                    Location tmp = (Location) internalPositionHash.get(r.getName());

                    if (!(r.getPos().equals(LOCATION_PIT) || r.getDamage() >= 10 || tmp.equals(LOCATION_PIT))) {
                        // ^^^^^^^^^^^^^
                        // otherwise we would not show
                        // the destroyed robot ever again
                        // as we would ignore him if he
                        // is placed on the board again
                        if (CAT.isDebugEnabled()) {
                            CAT.debug("ignoring server position of robot " + r.getName()
                                    + " as my calculated position will be more accurate");
                        }
                        // use the internal kept position of our robot unless it is not
                        // destroyed
                        r.setPos(tmp);
                    } else {
                        if (CAT.isDebugEnabled())
                            CAT.debug("using server position of robot " + r.getName());
                    }

                }
            } else { // no animation
                robos = robos_neu;
            }
        }

        repaint();


    }
    
    public void setScale(double scale){
        dScale = scale;
        blank = null;
    }
    
    
    /** Lookup the Bot's color (by name)
    @param name The Bot's name
    @return The Bot's color. If the name is unknown, Color.white will be returned,
    */
   protected Color getRobColor(String name) {
       Color foo = null;
       foo = (Color) nameToColorHash.get(name);
       if (foo == null) {
           CAT.error("getRobColor: Color for " + name + "'s Laser not found");
           return Color.white;
       } else
           return foo;
   }
   
   /** Create "name->color" - Hashtable*/
   private void setRobColors(Bot[] robs) {
       gotColors = true;
       nameToColorHash = new java.util.Hashtable();
       for (int i = 0; i < robs.length; i++)
           if (robs[i] == null)
               break;
           else
               nameToColorHash.put(robs[i].getName(), ROBOCOLOR[robs[i].getBotVis()]);
   }
    
/** Will be called by AnimationCanvas during animation of robot movements.
 * During animation the animated bot must not be painted here it will be painted by
 * the AnimationCanvas.
 * @param bot the bot that must not be painted 
 */
   protected void setBotToBeHiddenForAnimation(Bot bot ){
       this.animatedBot = bot;
   }
   
   private void paintRobos(Graphics g) {
      
       paintRobos(g, animatedBot);
   }

  
   
   private void paintRobos(Graphics g, Bot dontPaintMe) {
       Graphics2D g2d = (Graphics2D) g;
       if (dontPaintMe == null) {
           if (robos != null) {
               for (int robocount = 0; robocount < robos.length; robocount++) {
                   Bot robot = robos[robocount];
                   if ((robot.getDamage() < 10) &&
                           (robot.getLivesLeft() > 0)) {
                       paintRobot(g2d, robot, robocount);
                   }
               }
           }
       } else {
           if (robos != null) {
               for (int robocount = 0; robocount < robos.length; robocount++) {
                   Bot robot = robos[robocount];
                   if ((robot.getDamage() < 10) &&
                           (robot.getLivesLeft() > 0) &&
                           !dontPaintMe.getName().equals(robot.getName())) {
                       paintRobot(g2d, robot, robocount);
                   }
               }
           }
       }

   }
   
   private void paintRobot(Graphics2D g2d, Bot robot, int robocount) {

       int xpos = robot.getX() - 1;
       int ypos = gameboard.getSizeY() - robot.getY();
       int xpos64 = xpos * 64;
       int ypos64 = ypos * 64;

       int botVis = robot.getBotVis();
       Image imgRob = botImages[robot.getFacing() + botVis * 4];
       boolean virtuell = robot.isVirtual();

       if (imgRob != null) {
           if (virtuell) {
               AlphaComposite ac = AC_SRC_OVER_05;
               g2d.setComposite(ac);
           }
           else {
           	g2d.setComposite(AC_SRC_OVER);
           }
           g2d.drawImage(imgRob, xpos64, ypos64, 64, 64, this);
           if (virtuell) {
               g2d.setComposite(AC_SRC);
           }
           String beschriftung = "" + robot.getName();
           g2d.setColor(ROBOCOLOR[botVis]);
           g2d.drawString(beschriftung, xpos64, ypos64 + 8 + robocount * 8);
       }
   }

   
   public void paintComponent(Graphics g) {

       if (blank== null) {
           blank = BoardLayers.getBlankImage(dScale, gameboard);
          }
          g.drawImage(blank, 0, 0, this);

      
       Graphics2D dbg = (Graphics2D) g;
       dbg.scale(dScale, dScale);
       paintRobos(dbg);
   }
  
  
   
}
