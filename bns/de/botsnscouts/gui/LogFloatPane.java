package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;
import de.botsnscouts.gui.ColoredComponent;

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
    public void paint(Graphics g) {
        super.paint(g);
        g.setFont( font );
        g.setColor( Color.green );
        synchronized(msgs) {
            Iterator i = msgs.listIterator();
            int y = getHeight() - 2;
            while( i.hasNext() && y > 0) {
                String m = (String)i.next();
                g.drawString( m, 2, y );
                y -= 13;
            }
        }
        g.setColor(Color.black);
        g.fillRect( getWidth()-10, 0, 10, 10 );
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
        CAT.error( p );
        CAT.error( "container null? " + (container == null) );
        SwingUtilities.convertPointFromScreen(p, container);

        r.height = expanded ? expandedSize.height : normalSize.height;
        r.width = expanded ? expandedSize.width : normalSize.width;
        r.x = p.x;
        r.y = p.y + master.getHeight() - r.height - 5;
        return r;
    }

    private void setSize() {
        setBounds( recalcBounds() );
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