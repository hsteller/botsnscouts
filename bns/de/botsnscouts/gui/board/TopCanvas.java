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
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JViewport;

import org.apache.log4j.Category;

import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TopCanvas extends Canvas implements Scalable, DrawingConstants {
    Category CAT = Category.getInstance(TopCanvas.class);
    
    private double  dScale = 1.0;
    
    public void setScale(double scale){
        dScale = scale;
    }
}
