package de.botsnscouts.util;

import javax.swing.*;
import java.awt.*;

public class TJPanel extends JPanel {
    public TJPanel() {
	setOpaque( false );
    }

    public TJPanel(LayoutManager layout) {
	super(layout);
	setOpaque(false);
    }

}
