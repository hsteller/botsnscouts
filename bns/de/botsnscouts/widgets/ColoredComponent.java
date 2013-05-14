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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class ColoredComponent extends JComponent {

    public static final Color defaultColor = ColoredPanel.defaultColor;

    public static final int alpha = ColoredPanel.alpha;

    Color color;

    public ColoredComponent() {
        this(defaultColor);
    }

    public ColoredComponent(Color c) {
        color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public void paint(Graphics g) {
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

}
