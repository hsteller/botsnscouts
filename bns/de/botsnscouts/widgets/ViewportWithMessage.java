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
 * Created on 04.11.2005
 *
 */
package de.botsnscouts.widgets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JViewport;

import org.apache.log4j.Category;

/**
 * @author Hendrik Steller
 * @version $Id$
 * 
 * This is a basic JViewport with one additional feature:<br>
 * a text can be written across the content that is displayed in the viewport.<br> 
 * The text will be placed inside of a color filled rectangular box; colors, font and  transperency level<br>
 * of this textbox can be customized by using the appropriate setter methods.
 * 
 */
@SuppressWarnings("serial")
public class ViewportWithMessage extends JViewport {

          
   private boolean isTextToBeWritten = false;
   private String theText = "";
   private Font textFont = new Font("times", Font.BOLD, 12); // yeah, that's small and will be overwritten
   
   private Color boxColor =  Color.GREEN;
   private Color textColor = Color.RED;
   private Color borderColor = Color.BLACK;
   private AlphaComposite boxComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
   private AlphaComposite textComposite = AlphaComposite.getInstance(AlphaComposite.SRC);


  
    public void paint (Graphics g){
         super.paint(g);
         if (!isTextToBeWritten) {
             return;
         }
     
         Graphics2D dbg = (Graphics2D) g;
         Font oldFont = dbg.getFont();
         Composite oldComp = dbg.getComposite();
                           
         FontRenderContext frc = dbg.getFontRenderContext();
         TextLayout layout = new TextLayout(theText, textFont, frc);
         Rectangle2D textBounds = layout.getBounds();
         Rectangle rect = this.getVisibleRect();         
         int x1 = (int)rect.getCenterX();
         int y1 = (int)rect.getCenterY();
  
         int inset = 30;
         int textW =  (int) textBounds.getWidth();
         int textH =   (int) textBounds.getHeight();
         int textX =  x1 - (textW+inset)/2;
         int textY = y1 - (textH+inset)/2; 
         int boxX = textX - inset/2; 
         int boxY =  textY - inset/2 - textH; // "- textH": I guess the TextLayout y coordinate is the baseline of the text, not the y coordinate of its surrounding box..          
         int boxH = textH+inset;
         int boxW =textW+inset+10; // the width returned by bounds is a bit off (the text doesn't even fit in); 
                                                       // while the inset will make the text fit into the box, adding those 10 pixels will make it look a bit more centered
                  
          // painting the box, starting with the background:
          dbg.setComposite(boxComposite);
          dbg.setColor(boxColor);         
          dbg.fillRect(boxX,boxY,boxW,boxH);
          // painting the border of the box: 
          dbg.setComposite(textComposite);
          dbg.setColor(borderColor);
          dbg.drawRect(boxX, boxY, boxW, boxH); 
          // painting the text:
          dbg.setComposite(textComposite);
          dbg.setColor(textColor);       
          textBounds.setRect(textX, textY,textW,textH);
          layout.draw(dbg, textX, textY);
      
          // cleanUp, as BoardView (sometimes) assumes "sane" values atm (at least for the font)..
          dbg.setFont(oldFont);
          dbg.setComposite(oldComp);
       
    }
    
 
    
    public Color getBoxColor() {
        return boxColor;
    }
    public void setBoxColor(Color backgroundColor) {
        this.boxColor = backgroundColor;
    }
    public AlphaComposite getBoxComposite() {
        return boxComposite;
    }
    public void setBoxComposite(AlphaComposite backgroundComposite) {
        this.boxComposite = backgroundComposite;
    }
    public Color getTextColor() {
        return textColor;
    }
    public void setTextColor(Color foregroundColor) {
        this.textColor = foregroundColor;
    }
    public AlphaComposite getTextComposite() {
        return textComposite;
    }
    public void setTextComposite(AlphaComposite foregroundComposite) {
        this.textComposite = foregroundComposite;
    }
    public boolean isTextToBeWritten() {
        return isTextToBeWritten;
    }
    public void setTextToBeWritten(boolean isTextToBeWritten) {
        this.isTextToBeWritten = isTextToBeWritten;
    }
    public Font getTextFont() {
        return textFont;
    }
    public void setTextFont(Font textFont) {
        this.textFont = textFont;
    }
    public String getTheText() {
        return theText;
    }
    public void setTheText(String textToBeWritten) {
        this.theText = textToBeWritten;
    }    
    public Color getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

}

