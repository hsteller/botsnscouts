package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;

public class MyCellRenderer extends JLabel implements ListCellRenderer {

    ImageIcon[] robIcons = new ImageIcon[8];
    String zufall = Message.say("Start", "mFarbeEgal");
    Dimension size;
    boolean withEgal = true;

    MyCellRenderer() {
	this( true );
    }

    MyCellRenderer(boolean wEgal) {
	withEgal = wEgal;
	Image[] robbis = ImageMan.getImages(ImageMan.ROBOS);
	for (int i=0; i<8;i++) {
	    robIcons[i]=new ImageIcon(robbis[i*4]);
	}
	size=new Dimension(96,48);
    }
    public Component getListCellRendererComponent(
						  JList list,
						  Object value,            // value to display
						  int index,               // cell index
						  boolean isSelected,      // is the cell selected
						  boolean cellHasFocus)    // the list and the cell have the focus
    {
	 
	if (index==-1) {
	    index = list.getSelectedIndex();
	    if( index == -1 ) index = 0;
	}
	setPreferredSize( size );
	if(index == 0 && withEgal ) {
	    setText(zufall);
	    setIcon(null);
	    return this;
	}
	 
	setText("");
	if( withEgal )
	    setIcon( robIcons[index-1] );
	else
	    setIcon( robIcons[index] );

	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	}
	else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	return this;
    }



}

