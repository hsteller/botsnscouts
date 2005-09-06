/*
 * Created on 03.09.2005
 *
 */
package de.botsnscouts.widgets;

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class TJComboBox extends JComboBox {
    
    public TJComboBox(String [] items) {
        super(items);
        initTheme();
    }
    
    public TJComboBox() {
	    super();
	    initTheme();
    }
    
    
    
    private void initTheme() {
        setOpaque(false);                        
        setForeground(GreenTheme.getTextColor());
	    setFont(GreenTheme.getFont());
    }
}
