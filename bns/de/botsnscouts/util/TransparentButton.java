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
 
package de.botsnscouts.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.*;


public class TransparentButton extends JButton {

    static Font font = new Font("SansSerif", Font.BOLD, 24);
    static Stroke normalStroke = new BasicStroke(4);
    static Stroke pressedStroke = new BasicStroke(8);


      public TransparentButton(String s) {
	super(s);
	setRolloverEnabled( true );
	setOpaque( false );
	setFont( TransparentButton.font );
      }

    private final static Stroke[] hi = new Stroke[] {
        new BasicStroke(6), new BasicStroke(4), new BasicStroke(2), new BasicStroke(1)
    };

    private void mypaint2(Graphics _g, Color col) {
        Graphics2D g = (Graphics2D) _g;
        Dimension d = getSize();
	int x = 0, y=0;
	int width = d.width;
	int height = d.height;
        Rectangle rc = new Rectangle( x, y, width, height );
        //rc.grow(-3,-3);
        Color c = Color.black;
        for(int i = getModel().isPressed() ? 1 : 2; i < hi.length; i++ ) {
            g.setColor( c );
            g.setStroke( hi[i] );
            g.drawRect( rc.x, rc.y, rc.width, rc.height );
            c = c.brighter();
        }

        Color c1 = new Color( col.getRed(), col.getGreen(), col.getBlue(), 200 );
        Color c2 = new Color( col.getRed()/2, col.getGreen()/2, col.getBlue()/2, 128 );
        Paint p = new GradientPaint( rc.x, rc.y, c1, rc.x + rc.width, rc.y + rc.height * 2, c2 );
        g.setPaint( p );
        rc.grow(-3, -3);
        g.fillRect( rc.x, rc.y, rc.width, rc.height );
        drawText( g, rc.width, rc.height );
    }


      public void mypaint(Graphics g, Color c) {
	  Graphics2D g2d = (Graphics2D) g;
	  Dimension d = getSize();
	  int x = 0, y=0;
	  int width = d.width;
	  int height = d.height;


	  AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	  g2d.setComposite( ac );
	  g2d.setColor( c );
	  g2d.fillRoundRect(0,0, width, height, 8, 8);
	  g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	  g2d.setColor( Color.black );
	  g2d.setStroke( getModel().isPressed() ?
			 pressedStroke : normalStroke );
	  g2d.drawRoundRect(0,0, width, height, 8, 8);
          drawText( g2d, width, height );
      }

     void drawText( Graphics2D g2d, int width, int height ) {
	  FontMetrics fm = g2d.getFontMetrics();
	  int sw = fm.stringWidth(getText());
	  int sh = (fm.getAscent()); // * 90) / 100;
   	  g2d.setColor( Color.black );
	  g2d.drawString( getText(), (width-sw)/2, (height+sh)/2);
      }

      public void paint(Graphics g) {
	if( getModel().isPressed() ) {
	  mypaint2(g, Color.green ) ;
	} else if( getModel().isRollover() ) {
	  mypaint2(g, Color.green );
	} else {
	  mypaint2(g, Color.green.darker() );
	}
      }

}
