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
import de.botsnscouts.util.*;

public class WatchPanel extends JPanel implements  ActionListener, MouseListener{

    JLabel server;    
    //JLabel port;    

    JTextField serv;
    //JTextField prt;

    TransparentButton go;
    TransparentButton zurueck;

    Start parent;

    Paint paint;

    Font font;

    public WatchPanel(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mZuschauen"));
	paint=parent.paint;

	String[] farben={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	font=new Font("Sans", Font.BOLD, 24);

	GridLayout lay;
	lay=new GridLayout(2,2);
	lay.setHgap(170);
	lay.setVgap(200);

	setLayout(lay);
	setBorder(new EmptyBorder(200,100,200,100));
	
	server=new JLabel(Message.say("Start","mServer"));
	//port=new JLabel(Message.say("Start","mPort"));
        serv=new JTextField(Message.say("Start","mServerInh"));
        //prt=new JTextField(Message.say("Start","mPortInh"));
        go=new TransparentButton(Message.say("Start","mGoButton"));
        zurueck=new TransparentButton(Message.say("Start","mZurueckButton"));

	server.setFont(font);
	//port.setFont(font);
        serv.setFont(font);
        //prt.setFont(font);
        serv.setOpaque(false);
        //prt.setOpaque(false);

	server.setForeground(Color.lightGray);
	//port.setForeground(Color.lightGray);

        go.addActionListener(this);
        zurueck.addActionListener(this);

        go.setActionCommand("go");
        zurueck.setActionCommand("zurueck");

	add(server);
        add(serv);
	//add(port);
        //add(prt);
        add(go);
        add(zurueck);


// 	URL url = getClass().getResource(Message.say("Start","mBG"));
// 	ImageIcon icon = new ImageIcon( url );
// 	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
// 	Graphics g = bgimg.getGraphics();
// 	icon.paintIcon(this, g, 0,0);
// 	g.dispose();
// 	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
// 	paint = new TexturePaint( bgimg, anchor );

    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("go")){
//	    System.out.println(farb.getSelectedIndex());
	    int portnr=8077;
	    /*try{
		portnr=Integer.parseInt(prt.getText());
	    }catch (Exception x){
		System.err.println("Must be a number!");
		return;
		}*/
	    Thread ausgabe=parent.fassade.einemSpielZuschauen(serv.getText(),portnr);
	    parent.addKS(ausgabe);
	    parent.hide();
	    parent.dispose();
	    parent.beenden();
	}else if(e.getActionCommand().equals("zurueck")){
	    parent.showMainMenu();
	}

    }

    public void mouseEntered(MouseEvent e){
	
    }

    public void mouseExited(MouseEvent e){

    }


    public void mouseClicked(MouseEvent e){

    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

}//class StartTeilZusch end
