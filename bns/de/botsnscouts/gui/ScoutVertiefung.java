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

package de.botsnscouts.gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Message;

/**
 * Scoutvertiefung, die im oben rechts in der Infoleiste dargestellt ist
 */
@SuppressWarnings("serial")
public class ScoutVertiefung extends JToggleButton {
    int xsize = 60, ysize = 60;

    Image[] imageActive;

    Image[] imageDream;

    public Dimension getMinimumSize() {
        return new Dimension(xsize, ysize);
    }

    public Dimension getPreferredSize() {
        return new Dimension(xsize, ysize);
    }

    public ScoutVertiefung(ActionListener al) {
        imageActive = ImageMan.getImages(ImageMan.SCHLAFPLATZ);
        imageDream = ImageMan.getImages(ImageMan.SCHLAFSCOUT);
        // scoutImages=ImageMan.getImages(ImageMan.SCOUT);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(null);
        setToolTipText(Message.say("ScoutVertiefung", "mtooltip"));
        setIcon(new ImageIcon(imageActive[0]));
        // setPressedIcon(new ImageIcon(scoutImages[2]));
        setSelectedIcon(new ImageIcon(imageDream[0]));
        addActionListener(al);
        setFocusable(false);

    }

    public static void main(String[] args) {
        Message.setLanguage("deutsch");
        JFrame f = new JFrame();
        f.setSize(100, 100);
        f.getContentPane().add(new ScoutVertiefung(null));
        f.setVisible(true);
    }

    protected boolean selected() {
        return isSelected();
    }
}
