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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.util.*;
import de.botsnscouts.util.*;
import de.botsnscouts.gui.*;


class PlayersPanel extends JPanel{
    JList roblist;
    Start parent;
    Vector names = new Vector();
    Hashtable map = new Hashtable();

    public PlayersPanel(Start par){
	parent=par;
	roblist = new JList();
	roblist.setOpaque( false );
	roblist.setFixedCellWidth(250);
	roblist.setSize(100, 100);
	roblist.setOpaque( false );
	roblist.setBorder( new EtchedBorder(4) );
	roblist.setFont(new Font("Sans", Font.BOLD, 24));
	roblist.setCellRenderer( new CellRenderer() );

	JPanel p = new JPanel();
	p.setOpaque( false );

	JScrollPane sp = new JScrollPane
	    ( roblist,
	      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
	sp.setSize( 200, 8*25+20 );
	sp.setOpaque( false );
	p.add(sp);
	p.setBorder( new CompoundBorder( new EtchedBorder(8),
				       new EmptyBorder(10, 10, 10, 10) ));


	add(p);
	setOpaque(false);
    }

    public void neurob(String name, int farbe){
	map.put( name, new Integer(farbe) );
	Global.debug(this,"neuer roboter:"+name+SACanvas.robocolor[farbe]);
	names.addElement( name );
	roblist.setListData( names );
	parent.show();
    }

    public void spZE(){
	parent.wth.beende();
	Global.debug(this,"Spiel ist zu Ende");
    }

    public void spGL(){
	Global.debug(this,"Spiel geht los");
	parent.beenden();
    }


    class CellRenderer extends JLabel implements ListCellRenderer {
	Dimension size;
	CellRenderer() {
	    size=new Dimension(200,48);
	}

	public Component getListCellRendererComponent
	    (
	     JList list,
	     Object value,            // value to display
	     int index,               // cell index
	     boolean isSelected,      // is the cell selected
	     boolean cellHasFocus)    // the list and the cell have the focus
	{
	    String name = (String) value;

	    setText( name );
	    int farbe = ((Integer)map.get( name )).intValue();
	    setIcon( MyCellRenderer.robIcons[farbe] );
	    this.setFont(list.getFont());
	    this.setOpaque( false );
	    return this;
	}
    }
}
