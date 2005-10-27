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


package de.botsnscouts.widgets;



import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Document;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJTextField extends JTextField {
    public TJTextField(String text) {
        this(text, CENTER, false);
    }

    public TJTextField(String text, int alignment, boolean big) {
        super(text, alignment);
        init(big);
    }
    public TJTextField(int size){
        super(size);
        init(false);                
    }
    public TJTextField(String text,int size){
        super(text, size);
        init(false);                
    }
    public TJTextField(){
        super();
        init(false);                
    }
    
    public TJTextField(Document doc, String text, int columns){
        super(doc, text, columns);
        init(false);
    }
    
    protected void init(boolean useBigFont) {
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());
        setCaretColor(GreenTheme.getBnsCaretColor());
        setDisabledTextColor(MetalLookAndFeel.getControlDisabled());
        
        if (useBigFont) {
            setFont(GreenTheme.getBigFont());
        } else {
            setFont(GreenTheme.getFont());
        }
    }

}

