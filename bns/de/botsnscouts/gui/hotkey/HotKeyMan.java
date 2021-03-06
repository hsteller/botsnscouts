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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import org.apache.log4j.Category;

public class HotKeyMan {
    static Category CAT = Category.getInstance(HotKeyMan.class);

    // private HashMap keycodeToHotkey = new HashMap();
    private HashMap<String, HotKey> keynameToHotKey = new HashMap<String, HotKey>();

    private InputMap inputMap;

    private ActionMap actionMap;

    public HotKeyMan(InputMap inputMap, ActionMap actionMap) {
        this.inputMap = inputMap;
        this.actionMap = actionMap;
    }

    /**
     * Updates
     * 
     * @param keyName
     *            The internal name of an Hotkey as given by a constant in
     *            HotKeyConf
     * @param newStroke
     *            The actual Key(Stroke) that triggers the HotKey('s Action)
     * @return false, if there is key bound to newStroke or there is no key that
     *         matches keyName
     * @throws KeyReserved
     *             if newStroke must not be used as HotKey (for example: Enter
     *             is currently reserved for activating the ChatLine)
     */
    public synchronized boolean updateHotkey(String keyName, KeyStroke newStroke) throws KeyReserved {
        HotKey k = keynameToHotKey.get(keyName);
        if (k != null) {
            KeyStroke oldStroke = k.getKeyStroke();
            synchronized (inputMap) {
                // o = key that is bound to newStroke (if it exists)
                Object o = inputMap.get(newStroke);
                if (o != null) { // don't update if a key is already bound to
                                 // newStroke
                    return false;
                }
                else { // ok,no key is bound to newStroke
                    k.setKeyStroke(newStroke); // will throw a "KeyReserved" if
                                               // the new KeyStroke must not be
                                               // used
                    inputMap.remove(oldStroke);
                    inputMap.put(newStroke, keyName);
                    HotKeyConf.setHotKey(k); // save the now changed
                                             // configuration
                    // keycodeToHotkey.remove(new
                    // Integer(oldStroke.getKeyCode()));
                    return true;
                }
            }
        }
        else {
            return false;
        }

    }

    /**
     * Adds a binding for the given HotKey. NOTE: the binding for the added key
     * will not be saved to the configuration(automatically); to save the
     * binding one has to call {@link: save() }or {@link: updateHotkey(String,
     * KeyStroke)}
     * 
     * @param key
     *            the key that should to be added/bound
     */
    public synchronized void addHotKey(HotKey key) {
        CAT.debug("adding hotkey: " + key.toString());

        String keyname = key.getName();
        keynameToHotKey.put(keyname, key);
        synchronized (inputMap) {
            inputMap.put(key.getKeyStroke(), keyname);
        }
        synchronized (actionMap) {
            actionMap.put(keyname, key.getAction());
        }

    }

    /*
     * public synchronized void invoke (int keyCode) {
     * CAT.debug("invoking code: "+keyCode); invoke ( new Integer(keyCode)); }
     * 
     * public synchronized void invoke (Integer keyCode) { long start =
     * System.currentTimeMillis(); HotKey k = (HotKey) hotkeys.get(keyCode);
     * CAT.
     * debug("invoking code: "+keyCode==null?null:keyCode.intValue()+"("+keyCode
     * .byteValue()+")");
     * 
     * if ( k != null ) k.executeAction(); long end =
     * System.currentTimeMillis(); long total = end - start;
     * CAT.debug("needed "+total+"ms for executing Hotkey-action"); }
     */

    public synchronized void invoke(KeyStroke stroke) {

        Object keyname;
        synchronized (inputMap) {
            keyname = inputMap.get(stroke);
        }
        if (keyname != null) {
            Action action;
            synchronized (actionMap) {
                action = actionMap.get(keyname);
            }
            if (action != null) {
                ActionEvent dummyEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, stroke.toString());
                action.actionPerformed(dummyEvent);
            }
        }
    }

    public synchronized HotKey[] getHotKeys() {
        Set<String> keys = keynameToHotKey.keySet();
        int size = keys.size();
        HotKey[] back = new HotKey[size];
        Iterator<String> it = keys.iterator();
        for (int i = 0; i < size; i++) {
            back[i] = keynameToHotKey.get(it.next());
        }
        return back;
    }

    /*
     * public synchronized HotKey getHotKey(int keyCode) { return (HotKey)
     * hotkeys.get(new Integer(keyCode)); }
     * 
     * public synchronized HotKey getHotKey(Integer keyCode) { return (HotKey)
     * hotkeys.get(keyCode); }
     */
    public synchronized HotKey getHotKeyByName(String keyName) {
        return keynameToHotKey.get(keyName);

    }

    public synchronized void save() {
        for (HotKey key : keynameToHotKey.values()) {
            HotKeyConf.setHotKey(key, false);
        }
        HotKeyConf.save();
    }

    public synchronized String dump() {
        HotKey[] all = getHotKeys();
        StringBuffer sb = new StringBuffer("HOTKEYS:\n");
        for (int i = 0; i < all.length; i++)
            sb.append(all[i].toString()).append("\n");
        return sb.toString();
    }

}
