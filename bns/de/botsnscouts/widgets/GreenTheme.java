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

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import java.awt.*;

/**
 * Our default theme. (Not really "greenTheme" anymore...)
 */
public class GreenTheme extends DefaultMetalTheme {
	public String getName() {
		return "botsnscouts";
	}

	private final ColorUIResource prim1 = new ColorUIResource( new Color(64,255,64) );
	private final ColorUIResource prim2 = new ColorUIResource( new Color(64,192,64) );
	private final ColorUIResource prim3 = new ColorUIResource( new Color(64,128,64) );

	private final ColorUIResource sec1 = new ColorUIResource( new Color(0,128,0) );
	private final ColorUIResource sec2 = new ColorUIResource( new Color(0,96,0) );
	private final ColorUIResource sec3 = new ColorUIResource( new Color(0,64,0) );

    private final static Font font = new Font("Sans", Font.BOLD, 12);
    private final static Font bigFont = new Font("Sans", Font.BOLD, 24);

    static public Font getFont() {
        return font;
    }

    public static Font getBigFont() {
        return bigFont;
    }

	protected ColorUIResource getPrimary1() { return prim1; }
	protected ColorUIResource getPrimary2() { return prim2; }
	protected ColorUIResource getPrimary3() { return prim3; }
	protected ColorUIResource getSecondary1() { return sec1; }
	protected ColorUIResource getSecondary2() { return sec2; }
	protected ColorUIResource getSecondary3() { return sec3; }

    private final ColorUIResource black = new ColorUIResource( Color.lightGray );
	private final ColorUIResource white = new ColorUIResource( Color.black );
	protected ColorUIResource getBlack() { return black; }
	protected ColorUIResource getWhite() { return white; }

}

