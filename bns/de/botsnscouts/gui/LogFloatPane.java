package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;
import de.botsnscouts.widgets.ColoredComponent;

public class LogFloatPane extends ColoredComponent implements ComponentListener {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( ColoredComponent.class.getName() );

    Component master;
    Container container;

    public LogFloatPane(Component master, Container container) {
        this.master = master;
        this.container = container;
        master.addComponentListener(this);
        enableEvents( AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setBorder( BorderFactory.createEtchedBorder( new Color(0, 255,0, 128), new Color(0,128,0,128) ) );
        setOpaque( false );
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    Font font = new Font("Sans", Font.BOLD, 12 );
    public void paintComponent(Graphics g) {
        Insets in = getInsets();
        g.setFont( font );
        g.setColor( Color.green );
        synchronized(msgs) {
            Iterator i = msgs.listIterator();
            int y = getHeight() - in.top - in.bottom;
            while( i.hasNext() && y > 0) {
                String m = (String)i.next();
                g.drawString( m, in.left + 2, y );
                y -= 13;
            }
        }
        g.setColor(Color.black);
        g.fillRect( getWidth()-in.right - 10, in.top, 10, 10 );
        g.setColor(Color.green);
        if( !expanded ) {
            g.drawLine( getWidth()-in.right - 10, in.top + 9,
                        getWidth()-in.right - 6, in.top );
            g.drawLine( getWidth()-in.right - 2 , in.top + 9,
                        getWidth()-in.right - 6, in.top );
        } else {
            g.drawLine( getWidth()-in.right - 10, in.top ,
                        getWidth()-in.right - 6, in.top + 9);
            g.drawLine( getWidth()-in.right - 2 , in.top,
                        getWidth()-in.right - 6, in.top + 9);
        }
        super.paintBorder(g);

    }
    LinkedList msgs = new LinkedList();
    boolean expanded = false;
    private java.awt.Dimension expandedSize;
    private java.awt.Dimension normalSize;


    void setExpanded( boolean newExp ) {
        expanded = newExp;
        setSize();
    }


    private Rectangle recalcBounds() {
        Rectangle r = new Rectangle();
        Point p = master.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, container);

        r.height = expanded ? expandedSize.height : normalSize.height + 2;
        //r.width = expanded ? expandedSize.width : normalSize.width;
        r.width = (int)(master.getWidth() * 0.8);
        r.x = p.x;
        r.y = p.y + master.getHeight() - r.height - 5;
        return r;
    }

    private void setSize() {
        Rectangle r = recalcBounds();
        setBounds( r );
        Insets in = getInsets();
    }

    void switchSize() {
        expanded = ! expanded;
        setSize();
    }

    public void processMouseEvent(MouseEvent me) {
        if( me.getID() == MouseEvent.MOUSE_CLICKED ) {
            int x = me.getPoint().x;
            int y = me.getPoint().y;
            if( x > getWidth() - 10  && y < 10 )
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
        synchronized(msgs) {
            msgs.addFirst(msg);
        }
        repaint();
    }
    public void clear() {
        synchronized(msgs) {
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