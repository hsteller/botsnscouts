package de.botsnscouts.widgets;

import javax.swing.*;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJTextField extends JTextField {
    public TJTextField(String text) {
        super(text);
        setOpaque(false);
        setFont(GreenTheme.getFont());
    }
}
