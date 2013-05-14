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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 * Our default theme. (Not really "greenTheme" anymore...)
 */
public class GreenTheme extends DefaultMetalTheme {
    private static final ColorUIResource black = new ColorUIResource(Color.lightGray);

    private static final ColorUIResource white = new ColorUIResource(Color.black);

    private static final ColorUIResource prim1 = new ColorUIResource(new Color(64, 255, 64));

    private static final ColorUIResource prim2 = new ColorUIResource(new Color(64, 192, 64));

    private static final ColorUIResource prim3 = new ColorUIResource(new Color(64, 128, 64));

    private static final ColorUIResource sec1 = new ColorUIResource(new Color(0, 128, 0));

    private static final ColorUIResource sec2 = new ColorUIResource(new Color(0, 96, 0));

    private static final ColorUIResource sec3 = new ColorUIResource(new Color(0, 64, 0));

    private final static Font font = new Font("Sans", Font.BOLD, 12);

    private final static Font bigFont = new Font("Sans", Font.BOLD, 24);

    public String getName() {
        return "botsnscouts";
    }

    static public Font getFont() {
        return font;
    }

    public static Font getBigFont() {
        return bigFont;
    }

    protected ColorUIResource getPrimary1() {
        return prim1;
    }

    protected ColorUIResource getPrimary2() {
        return prim2;
    }

    protected ColorUIResource getPrimary3() {
        return prim3;
    }

    protected ColorUIResource getSecondary1() {
        return sec1;
    }

    protected ColorUIResource getSecondary2() {
        return sec2;
    }

    protected ColorUIResource getSecondary3() {
        return sec3;
    }

    protected ColorUIResource getBlack() {
        return black;
    }

    protected ColorUIResource getWhite() {
        return white;

    }

    public static Color getBnsCaretColor() {
        return prim1;
    }

    public static Color getBnsBackgroundColor() {
        return sec2;
    }

    public static Color getBnsForegroundColor() {
        return prim2;
    }

    public static Color getBnsDisabledTextColor() {
        return white;
    }

    public static Color getTextColor() {
        return black;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("color test");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        TJPanel content = new TJPanel();
        GridLayout lay = new GridLayout(2, 3);
        lay.setHgap(10);
        lay.setVgap(10);
        content.setLayout(lay);
        Color[] colors = new Color[] { prim1, prim2, prim3, sec1, sec2, sec3 };

        for (int i = 0; i < colors.length; i++) {
            TJLabel l = new TJLabel();
            l.setBackground(colors[i]);
            l.setOpaque(true);
            l.setText(colors[i].toString());
            content.add(l);
        }
        frame.getContentPane().add(content);
        frame.pack();
        frame.setVisible(true);

    }
}