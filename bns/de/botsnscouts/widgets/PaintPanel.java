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

package de.botsnscouts.widgets;

import javax.swing.*;
import java.awt.*;

import de.botsnscouts.widgets.OptionPane;

public class PaintPanel extends JPanel {
    Paint paint;
    boolean shade;

    static Color color;
    static {
        Color c = ColoredComponent.defaultColor;
        color = new Color(c.getRed(), c.getGreen(), c.getBlue(), ColoredComponent.alpha);
    }



    public PaintPanel( Paint paint ) {
        this( paint, false );
    }

    public PaintPanel( Paint paint, boolean shade ) {
        this.paint = paint;
        this.shade = shade;
    }

    public void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( OptionPane.getBackgroundPaint(this) );
	g2d.fillRect(0,0, d.width, d.height);
        if( shade ) {
            g2d.setPaint( color );
            g2d.fillRect(0,0, d.width, d.height);
        }
    }


}
