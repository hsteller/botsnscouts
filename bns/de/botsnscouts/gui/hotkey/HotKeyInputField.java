/*
 * Created on 15.10.2004
 */
package de.botsnscouts.gui.hotkey;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Category;

/**
 * @author hendrik
 */
public class HotKeyInputField extends JTextField {
    Category CAT = Category.getInstance(HotKeyInputField.class);
   
    private String keyname;
    private HotKeyMan keyman;
    
    public HotKeyInputField (HotKey hotkey, int size, HotKeyMan keyman){
        super(size);
        this.keyman = keyman;
        KeyStroke stroke = hotkey.getKeyStroke();     
        keyname = hotkey.getName();        
        createHotKeyEditField(hotkey);
    }
    
    private void createHotKeyEditField(HotKey k) {
        	 KeyListener listener = new KeyAdapter(){	           
	              public void keyPressed(KeyEvent e){        
	                  handleEvent(e);
	              }     
	              public void keyTyped(KeyEvent e){             
	                  e.consume();
	                  //handleEvent(e);
	              }
              
	              private synchronized void handleEvent(KeyEvent e) {
	                  int newKeycode = e.getKeyCode();
	                  KeyStroke newKeystroke = KeyStroke.getKeyStrokeForEvent(e);	                  	                  
	                  String oldText = HotKeyInputField.this.getText();
	                  HotKeyInputField.this.setText("");
	                  CAT.debug("oldText=" + oldText);
	                  try {	                      	                  
                            boolean valueChanged = keyman.updateHotkey(keyname, newKeystroke);
                            if (valueChanged){           
                                String text  = KeyEvent.getKeyText(newKeycode);
                                /*// <hack alert>                                                       
                                String s = text.toLowerCase();
                                if (s.startsWith("unknown"))  // inefficient & nothing to be proud of..
                                      text=""+((char)newKeycode);
                                else if (s.equals("minus"))
                                    	text= "-";
                                //</hack alert>                                
                               */
                                 HotKeyInputField.this.setText(text);
                                
                            }                                                           
                            else {
                                HotKeyInputField.this.setText(oldText);
                            }
                        //CAT.debug(keyman.dump());
	                  }
	                  catch (KeyReserved kr) {
	                        CAT.debug("reserved key!");
	                        HotKeyInputField.this.setText(oldText);
	                  }
                }         
        	 };
         this.addKeyListener(listener);
    }

}
