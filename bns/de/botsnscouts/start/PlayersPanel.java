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
import javax.swing.ScrollPaneConstants;

import de.botsnscouts.gui.BoardView;
import de.botsnscouts.util.Global;
import de.botsnscouts.widgets.ColoredPanel;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.TJLabel;

@SuppressWarnings("serial")
class PlayersPanel extends ColoredPanel {

    private JList<String> roblist;

    Start parent;

    Vector<String> names = new Vector<String>();

    private Hashtable<String, Integer> map = new Hashtable<String, Integer>(8);

    public PlayersPanel(Start par) {
        setLayout(new BorderLayout());
        parent = par;
        roblist = new JList<String>();
        roblist.setOpaque(false);
        roblist.setFixedCellWidth(250);
        roblist.setFont(new Font("Sans", Font.BOLD, 20));
        roblist.setCellRenderer(new CellRenderer());
        roblist.setFixedCellHeight(64);
        roblist.setBorder(BorderFactory.createEmptyBorder());

        JComponent p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JScrollPane sp = new JScrollPane(roblist, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        Global.debug(this, name + " added to name list");
        roblist.setListData(names);
        Global.debug(this, "name list replaced");
        parent.setVisible(true);
    }

    public void gameStarted() {
        Global.debug(this, "Spiel geht los");
        // XXX HS 28.05.2005 parent.beenden();
        map.clear();
        names.clear();
        roblist.removeAll();

        parent.setVisible(false);
    }

    class CellRenderer extends TJLabel implements ListCellRenderer<String> {

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String name, int index,
                        boolean isSelected, boolean cellHasFocus) {

            setText(name);
            int farbe = map.get(name).intValue();
            setIcon(RoboCellRenderer.robIcons[farbe]);
            this.setFont(list.getFont());
            this.setOpaque(false);
            return this;
        }
    }
}
