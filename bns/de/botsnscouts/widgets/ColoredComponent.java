package de.botsnscouts.widgets;

import javax.swing.*;
import java.awt.*;

public class ColoredComponent extends JComponent {

    public static final Color defaultColor = ColoredPanel.defaultColor;
    public static final int alpha = ColoredPanel.alpha;

    Color color;

    public ColoredComponent() {
        this(defaultColor);
    }

    public ColoredComponent(Color c) {
        color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public void paint(Graphics g) {
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

}
