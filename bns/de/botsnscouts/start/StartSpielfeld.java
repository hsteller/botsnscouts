package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;

public class StartSpielfeld extends JPanel implements  ActionListener{
    Paint paint;

    Start parent;
    StartSpielfeldBut buttons;
    StartSpielfeldOkZur unten;
    StartSpielfeldSpf spf;
    JPanel pnl;
    JScrollPane scrl;

    public StartSpielfeld(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mSpielStarten"));

	buttons=new StartSpielfeldBut(par);
	unten=new StartSpielfeldOkZur(par);
	spf=new StartSpielfeldSpf(par);
	BorderLayout lay=new BorderLayout();
//	GridLayout lay=new GridLayout(2,2);

	setLayout(lay);
	requestFocus();

	scrl=new JScrollPane();
	
	pnl =new JPanel();
	pnl.setLayout(new FlowLayout());
	pnl.setOpaque(false);
	pnl.setBorder(new EmptyBorder(50,50,50,50));
	pnl.add(spf);

	scrl.setOpaque(false);
	scrl.getViewport().setView(pnl);

 	add(BorderLayout.SOUTH,unten);
 	//add(BorderLayout.CENTER,spf);
 	add(BorderLayout.CENTER,scrl);
 	add(BorderLayout.EAST,buttons);


	URL url = getClass().getResource(Message.say("Start","mBG"));
	ImageIcon icon = new ImageIcon( url );
	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
	Graphics g = bgimg.getGraphics();
	icon.paintIcon(this, g, 0,0);
	g.dispose();
	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
	paint = new TexturePaint( bgimg, anchor );

   	spf.rasterChanged();
 }

    public void actionPerformed(ActionEvent e){

    }


    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

}//class StartSpielfeld end
