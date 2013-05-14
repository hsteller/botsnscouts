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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
class ThumbsCellRenderer extends JPanel implements ListCellRenderer<TileInfo> {

    JLabel image = new JLabel();

    JLabel text = new JLabel();

    Border selectedBorder = new MatteBorder(3, 3, 3, 3, Color.green.darker());

    Border normalBorder = new EmptyBorder(3, 3, 3, 3);

    public ThumbsCellRenderer() {
        setOpaque(false);
        image.setOpaque(false);
        text.setOpaque(false);
        text.setBackground(Color.black);
        text.setForeground(Color.green.darker());
        setLayout(new BorderLayout());
        add(image, BorderLayout.CENTER);
        add(text, BorderLayout.SOUTH);
        // setBackground( Color.black );
        // setForeground( Color.green );
    }

    /*
     * public Component getListCellRendererComponent(JList list, Object value, // value to display int index, // cell index boolean isSelected, // is
     * the cell selected boolean cellHasFocus) // the list and the cell have the focus { String name =
     * value.toString().substring(0, value.toString().indexOf(".rra")); setOpaque(false); text.setText(name); image.setIcon(new ImageIcon(((TileInfo)
     * value).getImage())); if (isSelected) { text.setText(name.toUpperCase());
     * image.setBorder(selectedBorder); } else { // setBackground(list.getBackground()); // setForeground(list.getForeground()); text.setText(name);
     * image.setBorder(normalBorder); } setEnabled(list.isEnabled()); setFont(list.getFont()); return this;
     * }
     */

    @Override
    public Component getListCellRendererComponent(JList<? extends TileInfo> list, TileInfo value, int index,
                    boolean isSelected, boolean cellHasFocus) {
        String name = value.toString().substring(0, value.toString().indexOf(".rra"));
        setOpaque(false);
        text.setText(name);
        image.setIcon(new ImageIcon(value.getImage()));
        if (isSelected) {
            text.setText(name.toUpperCase());
            image.setBorder(selectedBorder);
        }
        else {
            // setBackground(list.getBackground());
            // setForeground(list.getForeground());
            text.setText(name);
            image.setBorder(normalBorder);
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        return this;

    }

}
