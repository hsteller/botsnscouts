/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/

/*
 * Created on 18.05.2005
 *
 */
package de.botsnscouts.widgets;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.botsnscouts.util.Global;
import de.botsnscouts.util.RangeFilter;

/**
 * @author hendrik
 * @version $Id$
 * 
 *          A JTextField that only accepts integers as input;
 *          The accepted integer input can be restricted to an integer interval.
 * 
 */
@SuppressWarnings("serial")
public class TJNumberField extends TJTextField implements DocumentListener {

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
    public TJNumberField(int minValue, int maxValue, int columns) {
        this(new NumberFieldDocument(new RangeFilter(minValue, maxValue)), columns);
    }

    /**
     * Creates an integer-only textfield without a restriction for input values
     * 
     * @param cols the size of the field in columns (like in JTextField)
     * 
     * @see JTextField
     */
    public TJNumberField(int cols) {
        this(new NumberFieldDocument(null), cols);

    }

    private TJNumberField(NumberFieldDocument doc, int cols) {
        super(doc, null, cols);
        doku = doc;
        translateUpdateEvents();
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
        if (str != null && str.trim().length() > 0) {
            try {
                Integer.parseInt(str);
            }
            catch (NumberFormatException nfe) {
                Global.beeep();
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
        }
        catch (NumberFormatException nfe) {
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

}

@SuppressWarnings("serial")
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

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

        if (str == null) {
            return;
        }

        char[] upper = str.toCharArray();

        if (filter == null || filter.isTypInfinite()) {
            for (int i = 0; i < upper.length; i++) {
                if (!Character.isDigit(upper[i])) {
                    Global.beeep();
                    return;
                }
            }
            super.insertString(offs, new String(upper), a);
        }
        else {
            // FIXME (by now) the whole single digit seems more complicated/buggy than necessary
            // (or had I a reason for doing it that way in the program I originally made this class for?
            // hmm..)

            // The first half of the current content
            StringBuffer tmp1 = new StringBuffer(super.getText(0, offs));
            // The second half
            StringBuffer s2 = new StringBuffer(super.getText(offs, getLength() - offs));

            int maxValue = Integer.MIN_VALUE;
            try {
                maxValue = Integer.parseInt(tmp1.toString() + str + s2.toString());
            }
            catch (NumberFormatException ne) {
                // in this case: there was a non digit in "str"
                Global.beeep();
                return;
            }
            boolean getsBigEnough = maxValue >= filter.getMinimum();

            int l2 = s2.length();
            for (int i = 0; i < upper.length; i++) {
                int ende = tmp1.length();
                if (!Character.isDigit(upper[i])) {
                    // not a number -> stop inserting
                    Global.beeep();
                    return;
                }
                else {

                    // appending the next digit
                    tmp1.append(upper[i]);
                    ende = tmp1.length() - 1; // position of the new char
                    tmp1.append(s2);
                    try {
                        // the new value of the content:
                        int foo = Integer.parseInt(tmp1.toString());

                        // needed for initializing fields with a minimum value:
                        // in this case filter.isInRange() will return false as soon a one (single!) digit of "upper"
                        // is smaller as the whole value of minimum
                        // (for example: => false for sure for every non-single-digit minimum..)
                        if (getsBigEnough && foo < filter.getMaximum()) {

                        }
                        else
                            if (!filter.isInRange(foo)) {
                                Global.beeep();
                                return;
                            }

                    }
                    catch (NumberFormatException nfe) {
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
