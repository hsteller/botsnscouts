/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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


/*
 * Created on 04.09.2005
 *
 */
package de.botsnscouts.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import sun.reflect.Reflection;

import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class BnsOptionPane extends JOptionPane{

    
   
    private BnsOptionPane(){
        
    }
    
    
    
    
    public static int getIntForReturnValue(JOptionPane pane){
        Object val = pane.getValue();
        int intval;
        if (val != null){
           intval =  ((Integer)val).intValue();
        }
        else {
            intval = JOptionPane.CLOSED_OPTION;
        }
        return intval;
        
    }
    
    public int bnsShowConfirmDialog( Component parent, 
                    String title, Object messageToShow) {
        	return bnsShowConfirmDialog(this, parent, title, messageToShow);
    }
    
    public static int bnsShowConfirmDialog(JOptionPane opt, Component parent, 
                                                              String title, Object message) {        
		opt.setOpaque(false);		
		opt.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		opt.setMessageType(JOptionPane.QUESTION_MESSAGE);
		opt.setMessage(message);
		
		JDialog dialog = opt.createDialog(parent, title);
		
		Collection comps = getAllChildComponents(opt, JComponent.class);
		for (Iterator it = comps.iterator(); it.hasNext();){
		    JComponent comp = (JComponent)it.next();
		    comp.setOpaque(false);
		}	
		dialog.pack();
		dialog.setVisible(true);
		return getIntForReturnValue(opt);    
    }
    
    /**
     * Creates a list of all of a Container's components by traversing the
     *  (child) component tree.   
     * 
     * @param traversalRoot the GUI element to start the traversion
     * @return A list of all child components of <code>traversatRoot</code>
     */
    public static Collection getAllChildComponents(Container traversalRoot){
        return getAllChildComponents(traversalRoot, null);
    }
    /**
     * Creates a list of some or all  of a Container's components by traversing the
     *  (child) component tree.   
     * 
     * @param classfilter only include components that are instances of <code>classfilter</code>;
     *               set NULL if you want an unfiltered list of all components.  
     * @param traversalRoot the GUI element to start the traversion
     * @return A list of all child components of <code>traversatRoot</code> that are an instance of <code>filter</code>
     */
    public static Collection getAllChildComponents(Container traversalRoot, Class classfilter){
        LinkedList comps = new LinkedList();
        addComponentsOf(traversalRoot, comps, classfilter);
        return comps;
    }
    
    private static void addComponentsOf(Container comp, Collection toAddTo, Class filter){
        
        Component [] comps = comp.getComponents();
        int count = comps!=null?comps.length:0;
        for (int i=0;i<count;i++){
            Component cur = comps[i];
            if (filter == null || filter.isInstance(cur))  {
                toAddTo.add(cur);        
            }            
            if (cur instanceof Container) {
                addComponentsOf((Container)cur, toAddTo, filter);
            }
        }
        
    }
    
   
    public static BnsOptionPane createColoredOptionPane(Color color) {
        final Color finalColor = color;
        BnsOptionPane pane = new BnsOptionPane(){
            public void paint(Graphics g) {
                g.setColor(finalColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
        return pane;
        
    }
    
    
    public  static JOptionPane createPaintedOptionPane () {
        return createPaintedOptionPane(false);
    }
    public  static JOptionPane createShadedPaintedOptionPane () {
        return createPaintedOptionPane(true);
    }
    public static BnsOptionPane createPaintedOptionPane ( boolean shade) {
       
        final boolean finalShade = shade;        
        BnsOptionPane pane = new BnsOptionPane(){
            Paint myPaint = OptionPane.getBackgroundPaint(this);            
            public void paintComponent(Graphics g) {
	    		Graphics2D g2d = (Graphics2D) g;
	    		Dimension d = getSize();	
	    		g2d.setPaint(myPaint);
	    		g2d.fillRect(0,0, d.width, d.height);
	            if( finalShade ) {
	                g2d.setPaint(PaintPanel.color);
	                g2d.fillRect(0,0, d.width, d.height);
	            }
            }                    
        };
        return pane;
    }
    
   
    
   public static void main (String [] args){
       TJLabel msg = new TJLabel("bla fasel foo bar yaddayadda");
       JOptionPane colorPane = createColoredOptionPane(GreenTheme.getBnsBackgroundColor());
       JOptionPane imagePane = createPaintedOptionPane(true);
       BnsOptionPane.bnsShowConfirmDialog(colorPane, null,"window title color", msg);
       BnsOptionPane.bnsShowConfirmDialog(imagePane, null,"window title image", msg);
       
   }
    
    
    
    
    
}

