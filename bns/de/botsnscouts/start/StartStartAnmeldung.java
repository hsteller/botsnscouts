package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import java.util.*;
import de.botsnscouts.util.*;
import de.botsnscouts.gui.*;

public class StartStartAnmeldung extends JPanel implements  ActionListener, MouseListener{
    JList roblist;
    Start parent;
    Vector names = new Vector();
    Hashtable map = new Hashtable();

    public StartStartAnmeldung(Start par){
	parent=par;
	roblist = new JList();
	roblist.setOpaque( false );
	//	roblist.setFixedCellHeight(64);
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

    public void reset(){
    }

    public void actionPerformed(ActionEvent e){
    }

    public void mouseEntered(MouseEvent e){
	
    }

    public void mouseExited(MouseEvent e){

    }


    public void mouseClicked(MouseEvent e){

    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    public void neurob(String name, int farbe){
	map.put( name, new Integer(farbe) );
	Global.debug(this,"neuer roboter:"+name+SACanvas.robocolor[farbe]);
	/*
	JLabel tmp=new JLabel(name);
	tmp.setForeground(SACanvas.robocolor[farbe]);
	tmp.setFont(new Font("Sans", Font.BOLD, 24));
	add(tmp);
	*/
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

	ImageIcon[] robIcons = new ImageIcon[8];
	Dimension size;
	CellRenderer() {
	    Image[] robbis = ImageMan.getImages(ImageMan.ROBOS);
	    for (int i=0; i<8;i++) {
		robIcons[i]=new ImageIcon(robbis[i*4]);
	    }
	    //	 size=new Dimension(robIcons[0].getIconWidth(),robIcons[0].getIconHeight());
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
	    setIcon( robIcons[farbe] );
	    /*
	      if (isSelected) {
	      setBackground(list.getSelectionBackground());
	      setForeground(list.getSelectionForeground());
	      }
	      else {
	      setBackground(list.getBackground());
	      setForeground(list.getForeground());
	      }
	      setEnabled(list.isEnabled());
	    */
	    this.setFont(list.getFont());
	    this.setOpaque( false );
	    return this;
	}
    }
}
