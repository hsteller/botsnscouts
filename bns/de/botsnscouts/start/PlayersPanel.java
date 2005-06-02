/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
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

package de.botsnscouts.start;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import de.botsnscouts.gui.BoardView;
import de.botsnscouts.util.Global;
import de.botsnscouts.widgets.ColoredPanel;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJLabel;


class PlayersPanel extends ColoredPanel {
    private JList roblist;
    Start parent;
    Vector names = new Vector();
    private Hashtable map = new Hashtable();

    public PlayersPanel(Start par) {
        setLayout(new BorderLayout());
        parent = par;
        roblist = new JList();
        roblist.setOpaque(false);
        roblist.setFixedCellWidth(250);
        roblist.setOpaque(false);
        roblist.setFont(new Font("Sans", Font.BOLD, 20));
        roblist.setCellRenderer(new CellRenderer());
        roblist.setFixedCellHeight(64);
        roblist.setBorder(BorderFactory.createEmptyBorder());

        JComponent p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JScrollPane sp = new JScrollPane(roblist, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(OptionPane.niceBorder);
        add(sp, BorderLayout.CENTER);
        setOpaque(false);
    }

    public void newBotEntered(String name, int farbe) {
        map.put(name, new Integer(farbe));
        Global.debug(this, "neuer roboter:" + name + BoardView.ROBOCOLOR[farbe]);
        names.addElement(name);
        Global.debug(this, name+" added to name list");
        roblist.setListData(names);
        Global.debug(this, "name list replaced");
        parent.show();
    }

    public void gameStarted() {
        Global.debug(this, "Spiel geht los");
//      XXX HS 28.05.2005 parent.beenden();
        map.clear();
        names.clear();
        roblist.removeAll();
        
        parent.hide();
    }

    
    

    class CellRenderer extends TJLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name = (String) value;

            setText(name);
            int farbe = ((Integer) map.get(name)).intValue();
            setIcon(RoboCellRenderer.robIcons[farbe]);
            this.setFont(list.getFont());
            this.setOpaque(false);
            return this;
        }
    }
}
