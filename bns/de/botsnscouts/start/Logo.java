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

package de.botsnscouts.start;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import de.botsnscouts.util.ImageMan;

/**
 * Creates a JLabel instance with image and with an empty string for the title. The label is centered vertically in its display area. The label's
 * contents, once set, will be displayed on the leading edge of the label's display area.
 * 
 * The image used is located under \de\botsnscouts\images\bnslogo.jpg TODO(Make me custom..)
 */
@SuppressWarnings("serial")
public class Logo extends JLabel {

    public Logo() {
        super();
        Icon icon = ImageMan.getIcon("bnslogo.jpg");
        setIcon(icon);
        setBackground(Color.gray);
        setBorder(new EtchedBorder(8));
    }
}
