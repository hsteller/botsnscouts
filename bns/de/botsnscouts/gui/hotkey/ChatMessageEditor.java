package de.botsnscouts.gui.hotkey;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJCheckBox;
import de.botsnscouts.widgets.TJTextField;
import org.apache.log4j.Category;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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


  private ChatMessageEditor(String message, boolean autoCommit, HotKeyMan keyman, String hotkeyID) {
    this(keyman, hotkeyID);
    messageField.setText(message);
    autoCommitBox.setSelected(autoCommit);
  }

  private ChatMessageEditor(HotKeyMan keyman, String hotkeyID) {
    messageField = new TJTextField("");
    autoCommitBox = new TJCheckBox("", true);
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
  

  public static ChatMessageEditor createEditorForMessage (String propertyNameOfMessage, HotKeyMan keyMan){
      	ChatMessageEditor editPanel;  
      	String [] s = HotKeyConf.getOptinalValues(propertyNameOfMessage);
		  if (s == null || s.length==0) { 
		      // no message properties or even message found 
		      // => creating Panel with empty message-Textfield and autocommit-Checkbox
		    CAT.debug("no message properties found");
		    editPanel = new ChatMessageEditor(keyMan, propertyNameOfMessage);
		  }
		  else if (s.length==1){
		     // found only one message but nothing about autocommit
		     // => textfield will be filled, autocommit will be unchecked
		    CAT.debug("found chatmessage: "+s[0]);
		    CAT.debug("did not find autoCommit");
		    editPanel = new ChatMessageEditor(s[0], false,
		                keyMan, propertyNameOfMessage);
		  }
		  else { //s.length>1
		     // found all informations for the message => 
		      // creating Textfield and autocommit-Box accordingly           
		    CAT.debug("found chatmessage: "+s[0]);
		    CAT.debug("found autoCommit property: "+s[1]);
		    editPanel = new ChatMessageEditor(s[0], new Boolean(s[1]).booleanValue(),
		                keyMan, propertyNameOfMessage);
		  }
		  return editPanel;
  }


}