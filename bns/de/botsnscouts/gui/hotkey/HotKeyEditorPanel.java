package de.botsnscouts.gui.hotkey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Conf;

import org.apache.log4j.Category;


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

  private final static int KEY_FIELD_SIZE = 5;
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
        CAT.warn("no hotkey found for "+keys[i]);
        continue;
      }
      JLabel desc = new JLabel(k.getAction().getDescription());
      JTextField edit = createHotKeyEditField(k);

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
                                               GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                               new Insets(0,10,0,0), 0,0));
                                               //top,left,bottom,right
      this.add(lblFunc, new GridBagConstraints(startColumn, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.WEST, GridBagConstraints.NONE,
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
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,10,bottomInset,0), 0,0));
                                               //top,left,bottom,right
     if (x2 == null)
        startColumn++;
     else
      this.add(x2, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.WEST, GridBagConstraints.NONE,
                                               new Insets(0,10,bottomInset,10),0,0));
     if (x3 == null)
        startColumn++;
     else
      this.add(x3, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                               new Insets(0,0,bottomInset,5), 100,0));
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
    final JTextField keyedit = new JTextField(KEY_FIELD_SIZE);
      keyedit.addKeyListener (new AbstractHotKeyListener(k.getName()){
        public void doStuff (KeyEvent e, int hotKeyCode) {
          CAT.debug(keyman.dump());
          String oldText = keyedit.getText();
          keyedit.setText("");
          CAT.debug("oldText="+oldText);
          String id = getKeyName();
          try {
            Integer code = new Integer(hotKeyCode);
            HotKey old = keyman.getHotKey(code);
            if (old == null || old.getName().equals(id)){// code is unused or the old code of id
              keyman.updateHotkeyCode(id, code); // update keycode->hotkey  and keyname->hotkey mapping in keyman
              HotKey hk = keyman.getHotKey(code);
              if (e.isActionKey())
                keyedit.setText(hk.getKeyText());

            }
            else {
              keyedit.setText(oldText);
            }
            CAT.debug(keyman.dump());
          }
          catch (KeyReserved kr) {
             CAT.debug("reserved key!");
             keyedit.setText(oldText);
          }
        }
      });

      // <hack alert>
      int i = k.getKeyCode();
      String text  = KeyEvent.getKeyText(i);
      String s = text.toLowerCase();
      if (s.startsWith("unknown"))  // inefficient & nothing to be proud of..
            text=""+((char)i);
      else if (s.equals("minus"))
        text= "-";
      keyedit.setText(text);
      // </hack alert>
      return keyedit;
  }


}