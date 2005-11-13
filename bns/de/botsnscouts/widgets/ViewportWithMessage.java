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

import javax.swing.JViewport;

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
public class ViewportWithMessage extends JViewport {

    
    
// default values might not make sense, only given to ensure that there is no NullPointerEx.
// it would be cool to calculate the size of the textbox by using textlength and font size but
// I'm not sure if that results in valid and good looking values on all operating systems.. 
   private boolean isTextToBeWritten = false;
   private String theText = "";
   
   private Color boxColor =  Color.GREEN;
   private Color textColor = Color.RED;
   private Color borderColor = Color.BLACK;
   private AlphaComposite boxComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
   private AlphaComposite textComposite = AlphaComposite.getInstance(AlphaComposite.SRC);
   // the following values were pulled out of my <insert body part>..:
   private Font textFont = new Font("times", Font.BOLD, 12);
   private Rectangle dimensionOfTextBox = new Rectangle(0,0,300,100);
   private int textInsetLeft = 25;
   
    public void paint (Graphics g){
         super.paint(g);
         if (!isTextToBeWritten) {
             return;
         }
         
         Graphics2D dbg = (Graphics2D) g;
         Font oldFont = dbg.getFont();
         Composite oldComp = dbg.getComposite();
         
         Rectangle rect = this.getVisibleRect();         
          int x1 = (int)rect.getCenterX();
          int y1 = (int)rect.getCenterY();
          int boxWidth = dimensionOfTextBox.width;
          int boxHeight = dimensionOfTextBox.height;
          
          // coordinates for the upper left corner of  the surrounding box:
          int boxX = x1 - boxWidth/2; 
          int boxY = y1 - textFont.getSize();
          // (x,y) coordinates for "drawString"
          int textX = boxX+textInsetLeft;	
          int textY  = y1;
          
          
          // painting the box, starting with the background:
          dbg.setComposite(boxComposite);
          dbg.setColor(boxColor);
          dbg.fillRect(boxX, boxY, boxWidth, boxHeight); 
          // painting the border of the box: 
          dbg.setComposite(textComposite);
          dbg.setColor(borderColor);
          dbg.drawRect(boxX, boxY, boxWidth, boxHeight); 
          // painting the text:
          dbg.setColor(textColor);          
          dbg.setFont(textFont);
          dbg.drawString(theText,textX,textY);
          
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
public Rectangle getDimensionOfTextBox() {
    return dimensionOfTextBox;
}
public void setDimensionOfTextBox(Rectangle dimensionOfTextBox) {
    this.dimensionOfTextBox = dimensionOfTextBox;
}
public int getTextInsetLeft() {
    return textInsetLeft;
}
public void setTextInsetLeft(int textInsetLeft) {
    this.textInsetLeft = textInsetLeft;
}
}

