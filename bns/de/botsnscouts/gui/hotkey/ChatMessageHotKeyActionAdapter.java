package de.botsnscouts.gui.hotkey;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

public class ChatMessageHotKeyActionAdapter extends HotKeyAction {

  // not necessary anymore, could be replaced by single components
  ChatMessageEditor editor;

  public ChatMessageHotKeyActionAdapter (String description, ChatMessageEditor editor) {
      super (description, editor.getEditComponents(), new String [] {editor.getMessage(), editor.isAutoCommit()+""});
      this.editor = editor;
  }

   public ChatMessageHotKeyActionAdapter (ChatMessageEditor editor) {
      super (editor.getEditComponents(), new String [] {editor.getMessage(), editor.isAutoCommit()+""});
      this.editor = editor;
  }

  /** should not be called, I think.
   *  getOptionalComponent().getMessage()/isAutoCommit() seems better to me
   */
  public String [] getOptionalValues() {
    return editor.getValues();
  }

  public JComponent []  getOptionalComponents() {
    return editor.getEditComponents();
  }

  public ChatMessageEditor getEditor() {
    return editor;
  }


  public boolean isAutoCommit()  {
    return editor.isAutoCommit();
  }

  public String getMessage() {
    return editor.getMessage();
  }
/** Override this */
  public void actionPerformed(ActionEvent act) {

  }
}