package de.botsnscouts.gui.hotkey;

import javax.swing.*;

public class ChatMessageHotKeyActionAdapter extends HotKeyAction {

  ChatMessageEditor editor;

  public ChatMessageHotKeyActionAdapter (String description, ChatMessageEditor editor) {
      super (description, editor, new String [] {editor.getMessage(), editor.isAutoCommit()+""});
      this.editor = editor;
  }

   public ChatMessageHotKeyActionAdapter (ChatMessageEditor editor) {
      super (editor, new String [] {editor.getMessage(), editor.isAutoCommit()+""});
      this.editor = editor;
  }

  /** should not be called, I think.
   *  getOptionalComponent().getMessage()/isAutoCommit() seems better to me
   */
  public String [] getOptionalValues() {
    return new String [] {editor.getMessage(), editor.isAutoCommit()+""};
  }


  public boolean isAutoCommit()  {
    return editor.isAutoCommit();
  }

  public String getMessage() {
    return editor.getMessage();
  }

  public void execute() {

  }
}