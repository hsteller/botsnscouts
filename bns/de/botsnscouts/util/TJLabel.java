package de.spline.rr;

import javax.swing.*;
import java.awt.*;

public class TJLabel extends JLabel {
    public TJLabel() {
	setOpaque( false );
    }

    public TJLabel(String text) {
	super(text);
	setOpaque(false);
    }

    public TJLabel(String text, int align) {
	super(text, align);
	setOpaque(false);
    }

    public TJLabel(Icon icon) {
	super(icon);
	setOpaque(false);
    }

    public TJLabel(Icon icon, int align) {
	super(icon, align);
	setOpaque(false);
    }
}
