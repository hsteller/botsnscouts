/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/


package de.botsnscouts.gui.hotkey;

import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.KeyStroke;

import org.apache.log4j.Category;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Message;


/** Defines lots of constants that are important for hotkey management
 *  and also for displaying them in the HotKeyEditorPanel.
 */
public class HotKeyConf {
// To add a hotkey:
// (1)  add a constant with its name (see below: declaration of HOTKEY_something)
// (2)  add that constant to one of the group arrays below (probably GROUP_NORMAL)
// (3)  add a description for your hotkey to the HotKeyDescription section in the 
//       MessagesBundle files in botsnscouts.conf, using the value of your constant in (1) as key
// (4)  create a HotKey object, the HotKeyAction that should be executed by the HotKey and 
//       add both to the HotKeyMan (example see: AusgabeView, initHotKeysAndAddToHotkeyman)
// (5)  add a default value to bns.config; REMEMBER TO ADD THE PROPER SUFFIX!!
//       For a simple key (not a chatmessage) the entry should look like: 
//       "value of your key from (1)"+"value of CODE_SUFFIX"=keycode
//                                               
    
  static Category CAT = Category.getInstance(HotKeyConf.class);


  /** String to display for the 'Enter' key that is used to activate the Chatline;
   * Reason: toString() didn't work for the first implementation..TODO test if still necessary 
   * */
  public static final String SHOW_CHATLINE_TEXT = "Enter"; 
  public static final String HOTKEY_SHOW_CHATLINE = "reservedKeyShowChat";

  
  
  private static HashSet reservedKeys;
  static {
             reservedKeys =  new HashSet();
             reservedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
             reservedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0 ));
             reservedKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
  }


  public static final String CODE_SUFFIX = "Code";
  public static final String TEXT_SUFFIX = "Text";

  // The following constants are used to get the key description from the resource bundle files
  
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

  public static final String HOTKEY_TOGGLE_BOT_VISIBILITY = "keyToggleBotVisibility";

  // for easier access in loops:
  public static final String [] HOTKEY_SHOW_FLAG_X = new String [] {
                  HOTKEY_SHOW_FLAG1,HOTKEY_SHOW_FLAG2,HOTKEY_SHOW_FLAG3,
                  HOTKEY_SHOW_FLAG4,HOTKEY_SHOW_FLAG5,HOTKEY_SHOW_FLAG6
  };
  
  
  // Stuff used for ordering the keys in the HotKeyEditorpanel:
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
      HOTKEY_ZOOM_IN, HOTKEY_ZOOM_OUT, HOTKEY_TOGGLE_BOT_VISIBILITY, HOTKEY_SHOW_FLAG1, HOTKEY_SHOW_FLAG2,
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

  /** Loads  the KeyStroke that is saved for the given keyName.
   * 
   * @param keyName the name of the key
   * @return the KeyStroke for the keycode that was saved for the keyname or NULL if no (parsable) keycode was found
   */ 
  protected static KeyStroke getKeyStroke(String keyName){
     
     String [] s = Conf.getMultipleProperty(keyName+CODE_SUFFIX);
     if (s==null || s.length<1){
	      CAT.debug("no values, returing null");
	      return null;
     }
     String codeS = s[0];
     if (codeS==null || codeS.length()==0){
	      CAT.warn ("code for "+keyName+" not found!");
	      return null;
     }     
     try {
         int keycode = Integer.parseInt(codeS);
         KeyStroke back = KeyStroke.getKeyStroke(keycode,0);
         return back;
     }
     catch (NumberFormatException nfe){
         CAT.error(nfe.getMessage(), nfe);
     }
     return null;

  }


  protected static void setHotKey (HotKey k){
    setHotKey(k, true);
  }

  protected static void setHotKey (HotKey k, boolean save){
   KeyStroke stroke  =  k.getKeyStroke();
   if (stroke == null || isReserved(stroke))
      return;
   HotKeyAction act = k.getAction();
   String [] opts = act.getOptionalValues();
   String keyName = k.getName();
   Conf.setProperty(keyName+CODE_SUFFIX, ""+stroke.getKeyCode()); 
   if (opts != null && opts.length>0)
     Conf.setMultipleProperty(keyName+TEXT_SUFFIX, opts);
   if (save)
     save();
  }

  protected static String getDescription (String keyName) {
    return Message.say(MESSAGE_BUNDLE_SECTION, keyName);
  }

  protected static void save(){
    CAT.debug("saving new HotkeyConfiguration");
    Conf.saveProperties();
  }

  public static boolean isReserved (KeyStroke keyStroke){
      return reservedKeys.contains(keyStroke);
  }



}

