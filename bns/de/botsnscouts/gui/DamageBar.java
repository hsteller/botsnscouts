package de.botsnscouts.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class DamageBar extends JComponent {
    int damage;

    public DamageBar() {
        setFocusable(false);
    }

    public void paintComponent(Graphics g_) {
        Graphics2D g = (Graphics2D) g_;
        int h_ = getHeight();
        int h = (h_ / 9) * 9; // usable height should be dividable by 9
        int d = h / 9;
        int h2 = h / 2;
        int damageHeight = (9-damage) * d;

        //g.setPaint( new GradientPaint( 0, h2, Color.yellow, 0, h-2*h2, Color.red ) );
        g.setPaint( new GradientPaint( 0, h_, Color.red, 0, h_ - h2, Color.yellow ) );
        int temp = Math.min(damageHeight, h2);
        g.fillRect( 0, h_ - temp, getWidth(), temp);
        if( damageHeight > h2 ) {
            //g.setPaint( new GradientPaint( 0, h , Color.green, 0, h2, Color.yellow ) );
            g.setPaint( new GradientPaint( 0, h_ - h2 , Color.yellow, 0, h_ - h, Color.green ) );
            g.fillRect( 0, h_ - damageHeight, getWidth(), damageHeight-h2 );
        }

        for(int i = 0; i < (9-damage); i++ ) {
            g.setColor( Color.black );
            g.drawLine( 0,  h_ - (i+1)*d, getWidth(), h_ - (i+1)*d );
        }
    }

    public void setDamageValue(int damage) {
        this.damage = damage;
    }
    public int getDamageValue() {
        return damage;
    }

    public Dimension getMinimumSize() {
        return new Dimension( 5, 45 );
    }

//    public Dimension getPreferredSize() {
//        return new Dimension( 5, 45 );
//    }

    // zum Display
    public static void main(String[] args) {
        JFrame frame = new JFrame("damage");
        frame.setSize(300, 300);
        frame.getContentPane().setLayout( new BorderLayout() );
        Box b = new Box(BoxLayout.X_AXIS);
        for(int i=0; i<6; i++) {
            final DamageBar db = new DamageBar();
            db.setSize(10, 90);
            db.setBorder( BorderFactory.createBevelBorder(2) );
            int v = (int)(Math.random() *9.0);
            db.setDamageValue( v );
            b.add( db );
            db.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent me ) {
                    db.setDamageValue( (db.getDamageValue() + 1) % 10 );
                    db.repaint();
                }
            });
        }
        frame.getContentPane().add( b, BorderLayout.CENTER );
        frame.pack();
        frame.setVisible( true );
    }
}