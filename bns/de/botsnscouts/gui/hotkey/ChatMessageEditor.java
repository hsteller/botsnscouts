package de.botsnscouts.gui.hotkey;

import javax.swing.*;
import java.awt.event.*;

import de.botsnscouts.util.Message;

public class ChatMessageEditor extends JPanel {

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

  // @todo get HotKeyMan-Reference and hotkeyName!
  private void initListeners(){
    autoCommitBox.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {

        }
    });
  }

}