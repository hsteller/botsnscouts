package de.botsnscouts.gui.hotkey;

import java.awt.event.*;

import org.apache.log4j.Category;

public abstract class AbstractHotKeyListener implements KeyListener {
  private static Category CAT = Category.getInstance(AbstractHotKeyListener.class);

  private String keyName;

  public AbstractHotKeyListener(){
    this (null);
  }

  public AbstractHotKeyListener(String keyName){
    this.keyName = keyName;
  }

  public void keyTyped(KeyEvent e) {
     if (CAT.isDebugEnabled()) {
      CAT.debug("KEYTyped!");
      CAT.debug(HotKey.dumpEvent(e));
     }
      doStuff(e, e.getKeyChar());
  }

  public void keyPressed(KeyEvent e){
     if (CAT.isDebugEnabled()) {
       CAT.debug("KEYPressed!");
       CAT.debug(HotKey.dumpEvent(e));
     }
     doStuff(e, e.getKeyCode());
  }

  public void keyReleased(KeyEvent e) {}

  public String getKeyName() {
    return keyName;
  }

/*  public HotKey getHotKey() {
    return key;
  }

  public void setHotKey(HotKey key) {
    this.key = key;
  }
*/
  public abstract void doStuff (KeyEvent e, int hotkeyCode);

}