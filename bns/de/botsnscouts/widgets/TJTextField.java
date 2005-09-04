package de.botsnscouts.widgets;



import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Document;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class TJTextField extends JTextField {
    public TJTextField(String text) {
        this(text, CENTER, false);
    }

    public TJTextField(String text, int alignment, boolean big) {
        super(text, alignment);
        init(big);
    }
    public TJTextField(int size){
        super(size);
        init(false);                
    }
    public TJTextField(String text,int size){
        super(text, size);
        init(false);                
    }
    public TJTextField(){
        super();
        init(false);                
    }
    
    public TJTextField(Document doc, String text, int columns){
        super(doc, text, columns);
        init(false);
    }
    
    protected void init(boolean useBigFont) {
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());
        setCaretColor(GreenTheme.getBnsCaretColor());
        setDisabledTextColor(MetalLookAndFeel.getControlDisabled());
        
        if (useBigFont) {
            setFont(GreenTheme.getBigFont());
        } else {
            setFont(GreenTheme.getFont());
        }
    }

}
