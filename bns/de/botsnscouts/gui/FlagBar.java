package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class FlagBar extends JButton {
    int maxFlag;
    private int reachedFlag;

    public FlagBar(int max) {
        this.maxFlag = max;
        reachedFlag = 1;
        setBorderPainted( false );
        setOpaque( false );
        setRolloverEnabled( false );
    }

    public FlagBar() {
        this(6);
    }

    static Icon flag, grayFlag;
    Icon getFlag() {
        if( flag == null ) {
            flag = new ImageIcon( RobotInfo.getFlag() );
        }
        return flag;
    }
    Icon getGrayFlag() {
        if( grayFlag == null ) {
            grayFlag = new ImageIcon( RobotInfo.getGrayFlag() );
        }
        return grayFlag;
    }

    public void paintComponent(Graphics g) {
        int fw = getFlag().getIconWidth() - 6;
        int x = 0;
        for(int i = 0; i < maxFlag; i++ ) {
            if( i < reachedFlag )
                getFlag().paintIcon( this, g, x, 0 );
            else
                getGrayFlag().paintIcon( this, g, x, 0 );
            x += fw;
        }
    }


    public Dimension getMinimumSize() {
        int fw = getFlag().getIconWidth() - 6;
        return new Dimension( fw * maxFlag, getFlag().getIconHeight() );
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    // zum Display
    public static void main(String[] args) {
        JFrame frame = new JFrame("damage");
        frame.setSize(400, 400);
        frame.getContentPane().setLayout( new BorderLayout() );
        Box b = new Box(BoxLayout.Y_AXIS);
        for(int i=0; i<6; i++) {
            final FlagBar db = new FlagBar(10);
            db.setBorder( BorderFactory.createBevelBorder(2) );
            db.setSize( db.getPreferredSize() );
            int v = (int)(Math.random() *10.0);
            db.setReachedFlag( v );
            b.add( db );
            db.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent me ) {
                    db.setReachedFlag( (db.getReachedFlag() + 1) % 11 );
                    db.repaint();
                }
            });
        }
        frame.getContentPane().add( b, BorderLayout.CENTER );
        frame.setVisible( true );
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