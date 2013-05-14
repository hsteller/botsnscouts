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

import javax.swing.JComponent;
@SuppressWarnings("serial")
public class ChatMessageHotKeyActionAdapter extends HotKeyAction {

    // not necessary anymore, could be replaced by single components
    ChatMessageEditor editor;

    public ChatMessageHotKeyActionAdapter(String description, ChatMessageEditor editor) {
        super(description, editor.getEditComponents(), new String[] { editor.getMessage(), editor.isAutoCommit() + "" });
        this.editor = editor;
    }

    public ChatMessageHotKeyActionAdapter(ChatMessageEditor editor) {
        super(editor.getEditComponents(), new String[] { editor.getMessage(), editor.isAutoCommit() + "" });
        this.editor = editor;
    }

    /**
     * should not be called, I think.
     * getOptionalComponent().getMessage()/isAutoCommit() seems better to me
     */
    public String[] getOptionalValues() {
        return editor.getValues();
    }

    public JComponent[] getOptionalComponents() {
        return editor.getEditComponents();
    }

    public ChatMessageEditor getEditor() {
        return editor;
    }

    public boolean isAutoCommit() {
        return editor.isAutoCommit();
    }

    public String getMessage() {
        return editor.getMessage();
    }

    /** Override this */
    public void actionPerformed(ActionEvent act) {

    }
}
