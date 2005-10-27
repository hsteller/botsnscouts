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

import javax.swing.KeyStroke;

public class HotKey {


  private KeyStroke key;
  private HotKeyAction action;
  private String keyName;


   public HotKey (String keyName, KeyStroke key,  HotKeyAction action) {
    this.key = key;
    this.keyName = keyName;
    this.action = action;
    action.setDescription(HotKeyConf.getDescription(keyName));
    action.setOptionalValues(HotKeyConf.getOptinalValues(keyName));

  }

  public HotKey (String keyName, HotKeyAction action) {
    this(keyName, HotKeyConf.getKeyStroke(keyName),action);
  }




  protected KeyStroke getKeyStroke(){
      return key;
  }
  
  protected void setKeyStroke(KeyStroke keyS) throws KeyReserved{
      if (keyS == null)
        return;
      if (HotKeyConf.isReserved(keyS))
        throw new KeyReserved();
      this.key= keyS;
    // HotKeyConf.setHotKey(this);

  }

  public void executeAction() {
    action.actionPerformed(null);
  }

  public HotKeyAction getAction(){
    return action;
  }

  protected void setAction(HotKeyAction action){
    String [] values = action.getOptionalValues();
    this.action = action;
   // HotKeyConf.setOptionalValues(keyName, action.getOptionalValues());
    HotKeyConf.setHotKey(this);
  }

  public String getName() {
    return keyName;
  }

  public String getKeyText (){
    //hack! strange KeyEvents (no text for Enter) => crappy HotKeys :-(
    // ?? setting keytext at HotKey creation better??
    if (keyName.equals(HotKeyConf.HOTKEY_SHOW_CHATLINE))
      return HotKeyConf.SHOW_CHATLINE_TEXT;
    else
      return KeyEvent.getKeyText(key.getKeyCode());

  }

  public String toString() {
    StringBuffer sb = new StringBuffer("Hotkey: code= ").append(key);
    sb.append("   name=").append(keyName);
    sb.append("  action=").append(action.toString());
    sb.append("  optional=");
    String [] opts = action.getOptionalValues();
    if (opts != null)
      for (int i=0;i<opts.length;i++)
        sb.append(opts[i]).append(',');
    return sb.toString();
  }

  /** FIXME move elsewhere */
  public static String dumpEvent(java.awt.event.KeyEvent e) {
      StringBuffer sb = new StringBuffer(200);
      sb.append("KEYEVENT\n keychar: "+e.getKeyChar()+"\nkeycode: "+e.getKeyCode()
                +"\nnumValue: "+Character.getNumericValue(e.getKeyChar()));

      int mods = e.getModifiers();
      String ms = KeyEvent.getKeyModifiersText(mods);
      sb.append("\nmods="+mods+"\tmodString="+ms);
      sb.append("\nkeyText="+KeyEvent.getKeyText(e.getKeyCode()));
      sb.append("\nID="+e.getID());
      sb.append("\nparamString="+e.paramString());
      sb.append("\nconsumed?"+e.isConsumed());
      sb.append("\nactionKey?"+e.isActionKey());
      javax.swing.KeyStroke stroke = javax.swing.KeyStroke.getKeyStrokeForEvent(e);
      sb.append("\nstroke stuff: ");
      sb.append("\n\t"+stroke.getKeyChar());
      sb.append("\n\t"+stroke.getKeyCode());
      sb.append("\n\tchar#:"+Character.getNumericValue(stroke.getKeyChar()));
      sb.append("\n\tSTROKE="+stroke.toString());
      return sb.toString();

   }

}

