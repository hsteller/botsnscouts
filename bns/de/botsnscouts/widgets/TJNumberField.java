/*
 * Created on 18.05.2005
 *
 */
package de.botsnscouts.widgets;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.botsnscouts.util.RangeFilter;

/**
 * @author hendrik
*  @version $Id$
* 
*  A JTextField that only accepts integers as input;
*  The accepted integer input can be restricted to an integer interval.  
* 
*/
public class TJNumberField extends JTextField implements DocumentListener {
       
    private int dflt = 0;
    private NumberFieldDocument doku = null;

    protected Document createDefaultModel() {
        if (doku == null)
            return new NumberFieldDocument();
        else
            return doku;
    }

    /**
     * Creates an integer-only textfield with a restriction for input values.
     * 
     * @param minValue the minimum value that this field will accept
     * @param maxValue the maximum value that this field will accept
     * @param columns the size of the field in columns (like in JTextField)
     * 
     * @see JTextField
     */
    public TJNumberField(int minValue, int maxValue, int columns){
        this (new NumberFieldDocument(new RangeFilter(minValue, maxValue)), columns);
    }    
    
    /**
     * Creates an integer-only textfield without a restriction for input values 
     * 
     * @param cols the size of the field in columns (like in JTextField)
     * 
     * @see JTextField
     */
    public TJNumberField(int cols) {
        this (new NumberFieldDocument(null), cols);
        
    }
    
    private TJNumberField() {
        super();
        init();
    }


    private TJNumberField(NumberFieldDocument doc, int cols) {
        super(doc, null, cols);
        doku = doc;
        init();
    }

    private  TJNumberField(NumberFieldDocument doc) {
        this(doc, 0);
        init();
    }

    
    private void init() {
        tjIfy(false);
        translateUpdateEvents();
    }
    
    
    private void tjIfy(boolean useBigFont) {
        setOpaque(false);
        setForeground(GreenTheme.getTextColor());
        if (useBigFont) {
            setFont(GreenTheme.getBigFont());
        } else {
            setFont(GreenTheme.getFont());
        }
    }
    
    public void changedUpdate(DocumentEvent e) {
        fireActionPerformed();
    }

    public void insertUpdate(DocumentEvent e) {
        fireActionPerformed();
    }

    public void removeUpdate(DocumentEvent e) {
        fireActionPerformed();
    }


    private void translateUpdateEvents() {
        getDocument().addDocumentListener(this);
    }


    // Ueberschreiben wohl nicht mehr noetig, dank eigenem Dokument
    public void setText(String str) {
		if (str != null && str.length() > 0) {
			try {
				Integer.parseInt(str);
			} catch (NumberFormatException nfe) {
				beeep();
				return;
			}
		}
        super.setText(str);
    }

    // Ueberschreiben wohl nicht mehr noetig, dank eigenem Dokument
    public String getText() {
        String s = super.getText();
        try {
            int foo = Integer.parseInt(s.trim());
            return "" + foo;
        } catch (NumberFormatException nfe) {
            return "" + dflt;
        }
    }

    public int getValue() {
        try {
            return Integer.parseInt(getText());
        }
                // kann nicht passieren
        catch (NumberFormatException nfe) {
            return dflt;
        }
    }

    public void setValue(int v) {
        setText("" + v);
    }

    public int getDefaultValue() {
        return dflt;
    }

    public void setDefaultValue(int def) {
        dflt = def;
    }

    public boolean isEmpty() {
        String s = super.getText();
        return (s == null || s.trim().equals(""));
    }

    public static void beeep() {
        try {
            java.awt.Toolkit t = java.awt.Toolkit.getDefaultToolkit();
            t.beep();
        } catch (Throwable err) {

        }
    }
   
}


    class NumberFieldDocument extends PlainDocument {

        private RangeFilter filter;

        public NumberFieldDocument() {
            super();
            filter = null;
        }
        
        

        public NumberFieldDocument(RangeFilter ra) {
            super();
            filter = ra;
        }

        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] upper = str.toCharArray();

            if (filter == null || filter.isTypInfinite()) {
                for (int i = 0; i < upper.length; i++) {
                    if (!Character.isDigit(upper[i])) {
                        TJNumberField.beeep();
                        return; 
                    }
                }
                super.insertString(offs, new String(upper), a);
            } else {
                StringBuffer tmp1 = new StringBuffer(super.getText(0, offs));
                StringBuffer s2 = new StringBuffer(super.getText(offs, getLength() - offs));

                int l2 = s2.length();
                for (int i = 0; i < upper.length; i++) {
                    int ende = tmp1.length();
                    if (!Character.isDigit(upper[i])) {
                        TJNumberField.beeep();
                        return;
                    } else {
                        tmp1.append(upper[i]);
                        ende = tmp1.length() - 1;
                        tmp1.append(s2);
                        try {
                            int foo = Integer.parseInt(tmp1.toString());
                            if (!filter.isInRange(foo)) {
                                TJNumberField.beeep();
                                return;
                            }

                        } catch (NumberFormatException nfe) {
                            return;
                        }
                        if (l2 > 0) {
                            int fin = tmp1.length();
                            tmp1.delete(ende + 1, fin);
                        }

                    }
                }
                super.insertString(offs, new String(upper), a);
            }


        }
    }
    
    

