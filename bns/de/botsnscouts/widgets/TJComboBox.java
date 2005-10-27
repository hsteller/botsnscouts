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
 * Created on 03.09.2005
 *
 */
package de.botsnscouts.widgets;

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class TJComboBox extends JComboBox {
    
    public TJComboBox(String [] items) {
        super(items);
        initTheme();
    }
    
    public TJComboBox() {
	    super();
	    initTheme();
    }
    
    
    
    private void initTheme() {
        setOpaque(false);                        
        setForeground(GreenTheme.getTextColor());
	    setFont(GreenTheme.getFont());
    }
}

