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
  public final JLabel colKeyLbl  = new JLabel(Message.say(SEC, "colKeys"));
  public final JLabel colFuncLbl = new JLabel(Message.say(SEC, "colDesc"));

  public final JLabel colChatMesgLbl   = new JLabel(Message.say(SEC,"editMessageLabel"));
  public final JLabel colChatCommitLbl = new JLabel(Message.say(SEC, "autoCommitBoxLabel"));

  private final static int KEY_FIELD_SIZE = 2;
  private final static Color labelColor = Color.black;


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

    for (int i=0;i<size;i++) {
      HotKey k = keyman.getHotKeyByName(keys[i]);
      //HotKey k = keyman.getHotKey(HotKeyConf.getKeyCode(keys[i])); //KRANK! das gehoert in KeyMan
      JLabel desc = new JLabel(k.getAction().getDescription());
      JComponent additional = k.getAction().getOptionalComponent();
      JTextField edit = createHotKeyEditField(k);

      addLine(startRow++, startCol, edit, desc, additional, null);
    }

    return --startRow;
  }



  private int addNotEditableKeys(int startRow, int startCol) {
    int size = HotKeyConf.GROUP_NOT_EDITABLE.length;
    String [] keys = HotKeyConf.GROUP_NOT_EDITABLE;
    for (int i=0;i<size;i++) {
      HotKey k = keyman.getHotKeyByName(keys[i]);
      if (k==null){
        CAT.warn("NO HOTKEY FOUND FOR: "+keys[i]);
        continue;
      }
      JLabel lblKey = new JLabel(k.getKeyText());
      lblKey.setForeground(labelColor);
      JLabel lblFunc = new JLabel(k.getAction().getDescription());
      lblFunc.setForeground(labelColor);
      addLine(startRow++, startCol, lblKey, lblFunc, null, null);
    }
    return --startRow;
  }


  private void addLine(int startRow, int startColumn, JComponent x1, JComponent x2, JComponent x3, JComponent x4) {
     if (x1 == null)
      startColumn++;
     else
       this.add(x1, new GridBagConstraints(startColumn++, startRow ,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0), 0,0));
     if (x2 == null)
        startColumn++;
     else
      this.add(x2, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),0,0));
     if (x3 == null)
        startColumn++;
     else
      this.add(x3, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0), 0,0));
     if (x4 != null)
       this.add(x4, new GridBagConstraints(startColumn++, startRow,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),0,0));

  }

  /**
   * @param startLine the first line of the grid thats free == this method may add
   *      its components to the grid starting with <code>startLine</code>
   * @param startColumn like <code>startLine</code>, but describes the first free column
   * @return the last line of the grid used by this method..
   *
   */
  private int addHeader1 (int startLine, int startCol) {
    //                                             x          y
    this.add(colKeyLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));
    this.add(colFuncLbl, new GridBagConstraints(startCol, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));
    return startLine;
  }

  /** @see addHeader1(int, int) */
  private int addHeader2 (int startLine, int startCol) {
    //                                             x          y
    this.add(colKeyLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));
    this.add(colFuncLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));
    this.add(colChatMesgLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));
    this.add(colChatCommitLbl, new GridBagConstraints(startCol++, startLine,1,1,0.0,0.0,
                                               GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                               new Insets(0,0,0,0),
                                               0,0));

    return startLine;
  }

   private JTextField createHotKeyEditField(HotKey k) {
    final JTextField keyedit = new JTextField(KEY_FIELD_SIZE);
      keyedit.addKeyListener (new AbstractHotKeyListener(k){
        public void doStuff (KeyEvent e, int hotKeyCode) {
          String oldText = keyedit.getText();
          CAT.debug("oldText="+oldText);
          HotKey hk = getHotKey();
          try {
            hk.setKeyCode(new Integer(hotKeyCode));
            keyman.setHotKey(hk);
            if (e.isActionKey())
              keyedit.setText(hk.getKeyText());
            else
              keyedit.setText(e.getKeyChar()+"");
          }
          catch (KeyReserved kr) {
             CAT.debug("reserved key!");
             keyedit.setText(oldText);
          }
        }
      });
      // hack! funzt nur teilweise
      char c = (char)k.getKeyCode();
      if (Character.isISOControl(c))
           keyedit.setText(k.getKeyText());
      else
        keyedit.setText(new Character(c).charValue()+"");


      return keyedit;
  }


}