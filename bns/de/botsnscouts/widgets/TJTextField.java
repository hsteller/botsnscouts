package de.botsnscouts.widgets;

import javax.swing.*;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJTextField extends JTextField {
    public TJTextField(String text) {
        this(text, CENTER, false);
    }

    public TJTextField(String text, int alignment, boolean big) {
        super(text, alignment);
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());
        if (big) {
            setFont(GreenTheme.getBigFont());
        } else {
            setFont(GreenTheme.getFont());
        }
    }

}
