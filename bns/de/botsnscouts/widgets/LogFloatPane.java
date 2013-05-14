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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class LogFloatPane extends ColoredComponent implements ComponentListener {

    private Component master;

    private Container container;

    private Font font = new Font("Sans", Font.BOLD, 12);

    private LinkedList<String> msgs = new LinkedList<String>();

    private boolean expanded = false;

    private java.awt.Dimension expandedSize;

    private java.awt.Dimension normalSize;

    public LogFloatPane(Component master, Container container) {
        this.master = master;
        this.container = container;
        master.addComponentListener(this);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setBorder(BorderFactory.createEtchedBorder(new Color(0, 255, 0, 128), new Color(0, 128, 0, 128)));
        setOpaque(false);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    public void paintComponent(Graphics g) {
        Insets in = getInsets();
        g.setFont(font);
        g.setColor(Color.green);
        synchronized (msgs) {
            Iterator<String> i = msgs.listIterator();
            int y = getHeight() - in.top - in.bottom;
            while (i.hasNext() && y > 0) {
                String m = i.next();
                g.drawString(m, in.left + 2, y);
                y -= 13;
            }
        }
        g.setColor(Color.black);
        g.fillRect(getWidth() - in.right - 10, in.top, 10, 10);
        g.setColor(Color.green);
        if (!expanded) {
            g.drawLine(getWidth() - in.right - 10, in.top + 9, getWidth() - in.right - 6, in.top);
            g.drawLine(getWidth() - in.right - 2, in.top + 9, getWidth() - in.right - 6, in.top);
        }
        else {
            g.drawLine(getWidth() - in.right - 10, in.top, getWidth() - in.right - 6, in.top + 9);
            g.drawLine(getWidth() - in.right - 2, in.top, getWidth() - in.right - 6, in.top + 9);
        }
        super.paintBorder(g);

    }

    public void setExpanded(boolean newExp) {
        expanded = newExp;
        setSize();
    }

    private Rectangle recalcBounds() {
        Rectangle r = new Rectangle();
        Point p = master.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, container);

        r.height = expanded ? expandedSize.height : normalSize.height + 2;
        // r.width = expanded ? expandedSize.width : normalSize.width;
        r.width = (int) (master.getWidth() * 0.8);
        r.x = p.x;
        r.y = p.y + master.getHeight() - r.height - 5;
        return r;
    }

    private void setSize() {
        Rectangle r = recalcBounds();
        setBounds(r);
    }

    void switchSize() {
        expanded = !expanded;
        setSize();
    }

    public void processMouseEvent(MouseEvent me) {
        if (me.getID() == MouseEvent.MOUSE_CLICKED) {
            int x = me.getPoint().x;
            int y = me.getPoint().y;
            if (x > getWidth() - 10 && y < 10)
                switchSize();
        }
        else
            super.processMouseEvent(me);
    }

    public void setExpandedSize(java.awt.Dimension newExpandedSize) {
        expandedSize = newExpandedSize;
    }

    public java.awt.Dimension getExpandedSize() {
        return expandedSize;
    }

    public void setNormalSize(java.awt.Dimension newNormalSize) {
        normalSize = newNormalSize;
    }

    public java.awt.Dimension getNormalSize() {
        return normalSize;
    }

    public void addMessage(String msg) {
        synchronized (msgs) {
            msgs.addFirst(msg);
        }
        repaint();
    }

    public void clear() {
        synchronized (msgs) {
            msgs.clear();
        }
        repaint();
    }

    public void componentResized(ComponentEvent parm1) {
        setSize();
    }

    public void componentMoved(ComponentEvent parm1) {
        setSize();
    }

    public void componentShown(ComponentEvent parm1) {
        setSize();
    }

    public void componentHidden(ComponentEvent parm1) {
    }
}
