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

/**
 * Transparent JLabel
 */
public class TJLabel extends JLabel {

    private static Font fontSmall = new Font("Sans", Font.BOLD, 12);
    private static Font fontBig = new Font("Sans", Font.BOLD, 24);
    private static Color textColor = GreenTheme.getTextColor();

    public TJLabel() {
        init();
    }

    private void init() {
        setOpaque(false);
        setFont(fontSmall);
        setForeground(textColor);
    }

    public TJLabel(String text) {
        super(text);
        init();
    }

    public TJLabel(String text, int align) {
        super(text, align);
        init();
    }

    public TJLabel(String text, Color color) {
        super(text);
        init();
        setForeground(color);
    }

    public TJLabel(String text, Color color, boolean big) {
        super(text);
        init();
        setForeground(color);
        if (big) {
            setFont(fontBig);
        } else {
            setFont(fontSmall);
        }
    }

    public TJLabel(Icon icon) {
        super(icon);
        init();
    }

    public TJLabel(Icon icon, int align) {
        super(icon, align);
        init();
    }
}
