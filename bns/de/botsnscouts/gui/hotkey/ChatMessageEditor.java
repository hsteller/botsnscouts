package de.botsnscouts.gui.hotkey;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;


/**  Contains a JTextField and a JCheckbox for editing the ChatMessage and the
 *   autocommit setting of the associated ("Chatmessage"-) hotkey's action.
 *   The Hotkey is referenced by its name.
 *   NOTE: Changes will NOT be saved to bns.config
 *         (saving it here (in the Action-and Documentlistener would result in
 *          saving everytime when a letter of the chatmessage is entered).
 *         The editorpanel that contains/displays the JTextField and JCheckbox
 *         is supposed to save this stuff to disc when it is closing.
 */
public class ChatMessageEditor  {

  static Category CAT = Category.getInstance(ChatMessageEditor.class);

  private static final String commitBoxTooltip = Message.say(HotKeyConf.MESSAGE_BUNDLE_SECTION, "autoCommitBoxTooltip");

  private HotKeyMan keyman;
  private String hotkeyID;


  /** To enter a chatmessage that will be sent using a hotkey*/
  private JTextField messageField;

  /** To set the autocommit value of the prepared message.
   *  If checked, the message will be sent immediatley*/
  private JCheckBox  autoCommitBox;


  public ChatMessageEditor(String message, boolean autoCommit, HotKeyMan keyman, String hotkeyID) {
    this(keyman, hotkeyID);
    messageField.setText(message);
    autoCommitBox.setSelected(autoCommit);
  }

  public ChatMessageEditor(HotKeyMan keyman, String hotkeyID) {
    messageField = new JTextField();
    autoCommitBox = new JCheckBox();
  }


  public boolean isAutoCommit(){
    return autoCommitBox.isSelected();
  }

  public String getMessage(){
    return messageField.getText();
  }

  public String [] getValues(){
    return new String [] {messageField.getText(), ""+autoCommitBox.isSelected()};
  }

  public JComponent [] getEditComponents() {
    return new JComponent [] {messageField, autoCommitBox};
  }

  private void updateKey(){
      HotKey key = keyman.getHotKeyByName(hotkeyID);
      key.getAction().setOptionalValues(new String [] {messageField.getText(), autoCommitBox.isSelected()+""});

  }


  private void initListeners(){
    autoCommitBox.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          updateKey();
        }
    });
    messageField.getDocument().addDocumentListener(new DocumentListener(){
      public void changedUpdate(DocumentEvent e){
        CAT.debug("changed update");
        updateKey();
      }
      public void insertUpdate(DocumentEvent e){
        CAT.debug("insert update");
        updateKey();
      }
      public void removeUpdate(DocumentEvent e){
        CAT.debug("remove update");
      }

    });
  }

}