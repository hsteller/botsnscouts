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


package de.botsnscouts.start;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.botsnscouts.util.ImageMan;
import de.botsnscouts.widgets.ColoredComponent;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJLabel;

/**
 * Shown when something is happening, yet we don't know
 * how long it'll take to complete
 *
 * @author Dirk
 */

public class DoingStuffPanel extends JPanel {

    TJLabel txt;
    FourRobs progressIndicator;
    double angle;
    Paint paint;
    Font font = new Font("Sans", Font.PLAIN, 35);
    Image twonky;
    
    /* we should use a lazy instanciation here, because we
     * just dont want to waste time at startup. Maybe we 
     * can initialize all the components in the show method 
     * if needed? But i think this is future.
     */ 
    
    public DoingStuffPanel(Paint p) {
        paint = p;

        setLayout(new BorderLayout());

        JComponent comp = new ColoredComponent();
        comp.setLayout(new BorderLayout());
        JPanel pl = new JPanel();
        pl.setOpaque(false);
        pl.add(comp);
        txt = new TJLabel("Doing Stuff");
        txt.setFont(font);
        txt.setBorder(OptionPane.niceBorder);

        comp.add(txt, BorderLayout.CENTER);
        Box inner = Box.createHorizontalBox();
        inner.add(Box.createHorizontalGlue());
        inner.add(pl);
        inner.add(Box.createHorizontalGlue());

        progressIndicator = new FourRobs();

        Box outer = Box.createVerticalBox();
        outer.add(Box.createVerticalGlue());
        outer.add(inner);
        outer.add(Box.createVerticalGlue());
        outer.add(progressIndicator);
        outer.add(Box.createVerticalGlue());

        add(outer, BorderLayout.CENTER);
        setDoubleBuffered(true);
    }

    public void setText(String t) {
        txt.setText(t);
    }

    public void inc() {
        angle += 0.05;
        if (angle > 6.28)
            angle -= 6.28;
        progressIndicator.repaint();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, d.width, d.height);
    }

    private class FourRobs extends de.botsnscouts.widgets.ColoredComponent {
        private Rob[] robs;

        public FourRobs() {
            robs = new Rob[4];
            this.setOpaque(false);
            robs[0] = new Rob(ImageMan.getImage(3, 25), 0, 0);
            robs[1] = new Rob(ImageMan.getImage(3, 30), 50, 0);
            robs[2] = new Rob(ImageMan.getImage(3, 12), 0, 50);
            robs[3] = new Rob(ImageMan.getImage(3, 7), 50, 50);
            Dimension inner = new Dimension(64, 64);
            for (int i = 0; i < robs.length; i++)
                robs[i].setDimension(inner);
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            java.awt.geom.AffineTransform before = g2.getTransform();
            g2.translate(-60, -60);
            g2.rotate(angle, 60, 60);
            for (int i = 0; i < robs.length; i++)
                robs[i].paint(g2);
            g2.setTransform(before);
            g2.setColor(frameColor);
            g2.drawRect(0, 0, d.width - 1, d.height - 1);
        }

        private Dimension d = new Dimension(60, 60);

        public Dimension getPreferredSize() {
            return d;
        }

        public Dimension getMinimumSize() {
            return d;
        }

        public Dimension getMaximumSize() {
            return d;
        }
    }

    static Color frameColor = Color.green.darker();

    private class Rob {
        private Image im;
        private int x, y;
        private Dimension d;

        public Rob(Image i, int xx, int yy) {
            im = i;
            x = xx;
            y = yy;
        }

        public void setDimension(Dimension dd) {
            d = dd;
        }

        public void paint(Graphics2D g2) {
            g2.drawImage(im, x, y, d.width, d.height, DoingStuffPanel.this);
        }
        //d=new Dimension(im.getHeight(this)*2,im.getWidth(this)*2);
    }

    public static void main(String[] argv) {
        System.out.println("Class test code");
        ImageMan.finishLoading();
        JFrame f = new JFrame("DoingStuffPanel");
        final DoingStuffPanel p = new DoingStuffPanel(Color.gray);
        new Thread(new Runnable() {
            public void run() {
                for (; ;) {
                    p.inc();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
        f.setContentPane(p);
        f.setSize(500, 500);
        f.setVisible(true);
    }

}

