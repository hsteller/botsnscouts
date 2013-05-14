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

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.apache.log4j.Category;

@SuppressWarnings("serial")
public abstract class HotKeyAction extends AbstractAction {

    Category CAT = Category.getInstance(HotKeyAction.class);

    private String description;

    private String optionalValues[];

    private JComponent optionalComponents[];

    public HotKeyAction() {
    }

    public HotKeyAction(String description, JComponent[] optionalComponent, String[] optionalValues) {

        this.description = description;
        this.optionalComponents = optionalComponent;
        this.optionalValues = optionalValues;
    }

    public HotKeyAction(JComponent[] optionalComponent, String[] optionalValues) {
        this(null, optionalComponent, optionalValues);

    }

    public JComponent[] getOptionalComponents() {
        return optionalComponents;
    }

    public String[] getOptionalValues() {
        return optionalValues;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String desc) {
        if (this.description == null)
            this.description = desc;
        else
            CAT.warn("may not set HotKey description twice!");
    }

    protected void setOptionalValues(String[] values) {
        optionalValues = values;
    }

}
