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
