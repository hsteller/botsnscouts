package de.botsnscouts.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.*;


public class TransparentButton extends JButton {
    static Font font = new Font("Sans", Font.BOLD, 24);
    static Stroke normalStroke = new BasicStroke(4);
    static Stroke pressedStroke = new BasicStroke(8);
    

      public TransparentButton(String s) {
	super(s);
	setRolloverEnabled( true );
	setOpaque( false );
	setFont( TransparentButton.font );
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
	 
	  FontMetrics fm = g2d.getFontMetrics(); 
	  int sw = fm.stringWidth(getText());
	  int sh = fm.getAscent();
   	  g2d.setColor( Color.black );
	  g2d.drawString( getText(), (width-sw)/2, (height+sh)/2);
      }
     
      public void paint(Graphics g) {
	if( getModel().isPressed() ) {
	  mypaint(g, Color.green ) ;
	} else if( getModel().isRollover() ) {
	  mypaint(g, Color.green );
	} else {
	  mypaint(g, Color.green.darker().darker() );
	}
      }

}
