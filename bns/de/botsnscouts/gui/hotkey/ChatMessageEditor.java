package de.botsnscouts.gui.hotkey;

import javax.swing.*;

import de.botsnscouts.util.Message;

public class ChatMessageEditor extends JPanel {

  private static final String commitBoxTooltip = Message.say(HotKeyConf.MESSAGE_BUNDLE_SECTION, "autoCommitBoxTooltip");

  private JLabel messageLabel = new JLabel(Message.say(HotKeyConf.MESSAGE_BUNDLE_SECTION, "editMessageLabel"));
  private JLabel comitLabel   = new JLabel(Message.say(HotKeyConf.MESSAGE_BUNDLE_SECTION, "autoCommitBoxLabel"));
  JTextField messageField;
  JCheckBox  autoCommitBox;

  public ChatMessageEditor(JTextField message, JCheckBox autoCommit) {
      messageField = message;
      autoCommitBox = autoCommit;
      if (autoCommitBox != null)
        autoCommitBox.setToolTipText(commitBoxTooltip);
  }

  public ChatMessageEditor(String message, boolean autoCommit) {
    this();
    messageField.setText(message);
    autoCommitBox.setSelected(autoCommit);
  }

  public ChatMessageEditor() {
    this (new JTextField(), new JCheckBox());
  }


  public boolean isAutoCommit(){
    return autoCommitBox.isSelected();
  }

  public String getMessage(){
    return messageField.getText();
  }

}