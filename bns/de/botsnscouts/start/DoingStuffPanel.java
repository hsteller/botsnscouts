package de.botsnscouts.start;

import de.botsnscouts.util.ImageMan;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.ColoredComponent;

import javax.swing.*;
import java.awt.*;

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
