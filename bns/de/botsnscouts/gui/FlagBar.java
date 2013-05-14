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

package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class FlagBar extends JButton {

    int maxFlag;

    private int reachedFlag;

    public FlagBar(int max) {
        this.maxFlag = max;
        reachedFlag = 1;
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(false);
        setFocusable(false);
    }

    public FlagBar() {
        this(6);
    }

    static Icon flag, grayFlag;

    Icon getFlag() {
        if (flag == null) {
            flag = new ImageIcon(RobotInfo.getFlag());
        }
        return flag;
    }

    Icon getGrayFlag() {
        if (grayFlag == null) {
            grayFlag = new ImageIcon(RobotInfo.getGrayFlag());
        }
        return grayFlag;
    }

    public void paintComponent(Graphics g) {
        int fw = getFlag().getIconWidth() - 6;
        int x = 0;
        for (int i = 0; i < maxFlag; i++) {
            if (i < reachedFlag)
                getFlag().paintIcon(this, g, x, 0);
            else
                getGrayFlag().paintIcon(this, g, x, 0);
            x += fw;
        }
    }

    public Dimension getMinimumSize() {
        int fw = getFlag().getIconWidth() - 6;
        return new Dimension(fw * maxFlag, getFlag().getIconHeight());
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    // zum Display
    public static void main(String[] args) {
        JFrame frame = new JFrame("damage");
        frame.setSize(400, 400);
        frame.getContentPane().setLayout(new BorderLayout());
        Box b = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 6; i++) {
            final FlagBar db = new FlagBar(10);
            db.setBorder(BorderFactory.createBevelBorder(2));
            db.setSize(db.getPreferredSize());
            int v = (int) (Math.random() * 10.0);
            db.setReachedFlag(v);
            b.add(db);
            db.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    db.setReachedFlag((db.getReachedFlag() + 1) % 11);
                    db.repaint();
                }
            });
        }
        frame.getContentPane().add(b, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public int getMaxFlag() {
        return maxFlag;
    }

    public void setReachedFlag(int newReachedFlag) {
        reachedFlag = newReachedFlag;
    }

    public int getReachedFlag() {
        return reachedFlag;
    }
}
