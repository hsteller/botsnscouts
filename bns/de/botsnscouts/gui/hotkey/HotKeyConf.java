package de.botsnscouts.gui.hotkey;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Message;

import java.awt.event.KeyEvent;

import org.apache.log4j.Category;

public class HotKeyConf {

  static Category CAT = Category.getInstance(HotKeyConf.class);

  public static final int SHOW_CHATLINE = KeyEvent.VK_ENTER;
  public static final String HOTKEY_SHOW_CHATLINE = "reservedKeyShowChat";

  static {
    Conf.setProperty(HOTKEY_SHOW_CHATLINE, SHOW_CHATLINE+"");
  }

  public static final int [] RESERVED_KEYS = new int [] {
                                                      SHOW_CHATLINE
                                                      };

  public static final String CODE_SUFFIX = "Code";
  public static final String TEXT_SUFFIX = "Text";

  public static final String HOTKEY_MSG1 = "keyMsg1";
  public static final String HOTKEY_MSG2 = "keyMsg2";
  public static final String HOTKEY_MSG3 = "keyMsg3";
  public static final String HOTKEY_MSG4 = "keyMsg4";
  public static final String HOTKEY_MSG5 = "keyMsg5";
  public static final String [] MSGS = {
                                         HOTKEY_MSG1,
                                         HOTKEY_MSG2,
                                         HOTKEY_MSG3,
                                         HOTKEY_MSG4,
                                         HOTKEY_MSG5
                                        };

  public static final String HOTKEY_ZOOM_IN = "keyZoomIn";
  public static final String HOTKEY_ZOOM_OUT = "keyZoomOut";


  public static final String MESSAGE_BUNDLE_SECTION = "HotKeyDescription";

  public static String [] getOptinalValues(String keyName) {
    String []  s = Conf.getMultipleProperty(keyName+TEXT_SUFFIX);
    if ( s == null )
      return new String [] {""};
    else
      return s;
  }

  public static Integer getKeyCode(String keyName){
     String s = Conf.getProperty(keyName+CODE_SUFFIX);
     CAT.debug("loading code: name="+keyName+"\tvalue="+s);
     if (s==null || s.trim().length()<1){
      CAT.debug("returing null");
      return null;
     }
     s = s.trim();
     Integer back=null;
     try {
        back = new Integer(Integer.parseInt(s));

     }
     catch (NumberFormatException ne){
        char c = s.charAt(0);
        back = new Integer(c);
     }
     CAT.debug("returning for code: "+keyName+"\tvalue="+back.intValue());
     return back;

    /*int i = Conf.getIntProperty(keyName+CODE_SUFFIX);
    if (i>-1)
      return new Integer(i);
     else
      return null;
      */
  }

  protected static void setKeyCode (String keyName, Integer i) {
    if (i!=null && isReserved(i.intValue()))
      return;

    Conf.setProperty(keyName+CODE_SUFFIX, (i==null?"":i.toString()));
    save();
  }

  protected static void setOptionalValues(String keyName, String [] values) {
    Conf.setMultipleProperty(keyName+TEXT_SUFFIX, values);
    save();
  }


  public static String getDescription (String keyName) {
    return Message.say(MESSAGE_BUNDLE_SECTION, keyName);
  }

  private static void save(){
    CAT.debug("saving new HotkeyConfiguration");
    Conf.saveProperties();
  }

  public static boolean isReserved (int keyCode){
    for (int i=0;i<RESERVED_KEYS.length; i++)
      if (RESERVED_KEYS[i] == keyCode)
        return true;
    return false;
  }

}