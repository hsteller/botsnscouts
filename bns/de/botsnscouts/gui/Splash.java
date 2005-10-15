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

package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JLabel;

import de.botsnscouts.BotsNScouts;

/**
 * This class is used to produce the Splash Screen,
 * The displayed image can be found at: images/logosmall.jpg
 * Like the image location the dimension of this window is
 * hard coded and can be found here
 * @version $Id$
 */
public class Splash{
    public static final int WIDTH = 468;
    public static final int HEIGHT = 115;
    private Window splash;
    private Frame dummy;
    private JLabel textLabel;
    
    /**
     * Creates a splash screen. The imaged used is images/logosmall.jpg
     * To display the splash please use showsplash(true)
     */
    public Splash (){
        dummy = new Frame();
        splash = new Window(dummy);    	
        textLabel=new JLabel(); 
		Dimension screen = BotsNScouts.getScreenSize();
		int x = (screen.width-WIDTH)/2;
		int y = (screen.height-HEIGHT)/3;
		splash.setBounds(x,y,WIDTH,HEIGHT);
	
		ImageCanvas ic=new ImageCanvas(Toolkit.getDefaultToolkit().getImage(de.botsnscouts.BotsNScouts.class.getResource("images/logosmall.jpg")));
		splash.add(ic,BorderLayout.CENTER);
		splash.add(textLabel,BorderLayout.SOUTH);
	    splash.pack();
    }
  
    /**
     * Is only used in Ausgabe.java
     * This might be a point for refactoring
     * @param s
     */
    public void setText(String s){        
		textLabel.setText(s);		
		splash.repaint();
    }
  
    /**
     * Shows/Hides the Splash Screen
     * @param visible whether the splash should be visible or not
     */
    public void showSplash(boolean visible){				
        if (splash != null) {
            splash.setVisible(visible);
        }
    }
    /**
     * Is identical to showSplash(false);
     * and should be removed
     */
    public void noSplash(){
       showSplash(false);       
    }
    /**
     * Paints the Splash
     */
    private class ImageCanvas extends Canvas{
		private Image im;
		private Dimension d=new Dimension(Splash.WIDTH,Splash.HEIGHT);
		public ImageCanvas(Image im){ 
		    this.im=im; 
		}
		public void paint(Graphics g){
		    g.drawImage(im,0,0,im.getWidth(this),im.getHeight(this),this);
		}
		
		public Dimension getPreferredSize(){
		    return d;
		}
    }
}
