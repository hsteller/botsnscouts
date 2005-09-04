package de.botsnscouts.widgets;

import javax.swing.JCheckBox;


/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJCheckBox extends JCheckBox {

    public TJCheckBox() {
        super();
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());              
        setFont(GreenTheme.getFont());
    }
    
    public TJCheckBox(String label, boolean selected) {
        super(label, selected);
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());              
        setFont(GreenTheme.getFont());
    }

}
