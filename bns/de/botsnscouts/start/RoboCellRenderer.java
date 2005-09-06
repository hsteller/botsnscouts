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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Message;

/** 
 * 
 * @version $Id$
 */
public class RoboCellRenderer extends JLabel implements ListCellRenderer {
   
    
    public static ImageIcon[] robIcons = new ImageIcon[8];

    private String zufall = Message.say("Start", "mFarbeEgal");
    private Dimension size;
    private boolean withEgal = true;

    static {
        Image[] robbis = ImageMan.getImages(ImageMan.ROBOS);
        for (int i = 0; i < 8; i++) {
            robIcons[i] = new ImageIcon(robbis[i * 4]);
        }
    }

    RoboCellRenderer() {
        this(true);
    }

    RoboCellRenderer(boolean wEgal) {       
        withEgal = wEgal;
        size = new Dimension(96, 48);
        setOpaque(false);
    }

    /**
     * @param list
     * @param value        value to display
     * @param index        cell index
     * @param isSelected   is the cell selected
     * @param cellHasFocus the list and the cell have the focus
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (index == -1) {
            index = list.getSelectedIndex();
            if (index == -1) {
                index = 0;
            }
        }
        setPreferredSize(size);
        if (index == 0 && withEgal) {
            setText(zufall);
            setIcon(null);
            return this;
        }

        setText("");
        if (withEgal) {
            setIcon(robIcons[index - 1]);
        } else {
            setIcon(robIcons[index]);
        }

       if (isSelected) {       
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
           setBackground(list.getBackground());
           setForeground(list.getForeground());
        }
   
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        return this;
    }

}