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
    //  CAT.debug(HotKey.dumpEvent(e));
     }
     int code = e.getKeyCode();
     // <HACK ALERT>
     if (code == 0) {
      CAT.warn("HACK ALERT! keycode was 0, so Im using ((int)keyChar)+offset as keycode");
      code = e.getKeyChar()+HotKeyConf.MAGIC_OFFSET_HACK;
      CAT.debug("code="+code);
      doStuff(e, code);
     }
     else
      CAT.debug("ignoring key typed, hoping for key pressed..");
     // </HACK ALERT>
    //  doStuff(e, e.getKeyChar());
  }

  public void keyPressed(KeyEvent e){
     if (CAT.isDebugEnabled()) {
       CAT.debug("KEYPressed!");
       CAT.debug("using keycode: "+e.getKeyCode());
     //  CAT.debug(HotKey.dumpEvent(e));
       Object o = e.getSource();
       CAT.debug("source: class="+o.getClass()+"\t"+o.toString());
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