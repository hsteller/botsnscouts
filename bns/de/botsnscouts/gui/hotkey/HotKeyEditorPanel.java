package de.botsnscouts.gui.hotkey;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;


public class HotKeyEditorPanel extends JPanel {
  static Category CAT = Category.getInstance(HotKeyEditorPanel.class);

  // Hendrik is lazy right now..:
  private static final String SEC = HotKeyConf.MESSAGE_BUNDLE_SECTION;

  private HotKeyMan keyman;

  // <descriptions:>
  public final JLabel colKeyLblHeader1  = new JLabel(Message.say(SEC, "colKeys"));
  public final JLabel colFuncLblHeader1 = new JLabel(Message.say(SEC, "colDesc"));
  public final JLabel colKeyLblHeader2  = new JLabel(Message.say(SEC, "colKeys"));
  public final JLabel colFuncLblHeader2 = new JLabel(Message.say(SEC, "colDesc"));

  public final JLabel colChatMesgLbl   = new JLabel(Message.say(SEC,"editMessageLabel"));
  public final JLabel colChatCommitLbl = new JLabel(Message.say(SEC, "autoCommitBoxLabel"));

  private final static int KEY_FIELD_SIZE = 10;
  private final static Font headlineFont = new Font(null, Font.BOLD, 14);
  private final static String NOT_EDITABLE_TOOLTIP = Message.say(SEC, "notEditableTooltip");


  public HotKeyEditorPanel(HotKeyMan keyman) {
    this.keyman = keyman;
    layoutStuff();

  }

  private void layoutStuff() {
   //     Key        Function (<-Header1):
   // NOTEDITABLE_KEY_GROUP:
   //   name      abcabcabcabc
   // ...
   // NORMAL_KEY_GROUP:
   //
   //   [__]      abcabcabcabc
   //   [__]      abcabcabcabc
   //   [__]      abcabcabcabc
   //   [__]      abcabcabcabc
   // ...
   // CHAT_KEY_GROUP:
   //    Key        Function        msgText     autoCommit  (<-Header2)
   //   [__]      abcabcabcabc   [___________]     [x]
   //   [__]      abcabcabcabc   [___________]     [x]
   // ...

    this.setLayout(new GridBagLayout());

    int startRow = 0;
    int startCol = 0;
    int lastLine = addHeader1(startRow, startCol);
    lastLine = addNotEditableKeys(lastLine+1, startCol);
    lastLine = addNormalKeys(lastLine+1, startCol);
    lastLine = addHeader2(lastLine+1, startCol);
    lastLine = addChatMessageKeys(lastLine+1, startCol);
  }



  private int addNormalKeys(int startRow, int startCol){
    return addKeys(HotKeyConf.GROUP_NORMAL, startRow, startCol);
  }

  private int addChatMessageKeys(int startRow, int startCol){
    return addKeys(HotKeyConf.GROUP_MESSAGES, startRow, startCol);
  }

  private int addKeys(String [] keys, int startRow, int startCol) {

    int size = keys.length;
    int sizeMinusOne = size-1;

    for (int i=0;i<size;i++) {
      HotKey k = keyman.getHotKeyByName(keys[i]);
      if (k == null) {
          // this happens if there are less than six flags=>debug instead of warn like before
        CAT.debug("no hotkey found for "+keys[i]);
        continue;
      }
      JLabel desc = new JLabel(k.getAction().getDescription());
      JTextField edit = createHotKeyEditField(k);
      edit.setText(k.getKeyText());
      JComponent additionals[] = k.getAction().getOptionalComponents();
      JComponent additional1 = null;
      JComponent additional2 = null;
      if (additionals != null) {
          int num = additionals.length;
          if (num>0)
            additional1 = additionals [0];
          if (num>1)
            additional2 = additionals [1];
      }
      if (i == sizeMinusOne)
        addLine(startRow++, startCol, edit, desc, additional1, additional2, 10);
      else
        addLine(startRow++, startCol, edit, desc, additional1, additional2, 0);
    }

    return --startRow;
  }



  private int addNotEditableKeys(int startRow, int startColumn) {
    int size = HotKeyConf.GROUP_NOT_EDITABLE.length;
    String [] keys = HotKeyConf.GROUP_NOT_EDITABLE;
    for (int i=0;i<size;i++) {
      HotKey k = keyman.getHotKeyByName(keys[i]);
      if (k==null){
        CAT.warn("NO HOTKEY FOUND FOR: "+keys[i]);
        continue;
      }
      JTextField key = new JTextField(k.getKeyText());
      key.setEditable(false); // using Textfield instead of label to get a continuous layout
      key.setBackground(Color.black);
      key.setToolTipText(NOT_EDITABLE_TOOLTIP);
      JLabel lblFunc = new JLabel(k.getAction().getDescription());
      this.add(key, new GridBagConstraints(startColumn++, startRow ,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                               new Insets(0,10,0,0), 0,0));
                                               //top,left,bottom,right
      this.add(lblFunc, new GridBagConstraints(startColumn, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,10,0,10),0,0));

    }
    return startRow;
  }


  private void addLine(int startRow, int startColumn, JComponent x1, JComponent x2, JComponent x3, JComponent x4) {
    addLine (startRow,startColumn, x1,  x2,  x3,  x4, 0);
  }


  private void addLine(int startRow, int startColumn, JComponent x1, JComponent x2, JComponent x3, JComponent x4,
                      int bottomInset) {
     if (x1 == null)
      startColumn++;
     else
       this.add(x1, new GridBagConstraints(startColumn++, startRow ,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                               new Insets(0,10,bottomInset,0),0,0));
                                               //top,left,bottom,right
     if (x2 == null)
        startColumn++;
     else
      this.add(x2, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,10,bottomInset,10),0,0));
     if (x3 == null)
        startColumn++;
     else
      this.add(x3, new GridBagConstraints(startColumn++, startRow,1,1,1.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                               new Insets(0,0,bottomInset,5), 0,0));
     if (x4 != null)
       this.add(x4, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,bottomInset,10),0,0));

  }

  /**
   * @param startLine the first line of the grid thats free == this method may add
   *      its components to the grid starting with <code>startLine</code>
   * @param startColumn like <code>startLine</code>, but describes the first free column
   * @return the last line of the grid used by this method..
   *
   */
  private int addHeader1 (int startLine, int startCol) {
     colKeyLblHeader1.setFont(headlineFont);
     colFuncLblHeader1.setFont(headlineFont);
   //                                                    x          y
    this.add(colKeyLblHeader1, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(10,10,10,0),
                                               0,0));
    this.add(colFuncLblHeader1, new GridBagConstraints(startCol, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(10,10,10,0),
                                               0,0));
    return startLine;
  }

  /** @see addHeader1(int, int) */
  private int addHeader2 (int startLine, int startCol) {
     colKeyLblHeader2.setFont(headlineFont);
     colFuncLblHeader2.setFont(headlineFont);
     colChatMesgLbl.setFont(headlineFont);
     colChatCommitLbl.setFont(headlineFont);

    //                                             x          y
    this.add(colKeyLblHeader2, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(5,10,5,0),  //top,left,bottom,right
                                               0,0));
    this.add(colFuncLblHeader2, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(5,0,5,0),
                                               0,0));
    this.add(colChatMesgLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(5,0,5,0),
                                               0,0));
    this.add(colChatCommitLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(5,0,5,10),
                                               0,0));

    return startLine;
  }

  private JTextField createHotKeyEditField(HotKey k) {
      return new HotKeyInputField(k, KEY_FIELD_SIZE, keyman);
  }

}