package de.botsnscouts.widgets;

import javax.swing.*;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJCheckBox extends JCheckBox {

    public TJCheckBox(String label, boolean def) {
        super(label, def);
        setOpaque(false);
        setFont( GreenTheme.getFont());
    }

}
