package de.botsnscouts.widgets;

import javax.swing.*;
import java.awt.*;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJTextField extends JTextField {
    public TJTextField(String text) {
        super(text);
        setOpaque(false);
        setFont(GreenTheme.getFont());
    }

    public TJTextField(String text, int alignment) {
        super( text, alignment );
        setOpaque(false);
        setFont(GreenTheme.getFont());
    }

    public TJTextField(String text, int alignment, boolean big) {
        super( text, alignment );
        setOpaque(false);
        if (big)
            setFont( GreenTheme.getBigFont());
        else
            setFont( GreenTheme.getFont() );
    }



}
