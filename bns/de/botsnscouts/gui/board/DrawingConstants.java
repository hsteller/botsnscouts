/*
 * Created on 21.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.botsnscouts.gui.board;

import java.awt.AlphaComposite;
import java.awt.Color;

import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Directions;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface DrawingConstants {
    
//  Let's define some colors, so that everybody uses the same..
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
    /** size (length and width) of one little field in pixels*/
    static final int FIELDSIZE_IN_PIXELS = 64;
    
    
	  static final AlphaComposite AC_SRC = AlphaComposite.getInstance(AlphaComposite.SRC);
	  static final AlphaComposite AC_SRC_OVER = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	  static final AlphaComposite AC_SRC_OVER_05 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	  static final AlphaComposite AC_SRC_OVER_07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);

	    /** Constant for direction/facing north*/
	    static final int NORTH = Directions.NORTH;
	    /** Constant for direction/facing east*/
	    static final int EAST = Directions.EAST;
	    /** Constant for direction/facing south*/
	    static final int SOUTH = Directions.SOUTH;
	    /** Constant for direction/facing west*/
	    static final int WEST = Directions.WEST;

	    
	
	  
}
