package de.spline.rr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;

public class StartStart extends JPanel implements  ActionListener, MouseListener{
    Paint paint;
    Start parent;
    Thread thread;

    JLabel angem;
    StartStartAnmeldung anmeldung;
    StartStartOkZur unten;
    StartStartKS ks;
    StartStartMS ms;
    StSpListener listen;

    public StartStart(Start par){
	parent=par;
	Font font=new Font("Sans", Font.BOLD, 24);

	BorderLayout lay=new BorderLayout();

	setLayout(lay);
	setBorder(new EmptyBorder(50,50,50,50));
	setOpaque( false );
// 	anmeldung
// 	unten
//      ks

	angem=new JLabel(Message.say("Start","mAngem"));
	anmeldung=new StartStartAnmeldung(parent);
	unten=new StartStartOkZur(parent);
        ks=new StartStartKS(parent);
        ms=new StartStartMS(parent);
	listen=new StSpListener(anmeldung);
	listen.start();

	angem.setFont(font);
	//angem.setForeground(Color.lightGray);
	JPanel p = new JPanel(new BorderLayout());
	p.setOpaque( false );
	p.add( angem, BorderLayout.NORTH );
	p.add( anmeldung, BorderLayout.CENTER );
	//add(BorderLayout.NORTH,angem);
	add(BorderLayout.WEST,p);
	add(BorderLayout.SOUTH,unten);
	JPanel panel=new TJPanel();

	panel.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	gc.anchor = GridBagConstraints.NORTH;
	gc.fill = GridBagConstraints.BOTH;
	gc.gridx = 0;
	gc.gridy = GridBagConstraints.RELATIVE;
	gc.insets = new Insets(30, 30, 30, 30);
	
	panel.add(ks, gc);
	panel.add(ms, gc);
	add(BorderLayout.EAST,panel);

	URL url = getClass().getResource(Message.say("Start","mBG"));
	ImageIcon icon = new ImageIcon( url );
	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
	Graphics g = bgimg.getGraphics();
	icon.paintIcon(this, g, 0,0);
	g.dispose();
	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
	paint = new TexturePaint( bgimg, anchor );
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

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    public void setThreadToWait(Thread th){
	thread=th;
    }

}//class StartStart end
