package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;
import de.botsnscouts.util.*;

class ThumbsCellRenderer extends JPanel implements ListCellRenderer {

    JLabel image = new JLabel();
    JLabel text = new JLabel();
    Border selectedBorder = new MatteBorder( 3,3,3,3,Color.green.darker() );
    Border normalBorder = new EmptyBorder( 3,3,3,3 );

    public ThumbsCellRenderer () {
	setOpaque( false );
	image.setOpaque( false );
	text.setOpaque( false );
	text.setBackground(Color.black);
	text.setForeground(Color.green.darker());
	setLayout( new BorderLayout() );
	add( image, BorderLayout.CENTER );
	add( text, BorderLayout.SOUTH );
	//setBackground( Color.black );
	//setForeground( Color.green );
    }
    
    public Component getListCellRendererComponent(
						  JList list,
						  Object value,            // value to display
						  int index,               // cell index
						  boolean isSelected,      // is the cell selected
						  boolean cellHasFocus)    // the list and the cell have the focus
    {	
	String name=value.toString().substring(0,value.toString().indexOf(".rra"));
	setOpaque(false);
	text.setText(name);
	image.setIcon(new ImageIcon(((TileInfo)value).getImage()));
	if (isSelected) {
	    text.setText(name.toUpperCase());
	    image.setBorder( selectedBorder );
	}
	else {
// 	    setBackground(list.getBackground());
// 	    setForeground(list.getForeground());
	    text.setText(name);
	    image.setBorder( normalBorder );
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	return this;
    }

}


