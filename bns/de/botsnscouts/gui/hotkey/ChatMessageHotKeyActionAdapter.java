package de.botsnscouts.gui.hotkey;

import javax.swing.JTextField;

public class ChatMessageHotKeyActionAdapter extends HotKeyAction {

  JTextField editField;

  public ChatMessageHotKeyActionAdapter (String description, JTextField messageEditor) {
      super (description, messageEditor, messageEditor.getText());
      editField = messageEditor;
  }

   public ChatMessageHotKeyActionAdapter (JTextField messageEditor) {
      super (messageEditor, messageEditor.getText());
      editField = messageEditor;
  }

  public String getOptionalValue() {
    return editField.getText();
  }


  public void execute() {

  }
}