package de.botsnscouts.gui.hotkey;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Message;

import java.awt.event.KeyEvent;

import org.apache.log4j.Category;


/** Defines lots of constants that are important for hotkey management
 *  and also for displaying them in the HotKeyEditorPanel.
 */
public class HotKeyConf {

  static Category CAT = Category.getInstance(HotKeyConf.class);

  /** Necessary because in IBM JDK1.3 getKeyText(VK_ENTER) will
   *  return "Unknown keyCode: 0x0" or some similiar crap.
   *  Interesting: KeyEvent.paramString() will return "*,Enter" anyway..
   *                                ???
   *                              ? o o ?
   *                                 I
   */

  public static final String SHOW_CHATLINE_TEXT = "Enter";
  public static final int SHOW_CHATLINE = KeyEvent.VK_ENTER;
  public static final String HOTKEY_SHOW_CHATLINE = "reservedKeyShowChat";

  static {
    Conf.setProperty(HOTKEY_SHOW_CHATLINE, SHOW_CHATLINE+"");
  }

  public static final int [] RESERVED_KEYS = new int [] {
                                                      SHOW_CHATLINE,
                                                      KeyEvent.VK_BACK_SPACE,
                                                      KeyEvent.VK_SPACE
                                                      };

  public static final String CODE_SUFFIX = "Code";
  public static final String TEXT_SUFFIX = "Text";

  public static final String HOTKEY_MSG1 = "keyMsg1";
  public static final String HOTKEY_MSG2 = "keyMsg2";
  public static final String HOTKEY_MSG3 = "keyMsg3";
  public static final String HOTKEY_MSG4 = "keyMsg4";
  public static final String HOTKEY_MSG5 = "keyMsg5";


  public static final String HOTKEY_ZOOM_IN =  "keyZoomIn";
  public static final String HOTKEY_ZOOM_OUT = "keyZoomOut";
  public static final String HOTKEY_SHOW_FLAG1 = "keyShowFlag1";
  public static final String HOTKEY_SHOW_FLAG2 = "keyShowFlag2";
  public static final String HOTKEY_SHOW_FLAG3 = "keyShowFlag3";
  public static final String HOTKEY_SHOW_FLAG4 = "keyShowFlag4";
  public static final String HOTKEY_SHOW_FLAG5 = "keyShowFlag5";
  public static final String HOTKEY_SHOW_FLAG6 = "keyShowFlag6";


  // Stuff used for ordering the keys in the keys in the HotKeyEditorpanel:
   public static final String [] GROUP_MESSAGES = {
                                         HOTKEY_MSG1,
                                         HOTKEY_MSG2,
                                         HOTKEY_MSG3,
                                         HOTKEY_MSG4,
                                         HOTKEY_MSG5
                                        };
  public static final String [] GROUP_NOT_EDITABLE = new String [] {
                                                             HOTKEY_SHOW_CHATLINE
                                                              };
  public static final String [] GROUP_NORMAL  = new String [] {
      HOTKEY_ZOOM_IN, HOTKEY_ZOOM_OUT, HOTKEY_SHOW_FLAG1, HOTKEY_SHOW_FLAG2,
      HOTKEY_SHOW_FLAG3,HOTKEY_SHOW_FLAG4,HOTKEY_SHOW_FLAG5,HOTKEY_SHOW_FLAG6,
    };

  /** Name of the section in the MessagesBundle files */
  public static final String MESSAGE_BUNDLE_SECTION = "HotKeyDescription";

  public static String [] getOptinalValues(String keyName) {
    String []  s = Conf.getMultipleProperty(keyName+TEXT_SUFFIX);
    if ( s == null )
      return new String [] {""};
    else
      return s;
  }

  protected static Integer getKeyCode(String keyName){
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

  }


  protected static void setKeyCode (HotKey k){
   Integer code =  k.getKeyCodeI();
   if (code!=null && isReserved(code.intValue()))
      return;
   HotKeyAction act = k.getAction();
   String [] opts = act.getOptionalValues();
   int size = (opts==null?0:opts.length);
   String [] all = new String [size+1];
   all[0] = (code==null?" ":code.toString());

   for (int i=2;i<size;i++)
      all[i] = opts[i-2];

   Conf.setMultipleProperty(k.getName()+CODE_SUFFIX, all);
   save();
  }

  protected static String getDescription (String keyName) {
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